package com.example.gameclient;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class LobbyActivity extends AppCompatActivity {

    public static ServerConnection sharedConnection;

    private EditText etIP, etPort, etUsername;
    private Button btnRegister;
    private ListView lvUsers;
    private ArrayList<String> userList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        etIP = findViewById(R.id.et_ip);
        etPort = findViewById(R.id.et_port);
        etUsername = findViewById(R.id.et_username);
        btnRegister = findViewById(R.id.btn_register);
        lvUsers = findViewById(R.id.lv_users);

        userList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userList);
        lvUsers.setAdapter(adapter);

        btnRegister.setOnClickListener(v -> handleRegister());

        lvUsers.setOnItemClickListener((parent, view, position, id) -> {
            String targetUser = userList.get(position);
            sharedConnection.send("INVITE|" + targetUser);
        });
    }

    private void handleRegister() {
        String ip = etIP.getText().toString().trim();
        String portStr = etPort.getText().toString().trim();
        String username = etUsername.getText().toString().trim();

        if (ip.isEmpty() || portStr.isEmpty() || username.isEmpty()) {
            Toast.makeText(this, "unesi sva polja", Toast.LENGTH_SHORT).show();
            return;
        }

        int port = Integer.parseInt(portStr);

        sharedConnection = new ServerConnection(new ServerConnection.Listener() {
            @Override
            public void onConnected() {
                sharedConnection.send("REGISTER|" + username);
            }

            @Override
            public void onMessage(String message) {
                handleMessage(message);
            }

            @Override
            public void onDisconnected() {
                runOnUiThread(() -> {
                    btnRegister.setEnabled(true);
                    Toast.makeText(LobbyActivity.this, "Konekcija prekinuta", Toast.LENGTH_SHORT).show();
                });
            }
        });

        btnRegister.setEnabled(false);
        sharedConnection.connect(ip, port);
    }

    private void handleMessage(String message) {
        String[] parts = message.split("\\|");
        switch (parts[0]) {
            case "WELCOME":
                break;
            case "USERS":
                userList.clear();
                if (parts.length > 1 && !parts[1].isEmpty()) {
                    for (String u : parts[1].split(",")) userList.add(u);
                }
                adapter.notifyDataSetChanged();
                break;
            case "INVITE_INCOMING":
                showInviteDialog(parts[1]);
                break;
            case "INVITE_DECLINED":
                Toast.makeText(this, "Poziv odbijen", Toast.LENGTH_SHORT).show();
                break;
            case "START_GAME":
                String opponent = parts[1];
                int myPlayer = parts[2].equals("RED") ? 1 : 2;
                Intent intent = new Intent(LobbyActivity.this, GameActivity.class);
                intent.putExtra("opponent", opponent);
                intent.putExtra("myPlayer", myPlayer);
                intent.putExtra("myUsername", etUsername.getText().toString().trim());
                startActivity(intent);
                break;
            case "ERROR":
                runOnUiThread(() -> btnRegister.setEnabled(true));
                break;
        }
    }

    private void showInviteDialog(String fromUser) {
        runOnUiThread(() ->
                new AlertDialog.Builder(this)
                        .setTitle("Poziv")
                        .setMessage(fromUser + " te izaziva")
                        .setPositiveButton("Prihvati", (d, w) ->
                                sharedConnection.send("INVITE_RESPONSE|" + fromUser + "|ACCEPT"))
                        .setNegativeButton("Odbij", (d, w) ->
                                sharedConnection.send("INVITE_RESPONSE|" + fromUser + "|DECLINE"))
                        .show()
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}