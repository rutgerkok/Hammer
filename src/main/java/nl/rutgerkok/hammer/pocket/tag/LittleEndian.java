package nl.rutgerkok.hammer.pocket.tag;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Reads from a Little-endian stream, writes values in a little-endian format.
 *
 * <p>
 * Note that for {@link #readByte(InputStream)}, there is no difference between
 * big-endian and little-endian. However, unlike the methods on
 * {@link InputStream}, you don't need to cast the result of
 * {@link #readByte(InputStream)}, and you get an {@link EOFException} when the
 * end of the stream is reached.
 */
final class LittleEndian {

    /**
     * Reads a single byte from the stream.
     *
     * @param stream
     *            The stream.
     * @return The byte.
     * @throws IOException
     *             If an IO error occurs.
     */
    static byte readByte(InputStream stream) throws IOException {
        int theByte = stream.read();
        if (theByte == -1) {
            throw new EOFException();
        }
        return (byte) theByte;
    }

    /**
     * Reads a double from the little-endian stream.
     *
     * @param stream
     *            The stream.
     * @return The double.
     * @throws IOException
     *             If an IO error occurs.
     */
    static double readDouble(InputStream stream) throws IOException {
        return Double.longBitsToDouble(readLong(stream));
    }

    /**
     * Reads a float from the little-endian stream.
     *
     * @param stream
     *            The stream.
     * @return The float.
     * @throws IOException
     *             If an IO error occurs.
     */
    static float readFloat(InputStream stream) throws IOException {
        int value = readInt(stream);
        return Float.intBitsToFloat(value);
    }

    /**
     * Reads bytes from the stream until the given byte array is full.
     *
     * @param stream
     *            The stream.
     * @param into
     *            The byte array to read into.
     * @throws IOException
     *             If an IO error occurs, or if the stream contains less bytes
     *             than the length of the byte array.
     */
    static void readFully(InputStream stream, byte[] into) throws IOException {
        int bytesRead = stream.read(into);
        if (bytesRead != into.length) {
            throw new EOFException();
        }
    }

    /**
     * Reads an int from the little-endian stream.
     *
     * @param stream
     *            The stream.
     * @return The int.
     * @throws IOException
     *             If an IO error occurs.
     */
    static int readInt(InputStream stream) throws IOException {
        byte a = readByte(stream);
        byte b = readByte(stream);
        byte c = readByte(stream);
        byte d = readByte(stream);
        return (a & 0xff) | ((b & 0xff) << 8) |
                ((c & 0xff) << 16) | ((d & 0xff) << 24);
    }

    /**
     * Reads a long integer from the little-endian stream.
     *
     * @param stream
     *            The stream.
     * @return The long.
     * @throws IOException
     *             If an IO error occurs.
     */
    static long readLong(InputStream stream) throws IOException {
        byte a = readByte(stream);
        byte b = readByte(stream);
        byte c = readByte(stream);
        byte d = readByte(stream);
        byte e = readByte(stream);
        byte f = readByte(stream);
        byte g = readByte(stream);
        byte h = readByte(stream);
        return (a & 0xff) | ((long) (b & 0xff) << 8) |
                ((long) (c & 0xff) << 16) | ((long) (d & 0xff) << 24) |
                ((long) (e & 0xff) << 32) | ((long) (f & 0xff) << 40) |
                ((long) (g & 0xff) << 48) | ((long) (h & 0xff) << 56);
    }

    /**
     * Reads a short integer from the little-endian stream.
     *
     * @param stream
     *            The stream.
     * @return The short.
     * @throws IOException
     *             If an IO error occurs.
     */
    static short readShort(InputStream stream) throws IOException {
        byte a = readByte(stream);
        byte b = readByte(stream);
        return (short) ((a & 0xff) | ((b & 0xff) << 8));
    }

    /**
     * Reads an UTF-8 string from the stream, prefixed with a short that
     * indicates the length of the string.
     *
     * @param stream
     *            The stream.
     * @return The string.
     * @throws IOException
     *             If reading fails.
     */
    static String readUTF(InputStream stream) throws IOException {
        int length = readShort(stream) & 0xffff;
        byte[] bytes = new byte[length];
        readFully(stream, bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Writes a single byte to the output stream.
     *
     * @param outputStream
     *            Stream to write to.
     * @param number
     *            The number to write.
     * @throws IOException
     *             If an IO error occurs.
     */
    static void writeByte(OutputStream outputStream, byte number) throws IOException {
        outputStream.write(number & 0xff);
    }

    /**
     * Writes a double to the output stream in litte-endian format.
     *
     * @param outputStream
     *            Stream to write to.
     * @param number
     *            The number to write.
     * @throws IOException
     *             If an IO error occurs.
     */
    static void writeDouble(OutputStream outputStream, double number) throws IOException {
        writeLong(outputStream, Double.doubleToRawLongBits(number));
    }

    /**
     * Writes a float to the output stream in litte-endian format.
     *
     * @param outputStream
     *            Stream to write to.
     * @param number
     *            The number to write.
     * @throws IOException
     *             If an IO error occurs.
     */
    static void writeFloat(OutputStream outputStream, float number) throws IOException {
        int value = Float.floatToRawIntBits(number);
        writeInt(outputStream, value);
    }

    /**
     * Writes an int to the output stream in litte-endian format.
     *
     * @param outputStream
     *            Stream to write to.
     * @param number
     *            The number to write.
     * @throws IOException
     *             If an IO error occurs.
     */
    static void writeInt(OutputStream outputStream, int number) throws IOException {
        outputStream.write(number & 0x0000_00ff);
        outputStream.write((number & 0x0000_ff00) >>> 8);
        outputStream.write((number & 0x00ff_0000) >>> 16);
        outputStream.write((number & 0xff00_0000) >>> 24);
    }

    /**
     * Writes a long to the output stream in litte-endian format.
     *
     * @param outputStream
     *            Stream to write to.
     * @param number
     *            The number to write.
     * @throws IOException
     *             If an IO error occurs.
     */
    static void writeLong(OutputStream outputStream, long number) throws IOException {
        outputStream.write((int) (number & 0x0000_0000_0000_00ffL));
        outputStream.write((int) ((number & 0x0000_0000_0000_ff00L) >>> 8));
        outputStream.write((int) ((number & 0x0000_0000_00ff_0000L) >>> 16));
        outputStream.write((int) ((number & 0x0000_0000_ff00_0000L) >>> 24));

        outputStream.write((int) ((number & 0x0000_00ff_0000_0000L) >>> 32));
        outputStream.write((int) ((number & 0x0000_ff00_0000_0000L) >>> 40));
        outputStream.write((int) ((number & 0x00ff_0000_0000_0000L) >>> 48));
        outputStream.write((int) ((number & 0xff00_0000_0000_0000L) >>> 56));
    }

    /**
     * Writes a short to the output stream in litte-endian format.
     *
     * @param outputStream
     *            Stream to write to.
     * @param number
     *            The number to write.
     * @throws IOException
     *             If an IO error occurs.
     */
    static void writeShort(OutputStream outputStream, short number) throws IOException {
        outputStream.write(number & 0x00ff);
        outputStream.write((number & 0xff00) >>> 8);
    }

    /**
     * Writes the string as UTF-8 to the dataOutput, prefixed with a
     * {@link #writeShort(OutputStream, short) short} that represents the length
     * in bytes.
     *
     * @param outputStream
     *            Stream to write to.
     * @param string
     *            String to write.
     * @throws IOException
     *             If an IO error occurs.
     */
    static void writeUTF(OutputStream outputStream, String string) throws IOException {
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > 0xffff) {
            throw new IOException("String too big, byte length is " + bytes.length
                    + ", contents of string: " + string.substring(0, 50) + "...");
        }
        writeShort(outputStream, (short) bytes.length);
        outputStream.write(bytes);
    }
}
