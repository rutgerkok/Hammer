package nl.rutgerkok.hammer.material;

/**
 * Material lookup for a world. This class doesn't contain much methods, finding
 * materials by their name can be done through {@link GlobalMaterialMap},
 * finding materials by their world-specific id can be done trough classes
 * implementing this interface.
 */
public interface WorldMaterialMap {

    static final String MINECRAFT_PREFIX = "minecraft:";

    /**
     * Gets the material representing air.
     *
     * @return The material representing air.
     */
    MaterialData getAir();

    /**
     * Gets the global material map belonging to this world.
     * 
     * @return The global map.
     */
    GlobalMaterialMap getGlobal();

}
