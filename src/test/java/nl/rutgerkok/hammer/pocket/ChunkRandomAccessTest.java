package nl.rutgerkok.hammer.pocket;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.Before;
import org.junit.Test;

import nl.rutgerkok.hammer.Chunk;
import nl.rutgerkok.hammer.ChunkAccess;
import nl.rutgerkok.hammer.material.GlobalMaterialMap;
import nl.rutgerkok.hammer.material.MaterialData;
import nl.rutgerkok.hammer.util.MaterialNotFoundException;
import nl.rutgerkok.hammer.util.TestFile;

/**
 * Tests random (as opposed to sequential) access to chunks.
 */
public class ChunkRandomAccessTest {

    private PocketWorld world;

    @Before
    public void loadWorld() throws IOException {
        Path levelDat = TestFile.get("pocket_0_10_4/level.dat");
        world = new PocketWorld(new GlobalMaterialMap(), levelDat);
    }

    @Test
    public void testRetrieveChunk() throws IOException, MaterialNotFoundException {

        try (ChunkAccess<?> chunkAccess = world.getChunkAccess()) {
            Chunk chunk = chunkAccess.getChunk(0, 6);

            // There must be bedrock at layer 0
            MaterialData materialData = chunk.getMaterial(0, 0, 0);
            assertEquals("minecraft:bedrock", materialData.getName());
        }

    }
}
