package graph;

import java.util.ArrayList;
import java.util.List;

public class Graph {
    private final int n;
    private final List<Edge> edges = new ArrayList<>();
    private final List<List<Edge>> adj = new ArrayList<>();

    public Graph(int n) {
        if (n < 0) throw new IllegalArgumentException("n must be >= 0");
        this.n = n;
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }
    }

    public void addEdge(int u, int v, int w) {
        check(u);
        check(v);
        Edge e = new Edge(u, v, w);
        edges.add(e);
        adj.get(u).add(e);
        if (u != v) adj.get(v).add(e);
    }

    private void check(int x) {
        if (x < 0 || x >= n) throw new IndexOutOfBoundsException("vertex " + x);
    }

    public int vertexCount() {
        return n;
    }

    public int edgeCount() {
        return edges.size();
    }

    public List<Edge> edges() {
        return edges;
    }

    public List<Edge> neighbors(int v) {
        return adj.get(v);
    }

    public DirectedGraph toDirected() {
        DirectedGraph d = new DirectedGraph(n);
        for (Edge e : edges) {
            d.addEdge(e.u, e.v, e.weight);
            d.addEdge(e.v, e.u, e.weight);
        }
        return d;
    }
}