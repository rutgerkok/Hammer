package nl.rutgerkok.hammer;

import java.util.List;

import nl.rutgerkok.hammer.material.MaterialData;
import nl.rutgerkok.hammer.tag.CompoundTag;
import nl.rutgerkok.hammer.util.MaterialNotFoundException;

/**
 * Worlds are divided into chunks, to keep the infinite worlds manageable.
 *
 */
public interface Chunk {

    /**
     * Gets the chunk x in the world.
     *
     * @return The chunk x.
     */
    int getChunkX();

    /**
     * Gets the chunk z in the world.
     *
     * @return The chunk z.
     */
    int getChunkZ();

    /**
     * Gets the lowest y at which blocks can be placed, based on the currently
     * available chunk sections.
     *
     * @return The size in blocks.
     */
    int getDepth();

    /**
     * Gets all entities in this chunk.
     *
     * @return The entities.
     */
    List<CompoundTag> getEntities();

    /**
     * Gets the game factory used for this chunk.
     *
     * @return The game factory.
     */
    GameFactory getGameFactory();

    /**
     * Gets y limit at which blocks can be placed, based on the currently available
     * chunk sections. The highest value at which blocks can be placed is one lower
     * than this. For example, if this method returns "80", then the highest block
     * for which chunk sections are initialized is at y=79.
     *
     * @return The size in blocks.
     */
    int getHeight();

    /**
     * Gets the block at the given location.
     *
     * @param x
     *            X position of the block, <code>0 <= x < {@link #getSizeX()}
     *            </code>.
     * @param y
     *            Y position of the block, <code>0 <= y < {@link #getHeight()}
     *            </code>.
     * @param z
     *            Z position of the block, <code>0 <= z < {@link #getSizeZ()}
     *            </code>.
     * @return The block.
     * @throws MaterialNotFoundException
     *             If the material with the saved id is unknown.
     * @throws IndexOutOfBoundsException
     *             If the x, y or z is outside this chunk.
     */
    MaterialData getMaterial(int x, int y, int z) throws MaterialNotFoundException;

    /**
     * Gets the size of the chunk on the x-axis.
     *
     * @return The size in blocks.
     */
    int getSizeX();

    /**
     * Gets the size of the chunk on the z-axis.
     *
     * @return The size in blocks.
     */
    int getSizeZ();

    /**
     * Gets direct access to the chunk data tag. Modifying the returned chunk data
     * tag will modify the internal tag in this class.
     *
     * @return The chunk data tag.
     */
    CompoundTag getTag();

    /**
     * Gets access to the tile entities in this chunk.
     *
     * @return The tile entities.
     */
    List<CompoundTag> getTileEntities();

    /**
     * Checks if the given block is out of bounds for this chunk.
     *
     * @param x
     *            X position of the block, <code>0 <= x < {@link #getSizeX()}
     *            </code> for the block to be in bounds.
     * @param y
     *            Y position of the block, <code>0 <= y < {@link #getHeight()}
     *            </code> for the block to be in bounds.
     * @param z
     *            Z position of the block, <code>0 <= z < {@link #getSizeZ()}
     *            </code> for the block to be in bounds.
     * @return True if the position is out of bounds, false otherwise.
     */
    boolean isOutOfBounds(int x, int y, int z);

    /**
     * Sets the block at the given position.
     *
     * @param x
     *            X position of the block, <code>0 <= x < {@link #getSizeX()}
     *            </code>.
     * @param y
     *            Y position of the block, <code>0 <= y < {@link #getHeight()}
     *            </code>.
     * @param z
     *            Z position of the block, <code>0 <= z < {@link #getSizeZ()}
     *            </code>.
     * @param materialData
     *            Material to set.
     * @throws MaterialNotFoundException
     *             If the material data is not supported in this world.
     * @throws IndexOutOfBoundsException
     *             If the x, y or z is outside the chunk.
     */
    void setMaterial(int x, int y, int z, MaterialData materialData) throws MaterialNotFoundException;

}
