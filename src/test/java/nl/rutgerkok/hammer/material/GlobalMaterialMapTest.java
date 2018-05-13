package nl.rutgerkok.hammer.material;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.text.ParseException;
import java.util.Arrays;

import org.junit.Test;

import nl.rutgerkok.hammer.util.MaterialNotFoundException;

public class GlobalMaterialMapTest {

    @Test
    public void testAliases() throws MaterialNotFoundException, ParseException {
        GlobalMaterialMap dictionary = new GlobalMaterialMap();
        MaterialData withAliases = dictionary.addMaterial(Arrays.asList(MaterialName.ofBaseName("minecraft:foo"),
                MaterialName.ofBaseName("minecraft:bar"), MaterialName.ofBaseName("minecraft:baz")));

        assertEquals(withAliases, dictionary.getMaterialByName(MaterialName.ofBaseName("minecraft:foo")));
        assertEquals(withAliases, dictionary.getMaterialByName(MaterialName.ofBaseName("minecraft:bar")));
        assertEquals(withAliases, dictionary.getMaterialByName(MaterialName.ofBaseName("minecraft:baz")));

        // For names without a Minecraft prefix, they must be case-insensitive
        assertEquals(withAliases, dictionary.getMaterialByName(MaterialName.parse("Foo")));
        assertEquals(withAliases, dictionary.getMaterialByName(MaterialName.parse("BAR")));
        assertEquals(withAliases, dictionary.getMaterialByName(MaterialName.parse("bAz")));
    }

    @Test
    public void testCombining() {
        // Add a material, then add second material that is equal according to
        // its alias. Test equality and test lookup of second name

        GlobalMaterialMap dictionary = new GlobalMaterialMap();
        MaterialData first = dictionary.addMaterial(MaterialName.ofBaseName("test:test"));
        MaterialData second = dictionary.addMaterial(
                Arrays.asList(MaterialName.ofBaseName("test:otherName"), MaterialName.ofBaseName("test:test")));

        assertEquals(first, second);
        assertEquals(first, dictionary.getMaterialByName(MaterialName.ofBaseName("test:otherName")));
    }

    @Test
    public void testEquality() {
        // Registering two materials with different names should yield different
        // instances, but registering two materials with the same name should
        // yield equal instances.
        GlobalMaterialMap dictionary = new GlobalMaterialMap();

        MaterialData first = dictionary.addMaterial(MaterialName.ofBaseName("test:foo"));
        MaterialData second = dictionary.addMaterial(MaterialName.ofBaseName("test:bar"));
        MaterialData sameAsFirst = dictionary.addMaterial(MaterialName.ofBaseName("test:foo"));

        assertNotEquals(first, second);
        assertEquals(first, sameAsFirst);
    }
}
