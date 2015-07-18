package nl.rutgerkok.hammer.pocket.tag;

import nl.rutgerkok.hammer.tag.CompoundKey;
import nl.rutgerkok.hammer.tag.CompoundTag;
import nl.rutgerkok.hammer.tag.ListTag;

public final class PocketFormat {

    /**
     * Keys from the level.dat file.
     *
     */
    public static class LevelTag {
        public static final CompoundKey<Long> LAST_PLAYED = CompoundKey.of("LastPlayed");
        public static final CompoundKey<String> LEVEL_NAME = CompoundKey.of("LevelName");
    }

    /**
     * Tag keys appearing in tile entity tags.
     */
    public static class TileEntityTag {
        public static final CompoundKey<String> ID = CompoundKey.of("id");
        public static final CompoundKey<ListTag<CompoundTag>> ITEMS = CompoundKey.of("Items");
        public static final CompoundKey<Integer> X_POS = CompoundKey.of("x");
        public static final CompoundKey<Integer> Y_POS = CompoundKey.of("y");
        public static final CompoundKey<Integer> Z_POS = CompoundKey.of("z");
    }

    public static final int VERSION = 4;
}
