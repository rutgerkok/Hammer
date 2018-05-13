package nl.rutgerkok.hammer.anvil.tag;

import com.google.common.collect.ImmutableList;

import nl.rutgerkok.hammer.tag.CompoundKey;
import nl.rutgerkok.hammer.tag.CompoundTag;
import nl.rutgerkok.hammer.tag.ListTag;

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
        public static final CompoundKey<Integer> DATA_VERSION = CompoundKey.of("DataVersion");
    }

    /**
     * Format of the base chunk tag.
     */
    public static class ChunkTag {
        public static final CompoundKey<byte[]> BIOMES = CompoundKey.of("Biomes");
        public static final CompoundKey<ListTag<CompoundTag>> ENTITIES = CompoundKey.of("Entities");
        public static final CompoundKey<Long> INHABITED_TIME = CompoundKey.of("InhabitedTime");
        public static final CompoundKey<ListTag<CompoundTag>> SECTIONS = CompoundKey.of("Sections");
        public static final CompoundKey<ListTag<CompoundTag>> TILE_ENTITIES = CompoundKey.of("TileEntities");
        public static final CompoundKey<Integer> X_POS = CompoundKey.of("xPos");
        public static final CompoundKey<Integer> Z_POS = CompoundKey.of("zPos");
        public static final CompoundKey<CompoundTag> HEIGHT_MAPS = CompoundKey.of("Heightmaps");
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
        public static final CompoundKey<Integer> VERSION = CompoundKey.of("version");
        public static final CompoundKey<Long> LAST_PLAYED = CompoundKey.of("LastPlayed");
    }

    /**
     * The material tag in the palette of chunk sections.
     *
     */
    public static class MaterialTag {
        public static final CompoundKey<String> NAME = CompoundKey.of("Name");
        public static final CompoundKey<CompoundTag> PROPERTIES = CompoundKey.of("Properties");
    }

    /**
     * Tags that were in the base chunk tag, but have since been removed.
     *
     */
    public static class OldChunkTag {
        public static final CompoundKey<Boolean> LIGHT_POPULATED = CompoundKey.of("LightPopulated");
        public static final CompoundKey<Boolean> TERRAIN_POPULATED = CompoundKey.of("TerrainPopulated");
    }

    /**
     * Old chunk section format (Minecraft 1.2 - 1.12), no longer in use.
     */
    public static class OldSectionTag {
        public static final CompoundKey<byte[]> BLOCK_DATA = CompoundKey.of("Data");
        public static final CompoundKey<byte[]> BLOCK_IDS = CompoundKey.of("Blocks");
        public static final CompoundKey<byte[]> EXT_BLOCK_IDS = CompoundKey.of("Add");
    }

    /**
     * Player data format
     */
    public static class PlayerTag {
        public static final CompoundKey<ListTag<CompoundTag>> ENDER_INVENTORY = CompoundKey.of("EnderItems");
        public static final CompoundKey<ListTag<CompoundTag>> INVENTORY = CompoundKey.of("Inventory");
    }

    /**
     * Chunk section format. Tags that are no longer in use as of Minecraft 1.13
     * have been moved to {@link OldSectionTag}.
     */
    public static class SectionTag {
        public static final CompoundKey<long[]> BLOCK_STATES = CompoundKey.of("BlockStates");
        public static final CompoundKey<byte[]> BLOCK_LIGHT = CompoundKey.of("BlockLight");
        public static final CompoundKey<Byte> INDEX = CompoundKey.of("Y");
        public static final CompoundKey<byte[]> SKY_LIGHT = CompoundKey.of("SkyLight");
        public static final CompoundKey<ListTag<CompoundTag>> PALETTE = CompoundKey.of("Palette");
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
