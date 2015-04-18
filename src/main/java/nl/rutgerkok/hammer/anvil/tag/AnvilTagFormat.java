package nl.rutgerkok.hammer.anvil.tag;

import nl.rutgerkok.hammer.tag.CompoundKey;

/**
 * Names of the various NBT tags of Minecraft's chunk format.
 *
 */
public final class AnvilTagFormat {

    // Chunk format
    public static final CompoundKey CHUNK_BIOMES_TAG = CompoundKey.of("Biomes");
    public static final CompoundKey CHUNK_ENTITIES_TAG = CompoundKey.of("Entities");
    public static final CompoundKey CHUNK_INHABITED_TIME_TAG = CompoundKey.of("InhabitedTime");
    public static final CompoundKey CHUNK_SECTIONS_TAG = CompoundKey.of("Sections");
    public static final CompoundKey CHUNK_TILE_ENTITIES_TAG = CompoundKey.of("TileEntities");
    public static final CompoundKey CHUNK_X_POS_TAG = CompoundKey.of("xPos");
    public static final CompoundKey CHUNK_Z_POS_TAG = CompoundKey.of("zPos");

    // Chunk root tag format
    public static final CompoundKey CR_MINECRAFT_TAG = CompoundKey.of("Level");

    // Level.dat Data tag format
    public static final CompoundKey LEVEL_PLAYER_TAG = CompoundKey.of("Player");

    // Level.dat Forge Mod Loader tag format
    public static final CompoundKey LFML_ITEM_DATA_TAG = CompoundKey.of("ItemData");

    // Level.dat root tag format
    public static final CompoundKey LR_FML_TAG = CompoundKey.of("FML");
    public static final CompoundKey LR_MINECRAFT_TAG = CompoundKey.of("Data");

    // Player data format
    public static final CompoundKey PLAYER_INVENTORY = CompoundKey.of("Inventory");

    // Chunk section format
    public static final CompoundKey SECTION_BLOCK_DATA_TAG = CompoundKey.of("Data");
    public static final CompoundKey SECTION_BLOCK_IDS_TAG = CompoundKey.of("Blocks");
    public static final CompoundKey SECTION_EXT_BLOCK_IDS_TAG = CompoundKey.of("Add");
    public static final CompoundKey SECTION_Y_TAG = CompoundKey.of("Y");

    // Tile entity format
    public static final CompoundKey TILE_ENTITY_ID_TAG = CompoundKey.of("id");
}
