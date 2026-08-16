package com.example.musiclibrarydb;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.example.musiclibrarydb.db.DatabaseHelper;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {
    private DatabaseHelper databaseHelper;
    private EditText searchInput;
    private Spinner searchTypeSpinner;
    private ListView resultsListView;
    private int userId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        databaseHelper = new DatabaseHelper(this);

        String userName = getIntent().getStringExtra("user_name");
        userId = databaseHelper.getUserId(userName == null ? "admin" : userName);
        searchInput = findViewById(R.id.searchInput);
        searchTypeSpinner = findViewById(R.id.searchTypeSpinner);
        resultsListView = findViewById(R.id.searchResultsListView);

        findViewById(R.id.searchButton).setOnClickListener(v -> search());
        findViewById(R.id.backFromSearchButton).setOnClickListener(v -> finish());
    }

    private void search() {
        String text = searchInput.getText().toString().trim();
        String type = (String) searchTypeSpinner.getSelectedItem();
        Cursor cursor;
        if (type.equals("Pesme")) {
            cursor = databaseHelper.searchSongs(text);
        } else if (type.equals("Umetnici")) {
            cursor = databaseHelper.searchArtists(text);
        } else if (type.equals("Zanrovi")) {
            cursor = databaseHelper.searchGenres(text);
        } else {
            cursor = databaseHelper.searchPlaylistsForUser(text, userId);
        }

        ArrayList<String> results = new ArrayList<>();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            if (type.equals("Pesme")) {
                name += " - " + cursor.getString(cursor.getColumnIndexOrThrow("artist_name"))
                        + " (" + cursor.getString(cursor.getColumnIndexOrThrow("genre_name")) + ")";
            }
            results.add(id + ": " + name);
        }
        cursor.close();
        resultsListView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, results));
    }
}