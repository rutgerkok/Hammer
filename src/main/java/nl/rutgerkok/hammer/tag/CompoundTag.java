package nl.rutgerkok.hammer.tag;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Set;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import com.google.common.collect.ImmutableMap;

/**
 * A map that holds tags any supported {@link TagType tag type}. Maps can, and
 * almost always do, hold tags of different types together in one map.
 *
 * <p> Tags are indexed by a name. Names are always case insensitive. If an
 * non-existing tag is requested a new tag is created automatically.
 *
 * <p> If a requested tag is of the wrong type, it is overwritten with a new tag
 * of the correct type. All numeric types (long, int, short, byte, double,
 * float) are considered equivalent and numbers are automatically casted to the
 * requested type. On the other hand, lists and arrays of numbers do verify
 * their types.
 *
 * <p> Mutable objects returned by a compound tag (subtags, lists, arrays) will
 * have their changes written through to this tag.
 *
 * <p> Tags are not thread safe, and must only be read/modified by one thread at
 * the same time.
 */
public final class CompoundTag implements JSONAware {

    /**
     * Creates a deep copy of the object. The object must be an instance of the
     * valid tag types. CompoundTags, ListTags, byte arrays and int arrays are
     * the tags that are deeply copied. All other objects are assumed immutable
     * and returned without modification.
     *
     * @param value
     *            The object.
     * @return The deep copy.
     */
    static Object deepCopy(Object value) {
        if (value instanceof CompoundTag) {
            return ((CompoundTag) value).copy();
        }
        if (value instanceof ListTag) {
            return ((ListTag<?>) value).copy();
        }
        if (value instanceof byte[]) {
            return Arrays.copyOf((byte[]) value, ((byte[]) value).length);
        }
        if (value instanceof int[]) {
            return Arrays.copyOf((int[]) value, ((int[]) value).length);
        }
        // Assume value is immutable
        return value;
    }

    private Map<CompoundKey<?>, Object> map = new HashMap<>();

    public CompoundTag() {

    }

    public CompoundTag(CompoundTag copy) {
        for (Entry<CompoundKey<?>, Object> entry : copy.entrySet()) {
            Object value = deepCopy(entry.getValue());
            map.put(entry.getKey(), value);
        }
    }

    /**
     * Adds all keys and values from the other tag to this tag. If an entry
     * already exists with the same name, it is overwritten.
     *
     * @param otherTag
     *            The other tag.
     */
    public void addAll(CompoundTag otherTag) {
        this.map.putAll(otherTag.map);
    }

    /**
     * Clears this tag, removing all keys and values.
     */
    public void clear() {
        map.clear();
    }

    /**
     * Checks if a child tag with the given name exists.
     *
     * @param key
     *            Name of the tag, case insensitive.
     * @return True if such a tag exists, false otherwise.
     */
    public boolean containsKey(CompoundKey<?> key) {
        return map.containsKey(key);
    }

    /**
     * Creates a deep copy of this tag. Modifications to the copy have no
     * influence to the original, and vice versa. This means that copies can be
     * used safely in another thread.
     *
     * @return A copy.
     */
    public CompoundTag copy() {
        return new CompoundTag(this);
    }

    /**
     * Gets an immutable map of all entries of the given type.
     *
     * @param ofType
     *            The type.
     * @return The entries.
     */
    public <T> Map<CompoundKey<T>, T> entries(TagType<T> ofType) {
        ImmutableMap.Builder<CompoundKey<T>, T> entries = ImmutableMap.builder();
        for (Entry<CompoundKey<?>, Object> entry : this.map.entrySet()) {
            Object value = entry.getValue();
            if (ofType.isOfType(value)) {
                @SuppressWarnings("unchecked")
                CompoundKey<T> key = (CompoundKey<T>) entry.getKey();
                entries.put(key, ofType.cast(entry.getValue()));
            }
        }
        return entries.build();
    }

    /**
     * Gets all entries of this compound tag. Entry set will be immutable.
     *
     * @return All entries.
     */
    public Set<Entry<CompoundKey<?>, Object>> entrySet() {
        return Collections.unmodifiableSet(map.entrySet());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!(other instanceof CompoundTag)) {
            return false;
        }
        CompoundTag tag = (CompoundTag) other;
        if (tag.size() != map.size()) {
            return false;
        }
        for (Entry<CompoundKey<?>, Object> entry : tag.entrySet()) {
            if (!valueEquals(entry.getValue(), map.get(entry.getKey()))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the boolean with the given tag name. Booleans are saved as byte tags
     * internally. This method returns true if {@code getByte(name) != 0}.
     * Otherwise, this method returns false.
     *
     * @param key
     *            Name of the tag, case insensitive.
     * @return The boolean, or false if not found.
     */
    public boolean getBoolean(CompoundKey<Boolean> key) {
        // Sneaky conversion to byte key
        @SuppressWarnings({ "unchecked", "rawtypes" })
        CompoundKey<Byte> byteKey = (CompoundKey) key;

        return getByte(byteKey) != 0;
    }

    /**
     * Gets the byte with the given tag name. If the given tag does not exist or
     * is not a number, 0 is returned. If the tag does exist and is of another
     * numeric type, the number is casted to a byte.
     *
     * @param key
     *            Name of the tag, case insensitive.
     * @return The byte, or 0 if not found.
     */
    public byte getByte(CompoundKey<Byte> key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).byteValue();
        }
        return 0;
    }

    /**
     * Gets the byte array with the given tag name. If the given tag does not
     * exist, is not a byte array or has an incorrect length, an empty array of
     * the given length is returned. Changes to the array will write through to
     * this compound tag.
     *
     * @param key
     *            Name of the tag, case insensitive.
     * @param length
     *            Length of the byte array.
     * @return The byte array.
     */
    public byte[] getByteArray(CompoundKey<byte[]> key, int length) {
        Object value = map.get(key);
        if (value instanceof byte[]) {
            byte[] array = (byte[]) value;
            if (array.length == length) {
                return array;
            }
        }

        byte[] array = new byte[length];
        map.put(key, array);
        return array;
    }

    /**
     * Gets the compound tag with the given tag name. If the given tag does not
     * exist or is not a compound tag, an empty compound tag is returned.
     * Changes to the subtag will write through to this compound tag.
     *
     * @param key
     *            Name of the tag, case insensitive.
     * @return The compound tag.
     */
    public CompoundTag getCompound(CompoundKey<CompoundTag> key) {
        Object value = map.get(key);
        if (value instanceof CompoundTag) {
            return (CompoundTag) value;
        }

        // Put compound in the map so that changes to it will be reflected
        CompoundTag tag = new CompoundTag();
        map.put(key, tag);
        return tag;
    }

    /**
     * Gets the double with the given tag name. If the given tag does not exist
     * or is not a number, 0 is returned. If the tag does exist and is of
     * another numeric type, the number is casted to a double.
     *
     * @param key
     *            Name of the tag, case insensitive.
     * @return The double, or 0.0 if not found.
     */
    public double getDouble(CompoundKey<Double> key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0;
    }

    /**
     * Gets the float with the given tag name. If the given tag does not exist
     * or is not a number, 0.0 is returned. If the tag does exist and is of
     * another numeric type, the number is casted to a float.
     *
     * @param key
     *            Name of the tag, case insensitive.
     * @return The float, or 0.0 if not found.
     */
    public float getFloat(CompoundKey<Float> key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        return 0;
    }

    /**
     * Gets the integer with the given tag name. If the given tag does not exist
     * or is not a number, 0 is returned. If the tag does exist and is of
     * another numeric type, the number is casted to an integer.
     *
     * @param key
     *            Name of the tag, case insensitive.
     * @return The integer, or 0 if not found.
     */
    public int getInt(CompoundKey<Integer> key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }

    /**
     * Gets the integer array with the given tag name. If the given tag does not
     * exist, is not an integer array or has an incorrect length, an empty array
     * of the given length is returned. Changes to the array will write through
     * to this compound tag.
     *
     * @param key
     *            Name of the tag, case insensitive.
     * @param length
     *            Length of the integer array. If this is absent, but an int
     *            array of any length exists, that array is returned. Otherwise,
     *            a zero-sized array is returned.
     * @return The integer array.
     */
    public int[] getIntArray(CompoundKey<int[]> key, OptionalInt length) {
        Object value = map.get(key);
        if (value instanceof int[]) {
            int[] array = (int[]) value;
            if (!length.isPresent() || array.length == length.getAsInt()) {
                return array;
            }
        }

        int[] array = new int[length.getAsInt()];
        map.put(key, array);
        return array;
    }

    /**
     * Gets the list of the given type. If the given tag does not exist or is
     * not a list of the given type, a new list is created. Changes to the list
     * will write through to this compound tag.
     *
     * @param key
     *            Name of the tag, case insensitive.
     * @param type
     *            Type of the list.
     * @return The list.
     */
    @SuppressWarnings("unchecked")
    public <T> ListTag<T> getList(CompoundKey<ListTag<T>> key, TagType<T> type) {
        Object value = map.get(key);
        if (value instanceof ListTag) {
            ListTag<?> listTag = (ListTag<?>) value;
            if (listTag.getListType().equals(type)) {
                return (ListTag<T>) listTag;
            }
        }

        // Put list in map so that changes to it are reflected in this map
        ListTag<T> list = new ListTag<T>(type);
        map.put(key, list);
        return list;
    }

    /**
     * Gets the long with the given tag name. If the given tag does not exist or
     * is not a number, 0 is returned. If the tag does exist and is of another
     * numeric type, the number is casted to a long.
     *
     * @param key
     *            Name of the tag, case insensitive.
     * @return The long, or 0 if not found.
     */
    public long getLong(CompoundKey<Long> key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0;
    }

    /**
     * Gets the long array with the given tag name. If the given tag does not
     * exist, is not a long array or has an incorrect length, an empty array of
     * the given length is returned. Changes to the array will write through to
     * this compound tag.
     *
     * @param key
     *            Name of the tag, case insensitive.
     * @param length
     *            Length of the long array. If this is absent, but a long array
     *            of any length exists, that array is returned. Otherwise, a
     *            zero-sized array is returned.
     * @return The long array.
     */
    public long[] getLongArray(CompoundKey<long[]> key, OptionalInt length) {
        Object value = map.get(key);
        if (value instanceof long[]) {
            long[] array = (long[]) value;
            if (!length.isPresent() || array.length == length.getAsInt()) {
                return array;
            }
        }

        long[] array = new long[length.getAsInt()];
        map.put(key, array);
        return array;
    }

    /**
     * Gets the short with the given tag name. If the given tag does not exist
     * or is not a number, 0 is returned. If the tag does exist and is of
     * another numeric type, the number is casted to a short.
     *
     * @param key
     *            Name of the tag, case insensitive.
     * @return The integer, or 0 if not found.
     */
    public short getShort(CompoundKey<Short> key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).shortValue();
        }
        return 0;
    }

    /**
     * Gets the string of the given type. If the given tag does not exist or is
     * not a string, an empty string is returned.
     *
     * @param key
     *            Name of the tag, case insensitive.
     * @return The string.
     */
    public String getString(CompoundKey<String> key) {
        Object value = map.get(key);
        if (value instanceof String) {
            return (String) value;
        }
        return "";
    }

    @Override
    public int hashCode() {
        // Values have complex rules to be considered equal, so we leave them
        // out in the hash code
        return map.keySet().hashCode();
    }

    /**
     * Gets whether this tag is empty.
     *
     * @return True if this tag is empty, false otherwise.
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }


    /**
     * Gets whether the value with the given key is of the given type.
     *
     * @param <T>
     *            Expected type.
     * @param key
     *            Key of the tag.
     * @param tagType
     *            Type of the tag.
     *
     * @return True if the tag is of the given type, false otherwise.
     */
    public <T> boolean isType(CompoundKey<T> key, TagType<T> tagType) {
        Object value = this.map.get(key);
        if (value == null) {
            return false;
        }
        return tagType.isOfType(value);
    }

    /**
     * Sets a tag.
     *
     * @param key
     *            The name of the tag.
     * @param type
     *            The type of the tag.
     * @param value
     *            Value of the tag. Must be of the given type, may not be null.
     */
    public <T> void set(CompoundKey<T> key, TagType<T> type, T value) {
        value = type.cast(Objects.requireNonNull(value));
        map.put(key, value);
    }

    /**
     * Sets the a tag with the given value. This will override the tag (if any)
     * with the same name (case insensitive).
     *
     * <p> Booleans are saved as byte tags. Calling this method is equivalent to
     * calling {@code setByte(name, value? 1 : 0)}.
     *
     * @param key
     *            Name of the tag.
     * @param value
     *            Value of the tag.
     */
    public void setBoolean(CompoundKey<Boolean> key, boolean value) {
        // Sneaky conversion
        @SuppressWarnings({ "unchecked", "rawtypes" })
        CompoundKey<Byte> byteKey = (CompoundKey) key;

        setByte(byteKey, (byte) (value ? 1 : 0));
    }

    /**
     * Sets a byte tag with the given value. This will override the tag (if any)
     * with the same name (case insensitive).
     *
     * @param key
     *            Name of the tag.
     * @param value
     *            Value of the tag.
     */
    public void setByte(CompoundKey<Byte> key, byte value) {
        map.put(key, value);
    }

    /**
     * Sets a byte array tag with the given value. This will override the tag
     * (if any) with the same name (case insensitive).
     *
     * @param key
     *            Name of the tag.
     * @param value
     *            Value of the tag.
     */
    public void setByteArray(CompoundKey<byte[]> key, byte[] value) {
        map.put(key, Objects.requireNonNull(value));
    }

    /**
     * Sets a compound tag with the given value. This will override the tag (if
     * any) with the same name (case insensitive).
     *
     * @param key
     *            Name of the tag.
     * @param value
     *            Value of the tag.
     */
    public void setCompound(CompoundKey<CompoundTag> key, CompoundTag value) {
        map.put(key, Objects.requireNonNull(value));
    }

    /**
     * Sets the double tag with the given value. This will override the tag (if
     * any) with the same name (case insensitive).
     *
     * @param key
     *            Name of the tag.
     * @param value
     *            Value of the tag.
     */
    public void setDouble(CompoundKey<Double> key, double value) {
        map.put(key, value);
    }

    /**
     * Sets the float tag with the given value. This will override the tag (if
     * any) with the same name (case insensitive).
     *
     * @param key
     *            Name of the tag.
     * @param value
     *            Value of the tag.
     */
    public void setFloat(CompoundKey<Float> key, float value) {
        map.put(key, value);
    }

    /**
     * Sets the int tag with the given value. This will override the tag (if
     * any) with the same name (case insensitive).
     *
     * @param key
     *            Name of the tag.
     * @param value
     *            Value of the tag.
     */
    public void setInt(CompoundKey<Integer> key, int value) {
        map.put(key, value);
    }

    /**
     * Sets the int array tag with the given value. This will override the tag
     * (if any) with the same name (case insensitive).
     *
     * @param key
     *            Name of the tag.
     * @param value
     *            Value of the tag.
     */
    public void setIntArray(CompoundKey<int[]> key, int[] value) {
        map.put(key, Objects.requireNonNull(value));
    }

    /**
     * Sets the list tag with the given value. This will override the tag (if
     * any) with the same name (case insensitive).
     *
     * @param name
     *            Name of the tag.
     * @param value
     *            Value of the tag.
     */
    public void setList(CompoundKey<ListTag<?>> name, ListTag<?> value) {
        map.put(name, value);
    }

    /**
     * Sets the long tag with the given value. This will override the tag (if
     * any) with the same name (case insensitive).
     *
     * @param key
     *            Name of the tag.
     * @param value
     *            Value of the tag.
     */
    public void setLong(CompoundKey<Long> key, long value) {
        map.put(key, value);
    }

    /**
     * Sets the short tag with the given value. This will override the tag (if
     * any) with the same name (case insensitive).
     *
     * @param key
     *            Name of the tag.
     * @param value
     *            Value of the tag.
     */
    public void setShort(CompoundKey<Short> key, short value) {
        map.put(key, value);
    }

    /**
     * Sets the string tag with the given value. This will override the tag (if
     * any) with the same name (case insensitive).
     *
     * @param key
     *            Name of the tag.
     * @param value
     *            Value of the tag.
     */
    public void setString(CompoundKey<String> key, String value) {
        map.put(key, Objects.requireNonNull(value));
    }

    /**
     * Gets the amount of entries in this tag.
     *
     * @return The amount of entries.
     */
    public int size() {
        return map.size();
    }

    /**
     * Gets a string describing all information from the compound tag. No
     * guarantees are made about the format of the tag.
     *
     * @return The string.
     */
    public String toDebugString() {
        return TagDebug.toDebugString(this);
    }

    /**
     * Writes this object as a JSON string. int[] and byte[] tags will not be
     * serialized correctly because the JSON representation of those would be
     * really inefficient.
     *
     * @return JSON text
     */
    @Override
    public String toJSONString() {
        return JSONObject.toJSONString(map);
    }

    @Override
    public String toString() {
        return toDebugString();
    }

    private boolean valueEquals(Object obj1, Object obj2) {
        if (obj1 == null) {
            return obj2 == null;
        }
        if (obj2 == null) {
            return false;
        }
        if (obj1 == obj2) {
            return true;
        }
        if (obj1 instanceof byte[]) {
            if (obj2 instanceof byte[]) {
                return Arrays.equals((byte[]) obj1, (byte[]) obj2);
            }
            return false;
        }
        if (obj1 instanceof int[]) {
            if (obj2 instanceof int[]) {
                return Arrays.equals((int[]) obj1, (int[]) obj2);
            }
            return false;
        }
        if (obj1 instanceof Number) {
            if (obj2 instanceof Number) {
                double num1 = ((Number) obj1).doubleValue();
                double num2 = ((Number) obj2).doubleValue();
                return Math.abs(num1 - num2) < 0.001;
            }
            return false;
        }
        return obj1.equals(obj2);
    }
}
