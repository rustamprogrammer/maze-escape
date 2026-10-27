import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Zones {

     
    public static int[] kosaraju(Input.Slides s) {
        int n = s.n();
        int[][] outgoing = Graphs.slidesFrom(s);
        int[][] incoming = Graphs.slidesInto(s);
        boolean[] seen = new boolean[n];
        int[] stack = new int[n], nextEdge = new int[n], finished = new int[n];
        int count = 0;
        for (int start = 0; start < n; start++) {
            if (seen[start]) continue;
            int top = 0;
            stack[0] = start;
            seen[start] = true;
            while (top >= 0) {
                int v = stack[top];
                if (nextEdge[v] == outgoing[v].length) {
                    finished[count++] = v;
                    top--;
                } else {
                    int edge = outgoing[v][nextEdge[v]++];
                    int to = s.to()[edge];
                    if (!seen[to]) {
                        seen[to] = true;
                        stack[++top] = to;
                    }
                }
            }
        }

        int[] zoneOf = new int[n];
        Arrays.fill(zoneOf, -1);
        int zones = 0;
        for (int i = count - 1; i >= 0; i--) {
            int start = finished[i];
            if (zoneOf[start] != -1) continue;
            int size = 0;
            stack[size++] = start;
            zoneOf[start] = zones;
            while (size > 0) {
                int v = stack[--size];
                for (int edge : incoming[v]) {
                    int from = s.from()[edge];
                    if (zoneOf[from] == -1) {
                        zoneOf[from] = zones;  
                        stack[size++] = from;
                    }
                }
            }
            zones++;
        }
        return zoneOf;
    }

     
    public static long[] routes(Input.Slides s, int[] zoneOf, int zoneCount) {
        if (zoneCount == 0) return new long[]{0, 0};
        List<Map<Integer, Integer>> graph = new ArrayList<>(zoneCount);
        for (int z = 0; z < zoneCount; z++) graph.add(new HashMap<>());
        for (int edge = 0; edge < s.count(); edge++) {
            int from = zoneOf[s.from()[edge]], to = zoneOf[s.to()[edge]];
            if (from != to) graph.get(from).merge(to, s.time()[edge], Integer::min);
        }

        int[] stages = new int[zoneCount];
        Arrays.fill(stages, 1);  
        long[] distance = new long[zoneCount];
        Arrays.fill(distance, Long.MAX_VALUE);
        distance[zoneOf[0]] = 0;
        long longest = 1;
        for (int from = 0; from < zoneCount; from++) {
            longest = Math.max(longest, stages[from]);
            for (Map.Entry<Integer, Integer> edge : graph.get(from).entrySet()) {
                int to = edge.getKey();
                stages[to] = Math.max(stages[to], stages[from] + 1);
                if (distance[from] != Long.MAX_VALUE) {
                    distance[to] = Math.min(distance[to], distance[from] + edge.getValue());
                }
            }
        }
        long cost = distance[zoneOf[s.n() - 1]];
        return new long[]{longest, cost == Long.MAX_VALUE ? -1 : cost};
    }
}
