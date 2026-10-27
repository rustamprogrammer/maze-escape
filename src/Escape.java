/**
 * Mission 3. Escape: DFS from cell 0 to the last cell (rows * cols - 1) in the maze of Mission 1.
 * Two neighbouring cells are connected when the wall between them is broken.
 * The neighbours are tried in this order: up, right, down, left (cell - cols, cell + 1, cell + cols, cell - 1).
 * The ready method Graphs.open(w, broken) gives you the open neighbours of every cell, in no particular order:
 * to visit them in the required order, go through the four cells above one by one and take those that are in the list.
 * The program must not overflow the stack on a maze of 1000 by 1000 cells.
 */
import java.util.Arrays;

public class Escape {

    /**
     * path: the cells of the way out, from 0 to the last cell.
     * visited: how many cells are marked as visited at the moment the exit is marked (the start and the exit are counted).
     * deadEnds: how many cells of the maze have exactly one open neighbour.
     */
    public record Result(int[] path, int visited, int deadEnds) {}

    public static Result escape(Input.Walls w, int[] broken) {
        int n = w.cells();
        if (n == 0) return new Result(new int[0], 0, 0);
        int[][] open = Graphs.open(w, broken);
        int deadEnds = 0;
        for (int[] neighbours : open) if (neighbours.length == 1) deadEnds++;

        // Each frame remembers the next direction, just as a recursive call would.
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
                case 0 -> cell - w.cols(); // Up.
                case 1 -> cell + 1;        // Right.
                case 2 -> cell + w.cols(); // Down.
                default -> cell - 1;      // Left.
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
