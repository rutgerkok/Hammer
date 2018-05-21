package nl.rutgerkok.hammer.anvil;

/**
 * The data version of individual chunks.
 *
 */
public final class ChunkDataVersion implements Comparable<ChunkDataVersion> {

    public static final ChunkDataVersion MINECRAFT_1_12_2 = new ChunkDataVersion(1343);

    /**
     * Gets the chunk data version from the version stored in the chunk root
     * tag.
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
        return new ChunkDataVersion(1493); // Snapshot 18w20c
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
     * @return The raw id.
     */
    public int getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return id * 31;
    }

    @Override
    public String toString() {
        return "ChunkDataVersion [id=" + id + "]";
    }
}
