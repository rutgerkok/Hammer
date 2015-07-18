package nl.rutgerkok.hammer.anvil.tag;

import nl.rutgerkok.hammer.tag.CompoundKey;
import nl.rutgerkok.hammer.tag.CompoundTag;
import nl.rutgerkok.hammer.tag.ListTag;

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
        public static final CompoundKey<CompoundTag> MINECRAFT = CompoundKey.of("Level");
    }

    /**
     * Format of the base chunk tag.
     */
    public static class ChunkTag {
        public static final CompoundKey<byte[]> BIOMES = CompoundKey.of("Biomes");
        public static final CompoundKey<ListTag<CompoundTag>> ENTITIES = CompoundKey.of("Entities");
        public static final CompoundKey<Long> INHABITED_TIME = CompoundKey.of("InhabitedTime");
        public static final CompoundKey<Boolean> LIGHT_POPULATED = CompoundKey.of("LightPopulated");
        public static final CompoundKey<ListTag<CompoundTag>> SECTIONS = CompoundKey.of("Sections");
        public static final CompoundKey<Boolean> TERRAIN_POPULATED = CompoundKey.of("TerrainPopulated");
        public static final CompoundKey<ListTag<CompoundTag>> TILE_ENTITIES = CompoundKey.of("TileEntities");
        public static final CompoundKey<Integer> X_POS = CompoundKey.of("xPos");
        public static final CompoundKey<Integer> Z_POS = CompoundKey.of("zPos");
    }

    /**
     * Entity format
     */
    public static class EntityTag {
        public static final CompoundKey<CompoundTag> ITEM = CompoundKey.of("Item");
        public static final CompoundKey<ListTag<CompoundTag>> ITEMS = CompoundKey.of("Items");
    }

    /**
     * Level.dat root tag format
     */
    public static class LevelRootTag {
        public static final CompoundKey<CompoundTag> FML = CompoundKey.of("FML");
        public static final CompoundKey<CompoundTag> MINECRAFT = CompoundKey.of("Data");
    }

    /**
     * Level.dat data tag format.
     */
    public static class LevelTag {
        public static final CompoundKey<String> LEVEL_NAME = CompoundKey.of("LevelName");
        public static final CompoundKey<CompoundTag> PLAYER = CompoundKey.of("Player");
    }

    /**
     * Player data format
     */
    public static class PlayerTag {
        public static final CompoundKey<ListTag<CompoundTag>> ENDER_INVENTORY = CompoundKey.of("EnderItems");
        public static final CompoundKey<ListTag<CompoundTag>> INVENTORY = CompoundKey.of("Inventory");
    }

    /**
     * Chunk section format
     */
    public static class SectionTag {
        public static final CompoundKey<byte[]> BLOCK_DATA = CompoundKey.of("Data");
        public static final CompoundKey<byte[]> BLOCK_IDS = CompoundKey.of("Blocks");
        public static final CompoundKey<byte[]> BLOCK_LIGHT = CompoundKey.of("BlockLight");
        public static final CompoundKey<byte[]> EXT_BLOCK_IDS = CompoundKey.of("Add");
        public static final CompoundKey<Byte> INDEX = CompoundKey.of("Y");
        public static final CompoundKey<byte[]> SKY_LIGHT = CompoundKey.of("SkyLight");
    }

    /**
     * Tile entity format
     */
    public static class TileEntityTag {
        public static final CompoundKey<Integer> FLOWER_POT_BLOCK_DATA = CompoundKey.of("Data");
        public static final CompoundKey<String> FLOWER_POT_BLOCK_NAME = CompoundKey.of("Item");
        public static final CompoundKey<String> ID = CompoundKey.of("id");
        public static final CompoundKey<ListTag<CompoundTag>> ITEMS = CompoundKey.of("Items");
        public static final CompoundKey<Integer> PISTON_BLOCK_DATA = CompoundKey.of("blockData");
        public static final CompoundKey<Integer> PISTON_BLOCK_ID = CompoundKey.of("blockId");
        public static final ImmutableList<CompoundKey<String>> SIGN_LINE_NAMES = ImmutableList.of(
                CompoundKey.<String> of("Text1"),
                CompoundKey.<String> of("Text2"),
                CompoundKey.<String> of("Text3"),
                CompoundKey.<String> of("Text4"));
        public static final CompoundKey<Integer> X_POS = CompoundKey.of("x");
        public static final CompoundKey<Integer> Y_POS = CompoundKey.of("y");
        public static final CompoundKey<Integer> Z_POS = CompoundKey.of("z");
    }
    // Level.dat Forge Mod Loader tag format
    public static final CompoundKey<ListTag<CompoundTag>> LFML_ITEM_DATA_TAG = CompoundKey.of("ItemData");
}
