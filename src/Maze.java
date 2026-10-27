import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

public class Maze {

     
    public static int[] breakWalls(Input.Walls w) {
        DisjointSet sets = new DisjointSet(w.cells());
        int[] broken = new int[Math.max(0, w.cells() - 1)];
        int used = 0;
        for (int k = 0; k < w.count(); k++) {
            if (sets.union(w.a()[k], w.b()[k])) broken[used++] = k;
        }
        return Arrays.copyOf(broken, used);
    }

     
    public static int[] cheapestMaze(Input.Walls w) {
        Integer[] order = new Integer[w.count()];
        for (int k = 0; k < order.length; k++) order[k] = k;
        Arrays.sort(order, Comparator.comparingInt(k -> w.cost()[k]));
        DisjointSet sets = new DisjointSet(w.cells());
        int[] broken = new int[Math.max(0, w.cells() - 1)];
        int used = 0;
        for (int k : order) {
            if (sets.union(w.a()[k], w.b()[k])) {
                broken[used++] = k;
                if (used == broken.length) break;
            }
        }
        return Arrays.copyOf(broken, used);
    }

     
    public static long primCost(Input.Walls w) {
        int n = w.cells();
        if (n == 0) return 0;
        int[] degree = new int[n];
        for (int k = 0; k < w.count(); k++) {
            degree[w.a()[k]]++;
            degree[w.b()[k]]++;
        }
        int[][] incident = new int[n][];
        for (int v = 0; v < n; v++) incident[v] = new int[degree[v]];
        int[] filled = new int[n];
        for (int k = 0; k < w.count(); k++) {
            incident[w.a()[k]][filled[w.a()[k]]++] = k;
            incident[w.b()[k]][filled[w.b()[k]]++] = k;
        }

        boolean[] inTree = new boolean[n];
        PriorityQueue<Integer> frontier = new PriorityQueue<>(
                Comparator.comparingInt(k -> w.cost()[k]));
        inTree[0] = true;
        int reached = 1;
        for (int k : incident[0]) frontier.add(k);
        long total = 0;
        while (reached < n && !frontier.isEmpty()) {
            int k = frontier.remove();
            int a = w.a()[k], b = w.b()[k];
            if (inTree[a] && inTree[b]) continue;  
            int next = inTree[a] ? b : a;
            inTree[next] = true;
            reached++;
            total += w.cost()[k];
            for (int edge : incident[next]) {
                int other = w.a()[edge] == next ? w.b()[edge] : w.a()[edge];
                if (!inTree[other]) frontier.add(edge);
            }
        }
        if (reached != n) throw new IllegalArgumentException("Walls graph is disconnected");
        return total;
    }
}
