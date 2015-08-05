package nl.rutgerkok.hammer;

import nl.rutgerkok.hammer.material.MaterialData;
import nl.rutgerkok.hammer.util.MaterialNotFoundException;

/**
 * An item stack holds one or more items of the same type.
 *
 */
public interface ItemStack {

    /**
     * Gets the amount of items in this stack.
     *
     * @return The amount of items.
     */
    byte getCount();

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
    MaterialData getMaterialData() throws MaterialNotFoundException;

    /**
     * Gets whether the given material data matches the material data of this
     * item stack.
     *
     * @param materialData
     *            The material data.
     * @return True if the material matches, false otherwise.
     */
    boolean hasMaterialData(MaterialData materialData);

    /**
     * Sets the amount of items in this stack.
     *
     * @param count
     *            The new count. The number is simply saved to the NBT tag, even
     *            if the number is invalid.
     */
    void setCount(byte count);

    /**
     * Sets the material of this stack.
     *
     * @param materialData
     *            The material.
     * @throws MaterialNotFoundException
     *             If the material is not supported in this world.
     */
    void setMaterialData(MaterialData materialData) throws MaterialNotFoundException;

}