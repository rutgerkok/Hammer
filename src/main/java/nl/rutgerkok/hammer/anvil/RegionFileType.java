package nl.rutgerkok.hammer.anvil;

import java.util.Objects;

/**
 * The various types of region files that Minecraft uses.
 */
public enum RegionFileType {

    CHUNK("region"),
    ENTITY("entities"),
    POINT_OF_INTEREST("poi");

    final String folderName;

    private RegionFileType(String folderName) {
        this.folderName = Objects.requireNonNull(folderName, "folderName");
    }
}
