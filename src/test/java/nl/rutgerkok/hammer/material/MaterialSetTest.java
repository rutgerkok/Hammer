package nl.rutgerkok.hammer.material;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import nl.rutgerkok.hammer.material.GlobalMaterialMap;
import nl.rutgerkok.hammer.material.MaterialData;
import nl.rutgerkok.hammer.material.MaterialSet;

public class MaterialSetTest {

    @Test
    public void testAdd() {
        GlobalMaterialMap dictionary = new GlobalMaterialMap();
        MaterialData material = dictionary.addMaterial("foo");

        MaterialSet set = new MaterialSet();
        assertFalse(set.contains(material));

        // Now add it
        set.add(material);
        assertTrue(set.contains(material));
    }

    @Test(expected = NullPointerException.class)
    public void testAddNull() {
        MaterialSet set = new MaterialSet();
        set.add(null);
    }

    @Test
    public void testRemove() {
        GlobalMaterialMap dictionary = new GlobalMaterialMap();
        MaterialData material = dictionary.addMaterial("foo");

        MaterialSet set = new MaterialSet();
        set.add(material);

        // Remove it
        assertTrue(set.remove(material));
        assertFalse(set.contains(material));
    }

    @Test
    public void testRemoveNonExistant() {
        MaterialSet set = new MaterialSet();

        // Remove it
        assertFalse(set.remove(new Object()));
    }
}
