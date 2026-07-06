public class GameBoardSelfTest
{
    public static void main(String[] args)
    {
        testHorizontalWin();
        testVerticalWin();
        testDiagonalWin();
        testColumnFull();
        System.out.println("GameBoardSelfTest: OK");
    }

    private static void testHorizontalWin()
    {
        GameBoard board = new GameBoard();
        int row;
        row = board.dropDisk(0, GameBoard.RED);
        board.dropDisk(0, GameBoard.BLUE);
        row = board.dropDisk(1, GameBoard.RED);
        board.dropDisk(1, GameBoard.BLUE);
        row = board.dropDisk(2, GameBoard.RED);
        board.dropDisk(2, GameBoard.BLUE);
        row = board.dropDisk(3, GameBoard.RED);

        assertTrue(board.checkWin(row, 3, GameBoard.RED), "Horizontal win failed");
    }

    private static void testVerticalWin()
    {
        GameBoard board = new GameBoard();
        int row = -1;
        row = board.dropDisk(2, GameBoard.BLUE);
        row = board.dropDisk(2, GameBoard.BLUE);
        row = board.dropDisk(2, GameBoard.BLUE);
        row = board.dropDisk(2, GameBoard.BLUE);

        assertTrue(board.checkWin(row, 2, GameBoard.BLUE), "Vertical win failed");
    }

    private static void testDiagonalWin()
    {
        GameBoard board = new GameBoard();

        board.dropDisk(0, GameBoard.RED);

        board.dropDisk(1, GameBoard.BLUE);
        board.dropDisk(1, GameBoard.RED);

        board.dropDisk(2, GameBoard.BLUE);
        board.dropDisk(2, GameBoard.BLUE);
        board.dropDisk(2, GameBoard.RED);

        board.dropDisk(3, GameBoard.BLUE);
        board.dropDisk(3, GameBoard.BLUE);
        board.dropDisk(3, GameBoard.BLUE);
        int row = board.dropDisk(3, GameBoard.RED);

        assertTrue(board.checkWin(row, 3, GameBoard.RED), "Diagonal win failed");
    }

    private static void testColumnFull()
    {
        GameBoard board = new GameBoard();
        int i;
        for (i = 0; i < GameBoard.ROWS; i++)
        {
            board.dropDisk(0, GameBoard.RED);
        }

        int row = board.dropDisk(0, GameBoard.BLUE);
        assertTrue(row == -1, "Column full test failed");
    }

    private static void assertTrue(boolean condition, String message)
    {
        if (!condition)
        {
            throw new IllegalStateException(message);
        }
    }
}
