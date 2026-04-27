public class Player {
    private int row;
    private int col;

    public Player(int startRow, int startCol) {
        this.row = startRow;
        this.col = startCol;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }

    public void setRow(int row) { this.row = row; }
    public void setCol(int col) { this.col = col; }
}