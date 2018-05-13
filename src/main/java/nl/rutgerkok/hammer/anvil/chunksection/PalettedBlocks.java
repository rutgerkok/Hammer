package nl.rutgerkok.hammer.anvil.chunksection;

import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Set;

import com.google.common.collect.ImmutableMap;

import nl.rutgerkok.hammer.anvil.tag.AnvilFormat.ChunkTag;
import nl.rutgerkok.hammer.anvil.tag.AnvilFormat.MaterialTag;
import nl.rutgerkok.hammer.anvil.tag.AnvilFormat.SectionTag;
import nl.rutgerkok.hammer.material.GlobalMaterialMap;
import nl.rutgerkok.hammer.material.MaterialData;
import nl.rutgerkok.hammer.material.MaterialName;
import nl.rutgerkok.hammer.tag.CompoundKey;
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

    private static CompoundTag createMaterialTag(MaterialName material) {
        CompoundTag target = new CompoundTag();
        target.setString(MaterialTag.NAME, material.getBaseName());
        if (!material.hasProperties()) {
            return target; // Done!
        }
        CompoundTag properties = target.getCompound(MaterialTag.PROPERTIES);
        for (Entry<String, String> property : material.getProperties().entrySet()) {
            properties.setString(CompoundKey.of(property.getKey()), property.getValue());
        }
        return target;
    }

    static int getPositionInSectionArray(int xInSection, int yInSection, int zInSection) {
        return yInSection << (SECTION_X_BITS + SECTION_Z_BITS)
                | zInSection << SECTION_X_BITS | xInSection;
    }

    private final GlobalMaterialMap globalMaterials;
    private final int BLOCKS_PER_SECTION = SECTION_X_SIZE * SECTION_Y_SIZE * SECTION_Z_SIZE;

    PalettedBlocks(GlobalMaterialMap globalMaterials) {
        this.globalMaterials = Objects.requireNonNull(globalMaterials, "globalMaterials");
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
        palette.add(createMaterialTag(globalMaterials.getAir().getMaterialName()));

        // Add the new section
        chunkTag.getList(ChunkTag.SECTIONS, TagType.COMPOUND).add(chunkSection);

        // Mark chunk as needing light update
        chunkTag.remove(ChunkTag.HEIGHT_MAPS);

        return chunkSection;
    }

    private char findOrCreateMaterialId(CompoundTag sectionTag, MaterialName material) {
        ListTag<CompoundTag> materialsTag = sectionTag.getList(SectionTag.PALETTE, TagType.COMPOUND);
        for (int i = 0; i < materialsTag.size(); i++) {
            CompoundTag materialTag = materialsTag.get(i);
            if (!material.getBaseName().equals(materialTag.getString(MaterialTag.NAME))) {
                continue;
            }

            if (!material.hasProperties() && materialTag.containsKey(MaterialTag.PROPERTIES)) {
                return (char) i;
            }
        }

        // Ok, we need to add a new material
        CompoundTag newMaterial = createMaterialTag(material);
        materialsTag.add(newMaterial);

        // Check if it will fit in the chunk array
        int highestId = materialsTag.size() - 1;
        int bitsNeededPerBlock = Integer.SIZE - Integer.numberOfLeadingZeros(highestId);
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

    private MaterialData fromMaterialTag(CompoundTag tag) {
        String name = tag.getString(MaterialTag.NAME);
        if (!tag.containsKey(MaterialTag.PROPERTIES)) {
            return globalMaterials.addMaterial(MaterialName.ofBaseName(name));
        }

        // Do a bit more effort to read the material
        ImmutableMap.Builder<String, String> properties = ImmutableMap.builder();
        Set<Entry<CompoundKey<?>, Object>> propertyTags = tag.getCompound(MaterialTag.PROPERTIES).entrySet();
        for (Entry<CompoundKey<?>, Object> property : propertyTags) {
            properties.put(property.getKey().getKeyName(), property.getValue().toString());
        }
        return globalMaterials.addMaterial(MaterialName.create(name, properties.build()));
    }

    @Override
    public MaterialData getMaterial(CompoundTag chunkTag, int x, int y, int z) {
        CompoundTag section = getChunkSection(chunkTag, y);
        if (section == null) {
            return globalMaterials.getAir();
        }

        long[] blockStates = section.getLongArray(SectionTag.BLOCK_STATES, OptionalInt.empty());
        if (blockStates.length == 0) {
            return globalMaterials.getAir();
        }

        int bitsPerBlock = blockStates.length * Long.SIZE / BLOCKS_PER_SECTION;
        int position = getPositionInSectionArray(x, y & 0xf, z);
        int blockId = FretArray.get(blockStates, bitsPerBlock, position);
        CompoundTag material = section.getList(SectionTag.PALETTE, TagType.COMPOUND).get(blockId);
        return fromMaterialTag(material);
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

        char materialId = findOrCreateMaterialId(section, materialData.getMaterialName());
        long[] blockStates = section.getLongArray(SectionTag.BLOCK_STATES, OptionalInt.empty());
        int bitsPerBlock = blockStates.length * Long.SIZE / BLOCKS_PER_SECTION;
        int position = getPositionInSectionArray(x, y & 0xf, z);
        FretArray.set(blockStates, bitsPerBlock, position, materialId);
    }

}
