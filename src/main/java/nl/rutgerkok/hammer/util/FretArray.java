package nl.rutgerkok.hammer.util;

/**
 * I made up this name myself. A fret array is represented internally as an
 * array of 64 bit numbers (longs). However, you need to view this as one
 * continuous bitstream, that actually contains with bit sizes of 4 to 16. So
 * you might have an 5-bit array.
 *
 */
public final class FretArray {

    private static final long LOW_BITS = 0b00000000_00000000_00000000_00000000_11111111_11111111_11111111_11111111L;
    private static final long HIGH_BITS = 0b11111111_11111111_11111111_11111111_00000000_00000000_00000000_00000000L;

    /**
     * Changes the number of bits used per entry in the array.
     * @param oldArray The old array.
     * @param bitsPerEntryOld The number of bits previously used per entry.
     * @param bitsPerEntryNew The new number of bits used per entry.
     * @return A new array. (Or the same array if the number of bits didn't change.)
     */
    public static long[] changeBitsPerEntry(long[] oldArray, int bitsPerEntryOld, int bitsPerEntryNew) {
        if (bitsPerEntryOld == bitsPerEntryNew) {
            return oldArray;
        }

        int numberOfEntries = getNumberOfEntries(oldArray, bitsPerEntryOld);
        long[] newArray = new long[getLongArrayLength(bitsPerEntryNew, numberOfEntries)];
        for (int i = 0; i < numberOfEntries; i++) {
            set(newArray, bitsPerEntryNew, i, get(oldArray, bitsPerEntryOld, i));
        }
        return newArray;
    }

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
        boolean crossingBorders = false;
        if (bitPosInArray + bitsPerEntry > Long.SIZE) {
            // Ok, some bits of the next long also need to be changed
            bitPosInArray -= 32;
            crossingBorders = true;
        }

        long bitMaskUnshifted = (1L << bitsPerEntry) - 1;

        long valueInArray;
        if (!crossingBorders) {
            valueInArray = array[arrayPos];
        } else {
            valueInArray = ((array[arrayPos] & HIGH_BITS) >>> 32) | ((array[arrayPos + 1] & LOW_BITS) << 32);
        }
        return (char) ((valueInArray >> bitPosInArray) & bitMaskUnshifted);
    }

    /**
     * Gets the minimal number of {@code long} elements in a {@code long[]}
     * array required to hold the given amount of {@code bitsPerEntry}-sized
     * elements.
     *
     *
     * @param numEntries
     *            The amount of entries that you want to store.
     * @param bitsPerEntry
     *            The amount of space each entry takes.
     * @return The amount of {@code long}s required to hold those entries.
     */
    public static int getLongArrayLength(int numEntries, int bitsPerEntry) {
        return (bitsPerEntry * numEntries + Long.SIZE - 1) >> 6;
    }

    /**
     * Gets the number of entries the given array can store.
     *
     * @param array
     *            The array.
     * @param bitsPerEntry
     *            The number of bits each entry takes.
     * @return The entries.
     */
    public static int getNumberOfEntries(long[] array, int bitsPerEntry) {
        return (int) (((long) array.length) * Long.SIZE / bitsPerEntry);
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
        boolean crossingBorders = false;
        if (bitPosInArray + bitsPerEntry > Long.SIZE) {
            // Ok, some bits of the next long also need to be changed
            bitPosInArray -= 32;
            crossingBorders = true;
        }
        long bitMaskUnshifted = (1L << bitsPerEntry) - 1;
        if (newValue > bitMaskUnshifted) {
            throw new IllegalArgumentException(
                    ((int) newValue) + " takes more than " + bitsPerEntry + " bits to store");
        }

        // Get existing value (taking from two different numbers if necessary)
        long valueInArray;
        if (!crossingBorders) {
            valueInArray = array[arrayPos];
        } else {
            valueInArray = ((array[arrayPos] & HIGH_BITS) >>> 32) | ((array[arrayPos + 1] & LOW_BITS) << 32);
        }

        // Remove existing value from array
        long maskToEraseExistingValue = ~(bitMaskUnshifted << bitPosInArray);
        valueInArray &= maskToEraseExistingValue;

        // Set new value
        long newValueShifted = ((long) newValue) << bitPosInArray;
        valueInArray |= newValueShifted;

        if (!crossingBorders) {
            array[arrayPos] = valueInArray;
        } else {
            array[arrayPos] = ((valueInArray & LOW_BITS) << 32) | (array[arrayPos] & LOW_BITS);
            array[arrayPos + 1] = ((valueInArray & HIGH_BITS) >>> 32) | (array[arrayPos + 1] & HIGH_BITS);
            System.out.println(array);
        }
    }

    /**
     * Gets the array as a human readable string.
     *
     * @param array
     *            The array.
     * @param bitsPerEntry
     *            The number of bits used per entry.
     * @return A string like "{9, 5, 2, 5}".
     */
    public static String toString(long[] array, int bitsPerEntry) {
        StringBuilder builder = new StringBuilder("{");
        int entries = getNumberOfEntries(array, bitsPerEntry);
        for (int i = 0; i < entries; i++) {
            builder.append((int) get(array, bitsPerEntry, i));
            builder.append(", ");
        }
        if (entries > 0) {
            // Replace last ", " with "}"
            builder.setCharAt(builder.length() - 2, '}');
            builder.deleteCharAt(builder.length() - 1);
            return builder.toString();
        }
        return "{}";
    }
}
