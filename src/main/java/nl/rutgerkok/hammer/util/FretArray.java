package nl.rutgerkok.hammer.util;

/**
 * I made up this name myself. A fret array is represented internally as an
 * array of 64 bit Entrys (longs). In reality, it contains Entrys of any amount
 * of bits from 4 to 16.
 *
 */
public final class FretArray {

    private static void checkPosition(long[] array, int bitsPerEntry, int position) {
        if (bitsPerEntry < 2 || bitsPerEntry > 16) {
            throw new IllegalArgumentException("Invalid bitsPerEntry: " + bitsPerEntry);
        }
        if (position < 0 || position >= array.length * Long.SIZE / bitsPerEntry) {
            int entries = array.length * Long.SIZE / bitsPerEntry;
            throw new ArrayIndexOutOfBoundsException("Invalid position: " + position + " (array can contain " + entries
                    + " entries of " + bitsPerEntry + " bits each)");
        }
    }

    /**
     * Gets the i'th element in the array.
     *
     * @param array
     *            Stores all entries.
     * @param bitsPerEntry
     *            The amount of bits used per entry.
     * @param position
     *            The position: 0, 1, 2, ... , (array * {@value Long#SIZE} /
     *            bitsPerEntry) - 1
     * @return The value.
     */
    public static char get(long[] array, int bitsPerEntry, int position) {
        checkPosition(array, bitsPerEntry, position);

        long bitPos = position * bitsPerEntry;
        int arrayPos = (int) (bitPos >> 6); // == bitPos / (2**6)
                                            // == bitPos / Long.SIZE
        int bitPosInArray = (int) (bitPos & (Long.SIZE - 1));
        long bitMaskUnshifted = (((long) 1) << bitsPerEntry) - 1;

        long valueInArray = array[arrayPos];
        return (char) ((valueInArray >> bitPosInArray) & bitMaskUnshifted);
    }

    /**
     * Sets the i'th element in the array.
     *
     * @param array
     *            Stores all entries.
     * @param bitsPerEntry
     *            The amount of bits used per entry.
     * @param position
     *            The position: 0, 1, 2, ... , (array * {@value Long#SIZE} /
     *            bitsPerEntry) - 1
     * @param newValue
     *            The value.
     */
    public static void set(long[] array, int bitsPerEntry, int position, char newValue) {
        checkPosition(array, bitsPerEntry, position);

        long bitPos = position * bitsPerEntry;
        int arrayPos = (int) (bitPos >> 6); // == bitPos / (2**6)
                                            // == bitPos / Long.SIZE
        int bitPosInArray = (int) (bitPos & (Long.SIZE - 1));
        long bitMaskUnshifted = (((long) 1) << bitsPerEntry) - 1;
        if (newValue >= bitMaskUnshifted) {
            throw new IllegalArgumentException(newValue + " takes more than " + bitsPerEntry + " bits to store");
        }

        long valueInArray = array[arrayPos];

        // Remove existing value from array
        long maskToEraseExistingValue = ~(bitMaskUnshifted << bitPosInArray);
        valueInArray &= maskToEraseExistingValue;

        // Set new value
        long newValueShifted = ((long) newValue) << bitPosInArray;
        valueInArray |= newValueShifted;

        array[arrayPos] = valueInArray;
    }
}
