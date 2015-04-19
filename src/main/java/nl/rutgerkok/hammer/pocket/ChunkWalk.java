package nl.rutgerkok.hammer.pocket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

import nl.rutgerkok.hammer.GameFactory;
import nl.rutgerkok.hammer.pocket.PocketLevelDb.ChunkKeyType;
import nl.rutgerkok.hammer.pocket.tag.PocketNbtReader;
import nl.rutgerkok.hammer.pocket.tag.PocketNbtWriter;
import nl.rutgerkok.hammer.tag.CompoundTag;
import nl.rutgerkok.hammer.util.Progress;
import nl.rutgerkok.hammer.util.Result;
import nl.rutgerkok.hammer.util.Visitor;

class ChunkWalk {

    private final GameFactory gameFactory;
    private final PocketLevelDb pocketLevelDb;

    ChunkWalk(GameFactory gameFactory, PocketLevelDb db) {
        this.gameFactory = Objects.requireNonNull(gameFactory, "gameFactory");
        this.pocketLevelDb = Objects.requireNonNull(db, "db");
    }

    private void deleteChunk(PocketChunk chunk) {
        int chunkX = chunk.getChunkX();
        int chunkZ = chunk.getChunkZ();
        for (ChunkKeyType keyType : ChunkKeyType.values()) {
            pocketLevelDb.deleteBytes(keyType, chunkX, chunkZ);
        }
    }

    void forEach(Visitor<? super PocketChunk> visitor) throws IOException {
        try (Closeable claim = pocketLevelDb.claim()) {
            Progress progress = Progress.complete();
            for (Entry<byte[], byte[]> entry : pocketLevelDb) {
                PocketChunk chunk = getPocketChunkOrNull(entry);
                if (chunk == null) {
                    continue;
                }

                Result result = visitor.accept(chunk, progress);
                switch (result) {
                    case CHANGED:
                        saveChunk(chunk);
                        break;
                    case DELETE:
                        deleteChunk(chunk);
                        break;
                    case NO_CHANGES:
                        break;
                    default:
                        throw new AssertionError("Unknown result " + result);
                }
            }
        }
    }

    private PocketChunk getPocketChunkOrNull(Entry<byte[], byte[]> entry) throws IOException {
        byte[] key = entry.getKey();
        ChunkKeyType type = pocketLevelDb.getChunkKeyTypeOrNull(key);
        if (type != ChunkKeyType.TERRAIN) {
            return null;
        }

        // Get chunk data
        int chunkX = pocketLevelDb.getChunkX(key);
        int chunkZ = pocketLevelDb.getChunkZ(key);
        byte[] bytes = entry.getValue();
        byte[] entityBytes = pocketLevelDb.getBytes(ChunkKeyType.ENTITY, chunkX, chunkZ);
        byte[] tileEntityBytes = pocketLevelDb.getBytes(ChunkKeyType.TILE_ENTITY, chunkX, chunkZ);
        return new PocketChunk(gameFactory, chunkZ, chunkZ, bytes,
                readFromBytes(entityBytes), readFromBytes(tileEntityBytes));
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

    private void saveChunk(PocketChunk chunk) throws IOException {
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
