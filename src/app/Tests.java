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

import java.util.List;

public class Tests {
    static int passed = 0, failed = 0;

    static void check(String name, boolean cond) {
        if (cond) {
            passed++;
            System.out.println("PASS  " + name);
        } else {
            failed++;
            System.out.println("FAIL  " + name);
        }
    }

    public static void main(String[] args) {
        DSU d = new DSU(5);
        d.union(0, 1);
        d.union(3, 4);
        check("DSU connected", d.connected(0, 1) && !d.connected(1, 3));
        check("DSU components", d.components() == 3);

        Graph one = new Graph(1);
        check("MST single vertex", Kruskal.run(one).totalWeight == 0 && Prim.run(one).connected);

        Graph disc = new Graph(4);
        disc.addEdge(0, 1, 1);
        disc.addEdge(2, 3, 1);
        check("MST detects disconnected (Kruskal)", !Kruskal.run(disc).connected);
        check("MST detects disconnected (Prim)", !Prim.run(disc).connected);

        Graph sq = new Graph(4);
        sq.addEdge(0, 1, 1);
        sq.addEdge(1, 2, 1);
        sq.addEdge(2, 3, 1);
        sq.addEdge(3, 0, 1);
        check("MST multiple valid trees -> same weight",
                Kruskal.run(sq).totalWeight == 3 && Prim.run(sq).totalWeight == 3);

        boolean same = true;
        for (int s = 0; s < 20; s++) {
            Graph g = new GraphGenerator(s).connectedUndirected(50, 200, 10);
            MSTResult k = Kruskal.run(g), p = Prim.run(g);
            same &= k.totalWeight == p.totalWeight && k.edges.size() == 49 && p.edges.size() == 49;
        }
        check("Kruskal == Prim on 20 random graphs", same);

        boolean allValid = true;
        for (int s = 0; s < 20; s++) {
            DirectedGraph dag = new GraphGenerator(s).dag(60, 200, 10);
            allValid &= TopologicalSort.isValidOrder(dag, new TopologicalSort().sort(dag));
        }
        check("Topo order valid for 20 random DAGs", allValid);

        DirectedGraph cyc = new DirectedGraph(3);
        cyc.addEdge(0, 1);
        cyc.addEdge(1, 2);
        cyc.addEdge(2, 0);
        check("Topo detects cycle", new TopologicalSort().sort(cyc) == null);

        DirectedGraph sccG = new DirectedGraph(6);
        sccG.addEdge(0, 1);
        sccG.addEdge(1, 2);
        sccG.addEdge(2, 0);
        sccG.addEdge(2, 3);
        sccG.addEdge(3, 4);
        sccG.addEdge(4, 3);
        List<List<Integer>> sccs = new TarjanSCC().find(sccG);
        check("SCC count = 3", sccs.size() == 3);
        int[] seen = new int[6];
        for (List<Integer> c : sccs) for (int v : c) seen[v]++;
        boolean exactlyOnce = true;
        for (int x : seen) exactlyOnce &= x == 1;
        check("Every vertex in exactly one SCC", exactlyOnce);

        check("SCC single vertex", new TarjanSCC().find(new DirectedGraph(1)).size() == 1);

        DirectedGraph r = new DirectedGraph(5);
        r.addEdge(0, 1, 4);
        r.addEdge(0, 2, 1);
        r.addEdge(2, 1, 2);
        r.addEdge(1, 3, 5);
        Dijkstra dj = new Dijkstra();
        dj.run(r, 0);
        check("Dijkstra distances", dj.dist[1] == 3 && dj.dist[3] == 8 && dj.dist[2] == 1);
        check("Dijkstra path", dj.path(3).equals(List.of(0, 2, 1, 3)));
        check("Dijkstra unreachable", !dj.reachable(4) && dj.path(4).isEmpty());

        DirectedGraph neg = new DirectedGraph(2);
        neg.addEdge(0, 1, -1);
        boolean thrown = false;
        try {
            new Dijkstra().run(neg, 0);
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        check("Dijkstra rejects negative weights", thrown);

        System.out.println("\nPassed: " + passed + ", failed: " + failed);
        if (failed > 0) System.exit(1);
    }
}