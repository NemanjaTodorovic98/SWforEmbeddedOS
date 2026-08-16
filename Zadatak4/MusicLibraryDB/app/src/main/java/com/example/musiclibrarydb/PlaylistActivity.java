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

public class PlaylistActivity extends AppCompatActivity {
    private DatabaseHelper databaseHelper;
    private EditText playlistNameInput;
    private Spinner songSpinner;
    private ListView playlistListView;
    private ListView playlistSongsListView;
    private ArrayList<Integer> songIds = new ArrayList<>();
    private int userId = -1;
    private int selectedPlaylistId = -1;
    private int selectedPlaylistSongId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        databaseHelper = new DatabaseHelper(this);

        String userName = getIntent().getStringExtra("user_name");
        userId = databaseHelper.getUserId(userName == null ? "admin" : userName);
        playlistNameInput = findViewById(R.id.playlistNameInput);
        songSpinner = findViewById(R.id.playlistSongSpinner);
        playlistListView = findViewById(R.id.playlistListView);
        playlistSongsListView = findViewById(R.id.playlistSongsListView);

        findViewById(R.id.savePlaylistButton).setOnClickListener(v -> savePlaylist());
        findViewById(R.id.deletePlaylistButton).setOnClickListener(v -> deletePlaylist());
        findViewById(R.id.addSongToPlaylistButton).setOnClickListener(v -> addSong());
        findViewById(R.id.removeSongFromPlaylistButton).setOnClickListener(v -> removeSong());
        findViewById(R.id.backFromPlaylistButton).setOnClickListener(v -> finish());

        playlistListView.setOnItemClickListener((parent, view, position, id) -> {
            selectedPlaylistId = (int) id;
            String value = (String) parent.getItemAtPosition(position);
            playlistNameInput.setText(value.substring(value.indexOf(": ") + 2));
            loadPlaylistSongs();
            Toast.makeText(this, "Plejlista izabrana", Toast.LENGTH_SHORT).show();
        });
        playlistSongsListView.setOnItemClickListener((parent, view, position, id) -> selectedPlaylistSongId = (int) id);

        loadSongs();
        loadPlaylists();
    }

    private void savePlaylist() {
        String name = playlistNameInput.getText().toString().trim();
        if (name.isEmpty() || userId == -1) {
            Toast.makeText(this, "Unesite naziv plejliste", Toast.LENGTH_SHORT).show();
            return;
        }
        long result;
        if (selectedPlaylistId == -1) {
            result = databaseHelper.insertPlaylist(name, userId);
        } else {
            result = databaseHelper.updatePlaylist(selectedPlaylistId, name, userId);
            selectedPlaylistId = -1;
        }
        if (result != -1) {
            playlistNameInput.setText("");
            loadPlaylists();
            Toast.makeText(this, "Plejlista sacuvana", Toast.LENGTH_SHORT).show();
        }
    }

    private void deletePlaylist() {
        if (selectedPlaylistId == -1) {
            Toast.makeText(this, "Izaberite plejlistu", Toast.LENGTH_SHORT).show();
            return;
        }
        databaseHelper.deletePlaylist(selectedPlaylistId, userId);
        selectedPlaylistId = -1;
        loadPlaylists();
        Toast.makeText(this, "Plejlista obrisana", Toast.LENGTH_SHORT).show();
    }

    private void addSong() {
        if (selectedPlaylistId == -1 || songIds.isEmpty()) {
            Toast.makeText(this, "Izaberite plejlistu i pesmu", Toast.LENGTH_SHORT).show();
            return;
        }
        long result = databaseHelper.addSongToPlaylist(
                selectedPlaylistId, songIds.get(songSpinner.getSelectedItemPosition()));
        loadPlaylistSongs();
        if (result == -1) {
            Toast.makeText(this, "Pesma je vec u plejlisti", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Pesma dodata u plejlistu", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeSong() {
        if (selectedPlaylistId == -1 || selectedPlaylistSongId == -1) {
            Toast.makeText(this, "Izaberite pesmu iz plejliste", Toast.LENGTH_SHORT).show();
            return;
        }
        databaseHelper.removeSongFromPlaylist(selectedPlaylistId, selectedPlaylistSongId);
        selectedPlaylistSongId = -1;
        loadPlaylistSongs();
    }

    private void loadSongs() {
        ArrayList<String> names = new ArrayList<>();
        songIds.clear();
        Cursor cursor = databaseHelper.getAllSongs();
        while (cursor.moveToNext()) {
            songIds.add(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            names.add(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        }
        cursor.close();
        songSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names));
    }

    private void loadPlaylists() {
        ArrayList<String> names = new ArrayList<>();
        Cursor cursor = databaseHelper.getPlaylistsForUser(userId);
        while (cursor.moveToNext()) {
            names.add(cursor.getInt(cursor.getColumnIndexOrThrow("id")) + ": "
                    + cursor.getString(cursor.getColumnIndexOrThrow("name")));
        }
        cursor.close();
        playlistListView.setAdapter(createIdAdapter(names));
    }

    private void loadPlaylistSongs() {
        ArrayList<String> songs = new ArrayList<>();
        Cursor cursor = databaseHelper.getSongsForPlaylist(selectedPlaylistId);
        while (cursor.moveToNext()) {
            songs.add(cursor.getInt(cursor.getColumnIndexOrThrow("id")) + ": "
                    + cursor.getString(cursor.getColumnIndexOrThrow("name")));
        }
        cursor.close();
        playlistSongsListView.setAdapter(createIdAdapter(songs));
    }

    private ArrayAdapter<String> createIdAdapter(ArrayList<String> values) {
        return new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values) {
            @Override
            public long getItemId(int position) {
                String value = getItem(position);
                return Integer.parseInt(value.substring(0, value.indexOf(':')));
            }
        };
    }
}