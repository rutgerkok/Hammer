package nl.rutgerkok.hammer.anvil;

import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.ImmutableMap;

import nl.rutgerkok.hammer.anvil.tag.AnvilFormat.MaterialTag;
import nl.rutgerkok.hammer.material.BlockDataMaterialMap;
import nl.rutgerkok.hammer.material.BlockStatesMaterialMap;
import nl.rutgerkok.hammer.material.GlobalMaterialMap;
import nl.rutgerkok.hammer.material.MaterialData;
import nl.rutgerkok.hammer.material.MaterialName;
import nl.rutgerkok.hammer.material.WorldMaterialMap;
import nl.rutgerkok.hammer.tag.CompoundKey;
import nl.rutgerkok.hammer.tag.CompoundTag;
import nl.rutgerkok.hammer.util.MaterialNotFoundException;

public class AnvilMaterialMap implements WorldMaterialMap {

    private final BlockDataMaterialMap oldBlockIds;
    private final BlockStatesMaterialMap modernBlockIds;

    public AnvilMaterialMap(BlockDataMaterialMap oldBlockIds, BlockStatesMaterialMap modernBlockIds) {
        this.oldBlockIds = Objects.requireNonNull(oldBlockIds, "oldBlockIds");
        this.modernBlockIds = Objects.requireNonNull(modernBlockIds, "modernBlockIds");
    }

    @Override
    public GlobalMaterialMap getGlobal() {
        return modernBlockIds.getGlobal();
    }

    public MaterialData getMaterialDataFromOldIds(short id, byte data) {
        return oldBlockIds.getMaterialData(id, data);
    }

    public MaterialData getMaterialDataFromOldIds(String blockName, byte blockData) {
        return this.oldBlockIds.getMaterialData(blockName, blockData);
    }

    /**
     * Gets the old block id and data as a combined value (blockId * 16 +
     * blockData), as used by Minecraft 1.2 - 1.12. You can extract the block data
     * using {@code getMinecraftId(..) & 0xf} and the block id using
     * {@code getMinecraftId(..) >> 4}.
     *
     * @param materialData
     *            The material to get the block id for.
     * @return The old block id and data.
     * @throws MaterialNotFoundException
     *             If the material does not have an old id because it was added
     *             after Minecraft 1.13 (or because it's from a mod, Bedrock Edition
     *             exclusive, etc.)
     */
    public char getOldMinecraftId(MaterialData materialData) throws MaterialNotFoundException {
        return this.oldBlockIds.getMinecraftId(materialData);
    }

    /**
     * Gets the old block name, for example
     * "minecraft:stained_hardened_clay[color=red]" for "minecraft:red_terracotta".
     *
     * @param materialData
     *            The material.
     * @return The old block name.
     * @see #getOldMinecraftId(MaterialData) The old id and block data.
     */
    public MaterialName getOldMinecraftIdString(MaterialData materialData) {
        return this.oldBlockIds.getCanonicalMinecraftName(materialData);
    }


    /**
     * Parses the NBT tag into a material.
     *
     * @param tag
     *            The NBT tag.
     * @return The material.
     */
    public MaterialData parseBlockState(CompoundTag tag) {
        String name = tag.getString(MaterialTag.NAME);
        if (name.isEmpty()) {
            return this.getGlobal().getAir();
        }
        if (!tag.containsKey(MaterialTag.PROPERTIES)) {
            // Already done
            return this.getGlobal().addMaterial(MaterialName.ofBaseName(name));
        }

        // Do a bit more effort to read the material
        ImmutableMap.Builder<String, String> properties = ImmutableMap.builder();
        Set<Entry<CompoundKey<?>, Object>> propertyTags = tag.getCompound(MaterialTag.PROPERTIES).entrySet();
        for (Entry<CompoundKey<?>, Object> property : propertyTags) {
            properties.put(property.getKey().getKeyName(), property.getValue().toString());
        }
        return this.getGlobal().addMaterial(MaterialName.create(name, properties.build()));
    }

    /**
     * Serializes the material into an NBT tag.
     *
     * @param data
     *            The material.
     * @param target
     * @return The NBT tag.
     */
    public CompoundTag serializeToBlockState(MaterialData data, CompoundTag target) {
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
