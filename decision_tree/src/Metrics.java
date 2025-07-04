import java.util.*;

public class Metrics {
    public static double calculateInformationGain(List<String[]> data, String attribute, List<String> attributeValues, List<String> attributes) {
        int attrIndex = attributes.indexOf(attribute);
        if (attrIndex == -1) {
            throw new IllegalArgumentException("Attribute " + attribute + " not found");
        }

        // Calculate entropy of the entire dataset
        double totalEntropy = calculateEntropy(data);

        // Calculate weighted entropy after splitting
        Map<String, List<String[]>> subsets = new HashMap<>();
        for (String value : attributeValues) {
            subsets.put(value, new ArrayList<>());
        }
        for (String[] row : data) {
            String value = row[attrIndex];
            if (subsets.containsKey(value)) {
                subsets.get(value).add(row);
            } else {
                subsets.getOrDefault("Unknown", new ArrayList<>()).add(row);
            }
        }

        double weightedEntropy = 0.0;
        for (String value : attributeValues) {
            List<String[]> subset = subsets.getOrDefault(value, new ArrayList<>());
            if (!subset.isEmpty()) {
                double subsetEntropy = calculateEntropy(subset);
                weightedEntropy += ((double) subset.size() / data.size()) * subsetEntropy;
            }
        }

        return totalEntropy - weightedEntropy;
    }

    public static double calculateInformationGainRatio(List<String[]> data, String attribute, List<String> attributeValues, List<String> attributes) {
        double infoGain = calculateInformationGain(data, attribute, attributeValues, attributes);
        double splitInfo = calculateSplitInformation(data, attribute, attributeValues, attributes);
        return splitInfo == 0 ? 0 : infoGain / splitInfo;
    }

    public static double calculateNWIG(List<String[]> data, String attribute, List<String> attributeValues, List<String> attributes) {
        double infoGain = calculateInformationGain(data, attribute, attributeValues, attributes);
        int k = attributeValues.size();
        double datasetSize = data.size();
        double normalizationFactor = Math.log(k + 1) / Math.log(2); // log_2(k+1)
        double sizeAdjustment = 1 - (k - 1) / datasetSize;
        return normalizationFactor == 0 ? 0 : (infoGain / normalizationFactor) * sizeAdjustment;
    }

    private static double calculateEntropy(List<String[]> data) {
        if (data.isEmpty()) return 0.0;
        Map<String, Integer> classCounts = new HashMap<>();
        for (String[] row : data) {
            String label = row[row.length - 1];
            classCounts.put(label, classCounts.getOrDefault(label, 0) + 1);
        }

        double entropy = 0.0;
        int total = data.size();
        for (int count : classCounts.values()) {
            double prob = (double) count / total;
            if (prob > 0) {
                entropy -= prob * Math.log(prob) / Math.log(2);
            }
        }
        return entropy;
    }

    private static double calculateSplitInformation(List<String[]> data, String attribute, List<String> attributeValues, List<String> attributes) {
        int attrIndex = attributes.indexOf(attribute);
        if (attrIndex == -1) {
            throw new IllegalArgumentException("Attribute " + attribute + " not found");
        }

        Map<String, Integer> valueCounts = new HashMap<>();
        for (String[] row : data) {
            String value = row[attrIndex];
            valueCounts.put(value, valueCounts.getOrDefault(value, 0) + 1);
        }

        double splitInfo = 0.0;
        int total = data.size();
        for (int count : valueCounts.values()) {
            double prob = (double) count / total;
            if (prob > 0) {
                splitInfo -= prob * Math.log(prob) / Math.log(2);
            }
        }
        return splitInfo;
    }
}