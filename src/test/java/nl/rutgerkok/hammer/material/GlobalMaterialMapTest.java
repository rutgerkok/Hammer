package nl.rutgerkok.hammer.material;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Arrays;

import org.junit.Test;

public class GlobalMaterialMapTest {

    @Test
    public void testEquality() {
        // Registering two materials with different names should yield different
        // instances, but registering two materials with the same name should
        // yield equal instances.
        GlobalMaterialMap dictionary = new GlobalMaterialMap();

        MaterialData first = dictionary.addMaterial("foo");
        MaterialData second = dictionary.addMaterial("bar");
        MaterialData sameAsFirst = dictionary.addMaterial("foo");

        assertNotEquals(first, second);
        assertEquals(first, sameAsFirst);
    }

    @Test
    public void testAliases() {
        // getMaterialByName must be able to look up aliases in a case
        // insensitive manner

        GlobalMaterialMap dictionary = new GlobalMaterialMap();
        MaterialData withAliases = dictionary.addMaterial(Arrays.asList("foo", "bar", "baz"));

        assertEquals(withAliases, dictionary.getMaterialByName("foo"));
        assertEquals(withAliases, dictionary.getMaterialByName("bar"));
        assertEquals(withAliases, dictionary.getMaterialByName("baz"));

        assertEquals(withAliases, dictionary.getMaterialByName("Foo"));
        assertEquals(withAliases, dictionary.getMaterialByName("BAR"));
        assertEquals(withAliases, dictionary.getMaterialByName("bAz"));
    }

    @Test
    public void testCombining() {
        // Add a material, then add second material that is equal according to
        // its alias. Test equality and test lookup of second name

        GlobalMaterialMap dictionary = new GlobalMaterialMap();
        MaterialData first = dictionary.addMaterial("test");
        MaterialData second = dictionary.addMaterial(Arrays.asList("otherName", "test"));

        assertEquals(first, second);
        assertEquals(first, dictionary.getMaterialByName("otherName"));
    }
}
