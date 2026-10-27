public class Graphs {

     
    public static int[][] open(Input.Walls w, int[] broken) {
        int n = w.cells();
        int[] degree = new int[n];
        for (int k : broken) { degree[w.a()[k]]++; degree[w.b()[k]]++; }
        int[][] g = new int[n][];
        for (int i = 0; i < n; i++) g[i] = new int[degree[i]];
        int[] filled = new int[n];
        for (int k : broken) {
            int a = w.a()[k], b = w.b()[k];
            g[a][filled[a]++] = b;
            g[b][filled[b]++] = a;
        }
        return g;
    }

     
    public static int[][] slidesFrom(Input.Slides s) { return group(s.n(), s.from()); }

     
    public static int[][] slidesInto(Input.Slides s) { return group(s.n(), s.to()); }

    private static int[][] group(int n, int[] key) {
        int[] count = new int[n];
        for (int k : key) count[k]++;
        int[][] g = new int[n][];
        for (int i = 0; i < n; i++) g[i] = new int[count[i]];
        int[] filled = new int[n];
        for (int i = 0; i < key.length; i++) g[key[i]][filled[key[i]]++] = i;
        return g;
    }
}
