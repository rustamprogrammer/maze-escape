import java.util.*;

 
public class ExtraChecks {
    static void require(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("stress")) {
            stress();
            return;
        }
        for (int seed = 0; seed < 120; seed++) checkMaze(seed);
        for (int seed = 0; seed < 500; seed++) checkSlides(seed);
        Input.Slides parallel = new Input.Slides(4,
                new int[]{0, 1, 0, 1, 2}, new int[]{1, 0, 2, 2, 3},
                new int[]{2, 2, 50, 3, 4});
        int[] z = Zones.kosaraju(parallel);
        require(Arrays.equals(Zones.routes(parallel, z, 3), new long[]{3, 7}),
                "minimum of parallel inter-zone slides");
        Input.Slides bigCost = new Input.Slides(3, new int[]{0, 1},
                new int[]{1, 2}, new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE});
        require(Zones.routes(bigCost, Zones.kosaraju(bigCost), 3)[1] == 4294967294L,
                "route costs use long");
        Input.Walls bigWalls = new Input.Walls(1, 3, new int[]{0, 1},
                new int[]{1, 2}, new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE - 1});
        require(Maze.primCost(bigWalls) == 4294967293L, "Prim costs use long");
        System.out.println("PASS: 120 exhaustive MST/DFS cases, 500 reachability/route cases, parallel edges and long costs.");
    }

    static void checkMaze(int seed) {
        int rows = 1 + seed % 2, cols = 1 + (seed / 2) % 3;
        Input.Walls w = Generator.generate(rows, cols, seed);
        int n = w.cells();
        int[] broken = Maze.breakWalls(w);
        require(broken.length == n - 1, "spanning tree size");
         
        long best = Long.MAX_VALUE;
        for (int mask = 0; mask < (1 << w.count()); mask++) {
            if (Integer.bitCount(mask) != n - 1) continue;
            boolean[][] connected = new boolean[n][n];
            long cost = 0;
            for (int i = 0; i < n; i++) connected[i][i] = true;
            for (int k = 0; k < w.count(); k++) if ((mask & (1 << k)) != 0) {
                connected[w.a()[k]][w.b()[k]] = connected[w.b()[k]][w.a()[k]] = true;
                cost += w.cost()[k];
            }
            closure(connected);
            boolean all = true;
            for (int v = 0; v < n; v++) all &= connected[0][v];
            if (all) best = Math.min(best, cost);
        }
        long kruskal = 0;
        for (int k : Maze.cheapestMaze(w)) kruskal += w.cost()[k];
        require(kruskal == best && Maze.primCost(w) == best, "MST optimum, seed " + seed);

        boolean[][] doors = new boolean[n][n];
        int[] degree = new int[n];
        for (int k : broken) {
            doors[w.a()[k]][w.b()[k]] = doors[w.b()[k]][w.a()[k]] = true;
            degree[w.a()[k]]++; degree[w.b()[k]]++;
        }
        List<Integer> path = new ArrayList<>();
        int[] visited = {0};
        dfsOracle(0, w, doors, new boolean[n], path, visited);
        int dead = 0;
        for (int d : degree) if (d == 1) dead++;
        Escape.Result result = Escape.escape(w, broken);
        require(Arrays.equals(result.path(), path.stream().mapToInt(Integer::intValue).toArray()), "DFS path");
        require(result.visited() == visited[0] && result.deadEnds() == dead, "DFS counts");
    }

     
    static boolean dfsOracle(int v, Input.Walls w, boolean[][] doors, boolean[] seen,
                             List<Integer> path, int[] visited) {
        seen[v] = true; visited[0]++; path.add(v);
        if (v == w.cells() - 1) return true;
        int r = v / w.cols(), c = v % w.cols();
        int[][] steps = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};
        for (int[] step : steps) {
            int nr = r + step[0], nc = c + step[1];
            if (nr < 0 || nr >= w.rows() || nc < 0 || nc >= w.cols()) continue;
            int to = nr * w.cols() + nc;
            if (doors[v][to] && !seen[to] && dfsOracle(to, w, doors, seen, path, visited)) return true;
        }
        path.remove(path.size() - 1);
        return false;
    }

    static void closure(boolean[][] reach) {
        for (int k = 0; k < reach.length; k++)
            for (int i = 0; i < reach.length; i++)
                for (int j = 0; j < reach.length; j++) reach[i][j] |= reach[i][k] && reach[k][j];
    }

    static void checkSlides(int seed) {
        Random random = new Random(seed);
        int n = 1 + random.nextInt(8);
        List<Integer> from = new ArrayList<>(), to = new ArrayList<>(), time = new ArrayList<>();
        boolean[][] reach = new boolean[n][n];
        for (int v = 0; v < n; v++) reach[v][v] = true;
        for (int a = 0; a < n; a++) for (int b = 0; b < n; b++)
            if (a != b && random.nextInt(5) == 0) {
                from.add(a); to.add(b); time.add(1 + random.nextInt(30));
                reach[a][b] = true;
            }
        closure(reach);
        Input.Slides s = new Input.Slides(n, from.stream().mapToInt(Integer::intValue).toArray(),
                to.stream().mapToInt(Integer::intValue).toArray(), time.stream().mapToInt(Integer::intValue).toArray());
        int[] zone = Zones.kosaraju(s);
        int count = Arrays.stream(zone).max().orElse(-1) + 1;
        for (int a = 0; a < n; a++) for (int b = 0; b < n; b++)
            require((zone[a] == zone[b]) == (reach[a][b] && reach[b][a]), "SCC reachability, seed " + seed);
        for (int e = 0; e < s.count(); e++)
            require(zone[s.from()[e]] <= zone[s.to()[e]], "topological SCC numbering");

         
        long inf = Long.MAX_VALUE / 4;
        long[][] dist = new long[count][count];
        boolean[][] edge = new boolean[count][count];
        for (int a = 0; a < count; a++) { Arrays.fill(dist[a], inf); dist[a][a] = 0; }
        for (int e = 0; e < s.count(); e++) {
            int a = zone[s.from()[e]], b = zone[s.to()[e]];
            if (a != b) { edge[a][b] = true; dist[a][b] = Math.min(dist[a][b], s.time()[e]); }
        }
        for (int k = 0; k < count; k++) for (int a = 0; a < count; a++) for (int b = 0; b < count; b++)
            dist[a][b] = Math.min(dist[a][b], dist[a][k] + dist[k][b]);
        long expectedCost = dist[zone[0]][zone[n - 1]];
        if (expectedCost == inf) expectedCost = -1;
        int longest = 0;
        for (int v = 0; v < count; v++) longest = Math.max(longest, longestOracle(edge, v));
        long[] actual = Zones.routes(s, zone, count);
        require(actual[0] == longest && actual[1] == expectedCost, "route oracle, seed " + seed);
    }

    static int longestOracle(boolean[][] edge, int v) {
        int best = 1;
        for (int to = 0; to < edge.length; to++) if (edge[v][to]) best = Math.max(best, 1 + longestOracle(edge, to));
        return best;
    }

    static void stress() {
        int rows = 1000, cols = 1000, n = rows * cols;
        int[] a = new int[n - 1], b = new int[n - 1], cost = new int[n - 1], broken = new int[n - 1];
        for (int i = 0; i < n - 1; i++) {
            a[i] = snakeCell(i, cols); b[i] = snakeCell(i + 1, cols);
            cost[i] = i + 1; broken[i] = i;
        }
        Escape.Result escape = Escape.escape(new Input.Walls(rows, cols, a, b, cost), broken);
        require(escape.path().length == n - cols + 1 && escape.visited() == n - cols + 1,
                "deep snake path and visited count");
        require(escape.deadEnds() == 2, "snake has two dead ends");
        for (int i = 0; i < n - 1; i++) { a[i] = i; b[i] = i + 1; }
        Input.Slides chain = new Input.Slides(n, a, b, cost);
        int[] zone = Zones.kosaraju(chain);
        for (int i = 0; i < n; i++) require(zone[i] == i, "million-cell chain SCC order");
        long[] route = Zones.routes(chain, zone, n);
        require(route[0] == n && route[1] == (long) n * (n - 1) / 2, "million-zone route");
        System.out.println("PASS: 1000x1000 snake DFS and 1,000,000-cell SCC/route chain with no recursion.");
    }

    static int snakeCell(int position, int cols) {
        int row = position / cols, column = position % cols;
        return row * cols + (row % 2 == 0 ? column : cols - 1 - column);
    }
}
