package connectivity;

import graph.DirectedGraph;
import graph.Edge;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TarjanSCC {
    private DirectedGraph g;
    private int[] index, low;
    private boolean[] onStack;
    private ArrayDeque<Integer> stack;
    private int counter;
    private List<List<Integer>> components;
    public long operations = 0;

    public List<List<Integer>> find(DirectedGraph graph) {
        g = graph;
        int n = g.vertexCount();
        index = new int[n];
        low = new int[n];
        onStack = new boolean[n];
        Arrays.fill(index, -1);
        stack = new ArrayDeque<>();
        counter = 0;
        operations = 0;
        components = new ArrayList<>();
        for (int v = 0; v < n; v++) {
            if (index[v] == -1) dfs(v);
        }
        return components;
    }

    private void dfs(int v) {
        operations++;
        index[v] = low[v] = counter++;
        stack.push(v);
        onStack[v] = true;
        for (Edge e : g.outEdges(v)) {
            operations++;
            int w = e.v;
            if (index[w] == -1) {
                dfs(w);
                low[v] = Math.min(low[v], low[w]);
            } else if (onStack[w]) {
                low[v] = Math.min(low[v], index[w]);
            }
        }
        if (low[v] == index[v]) {
            List<Integer> comp = new ArrayList<>();
            int w;
            do {
                w = stack.pop();
                onStack[w] = false;
                comp.add(w);
            } while (w != v);
            components.add(comp);
        }
    }
}