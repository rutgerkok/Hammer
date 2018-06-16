package nl.rutgerkok.hammer.material;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import nl.rutgerkok.hammer.util.TestFile;

public class BlockDataMaterialMapTest {

    private static BlockDataMaterialMap map;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        map = new BlockDataMaterialMap(new GlobalMaterialMap(), TestFile.get("blocks_pc.json").toUri().toURL());
    }

    @Test
    public void testMissingProperties() {
        MaterialData material = map.getGlobal().getMaterialByName("minecraft:dropper");
        assertEquals("minecraft:dropper[facing=down,triggered=false]", material.toString());
    }

    @Test
    public void testOldName() {
        MaterialData material = map.getGlobal().getMaterialByName("minecraft:stained_hardened_clay[color=red]");
        assertEquals("minecraft:red_terracotta", material.toString());
    }
}
