package nl.rutgerkok.hammer.util;

import java.util.NoSuchElementException;

/**
 * Simple class that maps numbers (represented by chars, as those are
 * essentially unsigned shorts) to each other.
 *
 */
public final class NumberMap {

    private char[] idMapping = new char[0];

    public void put(char originalId, char translatedId) {
        // Mapping to 0 (null) is special cased
        if (translatedId == 0) {
            if (originalId != 0) {
                throw new IllegalArgumentException("Can't map to translated id of 0 (null) unless the original id is also 0");
            }
            return;
        }

        if (originalId >= idMapping.length) {
            growToSize(originalId);
        }

        idMapping[originalId] = translatedId;
    }

    public char getTranslatedId(char originalId) throws NoSuchElementException {

        if (originalId == 0) {
            return 0;
        }

        if (originalId > idMapping.length) {
            throw new NoSuchElementException();
        }

        char translatedId = idMapping[originalId];
        if (translatedId == 0) {
            // No mapping found
            throw new NoSuchElementException();
        }

        return translatedId;
    }

    private void growToSize(int highestIndexThatMustFit) {
        // Need to resize array
        int newSize = Math.max(1, idMapping.length);
        while (newSize <= highestIndexThatMustFit) {
            newSize *= 2;

            if (newSize <= 0 || newSize > Character.MAX_VALUE * 2) {
                // This should be impossible, as newSize only grows in powers of
                // two until it can fit a char
                throw new AssertionError("newSize got too big: " + newSize);
            }
        }
        char[] newArray = new char[newSize];
        System.arraycopy(idMapping, 0, newArray, 0, idMapping.length);
        idMapping = newArray;
    }
}
