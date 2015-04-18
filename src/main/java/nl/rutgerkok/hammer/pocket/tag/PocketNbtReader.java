package nl.rutgerkok.hammer.pocket.tag;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import nl.rutgerkok.hammer.tag.CompoundKey;
import nl.rutgerkok.hammer.tag.CompoundTag;
import nl.rutgerkok.hammer.tag.ListTag;
import nl.rutgerkok.hammer.tag.TagType;

/**
 * Contains methods to read NBT streams in the MCPE level format.
 *
 */
public final class PocketNbtReader {

    private static final int MAX_ARRAY_SIZE = 100000;

    /**
     * Reads a compound tag from the given stream.
     *
     * @param input
     *            The stream to read.
     * @return The compound tag.
     * @throws IOException
     *             If an IO error occurs.
     */
    private static final CompoundTag readCompoundTag(InputStream input) throws IOException {
        CompoundTag tag = new CompoundTag();
        while (true) {
            byte marker = LittleEndian.readByte(input);
            if (marker == 0) {
                // End of tag
                return tag;
            }

            @SuppressWarnings("unchecked")
            TagType<Object> type = (TagType<Object>) TagType.fromByte(marker);
            String tagName = LittleEndian.readUTF(input);
            tag.set(CompoundKey.of(tagName), type, readPayload(input, type));
        }
    }

    /**
     * Reads a compound tag from the given file.
     *
     * @param path
     *            The file to read from.
     * @return The tag.
     * @throws IOException
     *             If the file format is invalid.
     */
    public static CompoundTag readFromUncompressedFile(Path path) throws IOException {
        try (InputStream inputStream = new BufferedInputStream(Files.newInputStream(path))) {
            // Read version
            int version = LittleEndian.readInt(inputStream);
            if (version > PocketTagFormat.VERSION) {
                throw new IOException("Found NBT version of " + version + ", but highest supported is " + PocketTagFormat.VERSION);
            }

            // Read length
            LittleEndian.readInt(inputStream);

            return readFromUncompressedStream(inputStream);
        }
    }

    /**
     * Reads the tag from the uncompressed stream.
     *
     * @param inputStream
     *            Stream to read from.
     * @return The stream.
     * @throws IOException
     *             If an IO error occurs.
     */
    public static CompoundTag readFromUncompressedStream(InputStream inputStream) throws IOException {
        // Verify that we have a compound tag
        byte tagMarker = LittleEndian.readByte(inputStream);
        if (TagType.fromByte(tagMarker) != TagType.COMPOUND) {
            throw new IOException("Root tag must be a compound tag, found byte " + (tagMarker & 0xff) + " instead");
        }
        // Skip name
        LittleEndian.readUTF(inputStream);
        // Read the rest
        return readCompoundTag(inputStream);
    }

    private static ListTag<?> readListTag(InputStream dataInput) throws IOException {
        byte typeByte = LittleEndian.readByte(dataInput);
        int size = LittleEndian.readInt(dataInput);

        TagType<?> tagType;
        if (typeByte == 0) {
            if (size == 0) {
                // Just use some random type, for lists with zero elements
                // Minecraft is not so strict about the type
                tagType = TagType.BYTE;
            } else {
                throw new IOException("List of type 0, but size was " + size);
            }
        } else {
            tagType = TagType.fromByte(typeByte);
        }

        ListTag<Object> tag = new ListTag<>(tagType);
        for (int i = 0; i < size; i++) {
            tag.add(PocketNbtReader.readPayload(dataInput, tag.getListType()));
        }
        return tag;
    }

    /**
     * Reads the payload of the given type.
     *
     * @param <T>
     *
     * @param dataInput
     *            Stream to read from.
     * @param type
     *            The tag type.
     * @return The deserialized object.
     * @throws IOException
     *             If an io error occurs.
     */
    private static <T> T readPayload(InputStream dataInput, TagType<T> type) throws IOException {
        if (type == TagType.BYTE) {
            return type.cast(LittleEndian.readByte(dataInput));
        }
        if (type == TagType.COMPOUND) {
            return type.cast(readCompoundTag(dataInput));
        }
        if (type == TagType.DOUBLE) {
            return type.cast(LittleEndian.readDouble(dataInput));
        }
        if (type == TagType.FLOAT) {
            return type.cast(LittleEndian.readFloat(dataInput));
        }
        if (type == TagType.INT) {
            return type.cast(LittleEndian.readInt(dataInput));
        }
        if (type == TagType.INT_ARRAY) {
            int size = LittleEndian.readInt(dataInput);
            if (size < 0 || size > MAX_ARRAY_SIZE) {
                throw new IOException("Invalid int array size: " + size);
            }
            int[] array = new int[size];
            for (int i = 0; i < size; i++) {
                array[i] = LittleEndian.readInt(dataInput);
            }
            return type.cast(array);
        }
        if (type == TagType.LIST) {
            return type.cast(readListTag(dataInput));
        }
        if (type == TagType.LONG) {
            return type.cast(LittleEndian.readLong(dataInput));
        }
        if (type == TagType.BYTE_ARRAY) {
            int blobSize = LittleEndian.readInt(dataInput);
            byte[] result = new byte[blobSize];
            LittleEndian.readFully(dataInput, result);
            return type.cast(result);
        }
        if (type == TagType.SHORT) {
            return type.cast(LittleEndian.readShort(dataInput));
        }
        if (type == TagType.STRING) {
            return type.cast(LittleEndian.readUTF(dataInput));
        }
        throw new IOException("Unknown type: " + type);
    }

}
