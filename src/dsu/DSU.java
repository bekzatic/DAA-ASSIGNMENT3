package dsu;

public class DSU {
    private final int[] parent;
    private final int[] size;
    private final boolean pathCompression;
    private final boolean unionBySize;
    private int components;

    public long findCalls = 0;
    public long unionCalls = 0;
    public long parentSteps = 0;

    public DSU(int n) {
        this(n, true, true);
    }

    public DSU(int n, boolean pathCompression, boolean unionBySize) {
        parent = new int[n];
        size = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            size[i] = 1;
        }
        this.pathCompression = pathCompression;
        this.unionBySize = unionBySize;
        this.components = n;
    }

    public int find(int x) {
        findCalls++;
        int root = x;
        while (parent[root] != root) {
            root = parent[root];
            parentSteps++;
        }
        if (pathCompression) {
            while (parent[x] != root) {
                int next = parent[x];
                parent[x] = root;
                x = next;
            }
        }
        return root;
    }

    public boolean union(int a, int b) {
        unionCalls++;
        int ra = find(a), rb = find(b);
        if (ra == rb) return false;
        if (unionBySize && size[ra] < size[rb]) {
            int t = ra;
            ra = rb;
            rb = t;
        }
        parent[rb] = ra;
        size[ra] += size[rb];
        components--;
        return true;
    }

    public boolean connected(int a, int b) {
        return find(a) == find(b);
    }

    public int components() {
        return components;
    }
}