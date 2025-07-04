import java.io.IOException;
import java.util.List;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java Main <criterion> <maxDepth>");
            System.out.println("Example: java Main IG 3");
            System.exit(1);
        }

        String criterion = args[0];
        int maxDepth = 0;
        try {
            maxDepth = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("maxDepth must be an integer");
            System.exit(1);
        }

        String[] datasets = {"iris.csv", "adult.data"};

        for (String dataset : datasets) {
            try {
                System.out.println("\nProcessing dataset: " + dataset);
                DataHandler dataHandler = new DataHandler(dataset);
                List<List<String[]>> split = dataHandler.splitData(0.8, new Random());
                DecisionTree tree = new DecisionTree(criterion, maxDepth);
                tree.train(split.get(0), dataHandler.getAttributes(), dataHandler.getAttributeValues());
                Evaluator evaluator = new Evaluator(dataHandler, criterion, maxDepth);
                evaluator.runExperiments(20);
                System.out.println("Results for " + dataset + ":");
                evaluator.printResults();
            } catch (IOException e) {
                System.out.println("Error reading dataset " + dataset + ": " + e.getMessage());
            }
        }
    }
}