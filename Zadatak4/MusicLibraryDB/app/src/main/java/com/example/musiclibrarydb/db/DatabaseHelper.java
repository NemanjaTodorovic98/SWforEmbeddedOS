package com.example.musiclibrarydb.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "music_library.db";
    private static final int DATABASE_VERSION = 3;

    public static final String TABLE_USERS = "users";
    public static final String TABLE_ARTISTS = "artists";
    public static final String TABLE_GENRES = "genres";
    public static final String TABLE_SONGS = "songs";
    public static final String TABLE_PLAYLISTS = "playlists";
    public static final String TABLE_PLAYLIST_SONGS = "playlist_songs";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_ARTIST_ID = "artist_id";
    public static final String COLUMN_GENRE_ID = "genre_id";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_PLAYLIST_ID = "playlist_id";
    public static final String COLUMN_SONG_ID = "song_id";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME + " TEXT, "
                + COLUMN_PASSWORD + " TEXT"
                + ")";

        String createArtistsTable = "CREATE TABLE " + TABLE_ARTISTS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME + " TEXT"
                + ")";

        String createGenresTable = "CREATE TABLE " + TABLE_GENRES + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME + " TEXT"
                + ")";

            String createSongsTable = "CREATE TABLE " + TABLE_SONGS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME + " TEXT NOT NULL, "
                + COLUMN_ARTIST_ID + " INTEGER NOT NULL, "
                + COLUMN_GENRE_ID + " INTEGER NOT NULL, "
                + "FOREIGN KEY (" + COLUMN_ARTIST_ID + ") REFERENCES " + TABLE_ARTISTS + "(" + COLUMN_ID + "), "
                + "FOREIGN KEY (" + COLUMN_GENRE_ID + ") REFERENCES " + TABLE_GENRES + "(" + COLUMN_ID + ")"
                + ")";

        db.execSQL(createUsersTable);
        db.execSQL(createArtistsTable);
        db.execSQL(createGenresTable);
        db.execSQL(createSongsTable);
        createPlaylistTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("CREATE TABLE " + TABLE_SONGS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME + " TEXT NOT NULL, "
                + COLUMN_ARTIST_ID + " INTEGER NOT NULL, "
                + COLUMN_GENRE_ID + " INTEGER NOT NULL, "
                + "FOREIGN KEY (" + COLUMN_ARTIST_ID + ") REFERENCES " + TABLE_ARTISTS + "(" + COLUMN_ID + "), "
                + "FOREIGN KEY (" + COLUMN_GENRE_ID + ") REFERENCES " + TABLE_GENRES + "(" + COLUMN_ID + ")"
                + ")");
        }
        if (oldVersion < 3) {
            createPlaylistTables(db);
        }
    }

    private void createPlaylistTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PLAYLISTS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME + " TEXT NOT NULL, "
                + COLUMN_USER_ID + " INTEGER NOT NULL, "
                + "FOREIGN KEY (" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ")"
                + ")");
        db.execSQL("CREATE TABLE " + TABLE_PLAYLIST_SONGS + " ("
                + COLUMN_PLAYLIST_ID + " INTEGER NOT NULL, "
                + COLUMN_SONG_ID + " INTEGER NOT NULL, "
                + "PRIMARY KEY (" + COLUMN_PLAYLIST_ID + ", " + COLUMN_SONG_ID + "), "
                + "FOREIGN KEY (" + COLUMN_PLAYLIST_ID + ") REFERENCES " + TABLE_PLAYLISTS + "(" + COLUMN_ID + "), "
                + "FOREIGN KEY (" + COLUMN_SONG_ID + ") REFERENCES " + TABLE_SONGS + "(" + COLUMN_ID + ")"
                + ")");
    }

    public long insertUser(String name, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_PASSWORD, password);
        return db.insert(TABLE_USERS, null, values);
    }

    public boolean userExists(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_ID},
                COLUMN_NAME + " = ?",
                new String[]{name},
                null,
                null,
                null
        );

        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public boolean checkUser(String name, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_ID},
                COLUMN_NAME + " = ? AND " + COLUMN_PASSWORD + " = ?",
                new String[]{name, password},
                null,
                null,
                null
        );

        boolean valid = cursor.moveToFirst();
        cursor.close();
        return valid;
    }

    public boolean loginUser(String name, String password) {
        if (name == null || password == null) {
            return false;
        }

        if (!userExists(name)) {
            long id = insertUser(name, password);
            return id != -1;
        }

        return checkUser(name, password);
    }

    public int getUserId(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_ID}, COLUMN_NAME + " = ?",
                new String[]{name}, null, null, null);
        int id = cursor.moveToFirst() ? cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)) : -1;
        cursor.close();
        return id;
    }

    public long insertArtist(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        return db.insert(TABLE_ARTISTS, null, values);
    }

    public long insertGenre(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        return db.insert(TABLE_GENRES, null, values);
    }

    public int updateArtist(int id, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        return db.update(TABLE_ARTISTS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public int updateGenre(int id, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        return db.update(TABLE_GENRES, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public Cursor getAllArtists() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_ARTISTS, null, null, null, null, null, COLUMN_NAME + " ASC");
    }

    public Cursor getAllGenres() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_GENRES, null, null, null, null, null, COLUMN_NAME + " ASC");
    }

    public Cursor searchArtists(String text) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_ARTISTS, null, COLUMN_NAME + " LIKE ?",
                new String[]{"%" + text + "%"}, null, null, COLUMN_NAME + " ASC");
    }

    public Cursor searchGenres(String text) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_GENRES, null, COLUMN_NAME + " LIKE ?",
                new String[]{"%" + text + "%"}, null, null, COLUMN_NAME + " ASC");
    }

    public long insertSong(String title, int artistId, int genreId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, title);
        values.put(COLUMN_ARTIST_ID, artistId);
        values.put(COLUMN_GENRE_ID, genreId);
        return db.insert(TABLE_SONGS, null, values);
    }

    public int updateSong(int id, String title, int artistId, int genreId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, title);
        values.put(COLUMN_ARTIST_ID, artistId);
        values.put(COLUMN_GENRE_ID, genreId);
        return db.update(TABLE_SONGS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public Cursor getAllSongs() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT songs.id, songs.name, artists.name AS artist_name, genres.name AS genre_name "
                + "FROM songs INNER JOIN artists ON songs.artist_id = artists.id "
                + "INNER JOIN genres ON songs.genre_id = genres.id ORDER BY songs.name ASC", null);
    }

    public Cursor searchSongs(String text) {
        SQLiteDatabase db = this.getReadableDatabase();
        String pattern = "%" + text + "%";
        return db.rawQuery("SELECT songs.id, songs.name, artists.name AS artist_name, genres.name AS genre_name "
                + "FROM songs INNER JOIN artists ON songs.artist_id = artists.id "
                + "INNER JOIN genres ON songs.genre_id = genres.id "
                + "WHERE songs.name LIKE ? OR artists.name LIKE ? OR genres.name LIKE ? "
                + "ORDER BY songs.name ASC", new String[]{pattern, pattern, pattern});
    }

    public int deleteSong(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PLAYLIST_SONGS, COLUMN_SONG_ID + " = ?", new String[]{String.valueOf(id)});
        return db.delete(TABLE_SONGS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public int deleteArtist(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_SONGS, new String[]{COLUMN_ID},
                COLUMN_ARTIST_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null, "1");
        boolean used = cursor.moveToFirst();
        cursor.close();
        if (used) {
            return -1;
        }
        return db.delete(TABLE_ARTISTS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public int deleteGenre(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_SONGS, new String[]{COLUMN_ID},
                COLUMN_GENRE_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null, "1");
        boolean used = cursor.moveToFirst();
        cursor.close();
        if (used) {
            return -1;
        }
        return db.delete(TABLE_GENRES, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public long insertPlaylist(String name, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_USER_ID, userId);
        return db.insert(TABLE_PLAYLISTS, null, values);
    }

    public int updatePlaylist(int id, String name, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        return db.update(TABLE_PLAYLISTS, values,
                COLUMN_ID + " = ? AND " + COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(id), String.valueOf(userId)});
    }

    public int deletePlaylist(int id, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PLAYLIST_SONGS, COLUMN_PLAYLIST_ID + " = ?", new String[]{String.valueOf(id)});
        return db.delete(TABLE_PLAYLISTS,
                COLUMN_ID + " = ? AND " + COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(id), String.valueOf(userId)});
    }

    public Cursor getPlaylistsForUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_PLAYLISTS, null, COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}, null, null, COLUMN_NAME + " ASC");
    }

    public Cursor searchPlaylistsForUser(String text, int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_PLAYLISTS, null,
                COLUMN_USER_ID + " = ? AND " + COLUMN_NAME + " LIKE ?",
                new String[]{String.valueOf(userId), "%" + text + "%"},
                null, null, COLUMN_NAME + " ASC");
    }

    public long addSongToPlaylist(int playlistId, int songId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PLAYLIST_ID, playlistId);
        values.put(COLUMN_SONG_ID, songId);
        return db.insert(TABLE_PLAYLIST_SONGS, null, values);
    }

    public int removeSongFromPlaylist(int playlistId, int songId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_PLAYLIST_SONGS,
                COLUMN_PLAYLIST_ID + " = ? AND " + COLUMN_SONG_ID + " = ?",
                new String[]{String.valueOf(playlistId), String.valueOf(songId)});
    }

    public Cursor getSongsForPlaylist(int playlistId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT songs.id, songs.name, artists.name AS artist_name, genres.name AS genre_name "
                + "FROM playlist_songs INNER JOIN songs ON playlist_songs.song_id = songs.id "
                + "INNER JOIN artists ON songs.artist_id = artists.id "
                + "INNER JOIN genres ON songs.genre_id = genres.id "
                + "WHERE playlist_songs.playlist_id = ? ORDER BY songs.name ASC",
                new String[]{String.valueOf(playlistId)});
    }
}

