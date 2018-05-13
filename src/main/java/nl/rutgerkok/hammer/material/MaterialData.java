package nl.rutgerkok.hammer.material;

import java.util.Collection;
import java.util.Objects;

/**
 * Represents the material of a block.
 *
 * @see GlobalMaterialMap#addMaterial(Collection) Creating instances
 */
public final class MaterialData {

    private final char idh;
    private final MaterialName name;

    MaterialData(char idh, MaterialName name) {
        this.idh = idh;
        this.name = Objects.requireNonNull(name);
    }

    /**
     * Gets the base name, like "minecraft:grass" in "minecraft:grass[snowy=false]"
     * @return The base name.
     */
    public String getBaseName() {
        return name.getBaseName();
    }

    /**
     * Gets the Hammer id of this material.
     *
     * @return The Hammer id.
     */
    public char getId() {
        return idh;
    }

    /**
     * Gets the parsed material name.
     * @return The material name.
     */
    public MaterialName getMaterialName() {
        return name;
    }

    /**
     * Gets the (Mojang/mod provided) name of this material, like "minecraft:grass[snowy=false]".
     *
     * @return The name.
     */
    public String getName() {
        return name.toString();
    }

    /**
     * {@inheritDoc}
     *
     * <p>For this class, the return value is currently equal to
     * {@link #getName()}, but this may change in the future.
     *
     * @return {@inheritDoc}
     * @see #getName()
     */
    @Override
    public String toString() {
        return name.toString();
    }
}
