package nl.rutgerkok.hammer.anvil;

import java.util.Objects;

import nl.rutgerkok.hammer.GameFactory;
import nl.rutgerkok.hammer.ItemStack;
import nl.rutgerkok.hammer.material.BlockDataMaterialMap;
import nl.rutgerkok.hammer.material.MaterialData;
import nl.rutgerkok.hammer.tag.CompoundKey;
import nl.rutgerkok.hammer.tag.CompoundTag;
import nl.rutgerkok.hammer.tag.TagType;
import nl.rutgerkok.hammer.util.MaterialNotFoundException;

/**
 * Implementation of {@link ItemStack} for Anvil worlds.
 *
 * <p> Both the Minecraft 1.8+ format with named ids and the Minecraft 1.2 - 1.7
 * format with numeric ids are supported. </p>
 * 
 * @see GameFactory#createItemStack(CompoundTag)
 *
 */
final class AnvilItemStack implements ItemStack {
    private static final CompoundKey<Short> BLOCK_DATA_TAG = CompoundKey.of("Damage");
    private static final CompoundKey<String> BLOCK_ID_TAG = CompoundKey.of("id");
    private static final CompoundKey<Byte> COUNT_TAG = CompoundKey.of("Count");
    private static final CompoundKey<Short> OLD_BLOCK_ID_TAG = CompoundKey.of("id");

    private final BlockDataMaterialMap materialMap;
    private final CompoundTag tag;

    AnvilItemStack(BlockDataMaterialMap materialMap, CompoundTag tag) {
        this.materialMap = Objects.requireNonNull(materialMap);
        this.tag = Objects.requireNonNull(tag);
    }

    @Override
    public byte getCount() {
        return tag.getByte(COUNT_TAG);
    }


    /**
     * Gets the material and data represented as one object.
     *
     * @return The material and data.
     * @throws MaterialNotFoundException
     *             If no block material is present. Usually happens in the case
     *             of item materials.
     * @throws NullPointerException
     *             If the material map is null.
     */
    @Override
    public MaterialData getMaterialData() throws MaterialNotFoundException {
        short blockData = getRawBlockDataValue();
        if (blockData < 0 || blockData > BlockDataMaterialMap.MAX_BLOCK_DATA) {
            // Maybe used as armor/tool damage value?
            blockData = 0;
        }
        if (this.isBlockIdInStringFormat()) {
            String blockId = tag.getString(BLOCK_ID_TAG);
            try {
                return materialMap.getMaterialData(blockId, (byte) blockData);
            } catch (MaterialNotFoundException e) {
                // Try without block data, maybe block data is used as damage
                // value
                return materialMap.getMaterialData(blockId, (byte) 0);
            }
        } else {
            short blockId = tag.getShort(OLD_BLOCK_ID_TAG);
            try {
                return materialMap.getMaterialData(blockId, (byte) blockData);
            } catch (MaterialNotFoundException e) {
                // Try without block data, maybe block data is used as damage
                // value
                return materialMap.getMaterialData(blockId, (byte) 0);
            }
        }

    }

    /**
     * Gets the raw block data of this item.
     *
     * @return The block data.
     */
    public short getRawBlockDataValue() {
        return tag.getShort(BLOCK_DATA_TAG);
    }

    /**
     * Gets whether the given material data matches the material data of this
     * item stack.
     *
     * @param materialData
     *            The material data.
     * @return True if the material matches, false otherwise.
     */
    @Override
    public boolean hasMaterialData(MaterialData materialData) {
        try {
            return materialData.equals(this.getMaterialData());
        } catch (MaterialNotFoundException e) {
            return false;
        }
    }

    private boolean isBlockIdInStringFormat() {
        return tag.isType(BLOCK_ID_TAG, TagType.STRING);
    }

    @Override
    public void setCount(byte count) {
        tag.setByte(COUNT_TAG, count);
    }

    @Override
    public void setMaterialData(MaterialData materialData) throws MaterialNotFoundException {
        char anvilId = materialMap.getMinecraftId(materialData);
        if (isBlockIdInStringFormat()) {
            String baseBlockName = materialMap.getBaseName(materialData);
            tag.setString(BLOCK_ID_TAG, baseBlockName);
        } else {
            tag.setShort(OLD_BLOCK_ID_TAG, (short) (anvilId >> 4));
        }
        tag.setShort(BLOCK_DATA_TAG, (short) (anvilId & 0xf));
    }

    @Override
    public String toString() {
        String materialString;
        try {
            materialString = getMaterialData().toString();
        } catch (MaterialNotFoundException e) {
            materialString = tag.getString(BLOCK_ID_TAG) + ":" + tag.getShort(BLOCK_DATA_TAG);
        }
        return getClass().getSimpleName() + "(material=" + materialString + ", count=" + getCount() + ")";
    }
}
