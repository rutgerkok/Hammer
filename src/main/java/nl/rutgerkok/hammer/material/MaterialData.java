package nl.rutgerkok.hammer.material;

import java.util.Collection;
import java.util.Objects;

/**
 * Represents the material of a block.
 *
 * @see GlobalMaterialMap#addMaterial(Collection) Creating instances
 */
public final class MaterialData {
    MaterialData(char idh, String name) {
        this.idh = idh;
        this.name = Objects.requireNonNull(name);
    }

    private final char idh;
    private final String name;

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
        return name;
    }

    /**
     * Gets the (Mojang/mod provided) name of this material.
     * 
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the Hammer id of this material.
     * 
     * @return The Hammer id.
     */
    public char getId() {
        return idh;
    }
}
