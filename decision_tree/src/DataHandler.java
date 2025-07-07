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
            if (row.length > 1) {
                if (filename.contains("adult.data")) {
                    String[] newRow = new String[row.length - 1];
                    System.arraycopy(row, 0, newRow, 0, 2);
                    System.arraycopy(row, 3, newRow, 2, row.length - 3);
                    data.add(newRow);
                } else {
                    data.add(row);
                }
            }
        }
        reader.close();

        // Set attributes and mark numerical ones
        if (filename.contains("iris.csv")) {
            attributes = new ArrayList<>(Arrays.asList("SepalLengthCm", "SepalWidthCm", "PetalLengthCm", "PetalWidthCm"));
            numericalAttributeIndices = new ArrayList<>(Arrays.asList(0, 1, 2, 3));
        } else if (filename.contains("adult.data")) {
            attributes = new ArrayList<>(Arrays.asList("age", "workclass", "education", "education-num",
                    "marital-status", "occupation", "relationship", "race", "sex",
                    "capital-gain", "capital-loss", "hours-per-week", "native-country"));
            numericalAttributeIndices = new ArrayList<>(Arrays.asList(0, 3, 9, 10, 11));
        } else {
            throw new IllegalArgumentException("Unsupported dataset: " + filename);
        }
    }

    private void preprocessData() {
        // Initialize unique values list for each attribute
        for (String attr : attributes) {
            attributeValues.put(attr, new ArrayList<>());
        }

        // Collect unique values for all attributes (except `?`)
        for (String[] row : data) {
            for (int i = 0; i < attributes.size(); i++) {
                String value = row[i].trim();
                String attr = attributes.get(i);
                if (!value.equals("?") && !attributeValues.get(attr).contains(value)) {
                    attributeValues.get(attr).add(value);
                }
            }
        }

        // Group high-cardinality attributes
        groupHighCardinalityAttributes();

        // For categorical attributes, add "Unknown" to handle missing
        for (String attr : attributes) {
            if (!numericalAttributeIndices.contains(attributes.indexOf(attr))) {
                attributeValues.get(attr).add("Unknown");
            }
        }

        // Discretize numerical attributes into quartiles
        for (int index : numericalAttributeIndices) {
            discretizeNumericalAttribute(attributes.get(index), index);
        }

        // Set fixed quartile labels as new domain
        for (int index : numericalAttributeIndices) {
            attributeValues.put(attributes.get(index), new ArrayList<>(Arrays.asList("Q1", "Q2", "Q3", "Q4")));
        }
    }

    private void groupHighCardinalityAttributes() {
        // Group native-country by regions
        groupNativeCountryByRegion();

        // Group occupation by category
        groupOccupationByCategory();

        // Group workclass by type
        groupWorkclassByType();
    }

    private void groupNativeCountryByRegion() {
        int nativeCountryIndex = attributes.indexOf("native-country");
        if (nativeCountryIndex == -1) return;

        Map<String, String> countryToRegion = new HashMap<>();
        countryToRegion.put("United-States", "North-America");
        countryToRegion.put("Canada", "North-America");
        countryToRegion.put("Mexico", "North-America");
        countryToRegion.put("Puerto-Rico", "North-America");
        countryToRegion.put("Cuba", "North-America");
        countryToRegion.put("Jamaica", "North-America");
        countryToRegion.put("Haiti", "North-America");
        countryToRegion.put("Dominican-Republic", "North-America");
        countryToRegion.put("El-Salvador", "North-America");
        countryToRegion.put("Guatemala", "North-America");
        countryToRegion.put("Nicaragua", "North-America");
        countryToRegion.put("Honduras", "North-America");
        countryToRegion.put("Trinadad&Tobago", "North-America");

        countryToRegion.put("China", "Asia");
        countryToRegion.put("India", "Asia");
        countryToRegion.put("Philippines", "Asia");
        countryToRegion.put("Vietnam", "Asia");
        countryToRegion.put("Japan", "Asia");
        countryToRegion.put("Taiwan", "Asia");
        countryToRegion.put("Cambodia", "Asia");
        countryToRegion.put("Laos", "Asia");
        countryToRegion.put("Thailand", "Asia");
        countryToRegion.put("Hong-Kong", "Asia");
        countryToRegion.put("Iran", "Asia");

        countryToRegion.put("Germany", "Europe");
        countryToRegion.put("England", "Europe");
        countryToRegion.put("Italy", "Europe");
        countryToRegion.put("Poland", "Europe");
        countryToRegion.put("Portugal", "Europe");
        countryToRegion.put("France", "Europe");
        countryToRegion.put("Greece", "Europe");
        countryToRegion.put("Ireland", "Europe");
        countryToRegion.put("Hungary", "Europe");
        countryToRegion.put("Scotland", "Europe");
        countryToRegion.put("Yugoslavia", "Europe");
        countryToRegion.put("Holand-Netherlands", "Europe");

        countryToRegion.put("Columbia", "South-America");
        countryToRegion.put("Peru", "South-America");
        countryToRegion.put("Ecuador", "South-America");

        // Transform data
        for (String[] row : data) {
            String country = row[nativeCountryIndex].trim();
            row[nativeCountryIndex] = countryToRegion.getOrDefault(country, "Other");
        }

        // Update attribute values with mutable list
        attributeValues.put("native-country", new ArrayList<>(Arrays.asList("North-America", "Asia", "Europe", "South-America", "Other")));
    }

    private void groupOccupationByCategory() {
        int occupationIndex = attributes.indexOf("occupation");
        if (occupationIndex == -1) return;

        Map<String, String> occupationToCategory = new HashMap<>();
        occupationToCategory.put("Tech-support", "Professional");
        occupationToCategory.put("Craft-repair", "Blue-collar");
        occupationToCategory.put("Other-service", "Service");
        occupationToCategory.put("Sales", "Sales");
        occupationToCategory.put("Exec-managerial", "Management");
        occupationToCategory.put("Prof-specialty", "Professional");
        occupationToCategory.put("Handlers-cleaners", "Blue-collar");
        occupationToCategory.put("Machine-op-inspct", "Blue-collar");
        occupationToCategory.put("Adm-clerical", "Administrative");
        occupationToCategory.put("Farming-fishing", "Blue-collar");
        occupationToCategory.put("Transport-moving", "Blue-collar");
        occupationToCategory.put("Priv-house-serv", "Service");
        occupationToCategory.put("Protective-serv", "Service");
        occupationToCategory.put("Armed-Forces", "Military");

        // Transform data
        for (String[] row : data) {
            String occupation = row[occupationIndex].trim();
            row[occupationIndex] = occupationToCategory.getOrDefault(occupation, "Other");
        }

        // Update attribute values with mutable list
        attributeValues.put("occupation", new ArrayList<>(Arrays.asList("Professional", "Blue-collar", "Service", "Sales", "Management", "Administrative", "Military", "Other")));
    }

    private void groupWorkclassByType() {
        int workclassIndex = attributes.indexOf("workclass");
        if (workclassIndex == -1) return;

        Map<String, String> workclassToType = new HashMap<>();
        workclassToType.put("Private", "Private");
        workclassToType.put("Self-emp-not-inc", "Self-employed");
        workclassToType.put("Self-emp-inc", "Self-employed");
        workclassToType.put("Federal-gov", "Government");
        workclassToType.put("Local-gov", "Government");
        workclassToType.put("State-gov", "Government");
        workclassToType.put("Without-pay", "Other");
        workclassToType.put("Never-worked", "Other");

        // Transform data
        for (String[] row : data) {
            String workclass = row[workclassIndex].trim();
            row[workclassIndex] = workclassToType.getOrDefault(workclass, "Other");
        }

        // Update attribute values with mutable list
        attributeValues.put("workclass", new ArrayList<>(Arrays.asList("Private", "Self-employed", "Government", "Other")));
    }

    private void discretizeNumericalAttribute(String attribute, int index) {
        List<Double> values = new ArrayList<>();
        for (String[] row : data) {
            try {
                String value = row[index].trim();
                if (!value.equals("?")) {
                    values.add(Double.parseDouble(value));
                }
            } catch (NumberFormatException ignored) {}
        }

        if (values.isEmpty()) return;

        Collections.sort(values);
        int n = values.size();
        double q1 = values.get(n / 4);
        double q2 = values.get(n / 2);
        double q3 = values.get(3 * n / 4);

        for (String[] row : data) {
            try {
                String value = row[index].trim();
                if (value.equals("?")) {
                    row[index] = "Unknown";
                } else {
                    double num = Double.parseDouble(value);
                    if (num <= q1) row[index] = "Q1";
                    else if (num <= q2) row[index] = "Q2";
                    else if (num <= q3) row[index] = "Q3";
                    else row[index] = "Q4";
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
        return Arrays.asList(trainData, testData);
    }

    public List<String> getAttributes() {
        return new ArrayList<>(attributes); // Return a clone to prevent mutation
    }

    public Map<String, List<String>> getAttributeValues() {
        return attributeValues;
    }

    public List<String[]> getData() {
        return data;
    }
}
