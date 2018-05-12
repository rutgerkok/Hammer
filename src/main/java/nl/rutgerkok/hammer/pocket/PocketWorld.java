package nl.rutgerkok.hammer.pocket;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;

import nl.rutgerkok.hammer.Chunk;
import nl.rutgerkok.hammer.ChunkAccess;
import nl.rutgerkok.hammer.GameFactory;
import nl.rutgerkok.hammer.PlayerFile;
import nl.rutgerkok.hammer.World;
import nl.rutgerkok.hammer.material.BlockDataMaterialMap;
import nl.rutgerkok.hammer.material.GlobalMaterialMap;
import nl.rutgerkok.hammer.pocket.tag.PocketNbtReader;
import nl.rutgerkok.hammer.pocket.tag.PocketNbtWriter;
import nl.rutgerkok.hammer.tag.CompoundTag;
import nl.rutgerkok.hammer.util.Visitor;

public class PocketWorld implements World {

    public static final String LEVEL_DAT_NAME = "level.dat";
    private static final String LEVEL_DB_FOLDER = "db";
    private final PocketGameFactory gameFactory;
    private final Path levelDat;
    private final PocketLevelDb levelDb;
    private final CompoundTag rootLevelTag;

    /**
     * Creates a new world editor for the given world in Pocket Edition format.
     * @param dictionary
     *            The global material dictionary, for transferring material data
     *            objects between worlds.
     * @param levelDat
     *            The level.dat file of the Pocket Edition world.
     *
     * @throws IOException
     *             If an IO error occurs reading the level.dat file.
     */
    public PocketWorld(GlobalMaterialMap dictionary, Path levelDat) throws IOException {
        this.levelDat = Objects.requireNonNull(levelDat, "levelDat");

        if (!levelDat.getFileName().toString().equals(LEVEL_DAT_NAME)) {
            throw new IOException(levelDat + " is not a valid level.dat file");
        }
        this.rootLevelTag = PocketNbtReader.readFromUncompressedFile(levelDat);

        this.gameFactory = initGameFactory(dictionary);

        Path levelDbFolder = levelDat.resolveSibling(LEVEL_DB_FOLDER);
        this.levelDb = new PocketLevelDb(levelDbFolder);
    }

    @Override
    public ChunkAccess<PocketChunk> getChunkAccess() throws IOException {
        return new PocketChunkAccess(gameFactory, levelDb);
    }

    @Override
    public GameFactory getGameFactory() {
        return gameFactory;
    }

    @Override
    public CompoundTag getLevelTag() {
        // In the pocket edition, the root tag is the tag with all the
        // information
        return rootLevelTag;
    }

    private PocketGameFactory initGameFactory(GlobalMaterialMap dictionary) {
        URL vanillaMaterials = getClass().getResource("/blocks_pe.json");
        BlockDataMaterialMap materialMap = new BlockDataMaterialMap(dictionary, vanillaMaterials);
        return new PocketGameFactory(materialMap);
    }

    @Override
    public void saveLevelTag() throws IOException {
        PocketNbtWriter.writeUncompressedToFile(levelDat, rootLevelTag);
    }

    @Override
    public void walkChunks(Visitor<Chunk> visitor) throws IOException {
        try (ChunkWalk chunkWalk = new ChunkWalk(new PocketChunkAccess(gameFactory, levelDb))) {
            chunkWalk.forEach(visitor);
        }
    }

    @Override
    public void walkPlayerFiles(Visitor<PlayerFile> visitor) throws IOException {
        // TODO Auto-generated method stub

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
    public void walkPocketChunks(Visitor<PocketChunk> visitor) throws IOException {
        try (ChunkWalk chunkWalk = new ChunkWalk(new PocketChunkAccess(gameFactory, levelDb))) {
            chunkWalk.forEach(visitor);
        }
    }
}
