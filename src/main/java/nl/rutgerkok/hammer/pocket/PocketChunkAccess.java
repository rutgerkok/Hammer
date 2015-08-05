package nl.rutgerkok.hammer.pocket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterators;

import nl.rutgerkok.hammer.ChunkAccess;
import nl.rutgerkok.hammer.pocket.PocketLevelDb.ChunkKeyType;
import nl.rutgerkok.hammer.pocket.tag.PocketNbtReader;
import nl.rutgerkok.hammer.pocket.tag.PocketNbtWriter;
import nl.rutgerkok.hammer.tag.CompoundTag;

/**
 * Provides access to the chunks stored in LevelDb.
 *
 */
final class PocketChunkAccess implements ChunkAccess<PocketChunk>, Iterable<PocketChunk> {

    private final Closeable claim;
    private final PocketGameFactory gameFactory;
    private boolean open = true;
    private final PocketLevelDb pocketLevelDb;

    public PocketChunkAccess(PocketGameFactory gameFactory, PocketLevelDb pocketLevelDb) throws IOException {
        this.gameFactory = Objects.requireNonNull(gameFactory, "gameFactory");
        this.pocketLevelDb = Objects.requireNonNull(pocketLevelDb, "pocketLevelDb");

        this.claim = pocketLevelDb.claim();
    }

    private void checkState() {
        Preconditions.checkState(open, "Access to database is closed");
    }

    @Override
    public void close() throws IOException {
        open = false;
        claim.close();
    }

    void deleteChunk(PocketChunk chunk) {
        checkState();

        int chunkX = chunk.getChunkX();
        int chunkZ = chunk.getChunkZ();
        for (ChunkKeyType keyType : ChunkKeyType.values()) {
            pocketLevelDb.deleteBytes(keyType, chunkX, chunkZ);
        }
    }

    @Override
    public PocketChunk getChunk(int chunkX, int chunkZ) throws IOException {
        checkState();

        byte[] terrainData = pocketLevelDb.getBytes(ChunkKeyType.TERRAIN, chunkX, chunkZ);
        if (terrainData == null) {
            // Return empty chunk
            return PocketChunk.newEmptyChunk(gameFactory, chunkX, chunkZ);
        }
        return getRemainingDataForChunk(chunkX, chunkZ, terrainData);
    }

    PocketChunk getRemainingDataForChunk(int chunkX, int chunkZ, byte[] terrainData) throws IOException {
        byte[] entityBytes = pocketLevelDb.getBytes(ChunkKeyType.ENTITY, chunkX, chunkZ);
        byte[] tileEntityBytes = pocketLevelDb.getBytes(ChunkKeyType.TILE_ENTITY, chunkX, chunkZ);
        return new PocketChunk(gameFactory, chunkX, chunkZ, terrainData,
                readFromBytes(entityBytes), readFromBytes(tileEntityBytes));
    }

    @Override
    public Iterator<PocketChunk> iterator() {
        checkState();

        Iterator<PocketChunk> chunkIterator = Iterators.transform(pocketLevelDb.iterator(), new Function<Entry<byte[], byte[]>, PocketChunk>() {

            @Override
            public PocketChunk apply(Entry<byte[], byte[]> entry) {
                // Examine the key
                byte[] key = entry.getKey();
                ChunkKeyType type = pocketLevelDb.getChunkKeyTypeOrNull(key);
                if (type != ChunkKeyType.TERRAIN) {
                    return null;
                }

                // Get chunk data
                byte[] terrainData = entry.getValue();
                int chunkX = pocketLevelDb.getChunkX(key);
                int chunkZ = pocketLevelDb.getChunkZ(key);
                try {
                    return getRemainingDataForChunk(chunkX, chunkZ, terrainData);
                } catch (IOException e) {
                    throw Throwables.propagate(e);
                }
            }
        });
        return Iterators.filter(chunkIterator, Predicates.notNull());
    }

    private List<CompoundTag> readFromBytes(byte[] bytesOrNull) throws IOException {
        if (bytesOrNull == null) {
            return new ArrayList<>();
        }
        ByteArrayInputStream stream = new ByteArrayInputStream(bytesOrNull);
        List<CompoundTag> tags = new ArrayList<>();
        while (stream.available() > 0) {
            tags.add(PocketNbtReader.readFromUncompressedStream(stream));
        }
        return tags;
    }

    @Override
    public void saveChunk(PocketChunk chunk) throws IOException {
        checkState();

        int chunkX = chunk.getChunkX();
        int chunkZ = chunk.getChunkZ();

        byte[] terrain = chunk.accessBytes();
        pocketLevelDb.putBytes(ChunkKeyType.TERRAIN, chunkX, chunkZ, terrain);

        byte[] entities = writeToBytes(chunk.getEntities());
        pocketLevelDb.putBytes(ChunkKeyType.ENTITY, chunkX, chunkZ, entities);

        byte[] tileEntities = writeToBytes(chunk.getTileEntities());
        pocketLevelDb.putBytes(ChunkKeyType.ENTITY, chunkX, chunkZ, tileEntities);
    }

    private byte[] writeToBytes(List<CompoundTag> tags) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        for (CompoundTag tag : tags) {
            PocketNbtWriter.writeUncompressedToStream(stream, tag);
        }
        return stream.toByteArray();
    }

}
