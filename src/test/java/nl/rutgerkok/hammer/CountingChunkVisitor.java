package nl.rutgerkok.hammer;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import nl.rutgerkok.hammer.util.Progress;
import nl.rutgerkok.hammer.util.Result;
import nl.rutgerkok.hammer.util.Visitor;

/**
 * Counts all the chunks, entities and tile entities seen.
 *
 */
public final class CountingChunkVisitor implements Visitor<Chunk> {

    public final AtomicInteger chunksSeen = new AtomicInteger(0);
    public final AtomicInteger entitiesSeen = new AtomicInteger(0);
    public final AtomicInteger tileEntitiesSeen = new AtomicInteger(0);

    @Override
    public Result accept(Chunk chunk, Progress progress) {
        chunksSeen.incrementAndGet();
        try {
            entitiesSeen.addAndGet(chunk.getEntities().size());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        tileEntitiesSeen.addAndGet(chunk.getTileEntities().size());

        return Result.NO_CHANGES;
    }
}