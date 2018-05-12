package nl.rutgerkok.hammer.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FretArrayTest {

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void negativePositionIsNotAllowed() {
        long[] array = { 3689348814741905697L, 3689348814741910323L };
        assertEquals(1, FretArray.get(array, 4, -1));
    }

    @Test
    public void testGet() {
        long[] array = { 3689348814741905697L, 3689348814741910323L };
        assertEquals(1, FretArray.get(array, 4, 0));
        assertEquals(2, FretArray.get(array, 4, 1));
        assertEquals(1, FretArray.get(array, 4, 2));
        assertEquals(2, FretArray.get(array, 4, 3));
        assertEquals(3, FretArray.get(array, 4, 31));
    }

    @Test
    public void testSet() {
        // Change the first four positions so that there are only 4-bit 3's in
        // the array
        long[] array = { 3689348814741905697L, 3689348814741910323L };
        FretArray.set(array, 4, 0, (char) 3);
        FretArray.set(array, 4, 1, (char) 3);
        FretArray.set(array, 4, 2, (char) 3);
        FretArray.set(array, 4, 3, (char) 3);

        assertEquals(3689348814741910323L, array[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetTooBigValue() {
        // 16 does not fit in four bits
        long[] array = { 3689348814741905697L, 3689348814741910323L };
        FretArray.set(array, 4, 0, (char) 16);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSeventeenBitsIsNotAllowed() {
        // A char is only 16 bit, so 17 bits won't work
        long[] array = { 3689348814741905697L, 3689348814741910323L };
        assertEquals(1, FretArray.get(array, 17, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testZeroBitsIsNotAllowed() {
        long[] array = { 3689348814741905697L, 3689348814741910323L };
        assertEquals(1, FretArray.get(array, 0, 1));
    }


    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void tooBigPositionIsNotAllowed() {
        long[] array = { 3689348814741905697L, 3689348814741910323L };
        assertEquals(1, FretArray.get(array, 4, 32));
    }
}
