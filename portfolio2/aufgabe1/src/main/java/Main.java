import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Main {
    private static final int recursions = 100;

    public static Boolean lock = false;
    private static Reader readerThread;

    private static List<Long> messageTimes;

    public static void main(String[] args) {
        messageTimes = new ArrayList<>(recursions*2);
        readerThread = new Reader(messageTimes);
        readerThread.start();

        measure();
        readerThread.interrupt();

        List<Long> latencies = calculateLatencies(messageTimes);
        for (Long long1 : latencies) {
            System.out.println(long1);
        }

        Long min = calculateMinWith95Confidence(latencies);

        System.out.println("Size of data set: " + latencies.size());
        System.out.println("Minimum with 95 percentile confidence: " + min);
    }

    private static void measure() {
        for (int i = 0; i < recursions; i++) {
            // set lock to true -> send message
            lock = true;

            // wait for the return message
            while (lock) {
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

    public static Long calculateMinWith95Confidence(List<Long> numbers) {
        if (numbers == null || numbers.isEmpty()) {
            throw new IllegalArgumentException("The list of numbers cannot be null or empty.");
        }

        // Sort the list
        Collections.sort(numbers);

        // Calculate the index for the 5th percentile
        int percentileIndex = (int) Math.ceil(0.05 * numbers.size()) - 1;

        // Return the value at that index
        return numbers.get(percentileIndex);
    }
}