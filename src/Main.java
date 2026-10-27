import java.io.*;

 
public class Main {

    public static void main(String[] args) throws IOException {
        String wallsFile = args.length >= 2 ? args[0] : "data/walls.txt";
        String slidesFile = args.length >= 2 ? args[1] : "data/slides.txt";
        if (!new File(wallsFile).exists() || !new File(slidesFile).exists()) {
            System.out.println("Cannot find " + wallsFile + " or " + slidesFile);
            System.out.println("Make your files first:  java tools/MakeVariant.java YOUR_ID data");
            return;
        }
        Input.Walls walls = Input.readWalls(wallsFile);
        Input.Slides slides = Input.readSlides(slidesFile);

         
        int[] broken = Maze.breakWalls(walls);
        int[] cheapest = Maze.cheapestMaze(walls);
        long mstCost = 0, heaviest = 0;
        for (int k : cheapest) { mstCost += walls.cost()[k]; heaviest = Math.max(heaviest, walls.cost()[k]); }
        long prim = Maze.primCost(walls);
        if (prim != mstCost) System.out.println("WARNING: Kruskal gives " + mstCost + ", Prim gives " + prim);

         
        Escape.Result escape = Escape.escape(walls, broken);

         
        int[] zoneOf = Zones.kosaraju(slides);
        int zones = 0;
        int[] zoneSize = new int[slides.n() + 1];
        for (int z : zoneOf) { zones = Math.max(zones, z + 1); zoneSize[z]++; }
        long largest = 0;
        for (int size : zoneSize) largest = Math.max(largest, size);
        long isDag = zones == slides.n() ? 1 : 0;    
        long[] routes = Zones.routes(slides, zoneOf, zones);

        print("Broken", broken.length);
        print("Kept", walls.count() - broken.length);
        print("Last broken wall", broken.length == 0 ? 0 : broken[broken.length - 1] + 1);
        print("MST cost", mstCost);
        print("Heaviest broken wall", heaviest);
        print("Path length", escape.path().length == 0 ? 0 : escape.path().length - 1);
        print("DFS visited", escape.visited());
        print("Dead ends", escape.deadEnds());
        print("Zones", zones);
        print("Largest zone", largest);
        print("Is DAG", isDag);
        print("Flow stages", routes[0]);
        print("Fastest route cost", routes[1]);

         
        Render.draw(walls, broken, escape.path(), "output/maze.png");
        Render.page(walls, slides, broken, escape.path(), zoneOf, "output/maze.html");
    }

    static void print(String name, long value) {
        System.out.println(name + ": " + value);
    }
}
