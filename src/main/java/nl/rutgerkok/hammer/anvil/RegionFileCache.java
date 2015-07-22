package nl.rutgerkok.hammer.anvil;

import java.io.Closeable;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import nl.rutgerkok.hammer.util.DirectoryUtil;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

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
    private final Map<Path, Reference<RegionFile>> cache = new HashMap<>();
    private volatile int claims;

    private Object lock = new Object();

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
        claims++;
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

        // Check for cached reference
        Reference<RegionFile> ref = cache.get(file);
        if (ref != null) {
            RegionFile regionFile = ref.get();
            if (regionFile != null) {
                return regionFile;
            }
        }

        // Create the region folder if needed
        if (!Files.exists(regionFolder)) {
            Files.createDirectories(regionFolder);
        }

        return loadRegionFile(file);
    }

    // Directory stream is closed by the close method of the returned instance
    DirectoryStream<RegionFile> getRegionFiles() throws IOException {
        final DirectoryStream<Path> files = Files.newDirectoryStream(regionFolder);
        return new DirectoryStream<RegionFile>() {

            @Override
            public void close() throws IOException {
                files.close();
            }

            @Override
            public Iterator<RegionFile> iterator() {
                return Iterators.transform(files.iterator(), new Function<Path, RegionFile>() {

                    @Override
                    public RegionFile apply(Path input) {
                        return loadRegionFile(input);
                    }
                });
            }
        };
    }

    private RegionFile loadRegionFile(Path file) {
        RegionFile reg = new RegionFile(file);
        cache.put(file, new SoftReference<RegionFile>(reg));
        return reg;
    }

    private void release() {
        claims--;
        if (claims == 0) {
            synchronized (lock) {
                for (Reference<RegionFile> ref : cache.values()) {
                    try {
                        RegionFile region = ref.get();
                        if (region != null) {
                            region.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                cache.clear();
            }
        }
    }
}
