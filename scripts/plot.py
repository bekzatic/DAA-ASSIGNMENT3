import os
import pandas as pd
import matplotlib.pyplot as plt

os.makedirs("results/plots", exist_ok=True)
df = pd.read_csv("results/tables/benchmark.csv")

for x, fname, label in [
    ("V", "time_vs_vertices.png", "Number of vertices (V)"),
    ("E", "time_vs_edges.png", "Number of edges (E)"),
]:
    plt.figure(figsize=(7, 4.5))
    for alg, part in df.groupby("algorithm"):
        plt.plot(part[x], part["avg_time_ms"], marker="o", label=alg)
    plt.xscale("log")
    plt.yscale("log")
    plt.xlabel(label)
    plt.ylabel("Average time, ms (log)")
    plt.title(f"Execution time vs {x}")
    plt.grid(True, which="both", alpha=0.3)
    plt.legend()
    plt.tight_layout()
    plt.savefig(f"results/plots/{fname}", dpi=150)

mst = df[df["algorithm"].isin(["Kruskal", "Prim"])]
plt.figure(figsize=(7, 4.5))
for alg, part in mst.groupby("algorithm"):
    plt.plot(part["E"], part["operations"], marker="o", label=alg)
plt.xscale("log")
plt.yscale("log")
plt.xlabel("Number of edges (E)")
plt.ylabel("Operations")
plt.title("Kruskal vs Prim: operation count")
plt.grid(True, which="both", alpha=0.3)
plt.legend()
plt.tight_layout()
plt.savefig("results/plots/kruskal_vs_prim_ops.png", dpi=150)

print("Plots saved to results/plots/")