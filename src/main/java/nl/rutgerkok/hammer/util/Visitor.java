package nl.rutgerkok.hammer.util;

import java.io.IOException;

/**
 * Single-abstract-method interface. Implementations perform some operation on
 * the given input type.
 *
 * @param <T>
 *            The type.
 */
public interface Visitor<T> {

    /**
     * Performs this operation on the given argument.
     *
     * @param value
     *            the input argument
     * @param progress
     *            The current progress.
     * @return The result of this operation.
     * @throws IOException
     *             If an IO error occurs.
     */
    Result accept(T value, Progress progress) throws IOException;
}