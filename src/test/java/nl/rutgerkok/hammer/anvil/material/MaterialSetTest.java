package nl.rutgerkok.hammer.anvil.material;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import nl.rutgerkok.hammer.material.MaterialSet;

import org.junit.Test;

public class MaterialSetTest {

    @Test
    public void testAdd() {
        AnvilMaterial material = new AnvilMaterial("foo", 1);
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
        AnvilMaterial material = new AnvilMaterial("foo", 1);
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
