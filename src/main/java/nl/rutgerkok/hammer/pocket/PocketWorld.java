package nl.rutgerkok.hammer.pocket;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import nl.rutgerkok.hammer.Chunk;
import nl.rutgerkok.hammer.GameFactory;
import nl.rutgerkok.hammer.PlayerFile;
import nl.rutgerkok.hammer.World;
import nl.rutgerkok.hammer.anvil.material.VanillaMaterialMap;
import nl.rutgerkok.hammer.pocket.tag.PocketNbtReader;
import nl.rutgerkok.hammer.pocket.tag.PocketNbtWriter;
import nl.rutgerkok.hammer.tag.CompoundTag;
import nl.rutgerkok.hammer.util.Visitor;

public class PocketWorld implements World {

    private static final String LEVEL_DB_FOLDER = "db";
    private final GameFactory gameFactory;
    private final Path levelDat;
    private final PocketLevelDb levelDb;
    private final CompoundTag rootLevelTag;

    /**
     * Creates a new world editor for the given world in Pocket Edition format.
     * 
     * @param levelDat
     *            The level.dat file of the Pocket Edition world.
     * @throws IOException
     *             If an IO error occurs reading the level.dat file.
     */
    public PocketWorld(Path levelDat) throws IOException {
        this.levelDat = Objects.requireNonNull(levelDat, "level.dat");
        this.rootLevelTag = PocketNbtReader.readFromUncompressedFile(levelDat);

        // Use the same material map as Anvil for now
        this.gameFactory = new PocketGameFactory(new VanillaMaterialMap());

        Path levelDbFolder = levelDat.resolveSibling(LEVEL_DB_FOLDER);
        this.levelDb = new PocketLevelDb(levelDbFolder);
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

    @Override
    public void saveLevelTag() throws IOException {
        PocketNbtWriter.writeUncompressedToFile(levelDat, rootLevelTag);
    }

    @Override
    public void walkChunks(Visitor<Chunk> visitor) throws IOException {
        new ChunkWalk(this.gameFactory, this.levelDb).forEach(visitor);
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
        new ChunkWalk(gameFactory, levelDb).forEach(visitor);
    }
}
