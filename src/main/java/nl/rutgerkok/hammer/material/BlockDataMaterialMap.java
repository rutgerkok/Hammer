package nl.rutgerkok.hammer.material;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import com.google.common.base.Throwables;

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

    public BlockDataMaterialMap(GlobalMaterialMap materialDictionary, URL blocksFile) {
        this.globalMap = Objects.requireNonNull(materialDictionary);

        this.idhToAnvil = new NumberMap();
        this.anvilToIdh = new NumberMap();

        registerVanillaMaterials(blocksFile);
    }

    private void registerVanillaMaterials(URL blocksFile) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(blocksFile.openStream(), StandardCharsets.UTF_8))) {
            JSONArray list = (JSONArray) JSONValue.parseWithException(reader);
            for (Object entry : list) {
                JSONArray listEntry = (JSONArray) entry;
                short blockId = ((Number) listEntry.get(0)).shortValue();
                byte blockData = ((Number) listEntry.get(1)).byteValue();
                String blockName = (String) listEntry.get(2);

                // The next operation assumes all remaining elements in the list
                // are strings. We catch the ClassCastException below if this is
                // not the case
                @SuppressWarnings("unchecked")
                List<String> alternatives = listEntry.subList(3, listEntry.size());
                this.register(blockId, blockData, blockName, alternatives);
            }
        } catch (ClassCastException | ArrayIndexOutOfBoundsException | IOException | ParseException e) {
            // Invalid JSON, should be impossible as we're providing the JSON
            throw Throwables.propagate(e);
        }
    }

    /**
     * Registers a block type.
     * 
     * @param blockId
     *            Anvil block id.
     * @param blockData
     *            Anvil block data.
     * @param fullName
     *            Name, like "minecraft:stone[variant=stone]" or
     *            "minecraft:air". If the blockData is not 0, the brackets must
     *            be present.
     * @param aliases
     *            Alternative names for the block, like "minecraft:podzol" for
     *            "minecraft:dirt[variant=podzol]". This list won't be modified,
     *            only read by this method.
     */
    protected final void register(short blockId, byte blockData, String fullName, List<String> aliases) {
        String baseName = getBaseName(fullName);
        List<String> names = new ArrayList<>();
        // Allow lookup by full name and aliases
        names.add(fullName);
        names.addAll(aliases);

        // Allow lookup by base name when block data is 0
        if (blockData == 0 && !baseName.equals(fullName)) {
            names.add(baseName);
        }

        // Also allow lookup in blockname:blockdata format
        names.add(baseName + ":" + blockData);

        // Register in the various registries
        MaterialData materialData = globalMap.addMaterial(names);
        char ida = (char) (blockId << 4 | blockData);
        char idh = materialData.getId();
        idhToAnvil.put(idh, ida);
        anvilToIdh.put(ida, idh);
    }

    /**
     * Extracts the base name ("minecraft:stone") from a full block name
     * ("minecraft:stone[variant=stone]"). If the given input is already a base
     * block name (like "minecraft:air"), the input is simply returned.
     * 
     * @param fullBlockName
     *            The full block name.
     * @return The base name.
     */
    private String getBaseName(String fullBlockName) {
        int bracketIndex = fullBlockName.indexOf('[');
        if (bracketIndex == -1) {
            // Already a base name
            return fullBlockName;
        }
        return fullBlockName.substring(0, bracketIndex);
    }

    /**
     * Extracts the base name ("minecraft:stone") from a {@link MaterialData}
     * object ("minecraft:stone[variant=stone]").
     * 
     * @param materialData
     *            The material.
     * @return The base name.
     */
    public String getBaseName(MaterialData materialData) {
        return getBaseName(materialData.getName());
    }

    @Override
    public MaterialData getAir() {
        try {
            return globalMap.getMaterialById(0);
        } catch (MaterialNotFoundException e) {
            throw new IllegalStateException("No air material for this world; corrupted or unreadable world?");
        }
    }

    /**
     * Gets the Anvil block id and data as a combined value (blockId * 16 +
     * blockData). You can extract the block data using
     * {@code getMinecraftId(..) & 0xf} and the block id using
     * {@code getMinecraftId(..) >> 4}.
     *
     * @param materialData
     *            The material to get the block id for.
     * @return The Anvil block id and data.
     * @throws MaterialNotFoundException
     *             If the material is not available in this world (like the
     *             Nether Reactor).
     */
    public final char getMinecraftId(MaterialData materialData) throws MaterialNotFoundException {
        try {
            return idhToAnvil.getTranslatedId(materialData.getId());
        } catch (NoSuchElementException e) {
            throw new MaterialNotFoundException(materialData.getName());
        }
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
        MaterialData withoutBlockData = globalMap.getMaterialByName(blockName);

        // Now get the block id, and use it to find the right material data
        int blockId = getMinecraftId(withoutBlockData) >> 4;
        return getMaterialData((short) blockId, blockData);
    }

    @Override
    public GlobalMaterialMap getGlobal() {
        return globalMap;
    }

}
