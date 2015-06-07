package nl.rutgerkok.hammer.pocket;

import java.util.List;
import java.util.Objects;

import nl.rutgerkok.hammer.Chunk;
import nl.rutgerkok.hammer.GameFactory;
import nl.rutgerkok.hammer.material.MaterialData;
import nl.rutgerkok.hammer.tag.CompoundTag;

public final class PocketChunk implements Chunk {

    private static final int CHUNK_X_BITS = 4;
    private static final int CHUNK_X_SIZE = 1 << CHUNK_X_BITS;

    private static final int CHUNK_Y_BITS = 7;
    private static final int CHUNK_Y_SIZE = 1 << CHUNK_Y_BITS;

    private static final int CHUNK_Z_BITS = 4;
    private static final int CHUNK_Z_SIZE = 1 << CHUNK_Z_BITS;

    private static final int OFFSET_BLOCK_DATA = 32_768;
    private static final int OFFSET_BLOCK_IDS = 0;
    private static final int OFFSET_BLOCKLIGHT_DATA = 32_768 + 16_384 + 16_384;
    private static final int OFFSET_COLOR_DATA = 32_768 + 16_384 + 16_384 + 16_384 + 256;
    private static final int OFFSET_MARKER_DATA = 32_768 + 16_384 + 16_384 + 16_384;
    private static final int OFFSET_SKYLIGHT_DATA = 32_768 + 16_384;

    private final byte[] bytes;
    private final int chunkX;
    private final int chunkZ;
    private final List<CompoundTag> entities;
    private final GameFactory gameFactory;
    private final List<CompoundTag> tileEntities;

    PocketChunk(GameFactory gameFactory, int chunkX, int chunkZ,
            byte[] bytes, List<CompoundTag> entities, List<CompoundTag> tileEntities) {
        this.gameFactory = Objects.requireNonNull(gameFactory, "gameFactory");
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.bytes = Objects.requireNonNull(bytes, "bytes");
        this.entities = Objects.requireNonNull(entities, "entities");
        this.tileEntities = Objects.requireNonNull(tileEntities, "tileEntities");
    }

    /**
     * Gets the raw bytes for block and light storage.
     * 
     * @return The raw bytes.
     */
    byte[] accessBytes() {
        return bytes;
    }

    @Override
    public int getChunkX() {
        return chunkX;
    }

    @Override
    public int getChunkZ() {
        return chunkZ;
    }

    @Override
    public List<CompoundTag> getEntities() {
        return entities;
    }

    @Override
    public GameFactory getGameFactory() {
        return gameFactory;
    }

    @Override
    public short getMaterialId(int x, int y, int z) {
        return bytes[OFFSET_BLOCK_IDS + (y | (z << CHUNK_Y_BITS) | (x << (CHUNK_Y_BITS + CHUNK_Z_BITS)))];
    }

    @Override
    public int getSizeX() {
        return CHUNK_X_SIZE;
    }

    @Override
    public int getSizeY() {
        return CHUNK_Y_SIZE;
    }

    @Override
    public int getSizeZ() {
        return CHUNK_Z_SIZE;
    }

    @Override
    public CompoundTag getTag() {
        // This doesn't exist in the Pocket Edition level format
        throw new UnsupportedOperationException();
    }

    @Override
    public List<CompoundTag> getTileEntities() {
        return tileEntities;
    }

    @Override
    public void setBlock(int x, int y, int z, MaterialData materialData) {
        bytes[OFFSET_BLOCK_IDS + (y | (z << CHUNK_Y_BITS) | (x << (CHUNK_Y_BITS + CHUNK_Z_BITS)))] = (byte) materialData.getMaterial().getId();
    }

}
