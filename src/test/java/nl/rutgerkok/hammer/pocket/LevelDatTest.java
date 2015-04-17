package nl.rutgerkok.hammer.pocket;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Path;

import nl.rutgerkok.hammer.pocket.tag.PocketTagFormat;
import nl.rutgerkok.hammer.util.TestFile;

import org.junit.Before;
import org.junit.Test;

public class LevelDatTest {

    private PocketWorld world;

    @Before
    public void loadWorld() throws IOException {
        Path levelDat = TestFile.get("pocket_0_10_4/level.dat");
        world = new PocketWorld(levelDat);
    }

    @Test
    public void testLevelName() throws IOException {
        assertEquals("PocketTest", world.getLevelTag().getString(PocketTagFormat.LEVEL_NAME));
    }
}
