import java.util.*;

public class Metrics {

    public static double calculateInformationGain(List<String[]> data, String attribute, List<String> attributeValues, List<String> attributes) {
        int attrIndex = attributes.indexOf(attribute);
        if (attrIndex == -1) {
            throw new IllegalArgumentException("Attribute " + attribute + " not found");
        }

        double totalEntropy = calculateEntropy(data);

        Map<String, List<String[]>> subsets = new HashMap<>();
        for (String value : attributeValues) {
            subsets.put(value, new ArrayList<>());
        }

        for (String[] row : data) {
            if (attrIndex >= row.length) continue;
            String value = row[attrIndex];
            if (subsets.containsKey(value)) {
                subsets.get(value).add(row);
            } // ignore unexpected values
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
        int k = attributeValues.size();
        if (k == 0 || data.isEmpty()) return 0;

        double infoGain = calculateInformationGain(data, attribute, attributeValues, attributes);
        double normalizationFactor = Math.log(k + 1) / Math.log(2); // log2(k+1)
        double sizeAdjustment = 1 - ((double) (k - 1) / data.size());

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
        if (total == 0) return 0.0;

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
            if (attrIndex >= row.length) continue;
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
