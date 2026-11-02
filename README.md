# Assignment 3 — Smart City Infrastructure and Route Planning

Bekzat Amanbek

## Overview

For this assignment I modeled a small "smart city" as a graph:

- **vertices** are districts (Center, North, East, South, West, Airport in the demo),
- **edges** are roads or other infrastructure connections,
- **weights** are either construction costs (for the MST) or travel distances (for Dijkstra).

The program solves three tasks:

1. **Infrastructure planning:** find the cheapest set of roads that still connects every district. This is a minimum spanning tree (Kruskal and Prim).
2. **Connectivity analysis:** for directed connections (one-way roads, dependencies between services), find a valid build order with topological sort, detect when no such order exists, and find groups of districts that can all reach each other (strongly connected components, Tarjan).
3. **Route planning:** shortest distances and routes from a chosen district to every other one (Dijkstra).

**Graph representation.** Both graph types use adjacency lists.

- `Graph` (undirected, weighted) keeps an adjacency list for Prim plus a flat list of all edges for Kruskal. Each road is stored once in the edge list and appears in the adjacency lists of both of its ends.
- `DirectedGraph` keeps only the outgoing edges of each vertex. Topological sort, SCC and Dijkstra all use this one.
- `Graph.toDirected()` turns every two-way road into two one-way edges, so Dijkstra can run on the road map.

Everything is written by hand in Java, with no graph libraries.

```
src/
├── graph/          Edge, Graph, DirectedGraph, GraphGenerator
├── dsu/            DSU (Union-Find)
├── mst/            Kruskal, Prim, MSTResult
├── connectivity/   TopologicalSort, TarjanSCC
├── shortestpath/   Dijkstra
└── app/            Main (demo), Tests, Benchmark
scripts/plot.py     draws the charts
results/tables/     benchmark.csv, dsu.csv
results/plots/      charts
```

## Implementation

**DSU.** It stores a `parent` array and a `size` array. `find` uses path compression: after finding the root, every node on the path is pointed straight at it. `union` uses union by size, so the smaller tree goes under the bigger one. I made both optimizations switchable in the constructor so I could measure what each one gives. The DSU also counts `find` calls, `union` calls and the number of parent steps.

**Kruskal.** Sort all edges by weight, go through them, and keep an edge only if `union(u, v)` succeeds, which means its ends were in different components. Stop after V − 1 edges. If fewer edges were taken, the graph is disconnected, and `MSTResult.connected` is `false`. Counted operations: comparisons during sorting, edges examined, `find` calls and `union` calls.

**Prim.** Start at vertex 0 and keep a `PriorityQueue` of edges that leave the tree. I used the lazy version: old edges are not deleted from the queue, and an edge that comes out with both ends already in the tree is just skipped. If the tree ends up with fewer than V − 1 edges, the graph is disconnected. Counted operations: heap comparisons, edges examined, queue polls.

Both MST algorithms return the same kind of `MSTResult` (edges, total weight, connected flag, operation count), so comparing them is easy.

**Topological sort (Kahn).** Count the in-degree of every vertex. Then repeatedly take a vertex with in-degree 0, add it to the order and decrease the in-degree of its neighbours. If some vertices never reach in-degree 0, they lie on a cycle, so the method returns `null` to say that no order exists. `isValidOrder` checks that every edge goes forward in the order, and the tests use it.

**Tarjan SCC.** One DFS that assigns each vertex an `index` (discovery time) and a `low` value (the smallest index reachable). It also keeps a stack of visited vertices. When `low[v] == index[v]`, everything on the stack down to `v` is one component. Every vertex ends up in exactly one component.

**Dijkstra.** Uses a binary heap (`PriorityQueue<long[]>`). Instead of a decrease-key operation, a new entry is pushed and outdated entries are skipped when they come out. Distances are `long`, unreachable vertices keep the value `INF`, and `prev[]` is used to rebuild the route. Before starting, it checks the edges and throws `IllegalArgumentException` if any weight is negative.

## Complexity Analysis

Assumptions: adjacency lists for all graphs, a binary heap (`java.util.PriorityQueue`) for Prim and Dijkstra, and Java's merge-sort-based `List.sort` for Kruskal.

| Algorithm | Time | Extra space |
|---|---|---|
| DSU `find` / `union` (both optimizations) | O(α(V)) amortized | O(V) |
| DSU without optimizations | O(V) per operation in the worst case | O(V) |
| Kruskal | O(E log E) = O(E log V) | O(V + E) |
| Prim (lazy, binary heap) | O(E log E) = O(E log V) | O(E) |
| Topological sort (Kahn) | O(V + E) | O(V) |
| Tarjan SCC | O(V + E) | O(V) |
| Dijkstra (lazy, binary heap) | O((V + E) log V) | O(V + E) |

**DSU.** Without optimizations a tree can turn into a chain, and `find` then walks O(V) parents. Union by size keeps tree height at O(log V). Path compression flattens the paths that have been used. Together they give O(α(V)) amortized, where α is the inverse Ackermann function. It is at most 4 for any realistic V, so in practice it is constant.

**Kruskal.** Sorting the E edges costs O(E log E), and since E ≤ V², log E ≤ 2 log V. After sorting there are at most E DSU operations at almost O(1) each. So the number of edges is what matters, and V barely affects the running time.

**Prim.** Every edge is pushed into the heap at most once (when the first of its ends joins the tree), and each push or poll costs O(log E). That gives O(E log E) = O(E log V). Both V and E matter, but E dominates in connected graphs. An eager version with decrease-key would keep only V entries in the heap, but the asymptotics with a binary heap stay the same.

**Topological sort and SCC.** Each vertex is processed once and each edge is looked at a constant number of times, so the time is linear in V + E. Doubling either V or E roughly doubles the work. Tarjan's recursion depth can reach V.

**Dijkstra.** Each successful relaxation pushes one heap entry, so there are at most E entries, and each costs O(log E) = O(log V). Together with visiting every vertex, that is O((V + E) log V). On the road graphs E is 5 times V (10 times after `toDirected()`), so the edges dominate.

**Why Dijkstra is unsuitable for negative weights.** Dijkstra takes the vertex with the smallest distance from the queue and treats that distance as final. That is only correct because walking along more edges can never make a path cheaper. A negative edge breaks this. For example: `A→B = 2`, `A→C = 3`, `C→B = −2`. Dijkstra settles B at 2 before it even looks at C, but the path through C costs 3 − 2 = 1. Anything already computed from B would be wrong too. For graphs with negative weights, Bellman-Ford (O(V·E)) should be used instead. My implementation refuses such input instead of returning a wrong answer.

## Experimental Setup

- **Machine:** AMD Ryzen 9 8945HX, 32 GB RAM, Windows 11, OpenJDK 26.
- **Sizes:**

| Graph | V | E |
|---|---|---|
| Small | 10 | 30 |
| Medium | 100 | 500 |
| Large | 1000 | 5000 |

- **Reproducibility:** `GraphGenerator` uses `java.util.Random` with a fixed seed (42 for Small, 43 for Medium, 44 for Large), so every run builds exactly the same graphs.
- **Undirected graph (MST, Dijkstra):** first a random spanning tree is built (each vertex i connects to a random earlier vertex), which guarantees the graph is connected. Then random edges are added until there are E of them. There are no self-loops or duplicate edges, and weights are uniform from 1 to 100. For Dijkstra this graph is converted with `toDirected()`, which gives 2E directed edges.
- **DAG (topological sort):** the vertices are randomly shuffled, and every edge goes from an earlier vertex to a later one in that order. This means the graph **has no cycles**.
- **Cyclic directed graph (topological sort, SCC):** built the same way with E − E/10 forward edges, plus E/10 "back" edges that go against the order. The back edges create cycles, so the graph **does contain cycles**. Topological sort has to report it, and SCC finds non-trivial components.
- **Measurement:** each algorithm gets 3 warm-up runs (so the JIT compiles the code), then 5 measured runs. The time is the average over the 5 runs, measured with `System.nanoTime()`. The operation count comes from the last run. It is the same in every run because the input doesn't change.
- **Correctness checks during the benchmark:** Kruskal's and Prim's weights must be equal (otherwise it throws), the DAG order must pass `isValidOrder`, and the cyclic graph must be reported as cyclic.
- **DSU experiment:** V = 10 000. First 9 999 unions make one long chain (`union(v, v−1)`), which is the worst case for a plain DSU. Then come 10 000 `find` calls on random vertices. It runs 5 times for each of the 4 variants.

## Results

### Demo (`app.Main`)

```
=== Task 1: MST ===
Kruskal: MST{weight=13, edges=5, connected=true, ...}
Prim:    MST{weight=13, edges=5, connected=true, ...}

=== Task 2: Topological order (DAG) ===
[0, 1, 2, 3, 4, 5]
Cyclic graph topo order: IMPOSSIBLE (cycle detected)
SCCs: [[5], [4, 3], [2, 1, 0]]

=== Task 3: Dijkstra from Center ===
Center: 0 via [0]
North: 4 via [0, 1]
East: 3 via [0, 2]
South: 6 via [0, 1, 3]
West: 8 via [0, 1, 3, 4]
Airport: 11 via [0, 1, 3, 5]
```

### Benchmark (`app.Benchmark`, `results/tables/benchmark.csv`)

| Algorithm | Graph | V | E | Avg time (ms) | Operations |
|---|---|---|---|---|---|
| Kruskal | Small | 10 | 30 | 0.1995 | 163 |
| Prim | Small | 10 | 30 | 0.3584 | 248 |
| Tarjan SCC | Small | 10 | 30 | 0.1441 | 40 |
| Topo (DAG) | Small | 10 | 30 | 0.1380 | 70 |
| Topo (cyclic) | Small | 10 | 30 | 0.0572 | 42 |
| Dijkstra | Small | 10 | 30 | 0.2715 | 113 |
| Kruskal | Medium | 100 | 500 | 2.4957 | 4925 |
| Prim | Medium | 100 | 500 | 1.1611 | 6999 |
| Tarjan SCC | Medium | 100 | 500 | 0.8705 | 600 |
| Topo (DAG) | Medium | 100 | 500 | 1.0013 | 1100 |
| Topo (cyclic) | Medium | 100 | 500 | 0.3859 | 546 |
| Dijkstra | Medium | 100 | 500 | 1.4892 | 1542 |
| Kruskal | Large | 1000 | 5000 | 8.2499 | 63714 |
| Prim | Large | 1000 | 5000 | 6.8289 | 101464 |
| Tarjan SCC | Large | 1000 | 5000 | 1.7381 | 6000 |
| Topo (DAG) | Large | 1000 | 5000 | 5.7845 | 11000 |
| Topo (cyclic) | Large | 1000 | 5000 | 0.8789 | 5637 |
| Dijkstra | Large | 1000 | 5000 | 8.6539 | 15927 |

Other output of the benchmark:

| Graph | MST weight (Kruskal = Prim) | SCCs in cyclic graph | DAG order valid | Cyclic graph |
|---|---|---|---|---|
| Small | 129 | 5 | yes | cycle detected |
| Medium | 1352 | 21 | yes | cycle detected |
| Large | 12303 | 127 | yes | cycle detected |

### DSU optimizations (`results/tables/dsu.csv`)

| Variant | find calls | parent steps | Avg time (ms) |
|---|---|---|---|
| none | 29998 | 49860792 | 508.7835 |
| path compression | 29998 | 19995 | 1.7740 |
| union by size | 29998 | 19996 | 1.3088 |
| both | 29998 | 19996 | 1.2678 |

### Plots

![Execution time vs number of vertices](results/plots/time_vs_vertices.png)

![Execution time vs number of edges](results/plots/time_vs_edges.png)

![Kruskal vs Prim operation count](results/plots/kruskal_vs_prim_ops.png)

## Comparison and Discussion

**Kruskal vs Prim.** Both always returned the same total weight, both on the benchmark graphs and on 20 extra random graphs in the tests. The trees themselves can differ when several edges have equal weight, but the cost is the same. Prim counted more operations (101 464 vs 63 714 on Large). Its lazy heap can hold up to E edges, and every push and poll costs several comparisons. Kruskal sorts once and then only does DSU calls, which are almost free. The times were close, though: 6.8 ms for Prim vs 8.2 ms for Kruskal on Large, and Prim was also faster on Medium. My guess is that sorting a copy of the whole edge list costs more in practice than the comparison count suggests, while Prim only touches the edges of vertices already in the tree. From Medium to Large, E grows 10× and the operation count grows about 13× for Kruskal and 14.5× for Prim. That's the 10× from E plus the extra log factor, as predicted by O(E log E).

**DSU.** This is where theory and practice agree most clearly. Without optimizations the chain makes every `find` walk thousands of parents: about 50 million steps and half a second. Union by size alone stops the chain from forming in the first place (tree height stays ≤ log V). Path compression alone flattens the chain after the first long `find`. Each one reduces the parent steps by about 2500× and the time by about 300–400×. With both, the time is the lowest (1.27 ms). In this test union by size already keeps the trees almost flat, so path compression adds little on top. Thanks to this, the DSU part of Kruskal costs almost nothing.

**SCC.** Tarjan does exactly V + E operations: 40, 600 and 6000. That's perfectly linear, just as the theory says. The time grew from 0.14 ms to 0.87 ms to 1.74 ms, more slowly than the operations. On Small the time is mostly fixed overhead, and on the larger graphs the JIT-compiled code runs faster per operation. The number of components (5, 21, 127) grows with V because the back edges only join some of the vertices into cycles.

**Topological sort.** On the DAGs Kahn's algorithm does 2(V + E) operations (70, 1100, 11000): one pass to count in-degrees and one pass to remove vertices. Every order passed the validity check. On the cyclic graphs it stops early (42, 546, 5637) and correctly returns "no order". The vertices on cycles never reach in-degree 0, so the edges behind them are never processed. That's why the cyclic version is also faster.

**Dijkstra.** The operation count grows 113 → 1542 → 15927, about 10× per step, which matches V and E each growing 10× plus a small log factor. On Large it was the slowest algorithm (8.7 ms). It works on 2E = 10 000 directed edges, and every successful relaxation pushes a new heap entry.

**Theory vs experiment.** The operation counts match the predicted growth closely: linear for Tarjan and topological sort, and E log E for Kruskal, Prim and Dijkstra. The milliseconds are noisier. On the Small graphs the times are fractions of a millisecond, mostly JIT and timer overhead, so they don't really show the asymptotics. With only 3 sizes, the plots are also just a rough picture. For small inputs, the operation counts are the more reliable comparison.

## Testing

`app.Tests` runs 17 checks, and all of them pass:

| Required case | Test |
|---|---|
| small graphs | DSU on 5 vertices, Dijkstra on a hand-made 5-vertex graph with known distances and route |
| disconnected graphs | two separate edges: both Kruskal and Prim report `connected = false`; DSU component count |
| graphs with cycles | 3-cycle → topological sort returns `null`; 6-vertex graph with two cycles → exactly 3 SCCs |
| multiple valid MSTs | 4-cycle with equal weights → both algorithms give weight 3 |
| unreachable vertices | Dijkstra: isolated vertex has distance `INF` and an empty path |
| duplicate edge weights | 5-vertex graph with weights 2, 2, 2, 5, 2, 5 → both give weight 11 |
| single vertex | MST weight 0 and connected; SCC returns 1 component |

Correctness checks:

- **Kruskal == Prim:** same weight and V − 1 edges on 20 random connected graphs (50 vertices, 200 edges, different seeds).
- **Topological order is valid:** `isValidOrder` on 20 random DAGs (60 vertices, 200 edges).
- **Cycles are detected:** the 3-cycle above, plus the cyclic graphs in the benchmark.
- **Every vertex is in exactly one SCC:** counts how many times each vertex appears across all components.
- **Dijkstra distances:** compared with distances worked out by hand. Negative weights are rejected with an exception.

## Conclusion

- **Infrastructure planning (MST):** Kruskal and Prim always agree on the cost. For a sparse road network stored as an edge list, Kruskal with an optimized DSU is simple and fast. For dense networks stored as adjacency lists, Prim is the better fit because it doesn't need to sort every edge.
- **DSU:** at least one optimization is necessary. Without them the worst case is roughly 400× slower, even on 10 000 vertices.
- **Connectivity:** topological sort and Tarjan SCC are linear and the cheapest algorithms here. Use topological sort when an order of dependent tasks is needed (it also tells you if there is a cycle). Use SCC to find groups of districts that are mutually reachable over one-way roads.
- **Route planning:** Dijkstra with a binary heap gives correct shortest routes and handles unreachable districts, but only when no weight is negative. With negative weights, use Bellman-Ford.

## How to run

You need JDK 17 or newer (the code uses records). In IntelliJ IDEA, right-click `Main`, `Tests` or `Benchmark` and choose Run. From the terminal, in the project root:

```bash
javac -d out $(find src -name "*.java")
java -cp out app.Main
java -cp out app.Tests
java -cp out app.Benchmark
python scripts/plot.py
```

`Benchmark` writes the CSV files to `results/tables/`, and `plot.py` (needs `pandas` and `matplotlib`) draws the charts into `results/plots/`.
