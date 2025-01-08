import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Experiment {
    private Reader readerThread;

    private int recursions;
    private List<Long> sendTimes;
    private List<Long> receiveTimes;

    public Semaphore messageSemaphore = new Semaphore(1);
    public Semaphore replySemaphore = new Semaphore(1);

    public Experiment(int recursions) {
        this.recursions = recursions;
        this.sendTimes = new ArrayList<>(recursions);
        this.receiveTimes = new ArrayList<>(recursions);
        this.readerThread = new Reader(this);

        try {
            messageSemaphore.acquire();
            replySemaphore.acquire();
        } catch (Exception e) {
            // TODO: handle exception
        }

        this.readerThread.start();
    }
    
    /**
     * Runs the experiment with the recursion count set in the constructor
     * @return Minimum latency value
     */
    public long runExperiment() throws Exception {
        sendTimes.clear();
        receiveTimes.clear();
        measure(recursions);
        List<Long> latencies = calculateLatencies();
        return Collections.min(latencies);
    }

    public void finish() {
        readerThread.interrupt();
    }

    private void measure(int recursions) throws InterruptedException {
        long sendTime;
        long receiveTime;
        for (int i = 0; i < recursions; i++) {
            // Record time first, then
            sendTime = System.nanoTime();
            messageSemaphore.release();
            replySemaphore.acquire();
            receiveTime = System.nanoTime();

            sendTimes.add(sendTime);
            receiveTimes.add(receiveTime);
        }
    }

    private List<Long> calculateLatencies() throws Exception {
        if (sendTimes.size() != receiveTimes.size()) {
            throw new Exception("sendTimes and receiveTimes are not of equal size");
        }

        List<Long> results = new ArrayList<>(sendTimes.size());

        for (int i = 0; i < sendTimes.size(); i++) {
            long latency = (receiveTimes.get(i) - sendTimes.get(i))/2;
            results.add(latency);
        }

        return results;
    }
}
