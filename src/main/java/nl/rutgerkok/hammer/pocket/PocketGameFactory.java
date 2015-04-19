package nl.rutgerkok.hammer.pocket;

import java.util.Objects;

import nl.rutgerkok.hammer.GameFactory;
import nl.rutgerkok.hammer.ItemStack;
import nl.rutgerkok.hammer.material.MaterialMap;
import nl.rutgerkok.hammer.tag.CompoundTag;

/**
 * Implementation of {@link GameFactory} for the Pocket Edition.
 *
 */
final class PocketGameFactory implements GameFactory {

    private final MaterialMap materialMap;

    PocketGameFactory(MaterialMap materialMap) {
        this.materialMap = Objects.requireNonNull(materialMap, "materialMap");
    }

    @Override
    public ItemStack createItemStack(CompoundTag tag) {
        return new PocketItemStack(materialMap, tag);
    }

    @Override
    public MaterialMap getMaterialMap() {
        return materialMap;
    }

}