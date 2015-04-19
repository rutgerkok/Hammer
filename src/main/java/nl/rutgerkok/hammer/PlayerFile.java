package nl.rutgerkok.hammer;

import java.util.Objects;

import nl.rutgerkok.hammer.tag.CompoundTag;

/**
 * Represents the data file of a player.
 *
 */
public final class PlayerFile {

    private final GameFactory gameFactory;
    private final CompoundTag tag;

    /**
     * Creates a data file
     *
     * @param gameFactory
     *            The game factory for the world.
     * @param tag
     *            The tag.
     */
    public PlayerFile(GameFactory gameFactory, CompoundTag tag) {
        this.gameFactory = Objects.requireNonNull(gameFactory);
        this.tag = Objects.requireNonNull(tag);
    }

    /**
     * Gets the material map used for this player file.
     *
     * @return The material map.
     */
    public GameFactory getGameFactory() {
        return gameFactory;
    }

    /**
     * Gets the tag inside the player file.
     *
     * @return The tag, with sub tags like Inventory, EnderItems, etc.
     */
    public CompoundTag getTag() {
        return tag;
    }
}
