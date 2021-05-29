package nl.rutgerkok.hammer.anvil;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.Before;
import org.junit.Test;

import nl.rutgerkok.hammer.CountingChunkVisitor;
import nl.rutgerkok.hammer.material.GlobalMaterialMap;
import nl.rutgerkok.hammer.util.TestFile;

public class ChunkWalkTest {

    private AnvilWorld world;

    @Before
    public void loadWorld() throws IOException {
        Path levelDat = TestFile.get("anvil_1_13/level.dat");
        world = new AnvilWorld(new GlobalMaterialMap(), levelDat);
    }

    @Test
    public void testStatistics() throws IOException {
        CountingChunkVisitor chunkVisitor = new CountingChunkVisitor();
        world.walkChunks(chunkVisitor);
        assertEquals(5014, chunkVisitor.chunksSeen.get());
        assertEquals(427, chunkVisitor.entitiesSeen.get());
        assertEquals(113, chunkVisitor.tileEntitiesSeen.get());
    }
}
