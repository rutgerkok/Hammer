package nl.rutgerkok.hammer.pocket;

import java.util.List;
import java.util.Objects;

import nl.rutgerkok.hammer.Chunk;
import nl.rutgerkok.hammer.material.MaterialData;
import nl.rutgerkok.hammer.material.MaterialMap;
import nl.rutgerkok.hammer.tag.CompoundTag;

public final class PocketChunk implements Chunk {

    private static final int CHUNK_X_SIZE = 16;
    private static final int CHUNK_Y_SIZE = 128;
    private static final int CHUNK_Z_SIZE = 16;

    private final byte[] bytes;
    private final int chunkX;
    private final int chunkZ;
    private final List<CompoundTag> entities;
    private final List<CompoundTag> tileEntities;

    PocketChunk(int chunkX, int chunkZ, byte[] bytes, List<CompoundTag> entities, List<CompoundTag> tileEntities) {
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
    public short getMaterialId(int x, int y, int z) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public MaterialMap getMaterialMap() {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub

    }

}
