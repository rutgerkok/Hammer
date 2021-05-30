package nl.rutgerkok.hammer.anvil;

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
 * Tests getting a block at negative y.
 */
public class Chunk117RandomAccessTest {

    private AnvilWorld world;
    private int[] xyz = new int[] { 179, -49, -227 };

    @Before
    public void loadWorld() throws IOException {
        Path levelDat = TestFile.get("anvil_1_17_extended_height/level.dat");
        world = new AnvilWorld(new GlobalMaterialMap(), levelDat);
    }

    @Test
    public void testRetrieveChunk() throws IOException, MaterialNotFoundException {
        try (ChunkAccess<?> chunkAccess = world.getChunkAccess()) {
            Chunk chunk = chunkAccess.getChunk(Math.floorDiv(xyz[0], AnvilChunk.CHUNK_X_SIZE), Math
                    .floorDiv(xyz[2], AnvilChunk.CHUNK_Z_SIZE));

            // There must be bedrock at this location
            MaterialData materialData = chunk
                    .getMaterial(Math.floorMod(xyz[0], AnvilChunk.CHUNK_X_SIZE), xyz[1], Math
                            .floorMod(xyz[2], AnvilChunk.CHUNK_Z_SIZE));
            assertEquals("minecraft:tuff", materialData.getName());
        }

    }
}
