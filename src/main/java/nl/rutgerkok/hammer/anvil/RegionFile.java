package nl.rutgerkok.hammer.anvil;

/*
 * * 2011 January 5** The author disclaims copyright to this source code. In
 * place of* a legal notice, here is a blessing:** May you do good and not evil.
 * * May you find forgiveness for yourself and forgive others.* May you share
 * freely, never taking more than you give.
 */

/*
 * 2011 February 16
 * 
 * This source code is based on the work of Scaevolus (see notice above). It has
 * been slightly modified by Mojang AB (constants instead of magic numbers, a
 * chunk timestamp header, and auto-formatted according to our formatter
 * template).
 */

// Interfaces with region files on the disk

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import nl.rutgerkok.hammer.util.BooleanList;

/**
 *
 * Region File Format
 *
 * <p>
 * Concept: The minimum unit of storage on hard drives is 4KB. 90% of Minecraft
 * chunks are smaller than 4KB. 99% are smaller than 8KB. Write a simple
 * container to store chunks in single files in runs of 4KB sectors.
 *
 * <p>
 * Each region file represents a {@value #REGION_CHUNK_COUNT}x
 * {@value #REGION_CHUNK_COUNT} group of chunks. The conversion from chunk
 * number to region number is floor(coord / {@value #REGION_CHUNK_COUNT}): a
 * chunk at (30, -3) would be in region (0, -1), and one at (70, -30) would be
 * at (3, -1). Region files are named "r.x.z.data", where x and z are the region
 * coordinates.
 *
 * <p>
 * A region file begins with a 4KB header that describes where chunks are stored
 * in the file. A 4-byte big-endian integer represents sector offsets and sector
 * counts. The chunk offset for a chunk (x, z) begins at byte 4*(x+z*32) in the
 * file. The bottom byte of the chunk offset indicates the number of sectors the
 * chunk takes up, and the top 3 bytes represent the sector number of the chunk.
 * Given a chunk offset o, the chunk data begins at byte 4096*(o/256) and takes
 * up at most 4096*(o%256) bytes. A chunk cannot exceed 1MB in size. If a chunk
 * offset is 0, the corresponding chunk is not stored in the region file.
 *
 * <p>
 * Chunk data begins with a 4-byte big-endian integer representing the chunk
 * data length in bytes, not counting the length field. The length must be
 * smaller than 4096 times the number of sectors. The next byte is a version
 * field, to allow backwards-compatible updates to how chunks are encoded.
 *
 * <p>
 * A version of {@value #VERSION_GZIP} represents a gzipped NBT file. The
 * gzipped data is the chunk length - 1.
 *
 * <p>
 * A version of {@value #VERSION_DEFLATE} represents a deflated (zlib
 * compressed) NBT file. The deflated data is the chunk length - 1.
 */
public class RegionFile implements Closeable {

    /*
     * lets chunk writing be multithreaded by not locking the whole file as a
     * chunk is serializing -- only writes when serialization is over
     */
    class ChunkBuffer extends ByteArrayOutputStream {
        private int x, z;

        public ChunkBuffer(int x, int z) {
            super(8096); // initialize to 8KB
            this.x = x;
            this.z = z;
        }

        @Override
        public void close() {
            RegionFile.this.write(x, z, buf, count);
        }
    }

    static final int CHUNK_HEADER_BYTES = 5;

    private static final byte emptySector[] = new byte[4096];

    /**
     * The amount of chunks in a region file, on both the x and z axis.
     */
    public static final int REGION_CHUNK_COUNT = 32;

    private static final int SECTOR_BYTES = 4096;
    private static final int SECTOR_INTS = SECTOR_BYTES / 4;

    private static final int VERSION_DEFLATE = 2;
    private static final int VERSION_GZIP = 1;

    private final int chunkTimestamps[];
    private RandomAccessFile file;
    private final Path fileName;
    private long lastModified = 0;
    private final int offsets[];
    private BooleanList sectorFree;

    public RegionFile(Path path) {
        offsets = new int[SECTOR_INTS];
        chunkTimestamps = new int[SECTOR_INTS];

        fileName = path;
        debugln("REGION LOAD " + fileName);

        try {
            if (Files.exists(path)) {
                lastModified = Files.getLastModifiedTime(path).toMillis();
            }

            file = new RandomAccessFile(path.toFile(), "rw");

            if (file.length() < SECTOR_BYTES) {
                /* we need to write the chunk offset table */
                for (int i = 0; i < SECTOR_INTS; ++i) {
                    file.writeInt(0);
                }
                // write another sector for the timestamp info
                for (int i = 0; i < SECTOR_INTS; ++i) {
                    file.writeInt(0);
                }
            }

            if ((file.length() & 0xfff) != 0) {
                /* the file size is not a multiple of 4KB, grow it */
                for (int i = 0; i < (file.length() & 0xfff); ++i) {
                    file.write((byte) 0);
                }
            }

            /* set up the available sector map */
            int nSectors = (int) file.length() / SECTOR_BYTES;
            sectorFree = new BooleanList(nSectors);

            for (int i = 0; i < nSectors; ++i) {
                sectorFree.add(true);
            }

            sectorFree.set(0, false); // chunk offset table
            sectorFree.set(1, false); // for the last modified info

            file.seek(0);
            for (int i = 0; i < SECTOR_INTS; ++i) {
                int offset = file.readInt();
                offsets[i] = offset;
                if (offset != 0 && (offset >> 8) + (offset & 0xFF) <= sectorFree.size()) {
                    for (int sectorNum = 0; sectorNum < (offset & 0xFF); ++sectorNum) {
                        sectorFree.set((offset >> 8) + sectorNum, false);
                    }
                }
            }
            for (int i = 0; i < SECTOR_INTS; ++i) {
                int lastModValue = file.readInt();
                chunkTimestamps[i] = lastModValue;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* is this an invalid chunk coordinate? */
    private void checkBounds(int x, int z) {
        if (x < 0 || x >= REGION_CHUNK_COUNT || z < 0 || z >= REGION_CHUNK_COUNT) {
            throw new IndexOutOfBoundsException("out of bounds; x,z: " + x + "," + z);
        }
    }

    @Override
    public void close() throws IOException {
        file.close();
    }

    // various small debug printing helpers
    private void debug(String in) {
        // System.out.print(in);
    }

    private void debug(String mode, int x, int z, int count, String in) {
        debug("REGION " + mode + " " + fileName.getName(fileName.getNameCount() - 1) + "[" + x + "," + z + "] " + count + "B = " + in);
    }

    private void debug(String mode, int x, int z, String in) {
        debug("REGION " + mode + " " + fileName.getName(fileName.getNameCount() - 1) + "[" + x + "," + z + "] = " + in);
    }

    private void debugln(String in) {
        debug(in + "\n");
    }

    private void debugln(String mode, int x, int z, String in) {
        debug(mode, x, z, in + "\n");
    }

    /**
     * Deletes the chunk at the given x and z, marking the sectors as free and
     * deleting the chunks from the index.
     *
     * @param x
     *            X of the chunk in the region file.
     * @param z
     *            Z of the chunk in the region file.
     * @throws IOException
     *             If an IO error occurs.
     */
    public void deleteChunk(int x, int z) throws IOException {
        checkBounds(x, z);

        if (!hasChunk(x, z)) {
            return;
        }

        int offset = getOffset(x, z);
        int sectorNumber = offset >> 8;
        int numSectors = offset & 0xFF;

        // Check for file corruption
        if (sectorNumber + numSectors > sectorFree.size()) {
            debugln("READ", x, z, "invalid sector");
            return;
        }

        // Reset offset and timestamp
        setOffset(x, z, 0);
        setTimestamp(x, z, 0);

        // Mark sectors as free
        for (int i = 0; i < numSectors; i++) {
            sectorFree.set(sectorNumber + i, true);
        }
    }

    public OutputStream getChunkDataOutputStream(int x, int z) {
        checkBounds(x, z);

        return new DeflaterOutputStream(new ChunkBuffer(x, z));
    }

    /**
     * Gets an (uncompressed) stream representing the chunk data returns null if
     * the chunk is not found.
     *
     * @param chunkX
     *            The chunk x in the region file.
     * @param chunkZ
     *            The chunk z in the region file.
     * @return The stream, or null if there is no chunk.
     * @throws IOException
     *             If an IO error occurs.
     */
    public synchronized InputStream getChunkInputStream(int chunkX, int chunkZ) throws IOException {
        checkBounds(chunkX, chunkZ);

        int offset = getOffset(chunkX, chunkZ);
        if (offset == 0) {
            // debugln("READ", x, z, "miss");
            return null;
        }

        int sectorNumber = offset >> 8;
        int numSectors = offset & 0xFF;

        if (sectorNumber + numSectors > sectorFree.size()) {
            debugln("READ", chunkX, chunkZ, "invalid sector");
            return null;
        }

        file.seek(sectorNumber * SECTOR_BYTES);
        int length = file.readInt();

        if (length > SECTOR_BYTES * numSectors) {
            debugln("READ", chunkX, chunkZ, "invalid length: " + length + " > 4096 * " + numSectors);
            return null;
        }

        byte version = file.readByte();
        if (version == VERSION_GZIP) {
            byte[] data = new byte[length - 1];
            file.read(data);
            InputStream ret = new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(data)));
            // debug("READ", x, z, " = found");
            return ret;
        } else if (version == VERSION_DEFLATE) {
            byte[] data = new byte[length - 1];
            file.read(data);
            InputStream ret = new BufferedInputStream(new InflaterInputStream(new ByteArrayInputStream(data)));
            // debug("READ", x, z, " = found");
            return ret;
        }

        throw new IOException("Unknown version " + version);
    }

    private int getOffset(int x, int z) {
        return offsets[x + z * REGION_CHUNK_COUNT];
    }

    public boolean hasChunk(int x, int z) {
        return getOffset(x, z) != 0;
    }

    /* the modification date of the region file when it was first opened */
    public long lastModified() {
        return lastModified;
    }

    private void setOffset(int x, int z, int offset) throws IOException {
        offsets[x + z * REGION_CHUNK_COUNT] = offset;
        file.seek((x + z * REGION_CHUNK_COUNT) * 4);
        file.writeInt(offset);
    }

    private void setTimestamp(int x, int z, int value) throws IOException {
        chunkTimestamps[x + z * REGION_CHUNK_COUNT] = value;
        file.seek(SECTOR_BYTES + (x + z * REGION_CHUNK_COUNT) * 4);
        file.writeInt(value);
    }

    /* write a chunk data to the region file at specified sector number */
    private void write(int sectorNumber, byte[] data, int length) throws IOException {
        debugln(" " + sectorNumber);
        file.seek(sectorNumber * SECTOR_BYTES);
        file.writeInt(length + 1); // chunk length
        file.writeByte(VERSION_DEFLATE); // chunk version number
        file.write(data, 0, length); // chunk data
    }

    /* write a chunk at (x,z) with length bytes of data to disk */
    protected synchronized void write(int x, int z, byte[] data, int length) {
        try {
            int offset = getOffset(x, z);
            int sectorNumber = offset >> 8;
            int sectorsAllocated = offset & 0xFF;
            int sectorsNeeded = (length + CHUNK_HEADER_BYTES) / SECTOR_BYTES + 1;

            // maximum chunk size is 1MB
            if (sectorsNeeded >= 256) {
                return;
            }

            if (sectorNumber != 0 && sectorsAllocated == sectorsNeeded) {
                /* we can simply overwrite the old sectors */
                debug("SAVE", x, z, length, "rewrite");
                write(sectorNumber, data, length);
            } else {
                /* we need to allocate new sectors */

                /* mark the sectors previously used for this chunk as free */
                for (int i = 0; i < sectorsAllocated; ++i) {
                    sectorFree.set(sectorNumber + i, true);
                }

                /* scan for a free space large enough to store this chunk */
                int runStart = sectorFree.indexOf(true);
                int runLength = 0;
                if (runStart != -1) {
                    for (int i = runStart; i < sectorFree.size(); ++i) {
                        if (runLength != 0) {
                            if (sectorFree.get(i)) {
                                runLength++;
                            } else {
                                runLength = 0;
                            }
                        } else if (sectorFree.get(i)) {
                            runStart = i;
                            runLength = 1;
                        }
                        if (runLength >= sectorsNeeded) {
                            break;
                        }
                    }
                }

                if (runLength >= sectorsNeeded) {
                    /* we found a free space large enough */
                    debug("SAVE", x, z, length, "reuse");
                    sectorNumber = runStart;
                    setOffset(x, z, (sectorNumber << 8) | sectorsNeeded);
                    for (int i = 0; i < sectorsNeeded; ++i) {
                        sectorFree.set(sectorNumber + i, false);
                    }
                    write(sectorNumber, data, length);
                } else {
                    /*
                     * no free space large enough found -- we need to grow the
                     * file
                     */
                    debug("SAVE", x, z, length, "grow");
                    file.seek(file.length());
                    sectorNumber = sectorFree.size();
                    for (int i = 0; i < sectorsNeeded; ++i) {
                        file.write(emptySector);
                        sectorFree.add(false);
                    }

                    write(sectorNumber, data, length);
                    setOffset(x, z, (sectorNumber << 8) | sectorsNeeded);
                }
            }
            setTimestamp(x, z, (int) (System.currentTimeMillis() / 1000L));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}