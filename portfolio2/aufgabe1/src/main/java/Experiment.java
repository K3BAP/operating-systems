import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Experiment {
    private Boolean lock = false;
    private Reader readerThread;

    private int recursions;
    private List<Long> messageTimes;

    public Experiment(int recursions) {
        this.recursions = recursions;
        messageTimes = new ArrayList<>(recursions*2);
        this.readerThread = new Reader(this);
        readerThread.start();
    }
    
    /**
     * Runs the experiment with the recursion count set in the constructor
     * @return Minimum latency value
     */
    public long runExperiment() {
        messageTimes.clear();
        measure(recursions);
        List<Long> latencies = calculateLatencies(messageTimes);
        return Collections.min(latencies);
    }

    public void finish() {
        readerThread.interrupt();
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
