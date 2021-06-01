package nl.rutgerkok.hammer.util;

/**
 * I made up this name myself. Uses a long[] array as a raw memory block to
 * store an array of numbers, each taking X bits. So you can store an array of 5
 * bit numbers inside a long[] array.
 *
 * <p>
 * Each long in the long[] array is called a cell. There are two implementations
 * available: one where numbers can cross cell boundaries (so for example the
 * first two bits of a five-bit number might be in a different cell than the
 * other three) and one where numbers cannot.
 *
 * @see #crossingCellBoundaries() Creating instances where entries cross cell
 *      boundaries.
 * @see #notCrossingCellBoundaries() Creating instances where entries cannot
 *      cross cell boundaries.
 *
 */
public abstract class FretArray {

    /**
     * Only use for Minecraft 1.13, 1.14 and 1.15. Newer versions use a variant
     * where the 64-bit boundaries aren't crossed anymore.
     *
     */
    private static class CrossingCellBoundaries extends FretArray {
        /**
         * Singleton. Because the instance is stateless, this isn't bad. :)
         */
        private static final CrossingCellBoundaries INSTANCE = new CrossingCellBoundaries();
        private static final long LOW_BITS = 0b00000000_00000000_00000000_00000000_11111111_11111111_11111111_11111111L;
        private static final long HIGH_BITS = 0b11111111_11111111_11111111_11111111_00000000_00000000_00000000_00000000L;

        private CrossingCellBoundaries() {
            // Singleton
        }

        @Override
        public long[] changeBitsPerEntry(long[] oldArray, int numberOfEntries, int bitsPerEntryOld,
                int bitsPerEntryNew) {
            if (bitsPerEntryOld == bitsPerEntryNew) {
                return oldArray;
            }

            long[] newArray = new long[getLongArrayLength(bitsPerEntryNew, numberOfEntries)];
            for (int i = 0; i < numberOfEntries; i++) {
                set(newArray, bitsPerEntryNew, i, get(oldArray, bitsPerEntryOld, i));
            }
            return newArray;
        }

        private void checkPosition(long[] array, int bitsPerEntry, int position) {
            if (bitsPerEntry < 2 || bitsPerEntry > 16) {
                throw new IllegalArgumentException("Invalid bitsPerEntry: " + bitsPerEntry);
            }
            if (position < 0 || position >= array.length * Long.SIZE / bitsPerEntry) {
                int entries = array.length * Long.SIZE / bitsPerEntry;
                throw new ArrayIndexOutOfBoundsException(
                        "Invalid position: " + position + " (array can contain " + entries
                                + " entries of " + bitsPerEntry + " bits each)");
            }
        }

        @Override
        public char get(long[] array, int bitsPerEntry, int position) {
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

        @Override
        public int getLongArrayLength(int numEntries, int bitsPerEntry) {
            return (bitsPerEntry * numEntries + Long.SIZE - 1) >> 6;
        }

        @Override
        public int getMaxNumberOfEntries(long[] array, int bitsPerEntry) {
            return (int) (((long) array.length) * Long.SIZE / bitsPerEntry);
        }

        @Override
        public void set(long[] array, int bitsPerEntry, int position, char newValue) {
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
            }
        }

        @Override
        public String toString(long[] array, int bitsPerEntry) {
            StringBuilder builder = new StringBuilder("{");
            int entries = getMaxNumberOfEntries(array, bitsPerEntry);
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

    private static class NotCrossingCellBoundaries extends FretArray {

        /**
         * Singleton, but it's stateless, so it isn't evil. :)
         */
        public static final FretArray INSTANCE = new NotCrossingCellBoundaries();

        private NotCrossingCellBoundaries() {
            // Singleton
        }

        @Override
        public long[] changeBitsPerEntry(long[] oldArray, int numberOfEntries, int bitsPerEntryOld,
                int bitsPerEntryNew) {
            if (bitsPerEntryOld == bitsPerEntryNew) {
                return oldArray;
            }

            long[] newArray = new long[getLongArrayLength(numberOfEntries, bitsPerEntryNew)];
            for (int i = 0; i < numberOfEntries; i++) {
                set(newArray, bitsPerEntryNew, i, get(oldArray, bitsPerEntryOld, i));
            }
            return newArray;
        }

        private void checkPosition(long[] array, int bitsPerEntry, int position) {
            if (bitsPerEntry < 2 || bitsPerEntry > 16) {
                throw new IllegalArgumentException("Invalid bitsPerEntry: " + bitsPerEntry);
            }
            if (position < 0 || position >= array.length * (Long.SIZE / bitsPerEntry)) {
                int entries = array.length * (Long.SIZE / bitsPerEntry);
                throw new ArrayIndexOutOfBoundsException("Invalid position: " + position + " (array can contain " + entries
                        + " entries of " + bitsPerEntry + " bits each)");
            }
        }

        @Override
        public char get(long[] array, int bitsPerEntry, int index) {
            checkPosition(array, bitsPerEntry, index);

            int entriesPerCell = Long.SIZE / bitsPerEntry;
            int cellIndex = index / entriesPerCell;
            long cellValue = array[cellIndex];
            int bitIndexInCell = (index - cellIndex * entriesPerCell) * bitsPerEntry;
            long mask = (1L << bitsPerEntry) - 1L;
            return (char) (cellValue >> bitIndexInCell & mask);
        }

        @Override
        public int getLongArrayLength(int numEntries, int bitsPerEntry) {
            int entriesPerCell = Long.SIZE / bitsPerEntry;
            int requiredCells = (numEntries + entriesPerCell - 1) / entriesPerCell;
            return requiredCells;
        }

        @Override
        public int getMaxNumberOfEntries(long[] array, int bitsPerEntry) {
            return array.length * (Long.SIZE / bitsPerEntry);
        }

        @Override
        public void set(long[] array, int bitsPerEntry, int index, char newValue) {
            checkPosition(array, bitsPerEntry, index);

            int entriesPerCell = Long.SIZE / bitsPerEntry;
            int cellIndex = index / entriesPerCell;
            long cellValue = array[cellIndex];
            int bitIndexInCell = (index - cellIndex * entriesPerCell) * bitsPerEntry;
            long mask = (1L << bitsPerEntry) - 1L;

            if (newValue < 0 || newValue > mask) {
                throw new IllegalArgumentException(
                        ((int) newValue) + " takes more than " + bitsPerEntry + " bits to store");
            }
            array[cellIndex] = cellValue & (mask << bitIndexInCell ^ 0xFFFFFFFFFFFFFFFFL)
                    | (newValue & mask) << bitIndexInCell;
        }

        @Override
        public String toString(long[] array, int bitsPerEntry) {
            StringBuilder builder = new StringBuilder("{");
            int entries = getMaxNumberOfEntries(array, bitsPerEntry);
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

    /**
     * Gets a fret array where the entries will cross the cell boundaries. This
     * saves some space, but makes compression more difficult, and also makes
     * lookups slower.
     *
     * @return The array.
     */
    public static FretArray crossingCellBoundaries() {
        return CrossingCellBoundaries.INSTANCE;
    }

    /**
     * Gets a fret array where the entries will not cross the cell boundaries. This
     * uses some extra space, but makes compression more efficient and lookups
     * faster.
     *
     * @return The array.
     */
    public static FretArray notCrossingCellBoundaries() {
        return NotCrossingCellBoundaries.INSTANCE;
    }

    /**
     * Changes the number of bits used per entry in the array.
     *
     * @param oldArray
     *            The old array.
     * @param numberOfEntries
     *            The number of entries (in both the old and the new array).
     * @param bitsPerEntryOld
     *            The old number of bits used per entry.
     * @param bitsPerEntryNew
     *            The new number of bits used per entry.
     * @return A new array. (Or the same array if the number of bits didn't change.)
     */
    public abstract long[] changeBitsPerEntry(long[] oldArray, int numberOfEntries, int bitsPerEntryOld,
            int bitsPerEntryNew);

    /**
     * Gets the i'th element in the array.
     *
     * @param array
     *            Stores all entries.
     * @param bitsPerEntry
     *            The amount of bits used per entry.
     * @param index
     *            The position: 0, 1, 2, ... , {@link #getMaxNumberOfEntries(long[], int)} - 1
     * @return The value.
     */
    public abstract char get(long[] array, int bitsPerEntry, int index);

    /**
     * Gets the minimal number of {@code long} elements in a {@code long[]} array
     * required to hold the given amount of {@code bitsPerEntry}-sized elements.
     *
     *
     * @param numEntries
     *            The amount of entries that you want to store.
     * @param bitsPerEntry
     *            The amount of space each entry takes.
     * @return The amount of {@code long}s required to hold those entries.
     */
    public abstract int getLongArrayLength(int numEntries, int bitsPerEntry);

    /**
     * Gets the number of entries the given array can store. Note: the array may
     * actually contain less elements. For example, a long array of one long can
     * store 10 6-bit entries, but only the first seven might be in use. The last
     * few bits are then unused.
     *
     * @param array
     *            The array.
     * @param bitsPerEntry
     *            The number of bits each entry takes.
     * @return The entries.
     */
    public abstract int getMaxNumberOfEntries(long[] array, int bitsPerEntry);

    /**
     * Sets the i'th element in the array.
     *
     * @param array
     *            Stores all entries.
     * @param bitsPerEntry
     *            The amount of bits used per entry.
     * @param index
     *            The position: 0, 1, 2, ... , {@link #getMaxNumberOfEntries(long[], int)} - 1
     * @param newValue
     *            The value.
     */
    public abstract void set(long[] array, int bitsPerEntry, int index, char newValue);

    /**
     * Gets the array as a human readable string.
     *
     * @param array
     *            The array.
     * @param bitsPerEntry
     *            The number of bits used per entry.
     * @return A string like "{9, 5, 2, 5}".
     */
    public abstract String toString(long[] array, int bitsPerEntry);


}
