import java.util.List;

public class Reader extends Thread {

    private List<Long> finishTimes;

    public Reader(List<Long> times) {
        this.finishTimes = times;
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            while (Main.lock == false) {
                // do nothing (spinlock)
            }
    
            // Add the current time to the list
            finishTimes.add(System.nanoTime());

            // Reset the lock (main thread is waiting)
            if (!isInterrupted())
                Main.lock = false;
        }
    }
}
