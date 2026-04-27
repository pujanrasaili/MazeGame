public class Maze {
    public static final int WALL = 1;
    public static final int PATH = 0;

    private int[][] grid;
    private int rows;
    private int cols;

    public Maze(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        grid = new int[rows][cols];
        generateMaze();
    }

    private void generateMaze() {
        // Fill everything with walls first
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                grid[r][c] = WALL;

        // Carve paths using recursive backtracking
        carve(1, 1);

        // Make sure start and end are open
        grid[1][1] = PATH;
        grid[rows - 2][cols - 2] = PATH;
    }

    private void carve(int r, int c) {
        int[][] directions = {{0, 2}, {0, -2}, {2, 0}, {-2, 0}};

        // Shuffle directions randomly
        for (int i = 0; i < directions.length; i++) {
            int j = (int)(Math.random() * directions.length);
            int[] temp = directions[i];
            directions[i] = directions[j];
            directions[j] = temp;
        }

        for (int[] d : directions) {
            int nr = r + d[0];
            int nc = c + d[1];

            if (nr > 0 && nr < rows - 1 && nc > 0 && nc < cols - 1 && grid[nr][nc] == WALL) {
                grid[r + d[0] / 2][c + d[1] / 2] = PATH; // remove wall between
                grid[nr][nc] = PATH;
                carve(nr, nc);
            }
        }
    }

    public int getCell(int r, int c) {
        return grid[r][c];
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }
}