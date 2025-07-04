import java.util.*;

public class Evaluator {
    private DataHandler dataHandler;
    private String criterion;
    private int maxDepth;
    private List<Double> accuracies;
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

    private double evaluate(DecisionTree tree, List<String[]> testData) {
        int correct = 0;
        for (String[] instance : testData) {
            String prediction = tree.predict(instance);
            String actual = instance[instance.length - 1];
            if (prediction != null && actual != null && prediction.equals(actual)) {
                correct++;
            }
        }
        return (double) correct / testData.size();
    }

    public void printResults() {
        double avgAccuracy = accuracies.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        System.out.printf("Average Accuracy: %.4f%%\n", avgAccuracy * 100);
        System.out.printf("Average Node Count: %.2f%n", avgNodeCount);
        System.out.printf("Average Tree Depth: %.2f%n", avgTreeDepth);
    }
}