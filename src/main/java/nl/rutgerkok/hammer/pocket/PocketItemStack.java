package nl.rutgerkok.hammer.pocket;

import java.util.Objects;

import nl.rutgerkok.hammer.GameFactory;
import nl.rutgerkok.hammer.ItemStack;
import nl.rutgerkok.hammer.material.Material;
import nl.rutgerkok.hammer.material.MaterialData;
import nl.rutgerkok.hammer.material.MaterialMap;
import nl.rutgerkok.hammer.tag.CompoundTag;
import nl.rutgerkok.hammer.util.MaterialNotFoundException;

/**
 * Implementation of {@link ItemStack} for the Pocket Edition.
 *
 * @see GameFactory#createItemStack(CompoundTag)
 */
final class PocketItemStack implements ItemStack {

    private final MaterialMap materialMap;
    private final CompoundTag tag;

    public PocketItemStack(MaterialMap materialMap, CompoundTag tag) {
        this.materialMap = Objects.requireNonNull(materialMap, "materialMap");
        this.tag = Objects.requireNonNull(tag, "tag");
    }

    @Override
    public Material getMaterial() throws MaterialNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MaterialData getMaterialData() throws MaterialNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasMaterialData(MaterialData materialData) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setMaterialData(MaterialData materialData) {
        // TODO Auto-generated method stub

    }

}
