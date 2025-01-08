public class Reader extends Thread {

    private Experiment experiment;

    public Reader(Experiment experiment) {
        this.experiment = experiment;
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                experiment.messageSemaphore.acquire();
                experiment.replySemaphore.release();
            } catch (InterruptedException e) {
                System.out.println("Reader Thread shutting down.");
                break;
            }
        }
    }
}
