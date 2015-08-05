package nl.rutgerkok.hammer.pocket;

import java.util.Objects;

import nl.rutgerkok.hammer.GameFactory;
import nl.rutgerkok.hammer.ItemStack;
import nl.rutgerkok.hammer.material.BlockDataMaterialMap;
import nl.rutgerkok.hammer.material.MaterialData;
import nl.rutgerkok.hammer.tag.CompoundKey;
import nl.rutgerkok.hammer.tag.CompoundTag;
import nl.rutgerkok.hammer.util.MaterialNotFoundException;

/**
 * Implementation of {@link ItemStack} for the Pocket Edition.
 *
 * @see GameFactory#createItemStack(CompoundTag)
 */
final class PocketItemStack implements ItemStack {
    private static final CompoundKey<Short> BLOCK_DATA_TAG = CompoundKey.of("Damage");
    private static final CompoundKey<Short> BLOCK_ID_TAG = CompoundKey.of("id");
    private static final CompoundKey<Byte> COUNT_TAG = CompoundKey.of("Count");

    private final BlockDataMaterialMap materialMap;

    private final CompoundTag tag;

    public PocketItemStack(BlockDataMaterialMap materialMap, CompoundTag tag) {
        this.materialMap = Objects.requireNonNull(materialMap, "materialMap");
        this.tag = Objects.requireNonNull(tag, "tag");
    }

    @Override
    public byte getCount() {
        return tag.getByte(COUNT_TAG);
    }

    @Override
    public MaterialData getMaterialData() throws MaterialNotFoundException {
        short id = tag.getShort(BLOCK_ID_TAG);
        byte data = (byte) tag.getShort(BLOCK_DATA_TAG);
        return materialMap.getMaterialData(id, data);
    }

    @Override
    public boolean hasMaterialData(MaterialData materialData) {
        try {
            return getMaterialData().equals(materialData);
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
        char minecraftId = materialMap.getMinecraftId(materialData);
        tag.setShort(BLOCK_ID_TAG, (short) (minecraftId >> 4));
        tag.setShort(BLOCK_DATA_TAG, (short) (minecraftId & 0xf));
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
