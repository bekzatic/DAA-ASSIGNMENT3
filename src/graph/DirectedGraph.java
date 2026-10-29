package graph;

import java.util.ArrayList;
import java.util.List;

public class DirectedGraph {
    private final int n;
    private int m = 0;
    private final List<List<Edge>> adj = new ArrayList<>();

    public DirectedGraph(int n) {
        this.n = n;
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }
    }

    public void addEdge(int u, int v, int w) {
        if (u < 0 || u >= n || v < 0 || v >= n) {
            throw new IndexOutOfBoundsException();
        }
        adj.get(u).add(new Edge(u, v, w));
        m++;
    }

    public void addEdge(int u, int v) {
        addEdge(u, v, 1);
    }

    public int vertexCount() {
        return n;
    }

    public int edgeCount() {
        return m;
    }

    public List<Edge> outEdges(int v) {
        return adj.get(v);
    }
}