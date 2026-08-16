package com.example.musiclibrarydb;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.musiclibrarydb.db.DatabaseHelper;

import java.util.ArrayList;

public class SongActivity extends AppCompatActivity {
    private DatabaseHelper databaseHelper;
    private EditText titleInput;
    private Spinner artistSpinner;
    private Spinner genreSpinner;
    private ListView songListView;
    private ArrayList<Integer> artistIds = new ArrayList<>();
    private ArrayList<Integer> genreIds = new ArrayList<>();
    private int selectedSongId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);
        databaseHelper = new DatabaseHelper(this);

        titleInput = findViewById(R.id.songTitleInput);
        artistSpinner = findViewById(R.id.songArtistSpinner);
        genreSpinner = findViewById(R.id.songGenreSpinner);
        songListView = findViewById(R.id.songListView);

        Button saveButton = findViewById(R.id.saveSongButton);
        Button deleteButton = findViewById(R.id.deleteSongButton);
        Button backButton = findViewById(R.id.backButton);
        saveButton.setOnClickListener(v -> saveSong());
        deleteButton.setOnClickListener(v -> deleteSong());
        backButton.setOnClickListener(v -> finish());

        songListView.setOnItemClickListener((parent, view, position, id) -> {
            selectedSongId = (int) id;
            String value = (String) parent.getItemAtPosition(position);
            int titleStart = value.indexOf(": ") + 2;
            int titleEnd = value.indexOf(" - ", titleStart);
            titleInput.setText(value.substring(titleStart, titleEnd));
            Toast.makeText(this, "Pesma izabrana za brisanje ili izmenu", Toast.LENGTH_SHORT).show();
        });

        loadArtists();
        loadGenres();
        loadSongs();
    }

    private void loadArtists() {
        ArrayList<String> names = new ArrayList<>();
        artistIds.clear();
        Cursor cursor = databaseHelper.getAllArtists();
        while (cursor.moveToNext()) {
            artistIds.add(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            names.add(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        }
        cursor.close();
        artistSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names));
    }

    private void loadGenres() {
        ArrayList<String> names = new ArrayList<>();
        genreIds.clear();
        Cursor cursor = databaseHelper.getAllGenres();
        while (cursor.moveToNext()) {
            genreIds.add(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            names.add(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        }
        cursor.close();
        genreSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names));
    }

    private void saveSong() {
        String title = titleInput.getText().toString().trim();
        if (title.isEmpty() || artistIds.isEmpty() || genreIds.isEmpty()) {
            Toast.makeText(this, "Unesite naziv i prvo dodajte umetnika i zanr", Toast.LENGTH_SHORT).show();
            return;
        }

        int artistId = artistIds.get(artistSpinner.getSelectedItemPosition());
        int genreId = genreIds.get(genreSpinner.getSelectedItemPosition());
        long result;
        if (selectedSongId == -1) {
            result = databaseHelper.insertSong(title, artistId, genreId);
        } else {
            result = databaseHelper.updateSong(selectedSongId, title, artistId, genreId);
            selectedSongId = -1;
        }

        if (result != -1) {
            titleInput.setText("");
            loadSongs();
            Toast.makeText(this, "Pesma sacuvana", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Greska pri cuvanju pesme", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteSong() {
        if (selectedSongId == -1) {
            Toast.makeText(this, "Izaberite pesmu iz liste", Toast.LENGTH_SHORT).show();
            return;
        }
        databaseHelper.deleteSong(selectedSongId);
        selectedSongId = -1;
        loadSongs();
        Toast.makeText(this, "Pesma obrisana", Toast.LENGTH_SHORT).show();
    }

    private void loadSongs() {
        ArrayList<String> songs = new ArrayList<>();
        Cursor cursor = databaseHelper.getAllSongs();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String title = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String artist = cursor.getString(cursor.getColumnIndexOrThrow("artist_name"));
            String genre = cursor.getString(cursor.getColumnIndexOrThrow("genre_name"));
            songs.add(id + ": " + title + " - " + artist + " (" + genre + ")");
        }
        cursor.close();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, songs) {
            @Override
            public long getItemId(int position) {
                String value = getItem(position);
                return Integer.parseInt(value.substring(0, value.indexOf(':')));
            }
        };
        songListView.setAdapter(adapter);
    }
}