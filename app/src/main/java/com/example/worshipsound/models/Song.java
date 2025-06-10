package com.example.worshipsound.models;

import com.google.gson.annotations.SerializedName;

/**
 * Model class representing a song from Deezer API
 */
public class Song {
    @SerializedName("id")
    private long id;

    @SerializedName("title")
    private String title;

    @SerializedName("duration")
    private int duration;

    @SerializedName("preview")
    private String previewUrl;

    @SerializedName("album")
    private Album album;

    @SerializedName("artist")
    private Artist artist;

    // Local database fields
    private boolean isLiked = false;
    private String playlistName;

    // Constructors
    public Song() {}

    public Song(long id, String title, String artistName, String albumTitle, 
                String previewUrl, int duration, String albumCover) {
        this.id = id;
        this.title = title;
        this.duration = duration;
        this.previewUrl = previewUrl;
        this.artist = new Artist();
        this.artist.setName(artistName);
        this.album = new Album();
        this.album.setTitle(albumTitle);
        this.album.setCoverMedium(albumCover);
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public String getPreviewUrl() { return previewUrl; }
    public void setPreviewUrl(String previewUrl) { this.previewUrl = previewUrl; }

    public Album getAlbum() { return album; }
    public void setAlbum(Album album) { this.album = album; }

    public Artist getArtist() { return artist; }
    public void setArtist(Artist artist) { this.artist = artist; }

    public boolean isLiked() { return isLiked; }
    public void setLiked(boolean liked) { isLiked = liked; }

    public String getPlaylistName() { return playlistName; }
    public void setPlaylistName(String playlistName) { this.playlistName = playlistName; }

    // Helper methods
    public String getArtistName() {
        return artist != null ? artist.getName() : "Unknown Artist";
    }

    public String getAlbumTitle() {
        return album != null ? album.getTitle() : "Unknown Album";
    }

    public String getAlbumCover() {
        return album != null ? album.getCoverMedium() : "";
    }

    public String getDurationFormatted() {
        int minutes = duration / 60;
        int seconds = duration % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    // Inner classes for nested objects
    public static class Album {
        @SerializedName("title")
        private String title;

        @SerializedName("cover_medium")
        private String coverMedium;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getCoverMedium() { return coverMedium; }
        public void setCoverMedium(String coverMedium) { this.coverMedium = coverMedium; }
    }

    public static class Artist {
        @SerializedName("name")
        private String name;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", artist='" + getArtistName() + '\'' +
                ", album='" + getAlbumTitle() + '\'' +
                ", duration=" + duration +
                '}';
    }
}
