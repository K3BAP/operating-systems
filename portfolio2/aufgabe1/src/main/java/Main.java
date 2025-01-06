import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Main {
    private static final int recursions = 5000000;

    public static Boolean lock = false;
    private static Reader readerThread;

    private static List<Long> messageTimes;

    public static void main(String[] args) {
        messageTimes = new ArrayList<>(recursions*2);
        readerThread = new Reader(messageTimes);
        readerThread.start();

        System.out.println("Starting measurement.");
        measure();
        System.out.println("Measurement completed.");
        readerThread.interrupt();

        System.out.println("Calculating latencies");
        List<Long> latencies = calculateLatencies(messageTimes);

        System.out.println("Size of data set: " + latencies.size());

        exportLatencies(latencies, "./latencies.txt");
    }

    private static void measure() {
        for (int i = 0; i < recursions; i++) {
            // set lock to true -> send message
            setLock(true);;

            // wait for the return message
            while (getLock()) {
                // Do nothing (spinlock)
            }

            // add current time to list
            messageTimes.add(System.nanoTime());
        }
    }

    private static List<Long> calculateLatencies(List<Long> times) {
        List<Long> results = new ArrayList<>(times.size());

        Iterator<Long> timesIterator = times.iterator();
        Long lastTime = timesIterator.next();

        while (timesIterator.hasNext()) {
            Long nextTime = timesIterator.next();
            results.add(nextTime - lastTime);
            lastTime = nextTime;
        }

        return results;
    }

    private static void exportLatencies(List<Long> latencies, String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (Long latency : latencies) {
                writer.write(latency.toString());
                writer.newLine();
            }
            System.out.println("Latencies exported to " + filename);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    public static synchronized boolean getLock() {
        return lock;
    }

    public static synchronized void setLock(boolean value) {
        lock = value;
    }
}