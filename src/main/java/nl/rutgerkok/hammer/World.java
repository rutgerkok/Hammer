package nl.rutgerkok.hammer;

import java.io.IOException;

import nl.rutgerkok.hammer.anvil.AnvilWorld;
import nl.rutgerkok.hammer.pocket.PocketWorld;
import nl.rutgerkok.hammer.tag.CompoundTag;
import nl.rutgerkok.hammer.util.Progress;
import nl.rutgerkok.hammer.util.Visitor;

/**
 * Represents a Minecraft world.
 *
 * <p>
 * Implementations like {@link AnvilWorld} and {@link PocketWorld} are provided
 * for the various level formats of Minecraft. This interface exposes all
 * methods common to all world formats.
 *
 * <p>
 * For each world on disk, there may only be one {@link World} instance live.
 * This is very important, the world may be corrupted if multiple {@link World}
 * instances exist for the same world. The same goes for other programs (like
 * Minecraft itself) that may modify the world: only one program may modify a
 * world at the same time.
 */
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
     * @see #getChunkAccess() Random access to chunks
     */
    void walkChunks(Visitor<Chunk> visitor) throws IOException;

    /**
     * Gets random (as opposed to {@link #walkChunks(Visitor) sequential})
     * access to the chunks in the world.
     * 
     * <p>
     * The returned instance must be {@link ChunkAccess#close() closed} to free
     * up any associated resources.
     * 
     * <p>
     * The chunk access must be implemented in such a way that multiple chunk
     * accessors (either created through this method or through
     * {@link #walkChunks(Visitor)}) can safely be open at the same time. One
     * exception is the case where multiple {@link World} instances exist for
     * the same world. See the note at the top of the {@link World} class for
     * more information.
     * 
     * @return Access to the chunks.
     */
    ChunkAccess<?> getChunkAccess();

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