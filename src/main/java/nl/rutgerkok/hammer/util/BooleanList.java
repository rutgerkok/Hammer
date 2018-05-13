package nl.rutgerkok.hammer.util;

import java.util.AbstractList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * An implementation of {@link List} backed by a {@link BitSet}. I think it's
 * thread-safe.
 */
public class BooleanList extends AbstractList<Boolean> {

    private BitSet bitSet;
    private int size = 0;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

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
        try {
            lock.writeLock().lock();
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
        } finally {
            lock.writeLock().unlock();

        }
    }

    private void checkRange(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("size=" + size + ", index=" + index);
        }
    }

    @Override
    public void clear() {
        try {
            lock.writeLock().lock();
            this.size = 0;
            bitSet = new BitSet();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Boolean get(int index) {
        try {
            lock.readLock().lock();
            checkRange(index);
            return bitSet.get(index);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Boolean remove(int index) {
        try {
            lock.writeLock().lock();
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
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Boolean set(int index, Boolean element) {
        try {
            lock.writeLock().lock();
            Boolean old = bitSet.get(index);
            bitSet.set(index, element);
            return old;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public int size() {
        try {
            lock.readLock().lock();
            return size;
        } finally {
            lock.readLock().unlock();
        }
    }
}
