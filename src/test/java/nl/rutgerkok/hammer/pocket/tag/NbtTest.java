package nl.rutgerkok.hammer.pocket.tag;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import nl.rutgerkok.hammer.tag.CompoundKey;
import nl.rutgerkok.hammer.tag.CompoundTag;

import org.junit.Test;

public class NbtTest {

    @Test
    public void testWrite() throws IOException {
        CompoundKey intKey = CompoundKey.of("Int");
        CompoundKey floatKey = CompoundKey.of("Float");
        CompoundKey stringKey = CompoundKey.of("String");

        CompoundTag original = new CompoundTag();
        original.setInt(intKey, 3);
        original.setFloat(floatKey, 3.3f);
        original.setString(stringKey, "Test string");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PocketNbtWriter.writeUncompressedToStream(outputStream, original);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        CompoundTag retrieved = PocketNbtReader.readFromUncompressedStream(inputStream);

        assertEquals(original, retrieved);
    }
}
