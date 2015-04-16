package nl.rutgerkok.hammer.anvil.material;

import java.util.Objects;

import nl.rutgerkok.hammer.material.Material;
import nl.rutgerkok.hammer.material.MaterialData;

/**
 * Represents a Material + Data value as Bukkit calls it, or a Block State as
 * Minecraft 1.8 calls it.
 *
 *
 */
public final class AnvilMaterialData implements MaterialData {

    public static final byte MAX_BLOCK_DATA = 0xf;
    public static final byte MIN_BLOCK_DATA = 0x0;

    /**
     * Internally used to represent the unspecified state, never exposed, falls
     * outside the normal range of block data.
     */
    private static final byte UNSPECIFIED_BLOCK_DATA = -1;

    /**
     * Creates a new material data of the given material and data.
     *
     * @param material
     *            The material.
     * @param data
     *            The data.
     * @return The material and data.
     * @throws NullPointerException
     *             If the material is null.
     * @throws IllegalArgumentException
     *             If the block data is smaller than {@value #MIN_BLOCK_DATA} or
     *             larger than {@value #MAX_BLOCK_DATA}.
     */
    public static AnvilMaterialData of(Material material, byte data) {
        if (data < MIN_BLOCK_DATA || data > MAX_BLOCK_DATA) {
            throw new IllegalArgumentException("Block data out of bounds: " + data);
        }
        return new AnvilMaterialData(material, data);
    }

    /**
     * Creates a new material data of the default state of the given material.
     * However, {@link #matches(short)} will return true for any provided value.
     *
     * @param material
     *            The material.
     * @return The material data.
     * @throws NullPointerException
     *             If the material is null.
     */
    static AnvilMaterialData ofAnyState(AnvilMaterial material) {
        return new AnvilMaterialData(material, UNSPECIFIED_BLOCK_DATA);
    }

    private final byte data;
    private final Material material;

    private AnvilMaterialData(Material material, byte data) {
        this.material = Objects.requireNonNull(material);
        this.data = data;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AnvilMaterialData)) {
            return false;
        }
        AnvilMaterialData other = (AnvilMaterialData) obj;
        if (data != other.data) {
            return false;
        }
        if (!material.equals(other.material)) {
            return false;
        }
        return true;
    }

    @Override
    public byte getData() {
        if (data == UNSPECIFIED_BLOCK_DATA) {
            return MIN_BLOCK_DATA;
        }
        return data;
    }

    @Override
    public Material getMaterial() {
        return material;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + data;
        result = prime * result + ((material == null) ? 0 : material.hashCode());
        return result;
    }

    @Override
    public boolean isBlockDataUnspecified() {
        return data == UNSPECIFIED_BLOCK_DATA;
    }

    /**
     * Gets if the other block data matches the block data of this material
     * data. It matches if the block data of this material data
     * {@link #isBlockDataUnspecified() is unspecified}, or if the block data
     * has the same number.
     *
     * @param materialData
     *            The other block data.
     * @return True if they match, false otherwise.
     */
    @Override
    public boolean matches(MaterialData materialData) {
        if (!materialData.getMaterial().equals(this.material)) {
            // Not the same base type, no match
            return false;
        }

        if (this.data == UNSPECIFIED_BLOCK_DATA) {
            // Matches any subtype
            return true;
        }

        if (materialData instanceof AnvilMaterialData) {
            return this.data == ((AnvilMaterialData) materialData).data;
        }
        return false;
    }

    /**
     * Similar to {@link #matches(MaterialData)}, but only check the raw block
     * data.
     *
     * @param blockData
     *            The block data byte.
     * @return True if the block data matches, false otherwise.
     */
    public boolean matchesRawData(byte blockData) {
        if (data == UNSPECIFIED_BLOCK_DATA) {
            return true;
        }
        return data == blockData;
    }

    @Override
    public boolean materialNameEquals(String blockName) {
        return material.getName().equals(blockName);
    }

    @Override
    public String toString() {
        if (data == UNSPECIFIED_BLOCK_DATA) {
            return material.getName();
        }
        return material.getName() + ":" + data;
    }
}
