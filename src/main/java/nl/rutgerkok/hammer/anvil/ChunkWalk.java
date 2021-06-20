package nl.rutgerkok.hammer.anvil;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.util.Objects;

import nl.rutgerkok.hammer.anvil.RegionFileCache.Claim;
import nl.rutgerkok.hammer.util.Progress;
import nl.rutgerkok.hammer.util.Progress.UnitsProgress;
import nl.rutgerkok.hammer.util.Result;
import nl.rutgerkok.hammer.util.Visitor;

/**
 * Walks along all chunks in the world.
 *
 */
final class ChunkWalk {

    private final AnvilGameFactory gameFactory;
    private final RegionFileCache regionFileCache;

    ChunkWalk(AnvilGameFactory gameFactory, RegionFileCache regionFileCache) {
        this.gameFactory = Objects.requireNonNull(gameFactory, "materialMap");
        this.regionFileCache = Objects.requireNonNull(regionFileCache, "regionFileCache");
    }

    private void handleChunk(Progress progress, Visitor<? super AnvilChunk> visitor,
            RegionFile region, int chunkX, int chunkZ) throws IOException {
        RegionNbtIo regionNbtIo = new RegionNbtIo(ChunkDataVersion.latest(), regionFileCache, chunkX, chunkZ);
        boolean couldReadChunk = false;
        try {
            AnvilChunk chunk = new AnvilChunk(gameFactory, regionNbtIo);
            couldReadChunk = true;
            Result result = visitor.accept(chunk, progress);
            switch (result) {
                case CHANGED:
                    chunk.save();
                    break;
                case DELETE:
                    regionNbtIo.deleteAllDataOfChunk();
                    break;
                case NO_CHANGES:
                    break;
                default:
                    throw new AssertionError("Unknown result: " + result);
            }
        } catch (RuntimeException e) {
            throw new RuntimeException("Runtime error in " + chunkX + " " + chunkZ, e);
        } catch (IOException e) {
            if (couldReadChunk) {
                // Initial read was successful, so there is chunk data
                throw new IOException("IO error in " + chunkX + " " + chunkZ, e);
            } else {
                // There is no chunk data at all, delete from index
                System.err.println("Failed to read " + chunkX + " " + chunkZ + ", deleting chunk.");
                e.printStackTrace(System.err);
                regionNbtIo.deleteTag(RegionFileType.CHUNK); // Remove from index, tag is corrupted
            }
        }
    }

    void performWalk(Visitor<? super AnvilChunk> visitor) throws IOException {
        try (Claim claim = regionFileCache.claim()) {
            UnitsProgress progress = Progress.ofUnits(regionFileCache.countRegionFiles());
            try (DirectoryStream<RegionFile> stream = regionFileCache.getRegionFiles()) {
                for (RegionFile regionFile : stream) {
                    walkRegionFile(progress, visitor, regionFile);
                    progress.increment();
                }
            } catch (DirectoryIteratorException e) {
                // Throw the underlying IOException instead
                throw e.getCause();
            }
        }
    }

    private void walkRegionFile(Progress progress, Visitor<? super AnvilChunk> visitor, RegionFile regionFile) throws IOException {
        int startChunkX = regionFile.getStartChunkX();
        int startChunkZ = regionFile.getStartChunkZ();
        for (int localChunkX = 0; localChunkX < RegionFile.REGION_CHUNK_COUNT; localChunkX++) {
            for (int localChunkZ = 0; localChunkZ < RegionFile.REGION_CHUNK_COUNT; localChunkZ++) {
                if (regionFile.hasChunk(localChunkX, localChunkZ)) {
                    handleChunk(progress, visitor, regionFile, startChunkX + localChunkX, startChunkZ + localChunkZ);
                }
            }
        }

    }

}
