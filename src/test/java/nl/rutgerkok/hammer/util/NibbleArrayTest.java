package nl.rutgerkok.hammer.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NibbleArrayTest {

    @Test
    public void testByteArrayLength() {
        // Check if byte array has correct size
        // (need 3 bites to fit 5 nibbles)
        NibbleArray nibbleArray = new NibbleArray(5);
        assertEquals(3, nibbleArray.getHandle().length);
    }

    @Test
    public void testGetFilledByte() {
        NibbleArray nibbleArray = new NibbleArray(new byte[] { (byte) 0xFF });
        assertEquals(0xF, nibbleArray.get(0));
        assertEquals(0xF, nibbleArray.get(1));
    }

    @Test
    public void testGetOneByte() {
        // Premade byte, check returned nibbles using the methods
        NibbleArray nibbleArray = new NibbleArray(new byte[] { 0b0001__0000 });
        assertEquals(0b0001, nibbleArray.get(1));
        assertEquals(0b0000, nibbleArray.get(0));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testOverBounds() {
        // One-and-a-half byte, so 3 nibbles, so index 3 is too high
        NibbleArray nibbleArray = new NibbleArray(3);
        nibbleArray.set(3, (byte) 1);
    }

    @Test
    public void testPositionOne() {
        // Sets a byte to 16 by setting the second position to 1
        NibbleArray nibbleArray = new NibbleArray(new byte[1]);
        nibbleArray.set(1, (byte) 1);

        byte result = nibbleArray.getHandle()[0];
        assertEquals((byte) 0b0001__0000, result);
    }

    @Test
    public void testPositionZero() {
        // Sets a byte to 1 by setting the first position to 1
        NibbleArray nibbleArray = new NibbleArray(new byte[1]);
        nibbleArray.set(0, (byte) 1);

        byte result = nibbleArray.getHandle()[0];
        assertEquals((byte) 0b0000__0001, result);
    }

    @Test
    public void testSetAndGetTwoBytes() {
        // Everything coded with the methods
        NibbleArray nibbleArray = new NibbleArray(4);
        nibbleArray.set(0, (byte) 0b0001);
        nibbleArray.set(1, (byte) 0b0010);
        nibbleArray.set(2, (byte) 0b0100);
        nibbleArray.set(3, (byte) 0b1000);

        assertEquals(0b0001, nibbleArray.get(0));
        assertEquals(0b0010, nibbleArray.get(1));
        assertEquals(0b0100, nibbleArray.get(2));
        assertEquals(0b1000, nibbleArray.get(3));
    }

    @Test
    public void testSetTwoBytes() {
        // Setup using the methods, check for premade result
        NibbleArray nibbleArray = new NibbleArray(4);
        nibbleArray.set(0, (byte) 0b0001);
        nibbleArray.set(1, (byte) 0b0010);
        nibbleArray.set(2, (byte) 0b0100);
        nibbleArray.set(3, (byte) 0b1000);

        byte[] expected = new byte[] { 0b0010__0001, (byte) 0b1000__0100 };
        assertArrayEquals(expected, nibbleArray.getHandle());
    }

    @Test
    public void testTruncating() {
        // Test if second nibble is unaffected by setting the first nibble to a
        // value that is too high
        NibbleArray nibbleArray = new NibbleArray(new byte[1]);
        nibbleArray.set(1, (byte) 0b1111__1111);

        byte result = nibbleArray.getHandle()[0];
        assertEquals((byte) 0b1111__0000, result);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testUnderBounds() {
        // -1 is invalid position, obviously
        NibbleArray nibbleArray = new NibbleArray(new byte[1]);
        nibbleArray.set(-1, (byte) 1);
    }

}
