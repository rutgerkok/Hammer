package nl.rutgerkok.hammer.material;

import nl.rutgerkok.hammer.util.MaterialNotFoundException;

/**
 * Looks up materials.
 */
public interface MaterialMap {

    static final String MINECRAFT_PREFIX = "minecraft:";

    /**
     * Gets the material representing air.
     *
     * @return The material representing air.
     */
    Material getAir();

    /**
     * Gets a material with the given id.
     *
     * @param id
     *            Id of the material.
     * @return The material.
     * @throws MaterialNotFoundException
     *             If no such material exists.
     */
    Material getById(int id) throws MaterialNotFoundException;

    /**
     * Gets a material with the given name.
     *
     * @param name
     *            Name of the material.
     * @return The material.
     * @throws MaterialNotFoundException
     *             If no such material exists.
     */
    Material getByName(String name) throws MaterialNotFoundException;

    /**
     * Gets the material with the given name or id.
     *
     * @param nameOrId
     *            The name, or a numeric string with the id of the material.
     * @return The material.
     * @throws MaterialNotFoundException
     *             If the material is not found.
     */
    Material getByNameOrId(String nameOrId) throws MaterialNotFoundException;

}
