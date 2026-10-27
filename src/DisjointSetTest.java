public class DisjointSetTest {

    static int failed = 0;

    static void check(boolean ok, String what) {
        System.out.println((ok ? "ok    " : "FAIL  ") + what);
        if (!ok) failed++;
    }

    public static void main(String[] args) {
        DisjointSet d = new DisjointSet(6);
        check(d.count() == 6, "6 sets at the start");
        check(!d.connected(0, 1), "0 and 1 are apart at the start");
        check(d.union(0, 1), "union(0, 1) joins two sets");
        check(d.connected(0, 1), "0 and 1 are together");
        check(!d.union(1, 0), "union(1, 0) again returns false");
        check(d.count() == 5, "5 sets after one union");
        d.union(2, 3); d.union(1, 3);
        check(d.connected(0, 2), "0 and 2 are together through 1 and 3");
        check(d.count() == 3, "3 sets: {0,1,2,3} {4} {5}");
        check(d.find(4) == 4 && d.find(5) == 5, "a single cell is its own root");

        check(!d.union(4, 4), "union of a cell with itself returns false");
        check(d.count() == 3, "self union does not change count");
        d.union(4, 5);
        check(d.union(4, 0), "smaller first set joins the larger second set");
        check(d.count() == 1 && d.connected(5, 2), "all six cells are connected");

        DisjointSet empty = new DisjointSet(0);
        check(empty.count() == 0, "empty DSU has zero sets");
        DisjointSet single = new DisjointSet(1);
        check(single.find(0) == 0 && !single.union(0, 0), "one-cell DSU");

        int n = 1_000_000;
        DisjointSet chain = new DisjointSet(n);
        boolean joined = true;
        for (int i = 1; i < n; i++) joined &= chain.union(i - 1, i);
        check(joined, "a million-cell chain accepts every new link");
        check(chain.count() == 1, "one set after n - 1 successful unions");
        check(chain.connected(0, n - 1), "chain endpoints are connected without recursion");
        boolean repeated = false;
        for (int i = n - 1; i >= 0; i--) repeated |= chain.union(0, i);
        check(!repeated && chain.count() == 1, "repeated unions preserve count");

        DisjointSet balanced = new DisjointSet(1024);
        for (int step = 1; step < 1024; step *= 2)
            for (int i = 0; i < 1024; i += 2 * step) balanced.union(i, i + step);
        boolean sameRoot = true;
        for (int i = 0; i < 1024; i++) sameRoot &= balanced.find(i) == balanced.find(0);
        check(sameRoot && balanced.count() == 1, "balanced merges and repeated finds");

        System.out.println(failed == 0 ? "All checks passed." : failed + " checks failed.");
        if (failed != 0) throw new AssertionError(failed + " checks failed");
    }
}
