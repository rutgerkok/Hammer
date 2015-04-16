package nl.rutgerkok.hammer.util;

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
     */
    Result accept(T value, Progress progress);
}