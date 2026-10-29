package graph;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class GraphGenerator {
    private final Random rnd;

    public GraphGenerator(long seed) {
        this.rnd = new Random(seed);
    }

    private static long key(int u, int v) {
        return ((long) u << 32) | (v & 0xffffffffL);
    }

    public Graph connectedUndirected(int n, int e, int maxWeight) {
        long maxEdges = (long) n * (n - 1) / 2;
        if (e < n - 1 || e > maxEdges) throw new IllegalArgumentException("bad E for connected graph");
        Graph g = new Graph(n);
        Set<Long> used = new HashSet<>();
        for (int i = 1; i < n; i++) {
            int j = rnd.nextInt(i);
            used.add(key(Math.min(i, j), Math.max(i, j)));
            g.addEdge(j, i, 1 + rnd.nextInt(maxWeight));
        }
        while (g.edgeCount() < e) {
            int u = rnd.nextInt(n), v = rnd.nextInt(n);
            if (u == v) continue;
            long k = key(Math.min(u, v), Math.max(u, v));
            if (used.add(k)) g.addEdge(u, v, 1 + rnd.nextInt(maxWeight));
        }
        return g;
    }

    public DirectedGraph dag(int n, int e, int maxWeight) {
        int[] order = permutation(n);
        DirectedGraph g = new DirectedGraph(n);
        Set<Long> used = new HashSet<>();
        while (g.edgeCount() < e) {
            int a = rnd.nextInt(n), b = rnd.nextInt(n);
            if (a == b) continue;
            int from = order[Math.min(a, b)], to = order[Math.max(a, b)];
            if (used.add(key(from, to))) g.addEdge(from, to, 1 + rnd.nextInt(maxWeight));
        }
        return g;
    }

    public DirectedGraph cyclic(int n, int e, int backEdges, int maxWeight) {
        int[] order = permutation(n);
        DirectedGraph g = new DirectedGraph(n);
        Set<Long> used = new HashSet<>();
        int forward = e - backEdges;
        while (g.edgeCount() < forward) {
            int a = rnd.nextInt(n), b = rnd.nextInt(n);
            if (a == b) continue;
            int from = order[Math.min(a, b)], to = order[Math.max(a, b)];
            if (used.add(key(from, to))) g.addEdge(from, to, 1 + rnd.nextInt(maxWeight));
        }
        while (g.edgeCount() < e) {
            int a = rnd.nextInt(n), b = rnd.nextInt(n);
            if (a == b) continue;
            int from = order[Math.max(a, b)], to = order[Math.min(a, b)];
            if (used.add(key(from, to))) g.addEdge(from, to, 1 + rnd.nextInt(maxWeight));
        }
        return g;
    }

    private int[] permutation(int n) {
        int[] p = new int[n];
        for (int i = 0; i < n; i++) p[i] = i;
        for (int i = n - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int t = p[i];
            p[i] = p[j];
            p[j] = t;
        }
        return p;
    }
}