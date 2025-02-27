package os.portfolio3.zfstransactions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ZFSUtil {
    private static final String ZFS_MOUNTPOINT = "/mypool/myfs";
    private static final String ZFS_SNAPSHOT_DIRECTORY = "/mypool/myfs/.zfs/snapshot";
    private static final String ZFS_FILESYSTEM = "mypool/myfs";

    public static void createSnapshot(String snapshotName) throws IOException, InterruptedException {
        runCommand(new String[] {
                "zfs",
                "snapshot",
                ZFS_FILESYSTEM + "@" + snapshotName
        });
    }

    public static void deleteSnapshot(String snapshotName) throws IOException, InterruptedException {
        runCommand(new String[] {
                "zfs",
                "destroy",
                ZFS_FILESYSTEM + "@" + snapshotName
        });
    }

    public static void rollbackSnapshot(String snapshotName) throws IOException, InterruptedException {
        runCommand(new String[] {
                "zfs",
                "rollback",
                ZFS_FILESYSTEM + "@" + snapshotName
        });
    }

    public static String readFileFromSnapshot(String filename, String snapshotName) throws IOException {
        return readFile(Paths.get(ZFS_SNAPSHOT_DIRECTORY, snapshotName, filename));
    }

    public static String readFile(String filename) throws IOException {
        return readFile(Paths.get(ZFS_MOUNTPOINT, filename));
    }

    private static String readFile(Path filePath) throws IOException {
        return new String(java.nio.file.Files.readAllBytes(filePath));
    }

    public static void writeFile(String filePath, String content) {
        try (FileWriter writer = new FileWriter(Path.of(ZFS_MOUNTPOINT, filePath).toFile())) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return file.delete();
        }
        return false; // File didn't exist
    }

    public static int getHashOfFile(String filename) throws IOException {
        try {
            return readFile(filename).hashCode();
        }
        catch (NoSuchFileException e) {
            return 0;
        }
    }

    public static int getHashOfSnapshot(String filename, String snapshotName) throws IOException {
        try {
            return readFileFromSnapshot(filename, snapshotName).hashCode();
        }
        catch (NoSuchFileException e) {
            return 0;
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
