package nl.rutgerkok.hammer;

import java.io.IOException;

import nl.rutgerkok.hammer.tag.CompoundTag;
import nl.rutgerkok.hammer.util.Progress;
import nl.rutgerkok.hammer.util.Visitor;

public interface World {

    /**
     * Gets the game factory of this world.
     *
     * @return The game factory.
     */
    GameFactory getGameFactory();

    /**
     * Gets access to the main tag of the level.dat file, with subtags like
     * SpawnX and GameRules.
     *
     * @return The NBT root tag.
     */
    CompoundTag getLevelTag();

    /**
     * Saves the level.dat tag.
     *
     * @throws IOException
     *             If saving fails.
     */
    void saveLevelTag() throws IOException;

    /**
     * Gets a walk along all chunks in the world.
     *
     * @param visitor
     *            The method {@link Visitor#accept(Object, Progress)} is called
     *            for every chunk in the world.
     * @throws IOException
     *             If an IO error occurs.
     */
    void walkChunks(Visitor<Chunk> visitor) throws IOException;

    /**
     * Gets a walk along all player files in the world.
     *
     * @param visitor
     *            The method {@link Visitor#accept(Object, Progress)} is called
     *            for every player data tag in the world.
     * @throws IOException
     *             If an IO error occurs.
     */
    void walkPlayerFiles(Visitor<PlayerFile> visitor) throws IOException;

}