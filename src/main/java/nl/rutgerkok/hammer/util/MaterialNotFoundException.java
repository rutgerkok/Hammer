package nl.rutgerkok.hammer.util;

/**
 * Thrown when a material is not found by the material map.
 *
 */
public class MaterialNotFoundException extends RuntimeException {

    private static final long serialVersionUID = -6301652887983469894L;

    /**
     * Constructs a new exception with the given material id.
     *
     * @param materialId
     *            The material id.
     */
    public MaterialNotFoundException(int materialId) {
        super(Integer.toString(materialId));
    }

    /**
     * Constructs a new exception with the given material name.
     *
     * @param materialName
     *            The material name.
     */
    public MaterialNotFoundException(String materialName) {
        super(materialName);
    }

    /**
     * Constructs a new exception with the given material id and data.
     *
     * @param blockId
     *            The id of the block.
     * @param blockData
     *            The block data value of the block.
     */
    public MaterialNotFoundException(short blockId, byte blockData) {
        super(blockId + ":" + blockData);
    }

    /**
     * Gets the name of the material that was not found.
     *
     * @return The name.
     */
    public String getMaterial() {
        return getMessage();
    }
}
