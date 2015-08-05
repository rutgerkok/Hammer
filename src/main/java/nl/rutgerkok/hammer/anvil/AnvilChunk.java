package nl.rutgerkok.hammer.anvil;

import java.util.List;
import java.util.Objects;

import nl.rutgerkok.hammer.Chunk;
import nl.rutgerkok.hammer.anvil.tag.AnvilFormat.ChunkTag;
import nl.rutgerkok.hammer.material.MaterialData;
import nl.rutgerkok.hammer.tag.CompoundTag;
import nl.rutgerkok.hammer.tag.ListTag;
import nl.rutgerkok.hammer.tag.TagType;
import nl.rutgerkok.hammer.util.MaterialNotFoundException;

/**
 * Represents a single chunk, as used by Minecraft.
 *
 */
public final class AnvilChunk implements Chunk {

    public static final int CHUNK_X_SIZE = 16;
    public static final int CHUNK_Y_SIZE = 256;
    public static final int CHUNK_Z_SIZE = 16;

    /**
     * The highest possible biome id in a Minecraft map.
     */
    public static final int MAX_BIOME_ID = 254;

    /**
     * Creates a new, empty chunk.
     *
     * @param gameFactory
     *            Game factory belonging to Pocket Edition.
     * @param chunkX
     *            Chunk x coordinate.
     * @param chunkZ
     *            Chunk z coordinate.
     * @return The chunk.
     */
    static AnvilChunk newEmptyChunk(AnvilGameFactory gameFactory, int chunkX, int chunkZ) {
        CompoundTag chunkTag = new CompoundTag();
        chunkTag.setInt(ChunkTag.X_POS, chunkX);
        chunkTag.setInt(ChunkTag.Z_POS, chunkZ);
        return new AnvilChunk(gameFactory, chunkTag);
    }

    private final CompoundTag chunkTag;
    private final AnvilGameFactory gameFactory;

    /**
     * Creates a new chunk from the given data tag.
     *
     * @param gameFactory
     *            Game factory, for interpreting the raw data.
     * @param chunkTag
     *            The data tag, with child tags like Biomes, Sections, etc.
     */
    AnvilChunk(AnvilGameFactory gameFactory, CompoundTag chunkTag) {
        this.gameFactory = Objects.requireNonNull(gameFactory, "gameFactory");
        this.chunkTag = Objects.requireNonNull(chunkTag, "chunkTag");
    }

    private void checkOutOfBounds(int x, int y, int z) {
        if (isOutOfBounds(x, y, z)) {
            throw new IndexOutOfBoundsException("(" + x + "," + y + "," + z
                    + ") is outside the chunk, which ranges from (0,0,0) to ("
                    + CHUNK_X_SIZE + "," + CHUNK_Y_SIZE + "," + CHUNK_Z_SIZE + ")");
        }
    }

    /**
     * Gets direct access to the biome array of this chunk. Modifying the byte
     * array will modify the data of this chunk.
     *
     * @return The biome array.
     */
    public byte[] getBiomeArray() {
        return chunkTag.getByteArray(ChunkTag.BIOMES, CHUNK_X_SIZE * CHUNK_Z_SIZE);
    }

    /**
     * Gets the tags of the chunk sections.
     *
     * @return The tags.
     */
    public ListTag<CompoundTag> getChunkSections() {
        return chunkTag.getList(ChunkTag.SECTIONS, TagType.COMPOUND);
    }

    @Override
    public int getChunkX() {
        return chunkTag.getInt(ChunkTag.X_POS);
    }

    @Override
    public int getChunkZ() {
        return chunkTag.getInt(ChunkTag.Z_POS);
    }

    @Override
    public List<CompoundTag> getEntities() {
        return chunkTag.getList(ChunkTag.ENTITIES, TagType.COMPOUND);
    }

    @Override
    public AnvilGameFactory getGameFactory() {
        return gameFactory;
    }

    @Override
    public MaterialData getMaterial(int x, int y, int z) throws MaterialNotFoundException {
        checkOutOfBounds(x, y, z);

        short id = ChunkSection.getMaterialId(chunkTag, x, y, z);
        byte data = ChunkSection.getMaterialData(chunkTag, x, y, z);

        return gameFactory.getMaterialMap().getMaterialData(id, data);
    }

    @Override
    public short getMaterialId(int x, int y, int z) {
        checkOutOfBounds(x, y, z);
        return ChunkSection.getMaterialId(chunkTag, x, y, z);
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
        return chunkTag;
    }

    @Override
    public List<CompoundTag> getTileEntities() {
        return chunkTag.getList(ChunkTag.TILE_ENTITIES, TagType.COMPOUND);
    }

    @Override
    public boolean isOutOfBounds(int x, int y, int z) {
        return x < 0 || x >= CHUNK_X_SIZE || y < 0 || y >= CHUNK_Y_SIZE || z < 0 || z >= CHUNK_Z_SIZE;
    }

    @Override
    public void setMaterial(int x, int y, int z, MaterialData materialData) throws MaterialNotFoundException {
        checkOutOfBounds(x, y, z);

        char ida = gameFactory.getMaterialMap().getMinecraftId(materialData);
        short blockId = (short) (ida >> 4);
        byte blockData = (byte) (ida & 0xf);

        ChunkSection.setMaterialIdAndData(chunkTag, x, y, z, blockId, blockData);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + getChunkX() + "," + getChunkZ() + ")";
    }
}
