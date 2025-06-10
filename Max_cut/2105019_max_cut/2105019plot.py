import pandas as pd
import matplotlib.pyplot as plt

# Load CSV, using the second row (index 1) as the header
df = pd.read_csv("2105019.csv", header=1)

# Drop any empty columns (like the trailing ' ')
df = df.loc[:, [col.strip() != '' for col in df.columns]]

# Print column names to debug
print("Column names:", df.columns.tolist())

# Select the first 10 graphs (G1 to G10)
selected_graphs = df[df["Name"].isin([f"g{i}" for i in range(1, 11)])]

# Plot 1: Cut Weights Comparison for 10 graphs
# Map column names (from CSV) to legend labels (for display)
algorithm_mappings = [
    ("Simple Randomized or Randomized-1", "Simple Randomized or Randomized-1"),
    ("Simple Greedy or Greedy-1", "Simple Greedy or Greedy-1"),
    ("Semi-greedy-1 (alpha=0.5)", "Semi-greedy-1 (alpha=0.5)"),
    ("Average value", "Local search"),
    ("Best value", "GRASP")
]
fig, ax = plt.subplots(figsize=(12, 6))
x = range(len(selected_graphs))
width = 0.15
for i, (col_name, label) in enumerate(algorithm_mappings):
    ax.bar([pos + i * width for pos in x], selected_graphs[col_name], width, label=label)
ax.set_title("Cut Weights Comparison for G1 to G10")
ax.set_xlabel("Graph")
ax.set_ylabel("Cut Weight")
ax.set_xticks([pos + 2 * width for pos in x])
ax.set_xticklabels(selected_graphs["Name"], rotation=45)
ax.legend()
plt.tight_layout()
plt.savefig("cut_weights_comparison_10graphs.png")
plt.close()
