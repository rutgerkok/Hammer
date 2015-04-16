package nl.rutgerkok.hammer.anvil.material;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import nl.rutgerkok.hammer.material.MaterialData;

import org.junit.Test;

public class MaterialDataTest {

    @Test
    public void testBlockDataMatch() {
        AnvilMaterial testMaterial = new AnvilMaterial("test", 1);
        MaterialData specifiedBlockData = AnvilMaterialData.of(testMaterial, (byte) 0);
        MaterialData anotherBlockData = AnvilMaterialData.of(testMaterial, (byte) 2);
        MaterialData unspecifiedBlockData = AnvilMaterialData.ofAnyState(testMaterial);

        assertFalse(specifiedBlockData.isBlockDataUnspecified());
        assertTrue(unspecifiedBlockData.isBlockDataUnspecified());

        assertNotEquals(specifiedBlockData, unspecifiedBlockData);

        assertFalse(specifiedBlockData.matches(anotherBlockData));
        assertTrue(unspecifiedBlockData.matches(anotherBlockData));
    }
}
