package nl.rutgerkok.hammer;

import nl.rutgerkok.hammer.material.MaterialMap;
import nl.rutgerkok.hammer.tag.CompoundTag;

/**
 * Factory for creating game objects.
 *
 */
public interface GameFactory {

    /**
     * Creates an item stack from the given compound tag.
     *
     * @param tag
     *            The tag.
     * @return The item stack.
     */
    ItemStack createItemStack(CompoundTag tag);

    /**
     * Gets the material map.
     * 
     * @return The material map.
     */
    MaterialMap getMaterialMap();
}
