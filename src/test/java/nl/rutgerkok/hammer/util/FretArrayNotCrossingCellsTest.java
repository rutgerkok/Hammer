package nl.rutgerkok.hammer.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FretArrayNotCrossingCellsTest {

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void negativePositionIsNotAllowed() {
        long[] array = { 3689348814741905697L, 3689348814741910323L };
        assertEquals(1, FretArray.notCrossingCellBoundaries().get(array, 4, -1));
    }

    @Test
    public void testFiveBits() {
        // 5 bits sometimes need to be distributed over two longs
        long[] array = { 0, 0 }; // Enough room for 24 entries
        FretArray.notCrossingCellBoundaries().set(array, 5, 12, (char) 23);
        assertEquals("{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 23, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}",
                FretArray.notCrossingCellBoundaries().toString(array, 5));
    }

    @Test
    public void testGet() {
        long[] array = { 3689348814741905697L, 3689348814741910323L };
        assertEquals(1, FretArray.notCrossingCellBoundaries().get(array, 4, 0));
        assertEquals(2, FretArray.notCrossingCellBoundaries().get(array, 4, 1));
        assertEquals(1, FretArray.notCrossingCellBoundaries().get(array, 4, 2));
        assertEquals(2, FretArray.notCrossingCellBoundaries().get(array, 4, 3));
        assertEquals(3, FretArray.notCrossingCellBoundaries().get(array, 4, 31));
    }

    @Test
    public void testLongArrayLength() {
        assertEquals(2, FretArray.notCrossingCellBoundaries().getLongArrayLength(128, 1));
        assertEquals(2, FretArray.notCrossingCellBoundaries().getLongArrayLength(64, 2));
        assertEquals(2, FretArray.notCrossingCellBoundaries().getLongArrayLength(16, 8));

        assertEquals(5, FretArray.notCrossingCellBoundaries().getLongArrayLength(5, 64));

        assertEquals(410, FretArray.notCrossingCellBoundaries().getLongArrayLength(4096, 6));

    }

    @Test
    public void testSet() {
        // Change the first four positions so that there are only 4-bit 3's in
        // the array
        long[] array = { 3689348814741905697L, 3689348814741910323L };
        FretArray.notCrossingCellBoundaries().set(array, 4, 0, (char) 3);
        FretArray.notCrossingCellBoundaries().set(array, 4, 1, (char) 3);
        FretArray.notCrossingCellBoundaries().set(array, 4, 2, (char) 3);
        FretArray.notCrossingCellBoundaries().set(array, 4, 3, (char) 3);

        assertEquals(3689348814741910323L, array[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetTooBigValue() {
        // 16 does not fit in four bits
        long[] array = { 3689348814741905697L, 3689348814741910323L };
        FretArray.notCrossingCellBoundaries().set(array, 4, 0, (char) 16);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSeventeenBitsIsNotAllowed() {
        // A char is only 16 bit, so 17 bits won't work
        long[] array = { 3689348814741905697L, 3689348814741910323L };
        assertEquals(1, FretArray.notCrossingCellBoundaries().get(array, 17, 1));
    }

    @Test
    public void testToString() {
        long[] array = {0, 0};
        FretArray.notCrossingCellBoundaries().set(array, 4, 0, (char) 3);
        FretArray.notCrossingCellBoundaries().set(array, 4, 1, (char) 5);
        FretArray.notCrossingCellBoundaries().set(array, 4, 2, (char) 2);
        FretArray.notCrossingCellBoundaries().set(array, 4, 3, (char) 15);
        assertEquals(
                "{3, 5, 2, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}",
                FretArray.notCrossingCellBoundaries().toString(array, 4));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testZeroBitsIsNotAllowed() {
        long[] array = { 3689348814741905697L, 3689348814741910323L };
        assertEquals(1, FretArray.notCrossingCellBoundaries().get(array, 0, 1));
    }


    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void tooBigPositionIsNotAllowed() {
        long[] array = { 3689348814741905697L, 3689348814741910323L };
        assertEquals(1, FretArray.notCrossingCellBoundaries().get(array, 4, 32));
    }
}
