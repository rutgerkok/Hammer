package nl.rutgerkok.hammer.anvil.material;

import nl.rutgerkok.hammer.material.Material;
import nl.rutgerkok.hammer.material.MaterialData;

/**
 * Represents one of Minecraft's materials in the anvil world format.
 *
 */
public final class AnvilMaterial implements Material {

    public static final int AIR_ID = 0;

    private final short id;
    private final String name;

    /**
     * Creates a new material.
     *
     * @param name
     *            Name of the material, may not be null.
     * @param id
     *            Id of the material.
     */
    public AnvilMaterial(String name, int id) {
        this.name = name;
        this.id = (short) id;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof AnvilMaterial && ((AnvilMaterial) other).id == id;
    }

    @Override
    public short getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public MaterialData withAnyData() {
        return AnvilMaterialData.ofAnyState(this);
    }
}
