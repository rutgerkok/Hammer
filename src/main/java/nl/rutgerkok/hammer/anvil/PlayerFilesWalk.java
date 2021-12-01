package nl.rutgerkok.hammer.anvil;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import nl.rutgerkok.hammer.PlayerFile;
import nl.rutgerkok.hammer.anvil.tag.AnvilFormat.LevelTag;
import nl.rutgerkok.hammer.anvil.tag.AnvilNbtReader;
import nl.rutgerkok.hammer.anvil.tag.AnvilNbtWriter;
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
final class PlayerFilesWalk {

    private final AnvilWorld world;

    PlayerFilesWalk(AnvilWorld world) {
        this.world = Objects.requireNonNull(world);
    }

    private int calculateTotalUnits() throws IOException {
        Path playerDirectory = world.getPlayerDirectory();
        if (playerDirectory == null) {
            return 0;
        }
        int size = DirectoryUtil.countFiles(playerDirectory);
        if (world.getLevelTag().containsKey(LevelTag.PLAYER)) {
            size++;
        }

        return size;
    }

    void forEach(Visitor<PlayerFile> consumer) throws IOException {
        int totalUnits = calculateTotalUnits();
        if (totalUnits == 0) {
            return;
        }
        UnitsProgress progress = Progress.ofUnits(totalUnits);
        walkPlayerFiles(consumer, progress);
        walkLevelDatTag(consumer, progress);
    }

    private void walkLevelDatTag(Visitor<PlayerFile> consumer, UnitsProgress progress) throws IOException {
        CompoundTag levelTag = world.getLevelTag();
        if (!levelTag.containsKey(LevelTag.PLAYER)) {
            return;
        }

        CompoundTag playerTag = levelTag.getCompound(LevelTag.PLAYER);
        PlayerFile playerFile = new PlayerFile(world.getGameFactory(), playerTag);
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
        Path playerDirectory = world.getPlayerDirectory();
        if (playerDirectory == null) {
            return;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(playerDirectory)) {
            for (Path file : stream) {
                if (!file.toString().endsWith(".dat")) {
                    continue;
                }
                Path oldPlayerFile = file.getParent().resolve(file.getFileName().toString() + "_old");
                CompoundTag tag = AnvilNbtReader.readFromCompressedFile(file);
                PlayerFile playerFile = new PlayerFile(world.getGameFactory(), tag);
                Result result = consumer.accept(playerFile, progress);
                switch (result) {
                    case CHANGED:
                        AnvilNbtWriter.writeCompressedToFile(file, tag);
                        break;
                    case DELETE:
                        Files.delete(file);
                        Files.deleteIfExists(oldPlayerFile);
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
