package nl.rutgerkok.hammer.material;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.google.common.collect.ImmutableMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

public class BlockStatesMaterialMap implements WorldMaterialMap {

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

    public BlockStatesMaterialMap(GlobalMaterialMap materialDictionary, URL blocksFile) {
        this.globalMap = Objects.requireNonNull(materialDictionary);

        registerVanillaMaterials(blocksFile);
    }

    @Override
    public GlobalMaterialMap getGlobal() {
        return globalMap;
    }

    private void register(int stateId, String minecraftKey, Map<String, String> properties) {
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
                    int stateId = (int) state.get("id");
                    Map<String, String> properties = parsePropertyMap((Map<?, ?>) state.get("properties"));
                    register(stateId, minecraftKey, properties);
                }
            }
        } catch (ClassCastException | ArrayIndexOutOfBoundsException | IOException | ParseException e) {
            // Invalid JSON, should be impossible as we're providing the JSON
            throw new RuntimeException(e);
        }
    }

}
