import java.io.*;
import java.util.*;

public class Evaluator {
    private final DataHandler dataHandler;
    private final String criterion;
    private final int maxDepth;
    private final List<Double> accuracies;
    private double avgNodeCount;
    private double avgTreeDepth;

    public Evaluator(DataHandler dataHandler, String criterion, int maxDepth) {
        this.dataHandler = dataHandler;
        this.criterion = criterion;
        this.maxDepth = maxDepth;
        this.accuracies = new ArrayList<>();
        this.avgNodeCount = 0.0;
        this.avgTreeDepth = 0.0;
    }

    /**
     * Runs N randomized experiments with 80/20 split and accumulates evaluation statistics.
     */
    public void runExperiments(int numRuns) {
        Random rand = new Random();
        for (int i = 0; i < numRuns; i++) {
            List<List<String[]>> split = dataHandler.splitData(0.8, rand);
            List<String[]> trainData = split.get(0);
            List<String[]> testData = split.get(1);

            DecisionTree tree = new DecisionTree(criterion, maxDepth);
            tree.train(trainData, dataHandler.getAttributes(), dataHandler.getAttributeValues());

            double accuracy = evaluate(tree, testData);
            accuracies.add(accuracy);

            avgNodeCount += tree.getNodeCount();
            avgTreeDepth += tree.getTreeDepth();
        }

        avgNodeCount /= numRuns;
        avgTreeDepth /= numRuns;
    }

    /**
     * Evaluates one tree on a test set and returns the accuracy.
     */
    private double evaluate(DecisionTree tree, List<String[]> testData) {
        int correct = 0;
        for (String[] instance : testData) {
            String prediction = tree.predict(instance);
            String actual = instance[instance.length - 1];
            if (prediction != null && prediction.equals(actual)) {
                correct++;
            }
        }
        return (double) correct / testData.size();
    }

    /**
     * Prints summary statistics for the run.
     */
    public void printResults() {
        double avgAccuracy = accuracies.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        System.out.printf("Criterion: %s | Max Depth: %d\n", criterion, maxDepth);
        System.out.printf("Average Accuracy: %.4f%%\n", avgAccuracy * 100);
        System.out.printf("Average Node Count: %.2f\n", avgNodeCount);
        System.out.printf("Average Tree Depth: %.2f\n", avgTreeDepth);
    }

    /**
     * Appends a CSV summary line to the output file.
     * Format: Criterion,MaxDepth,AvgAccuracy,AvgNodeCount,AvgTreeDepth
     */
    public void saveSummaryLine(String filename, String criterion, int depth) throws IOException {
        double avgAccuracy = accuracies.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        try (PrintWriter out = new PrintWriter(new FileWriter(filename, true))) {
            out.printf("%s,%d,%.4f,%.2f,%.2f%n",
                    criterion, depth, avgAccuracy * 100, avgNodeCount, avgTreeDepth);
        }
    }

    /**
     * Getter for individual run accuracies (if you want to plot or analyze variance).
     */
    public List<Double> getAccuracies() {
        return accuracies;
    }
}
