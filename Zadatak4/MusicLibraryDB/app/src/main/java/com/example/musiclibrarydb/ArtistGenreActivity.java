package com.example.musiclibrarydb;

import android.database.Cursor;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.musiclibrarydb.db.DatabaseHelper;

import java.util.ArrayList;

public class ArtistGenreActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private EditText artistInput;
    private EditText genreInput;
    private ListView artistListView;
    private ListView genreListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_genre);

        databaseHelper = new DatabaseHelper(this);

        artistInput = findViewById(R.id.artistInput);
        genreInput = findViewById(R.id.genreInput);
        artistListView = findViewById(R.id.artistListView);
        genreListView = findViewById(R.id.genreListView);

        Button addArtistButton = findViewById(R.id.addArtistButton);
        Button addGenreButton = findViewById(R.id.addGenreButton);
        Button songsButton = findViewById(R.id.songsButton);

        addArtistButton.setOnClickListener(v -> addArtist());
        addGenreButton.setOnClickListener(v -> addGenre());
        songsButton.setOnClickListener(v -> startActivity(new Intent(this, SongActivity.class)));

        loadArtists();
        loadGenres();
    }

    private void addArtist() {
        String name = artistInput.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Unesite naziv izvodjac", Toast.LENGTH_SHORT).show();
            return;
        }

        long id = databaseHelper.insertArtist(name);
        if (id != -1) {
            artistInput.setText("");
            loadArtists();
            Toast.makeText(this, "Izvodac dodat", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "greska pri dodavanju", Toast.LENGTH_SHORT).show();
        }
    }

    private void addGenre() {
        String name = genreInput.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Unesite naziv zanra", Toast.LENGTH_SHORT).show();
            return;
        }

        long id = databaseHelper.insertGenre(name);
        if (id != -1) {
            genreInput.setText("");
            loadGenres();
            Toast.makeText(this, "Zanr dodat", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "greska pri dodavanju zanra", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadArtists() {
        Cursor cursor = databaseHelper.getAllArtists();
        ArrayList<String> artists = new ArrayList<>();

        while (cursor.moveToNext()) {
            int idIndex = cursor.getColumnIndexOrThrow("id");
            int nameIndex = cursor.getColumnIndexOrThrow("name");
            int id = cursor.getInt(idIndex);
            String name = cursor.getString(nameIndex);
            artists.add(id + ": " + name);
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, artists);
        artistListView.setAdapter(adapter);
    }

    private void loadGenres() {
        Cursor cursor = databaseHelper.getAllGenres();
        ArrayList<String> genres = new ArrayList<>();

        while (cursor.moveToNext()) {
            int idIndex = cursor.getColumnIndexOrThrow("id");
            int nameIndex = cursor.getColumnIndexOrThrow("name");
            int id = cursor.getInt(idIndex);
            String name = cursor.getString(nameIndex);
            genres.add(id + ": " + name);
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, genres);
        genreListView.setAdapter(adapter);
    }
}
