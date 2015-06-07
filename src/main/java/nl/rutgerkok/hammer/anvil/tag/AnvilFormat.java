package nl.rutgerkok.hammer.anvil.tag;

import nl.rutgerkok.hammer.tag.CompoundKey;

import com.google.common.collect.ImmutableList;

/**
 * Names of the various NBT tags of Minecraft's chunk format.
 *
 */
public final class AnvilFormat {

    /**
     * Chunk root tag format
     */
    public static class ChunkRootTag {
        public static final CompoundKey MINECRAFT = CompoundKey.of("Level");
    }

    /**
     * Format of the base chunk tag.
     */
    public static class ChunkTag {
        public static final CompoundKey BIOMES = CompoundKey.of("Biomes");
        public static final CompoundKey ENTITIES = CompoundKey.of("Entities");
        public static final CompoundKey INHABITED_TIME = CompoundKey.of("InhabitedTime");
        public static final CompoundKey SECTIONS = CompoundKey.of("Sections");
        public static final CompoundKey TILE_ENTITIES = CompoundKey.of("TileEntities");
        public static final CompoundKey X_POS = CompoundKey.of("xPos");
        public static final CompoundKey Z_POS = CompoundKey.of("zPos");
    }

    /**
     * Entity format
     */
    public static class EntityTag {
        public static final CompoundKey ITEM = CompoundKey.of("Item");
        public static final CompoundKey ITEMS = CompoundKey.of("Items");
    }

    /**
     * Level.dat root tag format
     */
    public static class LevelRootTag {
        public static final CompoundKey FML = CompoundKey.of("FML");
        public static final CompoundKey MINECRAFT = CompoundKey.of("Data");
    }

    /**
     * Level.dat data tag format.
     */
    public static class LevelTag {
        public static final CompoundKey PLAYER = CompoundKey.of("Player");
    }

    /**
     * Player data format
     */
    public static class PlayerTag {
        public static final CompoundKey ENDER_INVENTORY = CompoundKey.of("EnderItems");
        public static final CompoundKey INVENTORY = CompoundKey.of("Inventory");
    }

    /**
     * Chunk section format
     */
    public static class SectionTag {
        public static final CompoundKey BLOCK_DATA = CompoundKey.of("Data");
        public static final CompoundKey BLOCK_IDS = CompoundKey.of("Blocks");
        public static final CompoundKey EXT_BLOCK_IDS = CompoundKey.of("Add");
        public static final CompoundKey Y_POS = CompoundKey.of("Y");
    }

    /**
     * Tile entity format
     */
    public static class TileEntityTag {
        public static final CompoundKey FLOWER_POT_BLOCK_DATA = CompoundKey.of("Data");
        public static final CompoundKey FLOWER_POT_BLOCK_NAME = CompoundKey.of("Item");
        public static final CompoundKey ID = CompoundKey.of("id");
        public static final CompoundKey ITEMS = CompoundKey.of("Items");
        public static final CompoundKey PISTON_BLOCK_DATA = CompoundKey.of("blockData");
        public static final CompoundKey PISTON_BLOCK_ID = CompoundKey.of("blockId");
        public static final ImmutableList<CompoundKey> SIGN_LINE_NAMES = ImmutableList.of(
                CompoundKey.of("Text1"), CompoundKey.of("Text2"), CompoundKey.of("Text3"), CompoundKey.of("Text4"));
        public static final CompoundKey X_POS = CompoundKey.of("x");
        public static final CompoundKey Y_POS = CompoundKey.of("y");
        public static final CompoundKey Z_POS = CompoundKey.of("z");
    }
    // Level.dat Forge Mod Loader tag format
    public static final CompoundKey LFML_ITEM_DATA_TAG = CompoundKey.of("ItemData");
}
