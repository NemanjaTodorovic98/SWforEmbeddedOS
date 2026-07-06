public class GameBoard
{
    public static final int ROWS = 6;
    public static final int COLUMNS = 7;

    public static final int EMPTY = 0;
    public static final int RED = 1;
    public static final int BLUE = 2;

    private final int[][] grid;

    public GameBoard()
    {
        this.grid = new int[ROWS][COLUMNS];
        reset();
    }

    public void reset()
    {
        int row;
        int col;

        for (row = 0; row < ROWS; row++)
        {
            for (col = 0; col < COLUMNS; col++)
            {
                grid[row][col] = EMPTY;
            }
        }
    }

    public int dropDisk(int column, int player)
    {
        if (column < 0 || column >= COLUMNS)
        {
            return -1;
        }

        if (player != RED && player != BLUE)
        {
            return -1;
        }

        int row;
        for (row = ROWS - 1; row >= 0; row--)
        {
            if (grid[row][column] == EMPTY)
            {
                grid[row][column] = player;
                return row;
            }
        }

        return -1;
    }

    public boolean checkWin(int row, int col, int player)
    {
        if (!isInside(row, col))
        {
            return false;
        }

        if (grid[row][col] != player)
        {
            return false;
        }

        return hasLine(row, col, player, 0, 1)
            || hasLine(row, col, player, 1, 0)
            || hasLine(row, col, player, 1, 1)
            || hasLine(row, col, player, 1, -1);
    }

    public boolean isFull()
    {
        int col;
        for (col = 0; col < COLUMNS; col++)
        {
            if (grid[0][col] == EMPTY)
            {
                return false;
            }
        }

        return true;
    }

    private boolean hasLine(int row, int col, int player, int dr, int dc)
    {
        int count = 1;
        count += countDirection(row, col, player, dr, dc);
        count += countDirection(row, col, player, -dr, -dc);
        return count >= 4;
    }

    private int countDirection(int row, int col, int player, int dr, int dc)
    {
        int count = 0;
        int r = row + dr;
        int c = col + dc;

        while (isInside(r, c) && grid[r][c] == player)
        {
            count++;
            r += dr;
            c += dc;
        }

        return count;
    }

    private boolean isInside(int row, int col)
    {
        return row >= 0 && row < ROWS && col >= 0 && col < COLUMNS;
    }
}
