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
    private int selectedArtistId = -1;
    private int selectedGenreId = -1;

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
        Button updateArtistButton = findViewById(R.id.updateArtistButton);
        Button deleteArtistButton = findViewById(R.id.deleteArtistButton);
        Button addGenreButton = findViewById(R.id.addGenreButton);
        Button updateGenreButton = findViewById(R.id.updateGenreButton);
        Button deleteGenreButton = findViewById(R.id.deleteGenreButton);
        Button songsButton = findViewById(R.id.songsButton);
        Button playlistsButton = findViewById(R.id.playlistsButton);

        addArtistButton.setOnClickListener(v -> addArtist());
        updateArtistButton.setOnClickListener(v -> updateArtist());
        deleteArtistButton.setOnClickListener(v -> deleteArtist());
        addGenreButton.setOnClickListener(v -> addGenre());
        updateGenreButton.setOnClickListener(v -> updateGenre());
        deleteGenreButton.setOnClickListener(v -> deleteGenre());
        songsButton.setOnClickListener(v -> startActivity(new Intent(this, SongActivity.class)));
        playlistsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, PlaylistActivity.class);
            intent.putExtra("user_name", getIntent().getStringExtra("user_name"));
            startActivity(intent);
        });

        artistListView.setOnItemClickListener((parent, view, position, id) -> {
            selectedArtistId = (int) id;
            String value = (String) parent.getItemAtPosition(position);
            artistInput.setText(value.substring(value.indexOf(": ") + 2));
        });
        genreListView.setOnItemClickListener((parent, view, position, id) -> {
            selectedGenreId = (int) id;
            String value = (String) parent.getItemAtPosition(position);
            genreInput.setText(value.substring(value.indexOf(": ") + 2));
        });

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
            selectedArtistId = -1;
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
            selectedGenreId = -1;
            loadGenres();
            Toast.makeText(this, "Zanr dodat", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "greska pri dodavanju zanra", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateArtist() {
        String name = artistInput.getText().toString().trim();
        if (selectedArtistId == -1 || name.isEmpty()) {
            Toast.makeText(this, "Izaberite umetnika i unesite naziv", Toast.LENGTH_SHORT).show();
            return;
        }
        databaseHelper.updateArtist(selectedArtistId, name);
        selectedArtistId = -1;
        artistInput.setText("");
        loadArtists();
        Toast.makeText(this, "Umetnik izmenjen", Toast.LENGTH_SHORT).show();
    }

    private void deleteArtist() {
        if (selectedArtistId == -1) {
            Toast.makeText(this, "Izaberite umetnika iz liste", Toast.LENGTH_SHORT).show();
            return;
        }
        int deleted = databaseHelper.deleteArtist(selectedArtistId);
        if (deleted == -1) {
            Toast.makeText(this, "Umetnik izvodi pesmu iz liste", Toast.LENGTH_SHORT).show();
            return;
        }
        selectedArtistId = -1;
        artistInput.setText("");
        loadArtists();
        Toast.makeText(this, "Umetnik obrisan", Toast.LENGTH_SHORT).show();
    }

    private void updateGenre() {
        String name = genreInput.getText().toString().trim();
        if (selectedGenreId == -1 || name.isEmpty()) {
            Toast.makeText(this, "Izaberite zanr i unesite naziv", Toast.LENGTH_SHORT).show();
            return;
        }
        databaseHelper.updateGenre(selectedGenreId, name);
        selectedGenreId = -1;
        genreInput.setText("");
        loadGenres();
        Toast.makeText(this, "Zanr promenjen", Toast.LENGTH_SHORT).show();
    }

    private void deleteGenre() {
        if (selectedGenreId == -1) {
            Toast.makeText(this, "Izaberite zanr iz liste", Toast.LENGTH_SHORT).show();
            return;
        }
        int deleted = databaseHelper.deleteGenre(selectedGenreId);
        if (deleted == -1) {
            Toast.makeText(this, "Zanr se koristi u pesmi", Toast.LENGTH_SHORT).show();
            return;
        }
        selectedGenreId = -1;
        genreInput.setText("");
        loadGenres();
        Toast.makeText(this, "Zanr obrisan", Toast.LENGTH_SHORT).show();
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

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, artists) {
            @Override
            public long getItemId(int position) {
                String value = getItem(position);
                return Integer.parseInt(value.substring(0, value.indexOf(':')));
            }
        };
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

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, genres) {
            @Override
            public long getItemId(int position) {
                String value = getItem(position);
                return Integer.parseInt(value.substring(0, value.indexOf(':')));
            }
        };
        genreListView.setAdapter(adapter);
    }
}
