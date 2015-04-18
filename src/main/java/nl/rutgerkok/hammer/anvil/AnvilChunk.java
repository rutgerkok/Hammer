package nl.rutgerkok.hammer.anvil;

import static nl.rutgerkok.hammer.anvil.tag.FormatConstants.CHUNK_BIOMES_TAG;
import static nl.rutgerkok.hammer.anvil.tag.FormatConstants.CHUNK_ENTITIES_TAG;
import static nl.rutgerkok.hammer.anvil.tag.FormatConstants.CHUNK_SECTIONS_TAG;
import static nl.rutgerkok.hammer.anvil.tag.FormatConstants.CHUNK_TILE_ENTITIES_TAG;
import static nl.rutgerkok.hammer.anvil.tag.FormatConstants.CHUNK_X_POS_TAG;
import static nl.rutgerkok.hammer.anvil.tag.FormatConstants.CHUNK_Z_POS_TAG;

import java.util.List;

import nl.rutgerkok.hammer.Chunk;
import nl.rutgerkok.hammer.anvil.material.AnvilMaterial;
import nl.rutgerkok.hammer.material.MaterialData;
import nl.rutgerkok.hammer.material.MaterialMap;
import nl.rutgerkok.hammer.tag.CompoundTag;
import nl.rutgerkok.hammer.tag.ListTag;
import nl.rutgerkok.hammer.tag.TagType;

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

    private final CompoundTag chunkTag;
    private final MaterialMap materialMap;

    /**
     * Creates a new chunk from the given data tag.
     *
     * @param materialMap
     *            Map for materials, for interpreting the raw data.
     * @param chunkTag
     *            The data tag, with child tags like Biomes, Sections, etc.
     */
    public AnvilChunk(MaterialMap materialMap, CompoundTag chunkTag) {
        this.materialMap = materialMap;
        this.chunkTag = chunkTag;
    }

    /**
     * Gets direct access to the biome array of this chunk. Modifying the byte
     * array will modify the data of this chunk.
     *
     * @return The biome array.
     */
    public byte[] getBiomeArray() {
        return chunkTag.getByteArray(CHUNK_BIOMES_TAG, CHUNK_X_SIZE * CHUNK_Z_SIZE);
    }

    /**
     * Gets the tags of the chunk sections.
     *
     * @return The tags.
     */
    public ListTag<CompoundTag> getChunkSections() {
        return chunkTag.getList(CHUNK_SECTIONS_TAG, TagType.COMPOUND);
    }

    @Override
    public int getChunkX() {
        return chunkTag.getInt(CHUNK_X_POS_TAG);
    }

    @Override
    public int getChunkZ() {
        return chunkTag.getInt(CHUNK_Z_POS_TAG);
    }

    @Override
    public List<CompoundTag> getEntities() {
        return chunkTag.getList(CHUNK_ENTITIES_TAG, TagType.COMPOUND);
    }

    @Override
    public short getMaterialId(int x, int y, int z) {
        if (isOutOfBounds(x, y, z)) {
            return AnvilMaterial.AIR_ID;
        }
        return ChunkSection.getMaterialId(chunkTag, x, y, z);
    }

    @Override
    public MaterialMap getMaterialMap() {
        return materialMap;
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
        return chunkTag.getList(CHUNK_TILE_ENTITIES_TAG, TagType.COMPOUND);
    }

    private boolean isOutOfBounds(int x, int y, int z) {
        return x < 0 || x >= CHUNK_X_SIZE || y < 0 || y >= CHUNK_Y_SIZE || z < 0 || z >= CHUNK_Z_SIZE;
    }

    @Override
    public void setBlock(int x, int y, int z, MaterialData materialData) {
        if (isOutOfBounds(x, y, z)) {
            return;
        }

        ChunkSection.setMaterialId(chunkTag, x, y, z, materialData.getMaterial().getId());
        ChunkSection.setMaterialData(chunkTag, x, y, z, materialData.getData());
    }
}
