package nl.rutgerkok.hammer;

import java.io.Closeable;
import java.io.IOException;

import nl.rutgerkok.hammer.util.Visitor;

/**
 * Used for access to random chunks in the world.
 *
 * @param <T>
 *            The type of chunks that are stored.
 * @see World#walkChunks(Visitor) Sequential access to all chunks
 */
public interface ChunkAccess<T extends Chunk> extends Closeable {

    /**
     * Gets the chunk at the given chunk coordinates. If the chunk does not
     * exist, a chunk consisting of only air blocks is returned.
     *
     * @param chunkX
     *            The chunk x.
     * @param chunkZ
     *            The chunk z.
     * @return The chunk.
     * @throws IOException
     *             If an IO error occurs.
     * @throws IllegalStateException
     *             When called after {@link #close()}.
     */
    T getChunk(int chunkX, int chunkZ) throws IOException;

    /**
     * Saves a chunk. The saving action happens on the current thread.
     *
     * @param chunk
     *            The chunk to save.
     * @throws IOException
     *             If an IO error occurs during saving.
     */
    void saveChunk(T chunk) throws IOException;

    /**
     * Closes the chunk access for this instance, so that, if there are no other
     * open chunk accessors, the region files/level database can be closed.
     */
    @Override
    void close();
}
