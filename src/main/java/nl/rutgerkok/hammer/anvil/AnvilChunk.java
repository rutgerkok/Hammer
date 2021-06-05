package nl.rutgerkok.hammer.anvil;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import nl.rutgerkok.hammer.Chunk;
import nl.rutgerkok.hammer.anvil.chunksection.ChunkBlocks;
import nl.rutgerkok.hammer.anvil.tag.AnvilFormat.ChunkRootTag;
import nl.rutgerkok.hammer.anvil.tag.AnvilFormat.ChunkTag;
import nl.rutgerkok.hammer.anvil.tag.AnvilFormat.EntitiesRootTag;
import nl.rutgerkok.hammer.anvil.tag.AnvilFormat.OldSectionTag;
import nl.rutgerkok.hammer.anvil.tag.AnvilFormat.SectionTag;
import nl.rutgerkok.hammer.material.MaterialData;
import nl.rutgerkok.hammer.tag.CompoundTag;
import nl.rutgerkok.hammer.tag.ListTag;
import nl.rutgerkok.hammer.tag.TagType;
import nl.rutgerkok.hammer.util.MaterialNotFoundException;

/**
 * Represents a single chunk, as used by Minecraft.
 *
 */
public final class AnvilChunk implements Chunk {

    public static final int CHUNK_X_SIZE = 16;
    public static final int CHUNK_Z_SIZE = 16;
    public static final int CHUNK_SECTION_Y_SIZE = 16;

    /**
     * The highest possible biome id in a Minecraft map.
     */
    public static final int MAX_BIOME_ID = 254;

    private final CompoundTag chunkTag;
    private final AnvilGameFactory gameFactory;
    private final ChunkBlocks chunkSections;
    private final ChunkDataVersion version;
    private Optional<ListTag<CompoundTag>> entityTag = Optional.empty();

    private final RegionNbtIo tagIo;

    /**
     * Creates a new chunk from the given data tag.
     *
     * @param gameFactory
     *            Game factory, for interpreting the raw data.
     * @param nbtIo
     *            Loader/saver for the various compound tags of the chunk.
     * @throws IOException
     *             If the chunk fails to load.
     */
    AnvilChunk(AnvilGameFactory gameFactory, RegionNbtIo nbtIo) throws IOException {
        this.gameFactory = Objects.requireNonNull(gameFactory, "gameFactory");
        this.tagIo = Objects.requireNonNull(nbtIo, "nbtIo");

        CompoundTag chunkRootTag = nbtIo.loadTag(RegionFileType.CHUNK)
                .orElseGet(nbtIo::createEmptyChunkRootTag);
        this.chunkTag = chunkRootTag.getCompound(ChunkRootTag.MINECRAFT);
        this.version = ChunkDataVersion.fromId(chunkRootTag.getInt(ChunkRootTag.DATA_VERSION));

        this.chunkSections = ChunkBlocks.create(version, gameFactory.getMaterialMap());
    }

    private void checkOutOfBounds(int x, int y, int z) {
        if (isOutOfBounds(x, y, z)) {
            throw new IndexOutOfBoundsException("(" + x + "," + y + "," + z
                    + ") is outside the chunk, which ranges from (0,?,0) to ("
                    + CHUNK_X_SIZE + ",?," + CHUNK_Z_SIZE + ")");
        }
    }

    /**
     * Gets direct access to the biome array of this chunk. Modifying the byte array
     * will modify the data of this chunk.
     *
     * @return The biome array.
     */
    public byte[] getBiomeArray() {
        return chunkTag.getByteArray(ChunkTag.BIOMES, CHUNK_X_SIZE * CHUNK_Z_SIZE);
    }

    /**
     * Gets the tags of the chunk sections.
     *
     * @return The tags.
     */
    public ListTag<CompoundTag> getChunkSections() {
        return chunkTag.getList(ChunkTag.SECTIONS, TagType.COMPOUND);
    }

    @Override
    public int getChunkX() {
        return chunkTag.getInt(ChunkTag.X_POS);
    }

    @Override
    public int getChunkZ() {
        return chunkTag.getInt(ChunkTag.Z_POS);
    }

    @Override
    public int getDepth() {
        int minSectionIndex = Integer.MAX_VALUE;
        for (CompoundTag section : this.getChunkSections()) {
            if (!section.containsKey(SectionTag.BLOCK_STATES) && !section.containsKey(OldSectionTag.BLOCK_IDS)) {
                continue; // For some reason there's a section at sectionY = -5 that is always empty
            }
            int sectionIndex = section.getByte(SectionTag.INDEX);
            minSectionIndex = Math.min(sectionIndex, minSectionIndex);
        }

        if (minSectionIndex == Integer.MIN_VALUE) {
            return 0; // Don't know, probably a weird old chunk
        }
        return minSectionIndex * CHUNK_SECTION_Y_SIZE;
    }

    @Override
    public ListTag<CompoundTag> getEntities() throws IOException {
        if (this.version.isBefore(ChunkDataVersion.MINECRAFT_ENTITY_SEPARATION)) {
            return chunkTag.getList(ChunkTag.ENTITIES, TagType.COMPOUND);
        }
        if (this.entityTag.isPresent()) {
            // Cached
            return this.entityTag.get();
        }

        // Load & cache
        ListTag<CompoundTag> entities = this.tagIo.loadTag(RegionFileType.ENTITY)
                .map(tag -> tag.getList(EntitiesRootTag.ENTITIES, TagType.COMPOUND))
                .orElse(new ListTag<>(TagType.COMPOUND));
        this.entityTag = Optional.of(entities);
        return entities;
    }

    @Override
    public AnvilGameFactory getGameFactory() {
        return gameFactory;
    }

    @Override
    public int getHeight() {
        int maxSectionIndex = Integer.MIN_VALUE;
        for (CompoundTag section : this.getChunkSections()) {
            if (!section.containsKey(SectionTag.BLOCK_STATES) && !section.containsKey(OldSectionTag.BLOCK_IDS)) {
                continue;
            }
            int sectionIndex = section.getByte(SectionTag.INDEX);
            maxSectionIndex = Math.max(sectionIndex, maxSectionIndex);
        }

        if (maxSectionIndex == Integer.MIN_VALUE) {
            return 256; // Don't know, probably a weird old chunk
        }
        return maxSectionIndex * CHUNK_SECTION_Y_SIZE + CHUNK_SECTION_Y_SIZE;
    }

    @Override
    public MaterialData getMaterial(int x, int y, int z) throws MaterialNotFoundException {
        checkOutOfBounds(x, y, z);

        return chunkSections.getMaterial(chunkTag, x, y, z);
    }

    @Override
    public int getSizeX() {
        return CHUNK_X_SIZE;
    }

    @Override
    public int getSizeZ() {
        return CHUNK_Z_SIZE;
    }

    @Override
    public CompoundTag getTag() {
        return chunkTag;
    }

    @Override
    public List<CompoundTag> getTileEntities() {
        return chunkTag.getList(ChunkTag.TILE_ENTITIES, TagType.COMPOUND);
    }

    /**
     * Gets the Minecraft version used to save this chunk.
     *
     * @return The version.
     */
    public ChunkDataVersion getVersion() {
        return version;
    }

    boolean isEntityFileLoaded() {
        if (version.isBefore(ChunkDataVersion.MINECRAFT_ENTITY_SEPARATION)) {
            return false; // Will never be loaded from a separate file
        }
        return this.entityTag.isPresent();
    }

    @Override
    public boolean isOutOfBounds(int x, int y, int z) {
        return x < 0 || x >= CHUNK_X_SIZE || z < 0 || z >= CHUNK_Z_SIZE;
    }

    /**
     * Saves all data of the chunk to disk. Should be called through ChunkAccess or
     * ChunkWalk, to avoid problems with file locks.
     *
     * @throws IOException
     *             If saving fails.
     */
    void save() throws IOException {

        // Save main data
        CompoundTag root = new CompoundTag();
        root.setCompound(ChunkRootTag.MINECRAFT, getTag());
        root.setInt(ChunkRootTag.DATA_VERSION, getVersion().getId());
        tagIo.saveTag(RegionFileType.CHUNK, root);

        // Save entities (or delete them)
        if (isEntityFileLoaded()) {
            ListTag<CompoundTag> entities = getEntities();
            if (entities.isEmpty()) {
                // Delete them
                tagIo.deleteTag(RegionFileType.ENTITY, new CompoundTag());
            } else {
                // Save them
                CompoundTag entityRoot = new CompoundTag();
                entityRoot.setList(EntitiesRootTag.ENTITIES, entities);
                entityRoot.setIntArray(EntitiesRootTag.POSITION, new int[] { getChunkX(), getChunkZ() });
                entityRoot.setInt(EntitiesRootTag.DATA_VERSION, getVersion().getId());
                tagIo.saveTag(RegionFileType.ENTITY, entityRoot);
            }
        }

        // Save point of interest data

    }

    @Override
    public void setMaterial(int x, int y, int z, MaterialData materialData) throws MaterialNotFoundException {
        checkOutOfBounds(x, y, z);

        chunkSections.setMaterial(chunkTag, x, y, z, materialData);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + getChunkX() + "," + getChunkZ() + ")";
    }
}
