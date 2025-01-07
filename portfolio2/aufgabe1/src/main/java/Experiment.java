import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Experiment {
    private Boolean lock = false;
    private Reader readerThread;

    private List<Long> messageTimes;
    
    public void runExperiment(int recursions) {
        messageTimes = new ArrayList<>(recursions*2);
        readerThread = new Reader(this);
        readerThread.start();

        System.out.println("Starting measurement.");
        measure(recursions);
        System.out.println("Measurement completed.");
        readerThread.interrupt();

        System.out.println("Calculating latencies");
        List<Long> latencies = calculateLatencies(messageTimes);

        System.out.println("Size of data set: " + latencies.size());
    }

    private void measure(int recursions) {
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

    public synchronized boolean getLock() {
        return lock;
    }

    public synchronized void setLock(boolean value) {
        lock = value;
    }

    public List<Long> getMessageTimes() {
        return this.messageTimes;
    }
}
