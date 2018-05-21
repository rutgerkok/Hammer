package nl.rutgerkok.hammer.anvil;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
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

    private static final String FILE_EXTENSION = "mca";

    /**
     * Cache to prevent the same region file from being opened twice.
     */
    private final LoadingCache<Path, RegionFile> cache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .removalListener(notification -> {
                try {
                    ((RegionFile) notification.getValue()).close();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            })
            .build(new CacheLoader<Path, RegionFile>() {

                @Override
                public RegionFile load(Path key) throws IOException {
                    // Create the region folder if needed
                    if (!Files.exists(regionFolder)) {
                        Files.createDirectories(regionFolder);
                    }

                    return new RegionFile(key);
                }
            });

    private final AtomicInteger claims = new AtomicInteger();
    private final Path regionFolder;

    public RegionFileCache(Path regionFolder) {
        this.regionFolder = Objects.requireNonNull(regionFolder, "regionFolder");
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
        return DirectoryUtil.countFiles(regionFolder);
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
    RegionFile getRegionFile(int chunkX, int chunkZ) throws IOException {
        Path file = regionFolder.resolve("r." + (chunkX >> 5) + "." + (chunkZ >> 5) + "." + FILE_EXTENSION);

        try {
            return cache.get(file);
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
        final DirectoryStream<Path> files = Files.newDirectoryStream(regionFolder, "*.mca");
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
                        return cache.get(input);
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
