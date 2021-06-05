package nl.rutgerkok.hammer.anvil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.Optional;

import nl.rutgerkok.hammer.anvil.tag.AnvilFormat.ChunkRootTag;
import nl.rutgerkok.hammer.anvil.tag.AnvilFormat.ChunkTag;
import nl.rutgerkok.hammer.anvil.tag.AnvilNbtReader;
import nl.rutgerkok.hammer.anvil.tag.AnvilNbtWriter;
import nl.rutgerkok.hammer.tag.CompoundTag;

/**
 * Allows to read and write NBT tags from the region files.
 */
final class RegionNbtIo {

    private final ChunkDataVersion defaultDataVersion;
    private final RegionFileCache cache;
    private final int chunkX;
    private final int chunkZ;

    RegionNbtIo(ChunkDataVersion defaultDataVersion, RegionFileCache cache, int chunkX, int chunkZ) {
        this.defaultDataVersion = Objects.requireNonNull(defaultDataVersion, "defaultDataVersion");
        this.cache = Objects.requireNonNull(cache, "cache");
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    /**
     * Creates a new, empty chunk root tag.
     *
     * @return The chunk.
     */
    CompoundTag createEmptyChunkRootTag() {

        CompoundTag chunkTag = new CompoundTag();
        chunkTag.setInt(ChunkTag.X_POS, chunkX);
        chunkTag.setInt(ChunkTag.Z_POS, chunkZ);

        CompoundTag chunkRootTag = new CompoundTag();
        chunkRootTag.setInt(ChunkRootTag.DATA_VERSION, defaultDataVersion.getId());
        chunkRootTag.setCompound(ChunkRootTag.MINECRAFT, chunkTag);
        return chunkRootTag;
    }

    /**
     * Deletes all data of the chunk (including points of interests and entities).
     *
     * @throws IOException
     *             If an IO error occurs.
     */
    void deleteAllDataOfChunk() throws IOException {
        for (RegionFileType type : RegionFileType.values()) {
            cache.getRegionFile(type, chunkX, chunkZ).deleteChunk(chunkX & 31, chunkZ & 31);
        }
    }

    void deleteTag(RegionFileType type, CompoundTag tag) throws IOException {
        cache.getRegionFile(type, chunkX, chunkZ).deleteChunk(chunkX & 31, chunkZ & 31);
    }

    private OutputStream getChunkOutputStream(RegionFileType type, int chunkX, int chunkZ) throws IOException {
        RegionFile regionFile = cache.getRegionFile(type, chunkX, chunkZ);
        return regionFile.getChunkOutputStream(chunkX & 31, chunkZ & 31);
    }

    Optional<CompoundTag> loadTag(RegionFileType type) throws IOException {
        try (InputStream stream = cache.getRegionFile(type, chunkX, chunkZ)
                .getChunkInputStream(chunkX & 31, chunkZ & 31)) {
            if (stream == null) {
                // Chunk doesn't exist yet
                return Optional.empty();
            }

            // Read the tag
            return Optional.of(AnvilNbtReader.readFromUncompressedStream(stream));
        }
    }

    void saveTag(RegionFileType type, CompoundTag tag) throws IOException {
        try (OutputStream outputStream = getChunkOutputStream(type, chunkX, chunkZ)) {
            AnvilNbtWriter.writeUncompressedToStream(outputStream, tag);
        }
    }

}
