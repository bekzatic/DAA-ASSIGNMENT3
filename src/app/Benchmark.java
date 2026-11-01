package app;

import connectivity.TarjanSCC;
import connectivity.TopologicalSort;
import dsu.DSU;
import graph.DirectedGraph;
import graph.Graph;
import graph.GraphGenerator;
import mst.Kruskal;
import mst.MSTResult;
import mst.Prim;
import shortestpath.Dijkstra;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.LongSupplier;

public class Benchmark {
    static final long SEED = 42;
    static final int RUNS = 5;
    static final int WARMUP = 3;
    static final int MAX_W = 100;
    static final String[] NAMES = {"Small", "Medium", "Large"};
    static final int[][] SIZES = {{10, 30}, {100, 500}, {1000, 5000}};

    record Row(String algorithm, String graph, int v, int e, double avgMs, long ops) {}

    public static void main(String[] args) throws IOException {
        Locale.setDefault(Locale.US);
        List<Row> rows = new ArrayList<>();
        for (int i = 0; i < SIZES.length; i++) {
            int n = SIZES[i][0], m = SIZES[i][1];
            String name = NAMES[i];
            GraphGenerator gen = new GraphGenerator(SEED + i);
            Graph und = gen.connectedUndirected(n, m, MAX_W);
            DirectedGraph dag = gen.dag(n, m, MAX_W);
            DirectedGraph cyc = gen.cyclic(n, m, Math.max(1, m / 10), MAX_W);
            DirectedGraph roads = und.toDirected();

            MSTResult k = Kruskal.run(und), p = Prim.run(und);
            if (k.totalWeight != p.totalWeight) throw new IllegalStateException("MST weights differ!");

            rows.add(measure("Kruskal", name, n, m, () -> Kruskal.run(und).operations));
            rows.add(measure("Prim", name, n, m, () -> Prim.run(und).operations));
            rows.add(measure("Tarjan SCC", name, n, m, () -> {
                TarjanSCC t = new TarjanSCC();
                t.find(cyc);
                return t.operations;
            }));
            rows.add(measure("Topo (DAG)", name, n, m, () -> {
                TopologicalSort t = new TopologicalSort();
                t.sort(dag);
                return t.operations;
            }));
            rows.add(measure("Topo (cyclic)", name, n, m, () -> {
                TopologicalSort t = new TopologicalSort();
                t.sort(cyc);
                return t.operations;
            }));
            rows.add(measure("Dijkstra", name, n, m, () -> {
                Dijkstra d = new Dijkstra();
                d.run(roads, 0);
                return d.operations();
            }));

            System.out.printf("%s: MST weight=%d (Kruskal == Prim), SCCs=%d, DAG topo valid=%b, cyclic topo=%s%n",
                    name, k.totalWeight, new TarjanSCC().find(cyc).size(),
                    TopologicalSort.isValidOrder(dag, new TopologicalSort().sort(dag)),
                    new TopologicalSort().sort(cyc) == null ? "cycle detected" : "no cycle");
        }

        Path dir = Path.of("results", "tables");
        Files.createDirectories(dir);
        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(dir.resolve("benchmark.csv")))) {
            w.println("algorithm,graph,V,E,avg_time_ms,operations");
            for (Row r : rows) {
                w.printf("%s,%s,%d,%d,%.4f,%d%n", r.algorithm(), r.graph(), r.v(), r.e(), r.avgMs(), r.ops());
            }
        }
        printMarkdown(rows);
        dsuExperiment(dir);
    }

    static Row measure(String alg, String graph, int v, int e, LongSupplier task) {
        for (int i = 0; i < WARMUP; i++) task.getAsLong();
        long totalNs = 0, ops = 0;
        for (int i = 0; i < RUNS; i++) {
            long t0 = System.nanoTime();
            ops = task.getAsLong();
            totalNs += System.nanoTime() - t0;
        }
        return new Row(alg, graph, v, e, totalNs / (double) RUNS / 1e6, ops);
    }

    static void printMarkdown(List<Row> rows) {
        System.out.println("\n| Algorithm | Graph | V | E | Avg time (ms) | Operations |");
        System.out.println("|---|---|---|---|---|---|");
        for (Row r : rows) {
            System.out.printf("| %s | %s | %d | %d | %.4f | %d |%n",
                    r.algorithm(), r.graph(), r.v(), r.e(), r.avgMs(), r.ops());
        }
    }

    static void dsuExperiment(Path dir) throws IOException {
        int n = 10_000;
        boolean[][] cfg = {{false, false}, {true, false}, {false, true}, {true, true}};
        String[] label = {"none", "path compression", "union by size", "both"};
        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(dir.resolve("dsu.csv")))) {
            w.println("variant,find_calls,parent_steps,avg_time_ms");
            System.out.println("\n| DSU variant | find calls | parent steps | Avg time (ms) |");
            System.out.println("|---|---|---|---|");
            for (int i = 0; i < cfg.length; i++) {
                long totalNs = 0;
                DSU d = null;
                for (int r = 0; r < RUNS; r++) {
                    Random rnd = new Random(SEED);
                    long t0 = System.nanoTime();
                    d = new DSU(n, cfg[i][0], cfg[i][1]);
                    for (int v = 1; v < n; v++) d.union(v, v - 1);
                    for (int q = 0; q < n; q++) d.find(rnd.nextInt(n));
                    totalNs += System.nanoTime() - t0;
                }
                double ms = totalNs / (double) RUNS / 1e6;
                w.printf("%s,%d,%d,%.4f%n", label[i], d.findCalls, d.parentSteps, ms);
                System.out.printf("| %s | %d | %d | %.4f |%n", label[i], d.findCalls, d.parentSteps, ms);
            }
        }
    }
}