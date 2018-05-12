package nl.rutgerkok.hammer.anvil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import nl.rutgerkok.hammer.ChunkAccess;
import nl.rutgerkok.hammer.anvil.RegionFileCache.Claim;
import nl.rutgerkok.hammer.anvil.tag.AnvilFormat.ChunkRootTag;
import nl.rutgerkok.hammer.anvil.tag.AnvilNbtReader;
import nl.rutgerkok.hammer.anvil.tag.AnvilNbtWriter;
import nl.rutgerkok.hammer.tag.CompoundTag;

/**
 * Provides non-sequential access to the chunks in a world.
 *
 */
final class AnvilChunkAccess implements ChunkAccess<AnvilChunk> {

    private final RegionFileCache cache;
    private final Claim claim;
    private final AnvilGameFactory gameFactory;
    private final ChunkDataVersion chunkDataVersion;

    public AnvilChunkAccess(AnvilGameFactory gameFactory, RegionFileCache cache, ChunkDataVersion chunkDataVersion) {
        this.gameFactory = gameFactory;
        this.cache = cache;
        this.chunkDataVersion = chunkDataVersion;

        this.claim = cache.claim();
    }

    @Override
    public void close() {
        claim.close();
    }

    @Override
    public AnvilChunk getChunk(int chunkX, int chunkZ) throws IOException {
        try (InputStream stream = getChunkInputStream(chunkX, chunkZ)) {
            if (stream == null) {
                // Chunk doesn't exist yet
                return AnvilChunk.newEmptyChunk(gameFactory, chunkDataVersion, chunkX, chunkZ);
            }

            // Read the chunk
            CompoundTag chunkRootTag = AnvilNbtReader.readFromUncompressedStream(stream);
            CompoundTag chunkTag = chunkRootTag.getCompound(ChunkRootTag.MINECRAFT);
            ChunkDataVersion version = ChunkDataVersion.fromId(chunkRootTag.getInt(ChunkRootTag.DATA_VERSION));
            return new AnvilChunk(gameFactory, chunkTag, version);
        }
    }

    private InputStream getChunkInputStream(int chunkX, int chunkZ) throws IOException {
        RegionFile regionFile = cache.getRegionFile(chunkX, chunkZ);
        return regionFile.getChunkInputStream(chunkX & 31, chunkZ & 31);
    }

    private OutputStream getChunkOutputStream(int chunkX, int chunkZ) throws IOException {
        RegionFile regionFile = cache.getRegionFile(chunkX, chunkZ);
        return regionFile.getChunkOutputStream(chunkX & 31, chunkZ & 31);
    }

    @Override
    public void saveChunk(AnvilChunk chunk) throws IOException {
        int chunkX = chunk.getChunkX();
        int chunkZ = chunk.getChunkZ();

        try (OutputStream outputStream = getChunkOutputStream(chunkX, chunkZ)) {
            CompoundTag root = new CompoundTag();
            root.setCompound(ChunkRootTag.MINECRAFT, chunk.getTag());
            AnvilNbtWriter.writeUncompressedToStream(outputStream, root);
        }
    }
}