package nl.rutgerkok.hammer.anvil;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Path;

import nl.rutgerkok.hammer.Chunk;
import nl.rutgerkok.hammer.ChunkAccess;
import nl.rutgerkok.hammer.util.TestFile;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests random (as opposed to sequential) access to chunks.
 */
public class ChunkRandomAccessTest {

    private AnvilWorld world;

    @Before
    public void loadWorld() throws IOException {
        Path levelDat = TestFile.get("anvil_1_7_10/level.dat");
        world = new AnvilWorld(levelDat);
    }

    @Test
    public void testRetrieveChunk() throws IOException {
        try (ChunkAccess<?> chunkAccess = world.getChunkAccess()) {
            Chunk chunk = chunkAccess.getChunk(0, 9);

            // There must be bedrock at layer 0
            assertEquals(7, chunk.getMaterialId(0, 0, 0));
        }

    }
}
