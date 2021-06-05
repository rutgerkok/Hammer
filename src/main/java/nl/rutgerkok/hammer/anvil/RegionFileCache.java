package nl.rutgerkok.hammer.anvil;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterators;
import com.google.common.util.concurrent.UncheckedExecutionException;

import nl.rutgerkok.hammer.util.DirectoryUtil;

/**
 * A simple cache for region files. Ensures that a region file isn't opened
 * twice.
 *
 */
class RegionFileCache {

    class Claim implements Closeable {

        private Claim() {
            // Instantiated only by parent class
        }

        @Override
        public void close() {
            release();
        }
    }

    /**
     * A key used in the cache.
     *
     */
    private static class RegionKey {

        static RegionKey parseFile(Path input) {
            // Assumes a valid file name
            // File names are "r." + regionX + "." + regionZ + "." + FILE_EXTENSION
            String fileName = input.getFileName().toString();
            String[] parts = fileName.split("\\.");
            int regionX = Integer.parseInt(parts[1]);
            int regionZ = Integer.parseInt(parts[2]);
            return new RegionKey(input, regionX, regionZ);
        }

        final Path file;
        final int regionX;
        final int regionZ;

        public RegionKey(Path file, int regionX, int regionZ) {
            this.file = Objects.requireNonNull(file, "file");
            this.regionX = regionX;
            this.regionZ = regionZ;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            RegionKey other = (RegionKey) obj;
            if (!file.equals(other.file)) {
                return false;
            }
            if (regionX != other.regionX) {
                return false;
            }
            if (regionZ != other.regionZ) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + file.hashCode();
            result = prime * result + regionX;
            result = prime * result + regionZ;
            return result;
        }
    }

    private static final String FILE_EXTENSION = "mca";

    /**
     * Cache to prevent the same region file from being opened twice.
     */
    private final LoadingCache<RegionKey, RegionFile> cache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .removalListener(notification -> {
                try {
                    ((RegionFile) notification.getValue()).close();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            })
            .build(new CacheLoader<RegionKey, RegionFile>() {

                @Override
                public RegionFile load(RegionKey key) throws IOException {
                    // Create the region folder if needed
                    if (!Files.exists(key.file.getParent())) {
                        Files.createDirectories(key.file.getParent());
                    }

                    return new RegionFile(key.file, key.regionX, key.regionZ);
                }
            });
    private final AtomicInteger claims = new AtomicInteger();

    private final Map<RegionFileType, Path> folders;

    public RegionFileCache(Path worldFolder) {
        this.folders = new EnumMap<>(RegionFileType.class);
        for (RegionFileType type : RegionFileType.values()) {
            this.folders.put(type, worldFolder.resolve(type.folderName));
        }
    }

    /**
     * Registers a claim. The region cache will stay open until all claims have
     * {@link Claim#close() been closed}.
     *
     * @return The claim.
     */
    Claim claim() {
        claims.incrementAndGet();
        return new Claim();
    }

    /**
     * Counts the amount of region files. This method takes a while to execute
     * and the result is not cached.
     *
     * @return The amount of region files.
     * @throws IOException
     *             If an IO error occurs counting the files.
     */
    int countRegionFiles() throws IOException {
        return DirectoryUtil.countFiles(folders.get(RegionFileType.CHUNK));
    }

    /**
     * Gets the region file that contains the given chunk.
     *
     * @param chunkX
     *            The chunk x.
     * @param chunkZ
     *            The chunk z.
     * @return The region file.
     * @throws IOException
     *             If an IO error occurs reading/creating the region file.
     */
    RegionFile getRegionFile(RegionFileType type, int chunkX, int chunkZ) throws IOException {
        int regionX = chunkX >> 5;
        int regionZ = chunkZ >> 5;
        Path file = folders.get(type).resolve("r." + regionX + "." + regionZ + "." + FILE_EXTENSION);

        try {
            return cache.get(new RegionKey(file, regionX, regionZ));
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            // This code should never be reached
            throw new UncheckedExecutionException(cause);
        }
    }

    // Directory stream is closed by the close method of the returned instance
    DirectoryStream<RegionFile> getRegionFiles() throws IOException {
        final DirectoryStream<Path> files = Files.newDirectoryStream(folders.get(RegionFileType.CHUNK), "*.mca");
        return new DirectoryStream<RegionFile>() {

            @Override
            public void close() throws IOException {
                cache.cleanUp();
                files.close();
            }

            @Override
            public Iterator<RegionFile> iterator() {
                return Iterators.transform(files.iterator(), input -> {
                    try {
                        return cache.get(RegionKey.parseFile(input));
                    } catch (ExecutionException e) {
                        Throwable cause = e.getCause();
                        if (cause instanceof IOException) {
                            throw new UncheckedIOException((IOException) cause);
                        }
                        throw new UncheckedExecutionException(cause);
                    }
                });
            }
        };
    }

    private void release() {
        int claims = this.claims.decrementAndGet();
        if (claims == 0) {
            cache.invalidateAll();
        }
    }
}
