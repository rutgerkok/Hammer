package nl.rutgerkok.hammer.util;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DirectoryUtil {

    /**
     * Counts the amount of
     * {@link Files#isRegularFile(Path, java.nio.file.LinkOption...) regular
     * files} in the directory.
     *
     * @param directory
     *            The directory to count the files in.
     * @return The amount of files.
     * @throws IOException
     *             If an IO error occurs.
     * @throws NullPointerException
     *             If the given directory is null.
     */
    public static final int countFiles(Path directory) throws IOException {
        int fileCount = 0;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path file : stream) {
                if (Files.isRegularFile(file)) {
                    fileCount++;
                }
            }
        }
        return fileCount;
    }

    private DirectoryUtil() {
        // No instances
    }
}
