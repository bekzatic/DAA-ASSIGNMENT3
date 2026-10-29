package app;

import connectivity.TarjanSCC;
import connectivity.TopologicalSort;
import graph.DirectedGraph;
import graph.Graph;
import mst.Kruskal;
import mst.Prim;
import shortestpath.Dijkstra;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        String[] names = {"Center", "North", "East", "South", "West", "Airport"};

        Graph city = new Graph(6);
        city.addEdge(0, 1, 4);
        city.addEdge(0, 2, 3);
        city.addEdge(1, 2, 1);
        city.addEdge(1, 3, 2);
        city.addEdge(2, 3, 4);
        city.addEdge(3, 4, 2);
        city.addEdge(4, 5, 6);
        city.addEdge(3, 5, 5);
        System.out.println("=== Task 1: MST ===");
        System.out.println("Kruskal: " + Kruskal.run(city));
        System.out.println("Prim:    " + Prim.run(city));

        DirectedGraph deps = new DirectedGraph(6);
        deps.addEdge(0, 1);
        deps.addEdge(0, 2);
        deps.addEdge(1, 3);
        deps.addEdge(2, 3);
        deps.addEdge(3, 4);
        deps.addEdge(4, 5);
        System.out.println("\n=== Task 2: Topological order (DAG) ===");
        System.out.println(new TopologicalSort().sort(deps));

        DirectedGraph oneWay = new DirectedGraph(6);
        oneWay.addEdge(0, 1);
        oneWay.addEdge(1, 2);
        oneWay.addEdge(2, 0);
        oneWay.addEdge(2, 3);
        oneWay.addEdge(3, 4);
        oneWay.addEdge(4, 3);
        oneWay.addEdge(4, 5);
        List<Integer> topo = new TopologicalSort().sort(oneWay);
        System.out.println("Cyclic graph topo order: " + (topo == null ? "IMPOSSIBLE (cycle detected)" : topo));
        System.out.println("SCCs: " + new TarjanSCC().find(oneWay));

        System.out.println("\n=== Task 3: Dijkstra from " + names[0] + " ===");
        Dijkstra d = new Dijkstra();
        d.run(city.toDirected(), 0);
        for (int v = 0; v < names.length; v++) {
            System.out.println(names[v] + ": "
                    + (d.reachable(v) ? d.dist[v] + " via " + d.path(v) : "unreachable"));
        }
    }
}