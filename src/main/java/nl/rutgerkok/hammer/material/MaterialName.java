package nl.rutgerkok.hammer.material;

import java.text.ParseException;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.google.common.collect.ImmutableMap;

/**
 * The name of a material. For example, "minecraft:air" or
 * "minecraft:grass[snowy=false]". Instances are immutable. Two instances are
 * equal if the material name and properties match (case sensitive).
 */
public final class MaterialName {

    private static final String MINECRAFT_PREFIX = "minecraft:";

    private static void checkBaseName(String name) {
        if (name.contains("[")) {
            throw new IllegalArgumentException("A material name cannot contain the '[' character");
        }
        if (!name.contains(":")) {
            throw new IllegalArgumentException("A material name must contain a namespace seperator (the ':' char)");
        }
    }

    /**
     * Creates a material name.
     *
     * @param name
     *            The base name, like "minecraft:grass".
     * @param properties
     *            The properties, like {"snowy": "false"}.
     * @return The material name.
     * @throws IllegalArgumentException
     *             If the base name does not contain a :, or contains a [.
     */
    public static MaterialName create(String name, Map<String, String> properties) {
        checkBaseName(name);
        return new MaterialName(name, ImmutableMap.copyOf(properties));
    }

    /**
     * Creates a material name without any properties.
     *
     * @param name
     *            The base name.
     * @return The material name.
     * @throws IllegalArgumentException
     *             If the base name does not contain a :, or contains a [.
     */
    public static MaterialName ofBaseName(String name) {
        checkBaseName(name);
        return new MaterialName(name, ImmutableMap.of());
    }

    /**
     * Parses a material name.
     *
     * @param fullName
     *            The unparsed material name, like "minecraft:air", "STONE" or
     *            "minecraft:grass[snowy=false]". The case of the material is
     *            only changed if it is missing a namespace (like
     *            {@value #MINECRAFT_PREFIX}).
     * @return The parsed name.
     * @throws ParseException
     */
    public static MaterialName parse(String fullName) throws ParseException {
        int bracketIndex = fullName.indexOf('[');
        if (bracketIndex == -1) {
            return new MaterialName(prefixName(fullName), ImmutableMap.of());
        }

        if (!fullName.endsWith("]")) {
            throw new ParseException("Found a [, but no matching ] at the end of the name", fullName.length() - 1);
        }

        String baseName = prefixName(fullName.substring(0, bracketIndex));
        ImmutableMap.Builder<String, String> properties = ImmutableMap.builder();
        int parsePosition = bracketIndex + 1;
        for (String keyValuePair : fullName.substring(parsePosition, fullName.length() - 1).split(",")) {
            int assignmentOperatorIndex = keyValuePair.indexOf('=');
            if (assignmentOperatorIndex == -1) {
                throw new ParseException("Invalid key-value pair: missing assignment operator ('=')",
                        parsePosition + keyValuePair.length());
            }
            String key = keyValuePair.substring(0, assignmentOperatorIndex);
            String value = keyValuePair.substring(assignmentOperatorIndex + 1);
            properties.put(key, value);

            parsePosition += keyValuePair.length();
        }
        return new MaterialName(baseName, properties.build());
    }

    private static String prefixName(String baseName) {
        if (baseName.contains(":")) {
            return baseName;
        }
        // Change "STONE" into "minecraft:stone"
        return MINECRAFT_PREFIX + baseName.toLowerCase(Locale.ROOT);
    }

    private final String name;
    private final ImmutableMap<String, String> properties;
    private final int hashCode;

    private MaterialName(String name, ImmutableMap<String, String> properties) {
        this.name = Objects.requireNonNull(name, "name");
        this.properties = Objects.requireNonNull(properties, "properties");

        this.hashCode = Objects.hash(this.name, this.properties);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (!(other instanceof MaterialName)) {
            return false;
        }
        MaterialName that = (MaterialName) other;
        if (that.hashCode != this.hashCode) {
            return false;
        }
        return this.name.equals(that.name) && this.properties.equals(that.properties);
    }

    /**
     * Gets the material name when used without properties.
     * @return The base material name.
     */
    public MaterialName getBaseMaterialName() {
        if (this.properties.isEmpty()) {
            return this; // Already a base material
        }
        return new MaterialName(this.name, ImmutableMap.of());
    }

    /**
     * Gets the base name of this material, like "minecraft:grass" in
     * "minecraft:grass[snowy=false]".
     *
     * @return The base name.
     */
    public String getBaseName() {
        return this.name;
    }

    /**
     * Gets the properties of this material, like {"snowy": "false"} in
     * "minecraft:grass[snowy=false]".
     *
     * @return The properties.
     */
    public ImmutableMap<String, String> getProperties() {
        return this.properties;
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    /**
     * Returns whether the material has properties, like {"facing": "north"}.
     *
     * @return True if the material has properties, false otherwise.
     */
    public boolean hasProperties() {
        return !this.properties.isEmpty();
    }

    @Override
    public String toString() {
        if (properties.isEmpty()) {
            return name;
        }

        boolean first = true;
        StringBuilder builder = new StringBuilder(name);
        builder.append('[');
        for (Entry<String, String> property : properties.entrySet()) {
            if (first) {
                first = false;
            } else {
                builder.append(',');
            }
            builder.append(property.getKey());
            builder.append('=');
            builder.append(property.getValue());
        }
        builder.append(']');
        return builder.toString();
    }

}
