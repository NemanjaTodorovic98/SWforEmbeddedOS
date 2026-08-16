package com.example.musiclibrarydb.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "music_library.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_USERS = "users";
    public static final String TABLE_ARTISTS = "artists";
    public static final String TABLE_GENRES = "genres";
    public static final String TABLE_SONGS = "songs";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_ARTIST_ID = "artist_id";
    public static final String COLUMN_GENRE_ID = "genre_id";

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

    public Cursor getAllArtists() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_ARTISTS, null, null, null, null, null, COLUMN_NAME + " ASC");
    }

    public Cursor getAllGenres() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_GENRES, null, null, null, null, null, COLUMN_NAME + " ASC");
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

    public int deleteSong(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_SONGS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public int deleteArtist(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_ARTISTS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public int deleteGenre(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_GENRES, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }
}

