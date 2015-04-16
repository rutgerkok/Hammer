package nl.rutgerkok.hammer;

import java.util.List;

import nl.rutgerkok.hammer.anvil.material.AnvilMaterial;
import nl.rutgerkok.hammer.anvil.material.AnvilMaterialData;
import nl.rutgerkok.hammer.material.MaterialMap;
import nl.rutgerkok.hammer.tag.CompoundTag;

/**
 * Worlds are devided into chunks, to keep the infinite worlds manageable.
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
     * Gets all entities in this chunk.
     *
     * @return The entities.
     */
    List<CompoundTag> getEntities();

    /**
     * Gets the material id at the given position.
     *
     * @param x
     *            X position of the block,
     *            <code>0 <= x < {@link #getSizeX()}</code>.
     * @param y
     *            Y position of the block,
     *            <code>0 <= y < {@link #getSizeZ()}</code>.
     * @param z
     *            Z position of the block,
     *            <code>0 <= z < {@link #getSizeZ()}</code>.
     * @return The id, or {@value AnvilMaterial#AIR_ID} if the coordinates are
     *         out of bounds.
     */
    short getMaterialId(int x, int y, int z);

    /**
     * Gets the material map used for this chunk.
     *
     * @return The material map.
     */
    MaterialMap getMaterialMap();

    /**
     * Gets the size of the chunk on the x-axis.
     *
     * @return The size in blocks.
     */
    int getSizeX();

    /**
     * Gets the size of the chunk on the y-axis.
     *
     * @return The size in blocks.
     */
    int getSizeY();

    /**
     * Gets the size of the chunk on the z-axis.
     *
     * @return The size in blocks.
     */
    int getSizeZ();

    /**
     * Gets direct access to the chunk data tag. Modifying the returned chunk
     * data tag will modify the internal tag in this class.
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
     * Sets the block at the given position. Silently fails if the position is
     * out of bounds.
     *
     * @param x
     *            X position of the block,
     *            <code>0 <= x < {@link #getSizeX()}</code>.
     * @param y
     *            Y position of the block,
     *            <code>0 <= y < {@link #getSizeY()}</code>.
     * @param z
     *            Z position of the block,
     *            <code>0 <= z < {@link #getSizeZ()}</code>.
     * @param materialData
     *            Material to set.
     */
    void setBlock(int x, int y, int z, AnvilMaterialData materialData);

}