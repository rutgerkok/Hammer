package nl.rutgerkok.hammer.pocket.tag;

import nl.rutgerkok.hammer.tag.CompoundKey;

public final class PocketFormat {

    /**
     * Keys from the level.dat file.
     *
     */
    public static class LevelTag {
        public static final CompoundKey LAST_PLAYED = CompoundKey.of("LastPlayed");
        public static final CompoundKey LEVEL_NAME = CompoundKey.of("LevelName");
    }

    /**
     * Tag keys appearing in tile entity tags.
     */
    public static class TileEntityTag {
        public static final CompoundKey ID = CompoundKey.of("id");
        public static final CompoundKey ITEMS = CompoundKey.of("Items");
        public static final CompoundKey X_POS = CompoundKey.of("x");
        public static final CompoundKey Y_POS = CompoundKey.of("y");
        public static final CompoundKey Z_POS = CompoundKey.of("z");
    }

    public static final int VERSION = 4;
}
