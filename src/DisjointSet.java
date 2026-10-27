public class DisjointSet {
    private final int[] parent;
    private final int[] size;
    private int groups;

    public DisjointSet(int n) {
        if (n < 0) throw new IllegalArgumentException("Negative number of cells");
        parent = new int[n];
        size = new int[n];
        groups = n;
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            size[i] = 1;
        }
    }

     
    public int find(int x) {
        int root = x;
        while (parent[root] != root) root = parent[root];
         
        while (parent[x] != x) {
            int next = parent[x];
            parent[x] = root;
            x = next;
        }
        return root;
    }

     
    public boolean union(int a, int b) {
        int ra = find(a), rb = find(b);
        if (ra == rb) return false;
        if (size[ra] < size[rb]) {
            int tmp = ra; ra = rb; rb = tmp;
        }
        parent[rb] = ra;
        size[ra] += size[rb];
        groups--;
        return true;
    }

    public boolean connected(int a, int b) {
        return find(a) == find(b);
    }

     
    public int count() {
        return groups;
    }
}
