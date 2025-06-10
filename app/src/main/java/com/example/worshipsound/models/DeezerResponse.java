package com.example.worshipsound.models;

import com.example.worshipsound.utils.SpiritualSongFilter;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Model class for Deezer API search response
 */
public class DeezerResponse {
    @SerializedName("data")
    private List<Song> songs;

    @SerializedName("total")
    private int total;

    @SerializedName("next")
    private String next;

    @SerializedName("prev")
    private String prev;

    // Constructors
    public DeezerResponse() {}

    // Getters and Setters
    public List<Song> getSongs() { return songs; }
    public void setSongs(List<Song> songs) { this.songs = songs; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public String getNext() { return next; }
    public void setNext(String next) { this.next = next; }

    public String getPrev() { return prev; }
    public void setPrev(String prev) { this.prev = prev; }

    public boolean hasData() {
        return songs != null && !songs.isEmpty();
    }

    /**
     * Get only spiritual/worship songs from the response
     * @return Filtered list containing only spiritual songs
     */
    public List<Song> getSpiritualSongs() {
        if (songs == null || songs.isEmpty()) {
            return songs;
        }
        return SpiritualSongFilter.filterSpiritualSongs(songs);
    }

    /**
     * Check if response has spiritual songs
     * @return true if contains at least one spiritual song
     */
    public boolean hasSpiritualData() {
        List<Song> spiritualSongs = getSpiritualSongs();
        return spiritualSongs != null && !spiritualSongs.isEmpty();
    }

    /**
     * Get count of spiritual songs in response
     * @return Number of spiritual songs
     */
    public int getSpiritualSongCount() {
        List<Song> spiritualSongs = getSpiritualSongs();
        return spiritualSongs != null ? spiritualSongs.size() : 0;
    }

    /**
     * Filter songs to only include high-quality spiritual content
     * @param minimumScore Minimum spiritual score (0-100)
     * @return Filtered list of high-quality spiritual songs
     */
    public List<Song> getHighQualitySpiritualSongs(int minimumScore) {
        List<Song> spiritualSongs = getSpiritualSongs();
        if (spiritualSongs == null || spiritualSongs.isEmpty()) {
            return spiritualSongs;
        }

        return spiritualSongs.stream()
            .filter(song -> SpiritualSongFilter.calculateSpiritualScore(song) >= minimumScore)
            .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public String toString() {
        return "DeezerResponse{" +
                "songs=" + (songs != null ? songs.size() : 0) +
                ", total=" + total +
                '}';
    }
}
