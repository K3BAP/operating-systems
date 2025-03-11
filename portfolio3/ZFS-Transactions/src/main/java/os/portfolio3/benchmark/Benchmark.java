package os.portfolio3.benchmark;

import os.portfolio3.zfstransactions.ZFSTransaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Benchmark {
    public static boolean stopThreads = false;
    public static AtomicInteger errorCounter = new AtomicInteger(0);
    public static AtomicInteger exceptionCounter = new AtomicInteger(0);

    public static void main(String[] args) throws Exception {
        System.out.println("Running Benchmark");
        simultaneousEdits();
        simultaneousEditsDifferentFiles();
        concurringReadWrite();
        concurringWrites(10, 10);
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

    public static void concurringReadWrite() throws IOException, InterruptedException {
        System.out.println("\nRunning concurring read-write");
        System.out.println("=============================\n");

        Thread writerThread = new Thread(() -> {
            try {
                ZFSTransaction transaction;
                StringBuilder builder;
                while (!stopThreads) {
                    builder = new StringBuilder();
                    for (int i = 0; i < Math.random()*100; i++) {
                        builder.append("consistentcontent");
                    }
                    transaction = ZFSTransaction.open();
                    transaction.writeFile("consistent", builder.toString());
                    transaction.close();
                }
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        Thread readerThread = new Thread(() -> {
           try {
               ZFSTransaction transaction;
               while (!stopThreads) {
                   transaction = ZFSTransaction.open();
                   String content = transaction.readFile("consistent");
                   transaction.close();
                   if (!content.matches("^(consistentcontent)*$")) {
                       System.out.println("Inconsistent content read. Content: " + content);
                   }
               }
           } catch (IOException | InterruptedException e) {
               throw new RuntimeException(e);
           }
        });

        writerThread.start();
        readerThread.start();

        System.out.println("Threads started (Errors are logged)");
        Thread.sleep(10000);

        System.out.println("Stopping threads...");
        stopThreads = true;
        writerThread.join();
        readerThread.join();
        System.out.println("Threads stopped");

        stopThreads = false;
    }

    public static void concurringWrites(int threadCount, int transactionsPerThread) throws IOException, InterruptedException {
        System.out.println("\nRunning concurring writes");
        System.out.println("=========================\n");
        ZFSTransaction.open().writeFile("consistent", "").commit();

        Runnable run = () -> {
            ZFSTransaction transaction;
            for (int i = 0; i < transactionsPerThread; i++) {
                try {
                    transaction = ZFSTransaction.open();
                    String content = transaction.readFile("consistent");
                    content = content + "consistentcontent";
                    transaction.writeFile("consistent", content);
                    boolean success = transaction.commit();
                    if (!success) {
                        errorCounter.incrementAndGet();
                    }
                } catch (InterruptedException | IOException e) {
                    System.out.println("Error: Exception while running command");
                    exceptionCounter.incrementAndGet();
                }

            }
        };

        System.out.println("Starting threads...");
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            threads.add(new Thread(run));
        }

        threads.forEach(Thread::start);
        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        System.out.println("Threads: " + threadCount + "; transactions per thread: " + transactionsPerThread);
        System.out.println("Total transactions: " + transactionsPerThread * threadCount);
        System.out.println("Total errors: " + errorCounter.get());
        System.out.println("Total exceptions: " + exceptionCounter.get());
    }
}
