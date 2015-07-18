package nl.rutgerkok.hammer.anvil.material;

import java.util.HashMap;
import java.util.Map;

import nl.rutgerkok.hammer.material.Material;
import nl.rutgerkok.hammer.material.MaterialMap;
import nl.rutgerkok.hammer.tag.CompoundKey;
import nl.rutgerkok.hammer.tag.CompoundTag;
import nl.rutgerkok.hammer.tag.ListTag;
import nl.rutgerkok.hammer.util.MaterialNotFoundException;

/**
 * Uses the mappings as written by Forge.
 *
 */
public class ForgeMaterialMap implements MaterialMap {

    private static final String AIR_NAME = "air";
    private static final CompoundKey<String> MAPPING_KEY = CompoundKey.of("K");
    private static final CompoundKey<Integer> MAPPING_VALUE = CompoundKey.of("V");

    private Material air = null;
    private Map<Integer, AnvilMaterial> byId = new HashMap<>();
    private Map<String, AnvilMaterial> byName = new HashMap<>();

    public ForgeMaterialMap(ListTag<CompoundTag> itemDataTag) {
        for (CompoundTag mapping : itemDataTag) {
            // Forge seems to add a strange character before the key
            register(mapping.getString(MAPPING_KEY).replaceAll("[^\\w\\-:]", ""),
                    mapping.getInt(MAPPING_VALUE));
        }
    }

    @Override
    public Material getAir() {
        if (this.air == null) {
            try {
                this.air = getByName(AIR_NAME);
            } catch (MaterialNotFoundException e) {
                throw new RuntimeException("No material for " + AIR_NAME);
            }
        }
        return this.air;
    }

    @Override
    public AnvilMaterial getById(int id) throws MaterialNotFoundException {
        AnvilMaterial material = byId.get(id);
        if (material == null) {
            throw new MaterialNotFoundException(id);
        }
        return material;
    }

    @Override
    public AnvilMaterial getByName(String name) throws MaterialNotFoundException {
        if (!name.contains(":")) {
            // Missing separator, assume default namespace
            name = MINECRAFT_PREFIX + name;
        }

        AnvilMaterial material = byName.get(name);
        if (material == null) {
            throw new MaterialNotFoundException(name);
        }
        return material;
    }

    @Override
    public AnvilMaterial getByNameOrId(String nameOrId) throws MaterialNotFoundException {
        try {
            return getById(Integer.parseInt(nameOrId));
        } catch (NumberFormatException e) {
            return getByName(nameOrId);
        }
    }

    private void register(String name, int id) {
        AnvilMaterial material = new AnvilMaterial(name, id);
        byName.put(name, material);
        byId.put(id, material);
    }

}
