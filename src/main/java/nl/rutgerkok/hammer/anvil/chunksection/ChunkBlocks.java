package nl.rutgerkok.hammer.anvil.chunksection;

import java.util.List;

import nl.rutgerkok.hammer.anvil.AnvilChunk;
import nl.rutgerkok.hammer.anvil.AnvilMaterialMap;
import nl.rutgerkok.hammer.anvil.ChunkDataVersion;
import nl.rutgerkok.hammer.anvil.tag.AnvilFormat.ChunkTag;
import nl.rutgerkok.hammer.anvil.tag.AnvilFormat.SectionTag;
import nl.rutgerkok.hammer.material.MaterialData;
import nl.rutgerkok.hammer.tag.CompoundTag;
import nl.rutgerkok.hammer.tag.TagType;
import nl.rutgerkok.hammer.util.FretArray;

/**
 * Abstracts away the raw block ids of a chunk, which are different between
 * Minecraft versions.
 *
 */
public abstract class ChunkBlocks {
    static final int SECTION_X_BITS = 4;
    static final int SECTION_X_SIZE = AnvilChunk.CHUNK_X_SIZE;
    static final int SECTION_Y_BITS = 4;
    static final int SECTION_Y_SIZE = 16;
    static final int SECTION_Z_BITS = 4;
    static final int SECTION_Z_SIZE = AnvilChunk.CHUNK_Z_SIZE;
    static final int TOTAL_SIZE = SECTION_X_SIZE * SECTION_Y_SIZE * SECTION_Z_SIZE;
    static final int TOTAL_SIZE_NIBBLE = TOTAL_SIZE / 2;

    /**
     * Creates a block lookup using either the old or the new block id storage
     * method.
     *
     * @param dataVersion
     *            Which version to use.
     * @param materialMap
     *            The material map.
     * @return The block lookup.
     */
    public static ChunkBlocks create(ChunkDataVersion dataVersion, AnvilMaterialMap materialMap) {
        if (dataVersion.isBefore(ChunkDataVersion.MINECRAFT_FLATTENING)) {
            // Very old
            return new IdAndDataBlocks(materialMap);
        }
        if (dataVersion.isAfter(ChunkDataVersion.MINECRAFT_1_15_2)) {
            // Newest format
            return new PalettedBlocks(FretArray.notCrossingCellBoundaries(), materialMap);
        }

        // In between
        return new PalettedBlocks(FretArray.crossingCellBoundaries(), materialMap);
    }

    static CompoundTag getChunkSection(CompoundTag chunkTag, int y) {
        List<CompoundTag> sections = chunkTag.getList(ChunkTag.SECTIONS,
                TagType.COMPOUND);

        int sectionIndex = y >> SECTION_Y_BITS;

        if (!sections.isEmpty()) {
            // Do a quick guess (correct only if no chunk sections are omitted at
            // and below this section index)
            int firstSectionIndex = sections.get(0).getByte(SectionTag.INDEX);
            int posInList = sectionIndex - firstSectionIndex;
            if (posInList >= 0 && posInList < sections.size()) {
                CompoundTag section = sections.get(posInList);
                if (section != null && section.getByte(SectionTag.INDEX) == sectionIndex) {
                    return section;
                }
            }
        }

        // Search for section
        for (CompoundTag section : sections) {
            if (section != null && section.getByte(SectionTag.INDEX) == sectionIndex) {
                return section;
            }
        }
        return null;
    }

    /**
     * Gets the material from the chunk tag.
     *
     * @param chunkTag
     *            The tag.
     * @param x
     *            X in the chunk.
     * @param y
     *            Y in the chunk.
     * @param z
     *            Z in the chunk.
     * @return The material, never null, but can be air.
     */
    public abstract MaterialData getMaterial(CompoundTag chunkTag, int x, int y, int z);

    /**
     * Sets the material in the chunk.
     *
     * @param chunkTag
     *            Chunk data tag.
     * @param x
     *            X in the chunk.
     * @param y
     *            Y in the chunk.
     * @param z
     *            Z in the chunk.
     * @param materialData
     *            The new material.
     */
    public abstract void setMaterial(CompoundTag chunkTag, int x, int y, int z, MaterialData materialData);

}
