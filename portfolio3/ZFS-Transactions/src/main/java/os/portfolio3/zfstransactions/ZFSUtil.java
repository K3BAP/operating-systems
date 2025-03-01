package os.portfolio3.zfstransactions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * A utility class for interacting with ZFS file systems. This class provides methods
 * for managing ZFS snapshots, reading and writing files, and performing file operations
 * within a ZFS mount point.
 * The following operations are supported:
 * - Creating, deleting, and rolling back ZFS snapshots.
 * - Reading files either directly or from specific snapshots.
 * - Writing, deleting, and hashing file contents within the ZFS mount point.
 *
 * @author Fabian Sponholz (1561546)
 */
public class ZFSUtil {
    public static String ZFS_MOUNTPOINT = "/mypool/myfs";
    public static String ZFS_SNAPSHOT_DIRECTORY = "/mypool/myfs/.zfs/snapshot";
    public static String ZFS_FILESYSTEM = "mypool/myfs";

    /**
     * Creates a ZFS snapshot with the given name.
     * This operation utilizes the ZFS command-line interface to execute the snapshot creation
     * for the specified filesystem.
     *
     * @param snapshotName the name of the snapshot to be created
     * @throws IOException if an I/O error occurs during the execution of the command
     * @throws InterruptedException if the command execution is interrupted
     */
    public static void createSnapshot(String snapshotName) throws IOException, InterruptedException {
        runCommand(new String[] {
                "zfs",
                "snapshot",
                ZFS_FILESYSTEM + "@" + snapshotName
        });
    }

    /**
     * Deletes a ZFS snapshot with the specified name. This operation uses the ZFS
     * command-line interface to execute the snapshot deletion for the specified filesystem.
     *
     * @param snapshotName the name of the snapshot to be deleted
     * @throws IOException if an I/O error occurs during the execution of the command
     * @throws InterruptedException if the command execution is interrupted
     */
    public static void deleteSnapshot(String snapshotName) throws IOException, InterruptedException {
        runCommand(new String[] {
                "zfs",
                "destroy",
                ZFS_FILESYSTEM + "@" + snapshotName
        });
    }

    /**
     * Rolls back a ZFS filesystem to a specified snapshot. This operation uses the ZFS
     * command-line interface to execute the rollback command for the specified snapshot.
     *
     * @param snapshotName the name of the snapshot to which the filesystem should be rolled back
     * @throws IOException if an I/O error occurs during the execution of the command
     * @throws InterruptedException if the command execution is interrupted
     */
    public static void rollbackSnapshot(String snapshotName) throws IOException, InterruptedException {
        runCommand(new String[] {
                "zfs",
                "rollback",
                ZFS_FILESYSTEM + "@" + snapshotName
        });
    }

    /**
     * Reads the content of a file from a specified ZFS snapshot.
     *
     * @param filename the name of the file to be read from the snapshot
     * @param snapshotName the name of the ZFS snapshot from which the file will be read
     * @return the content of the file as a String
     * @throws IOException if an I/O error occurs while reading the file
     */
    public static String readFileFromSnapshot(String filename, String snapshotName) throws IOException {
        return readFile(Paths.get(ZFS_SNAPSHOT_DIRECTORY, snapshotName, filename));
    }

    /**
     * Reads the contents of a file located at the specified filename within the ZFS mount point.
     *
     * @param filename the name of the file to be read from the ZFS mount point
     * @return the contents of the file as a String
     * @throws IOException if an I/O error occurs while reading the file
     */
    public static String readFile(String filename) throws IOException {
        return readFile(Paths.get(ZFS_MOUNTPOINT, filename));
    }

    private static String readFile(Path filePath) throws IOException {
        return new String(Files.readAllBytes(filePath));
    }

    /**
     * Writes the specified content to a file at the given file path.
     * If the file already exists, its content will be overwritten.
     *
     * @param filePath the path of the file to write to, relative to the ZFS mount point
     * @param content the content to be written to the file
     */
    public static void writeFile(String filePath, String content) {
        try (FileWriter writer = new FileWriter(Path.of(ZFS_MOUNTPOINT, filePath).toFile())) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes the specified file located at the provided file path within the ZFS mount point.
     * If the file exists, it is deleted, and the method returns true. If the file does not exist,
     * the method returns false.
     *
     * @param filePath the relative path of the file to be deleted within the ZFS mount point
     * @return true if the file was successfully deleted, false if the file did not exist
     */
    public static boolean deleteFile(String filePath) {
        File file = Path.of(ZFS_MOUNTPOINT, filePath).toFile();
        if (file.exists()) {
            return file.delete();
        }
        return false; // File didn't exist
    }

    /**
     * Computes the hash code of the content of the specified file.
     * If the file does not exist, returns 0.
     *
     * @param filename the name of the file whose content hash code is to be computed
     * @return the hash code of the file's content if the file exists, or 0 if the file does not exist
     * @throws IOException if an I/O error occurs while accessing the file
     */
    public static File getFile(String filename) throws IOException {
        return Path.of(ZFS_MOUNTPOINT, filename).toFile();
    }

    /**
     * Computes the hash code of the content of a specified file from a ZFS snapshot.
     * If the file does not exist in the snapshot, returns 0.
     *
     * @param filename the name of the file whose content hash code is to be computed
     * @param snapshotName the name of the ZFS snapshot from which the file will be read
     * @return the hash code of the file's content if the file exists in the snapshot, or 0 if the file does not exist
     * @throws IOException if an I/O error occurs while accessing the file
     */
    public static File getFileFromSnapshot(String filename, String snapshotName) throws IOException {
        return Path.of(ZFS_SNAPSHOT_DIRECTORY, snapshotName, filename).toFile();
    }

    public static List<String> getFileList() {
        try {
            return Files.list(Paths.get(ZFS_MOUNTPOINT))
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .toList();
        } catch (IOException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    private static void runCommand(String[] command) throws IOException, InterruptedException {
        Process commandProcess = Runtime.getRuntime().exec(command);
        int status = commandProcess.waitFor();
        if (status != 0) {
            throw new IOException("Command " + String.join(" ", command) + " exited with status " + status);
        }
    }
}
