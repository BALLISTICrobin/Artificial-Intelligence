import java.util.*;

class DecisionTree {
    private Node root;
    private int maxDepth;
    private String criterion;
    private int nodeCount;
    private int treeDepth;
    private List<String> attributes;

    static class Node {
        String attribute;
        String classLabel;
        Map<String, Node> children;
        boolean isLeaf;

        Node() {
            children = new HashMap<>();
            isLeaf = false;
        }
    }

    public DecisionTree(String criterion, int maxDepth) {
        this.criterion = criterion;
        this.maxDepth = maxDepth;
        this.nodeCount = 0;
        this.treeDepth = 0;
    }

    public void train(List<String[]> data, List<String> attributes, Map<String, List<String>> attributeValues) {
        this.attributes = new ArrayList<>(attributes);
        root = buildTree(data, attributes, attributeValues, 0);
    }

    private Node buildTree(List<String[]> data, List<String> attributes, Map<String, List<String>> attributeValues, int depth) {
        Node node = new Node();
        nodeCount++;
        treeDepth = Math.max(treeDepth, depth);

        String majorityClass = getMajorityClass(data);
        if (isPure(data) || attributes.isEmpty() || (maxDepth > 0 && depth >= maxDepth)) {
            node.isLeaf = true;
            node.classLabel = majorityClass;
            return node;
        }

        String bestAttribute = selectBestAttribute(data, attributes, attributeValues, criterion);
        node.attribute = bestAttribute;

        Map<String, List<String[]>> subsets = splitData(data, bestAttribute, attributeValues.get(bestAttribute));
        List<String> remainingAttributes = new ArrayList<>(attributes);
        remainingAttributes.remove(bestAttribute);

        for (String value : attributeValues.get(bestAttribute)) {
            List<String[]> subset = subsets.getOrDefault(value, new ArrayList<>());
            if (subset.isEmpty()) {
                Node leaf = new Node();
                leaf.isLeaf = true;
                leaf.classLabel = majorityClass;
                node.children.put(value, leaf);
                nodeCount++;
            } else {
                node.children.put(value, buildTree(subset, remainingAttributes, attributeValues, depth + 1));
            }
        }

        return node;
    }

    private String selectBestAttribute(List<String[]> data, List<String> attributes, Map<String, List<String>> attributeValues, String criterion) {
        double bestScore = -Double.MAX_VALUE;
        String bestAttr = null;

        for (String attr : attributes) {
            double score = switch (criterion) {
                case "IG" -> Metrics.calculateInformationGain(data, attr, attributeValues.get(attr), attributes);
                case "IGR" -> Metrics.calculateInformationGainRatio(data, attr, attributeValues.get(attr), attributes);
                case "NWIG" -> Metrics.calculateNWIG(data, attr, attributeValues.get(attr), attributes);
                default -> throw new IllegalArgumentException("Unknown criterion: " + criterion);
            };
            if (score > bestScore) {
                bestScore = score;
                bestAttr = attr;
            }
        }
        return bestAttr;
    }

    private boolean isPure(List<String[]> data) {
        String firstClass = data.get(0)[data.get(0).length - 1];
        return data.stream().allMatch(row -> row[row.length - 1].equals(firstClass));
    }

    private String getMajorityClass(List<String[]> data) {
        Map<String, Integer> classCounts = new HashMap<>();
        for (String[] row : data) {
            String label = row[row.length - 1];
            classCounts.put(label, classCounts.getOrDefault(label, 0) + 1);
        }
        return classCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private Map<String, List<String[]>> splitData(List<String[]> data, String attribute, List<String> values) {
        Map<String, List<String[]>> subsets = new HashMap<>();
        for (String value : values) {
            subsets.put(value, new ArrayList<>());
        }
        int attrIndex = attributes.indexOf(attribute);
        if (attrIndex == -1) {
            throw new IllegalArgumentException("Attribute " + attribute + " not found in data");
        }
        for (String[] row : data) {
            String value = row[attrIndex];
            if (subsets.containsKey(value)) {
                subsets.get(value).add(row);
            } else {
                // Safely handle unexpected values by ensuring 'Unknown' key exists
                subsets.computeIfAbsent("Unknown", k -> new ArrayList<>()).add(row);
            }
        }
        return subsets;
    }

    public String predict(String[] instance) {
        return predict(instance, root);
    }

    private String predict(String[] instance, Node node) {
        if (node.isLeaf) {
            return node.classLabel;
        }
        int attrIndex = attributes.indexOf(node.attribute);
        if (attrIndex == -1) {
            return node.classLabel; // Default to majority class if attribute not found
        }
        String value = instance[attrIndex];
        Node child = node.children.getOrDefault(value, null);
        if (child == null) {
            return node.classLabel; // Default to majority class if path doesn't exist
        }
        return predict(instance, child);
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public int getTreeDepth() {
        return treeDepth;
    }
}