import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        String[] datasets = {"iris.csv", "adult.data"};
        String[] criteria = {"IG", "IGR", "NWIG"};
        int maxDepthLimit = 6;
        int numRuns = 20;

        for (String dataset : datasets) {
            for (String criterion : criteria) {
                try {
                    DataHandler dataHandler = new DataHandler(dataset);
                    String datasetName = dataset.replace(".csv", "").replace(".data", "");
                    String outputFile = "results_" + datasetName + "_" + criterion + ".csv";

                    // Prepare CSV header
                    try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
                        writer.println("Criterion,MaxDepth,AvgAccuracy,AvgNodeCount,AvgTreeDepth");
                    }

                    for (int depth = 0; depth <= maxDepthLimit; depth++) {
                        Evaluator evaluator = new Evaluator(dataHandler, criterion, depth);
                        evaluator.runExperiments(numRuns);
                        evaluator.saveSummaryLine(outputFile, criterion, depth);
                        System.out.printf("Saved %s depth=%d\n", dataset, depth);
                    }

                } catch (IOException e) {
                    System.err.println("Error processing dataset " + dataset + ": " + e.getMessage());
                }
            }
        }

        System.out.println("✅ All evaluations completed.");
    }
}
