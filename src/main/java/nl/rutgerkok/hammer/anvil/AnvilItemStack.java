package nl.rutgerkok.hammer.anvil;

import java.util.Objects;

import nl.rutgerkok.hammer.GameFactory;
import nl.rutgerkok.hammer.ItemStack;
import nl.rutgerkok.hammer.material.BlockDataMaterialMap;
import nl.rutgerkok.hammer.material.MaterialData;
import nl.rutgerkok.hammer.material.MaterialName;
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

    private enum Format {
        BLOCK_ID_DATA,
        BLOCK_NAME_DATA,
        BLOCK_NAME
    }

    private static final CompoundKey<Short> BLOCK_DATA_TAG = CompoundKey.of("Damage");
    private static final CompoundKey<String> BLOCK_ID_TAG = CompoundKey.of("id");
    private static final CompoundKey<Byte> COUNT_TAG = CompoundKey.of("Count");
    private static final CompoundKey<Short> OLD_BLOCK_ID_TAG = CompoundKey.of("id");

    private final AnvilMaterialMap materialMap;
    private final CompoundTag tag;

    AnvilItemStack(AnvilMaterialMap materialMap, CompoundTag tag) {
        this.materialMap = Objects.requireNonNull(materialMap);
        this.tag = Objects.requireNonNull(tag);
    }

    @Override
    public byte getCount() {
        return tag.getByte(COUNT_TAG);
    }


    private Format getFormat() {
        if (tag.isType(BLOCK_ID_TAG, TagType.STRING)) {
            if (tag.containsKey(BLOCK_DATA_TAG)) {
                return Format.BLOCK_NAME_DATA;
            }
            return Format.BLOCK_NAME;
        }
        return Format.BLOCK_ID_DATA;
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
        switch (this.getFormat()) {
            case BLOCK_ID_DATA:
                short blockData = getRawBlockDataValue();
                if (blockData < 0 || blockData > BlockDataMaterialMap.MAX_BLOCK_DATA) {
                    // Maybe used as armor/tool damage value?
                    blockData = 0;
                }

                short blockId = tag.getShort(OLD_BLOCK_ID_TAG);
                try {
                    return materialMap.getMaterialDataFromOldIds(blockId, (byte) blockData);
                } catch (MaterialNotFoundException e) {
                    // Try without block data, maybe block data is used as damage
                    // value
                    return materialMap.getMaterialDataFromOldIds(blockId, (byte) 0);
                }
            case BLOCK_NAME:
                blockData = getRawBlockDataValue();
                if (blockData < 0 || blockData > BlockDataMaterialMap.MAX_BLOCK_DATA) {
                    // Maybe used as armor/tool damage value?
                    blockData = 0;
                }

                String blockName = tag.getString(BLOCK_ID_TAG);
                try {
                    return materialMap.getMaterialDataFromOldIds(blockName, (byte) blockData);
                } catch (MaterialNotFoundException e) {
                    // Ignore block data, add whatever we find
                    return materialMap.getGlobal().addMaterial(MaterialName.ofBaseName(blockName));
                }
            case BLOCK_NAME_DATA:
                blockName = tag.getString(BLOCK_ID_TAG);
                return materialMap.getGlobal().addMaterial(MaterialName.ofBaseName(blockName));
            default:
                throw new RuntimeException("Unknown stack format: " + this.getFormat());
        }


    }

    /**
     * Gets the raw block data of this item. Minecraft 1.13 and newer don't use
     * block data, so for items touched by those versions this will return 0.
     *
     * @return The block data.
     * @see #getMaterialData() Method that works in any Minecraft version.
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

    @Override
    public void setCount(byte count) {
        tag.setByte(COUNT_TAG, count);
    }

    @Override
    public void setMaterialData(MaterialData materialData) throws MaterialNotFoundException {
        switch (getFormat()) {
            case BLOCK_ID_DATA:
                char anvilId = materialMap.getOldMinecraftId(materialData);
                tag.setShort(OLD_BLOCK_ID_TAG, (short) (anvilId >> 4));
                tag.setShort(BLOCK_DATA_TAG, (short) (anvilId & 0xf));
                break;
            case BLOCK_NAME:
                tag.setString(BLOCK_ID_TAG, materialData.getBaseName());
                break;
            case BLOCK_NAME_DATA:
                char anvilIdForData = materialMap.getOldMinecraftId(materialData);
                tag.setString(BLOCK_ID_TAG, materialMap.getOldMinecraftIdString(materialData).getBaseName());
                tag.setShort(BLOCK_DATA_TAG, (short) (anvilIdForData & 0xf));
                break;
            default:
                throw new RuntimeException("Unknown format: " + getFormat());
        }
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
