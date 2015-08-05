package nl.rutgerkok.hammer.anvil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.util.Objects;

import nl.rutgerkok.hammer.anvil.RegionFileCache.Claim;
import nl.rutgerkok.hammer.anvil.tag.AnvilFormat.ChunkRootTag;
import nl.rutgerkok.hammer.anvil.tag.AnvilNbtReader;
import nl.rutgerkok.hammer.anvil.tag.AnvilNbtWriter;
import nl.rutgerkok.hammer.tag.CompoundTag;
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
        try (InputStream stream = region.getChunkInputStream(chunkX, chunkZ)) {
            if (stream == null) {
                return;
            }
            CompoundTag chunkTag = AnvilNbtReader.readFromUncompressedStream(stream).getCompound(ChunkRootTag.MINECRAFT);

            AnvilChunk chunk = new AnvilChunk(gameFactory, chunkTag);
            Result result = visitor.accept(chunk, progress);
            switch (result) {
                case CHANGED:
                    // Save the chunk
                    try (OutputStream outputStream = region.getChunkOutputStream(chunkX, chunkZ)) {
                        CompoundTag root = new CompoundTag();
                        root.setCompound(ChunkRootTag.MINECRAFT, chunk.getTag());
                        AnvilNbtWriter.writeUncompressedToStream(outputStream, root);
                    }
                    break;
                case DELETE:
                    region.deleteChunk(chunkX, chunkZ);
                    break;
                case NO_CHANGES:
                    break;
                default:
                    throw new AssertionError("Unknown result: " + result);
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

        for (int chunkX = 0; chunkX < RegionFile.REGION_CHUNK_COUNT; chunkX++) {
            for (int chunkZ = 0; chunkZ < RegionFile.REGION_CHUNK_COUNT; chunkZ++) {
                handleChunk(progress, visitor, regionFile, chunkX, chunkZ);
            }
        }

    }

}
