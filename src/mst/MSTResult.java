package mst;

import graph.Edge;

import java.util.List;

public class MSTResult {
    public final List<Edge> edges;
    public final long totalWeight;
    public final boolean connected;
    public final long operations;
    public final String operationsInfo;

    public MSTResult(List<Edge> edges, long totalWeight, boolean connected, long operations, String operationsInfo) {
        this.edges = edges;
        this.totalWeight = totalWeight;
        this.connected = connected;
        this.operations = operations;
        this.operationsInfo = operationsInfo;
    }

    @Override
    public String toString() {
        return "MST{weight=" + totalWeight + ", edges=" + edges.size() + ", connected=" + connected
                + ", ops=[" + operationsInfo + "]}";
    }
}