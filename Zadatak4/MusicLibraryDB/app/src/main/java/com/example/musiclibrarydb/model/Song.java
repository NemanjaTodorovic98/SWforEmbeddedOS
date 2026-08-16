package com.example.musiclibrarydb.model;

public class Song {
    private int id;
    private String title;
    private int artistId;
    private int genreId;

    public Song() {
    }

    public Song(int id, String title, int artistId, int genreId) {
        this.id = id;
        this.title = title;
        this.artistId = artistId;
        this.genreId = genreId;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getArtistId() {
        return artistId;
    }

    public int getGenreId() {
        return genreId;
    }
}