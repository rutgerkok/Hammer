package nl.rutgerkok.hammer.anvil.tag;

import java.io.BufferedOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map.Entry;
import java.util.zip.GZIPOutputStream;

import nl.rutgerkok.hammer.tag.CompoundKey;
import nl.rutgerkok.hammer.tag.CompoundTag;
import nl.rutgerkok.hammer.tag.ListTag;
import nl.rutgerkok.hammer.tag.TagType;

/**
 * Contains methods to write NBT tags in the PC level format.
 *
 */
public class AnvilNbtWriter {

    private static void writeCompound(DataOutput dos, CompoundTag tag) throws IOException {
        for (Entry<CompoundKey, Object> entry : tag.entrySet()) {
            TagType<?> tagType = TagType.ofObject(entry.getValue());
            tagType.write(dos);
            dos.writeUTF(entry.getKey().getKeyName());
            writePayload(dos, tagType, entry.getValue());
        }
        // Close tag with a null byte
        dos.writeByte(0);
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
    public static void writeCompressedToFile(Path path, CompoundTag tag) throws IOException {
        if (!Files.exists(path)) {
            Files.createDirectories(path.getParent());
            try {
                Files.createFile(path);
            } catch (FileAlreadyExistsException e) {
                // Cannot create file atomically, so this error is possible
            }
        }
        try (DataOutputStream dataOutput = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(Files.newOutputStream(path))))) {
            writeUncompressedToStream(dataOutput, tag);
        }
    }

    private static void writeList(DataOutput stream, ListTag<?> list) throws IOException {
        TagType<?> listType = list.getListType();
        listType.write(stream);
        stream.writeInt(list.size());
        for (Object value : list) {
            writePayload(stream, listType, value);
        }
    }

    private static <T> void writePayload(DataOutput dataOutput, TagType<? extends T> tagType, T value) throws IOException {
        if (tagType == TagType.BYTE) {
            dataOutput.writeByte((Byte) value);
        } else if (tagType == TagType.COMPOUND) {
            writeCompound(dataOutput, (CompoundTag) value);
        } else if (tagType == TagType.DOUBLE) {
            dataOutput.writeDouble((Double) value);
        } else if (tagType == TagType.FLOAT) {
            dataOutput.writeFloat((Float) value);
        } else if (tagType == TagType.INT) {
            dataOutput.writeInt((Integer) value);
        } else if (tagType == TagType.INT_ARRAY) {
            int[] intArray = (int[]) value;
            dataOutput.writeInt(intArray.length);
            for (int integer : intArray) {
                dataOutput.writeInt(integer);
            }
        } else if (tagType == TagType.LIST) {
            writeList(dataOutput, (ListTag<?>) value);
        } else if (tagType == TagType.LONG) {
            dataOutput.writeLong((Long) value);
        } else if (tagType == TagType.BYTE_ARRAY) {
            byte[] byteArray = (byte[]) value;
            dataOutput.writeInt(byteArray.length);
            dataOutput.write(byteArray);
        } else if (tagType == TagType.SHORT) {
            dataOutput.writeShort((Short) value);
        } else if (tagType == TagType.STRING) {
            dataOutput.writeUTF((String) value);
        } else {
            throw new IOException("Unknown tag type: " + tagType);
        }
    }

    /**
     * Writes the compound tag with its header to the given stream, without
     * extra compression.
     *
     * @param dataOutput
     *            Stream to write to.
     * @param tag
     *            Tag to write.
     * @throws IOException
     *             If writing fails.
     */
    private static void writeUncompressedToStream(DataOutput dataOutput, CompoundTag tag) throws IOException {
        TagType.COMPOUND.write(dataOutput);
        dataOutput.writeUTF(""); // Required for NBT format
        writeCompound(dataOutput, tag);
    }
}
