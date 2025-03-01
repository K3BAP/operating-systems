package os.portfolio3.zfstransactions;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.*;

/**
 * The ZFSTransaction class provides a mechanism for managing transactions
 * for file operations on a ZFS filesystem. Each transaction creates a
 * snapshot that allows handling operations like reading, writing, and
 * deleting files with transactional consistency. Snapshots can be rolled
 * back in case of conflicts or errors during the commit process.
 *
 * This class includes methods to read files during a transaction, make
 * modifications, and commit or rollback the modifications based on conflicts.
 *
 * Features:
 * - Open a transactional context which generates a ZFS snapshot.
 * - Read file contents from the saved snapshot.
 * - Perform write, delete, or read operations within the snapshot context.
 * - Commit changes back to the main file system with conflict detection.
 * - Rollback changes in case of conflicts and reset the transaction state.
 */
public class ZFSTransaction {
    private Map<String, String> files;
    private UUID transactionId;

    /**
     * Opens a new ZFSTransaction by initializing its fields and creating a ZFS snapshot.
     * A unique transaction ID is generated, and the snapshot is created in the filesystem
     * using the transaction ID as its name.
     *
     * @return a new instance of ZFSTransaction initialized with an empty set of files
     * and a unique transaction ID, representing the open transaction.
     * @throws IOException if an I/O error occurs while creating the ZFS snapshot.
     * @throws InterruptedException if the snapshot creation process is interrupted.
     */
    public static ZFSTransaction open() throws IOException, InterruptedException {
        ZFSTransaction transaction = new ZFSTransaction();
        transaction.files = new HashMap<>();
        transaction.transactionId = UUID.randomUUID();

        ZFSUtil.createSnapshot(transaction.transactionId.toString());

        return transaction;
    }

    /**
     * Opens a file from a ZFS snapshot and stores its content in the internal map of files.
     * If the file does not exist in the snapshot, an empty string is stored instead.
     *
     * @param filename the name of the file to be opened from the ZFS snapshot
     * @throws IOException if an I/O error occurs while attempting to read the file
     */
    private void openFile(String filename) throws IOException {
        try {
            files.put(filename, ZFSUtil.readFileFromSnapshot(filename, transactionId.toString()));
        }
        catch (NoSuchFileException e) {
            files.put(filename, "");
        }
    }

    /**
     * Reads the content of a file from the transaction's internal map of files. If the file
     * is not already loaded in the map, it will be opened and its content will be loaded.
     *
     * @param filename the name of the file to be read
     * @return the content of the file as a string
     * @throws IOException if an I/O error occurs while opening or reading the file
     */
    public String readFile(String filename) throws IOException {
        if (!files.containsKey(filename)) openFile(filename);
        return files.get(filename);
    }

    /**
     * Writes the specified content to a file within the transaction.
     *
     * @param filename the name of the file to which the content will be written
     * @param fileContent the content to be written to the file
     * @return the current ZFSTransaction instance, allowing for method chaining
     * @throws IOException if an I/O error occurs while accessing the file
     */
    public ZFSTransaction writeFile(String filename, String fileContent) throws IOException {
        files.put(filename, fileContent);
        return this;
    }

    /**
     * Marks a file from the transaction as deleted by marking its content as null in the internal map of files.
     *
     * @param filename the name of the file to be deleted
     * @return the current ZFSTransaction instance, allowing for method chaining
     */
    public ZFSTransaction deleteFile(String filename) {
        files.put(filename, null);
        return this;
    }

    /**
     * Closes the current ZFSTransaction, cleaning up resources and removing the associated ZFS snapshot.
     *
     * This method deletes the ZFS snapshot corresponding to the current transaction using its unique
     * transaction ID. It then clears the internal map of files and resets the transaction ID to null.
     *
     * @throws IOException if an I/O error occurs while deleting the ZFS snapshot.
     * @throws InterruptedException if the snapshot deletion process is interrupted.
     */
    public void close() throws IOException, InterruptedException {
        ZFSUtil.deleteSnapshot(transactionId.toString());
        this.files.clear();
        this.transactionId = null;
    }

    /**
     * Reverts the ZFS snapshot associated with the current transaction to its original state.
     * This method uses the transaction ID to identify and roll back the snapshot, undoing
     * all changes made during the transaction. After rolling back the snapshot, the transaction
     * is closed to release resources and ensure that no further operations can be performed
     * on this transaction.
     *
     * @throws IOException if an I/O error occurs while attempting to roll back the snapshot.
     * @throws InterruptedException if the rollback process is interrupted.
     */
    public void rollback() throws IOException, InterruptedException {
        ZFSUtil.rollbackSnapshot(transactionId.toString());
        close();
    }

    /**
     * Executes the commit operation for the current transaction. This process compares
     * the hash of each file in the transaction with its corresponding hash in the
     * associated ZFS snapshot. If the hashes match, file changes are written or deleted
     * as specified. If a conflict is detected (hashes do not match), the transaction is
     * rolled back, undoing all changes.
     *
     * The operation ensures consistency between the ZFS snapshot and the files involved
     * in the transaction. If no conflicts are detected, the transaction is finalized and
     * cleaned up. Otherwise, it is reverted to the original state.
     *
     * @return true if the transaction is committed successfully without conflicts, false
     *         if a conflict is detected and the transaction is rolled back.
     * @throws IOException if an I/O error occurs during the commit, rollback, or file
     *         operations.
     * @throws InterruptedException if the commit or rollback process is interrupted.
     */
    private boolean commit() throws IOException, InterruptedException {
        boolean conflictDetected = false;
        for (String filename : files.keySet()) {
            if (ZFSUtil.getHashOfSnapshot(filename, transactionId.toString()) == ZFSUtil.getHashOfFile(filename)) {
                if (files.get(filename) != null)
                    ZFSUtil.writeFile(filename, files.get(filename));
                else
                    ZFSUtil.deleteFile(filename);
            }
            else {
                conflictDetected = true;
                break;
            }
        }
        if (conflictDetected) {
            rollback();
            return false;
        }
        else {
            close();
            return true;
        }
    }

    /*
    Testing
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Creating Snapshot");
        ZFSTransaction transaction = ZFSTransaction.open();
        System.out.println("File Content: " + transaction.readFile("test.txt"));
        transaction.writeFile("test.txt", "Hello World!");
        transaction.commit();

        ZFSTransaction transaction2 = ZFSTransaction.open();
        transaction2.deleteFile("test.txt");
        transaction2.commit();
    }
}
