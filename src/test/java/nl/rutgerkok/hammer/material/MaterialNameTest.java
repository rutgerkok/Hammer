package nl.rutgerkok.hammer.material;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.Collections;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class MaterialNameTest {

    @Test
    public void basics() {
        MaterialName test = MaterialName.ofBaseName("test:foo");
        assertEquals("test:foo", test.getBaseName());
        assertEquals(Collections.emptyMap(), test.getProperties());
        assertEquals("test:foo", test.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void fullNamesCannotAccidentallyBeUsedAsBaseNames() {
        MaterialName.ofBaseName("minecraft:grass[snowy=false]");
    }

    @Test
    public void parsingMultipleProperties() throws ParseException {
        MaterialName dispenserDown = MaterialName.parse("minecraft:dispenser[facing=down,triggered=false]");
        MaterialName dispenser2Down = MaterialName.parse("minecraft:dispenser[triggered=false,facing=down]");
        MaterialName dispenserUp = MaterialName.parse("minecraft:dispenser[facing=up,triggered=false]");

        assertEquals(dispenserDown, dispenser2Down);
        assertEquals(ImmutableMap.of("facing", "down", "triggered", "false"), dispenserDown.getProperties());
        assertNotEquals(dispenserDown, dispenserUp);
        assertTrue(dispenserDown.toString().contains(","));
    }

    @Test(expected = ParseException.class)
    public void parsingWithMissingAssignmentOperator() throws ParseException {
        MaterialName.parse("minecraft:grass[snowy]");
    }

    @Test(expected = ParseException.class)
    public void parsingWithMissingClosingBracket() throws ParseException {
        MaterialName.parse("minecraft:grass[snowy=false");
    }

    @Test
    public void parsingWithoutPrefix() throws ParseException {
        MaterialName dirt = MaterialName.parse("DIRT");

        assertEquals("minecraft:dirt", dirt.getBaseName());
        assertEquals(ImmutableMap.of(), dirt.getProperties());
        assertEquals("minecraft:dirt", dirt.toString());
    }

    @Test
    public void parsingWithProperties() throws ParseException {
        MaterialName grass = MaterialName.parse("minecraft:grass[snowy=false]");

        assertEquals("minecraft:grass", grass.getBaseName());
        assertEquals(ImmutableMap.of("snowy", "false"), grass.getProperties());
        assertEquals("minecraft:grass[snowy=false]", grass.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void prefixIsObligatory() {
        MaterialName.ofBaseName("foo");
    }
}
