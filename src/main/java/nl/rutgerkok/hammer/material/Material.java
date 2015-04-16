package nl.rutgerkok.hammer.material;

/**
 * One of Minecraft's materials.
 *
 */
public interface Material {

    /**
     * Gets the id of this material.
     *
     * @return The id.
     */
    short getId();

    /**
     * Gets the name of this material.
     *
     * @return The name.
     */
    String getName();

    /**
     * Gets a {@link MaterialData} instance with an unspecified block data.
     *
     * @return The material data instance.
     */
    MaterialData withAnyData();

}