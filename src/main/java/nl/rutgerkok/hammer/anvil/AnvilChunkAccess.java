package nl.rutgerkok.hammer.anvil;

import java.io.IOException;
import java.io.OutputStream;

import nl.rutgerkok.hammer.ChunkAccess;
import nl.rutgerkok.hammer.anvil.RegionFileCache.Claim;
import nl.rutgerkok.hammer.anvil.tag.AnvilFormat.ChunkRootTag;
import nl.rutgerkok.hammer.anvil.tag.AnvilFormat.EntitiesRootTag;
import nl.rutgerkok.hammer.anvil.tag.AnvilNbtWriter;
import nl.rutgerkok.hammer.tag.CompoundTag;
import nl.rutgerkok.hammer.tag.ListTag;

/**
 * Provides non-sequential access to the chunks in a world.
 *
 */
final class AnvilChunkAccess implements ChunkAccess<AnvilChunk> {

    private final RegionFileCache cache;
    private final Claim claim;
    private final AnvilGameFactory gameFactory;

    public AnvilChunkAccess(AnvilGameFactory gameFactory, RegionFileCache cache) {
        this.gameFactory = gameFactory;
        this.cache = cache;
        this.claim = cache.claim();
    }

    @Override
    public void close() {
        claim.close();
    }

    @Override
    public void deleteChunk(AnvilChunk chunk) throws IOException {
        new RegionNbtIo(ChunkDataVersion.latest(), cache, chunk.getChunkX(), chunk.getChunkZ()).deleteAllDataOfChunk();
    }

    @Override
    public AnvilChunk getChunk(int chunkX, int chunkZ) throws IOException {
        return new AnvilChunk(gameFactory, new RegionNbtIo(ChunkDataVersion.latest(), cache, chunkX, chunkZ));
    }

    private OutputStream getChunkOutputStream(RegionFileType type, int chunkX, int chunkZ) throws IOException {
        RegionFile regionFile = cache.getRegionFile(type, chunkX, chunkZ);
        return regionFile.getChunkOutputStream(chunkX & 31, chunkZ & 31);
    }

    @Override
    public void saveChunk(AnvilChunk chunk) throws IOException {
        int chunkX = chunk.getChunkX();
        int chunkZ = chunk.getChunkZ();

        try (OutputStream outputStream = getChunkOutputStream(RegionFileType.CHUNK, chunkX, chunkZ)) {
            CompoundTag root = new CompoundTag();
            root.setCompound(ChunkRootTag.MINECRAFT, chunk.getTag());
            AnvilNbtWriter.writeUncompressedToStream(outputStream, root);
        }

        // Save entities (or delete them)
        if (chunk.isEntityFileLoaded()) {
            ListTag<CompoundTag> entities = chunk.getEntities();
            if (entities.isEmpty()) {
                this.cache.getRegionFile(RegionFileType.ENTITY, chunkX, chunkZ);
                return;
            }
            try (OutputStream outputStream = getChunkOutputStream(RegionFileType.ENTITY, chunkX, chunkZ)) {
                CompoundTag root = new CompoundTag();
                root.setList(EntitiesRootTag.ENTITIES, entities);
                root.setIntArray(EntitiesRootTag.POSITION, new int[] { chunkX, chunkZ });
                root.setInt(EntitiesRootTag.DATA_VERSION, chunk.getVersion().getId());
                AnvilNbtWriter.writeUncompressedToStream(outputStream, root);
            }
        }
    }
}