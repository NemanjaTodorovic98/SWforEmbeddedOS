public class GameSession
{
    private final String playerOne;
    private final String playerTwo;

    private String currentTurn;

    private final GameBoard board;
    private boolean gameOver;
    private String winner;

    public GameSession(String playerOne, String playerTwo)
    {
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;
        this.board = new GameBoard();
        startRound(playerOne);
    }

    public String getPlayerOne()
    {
        return playerOne;
    }

    public String getPlayerTwo()
    {
        return playerTwo;
    }

    public String getCurrentTurn()
    {
        return currentTurn;
    }

    public String getWinner()
    {
        return winner;
    }

    public boolean isGameOver()
    {
        return gameOver;
    }

    public String getOpponent(String username)
    {
        if (playerOne.equals(username))
        {
            return playerTwo;
        }

        if (playerTwo.equals(username))
        {
            return playerOne;
        }

        return null;
    }

    public MoveResult playMove(String username, int column)
    {
        if (gameOver)
        {
            return MoveResult.invalid("Igra je vec zavrsena");
        }

        if (!playerOne.equals(username) && !playerTwo.equals(username))
        {
            return MoveResult.invalid("Niste deo ove igre");
        }

        if (!username.equals(currentTurn))
        {
            return MoveResult.invalid("Nije vas red");
        }

        int value = playerOne.equals(username) ? GameBoard.RED : GameBoard.BLUE;
        int row = board.dropDisk(column, value);

        if (row < 0)
        {
            return MoveResult.invalid("Kolona je puna ili neispravna");
        }

        boolean hasWinner = board.checkWin(row, column, value);
        boolean draw = !hasWinner && board.isFull();

        if (hasWinner)
        {
            gameOver = true;
            winner = username;
        }
        else if (draw)
        {
            gameOver = true;
            winner = null;
        }
        else
        {
            currentTurn = getOpponent(username);
        }

        return MoveResult.valid(row, column, gameOver, winner, currentTurn);
    }

    private void startRound(String first)
    {
        board.reset();
        gameOver = false;
        winner = null;
        currentTurn = first;
    }

    public static class MoveResult
    {
        private final boolean valid;
        private final String reason;
        private final int row;
        private final int column;
        private final boolean gameOver;
        private final String winner;
        private final String nextPlayer;

        private MoveResult(boolean valid, String reason, int row, int column, boolean gameOver, String winner, String nextPlayer)
        {
            this.valid = valid;
            this.reason = reason;
            this.row = row;
            this.column = column;
            this.gameOver = gameOver;
            this.winner = winner;
            this.nextPlayer = nextPlayer;
        }

        public static MoveResult invalid(String reason)
        {
            return new MoveResult(false, reason, -1, -1, false, null, null);
        }

        public static MoveResult valid(int row, int column, boolean gameOver, String winner, String nextPlayer)
        {
            return new MoveResult(true, null, row, column, gameOver, winner, nextPlayer);
        }

        public boolean isValid()
        {
            return valid;
        }

        public String getReason()
        {
            return reason;
        }

        public int getRow()
        {
            return row;
        }

        public int getColumn()
        {
            return column;
        }

        public boolean isGameOver()
        {
            return gameOver;
        }

        public String getWinner()
        {
            return winner;
        }

        public String getNextPlayer()
        {
            return nextPlayer;
        }
    }
}
