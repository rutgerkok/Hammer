package nl.rutgerkok.hammer.material;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Specialized set for materials. It is faster than a general HashSet, as it
 * keeps a BitSet with all material ids.
 *
 */
public final class MaterialSet extends HashSet<Material> {

    /**
     * Default serial version id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Iterator that keeps the BitSet in sync.
     *
     */
    private class MaterialIterator implements Iterator<Material> {
        private Material current;
        private Iterator<Material> parent;

        MaterialIterator(Iterator<Material> parent) {
            this.parent = parent;
        }

        @Override
        public final boolean hasNext() {
            return parent.hasNext();
        }

        @Override
        public Material next() {
            current = parent.next();
            return current;
        }

        @Override
        public void remove() {
            parent.remove();
            MaterialSet.this.remove(current);
        }
    }

    private final BitSet bitSet = new BitSet();

    @Override
    public boolean add(Material material) {
        short id = material.getId();
        if (!bitSet.get(id)) {
            bitSet.set(id);
            super.add(material);
            return true;
        }
        return false;
    }

    @Override
    public boolean contains(Object object) {
        if (object instanceof Material) {
            return bitSet.get(((Material) object).getId());
        }
        return false;
    }

    /**
     * Gets whether the set contains the material with the given id.
     *
     * @param materialId
     *            The material id.
     * @return True if the set contains a material with the given id, false
     *         otherwise.
     */
    public boolean containsId(short materialId) {
        return bitSet.get(materialId);
    }

    @Override
    public Iterator<Material> iterator() {
        return new MaterialIterator(super.iterator());
    }

    @Override
    public boolean remove(Object object) {
        if (object instanceof Material) {
            int id = ((Material) object).getId();
            if (bitSet.get(id)) {
                bitSet.clear(id);
                super.remove(object);
                return true;
            }
        }
        return false;
    }

}
