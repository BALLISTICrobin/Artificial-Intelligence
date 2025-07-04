import java.io.*;
import java.util.*;

public class DataHandler {
    private List<String[]> data;
    private List<String> attributes;
    private Map<String, List<String>> attributeValues;
    private List<Integer> numericalAttributeIndices;

    public DataHandler(String filename) throws IOException {
        data = new ArrayList<>();
        attributes = new ArrayList<>();
        attributeValues = new HashMap<>();
        numericalAttributeIndices = new ArrayList<>();
        loadData(filename);
        preprocessData();
    }

    private void loadData(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] row = line.split(",\\s*");
            if (row.length > 1) { // Ensure row has attributes + class label
                data.add(row);
            }
        }
        reader.close();

        // Set attributes based on dataset
        if (filename.contains("iris.csv")) {
            attributes = Arrays.asList("SepalLengthCm", "SepalWidthCm", "PetalLengthCm", "PetalWidthCm");
            numericalAttributeIndices = Arrays.asList(0, 1, 2, 3); // All attributes are numerical
        } else if (filename.contains("adult.data")) {
            attributes = Arrays.asList("age", "workclass", "fnlwgt", "education", "education-num",
                    "marital-status", "occupation", "relationship", "race", "sex",
                    "capital-gain", "capital-loss", "hours-per-week", "native-country");
            numericalAttributeIndices = Arrays.asList(0, 2, 4, 10, 11, 12); // Numerical attributes
        } else {
            throw new IllegalArgumentException("Unsupported dataset: " + filename);
        }
    }

    private void preprocessData() {
        // Initialize attributeValues for all attributes
        for (String attr : attributes) {
            attributeValues.put(attr, new ArrayList<>());
        }

        // Collect unique values for all attributes
        for (String[] row : data) {
            for (int i = 0; i < attributes.size(); i++) {
                String value = row[i].trim();
                if (!attributeValues.get(attributes.get(i)).contains(value) && !value.equals("?")) {
                    attributeValues.get(attributes.get(i)).add(value);
                }
            }
        }

        // Handle missing values ('?') as 'Unknown' for categorical attributes
        for (String attr : attributes) {
            if (!numericalAttributeIndices.contains(attributes.indexOf(attr))) {
                if (attributeValues.get(attr).contains("?")) {
                    attributeValues.get(attr).add("Unknown");
                }
            }
        }

        // Discretize numerical attributes
        for (int index : numericalAttributeIndices) {
            discretizeNumericalAttribute(attributes.get(index), index);
        }

        // Update attributeValues for numerical attributes after discretization
        for (int index : numericalAttributeIndices) {
            attributeValues.put(attributes.get(index), Arrays.asList("Q1", "Q2", "Q3", "Q4"));
        }
    }

    private void discretizeNumericalAttribute(String attribute, int index) {
        List<Double> values = new ArrayList<>();
        for (String[] row : data) {
            try {
                String value = row[index].trim();
                if (!value.equals("?")) {
                    values.add(Double.parseDouble(value));
                }
            } catch (NumberFormatException e) {
                // Skip invalid entries
            }
        }

        // Sort and find quartile boundaries
        Collections.sort(values);
        int n = values.size();
        if (n == 0) return;

        double q1 = values.get(n / 4);
        double q2 = values.get(n / 2);
        double q3 = values.get(3 * n / 4);

        // Replace numerical values with quartile labels
        for (String[] row : data) {
            try {
                String value = row[index].trim();
                if (value.equals("?")) {
                    row[index] = "Unknown";
                } else {
                    double num = Double.parseDouble(value);
                    if (num <= q1) {
                        row[index] = "Q1";
                    } else if (num <= q2) {
                        row[index] = "Q2";
                    } else if (num <= q3) {
                        row[index] = "Q3";
                    } else {
                        row[index] = "Q4";
                    }
                }
            } catch (NumberFormatException e) {
                row[index] = "Unknown";
            }
        }
    }

    public List<List<String[]>> splitData(double trainRatio, Random rand) {
        Collections.shuffle(data, rand);
        int trainSize = (int) (data.size() * trainRatio);
        List<String[]> trainData = new ArrayList<>(data.subList(0, trainSize));
        List<String[]> testData = new ArrayList<>(data.subList(trainSize, data.size()));
        List<List<String[]>> split = new ArrayList<>();
        split.add(trainData);
        split.add(testData);
        return split;
    }

    public List<String> getAttributes() {
        return attributes;
    }

    public Map<String, List<String>> getAttributeValues() {
        return attributeValues;
    }

    public List<String[]> getData() {
        return data;
    }
}