import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Main {
    private static final int recursions = 50000;

    public static void main(String[] args) {
        Experiment experiment = new Experiment();

        experiment.runExperiment(recursions);
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


}