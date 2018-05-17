package nl.rutgerkok.hammer.util;

import java.text.ParseException;

import nl.rutgerkok.hammer.material.MaterialName;

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
    public MaterialNotFoundException(MaterialName materialName) {
        super(materialName.toString());
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
     * Constructs a new exception with the given raw material name.
     *
     * @param materialName
     *            The raw material name.
     * @param e
     *            The reason why parsing failed.
     */
    public MaterialNotFoundException(String materialName, ParseException e) {
        super(materialName, e);
    }

    /**
     * Gets the name of the material that was not found.
     *
     * @return The name.
     */
    public String getMaterial() {
        return super.getMessage();
    }

    @Override
    public String getMessage() {
        Throwable cause = this.getCause();
        if (cause instanceof ParseException) {
            int charNumber = ((ParseException) cause).getErrorOffset();
            return "Invalid material " + super.getMessage() + "; error at char " + charNumber + ": "
                    + cause.getMessage();
        }
        return super.getMessage();
    }
}
