package shortestpath;

import graph.DirectedGraph;
import graph.Edge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class Dijkstra {
    public static final long INF = Long.MAX_VALUE;

    public long[] dist;
    public int[] prev;
    public long relaxations = 0, pqOps = 0, edgesExamined = 0;

    public void run(DirectedGraph g, int source) {
        int n = g.vertexCount();
        for (int v = 0; v < n; v++) {
            for (Edge e : g.outEdges(v)) {
                if (e.weight < 0) {
                    throw new IllegalArgumentException("Dijkstra requires non-negative weights: " + e);
                }
            }
        }

        relaxations = 0;
        pqOps = 0;
        edgesExamined = 0;
        dist = new long[n];
        prev = new int[n];
        Arrays.fill(dist, INF);
        Arrays.fill(prev, -1);
        dist[source] = 0;

        PriorityQueue<long[]> pq = new PriorityQueue<>((a, b) -> Long.compare(a[0], b[0]));
        pq.add(new long[]{0, source});
        pqOps++;

        while (!pq.isEmpty()) {
            long[] top = pq.poll();
            pqOps++;
            int u = (int) top[1];
            if (top[0] > dist[u]) continue;
            for (Edge e : g.outEdges(u)) {
                edgesExamined++;
                long nd = dist[u] + e.weight;
                if (nd < dist[e.v]) {
                    dist[e.v] = nd;
                    prev[e.v] = u;
                    relaxations++;
                    pq.add(new long[]{nd, e.v});
                    pqOps++;
                }
            }
        }
    }

    public boolean reachable(int v) {
        return dist[v] != INF;
    }

    public List<Integer> path(int target) {
        List<Integer> p = new ArrayList<>();
        if (!reachable(target)) return p;
        for (int v = target; v != -1; v = prev[v]) p.add(v);
        Collections.reverse(p);
        return p;
    }

    public long operations() {
        return relaxations + pqOps + edgesExamined;
    }
}