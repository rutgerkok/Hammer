package nl.rutgerkok.hammer.util;

/**
 * The result of an operation performed by a {@link Visitor}.
 *
 */
public enum Result {
    /**
     * Changes were made, chunk needs to be saved.
     */
    CHANGED {
        @Override
        public Result getCombined(Result update) {
            if (update == Result.DELETE) {
                return Result.DELETE;
            }
            return this;
        }
    },
    /**
     * Chunk needs to be deleted.
     */
    DELETE {
        @Override
        public Result getCombined(Result update) {
            return this;
        }
    },
    /**
     * No changes were made, no need to save the chunk.
     */
    NO_CHANGES {
        @Override
        public Result getCombined(Result update) {
            return update;
        }
    };

    /**
     * Returns a new result that is a combination of this result and the given
     * result. For example, {@link #NO_CHANGES} and {@link #CHANGED} combine to
     * {@link #CHANGED}. {@link #CHANGED} and {@link #DELETE} combine to
     * {@link #DELETE}.
     *
     * @param update
     *            The result to combine with this result.
     * @return The combined result.
     */
    public abstract Result getCombined(Result update);
}