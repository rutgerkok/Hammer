package nl.rutgerkok.hammer.pocket.tag;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

public class LittleEndianTest {

    @Test
    public void testInt() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        LittleEndian.writeInt(outputStream, 10);
        LittleEndian.writeInt(outputStream, Integer.MAX_VALUE);
        LittleEndian.writeInt(outputStream, Integer.MIN_VALUE);

        ByteArrayInputStream inputStream = flip(outputStream);
        assertEquals(10, LittleEndian.readInt(inputStream));
        assertEquals(Integer.MAX_VALUE, LittleEndian.readInt(inputStream));
        assertEquals(Integer.MIN_VALUE, LittleEndian.readInt(inputStream));
    }

    @Test
    public void testLong() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        LittleEndian.writeLong(outputStream, 10);
        LittleEndian.writeLong(outputStream, Long.MAX_VALUE);
        LittleEndian.writeLong(outputStream, Long.MIN_VALUE);

        ByteArrayInputStream inputStream = flip(outputStream);
        assertEquals(10, LittleEndian.readLong(inputStream));
        assertEquals(Long.MAX_VALUE, LittleEndian.readLong(inputStream));
        assertEquals(Long.MIN_VALUE, LittleEndian.readLong(inputStream));
    }

    @Test
    public void testFloat() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        LittleEndian.writeFloat(outputStream, 3.3f);
        LittleEndian.writeFloat(outputStream, Float.MAX_VALUE);
        LittleEndian.writeFloat(outputStream, Float.NaN);
        LittleEndian.writeFloat(outputStream, Float.NEGATIVE_INFINITY);

        ByteArrayInputStream inputStream = flip(outputStream);
        assertEquals(3.3f, LittleEndian.readFloat(inputStream), 0.001);
        assertEquals(Float.MAX_VALUE, LittleEndian.readFloat(inputStream), 0.001);
        assertEquals(Float.NaN, LittleEndian.readFloat(inputStream), 0.001);
        assertEquals(Float.NEGATIVE_INFINITY, LittleEndian.readFloat(inputStream), 0.001);
    }

    private ByteArrayInputStream flip(ByteArrayOutputStream outputStream) {
        return new ByteArrayInputStream(outputStream.toByteArray());
    }
}
