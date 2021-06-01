package nl.rutgerkok.hammer.anvil;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import nl.rutgerkok.hammer.util.MaterialNotFoundException;

public class ChunkDataVersionTest {

    @Test
    public void testAfter() throws IOException, MaterialNotFoundException {
        assertFalse(ChunkDataVersion.MINECRAFT_1_12_2.isAfter(ChunkDataVersion.MINECRAFT_1_13));
        assertFalse(ChunkDataVersion.MINECRAFT_1_12_2.isAfter(ChunkDataVersion.MINECRAFT_1_12_2));
        assertTrue(ChunkDataVersion.MINECRAFT_1_12_2.isAfter(ChunkDataVersion.MINECRAFT_1_12_1));
    }

    @Test
    public void testBefore() throws IOException, MaterialNotFoundException {
        assertTrue(ChunkDataVersion.MINECRAFT_1_12_2.isBefore(ChunkDataVersion.MINECRAFT_1_13));
        assertFalse(ChunkDataVersion.MINECRAFT_1_12_2.isBefore(ChunkDataVersion.MINECRAFT_1_12_2));
        assertFalse(ChunkDataVersion.MINECRAFT_1_12_2.isBefore(ChunkDataVersion.MINECRAFT_1_12_1));
    }
}
