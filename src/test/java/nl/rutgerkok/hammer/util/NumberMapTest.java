package nl.rutgerkok.hammer.util;

import static org.junit.Assert.*;

import org.junit.Test;

public final class NumberMapTest {

    @Test(expected = IllegalArgumentException.class)
    public void testAddInvalidZeroBinding() {
        NumberMap numberMap = new NumberMap();
        numberMap.addId((char) 10, (char) 0);
    }

    @Test
    public void testAddZeroBinding() throws MaterialNotFoundException {
        NumberMap numberMap = new NumberMap();
        numberMap.addId((char) 0, (char) 0);
        assertEquals(0, numberMap.getTranslatedId((char) 0));
    }

    @Test
    public void testBasics() throws MaterialNotFoundException {
        NumberMap numberMap = new NumberMap();
        numberMap.addId((char) 200, (char) 30);
        assertEquals(30, numberMap.getTranslatedId((char) 200));
    }

    @Test(expected = MaterialNotFoundException.class)
    public void testNonExistent() throws MaterialNotFoundException {
        NumberMap numberMap = new NumberMap();
        numberMap.getTranslatedId((char) 200);
    }
}
