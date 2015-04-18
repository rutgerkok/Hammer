package nl.rutgerkok.hammer.pocket.tag;

import nl.rutgerkok.hammer.tag.CompoundKey;

public final class PocketTagFormat {

    // Level.dat root tag
    public static final CompoundKey LEVEL_LAST_PLAYED = CompoundKey.of("LastPlayed");

    public static final CompoundKey LEVEL_NAME = CompoundKey.of("LevelName");
    public static final int VERSION = 4;
}
