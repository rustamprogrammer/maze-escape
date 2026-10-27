import java.util.Arrays;

public class Escape {

     
    public record Result(int[] path, int visited, int deadEnds) {}

    public static Result escape(Input.Walls w, int[] broken) {
        int n = w.cells();
        if (n == 0) return new Result(new int[0], 0, 0);
        int[][] open = Graphs.open(w, broken);
        int deadEnds = 0;
        for (int[] neighbours : open) if (neighbours.length == 1) deadEnds++;

         
        int[] stack = new int[n];
        int[] direction = new int[n];
        boolean[] seen = new boolean[n];
        int top = 0, visited = 1;
        stack[0] = 0;
        seen[0] = true;
        while (top >= 0) {
            int cell = stack[top];
            if (cell == n - 1)
                return new Result(Arrays.copyOf(stack, top + 1), visited, deadEnds);
            if (direction[top] == 4) {
                top--;
                continue;
            }
            int next = switch (direction[top]++) {
                case 0 -> cell - w.cols();  
                case 1 -> cell + 1;         
                case 2 -> cell + w.cols();  
                default -> cell - 1;       
            };
            if (next < 0 || next >= n || seen[next]) continue;
            boolean door = false;
            for (int neighbour : open[cell]) if (neighbour == next) door = true;
            if (!door) continue;
            seen[next] = true;
            visited++;
            stack[++top] = next;
            direction[top] = 0;
        }
        return new Result(new int[0], visited, deadEnds);
    }
}
