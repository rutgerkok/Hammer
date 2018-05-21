package nl.rutgerkok.hammer.util;

import java.util.AbstractList;
import java.util.BitSet;
import java.util.List;

/**
 * An implementation of {@link List} backed by a {@link BitSet}.
 */
public class BooleanList extends AbstractList<Boolean> {

    private BitSet bitSet;
    private int size = 0;

    /**
     * Creates a new list.
     */
    public BooleanList() {
        this.bitSet = new BitSet();
    }

    /**
     * Creates a new list.
     *
     * @param size
     *            Initial size of the list.
     */
    public BooleanList(int size) {
        this.bitSet = new BitSet(size);
    }

    @Override
    public void add(int index, Boolean element) {
        if (index == size) {
            // Just add a new element
            bitSet.set(index, element);
            size++;
            return;
        }

        // We need to insert a value
        checkRange(index);

        // Shift all elements next to it
        int length = bitSet.length();
        for (int i = length; i > index; i--) {
            bitSet.set(i, bitSet.get(i - 1));
        }

        // Finally set the element
        bitSet.set(index, element);
        size++;
    }

    private void checkRange(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("size=" + size + ", index=" + index);
        }
    }

    @Override
    public void clear() {
        this.size = 0;
        bitSet = new BitSet();
    }

    public boolean contains(Boolean element) {
        if (element == null) {
            return false;
        }
        if (element.booleanValue()) {
            return bitSet.nextSetBit(0) != -1;
        } else {
            return bitSet.nextClearBit(0) != -1;
        }
    }

    @Override
    public Boolean get(int index) {
        checkRange(index);
        return bitSet.get(index);
    }

    @Override
    public Boolean remove(int index) {
        checkRange(index);

        Boolean old = bitSet.get(index);

        // Shift all elements
        int length = bitSet.length();
        for (int i = index; i < length - 1; i++) {
            bitSet.set(i, bitSet.get(i + 1));
        }

        // Remove the last element
        bitSet.clear(length - 1);
        size--;

        return old;
    }

    @Override
    public Boolean set(int index, Boolean element) {
        Boolean old = bitSet.get(index);
        bitSet.set(index, element);
        return old;
    }

    @Override
    public int size() {
        return size;
    }
}
