package nl.rutgerkok.hammer.tag;

import java.util.ArrayList;
import java.util.Objects;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;

/**
 * Represents a list tag: it contains ordered, nameless tags of the given type.
 *
 * <p>
 * Tags are not thread safe, and must only be read/modified by one thread at the
 * same time.
 *
 * @param <T>
 *            Type of the tags in the list.
 */
public final class ListTag<T> extends ArrayList<T> implements JSONAware {

    private static final long serialVersionUID = -100623061960128101L;

    /**
     * Creates a list tag with the specified elements.
     *
     * @param first
     *            First element.
     * @param others
     *            Other elements.
     * @return The list tag.
     * @throws IllegalArgumentException
     *             If values aren't of a valid {@link TagType tag type}.
     */
    @SafeVarargs
    static <T> ListTag<T> of(T first, T... others) {
        TagType<T> type = TagType.ofObject(first);
        ListTag<T> tag = new ListTag<T>(type);
        tag.add(first);
        for (T element : others) {
            tag.add(element);
        }
        return tag;
    }

    private final TagType<? extends T> listType;

    @SuppressWarnings("unchecked")
    public ListTag(ListTag<T> original) {
        this(original.getListType());
        for (T value : original) {
            add((T) CompoundTag.deepCopy(value));
        }
    }

    public ListTag(TagType<? extends T> listType) {
        this.listType = Objects.requireNonNull(listType);
    }

    /**
     * Creates a deep copy of this tag. Modifications to the copy have no
     * influence to the original, and vice versa. This means that copies can be
     * used safely in another thread.
     *
     * @return The copy.
     */
    public ListTag<T> copy() {
        return new ListTag<T>(this);
    }

    /**
     * Gets the type of the list.
     *
     * @return The type.
     */
    public TagType<? extends T> getListType() {
        return listType;
    }

    @Override
    public String toJSONString() {
        // Use default JSON representation of this list
        return JSONArray.toJSONString(this);
    }

}
