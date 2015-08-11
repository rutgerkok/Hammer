package nl.rutgerkok.hammer.pocket;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;

import nl.rutgerkok.hammer.util.Progress;
import nl.rutgerkok.hammer.util.Progress.UnitsProgress;
import nl.rutgerkok.hammer.util.Result;
import nl.rutgerkok.hammer.util.Visitor;

/**
 * Provides sequential access to all chunks in a world.
 *
 */
final class ChunkWalk implements Closeable {

    private final PocketChunkAccess chunkAccess;

    ChunkWalk(PocketChunkAccess chunkAccess) {
        this.chunkAccess = Objects.requireNonNull(chunkAccess, "chunkAccess");
    }

    @Override
    public void close() throws IOException {
        chunkAccess.close();
    }

    void forEach(Visitor<? super PocketChunk> visitor) throws IOException {
        UnitsProgress progress = Progress.ofUnits(chunkAccess.getChunkCount());
        for (PocketChunk chunk : chunkAccess) {
            Result result = visitor.accept(chunk, progress);
            switch (result) {
                case CHANGED:
                    chunkAccess.saveChunk(chunk);
                    break;
                case DELETE:
                    chunkAccess.deleteChunk(chunk);
                    break;
                case NO_CHANGES:
                    break;
                default:
                    throw new AssertionError("Unknown result " + result);
            }
            progress.increment();
        }

    }

}
