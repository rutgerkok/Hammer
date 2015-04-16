package nl.rutgerkok.hammer;

import java.util.Objects;

import nl.rutgerkok.hammer.material.MaterialMap;
import nl.rutgerkok.hammer.tag.CompoundTag;

/**
 * Represents the data file of a player.
 *
 */
public final class PlayerFile {

    private final MaterialMap materialMap;
    private final CompoundTag tag;

    /**
     * Creates a data file
     *
     * @param materialMap
     * @param tag
     */
    public PlayerFile(MaterialMap materialMap, CompoundTag tag) {
        this.materialMap = Objects.requireNonNull(materialMap);
        this.tag = Objects.requireNonNull(tag);
    }

    /**
     * Gets the material map used for this player file.
     *
     * @return The material map.
     */
    public MaterialMap getMaterialMap() {
        return materialMap;
    }

    /**
     * Gets the tag inside the player file.
     *
     * @return The tag, with sub tags like Inventory, EnderItems, etc.
     */
    public CompoundTag getTag() {
        return tag;
    }
}
