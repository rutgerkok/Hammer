package nl.rutgerkok.hammer.anvil.chunksection;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.OptionalInt;

import nl.rutgerkok.hammer.anvil.tag.AnvilFormat.MaterialTag;
import nl.rutgerkok.hammer.anvil.tag.AnvilFormat.SectionTag;
import nl.rutgerkok.hammer.material.GlobalMaterialMap;
import nl.rutgerkok.hammer.material.MaterialData;
import nl.rutgerkok.hammer.tag.CompoundKey;
import nl.rutgerkok.hammer.tag.CompoundTag;
import nl.rutgerkok.hammer.tag.TagType;
import nl.rutgerkok.hammer.util.FretArray;

/**
 * Used in Minecraft 1.13 and newer.
 *
 */
final class PalettedBlocks extends ChunkBlocks {

    static int getPositionInSectionArray(int xInSection, int yInSection, int zInSection) {
        return yInSection << (SECTION_X_BITS + SECTION_Z_BITS)
                | zInSection << SECTION_X_BITS | xInSection;
    }

    private final GlobalMaterialMap globalMaterials;

    private final int BLOCKS_PER_SECTION = SECTION_X_SIZE * SECTION_Y_SIZE * SECTION_Z_SIZE;

    PalettedBlocks(GlobalMaterialMap globalMaterials) {
        this.globalMaterials = Objects.requireNonNull(globalMaterials, "globalMaterials");
    }

    private MaterialData fromMaterialTag(CompoundTag tag) {
        String name = tag.getString(MaterialTag.NAME);
        if (!tag.containsKey(MaterialTag.PROPERTIES)) {
            return globalMaterials.addMaterial(name);
        }

        // Do a bit more effort to read the material
        boolean first = true;
        StringBuilder nameBuilder = new StringBuilder(name);
        Map<CompoundKey<String>, String> properties = tag.getCompound(MaterialTag.PROPERTIES).entries(TagType.STRING);
        for (Entry<CompoundKey<String>, String> property : properties.entrySet()) {
            if (first) {
                first = false;
            } else {
                nameBuilder.append(',');
            }
            nameBuilder.append(property.getKey().getKeyName());
            nameBuilder.append('=');
            nameBuilder.append(property.getValue());
        }
        return globalMaterials.addMaterial(name);
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

    @Override
    public void setMaterial(CompoundTag chunkTag, int x, int y, int z, MaterialData materialData) {
        // TODO Auto-generated method stub

    }

}
