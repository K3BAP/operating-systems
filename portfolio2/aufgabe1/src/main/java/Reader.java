public class Reader extends Thread {

    private Experiment experiment;

    public Reader(Experiment experiment) {
        this.experiment = experiment;
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            while (!isInterrupted() && experiment.getLock() == false) {
                // do nothing (spinlock)
            }
    
            // Add the current time to the list
            experiment.getMessageTimes().add(System.nanoTime());

            if (isInterrupted()) break;
            
            // Reset the lock (main thread is waiting)
            experiment.setLock(false);
        }
    }
}
