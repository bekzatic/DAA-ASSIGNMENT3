package mst;

import graph.Edge;
import graph.Graph;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class Prim {
    public static MSTResult run(Graph g) {
        int n = g.vertexCount();
        List<Edge> mst = new ArrayList<>();
        if (n == 0) return new MSTResult(mst, 0, true, 0, "empty graph");

        boolean[] inTree = new boolean[n];
        long[] c = new long[3];
        PriorityQueue<Edge> pq = new PriorityQueue<>((a, b) -> {
            c[0]++;
            return Integer.compare(a.weight, b.weight);
        });

        long total = 0;
        visit(g, 0, inTree, pq, c);
        while (!pq.isEmpty() && mst.size() < n - 1) {
            Edge e = pq.poll();
            c[2]++;
            if (inTree[e.u] && inTree[e.v]) continue;
            mst.add(e);
            total += e.weight;
            visit(g, inTree[e.u] ? e.v : e.u, inTree, pq, c);
        }
        boolean connected = mst.size() == n - 1;
        long ops = c[0] + c[1] + c[2];
        String info = "comparisons=" + c[0] + ", edgesExamined=" + c[1] + ", pqOps=" + c[2];
        return new MSTResult(mst, total, connected, ops, info);
    }

    private static void visit(Graph g, int v, boolean[] inTree, PriorityQueue<Edge> pq, long[] c) {
        inTree[v] = true;
        for (Edge e : g.neighbors(v)) {
            c[1]++;
            if (!inTree[e.other(v)]) {
                pq.add(e);
                c[2]++;
            }
        }
    }
}