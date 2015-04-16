package nl.rutgerkok.hammer.anvil.tag;

import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

import nl.rutgerkok.hammer.tag.CompoundKey;
import nl.rutgerkok.hammer.tag.CompoundTag;
import nl.rutgerkok.hammer.tag.ListTag;
import nl.rutgerkok.hammer.tag.TagType;

/**
 * Contains methods to read NBT streams in the PC level format.
 *
 */
public final class AnvilNbtReader {

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
    static final CompoundTag readCompoundTag(DataInput input) throws IOException {
        CompoundTag tag = new CompoundTag();
        while (true) {
            byte marker = input.readByte();
            if (marker == 0) {
                // End of tag
                return tag;
            }

            @SuppressWarnings("unchecked")
            TagType<Object> type = (TagType<Object>) TagType.fromByte(marker);
            String tagName = input.readUTF();
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
    public static CompoundTag readFromCompressedFile(Path path) throws IOException {
        try (DataInputStream dataInput = new DataInputStream(new BufferedInputStream(new GZIPInputStream(Files.newInputStream(path))))) {
            return readFromUncompressedStream(dataInput);
        }
    }

    /**
     * Reads the tag from the uncompressed stream.
     *
     * @param dataInput
     *            Stream to read from.
     * @return The stream.
     * @throws IOException
     *             If an IO error occurs.
     */
    public static CompoundTag readFromUncompressedStream(DataInput dataInput) throws IOException {
        // Verify that we have a compound tag
        byte tagMarker = dataInput.readByte();
        if (TagType.fromByte(tagMarker) != TagType.COMPOUND) {
            throw new IOException("Root tag must be a compound tag, found byte " + (tagMarker & 0xff) + " instead");
        }
        // Skip name
        dataInput.readUTF();
        // Read the rest
        return readCompoundTag(dataInput);
    }

    static ListTag<?> readListTag(DataInput stream) throws IOException {
        byte typeByte = stream.readByte();
        int size = stream.readInt();

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
            tag.add(AnvilNbtReader.readPayload(stream, tag.getListType()));
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
    static <T> T readPayload(DataInput dataInput, TagType<T> type) throws IOException {
        if (type == TagType.BYTE) {
            return type.cast(dataInput.readByte());
        }
        if (type == TagType.COMPOUND) {
            return type.cast(readCompoundTag(dataInput));
        }
        if (type == TagType.DOUBLE) {
            return type.cast(dataInput.readDouble());
        }
        if (type == TagType.FLOAT) {
            return type.cast(dataInput.readFloat());
        }
        if (type == TagType.INT) {
            return type.cast(dataInput.readInt());
        }
        if (type == TagType.INT_ARRAY) {
            int size = dataInput.readInt();
            if (size < 0 || size > MAX_ARRAY_SIZE) {
                throw new IOException("Invalid int array size: " + size);
            }
            int[] array = new int[size];
            for (int i = 0; i < size; i++) {
                array[i] = dataInput.readInt();
            }
            return type.cast(array);
        }
        if (type == TagType.LIST) {
            return type.cast(readListTag(dataInput));
        }
        if (type == TagType.LONG) {
            return type.cast(dataInput.readLong());
        }
        if (type == TagType.BYTE_ARRAY) {
            int blobSize = dataInput.readInt();
            byte[] result = new byte[blobSize];
            dataInput.readFully(result);
            return type.cast(result);
        }
        if (type == TagType.SHORT) {
            return type.cast(dataInput.readShort());
        }
        if (type == TagType.STRING) {
            return type.cast(dataInput.readUTF());
        }
        throw new IOException("Unknown type: " + type);
    }

}
