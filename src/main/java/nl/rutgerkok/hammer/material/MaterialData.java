package nl.rutgerkok.hammer.material;

/**
 * A combination between a base {@link Material} and some further block data.
 *
 */
public interface MaterialData {

    /**
     * Gets the material data number. The data numbers are deprecated, and will
     * probably be removed in a future update. However, there's no alternative
     * yet.
     *
     * @return The material data.
     */
    byte getData();

    /**
     * Gets the material part of this MaterialData.
     *
     * @return The material.
     */
    Material getMaterial();

    /**
     * Gets whether the block data of this object is a wildcard. If it is, it
     * will {@link #matches(MaterialData) match} any other block data value.
     *
     * @return True if this block data is a wildcard, false otherwise.
     */
    boolean isBlockDataUnspecified();

    /**
     * Gets if the other block data matches the block data of this material
     * data. It matches if the block data of this material data
     * {@link #isBlockDataUnspecified() is unspecified}, or if the block data
     * has the same number.
     *
     * @param blockData
     *            The other block data.
     * @return True if they match, false otherwise.
     */
    boolean matches(MaterialData blockData);

    /**
     * Gets whether the given block name matches the name of
     * {@link #getMaterial() the material part} of this material data. Case
     * sensitive.
     *
     * @param blockName
     *            Name of the block to check.
     * @return True if the name matches, false otherwise.
     */
    boolean materialNameEquals(String blockName);

}