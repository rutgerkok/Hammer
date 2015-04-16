package nl.rutgerkok.hammer.util;

import java.util.AbstractList;
import java.util.BitSet;
import java.util.List;

/**
 * An implementation of {@link List} backed by a {@link BitSet}.
 *
 * <p>
 * This class is not thread-safe.
 */
public class BooleanList extends AbstractList<Boolean> {

    private BitSet bitSet;
    private int size = 0;
    private final Object writeLock = new Object();

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
        synchronized (writeLock) {
            Boolean old = bitSet.get(index);
            bitSet.set(index, element);
            return old;
        }
    }

    @Override
    public int size() {
        return size;
    }
}
