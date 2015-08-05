package nl.rutgerkok.hammer.material;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Specialized set for materials. It is faster than a general HashSet, as it
 * keeps a BitSet with all material ids. It therefore also consumes more memory.
 *
 */
public final class MaterialSet extends HashSet<MaterialData> {

    /**
     * Default serial version id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Iterator that keeps the BitSet in sync.
     *
     */
    private class MaterialIterator implements Iterator<MaterialData> {
        private MaterialData current;
        private Iterator<MaterialData> parent;

        MaterialIterator(Iterator<MaterialData> parent) {
            this.parent = parent;
        }

        @Override
        public final boolean hasNext() {
            return parent.hasNext();
        }

        @Override
        public MaterialData next() {
            current = parent.next();
            return current;
        }

        @Override
        public void remove() {
            parent.remove();
            bitSet.clear(current.getId());
        }
    }

    private final BitSet bitSet = new BitSet();

    @Override
    public boolean add(MaterialData material) {
        char id = material.getId();
        if (!bitSet.get(id)) {
            bitSet.set(id);
            super.add(material);
            return true;
        }
        return false;
    }

    @Override
    public boolean contains(Object object) {
        if (object instanceof MaterialData) {
            return bitSet.get(((MaterialData) object).getId());
        }
        return false;
    }

    @Override
    public Iterator<MaterialData> iterator() {
        return new MaterialIterator(super.iterator());
    }

    @Override
    public boolean remove(Object object) {
        if (object instanceof MaterialData) {
            int id = ((MaterialData) object).getId();
            if (bitSet.get(id)) {
                bitSet.clear(id);
                super.remove(object);
                return true;
            }
        }
        return false;
    }

}
