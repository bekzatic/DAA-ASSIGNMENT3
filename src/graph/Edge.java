package graph;

public class Edge implements Comparable<Edge>{
    public final int u, v;
    public final int weight;

    public Edge(int u, int v, int weight) {
        this.u = u;
        this.v = v;
        this.weight = weight;
    }

    public int other(int x) {
        return x == u ? v : u;
    }

    @Override
    public int compareTo(Edge o) {
        return Integer.compare(weight, o.weight);
    }
}
