package nl.rutgerkok.hammer.anvil;

/**
 * The data version of individual chunks.
 *
 */
public final class ChunkDataVersion implements Comparable<ChunkDataVersion> {

    public static final ChunkDataVersion MINECRAFT_1_9 = new ChunkDataVersion(169);
    public static final ChunkDataVersion MINECRAFT_1_9_1 = new ChunkDataVersion(175);
    public static final ChunkDataVersion MINECRAFT_1_9_2 = new ChunkDataVersion(176);
    public static final ChunkDataVersion MINECRAFT_1_9_3 = new ChunkDataVersion(183);
    public static final ChunkDataVersion MINECRAFT_1_9_4 = new ChunkDataVersion(184);
    public static final ChunkDataVersion MINECRAFT_1_10 = new ChunkDataVersion(510);
    public static final ChunkDataVersion MINECRAFT_1_10_1 = new ChunkDataVersion(511);
    public static final ChunkDataVersion MINECRAFT_1_10_2 = new ChunkDataVersion(512);
    public static final ChunkDataVersion MINECRAFT_1_11 = new ChunkDataVersion(819);
    public static final ChunkDataVersion MINECRAFT_1_11_1 = new ChunkDataVersion(921);
    public static final ChunkDataVersion MINECRAFT_1_11_2 = new ChunkDataVersion(922);
    public static final ChunkDataVersion MINECRAFT_1_12 = new ChunkDataVersion(1139);
    public static final ChunkDataVersion MINECRAFT_1_12_1 = new ChunkDataVersion(1241);
    public static final ChunkDataVersion MINECRAFT_1_12_2 = new ChunkDataVersion(1343);

    /**
     * From here on, block ids and data where replaced by block states.
     */
    public static final ChunkDataVersion MINECRAFT_FLATTENING = new ChunkDataVersion(1451);
    public static final ChunkDataVersion MINECRAFT_1_13 = new ChunkDataVersion(1519);
    public static final ChunkDataVersion MINECRAFT_1_13_1 = new ChunkDataVersion(1628);
    public static final ChunkDataVersion MINECRAFT_1_13_2 = new ChunkDataVersion(1631);
    public static final ChunkDataVersion MINECRAFT_1_14 = new ChunkDataVersion(1952);
    public static final ChunkDataVersion MINECRAFT_1_14_1 = new ChunkDataVersion(1957);
    public static final ChunkDataVersion MINECRAFT_1_14_2 = new ChunkDataVersion(1963);
    public static final ChunkDataVersion MINECRAFT_1_14_3 = new ChunkDataVersion(1968);
    public static final ChunkDataVersion MINECRAFT_1_14_4 = new ChunkDataVersion(1976);
    public static final ChunkDataVersion MINECRAFT_1_15 = new ChunkDataVersion(2225);
    public static final ChunkDataVersion MINECRAFT_1_15_1 = new ChunkDataVersion(2227);
    public static final ChunkDataVersion MINECRAFT_1_15_2 = new ChunkDataVersion(2230);
    public static final ChunkDataVersion MINECRAFT_1_16 = new ChunkDataVersion(2566);
    public static final ChunkDataVersion MINECRAFT_1_16_1 = new ChunkDataVersion(2567);
    public static final ChunkDataVersion MINECRAFT_1_16_2 = new ChunkDataVersion(2578);
    public static final ChunkDataVersion MINECRAFT_1_16_3 = new ChunkDataVersion(2580);
    public static final ChunkDataVersion MINECRAFT_1_16_4 = new ChunkDataVersion(2584);
    public static final ChunkDataVersion MINECRAFT_1_16_5 = new ChunkDataVersion(2586);
    public static final ChunkDataVersion MINECRAFT_ENTITY_SEPARATION = new ChunkDataVersion(2681);

    /**
     * Gets the chunk data version from the version stored in the chunk root tag.
     *
     * @param versionId
     *            The version id.
     * @return The version.
     * @throws IllegalArgumentException
     *             If the id is negative.
     */
    public static ChunkDataVersion fromId(int versionId) {
        return new ChunkDataVersion(versionId);
    }

    /**
     * Gets the newest supported world format.
     *
     * @return The format.
     */
    public static ChunkDataVersion latest() {
        return new ChunkDataVersion(2716); // 1.17 pre-release 1
    }

    private final int id;

    private ChunkDataVersion(int id) {
        this.id = id;
        if (id < 0) {
            throw new IllegalArgumentException("Id cannot be negative: " + id);
        }
    }

    @Override
    public int compareTo(ChunkDataVersion o) {
        return Integer.compare(id, o.id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ChunkDataVersion other = (ChunkDataVersion) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }

    /**
     * Gets the raw id of the version.
     *
     * @return The raw id.
     */
    public int getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return id * 31;
    }

    /**
     * Returns true if this data version is from after the other.
     *
     * @param other
     *            The other.
     * @return True if the data version is from after, false if it's equal or
     *         before.
     */
    public boolean isAfter(ChunkDataVersion other) {
        return this.id > other.id;
    }

    /**
     * Returns true if this data version is from before the other.
     *
     * @param other
     *            The other.
     * @return True if the data version is from before, false if it's equal or
     *         after.
     */
    public boolean isBefore(ChunkDataVersion other) {
        return this.id < other.id;
    }

    @Override
    public String toString() {
        return "ChunkDataVersion [id=" + id + "]";
    }
}
