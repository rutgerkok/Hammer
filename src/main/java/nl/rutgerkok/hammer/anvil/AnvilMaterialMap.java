package nl.rutgerkok.hammer.anvil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import com.google.common.collect.ImmutableMap;

import nl.rutgerkok.hammer.anvil.tag.AnvilFormat.MaterialTag;
import nl.rutgerkok.hammer.material.GlobalMaterialMap;
import nl.rutgerkok.hammer.material.MaterialData;
import nl.rutgerkok.hammer.material.MaterialName;
import nl.rutgerkok.hammer.material.WorldMaterialMap;
import nl.rutgerkok.hammer.tag.CompoundKey;
import nl.rutgerkok.hammer.tag.CompoundTag;

public class AnvilMaterialMap implements WorldMaterialMap {

    /**
     * Used to parse a property map to a map of <String, String>.
     *
     * @param propertiesOrNull
     *            The incoming map, may be null (that's how the file format works).
     *
     * @return An immutable map.
     */
    private static ImmutableMap<String, String> parsePropertyMap(Map<?, ?> propertiesOrNull) {
            if (propertiesOrNull == null) {
            return ImmutableMap.of();
            } else {
                ImmutableMap.Builder<String, String> properties = ImmutableMap.builder();
                for (Entry<?, ?> entry : propertiesOrNull.entrySet()) {
                    String key = (String) entry.getKey();
                    String value = (String) entry.getValue();
                    properties.put(key, value);
                }
            return properties.build();
            }
        }


    private final GlobalMaterialMap globalMap;

    public AnvilMaterialMap(GlobalMaterialMap materialDictionary, URL blocksFile) {
        this.globalMap = Objects.requireNonNull(materialDictionary);

        registerVanillaMaterials(blocksFile);
    }

    @Override
    public GlobalMaterialMap getGlobal() {
        return globalMap;
    }

    /**
     * Parses the NBT tag into a material.
     *
     * @param tag
     *            The NBT tag.
     * @return The material.
     */
    public MaterialData parseMaterialData(CompoundTag tag) {
        String name = tag.getString(MaterialTag.NAME);
        if (name.isEmpty()) {
            return this.globalMap.getAir();
        }
        if (!tag.containsKey(MaterialTag.PROPERTIES)) {
            return this.globalMap.addMaterial(MaterialName.ofBaseName(name));
        }

        // Do a bit more effort to read the material
        ImmutableMap.Builder<String, String> properties = ImmutableMap.builder();
        Set<Entry<CompoundKey<?>, Object>> propertyTags = tag.getCompound(MaterialTag.PROPERTIES).entrySet();
        for (Entry<CompoundKey<?>, Object> property : propertyTags) {
            properties.put(property.getKey().getKeyName(), property.getValue().toString());
        }
        return this.globalMap.addMaterial(MaterialName.create(name, properties.build()));
    }

    private void register(long stateId, String minecraftKey, Map<String, String> properties) {
        this.globalMap.addMaterial(MaterialName.create(minecraftKey, properties));
    }

    private void registerVanillaMaterials(URL blocksFile) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(blocksFile.openStream(), StandardCharsets.UTF_8))) {
            JSONObject registry = (JSONObject) JSONValue.parseWithException(reader);
            for (Object entryObject : registry.entrySet()) {
                String minecraftKey = (String) ((Entry<?, ?>) entryObject).getKey();
                JSONObject value = (JSONObject) ((Entry<?, ?>) entryObject).getValue();
                JSONArray states = (JSONArray) value.get("states");
                for (Object stateObject : states) {
                    JSONObject state = (JSONObject) stateObject;
                    long stateId = ((Number) state.get("id")).longValue();
                    Map<String, String> properties = parsePropertyMap((Map<?, ?>) state.get("properties"));
                    register(stateId, minecraftKey, properties);
                }
            }
        } catch (ClassCastException | ArrayIndexOutOfBoundsException | IOException | ParseException e) {
            // Invalid JSON, should be impossible as we're providing the JSON
            throw new RuntimeException(e);
        }
    }

    /**
     * Serializes the material into an NBT tag.
     *
     * @param data
     *            The material.
     * @param target
     * @return The NBT tag.
     */
    public CompoundTag serializeMaterialData(MaterialData data, CompoundTag target) {
        MaterialName material = data.getMaterialName();

        target.setString(MaterialTag.NAME, material.getBaseName());
        target.remove(MaterialTag.PROPERTIES);

        if (!material.hasProperties()) {
            return target; // Done!
        }

        // Add new properties
        CompoundTag properties = target.getCompound(MaterialTag.PROPERTIES);
        for (Entry<String, String> property : material.getProperties().entrySet()) {
            properties.setString(CompoundKey.of(property.getKey()), property.getValue());
        }

        return target;
    }

}
