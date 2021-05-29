package nl.rutgerkok.hammer.anvil;

import java.util.Objects;

import nl.rutgerkok.hammer.GameFactory;
import nl.rutgerkok.hammer.ItemStack;
import nl.rutgerkok.hammer.material.MaterialData;
import nl.rutgerkok.hammer.material.MaterialName;
import nl.rutgerkok.hammer.material.WorldMaterialMap;
import nl.rutgerkok.hammer.tag.CompoundKey;
import nl.rutgerkok.hammer.tag.CompoundTag;
import nl.rutgerkok.hammer.util.MaterialNotFoundException;

/**
 * Implementation of {@link ItemStack} for Anvil worlds.
 *
 *
 * @see GameFactory#createItemStack(CompoundTag)
 *
 */
final class AnvilItemStack implements ItemStack {

    private static final CompoundKey<String> BLOCK_ID_TAG = CompoundKey.of("id");
    private static final CompoundKey<Byte> COUNT_TAG = CompoundKey.of("Count");

    private final WorldMaterialMap materialMap;
    private final CompoundTag tag;

    AnvilItemStack(WorldMaterialMap materialMap, CompoundTag tag) {
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
     *             If no block material is present. Usually happens in the case of
     *             item materials.
     * @throws NullPointerException
     *             If the material map is null.
     */
    @Override
    public MaterialData getMaterialData() throws MaterialNotFoundException {
        String blockName = tag.getString(BLOCK_ID_TAG);
        try {
            return materialMap.getGlobal().getMaterialByName(blockName);
        } catch (MaterialNotFoundException e) {
            // Ignore block data, add whatever we find
            return materialMap.getGlobal().addMaterial(MaterialName.ofBaseName(blockName));
        }
    }

    /**
     * Gets whether the given material data matches the material data of this item
     * stack.
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

    @Override
    public void setCount(byte count) {
        tag.setByte(COUNT_TAG, count);
    }

    @Override
    public void setMaterialData(MaterialData materialData) throws MaterialNotFoundException {
        tag.setString(BLOCK_ID_TAG, materialData.getBaseName());
    }

    @Override
    public String toString() {
        String materialString;
        try {
            materialString = getMaterialData().toString();
        } catch (MaterialNotFoundException e) {
            materialString = tag.getString(BLOCK_ID_TAG);
        }
        return getClass().getSimpleName() + "(material=" + materialString + ", count=" + getCount() + ")";
    }
}
