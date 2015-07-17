package nl.rutgerkok.hammer.anvil;

import static nl.rutgerkok.hammer.anvil.tag.AnvilFormat.LFML_ITEM_DATA_TAG;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import nl.rutgerkok.hammer.Chunk;
import nl.rutgerkok.hammer.ChunkAccess;
import nl.rutgerkok.hammer.GameFactory;
import nl.rutgerkok.hammer.PlayerFile;
import nl.rutgerkok.hammer.World;
import nl.rutgerkok.hammer.anvil.material.ForgeMaterialMap;
import nl.rutgerkok.hammer.anvil.material.VanillaMaterialMap;
import nl.rutgerkok.hammer.anvil.tag.AnvilFormat.LevelRootTag;
import nl.rutgerkok.hammer.anvil.tag.AnvilNbtReader;
import nl.rutgerkok.hammer.anvil.tag.AnvilNbtWriter;
import nl.rutgerkok.hammer.material.MaterialMap;
import nl.rutgerkok.hammer.tag.CompoundTag;
import nl.rutgerkok.hammer.tag.TagType;
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

    private final GameFactory gameFactory;
    private final Path levelDat;
    private final CompoundTag tag;
    private final RegionFileCache regionFileCache;

    public AnvilWorld(Path levelDat) throws IOException {
        if (!levelDat.getFileName().toString().equals(LEVEL_DAT_NAME)) {
            throw new IOException("Expected a " + LEVEL_DAT_NAME + " file, got \"" + levelDat.getName(levelDat.getNameCount() - 1) + "\"");
        }
        this.levelDat = levelDat.toAbsolutePath();
        this.tag = AnvilNbtReader.readFromCompressedFile(levelDat);
        this.gameFactory = new AnvilGameFactory(initMaterialMap());
        this.regionFileCache = new RegionFileCache(getRegionDirectory());
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

    /**
     * Scans the level.dat for a Forge name->id map, if found it used that,
     * otherwise it uses the vanilla ids.
     *
     * @return The id map.
     */
    private MaterialMap initMaterialMap() {
        if (tag.containsKey(LevelRootTag.FML)) {
            CompoundTag fmlTag = tag.getCompound(LevelRootTag.FML);
            if (fmlTag.containsKey(LFML_ITEM_DATA_TAG)) {
                return new ForgeMaterialMap(fmlTag.getList(LFML_ITEM_DATA_TAG, TagType.COMPOUND));
            }
        }
        return new VanillaMaterialMap();
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

    private Path getRegionDirectory() {
        return levelDat.resolveSibling(REGION_FOLDER_NAME);
    }

    @Override
    public ChunkAccess<AnvilChunk> getChunkAccess() {
        return new AnvilChunkAccess(gameFactory, regionFileCache);
    }

}
