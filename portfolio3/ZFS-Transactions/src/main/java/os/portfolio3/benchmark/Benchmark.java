package os.portfolio3.benchmark;

import os.portfolio3.zfstransactions.ZFSTransaction;

import java.io.IOException;

public class Benchmark {

    public static void main(String[] args) throws Exception {
        System.out.println("Running Benchmark");
        simultaneousEdits();
        simultaneousEditsDifferentFiles();
    }

    public static void simultaneousEdits() throws IOException, InterruptedException {
        System.out.println("\nRunning simultaneous edits");
        System.out.println("==========================\n");
        ZFSTransaction transaction1 = ZFSTransaction.open();
        transaction1.writeFile("simultaneous", "Transaction 1");
        ZFSTransaction transaction2 = ZFSTransaction.open();
        transaction2.writeFile("simultaneous", "Transaction 2");
        boolean trans1_success = transaction1.commit();
        boolean trans2_success = transaction2.commit();
        if(trans1_success) {
            System.out.println("Transaction 1 committed");
        }
        else {
            System.out.println("Transaction 1 failed");
        }
        if(trans2_success) {
            System.out.println("Transaction 2 committed");
        }
        else {
            System.out.println("Transaction 2 failed");
        }

        ZFSTransaction transaction3 = ZFSTransaction.open();
        String fileContentAfterTransaction = transaction3.readFile("simultaneous");
        System.out.println("File content after transaction: " + fileContentAfterTransaction);
        transaction3.close();

        switch (fileContentAfterTransaction){
            case "Transaction 1":
                System.out.println("Transaction 1 persisted");
                break;
            case "Transaction 2":
                System.out.println("Transaction 2 persisted");
                break;
            default:
                System.out.println("No transaction persisted");
        }
    }

    public static void simultaneousEditsDifferentFiles() throws IOException, InterruptedException {
        System.out.println("\nRunning simultaneous edits on different files");
        System.out.println("=============================================\n");
        ZFSTransaction transaction1 = ZFSTransaction.open();
        transaction1.writeFile("simultaneous1", "Transaction 1");
        ZFSTransaction transaction2 = ZFSTransaction.open();
        transaction2.writeFile("simultaneous2", "Transaction 2");
        boolean trans1_success = transaction1.commit();
        boolean trans2_success = transaction2.commit();
        if(trans1_success) {
            System.out.println("Transaction 1 committed");
        }
        else {
            System.out.println("Transaction 1 failed");
        }
        if(trans2_success) {
            System.out.println("Transaction 2 committed");
        }
        else {
            System.out.println("Transaction 2 failed");
        }

        ZFSTransaction transaction3 = ZFSTransaction.open();
        String fileContent1AfterTransaction = transaction3.readFile("simultaneous1");
        String fileContent2AfterTransaction = transaction3.readFile("simultaneous2");
        transaction3.close();

        if (fileContent1AfterTransaction.equals("Transaction 1")) {
            System.out.println("Transaction 1 persisted");
        }
        else {
            System.out.println("Transaction 1 failed");
        }

        if (fileContent2AfterTransaction.equals("Transaction 2")) {
            System.out.println("Transaction 2 persisted");
        }
        else {
            System.out.println("Transaction 2 failed");
        }
    }
}
