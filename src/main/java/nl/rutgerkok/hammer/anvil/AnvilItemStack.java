package nl.rutgerkok.hammer.anvil;

import java.util.Objects;

import nl.rutgerkok.hammer.GameFactory;
import nl.rutgerkok.hammer.ItemStack;
import nl.rutgerkok.hammer.anvil.material.AnvilMaterialData;
import nl.rutgerkok.hammer.material.Material;
import nl.rutgerkok.hammer.material.MaterialData;
import nl.rutgerkok.hammer.material.MaterialMap;
import nl.rutgerkok.hammer.tag.CompoundKey;
import nl.rutgerkok.hammer.tag.CompoundTag;
import nl.rutgerkok.hammer.tag.TagType;
import nl.rutgerkok.hammer.util.MaterialNotFoundException;

/**
 * Implementation of {@link ItemStack} for Anvil worlds.
 *
 * <p>
 * Both the Minecraft 1.8+ format with named ids and the Minecraft 1.2 - 1.7
 * format with numeric ids are supported.
 * </p>
 * 
 * @see GameFactory#createItemStack(CompoundTag)
 *
 */
final class AnvilItemStack implements ItemStack {
    private static final CompoundKey BLOCK_DATA_TAG = CompoundKey.of("Damage");
    private static final CompoundKey BLOCK_ID_TAG = CompoundKey.of("id");
    private static final CompoundKey COUNT_TAG = CompoundKey.of("Count");

    private final MaterialMap materialMap;
    private final CompoundTag tag;

    AnvilItemStack(MaterialMap materialMap, CompoundTag tag) {
        this.materialMap = Objects.requireNonNull(materialMap);
        this.tag = Objects.requireNonNull(tag);
    }

    @Override
    public byte getCount() {
        return tag.getByte(COUNT_TAG);
    }

    /**
     * Gets the material of this tag.
     *
     * @return The material.
     * @throws MaterialNotFoundException
     *             If no block material is present. Usually happens in the case
     *             of item materials.
     * @throws NullPointerException
     *             If the material map is null.
     */
    @Override
    public Material getMaterial() throws MaterialNotFoundException {
        if (isBlockIdInStringFormat()) {
            // Try as string
            return materialMap.getByName(tag.getString(BLOCK_ID_TAG));
        } else {
            // Try as number
            return materialMap.getById(tag.getShort(BLOCK_ID_TAG));
        }
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
        Material material = getMaterial();
        short blockData = getRawBlockDataValue();
        if (blockData < AnvilMaterialData.MIN_BLOCK_DATA || blockData > AnvilMaterialData.MAX_BLOCK_DATA) {
            throw new MaterialNotFoundException("Block data: " + blockData);
        }
        return AnvilMaterialData.of(material, (byte) blockData);
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
            return materialData.matches(this.getMaterialData());
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

    /**
     * Sets the material of this stack.
     *
     * @param materialData
     *            The material.
     * @throws NullPointerException
     *             If the material is null.
     */
    @Override
    public void setMaterialData(MaterialData materialData) {
        if (isBlockIdInStringFormat()) {
            tag.setString(BLOCK_ID_TAG, materialData.getMaterial().getName());
        } else {
            tag.setShort(BLOCK_ID_TAG, materialData.getMaterial().getId());
        }
        tag.setShort(BLOCK_DATA_TAG, materialData.getData());
    }

    @Override
    public String toString() {
        String materialString;
        try {
            materialString = getMaterialData().toString();
        } catch (MaterialNotFoundException e) {
            materialString = tag.getShort(BLOCK_ID_TAG) + ":" + tag.getShort(BLOCK_DATA_TAG);
        }
        return getClass().getSimpleName() + "(material=" + materialString + ", count=" + getCount() + ")";
    }
}
