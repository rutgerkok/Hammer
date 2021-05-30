package nl.rutgerkok.hammer.anvil.chunksection;

import java.util.Objects;

import nl.rutgerkok.hammer.anvil.AnvilMaterialMap;
import nl.rutgerkok.hammer.anvil.tag.AnvilFormat.ChunkTag;
import nl.rutgerkok.hammer.anvil.tag.AnvilFormat.OldChunkTag;
import nl.rutgerkok.hammer.anvil.tag.AnvilFormat.OldSectionTag;
import nl.rutgerkok.hammer.anvil.tag.AnvilFormat.SectionTag;
import nl.rutgerkok.hammer.material.MaterialData;
import nl.rutgerkok.hammer.tag.CompoundTag;
import nl.rutgerkok.hammer.tag.TagType;
import nl.rutgerkok.hammer.util.NibbleArray;

/**
 * The good old way to store blocks. For chunks from Minecraft 1.2 - 1.12.
 *
 */
final class IdAndDataBlocks extends ChunkBlocks {

    static int getPositionInSectionArray(int xInSection, int yInSection, int zInSection) {
        return yInSection << (SECTION_X_BITS + SECTION_Z_BITS)
                | zInSection << SECTION_X_BITS | xInSection;
    }

    private final AnvilMaterialMap materialMap;

    IdAndDataBlocks(AnvilMaterialMap materialMap) {
        this.materialMap = Objects.requireNonNull(materialMap);
    }

    /**
     * Adds the requested chunk section to the chunk, and returns it.
     *
     * @param chunkTag
     *            The chunk to add the new section to.
     * @param y
     *            The block y of the chunk section.
     * @return The chunk section.
     */
    private CompoundTag createChunkSection(CompoundTag chunkTag, int y) {
        CompoundTag chunkSection = new CompoundTag();
        int sectionIndex = y >>> SECTION_Y_BITS;
        chunkSection.setByte(SectionTag.INDEX, (byte) sectionIndex);
        chunkSection.setByteArray(OldSectionTag.BLOCK_IDS, new byte[TOTAL_SIZE]);
        chunkSection.setByteArray(OldSectionTag.BLOCK_DATA, new byte[TOTAL_SIZE_NIBBLE]);
        chunkSection.setByteArray(SectionTag.BLOCK_LIGHT, new byte[TOTAL_SIZE_NIBBLE]);
        chunkSection.setByteArray(SectionTag.SKY_LIGHT, new byte[TOTAL_SIZE_NIBBLE]);

        // Add the new section
        chunkTag.getList(ChunkTag.SECTIONS, TagType.COMPOUND).add(chunkSection);

        // Mark chunk as needing light update
        chunkTag.setBoolean(OldChunkTag.LIGHT_POPULATED, false);

        return chunkSection;
    }


    @Override
    public MaterialData getMaterial(CompoundTag chunkTag, int x, int y, int z) {
        short id = getMaterialId(chunkTag, x, y, z);
        byte data = getMaterialData(chunkTag, x, y, z);

        return materialMap.getMaterialDataFromOldIds(id, data);
    }

    /**
     * Gets the block data at the given position.
     *
     * @param chunkTag
     *            Chunk data tag.
     * @param x
     *            X in the chunk.
     * @param y
     *            Y in the chunk.
     * @param z
     *            Z in the chunk.
     * @return The block data.
     * @throws ArrayIndexOutOfBoundsException
     *             If the x, y or z are out of bounds.
     */
    byte getMaterialData(CompoundTag chunkTag, int x, int y, int z) {
        CompoundTag section = getChunkSection(chunkTag, y);
        if (section == null) {
            // Empty section
            return 0;
        }

        int yInSection = y & (SECTION_Y_SIZE - 1);
        int position = getPositionInSectionArray(x, yInSection, z);

        byte[] dataArray = section.getByteArray(OldSectionTag.BLOCK_DATA,
                TOTAL_SIZE_NIBBLE);
        return NibbleArray.getInArray(dataArray, position);
    }

    /**
     * Gets the block id at the given position.
     *
     * @param chunkTag
     *            Chunk data tag.
     * @param x
     *            X in the chunk.
     * @param y
     *            Y in the chunk.
     * @param z
     *            Z in the chunk.
     * @return The block id.
     * @throws ArrayIndexOutOfBoundsException
     *             If the x, y or z are out of bounds.
     */
    final short getMaterialId(CompoundTag chunkTag, int x, int y, int z) {
        CompoundTag section = getChunkSection(chunkTag, y);
        if (section == null) {
            return 0;
        }

        int yInSection = y & (SECTION_Y_SIZE - 1);
        int position = getPositionInSectionArray(x, yInSection, z);

        byte[] blocks = section.getByteArray(OldSectionTag.BLOCK_IDS, TOTAL_SIZE);
        byte[] extBlocks = section.getByteArray(OldSectionTag.EXT_BLOCK_IDS, TOTAL_SIZE_NIBBLE);

        int blockId = blocks[position] & 0xff;
        if (extBlocks.length != 0) {
            blockId |= NibbleArray.getInArray(extBlocks, position) << Byte.SIZE;
        }

        return (short) blockId;
    }


    @Override
    public void setMaterial(CompoundTag chunkTag, int x, int y, int z, MaterialData materialData) {
        char ida = materialMap.getOldMinecraftId(materialData);
        short blockId = (short) (ida >> 4);
        byte blockData = (byte) (ida & 0xf);
        this.setMaterialIdAndData(chunkTag, x, y, z, blockId, blockData);
    }

    /**
     * Sets the material id at the given position. Silently fails when setting a
     * block in a non-existent section.
     *
     * @param chunkTag
     *            Chunk data tag.
     * @param x
     *            X in the chunk.
     * @param y
     *            Y in the chunk.
     * @param z
     *            Z in the chunk.
     * @param blockId
     *            The block id.
     * @param blockData
     *            The block data.
     */
    void setMaterialIdAndData(CompoundTag chunkTag, int x, int y, int z, short blockId, byte blockData) {
        CompoundTag section = getChunkSection(chunkTag, y);
        if (section == null) {
            // Create the section first
            section = createChunkSection(chunkTag, y);
        }

        int yInSection = y & (SECTION_Y_SIZE - 1);
        int position = getPositionInSectionArray(x, yInSection, z);

        // Set low id
        byte[] blocks = section.getByteArray(OldSectionTag.BLOCK_IDS, TOTAL_SIZE);
        blocks[position] = (byte) blockId;

        // Set high id
        if (blockId > 0xff) {
            int highId = blockId >>> Byte.SIZE;
            byte[] extBlocks = section.getByteArray(OldSectionTag.EXT_BLOCK_IDS,
                    TOTAL_SIZE);
            if (extBlocks.length == 0) {
                // Create array for this high id
                extBlocks = new NibbleArray(
                        SECTION_X_SIZE * SECTION_Y_SIZE * SECTION_Z_SIZE)
                                .getHandle();
                section.setByteArray(OldSectionTag.EXT_BLOCK_IDS, extBlocks);
            }
            NibbleArray.setInArray(extBlocks, position, (byte) highId);
        }

        // Set data
        byte[] dataArray = section.getByteArray(OldSectionTag.BLOCK_DATA,
                TOTAL_SIZE_NIBBLE);
        NibbleArray.setInArray(dataArray, position, blockData);
    }

}
