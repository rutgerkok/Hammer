package nl.rutgerkok.hammer.pocket;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.Before;
import org.junit.Test;

import nl.rutgerkok.hammer.CountingChunkVisitor;
import nl.rutgerkok.hammer.material.GlobalMaterialMap;
import nl.rutgerkok.hammer.util.TestFile;

public class ChunkWalkTest {

    private PocketWorld world;

    @Before
    public void loadWorld() throws IOException {
        Path levelDat = TestFile.get("pocket_0_10_4/level.dat");
        world = new PocketWorld(new GlobalMaterialMap(), levelDat);
    }

    @Test
    public void testStatistics() throws IOException {
        CountingChunkVisitor chunkVisitor = new CountingChunkVisitor();
        world.walkChunks(chunkVisitor);
        assertEquals(124, chunkVisitor.chunksSeen.get());
        assertEquals(22, chunkVisitor.entitiesSeen.get());
        assertEquals(3, chunkVisitor.tileEntitiesSeen.get());
    }
}
