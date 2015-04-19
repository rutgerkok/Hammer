package nl.rutgerkok.hammer.util;

/**
 * A consumer consumes values, and does some action with them.
 *
 * @param <T>
 *            Type of the result.
 */
public interface Consumer<T> {

    /**
     * When a result becomes available, this method is called.
     * 
     * @param value
     *            The result value.
     */
    void accept(T value);
}
