import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final int recursions = 50000;
    private static final int repeatExperimentCount = 10000;

    private static List<Long> minimumLatencies;

    public static void main(String[] args) throws Exception {
        minimumLatencies = new ArrayList<>();
        System.out.println("Running " + repeatExperimentCount + " experiments with " + recursions + " recursions each.");
        Experiment experiment = new Experiment(recursions);

        for (int i = 0; i < repeatExperimentCount; i++) {
            if ((i+1) % 100 == 0) System.out.println("Running experiment " + (i + 1) + "/" + repeatExperimentCount);
            long min = experiment.runExperiment();
            minimumLatencies.add(min);
        }
        experiment.finish();
        exportLatencies(minimumLatencies, "./latencies.txt");
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