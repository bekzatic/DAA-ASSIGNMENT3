package connectivity;

import graph.DirectedGraph;
import graph.Edge;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class TopologicalSort {
    public long operations = 0;

    public List<Integer> sort(DirectedGraph g) {
        operations = 0;
        int n = g.vertexCount();
        int[] indeg = new int[n];
        for (int v = 0; v < n; v++) {
            for (Edge e : g.outEdges(v)) {
                indeg[e.v]++;
                operations++;
            }
        }

        ArrayDeque<Integer> q = new ArrayDeque<>();
        for (int v = 0; v < n; v++) {
            if (indeg[v] == 0) q.add(v);
        }

        List<Integer> order = new ArrayList<>(n);
        while (!q.isEmpty()) {
            int v = q.poll();
            operations++;
            order.add(v);
            for (Edge e : g.outEdges(v)) {
                operations++;
                if (--indeg[e.v] == 0) q.add(e.v);
            }
        }
        return order.size() == n ? order : null;
    }

    public static boolean isValidOrder(DirectedGraph g, List<Integer> order) {
        if (order == null || order.size() != g.vertexCount()) return false;
        int[] pos = new int[g.vertexCount()];
        for (int i = 0; i < order.size(); i++) pos[order.get(i)] = i;
        for (int v = 0; v < g.vertexCount(); v++) {
            for (Edge e : g.outEdges(v)) {
                if (pos[e.u] >= pos[e.v]) return false;
            }
        }
        return true;
    }
}