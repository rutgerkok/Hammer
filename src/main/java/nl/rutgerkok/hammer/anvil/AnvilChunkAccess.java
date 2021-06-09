package nl.rutgerkok.hammer.anvil;

import java.io.IOException;

import nl.rutgerkok.hammer.ChunkAccess;
import nl.rutgerkok.hammer.anvil.RegionFileCache.Claim;

/**
 * Provides non-sequential access to the chunks in a world.
 *
 */
final class AnvilChunkAccess implements ChunkAccess<AnvilChunk> {

    private final RegionFileCache cache;
    private final Claim claim;
    private final AnvilGameFactory gameFactory;

    public AnvilChunkAccess(AnvilGameFactory gameFactory, RegionFileCache cache) {
        this.gameFactory = gameFactory;
        this.cache = cache;
        this.claim = cache.claim();
    }

    @Override
    public void close() {
        claim.close();
    }

    @Override
    public void deleteChunk(AnvilChunk chunk) throws IOException {
        new RegionNbtIo(ChunkDataVersion.latest(), cache, chunk.getChunkX(), chunk.getChunkZ()).deleteAllDataOfChunk();
    }

    @Override
    public AnvilChunk getChunk(int chunkX, int chunkZ) throws IOException {
        return new AnvilChunk(gameFactory, new RegionNbtIo(ChunkDataVersion.latest(), cache, chunkX, chunkZ));
    }

    @Override
    public void saveChunk(AnvilChunk chunk) throws IOException {
        chunk.save();
    }
}