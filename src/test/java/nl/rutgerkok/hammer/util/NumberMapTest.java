package nl.rutgerkok.hammer.util;

import static org.junit.Assert.assertEquals;

import java.util.NoSuchElementException;

import org.junit.Test;

public final class NumberMapTest {

    @Test(expected = IllegalArgumentException.class)
    public void testAddInvalidZeroBinding() {
        NumberMap numberMap = new NumberMap();
        numberMap.put((char) 10, (char) 0);
    }

    @Test
    public void testAddZeroBinding() {
        NumberMap numberMap = new NumberMap();
        numberMap.put((char) 0, (char) 0);
        assertEquals(0, numberMap.getTranslatedId((char) 0));
    }

    @Test
    public void testBasics() {
        NumberMap numberMap = new NumberMap();
        numberMap.put((char) 200, (char) 30);
        assertEquals(30, numberMap.getTranslatedId((char) 200));
    }

    @Test(expected = NoSuchElementException.class)
    public void testNonExistent() throws MaterialNotFoundException {
        NumberMap numberMap = new NumberMap();
        numberMap.getTranslatedId((char) 200);
    }
}
