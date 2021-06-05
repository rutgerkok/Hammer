package nl.rutgerkok.hammer.material;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import nl.rutgerkok.hammer.util.MaterialNotFoundException;
import nl.rutgerkok.hammer.util.NumberMap;

/**
 * Material map based on the assumption that all block materials have both a
 * block id and block data. The block data must be in the range {@code 0} to
 * {@value #MAX_BLOCK_DATA}, inclusive.
 *
 */
public class BlockDataMaterialMap implements WorldMaterialMap {

    /**
     * Maximum allowed data value for blocks.
     */
    public static final short MAX_BLOCK_DATA = 15;

    protected final GlobalMaterialMap globalMap;
    private final NumberMap idhToAnvil;
    private final NumberMap anvilToIdh;
    private final Map<Integer, MaterialName> idhToAnvilName;

    public BlockDataMaterialMap(GlobalMaterialMap materialDictionary, URL blocksFile) {
        this.globalMap = Objects.requireNonNull(materialDictionary);

        this.idhToAnvil = new NumberMap();
        this.anvilToIdh = new NumberMap();
        this.idhToAnvilName = new HashMap<>();

        try {
            registerVanillaMaterials(blocksFile);
        } catch (java.text.ParseException e) {
            throw new RuntimeException("Failed to load Vanilla materials", e);
        }
    }

    /**
     * @param materialData
     *            The material data.
     * @return The base name.
     * @deprecated Use {@link MaterialData#getBaseName()}
     */
    @Deprecated
    public String getBaseName(MaterialData materialData) {
        return materialData.getBaseName();
    }

    /**
     * Gets the canonical name for the given material. For example, Minecraft 1.12
     * worlds store red terracotta as "minecraft:stained_hardened_clay[color=red]",
     * while {@link MaterialData#getBaseName()} will return
     * "minecraft:red_terracotta".
     *
     * @param materialData
     *            The material.
     * @return The name.
     */
    public MaterialName getCanonicalMinecraftName(MaterialData materialData) {
        MaterialName name = idhToAnvilName.get((int) materialData.getId());
        if (name == null) {
            return materialData.getMaterialName();
        }
        return name;
    }

    @Override
    public GlobalMaterialMap getGlobal() {
        return globalMap;
    }

    /**
     * Gets the material data object belonging to the given block id and data.
     *
     * @param blockId
     *            The block id.
     * @param blockData
     *            The block data.
     * @return The material data object.
     * @throws MaterialNotFoundException
     *             If no such material is registered.
     */
    public final MaterialData getMaterialData(short blockId, byte blockData) throws MaterialNotFoundException {
        try {
            int idh = anvilToIdh.getTranslatedId((char) (blockId * 16 | blockData));
            return globalMap.getMaterialById(idh);
        } catch (NoSuchElementException e) {
            throw new MaterialNotFoundException(blockId, blockData);
        }
    }

    /**
     * Gets the material data object belonging to the given block name and data.
     *
     * @param blockName
     *            The block id.
     * @param blockData
     *            The block data.
     * @return The material data object.
     * @throws MaterialNotFoundException
     *             If no such material is registered.
     */
    public MaterialData getMaterialData(String blockName, byte blockData) throws MaterialNotFoundException {
        // Get material data for base name
        MaterialData withoutBlockData = globalMap.getMaterialByName(MaterialName.ofBaseName(blockName));

        // Now get the block id, and use it to find the right material data
        int blockId = getMinecraftId(withoutBlockData) >> 4;
        return getMaterialData((short) blockId, blockData);
    }

    /**
     * Gets the block id and data as a combined value (blockId * 16 + blockData).
     * You can extract the block data using {@code getMinecraftId(..) & 0xf} and the
     * block id using {@code getMinecraftId(..) >> 4}.
     *
     * @param materialData
     *            The material to get the block id for.
     * @return The Anvil block id and data.
     * @throws MaterialNotFoundException
     *             If the material is not available in this world (like the Nether
     *             Reactor).
     */
    public final char getMinecraftId(MaterialData materialData) throws MaterialNotFoundException {
        try {
            return idhToAnvil.getTranslatedId(materialData.getId());
        } catch (NoSuchElementException e) {
            throw new MaterialNotFoundException(materialData.getMaterialName());
        }
    }

    /**
     * Registers a block type.
     *
     * @param blockId
     *            Anvil block id.
     * @param blockData
     *            Anvil block data.
     * @param names
     *            Names, like "minecraft:granite" or "minecraft:air". If the
     *            blockData is not 0, the brackets must be present. The first name
     *            is the global name, the last name the canonical name for this map.
     *            For example: ["minecraft:podzol",
     *            "minecraft:dirt[variant=podzol]"]
     * @throws java.text.ParseException
     *             If the format of the material name is invalid.
     */
    protected final void register(short blockId, byte blockData, List<String> names)
            throws java.text.ParseException {
        MaterialName canonicalName = MaterialName.parse(names.get(names.size() - 1));
        List<MaterialName> parsedNames = new ArrayList<>();
        // Allow lookup by full name and aliases
        for (String name : names) {
            parsedNames.add(MaterialName.parse(name));
        }

        // Register in the various registries
        MaterialData materialData = globalMap.addMaterial(parsedNames);
        char ida = (char) (blockId << 4 | blockData);
        char idh = materialData.getId();
        idhToAnvil.put(idh, ida);
        anvilToIdh.put(ida, idh);
        idhToAnvilName.put((int) idh, canonicalName);
    }

    private void registerVanillaMaterials(URL blocksFile) throws java.text.ParseException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(blocksFile.openStream(), StandardCharsets.UTF_8))) {
            JSONArray list = (JSONArray) JSONValue.parseWithException(reader);
            for (Object entry : list) {
                JSONArray listEntry = (JSONArray) entry;
                short blockId = ((Number) listEntry.get(0)).shortValue();
                byte blockData = ((Number) listEntry.get(1)).byteValue();

                // The next operation assumes all remaining elements in the list
                // are strings. We catch the ClassCastException below if this is
                // not the case
                @SuppressWarnings("unchecked")
                List<String> names = listEntry.subList(2, listEntry.size());
                this.register(blockId, blockData, names);
            }
        } catch (ClassCastException | ArrayIndexOutOfBoundsException | IOException | ParseException e) {
            // Invalid JSON, should be impossible as we're providing the JSON
            throw new RuntimeException(e);
        }
    }

}
