package nl.rutgerkok.hammer.pocket;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import nl.rutgerkok.hammer.util.Progress;
import nl.rutgerkok.hammer.util.Result;
import nl.rutgerkok.hammer.util.TestFile;
import nl.rutgerkok.hammer.util.Visitor;

import org.junit.Before;
import org.junit.Test;

public class ChunkWalkTest {

    private static class ChunkVisitor implements Visitor<PocketChunk> {

        private AtomicInteger chunksSeen = new AtomicInteger(0);
        private AtomicInteger entitiesSeen = new AtomicInteger(0);
        private AtomicInteger tileEntitiesSeen = new AtomicInteger(0);

        @Override
        public Result accept(PocketChunk chunk, Progress progress) {
            chunksSeen.incrementAndGet();
            entitiesSeen.addAndGet(chunk.getEntities().size());
            tileEntitiesSeen.addAndGet(chunk.getTileEntities().size());
            return Result.NO_CHANGES;
        }
    }

    private PocketWorld world;

    @Before
    public void loadWorld() throws IOException {
        Path levelDat = TestFile.get("pocket_0_10_4/level.dat");
        world = new PocketWorld(levelDat);
    }

    @Test
    public void testStatistics() throws IOException {
        ChunkVisitor chunkVisitor = new ChunkVisitor();
        world.walkPocketChunks(chunkVisitor);
        assertEquals(124, chunkVisitor.chunksSeen.get());
        assertEquals(22, chunkVisitor.entitiesSeen.get());
        assertEquals(3, chunkVisitor.tileEntitiesSeen.get());
    }
}
