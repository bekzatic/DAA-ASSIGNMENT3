package mst;

import dsu.DSU;
import graph.Edge;
import graph.Graph;

import java.util.ArrayList;
import java.util.List;

public class Kruskal {
    public static MSTResult run(Graph g) {
        int n = g.vertexCount();
        List<Edge> sorted = new ArrayList<>(g.edges());
        long[] comparisons = {0};
        sorted.sort((a, b) -> {
            comparisons[0]++;
            return Integer.compare(a.weight, b.weight);
        });

        DSU dsu = new DSU(n);
        List<Edge> mst = new ArrayList<>();
        long total = 0, examined = 0;
        for (Edge e : sorted) {
            if (mst.size() == n - 1) break;
            examined++;
            if (dsu.union(e.u, e.v)) {
                mst.add(e);
                total += e.weight;
            }
        }
        boolean connected = n <= 1 || mst.size() == n - 1;
        long ops = comparisons[0] + examined + dsu.findCalls + dsu.unionCalls;
        String info = "comparisons=" + comparisons[0] + ", edgesExamined=" + examined
                + ", find=" + dsu.findCalls + ", union=" + dsu.unionCalls;
        return new MSTResult(mst, total, connected, ops, info);
    }
}