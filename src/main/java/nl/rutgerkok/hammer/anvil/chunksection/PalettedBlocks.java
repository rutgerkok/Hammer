package nl.rutgerkok.hammer.anvil.chunksection;

import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;

import nl.rutgerkok.hammer.anvil.AnvilMaterialMap;
import nl.rutgerkok.hammer.anvil.tag.AnvilFormat.ChunkTag;
import nl.rutgerkok.hammer.anvil.tag.AnvilFormat.SectionTag;
import nl.rutgerkok.hammer.material.MaterialData;
import nl.rutgerkok.hammer.tag.CompoundTag;
import nl.rutgerkok.hammer.tag.ListTag;
import nl.rutgerkok.hammer.tag.TagType;
import nl.rutgerkok.hammer.util.FretArray;

/**
 * Used in Minecraft 1.13 and newer.
 *
 */
final class PalettedBlocks extends ChunkBlocks {

    private static final int TOTAL_SIZE_4BIT_FRET = TOTAL_SIZE * 4 / Long.SIZE;

    static int getPositionInSectionArray(int xInSection, int yInSection, int zInSection) {
        return yInSection << (SECTION_X_BITS + SECTION_Z_BITS)
                | zInSection << SECTION_X_BITS | xInSection;
    }

    private final AnvilMaterialMap materialMap;
    private final int BLOCKS_PER_SECTION = SECTION_X_SIZE * SECTION_Y_SIZE * SECTION_Z_SIZE;

    PalettedBlocks(AnvilMaterialMap materialMap) {
        this.materialMap = Objects.requireNonNull(materialMap, "materialMap");
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
        chunkSection.setLongArray(SectionTag.BLOCK_STATES, new long[TOTAL_SIZE_4BIT_FRET]);
        chunkSection.setByteArray(SectionTag.BLOCK_LIGHT, new byte[TOTAL_SIZE_NIBBLE]);
        chunkSection.setByteArray(SectionTag.SKY_LIGHT, new byte[TOTAL_SIZE_NIBBLE]);
        List<CompoundTag> palette = chunkSection.getList(SectionTag.PALETTE, TagType.COMPOUND);
        palette.add(materialMap.serializeToBlockState(materialMap.getGlobal().getAir(), new CompoundTag()));

        // Add the new section
        chunkTag.getList(ChunkTag.SECTIONS, TagType.COMPOUND).add(chunkSection);

        // Mark chunk as needing light update
        chunkTag.remove(ChunkTag.HEIGHT_MAPS);

        return chunkSection;
    }

    private char findOrCreateMaterialId(CompoundTag sectionTag, MaterialData material) {
        ListTag<CompoundTag> materialsTag = sectionTag.getList(SectionTag.PALETTE, TagType.COMPOUND);
        for (int i = 0; i < materialsTag.size(); i++) {
            MaterialData foundMaterial = this.materialMap.parseBlockState(materialsTag.get(i));
            if (foundMaterial.equals(material)) {
                return (char) i;
            }
        }

        // Ok, we need to add a new material
        CompoundTag newMaterial = materialMap.serializeToBlockState(material, new CompoundTag());
        materialsTag.add(newMaterial);

        // Check if it will fit in the chunk array
        int highestId = materialsTag.size() - 1;
        int bitsNeededPerBlock = FretArray.toSafeBitCount(Integer.SIZE - Integer.numberOfLeadingZeros(highestId));
        long[] currentBlockStates = sectionTag.getLongArray(SectionTag.BLOCK_STATES, OptionalInt.empty());
        int currentBitsPerBlock = currentBlockStates.length * Long.SIZE / BLOCKS_PER_SECTION;
        if (currentBitsPerBlock < bitsNeededPerBlock) {
            // Nope, so resize the array
            long[] resizedArray = FretArray.changeBitsPerEntry(currentBlockStates, currentBitsPerBlock,
                    bitsNeededPerBlock);
            sectionTag.setLongArray(SectionTag.BLOCK_STATES, resizedArray);
        }

        return (char) highestId;
    }

    @Override
    public MaterialData getMaterial(CompoundTag chunkTag, int x, int y, int z) {
        CompoundTag section = getChunkSection(chunkTag, y);
        if (section == null) {
            return materialMap.getGlobal().getAir();
        }

        long[] blockStates = section.getLongArray(SectionTag.BLOCK_STATES, OptionalInt.empty());
        if (blockStates.length == 0) {
            return materialMap.getGlobal().getAir();
        }

        int bitsPerBlock = blockStates.length * Long.SIZE / BLOCKS_PER_SECTION;
        int position = getPositionInSectionArray(x, y & 0xf, z);
        int blockId = FretArray.get(blockStates, bitsPerBlock, position);
        CompoundTag material = section.getList(SectionTag.PALETTE, TagType.COMPOUND).get(blockId);
        return materialMap.parseBlockState(material);
    }

    int log2(int value) {
        return Integer.SIZE - Integer.numberOfLeadingZeros(value);
    }

    @Override
    public void setMaterial(CompoundTag chunkTag, int x, int y, int z, MaterialData materialData) {
        CompoundTag section = getChunkSection(chunkTag, y);
        if (section == null) {
            // Create the section first
            section = createChunkSection(chunkTag, y);
        }

        char materialId = findOrCreateMaterialId(section, materialData);
        long[] blockStates = section.getLongArray(SectionTag.BLOCK_STATES, OptionalInt.empty());
        int bitsPerBlock = blockStates.length * Long.SIZE / BLOCKS_PER_SECTION;
        int position = getPositionInSectionArray(x, y & 0xf, z);
        FretArray.set(blockStates, bitsPerBlock, position, materialId);
    }

}
