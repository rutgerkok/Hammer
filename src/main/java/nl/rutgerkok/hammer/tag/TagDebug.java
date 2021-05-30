package nl.rutgerkok.hammer.tag;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONObject;

import com.google.common.base.Preconditions;

/**
 * Class for getting debug information from tags.
 *
 * @see CompoundTag#toDebugString()
 */
final class TagDebug {

    /**
     * Gets a string describing all information from the compound tag. No
     * guarantees are made about the format of the tag.
     *
     * @param tag
     *            The tag, may not be null.
     * @return The string.
     */
    static final String toDebugString(CompoundTag tag) {
        Preconditions.checkNotNull(tag, "tag");
        return JSONObject.toJSONString(toTypedMap(tag));
    }

    private static List<Object> toTypedList(ListTag<?> tag) {
        List<Object> typedList = new ArrayList<>();
        if (tag.isEmpty()) {
            typedList.add("^=== empty list of " + tag.getListType().toString().toLowerCase(Locale.ROOT) + "");
        }
        for (Object entry : tag) {
            typedList.add(toTypedValue(entry));
        }
        return typedList;
    }

    private static Map<String, Object> toTypedMap(CompoundTag tag) {
        Map<String, Object> typedMap = new LinkedHashMap<>();
        for (Entry<CompoundKey<?>, Object> entry : tag.entrySet()) {
            String keyName = entry.getKey().getKeyName();
            Object value = entry.getValue();

            typedMap.put(keyName, toTypedValue(value));
        }
        return typedMap;
    }

    private static Object toTypedValue(Object value) {
        if (value instanceof CompoundTag) {
            return toTypedMap((CompoundTag) value);
        }
        if (value instanceof ListTag) {
            return toTypedList((ListTag<?>) value);
        }
        if (value instanceof Byte) {
            return "byte(" + value + ")";
        }
        if (value instanceof Short) {
            return "short(" + value + ")";
        }
        if (value instanceof Integer) {
            return "int(" + value + ")";
        }
        if (value instanceof Long) {
            return "long(" + value + ")";
        }
        if (value instanceof Float) {
            return "float(" + value + ")";
        }
        if (value instanceof Double) {
            return "double(" + value + ")";
        }
        if (value instanceof String) {
            return "str('" + value + "')";
        }
        if (value instanceof byte[]) {
            return List.of("^=== array of byte (length=" + ((byte[]) value).length + ")");
        }
        if (value instanceof int[]) {
            return List.of("^=== array of int (length=" + ((int[]) value).length + ")");
        }
        if (value instanceof long[]) {
            return List.of("^=== array of long (length=" + ((long[]) value).length + ")");
        }
        return value;
    }
}
