package com.example.musiclibrarydb;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.musiclibrarydb.db.DatabaseHelper;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHelper = new DatabaseHelper(this);

        TextView titleText = findViewById(R.id.welcomeTitle);
        TextView welcomeText = findViewById(R.id.welcomeMessage);
        EditText usernameInput = findViewById(R.id.usernameInput);
        EditText passwordInput = findViewById(R.id.passwordInput);
        Button startButton = findViewById(R.id.startButton);
        Button loginButton = findViewById(R.id.loginButton);

        titleText.setText(R.string.welcome_title);
        welcomeText.setText("sql base test");
        startButton.setText(R.string.start_button);

        startButton.setOnClickListener(v -> testDatabase());

        loginButton.setOnClickListener(v -> {
            String name = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (name.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Unesite korisnicko ime i pass", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean loginOk = databaseHelper.loginUser(name, password);
            if (loginOk) {
                Toast.makeText(this, "Uspesna prijava: " + name, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Ne postoji korisnik ili je sifra pogresna", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void testDatabase() {
        String name = "admin";
        String password = "1234";

        if (!databaseHelper.userExists(name)) {
            long result = databaseHelper.insertUser(name, password);
            if (result != -1) {
                Toast.makeText(this, "DB OK: user saevd", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "DB ERROR: user not saved", Toast.LENGTH_SHORT).show();
            }
        } else {
            boolean valid = databaseHelper.checkUser(name, password);
            if (valid) {
                Toast.makeText(this, "DB OK: user already exists", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "DB ERROR: data not valid", Toast.LENGTH_SHORT).show();
            }
        }
    }
}