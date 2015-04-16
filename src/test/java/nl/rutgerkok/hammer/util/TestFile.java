package nl.rutgerkok.hammer.util;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class TestFile {

    /**
     * Gets a test file with the given name.
     *
     * @param name
     *            Name of the file. Must not be prefixed with a /.
     * @return The file.
     */
    public static final Path get(String name) {
        try {
            return Paths.get(TestFile.class.getResource("/" + name).toURI());
        } catch (URISyntaxException e) {
            throw new AssertionError("Failed to get file for " + name, e);
        }
    }
}
