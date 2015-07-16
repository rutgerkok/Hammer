package nl.rutgerkok.hammer.pocket;

import java.util.Objects;

import nl.rutgerkok.hammer.GameFactory;
import nl.rutgerkok.hammer.ItemStack;
import nl.rutgerkok.hammer.anvil.material.AnvilMaterialData;
import nl.rutgerkok.hammer.material.Material;
import nl.rutgerkok.hammer.material.MaterialData;
import nl.rutgerkok.hammer.material.MaterialMap;
import nl.rutgerkok.hammer.tag.CompoundKey;
import nl.rutgerkok.hammer.tag.CompoundTag;
import nl.rutgerkok.hammer.util.MaterialNotFoundException;

/**
 * Implementation of {@link ItemStack} for the Pocket Edition.
 *
 * @see GameFactory#createItemStack(CompoundTag)
 */
final class PocketItemStack implements ItemStack {
    private static final CompoundKey BLOCK_DATA_TAG = CompoundKey.of("Damage");
    private static final CompoundKey BLOCK_ID_TAG = CompoundKey.of("id");

    private static final CompoundKey COUNT_TAG = CompoundKey.of("Count");
    private final MaterialMap materialMap;

    private final CompoundTag tag;

    public PocketItemStack(MaterialMap materialMap, CompoundTag tag) {
        this.materialMap = Objects.requireNonNull(materialMap, "materialMap");
        this.tag = Objects.requireNonNull(tag, "tag");
    }

    @Override
    public byte getCount() {
        return tag.getByte(COUNT_TAG);
    }

    @Override
    public Material getMaterial() throws MaterialNotFoundException {
        short id = tag.getShort(BLOCK_ID_TAG);
        return materialMap.getById(id);
    }

    @Override
    public MaterialData getMaterialData() throws MaterialNotFoundException {
        byte data = tag.getByte(BLOCK_DATA_TAG);
        // We're assuming that Pocket and Anvil material data are compatible
        // here. This assumption is mostly correct, except for some edge cases,
        // like double slabs.
        return AnvilMaterialData.of(getMaterial(), data);
    }

    @Override
    public boolean hasMaterialData(MaterialData materialData) {
        if (materialData.getMaterial().getId() != tag.getShort(BLOCK_ID_TAG)) {
            return false;
        }
        if (materialData.isBlockDataUnspecified()) {
            return true;
        }
        return materialData.getData() == tag.getByte(BLOCK_DATA_TAG);
    }

    @Override
    public void setCount(byte count) {
        tag.setByte(COUNT_TAG, count);
    }

    @Override
    public void setMaterialData(MaterialData materialData) {
        tag.setShort(BLOCK_ID_TAG, materialData.getMaterial().getId());
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
