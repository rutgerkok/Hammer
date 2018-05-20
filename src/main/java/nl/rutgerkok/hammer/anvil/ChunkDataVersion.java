package nl.rutgerkok.hammer.anvil;

/**
 * The data version of individual chunks.
 *
 */
public enum ChunkDataVersion {
    /**
     * Used from Minecraft 1.2 to 1.12, inclusive.
     */
    ORIGINAL_ANVIL,
    /**
     * Used in Minecraft 1.13 and newer. No more id + data byte combinations,
     * now a flattened array of just block ids is used.
     */
    FLAT_ANVIL;

    /**
     * Gets the chunk data version from the version stored in the chunk root tag.
     * @param versionId The version id.
     * @return The version.
     */
    public static ChunkDataVersion fromId(int versionId) {
        if (versionId <= 1343) {
            return ORIGINAL_ANVIL;
        }
        return FLAT_ANVIL;
    }

    /**
     * Gets the newest supported world format.
     *
     * @return The format.
     */
    public static ChunkDataVersion latest() {
        return FLAT_ANVIL;
    }
}
