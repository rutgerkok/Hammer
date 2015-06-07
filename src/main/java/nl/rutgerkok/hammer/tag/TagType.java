package nl.rutgerkok.hammer.tag;

import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;

import com.google.common.base.CaseFormat;

/**
 * Constants of the various tag types supported by {@link CompoundTag} and
 * {@link ListTag}.
 *
 * @param <T>
 *            Type of the value of the tag.
 *
 */
public final class TagType<T> {
    private static TagType<?>[] ALL_BY_ID = new TagType[12];

    public static final TagType<Byte> BYTE = register(1, Byte.class);
    public static final TagType<byte[]> BYTE_ARRAY = register(7, byte[].class);
    public static final TagType<CompoundTag> COMPOUND = register(10, CompoundTag.class);
    public static final TagType<Double> DOUBLE = register(6, Double.class);
    public static final TagType<Float> FLOAT = register(5, Float.class);
    public static final TagType<Integer> INT = register(3, Integer.class);
    public static final TagType<int[]> INT_ARRAY = register(11, int[].class);
    @SuppressWarnings("rawtypes")
    public static final TagType<ListTag> LIST = register(9, ListTag.class);
    public static final TagType<Long> LONG = register(4, Long.class);
    public static final TagType<Short> SHORT = register(2, Short.class);
    public static final TagType<String> STRING = register(8, String.class);

    /**
     * Reads the marker byte from the stream and returns the corresponding tag
     * type.
     *
     * @param marker
     *            The marker read from the stream.
     * @return The tag type.
     * @throws IOException
     *             If the stream could not be read or if the byte on the stream
     *             is invalid.
     */
    public static TagType<?> fromByte(byte marker) throws IOException {
        if (marker < 0 || marker >= ALL_BY_ID.length) {
            throw new IOException("Marker out of bounds; marker = " + marker + ", length = " + ALL_BY_ID.length);
        }

        TagType<?> type = ALL_BY_ID[marker];
        if (type == null) {
            throw new IOException("Invalid marker: " + marker);
        }
        return type;
    }

    /**
     * Gets the tag type that holds the given class.
     *
     * @param clazz
     *            The class.
     * @return The tag type.
     * @throws IllegalArgumentException
     *             If there is no class that holds the given tag type.
     */
    public static <T> TagType<T> ofClass(Class<T> clazz) throws IllegalArgumentException {
        for (TagType<?> type : ALL_BY_ID) {
            if (type == null) {
                continue;
            }
            if (type.clazz.isAssignableFrom(clazz)) {
                @SuppressWarnings("unchecked")
                // Checked above
                TagType<T> returnType = (TagType<T>) type;
                return returnType;
            }
        }
        throw new IllegalArgumentException("Invalid class: " + clazz);
    }

    /**
     * Gets the tag type for the given object. Markers are used to mark the type
     * of the object(s) that will come next on the stream. Returned values can
     * be compared to the constants in the {@link TagType} class.
     *
     * @param <T>
     *
     * @param value
     *            The given object.
     * @return The marker type.
     * @throws ClassCastException
     *             If the object is not of a serializable type.
     */
    public static <T> TagType<T> ofObject(T value) throws ClassCastException {
        Class<?> clazz = value.getClass();
        @SuppressWarnings("unchecked")
        TagType<T> type = (TagType<T>) ofClass(clazz);
        return type;
    }

    private static <T> TagType<T> register(int id, Class<T> type) {
        TagType<T> tagType = new TagType<>(id, type);
        ALL_BY_ID[tagType.marker] = tagType;
        return tagType;
    }

    private final Class<T> clazz;
    private final byte marker;

    private TagType(int markerByte, Class<T> clazz) {
        this.marker = (byte) markerByte;
        this.clazz = clazz;
    }

    /**
     * Casts the given object to the {@link #getValueType() class of the values}
     * of this tag type.
     *
     * @param value
     *            The value to cast.
     * @return The casted value.
     * @throws ClassCastException
     *             If the given object is of another type than the
     *             {@link #getValueType()} of this tag type.
     */
    public T cast(Object value) {
        return clazz.cast(value);
    }

    /**
     * Gets the type that values of this tag must have.
     *
     * @return The type.
     */
    public Class<T> getValueType() {
        return clazz;
    }

    @Override
    public String toString() {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, clazz.getSimpleName());
    }

    /**
     * Writes the marker of this type to the given stream.
     *
     * @param dataOutput
     *            Stream to write to.
     * @throws IOException
     *             If the marker could not be written.
     */
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeByte(marker);
    }

    /**
     * Writes the marker of this type to the given stream.
     *
     * @param outputStream
     *            Stream to write to.
     * @throws IOException
     *             If the marker could not be written.
     */
    public void write(OutputStream outputStream) throws IOException {
        outputStream.write(marker & 0xff);
    }
}