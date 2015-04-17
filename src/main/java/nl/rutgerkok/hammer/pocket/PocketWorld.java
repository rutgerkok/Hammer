package nl.rutgerkok.hammer.pocket;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import nl.rutgerkok.hammer.PlayerFile;
import nl.rutgerkok.hammer.World;
import nl.rutgerkok.hammer.anvil.material.VanillaMaterialMap;
import nl.rutgerkok.hammer.material.MaterialMap;
import nl.rutgerkok.hammer.pocket.tag.PocketNbtReader;
import nl.rutgerkok.hammer.pocket.tag.PocketNbtWriter;
import nl.rutgerkok.hammer.tag.CompoundTag;
import nl.rutgerkok.hammer.util.Visitor;

public class PocketWorld implements World {

    private final Path levelDat;
    private final MaterialMap materialMap;

    private final CompoundTag rootLevelTag;

    /**
     * Creates a new world editor for the given world in Pocket Edition format.
     * 
     * @param levelDat
     *            The level.dat file of the Pocket Edition world.
     * @throws IOException
     *             If an IO error occurs reading the level.dat file.
     */
    public PocketWorld(Path levelDat) throws IOException {
        this.levelDat = Objects.requireNonNull(levelDat, "level.dat");
        this.rootLevelTag = PocketNbtReader.readFromUncompressedFile(levelDat);

        // Use the same material map as Anvil for now
        this.materialMap = new VanillaMaterialMap();
    }

    @Override
    public CompoundTag getLevelTag() {
        // In the pocket edition, the root tag is the tag with all the
        // information
        return rootLevelTag;
    }

    @Override
    public MaterialMap getMaterialMap() {
        return materialMap;
    }

    @Override
    public void saveLevelTag() throws IOException {
        PocketNbtWriter.writeUncompressedToFile(levelDat, rootLevelTag);
    }

    @Override
    public void walkPlayerFiles(Visitor<PlayerFile> visitor) throws IOException {
        // TODO Auto-generated method stub

    }

}
