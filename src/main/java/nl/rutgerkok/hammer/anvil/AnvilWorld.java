package nl.rutgerkok.hammer.anvil;

import static nl.rutgerkok.hammer.anvil.tag.AnvilTagFormat.LFML_ITEM_DATA_TAG;
import static nl.rutgerkok.hammer.anvil.tag.AnvilTagFormat.LR_FML_TAG;
import static nl.rutgerkok.hammer.anvil.tag.AnvilTagFormat.LR_MINECRAFT_TAG;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import nl.rutgerkok.hammer.Chunk;
import nl.rutgerkok.hammer.PlayerFile;
import nl.rutgerkok.hammer.World;
import nl.rutgerkok.hammer.anvil.material.ForgeMaterialMap;
import nl.rutgerkok.hammer.anvil.material.VanillaMaterialMap;
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

    private final Path levelDat;
    private final MaterialMap materialMap;
    private final CompoundTag tag;

    public AnvilWorld(Path levelDat) throws IOException {
        if (!levelDat.getFileName().toString().equals(LEVEL_DAT_NAME)) {
            throw new IOException("Expected a " + LEVEL_DAT_NAME + " file, got \"" + levelDat.getName(levelDat.getNameCount() - 1) + "\"");
        }
        this.levelDat = levelDat.toAbsolutePath();
        this.tag = AnvilNbtReader.readFromCompressedFile(levelDat);
        this.materialMap = initMaterialMap();
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
     * Gets access to the main tag of the level.dat file, with subtags like
     * SpawnX and GameRules.
     *
     * @return The NBT root tag.
     */
    @Override
    public CompoundTag getLevelTag() {
        return tag.getCompound(LR_MINECRAFT_TAG);
    }

    /**
     * Gets the material map of this world.
     *
     * @return The material map.
     */
    @Override
    public MaterialMap getMaterialMap() {
        return materialMap;
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
        if (tag.containsKey(LR_FML_TAG)) {
            CompoundTag fmlTag = tag.getCompound(LR_FML_TAG);
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

    @Override
    public void walkChunks(Visitor<Chunk> visitor) throws IOException {
        new ChunkWalk(materialMap, levelDat.resolveSibling(REGION_FOLDER_NAME)).startWalk(visitor);
    }

    @Override
    public void walkPlayerFiles(Visitor<PlayerFile> visitor) throws IOException {
        new AnvilPlayerFilesWalk(this).forEach(visitor);
    }

}
