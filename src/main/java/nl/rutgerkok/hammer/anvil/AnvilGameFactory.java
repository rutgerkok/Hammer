package nl.rutgerkok.hammer.anvil;

import java.util.Objects;

import nl.rutgerkok.hammer.GameFactory;
import nl.rutgerkok.hammer.ItemStack;
import nl.rutgerkok.hammer.material.MaterialMap;
import nl.rutgerkok.hammer.tag.CompoundTag;

/**
 * Implementation of {@link GameFactory} for Anvil worlds.
 *
 */
final class AnvilGameFactory implements GameFactory {

    private final MaterialMap materialMap;

    public AnvilGameFactory(MaterialMap materialMap) {
        this.materialMap = Objects.requireNonNull(materialMap, "materialMap");
    }

    @Override
    public ItemStack createItemStack(CompoundTag tag) {
        return new AnvilItemStack(materialMap, tag);
    }

    @Override
    public MaterialMap getMaterialMap() {
        return materialMap;
    }

}
