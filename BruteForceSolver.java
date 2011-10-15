import java.awt.Point;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

// This one is horribly inefficient
public class BruteForceSolver {
    
    public static void main(String[] args) {
        BruteForceSolver solver = new BruteForceSolver(5, 5);
        
        Stack<Long> solution = solver.solve();
        if (solution != null) {
            for (Long codedPoint : solution) {
                Point pt = decodePoint(codedPoint);
                System.out.println(pt.x + " " + pt.y);
            }
        } else {
            System.out.println("No solution found :(");
        }
    }
    
    
    private int width;
    private int height;
    private Queue<Searcher> searchers;
    private Set<Long> statesSeen;
    
    public BruteForceSolver(int width, int height) {
        this.width = width;
        this.height = height;
        if (width * height > 64) {
            throw new AssertionError("Board too big");
        }
        this.searchers = new ArrayDeque<Searcher>();
        this.statesSeen = new TreeSet<Long>();
        
        this.searchers.add(new Searcher());
    }
    
    public Stack<Long> solve() {
        // Brute-force bradth-first search of the entire state space
        while (!searchers.isEmpty()) {
            Searcher s = searchers.poll();
            if (s.done()) {
                return s.getStack();
            } else {
                s.addNewForks();
            }
        }
        return null;
    }
    
    private static final long HIGH_MASK = 0xFFFFFFFF00000000L;
    private static final long LOW_MASK = 0x00000000FFFFFFFFL;
    
    private static long encodePoint(int row, int col) {
        return (((long)row << 32) | col);
    }
    private static Point decodePoint(long codedPoint) {
        return new Point((int)((codedPoint & HIGH_MASK) >> 32), (int)(codedPoint & LOW_MASK));
    }
    
    private class Searcher {
        private Stack<Long> stack;
        private boolean[][] tiles;
        
        public Searcher() {
            this.stack = new Stack<Long>();
            this.tiles = new boolean[height][width];
        }

        public Searcher(Searcher that) {
            this();
            this.stack.addAll(that.stack);
            for (int row = 0; row < height; ++row) {
                for (int col = 0; col < width; ++col) {
                    this.tiles[row][col] = that.tiles[row][col];
                }
            }
        }
        
        public Stack<Long> getStack() {
            return stack;
        }
        
        public void addNewForks() {
            for (int row = 0; row < height; ++row) {
                for (int col = 0; col < width; ++col) {
                    switchAt(row, col);
                    long stateCode = getStateCode();
                    if (!statesSeen.contains(stateCode)) {
                        statesSeen.add(stateCode);
                        stack.push(encodePoint(row, col));
                        searchers.add(new Searcher(this));
                        stack.pop();
                    }
                    switchAt(row, col);
                }
            }
        }
        
        private long getStateCode() {
            long result = 0;
            for (int row = 0; row < height; ++row) {
                for (int col = 0; col < width; ++col) {
                    if (tiles[row][col]) {
                        result |= (1 << (row * width + col));
                    }
                }
            }
            return result;
        }
        
        private void switchAt(int row, int col) {
            turn(row, col);
            turn(row+1, col);
            turn(row, col+1);
            turn(row-1, col);
            turn(row, col-1);
        }
        
        private void turn(int row, int col) {
            if (row >= 0 && row < height && col >= 0 && col < width) {
                tiles[row][col] = !tiles[row][col];
            }
        }
        
        public boolean done() {
            for (int row = 0; row < height; ++row) {
                for (int col = 0; col < width; ++col) {
                    if (!tiles[row][col]) {
                        return false;
                    }
                }
            }
            return true;
        }
    }
}
