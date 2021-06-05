package nl.rutgerkok.hammer.anvil.old;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import nl.rutgerkok.hammer.material.BlockDataMaterialMap;
import nl.rutgerkok.hammer.material.GlobalMaterialMap;
import nl.rutgerkok.hammer.material.MaterialData;
import nl.rutgerkok.hammer.util.TestFile;

public class OldMaterialMapTest {

    private static BlockDataMaterialMap map;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        map = new BlockDataMaterialMap(new GlobalMaterialMap(), TestFile.get("blocks_pc_1_12.json").toUri().toURL());
    }

    @Test
    public void testMissingProperties() {
        MaterialData material = map.getMaterialData("minecraft:dropper", (byte) 1);
        assertEquals("minecraft:dropper[facing=up,triggered=false]", material.toString());
    }

    @Test
    public void testNewNameFromOld() {
        MaterialData material = map.getGlobal().getMaterialByName("minecraft:stained_hardened_clay[color=red]");
        assertEquals("minecraft:red_terracotta", material.toString());
        assertEquals("minecraft:stained_hardened_clay", map.getCanonicalMinecraftName(material).getBaseName());
        assertEquals(159 * 16 + 14, map.getMinecraftId(material));

    }

    @Test
    public void testNewNameFromOld2() {
        MaterialData material = map.getGlobal().getMaterialByName("minecraft:dirt[variant=podzol]");
        assertEquals("minecraft:podzol", material.toString());
        assertEquals("minecraft:dirt[variant=podzol]", map.getCanonicalMinecraftName(material).toString());
        assertEquals(3 * 16 + 2, map.getMinecraftId(material));
    }

    @Test
    public void testOldNameFromNew() {
        MaterialData material = map.getGlobal().getMaterialByName("minecraft:red_terracotta");
        assertEquals("minecraft:red_terracotta", material.toString());
        assertEquals("minecraft:stained_hardened_clay", map.getCanonicalMinecraftName(material).getBaseName());
    }
}
