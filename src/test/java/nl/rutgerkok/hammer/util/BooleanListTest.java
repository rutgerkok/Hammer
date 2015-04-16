package nl.rutgerkok.hammer.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class BooleanListTest {

    @Test
    public void testAdd() {
        List<Boolean> booleanList = new BooleanList();
        booleanList.add(true);
        booleanList.add(false);
        booleanList.add(true);
        booleanList.addAll(Arrays.asList(false, false, false));

        assertEquals(6, booleanList.size());
    }

    @Test
    public void testDelete() {
        AbstractList<Boolean> booleanList = new BooleanList();
        booleanList.add(false);
        booleanList.add(true);
        booleanList.add(false);

        booleanList.remove(1);

        assertEquals(Arrays.asList(false, false), booleanList);
    }

    @Test
    public void testEqualsAndHashcode() {
        List<Boolean> booleanList = new BooleanList();
        booleanList.add(true);
        booleanList.add(false);
        booleanList.add(true);

        List<Boolean> arrayList = Arrays.asList(true, false, true);
        assertEquals(arrayList, booleanList);

        // Call equals directly, as assertEquals calls equals on arrayList
        // instead of booleanList
        assertTrue(booleanList.equals(arrayList));

        // Check the hash code too
        assertEquals("hashCode", booleanList.hashCode(), arrayList.hashCode());
    }

    @Test
    public void testInsert() {
        AbstractList<Boolean> booleanList = new BooleanList();
        booleanList.add(true);
        booleanList.add(true);
        booleanList.add(false);

        booleanList.add(1, false);

        assertEquals(Arrays.asList(true, false, true, false), booleanList);
    }
}
