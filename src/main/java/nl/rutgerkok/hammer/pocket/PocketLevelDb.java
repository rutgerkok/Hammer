package nl.rutgerkok.hammer.pocket;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Objects;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;

import com.google.common.base.Preconditions;

/**
 * Pocket Edition stores everything (excluding the level.dat file) as key=>value
 * pairs in LevelDB.
 *
 * <h3>Chunks</h3>
 * <p>
 * <code>(int chunkX, int chunkZ, byte chunkKeyType) => (chunk data)</code>
 * ChunkKeyType is equal to one of the values in {@link ChunkKeyType}. Chunk
 * data is NBT for (tile) entities, a single byte for the version or a long
 * array for the terrain data.
 * </p>
 *
 * <h3>Players</h3>
 * <p>
 * Unknown.
 * </p>
 */
class PocketLevelDb implements Iterable<Entry<byte[], byte[]>> {

    enum ChunkKeyType {
        ENTITY('2'),
        TERRAIN('0'),
        TILE_ENTITY('1'),
        VERSION('v');

        private static ChunkKeyType[] values;
        static {
            values = values();
        }

        private static ChunkKeyType ofByte(byte type) {
            for (ChunkKeyType keyType : values) {
                if (keyType.byteType == type) {
                    return keyType;
                }
            }
            return null;
        }

        private final byte byteType;

        private ChunkKeyType(char type) {
            this.byteType = (byte) type;
        }
    }

    class Lock implements Closeable {

        private Lock() {
            // Instantiated only by parent class
        }

        @Override
        public void close() throws IOException {
            release();
        }

    }

    private int claims = 0;
    private final Path databaseDirectory;
    private DB db = null;

    PocketLevelDb(Path databaseDirectory) {
        this.databaseDirectory = databaseDirectory;
    }

    /**
     * This method needs to be called to make sure the database is open.
     * 
     * @return
     * @throws IOException
     */
    Closeable claim() throws IOException {
        claims++;
        if (db == null) {
            db = Iq80DBFactory.factory.open(databaseDirectory.toFile(), new Options());
        }
        return new Lock();
    }

    private byte[] createChunkKey(ChunkKeyType keyType, int chunkX, int chunkZ) {
        byte[] bytes = new byte[9];
        writeInt(bytes, 0, chunkX);
        writeInt(bytes, 4, chunkZ);
        bytes[8] = (byte) keyType.byteType;
        return bytes;
    }

    /**
     * Deletes the bytes for the given chunk and key type.
     * 
     * @param keyType
     *            The key type.
     * @param chunkX
     *            The chunk x.
     * @param chunkZ
     *            The chunk z.
     */
    void deleteBytes(ChunkKeyType keyType, int chunkX, int chunkZ) {
        getDb().delete(createChunkKey(keyType, chunkX, chunkZ));
    }

    @Override
    public void finalize() {
        if (claims != 0) {
            System.err.println(getClass().getSimpleName() + " was not closed - there's a resource leak somewhere!");
        }
    }

    /**
     * Gets the raw bytes for the given chunk and key type.
     * 
     * @param keyType
     *            The key type.
     * @param chunkX
     *            The chunk x.
     * @param chunkZ
     *            The chunk z.
     * @return The bytes.
     */
    byte[] getBytes(ChunkKeyType keyType, int chunkX, int chunkZ) {
        return getDb().get(createChunkKey(keyType, chunkX, chunkZ));
    }

    /**
     * Gets the {@link ChunkKeyType} from the given key.
     * 
     * @param key
     *            The key.
     * @return The key type, or null if the key is not the key of a chunk.
     */
    ChunkKeyType getChunkKeyTypeOrNull(byte[] key) {
        if (key.length != 9) {
            return null;
        }
        return ChunkKeyType.ofByte(key[8]);
    }

    /**
     * Gets the chunk x from the chunk key.
     * 
     * @param key
     *            The chunk key.
     * @return The chunk x.
     * @throws IllegalArgumentException
     *             If the key is not a chunk key.
     */
    int getChunkX(byte[] key) {
        if (key.length != 9) {
            throw new IllegalArgumentException("Not a chunk key");
        }
        return readInt(key, 0);
    }

    int getChunkZ(byte[] key) {
        if (key.length != 9) {
            throw new IllegalArgumentException("Not a chunk key");
        }
        return readInt(key, 4);
    }

    private DB getDb() {
        DB db = this.db;
        Preconditions.checkState(db != null, "Database not open, forgot to call claim()?");
        return db;
    }

    @Override
    public Iterator<Entry<byte[], byte[]>> iterator() {
        return getDb().iterator();
    }

    /**
     * Puts the given bytes for the given chunk and key type.
     * 
     * @param keyType
     *            The key type.
     * @param chunkX
     *            The chunk x.
     * @param chunkZ
     *            The chunk z.
     * @param bytes
     *            The bytes to put.
     */
    void putBytes(ChunkKeyType keyType, int chunkX, int chunkZ, byte[] bytes) {
        Objects.requireNonNull(bytes, "bytes");
        getDb().put(createChunkKey(keyType, chunkZ, chunkZ), bytes);
    }

    private int readInt(byte[] bytes, int offset) {
        return (bytes[offset] & 0xff) | ((bytes[offset + 1] & 0xff) << 8) |
                ((bytes[offset + 2] & 0xff) << 16) | ((bytes[offset + 3] & 0xff) << 24);
    }

    private void release() throws IOException {
        if (claims == 1) {
            // The last claim was just released, stop the database
            claims = 0;
            getDb().close();
        } else if (claims <= 0) {
            throw new IllegalStateException("Already closed all claims");
        } else {
            claims--;
        }
    }

    private void writeInt(byte[] array, int offset, int number) {
        array[offset] = (byte) (number & 0x0000_00ff);
        array[offset + 1] = (byte) ((number & 0x0000_ff00) >>> 8);
        array[offset + 2] = (byte) ((number & 0x00ff_0000) >>> 16);
        array[offset + 3] = (byte) ((number & 0xff00_0000) >>> 24);
    }

}
