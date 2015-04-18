package nl.rutgerkok.hammer.anvil;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import nl.rutgerkok.hammer.PlayerFile;
import nl.rutgerkok.hammer.anvil.tag.AnvilNbtReader;
import nl.rutgerkok.hammer.anvil.tag.AnvilNbtWriter;
import nl.rutgerkok.hammer.anvil.tag.AnvilTagFormat;
import nl.rutgerkok.hammer.tag.CompoundTag;
import nl.rutgerkok.hammer.util.DirectoryUtil;
import nl.rutgerkok.hammer.util.Progress;
import nl.rutgerkok.hammer.util.Progress.UnitsProgress;
import nl.rutgerkok.hammer.util.Result;
import nl.rutgerkok.hammer.util.Visitor;

/**
 * This class controls the walking along all the player files. Just create an
 * instance of it and invoke {@link #forEach(Visitor)}.
 *
 * @see AnvilWorld#walkPlayerFiles(Visitor) The public API.
 */
class AnvilPlayerFilesWalk {

    private final AnvilWorld world;

    AnvilPlayerFilesWalk(AnvilWorld world) {
        this.world = Objects.requireNonNull(world);
    }

    private int calculateotalUnits() throws IOException {
        int size = DirectoryUtil.countFiles(world.getPlayerDirectory());
        if (world.getLevelTag().containsKey(AnvilTagFormat.LEVEL_PLAYER_TAG)) {
            size++;
        }

        return size;
    }

    void forEach(Visitor<PlayerFile> consumer) throws IOException {
        UnitsProgress progress = Progress.ofUnits(calculateotalUnits());
        walkPlayerFiles(consumer, progress);
        walkLevelDatTag(consumer, progress);
    }

    private void walkLevelDatTag(Visitor<PlayerFile> consumer, UnitsProgress progress) throws IOException {
        CompoundTag levelTag = world.getLevelTag();
        if (!levelTag.containsKey(AnvilTagFormat.LEVEL_PLAYER_TAG)) {
            return;
        }

        CompoundTag playerTag = levelTag.getCompound(AnvilTagFormat.LEVEL_PLAYER_TAG);
        PlayerFile playerFile = new PlayerFile(world.getMaterialMap(), playerTag);
        Result result = consumer.accept(playerFile, progress);
        switch (result) {
            case CHANGED:
                world.saveLevelTag();
                break;
            case DELETE:
                playerTag.clear();
                world.saveLevelTag();
                break;
            case NO_CHANGES:
                break;
            default:
                throw new AssertionError("Unknown result: " + result);
        }
        progress.increment();
    }

    private void walkPlayerFiles(Visitor<PlayerFile> consumer, UnitsProgress progress) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(world.getPlayerDirectory())) {
            for (Path file : stream) {
                CompoundTag tag = AnvilNbtReader.readFromCompressedFile(file);
                PlayerFile playerFile = new PlayerFile(world.getMaterialMap(), tag);
                Result result = consumer.accept(playerFile, progress);
                switch (result) {
                    case CHANGED:
                        AnvilNbtWriter.writeCompressedToFile(file, tag);
                        break;
                    case DELETE:
                        Files.delete(file);
                        break;
                    case NO_CHANGES:
                        break;
                    default:
                        throw new AssertionError("Unknown result: " + result);
                }
                progress.increment();
            }
        }
    }

}
