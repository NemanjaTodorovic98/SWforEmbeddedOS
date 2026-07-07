package com.example.gameclient;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {

    private BoardView boardView;
    private TextView tvStatus;
    private String myUsername;
    private String opponent;
    private int myPlayer;
    private boolean myTurn;
    private String nextPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        myUsername = getIntent().getStringExtra("myUsername");
        opponent = getIntent().getStringExtra("opponent");
        myPlayer = getIntent().getIntExtra("myPlayer", 1);
        myTurn = (myPlayer == 1);
        nextPlayer = (myPlayer == 1) ? myUsername : opponent;

        tvStatus = findViewById(R.id.tv_status);
        boardView = findViewById(R.id.board_view);

        updateStatus();

        boardView.setOnColumnClickListener(col -> {
            if (!myTurn) return;
            LobbyActivity.sharedConnection.send("MOVE|" + col);
        });

        LobbyActivity.sharedConnection.setListener(new ServerConnection.Listener() {
            @Override
            public void onConnected() {}

            @Override
            public void onMessage(String message) {
                handleMessage(message);
            }

            @Override
            public void onDisconnected() {
                finish();
            }
        });
    }

    private void handleMessage(String message) {
        String[] parts = message.split("\\|");
        switch (parts[0]) {
            case "MOVE_OK":
                int row = Integer.parseInt(parts[1]);
                int col = Integer.parseInt(parts[2]);
                int player = Integer.parseInt(parts[3]);
                String nextPlay = parts[4];
                runOnUiThread(() -> {
                    boardView.setCell(row, col, player);
                    nextPlayer = nextPlay;
                    myTurn = nextPlayer.equals(myUsername);
                    updateStatus();
                });
                break;
            case "GAME_END":
                String winner = parts[1];
                String msg = winner.equals("DRAW") ? "Nereseno!" : winner + " je pobedio!";
                runOnUiThread(() ->
                        new AlertDialog.Builder(this)
                                .setTitle("Kraj igre")
                                .setMessage(msg + "\n\nIgraj ponovo?")
                                .setCancelable(false)
                                .setPositiveButton("Da", (d, w) ->
                                        LobbyActivity.sharedConnection.send("REMATCH|YES"))
                                .setNegativeButton("Ne", (d, w) ->
                                        LobbyActivity.sharedConnection.send("REMATCH|NO"))
                                .show()
                );
                break;
            case "REMATCH_START":
                opponent = parts[1];
                myPlayer = Integer.parseInt(parts[2]);
                myTurn = (myPlayer == 1);
                nextPlayer = (myPlayer == 1) ? myUsername : opponent;
                runOnUiThread(() -> {
                    boardView.resetBoard();
                    updateStatus();
                });
                break;
            case "BACK_TO_LOBBY":
                finish();
                break;
        }
    }

    private void updateStatus() {
        tvStatus.setText(myTurn ? "Tvoj potez" : "Ceka se " + nextPlayer);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (LobbyActivity.sharedConnection != null) {
            LobbyActivity.sharedConnection.setListener(null);
        }
    }
}