package nl.rutgerkok.hammer;

import java.io.IOException;

import nl.rutgerkok.hammer.material.MaterialMap;
import nl.rutgerkok.hammer.tag.CompoundTag;
import nl.rutgerkok.hammer.util.Progress;
import nl.rutgerkok.hammer.util.Visitor;

public interface World {

    /**
     * Gets access to the main tag of the level.dat file, with subtags like
     * SpawnX and GameRules.
     *
     * @return The NBT root tag.
     */
    CompoundTag getLevelTag();

    /**
     * Gets the material map of this world.
     *
     * @return The material map.
     */
    MaterialMap getMaterialMap();

    /**
     * Saves the level.dat tag.
     *
     * @throws IOException
     *             If saving fails.
     */
    void saveLevelTag() throws IOException;

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