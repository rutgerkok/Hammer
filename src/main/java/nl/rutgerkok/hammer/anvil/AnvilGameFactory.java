package nl.rutgerkok.hammer.anvil;

import java.util.Objects;

import nl.rutgerkok.hammer.GameFactory;
import nl.rutgerkok.hammer.ItemStack;
import nl.rutgerkok.hammer.tag.CompoundTag;

/**
 * Implementation of {@link GameFactory} for Anvil worlds.
 *
 */
public final class AnvilGameFactory implements GameFactory {

    private final AnvilMaterialMap materialMap;

    public AnvilGameFactory(AnvilMaterialMap materialMap) {
        this.materialMap = Objects.requireNonNull(materialMap, "materialMap");
    }

    @Override
    public ItemStack createItemStack(CompoundTag tag) {
        return new AnvilItemStack(materialMap, tag);
    }

    @Override
    public AnvilMaterialMap getMaterialMap() {
        return materialMap;
    }

}
