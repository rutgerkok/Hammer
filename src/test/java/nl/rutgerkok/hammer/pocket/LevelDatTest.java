package nl.rutgerkok.hammer.pocket;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.Before;
import org.junit.Test;

import nl.rutgerkok.hammer.material.GlobalMaterialMap;
import nl.rutgerkok.hammer.pocket.tag.PocketFormat.LevelTag;
import nl.rutgerkok.hammer.util.TestFile;

public class LevelDatTest {

    private PocketWorld world;

    @Before
    public void loadWorld() throws IOException {
        Path levelDat = TestFile.get("pocket_0_10_4/level.dat");
        world = new PocketWorld(new GlobalMaterialMap(), levelDat);
    }

    @Test
    public void testLevelName() throws IOException {
        assertEquals("PocketTest", world.getLevelTag().getString(LevelTag.LEVEL_NAME));
    }
}
