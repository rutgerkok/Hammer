package nl.rutgerkok.hammer.pocket.tag;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map.Entry;

import nl.rutgerkok.hammer.tag.CompoundKey;
import nl.rutgerkok.hammer.tag.CompoundTag;
import nl.rutgerkok.hammer.tag.ListTag;
import nl.rutgerkok.hammer.tag.TagType;

/**
 * Contains methods to write NBT tags in the PC level format.
 *
 */
public class PocketNbtWriter {

    private static void writeCompound(OutputStream outputStream, CompoundTag tag) throws IOException {
        for (Entry<CompoundKey, Object> entry : tag.entrySet()) {
            TagType<?> tagType = TagType.ofObject(entry.getValue());
            tagType.write(outputStream);
            LittleEndian.writeUTF(outputStream, entry.getKey().getKeyName());
            writePayload(outputStream, tagType, entry.getValue());
        }
        // Close tag with a null byte
        LittleEndian.writeByte(outputStream, (byte) 0);
    }

    /**
     * Writes a compound tag to a file.
     *
     * @param path
     *            The file to write to. Will be created if it doesn't exist yet.
     * @param tag
     *            The tag to write.
     * @throws IOException
     *             If an IO error occurs.
     */
    public static void writeUncompressedToFile(Path path, CompoundTag tag) throws IOException {
        if (!Files.exists(path)) {
            Files.createDirectories(path.getParent());
            try {
                Files.createFile(path);
            } catch (FileAlreadyExistsException e) {
                // Cannot create file atomically, so this error is possible
            }
        }
        try (DataOutputStream dataOutput = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(path)))) {
            writeUncompressedToStream(dataOutput, tag);
        }
    }

    private static void writeList(OutputStream outputStream, ListTag<?> list) throws IOException {
        TagType<?> listType = list.getListType();
        listType.write(outputStream);
        LittleEndian.writeInt(outputStream, list.size());
        for (Object value : list) {
            writePayload(outputStream, listType, value);
        }
    }

    private static <T> void writePayload(OutputStream outputStream, TagType<? extends T> tagType, T value) throws IOException {
        if (tagType == TagType.BYTE) {
            LittleEndian.writeByte(outputStream, (Byte) value);
        } else if (tagType == TagType.COMPOUND) {
            writeCompound(outputStream, (CompoundTag) value);
        } else if (tagType == TagType.DOUBLE) {
            LittleEndian.writeDouble(outputStream, (Double) value);
        } else if (tagType == TagType.FLOAT) {
            LittleEndian.writeFloat(outputStream, (Float) value);
        } else if (tagType == TagType.INT) {
            LittleEndian.writeInt(outputStream, (Integer) value);
        } else if (tagType == TagType.INT_ARRAY) {
            int[] intArray = (int[]) value;
            LittleEndian.writeInt(outputStream, intArray.length);
            for (int integer : intArray) {
                LittleEndian.writeInt(outputStream, integer);
            }
        } else if (tagType == TagType.LIST) {
            writeList(outputStream, (ListTag<?>) value);
        } else if (tagType == TagType.LONG) {
            LittleEndian.writeLong(outputStream, (Long) value);
        } else if (tagType == TagType.BYTE_ARRAY) {
            byte[] byteArray = (byte[]) value;
            LittleEndian.writeInt(outputStream, byteArray.length);
            outputStream.write(byteArray);
        } else if (tagType == TagType.SHORT) {
            LittleEndian.writeShort(outputStream, (Short) value);
        } else if (tagType == TagType.STRING) {
            LittleEndian.writeUTF(outputStream, (String) value);
        } else {
            throw new IOException("Unknown tag type: " + tagType);
        }
    }

    /**
     * Writes the compound tag with its header to the given stream, without
     * extra compression.
     *
     * @param outputStream
     *            Stream to write to.
     * @param tag
     *            Tag to write.
     * @throws IOException
     *             If writing fails.
     */
    public static void writeUncompressedToStream(OutputStream outputStream, CompoundTag tag) throws IOException {
        LittleEndian.writeInt(outputStream, PocketTagFormat.VERSION);

        // Write tag to memory buffer, so that length can be captured
        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        TagType.COMPOUND.write(byteArrayStream);
        LittleEndian.writeUTF(byteArrayStream, ""); // Required for NBT format
        writeCompound(byteArrayStream, tag);

        // Write tag to stream
        byte[] bytes = byteArrayStream.toByteArray();
        LittleEndian.writeInt(outputStream, bytes.length);
        outputStream.write(bytes);
    }
}
