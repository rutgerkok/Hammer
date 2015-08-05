package nl.rutgerkok.hammer.anvil.material;

import java.net.URL;
import java.util.Collections;

import nl.rutgerkok.hammer.material.BlockDataMaterialMap;
import nl.rutgerkok.hammer.material.GlobalMaterialMap;
import nl.rutgerkok.hammer.tag.CompoundKey;
import nl.rutgerkok.hammer.tag.CompoundTag;
import nl.rutgerkok.hammer.tag.ListTag;
import nl.rutgerkok.hammer.util.MaterialNotFoundException;

/**
 * Uses the mappings as written by Forge.
 *
 */
public class ForgeMaterialMap extends BlockDataMaterialMap {

    private static final CompoundKey<String> MAPPING_KEY = CompoundKey.of("K");
    private static final CompoundKey<Integer> MAPPING_VALUE = CompoundKey.of("V");

    public ForgeMaterialMap(GlobalMaterialMap dictionary, URL vanillaBlocks, ListTag<CompoundTag> itemDataTag) {
        super(dictionary, vanillaBlocks);
        for (CompoundTag mapping : itemDataTag) {
            // Forge seems to add a strange character before the key
            registerForgeBlock(mapping.getString(MAPPING_KEY).replaceAll("[^\\w\\-:]", ""),
                    mapping.getInt(MAPPING_VALUE));
        }
    }

    private void registerForgeBlock(String name, int id) {
        try {
            // Check if block already exists (loaded from our vanilla states
            // file)
            this.globalMap.getMaterialByName(name);
        } catch (MaterialNotFoundException e) {
            // Nope, so register it
            for (int i = 0; i <= MAX_BLOCK_DATA; i++) {
                // Block states unfortunately aren't saved to the level.dat,
                // so we just assume all data values are valid block states
                super.register((short) id, (byte) i, name + "[dataValue=" + i + "]", Collections.<String> emptyList());
            }
        }

    }

}
