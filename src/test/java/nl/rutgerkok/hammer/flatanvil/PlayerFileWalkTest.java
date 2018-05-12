package nl.rutgerkok.hammer.flatanvil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.Test;

import nl.rutgerkok.hammer.PlayerFile;
import nl.rutgerkok.hammer.World;
import nl.rutgerkok.hammer.anvil.AnvilWorld;
import nl.rutgerkok.hammer.anvil.tag.AnvilFormat;
import nl.rutgerkok.hammer.material.GlobalMaterialMap;
import nl.rutgerkok.hammer.tag.CompoundTag;
import nl.rutgerkok.hammer.util.Progress;
import nl.rutgerkok.hammer.util.Result;
import nl.rutgerkok.hammer.util.TestFile;
import nl.rutgerkok.hammer.util.Visitor;

public class PlayerFileWalkTest {

    private class TestVisitor implements Visitor<PlayerFile> {
        private int fileCount = 0;

        @Override
        public Result accept(PlayerFile value, Progress progress) {
            CompoundTag tag = value.getTag();
            assertTrue("Tag must contain inventory subtag", tag.containsKey(AnvilFormat.PlayerTag.INVENTORY));
            fileCount++;
            return Result.NO_CHANGES;
        }
    }

    @Test
    public void testBasicUsage() throws IOException {
        Path levelDat = TestFile.get("anvil_1_13/level.dat");
        World world = new AnvilWorld(new GlobalMaterialMap(), levelDat);

        TestVisitor visitor = new TestVisitor();
        world.walkPlayerFiles(visitor);

        assertEquals("This world has one player data tag", 1, visitor.fileCount);
    }

}
