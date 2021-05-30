package nl.rutgerkok.hammer.anvil;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import nl.rutgerkok.hammer.Chunk;
import nl.rutgerkok.hammer.ChunkAccess;
import nl.rutgerkok.hammer.GameFactory;
import nl.rutgerkok.hammer.PlayerFile;
import nl.rutgerkok.hammer.World;
import nl.rutgerkok.hammer.anvil.tag.AnvilFormat.LevelRootTag;
import nl.rutgerkok.hammer.anvil.tag.AnvilNbtReader;
import nl.rutgerkok.hammer.anvil.tag.AnvilNbtWriter;
import nl.rutgerkok.hammer.material.BlockDataMaterialMap;
import nl.rutgerkok.hammer.material.BlockStatesMaterialMap;
import nl.rutgerkok.hammer.material.GlobalMaterialMap;
import nl.rutgerkok.hammer.tag.CompoundTag;
import nl.rutgerkok.hammer.util.Visitor;

/**
 * Represents a Minecraft world.
 *
 */
public class AnvilWorld implements World {

    public static final String LEVEL_DAT_NAME = "level.dat";

    private static final String PLAYER_DIRECTORY = "playerdata";
    private static final String PLAYER_DIRECTORY_OLD = "players";
    private static final String REGION_FOLDER_NAME = "region";

    private final AnvilGameFactory gameFactory;
    private final Path levelDat;
    private final RegionFileCache regionFileCache;
    private final CompoundTag tag;

    /**
     * Creates a new world in the Anvil world format.
     *
     * @param dictionary
     *            Material dictionary.
     * @param levelDat
     *            Path to the level.dat file.
     * @throws IOException
     *             Thrown if reading the level.dat file fails.
     */
    public AnvilWorld(GlobalMaterialMap dictionary, Path levelDat) throws IOException {
        if (!levelDat.getFileName().toString().equals(LEVEL_DAT_NAME)) {
            throw new IOException("Expected a " + LEVEL_DAT_NAME + " file, got \""
                    + levelDat.getName(levelDat.getNameCount() - 1) + "\"");
        }
        this.levelDat = levelDat.toAbsolutePath();
        this.tag = Files.exists(levelDat) ? AnvilNbtReader.readFromCompressedFile(levelDat) : new CompoundTag();
        this.gameFactory = new AnvilGameFactory(initMaterialMap(dictionary));
        this.regionFileCache = new RegionFileCache(getRegionDirectory());
    }

    @Override
    public ChunkAccess<AnvilChunk> getChunkAccess() {
        return new AnvilChunkAccess(gameFactory, regionFileCache);
    }

    /**
     * Gets the directory next to the level.dat with the given name.
     *
     * @param name
     *            Name of the directory.
     * @return The directory, or null if not found.
     */
    private Path getDirectory(String name) {
        Path file = levelDat.resolveSibling(name);
        if (Files.isDirectory(file)) {
            return file;
        }
        return null;
    }

    /**
     * Gets the material map of this world.
     *
     * @return The material map.
     */
    @Override
    public GameFactory getGameFactory() {
        return gameFactory;
    }

    /**
     * Gets access to the main tag of the level.dat file, with subtags like
     * SpawnX and GameRules.
     *
     * @return The NBT root tag.
     */
    @Override
    public CompoundTag getLevelTag() {
        return tag.getCompound(LevelRootTag.MINECRAFT);
    }

    /**
     * Gets the player directory. May be null if no player directory exists.
     *
     * @return The player directory.
     */
    public Path getPlayerDirectory() {
        // Try modern file
        Path dir = getDirectory(PLAYER_DIRECTORY);
        if (dir != null) {
            return dir;
        }

        // Try again with old file
        return getDirectory(PLAYER_DIRECTORY_OLD);
    }

    private Path getRegionDirectory() {
        return levelDat.resolveSibling(REGION_FOLDER_NAME);
    }

    /**
     * Scans the level.dat for a Forge name->id map, if found it used that,
     * otherwise it uses the vanilla ids.
     *
     * @param dictionary
     *            Global material dictionary.
     * @return The id map.
     */
    private AnvilMaterialMap initMaterialMap(GlobalMaterialMap dictionary) {
        URL vanillaBlocks = getClass().getResource("/blocks_pc.json");
        URL oldBlocks = getClass().getResource("/blocks_pc_1_12.json");

        BlockStatesMaterialMap modern = new BlockStatesMaterialMap(dictionary, vanillaBlocks);
        BlockDataMaterialMap old = new BlockDataMaterialMap(dictionary, oldBlocks);

        return new AnvilMaterialMap(old, modern);
    }

    /**
     * Saves the tag if needed.
     *
     * @throws IOException
     *             If saving fails.
     */
    @Override
    public void saveLevelTag() throws IOException {
        AnvilNbtWriter.writeCompressedToFile(levelDat, tag);
    }

    /**
     * Same as {@link #walkChunks(Visitor)}, but may save you from casting the
     * chunks.
     *
     * @param visitor
     *            The visitor.
     * @throws IOException
     *             If an IO error occurs.
     */
    public void walkAnvilChunks(Visitor<AnvilChunk> visitor) throws IOException {
        new ChunkWalk(gameFactory, regionFileCache).performWalk(visitor);
    }

    @Override
    public void walkChunks(Visitor<Chunk> visitor) throws IOException {
        new ChunkWalk(gameFactory, regionFileCache).performWalk(visitor);
    }

    @Override
    public void walkPlayerFiles(Visitor<PlayerFile> visitor) throws IOException {
        new PlayerFilesWalk(this).forEach(visitor);
    }

}
