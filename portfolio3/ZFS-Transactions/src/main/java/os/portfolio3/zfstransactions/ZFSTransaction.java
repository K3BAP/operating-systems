package os.portfolio3.zfstransactions;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.*;

public class ZFSTransaction {
    private Map<String, String> files;
    private UUID transactionId;

    public static ZFSTransaction open() throws IOException, InterruptedException {
        ZFSTransaction transaction = new ZFSTransaction();
        transaction.files = new HashMap<>();
        transaction.transactionId = UUID.randomUUID();

        ZFSUtil.createSnapshot(transaction.transactionId.toString());

        return transaction;
    }

    private void openFile(String filename) throws IOException {
        try {
            files.put(filename, ZFSUtil.readFileFromSnapshot(filename, transactionId.toString()));
        }
        catch (NoSuchFileException e) {
            files.put(filename, "");
        }
    }

    public String readFile(String filename) {
        if (!files.containsKey(filename)) {
            try {
                openFile(filename);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return files.get(filename);
    }

    public ZFSTransaction writeFile(String filename, String fileContent) {
        if (!files.containsKey(filename)) {
            try {
                openFile(filename);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        files.put(filename, fileContent);
        return this;
    }

    public ZFSTransaction deleteFile(String filename) {
        files.put(filename, null);
        return this;
    }

    public void close() throws IOException, InterruptedException {
        ZFSUtil.deleteSnapshot(transactionId.toString());
        reset();
    }

    private void commit() throws IOException, InterruptedException {
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
            System.out.println("Conflict detected. Rolling back.");
            ZFSUtil.rollbackSnapshot(this.transactionId.toString());
            close();
        }
        else {
            System.out.println("Committed successfully.");
            close();
        }
    }

    private void reset() {
        this.files.clear();
        this.transactionId = null;
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
    }
}
