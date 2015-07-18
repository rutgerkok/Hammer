package nl.rutgerkok.hammer.tag;

import java.util.Objects;

/**
 * Represents a key in a compound tag. {@link #equals(Object)} and
 * {@link #hashCode()} are case insensitive.
 *
 * @param <T>
 *            Type of the value belonging to this key.
 */
public final class CompoundKey<T> {

    /**
     * Creates a new key with the given string representation.
     *
     * @param key
     *            The string representation.
     * @return The key.
     */
    public static final <T> CompoundKey<T> of(String key) {
        return new CompoundKey<T>(key);
    }

    private int hash = 0;

    private final String key;

    private CompoundKey(String key) {
        this.key = Objects.requireNonNull(key, "key");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CompoundKey)) {
            return false;
        }
        return ((CompoundKey<?>) obj).key.equalsIgnoreCase(key);
    }

    /**
     * Gets the key name.
     *
     * @return The key name.
     */
    public String getKeyName() {
        return key;
    }

    @Override
    public int hashCode() {
        // Case insensitive hashcode
        int h = hash;

        if (h == 0 && key.length() > 0) {
            for (int i = 0; i < key.length(); i++) {
                int codePoint = key.codePointAt(i);
                h = 31 * h + Character.toLowerCase(codePoint);
            }
            hash = h;
        }
        return h;
    }

    /**
     * Same as {@link #getKeyName()}.
     *
     * @return The key name.
     */
    @Override
    public String toString() {
        return key;
    }
}
