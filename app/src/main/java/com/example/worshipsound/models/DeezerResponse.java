package com.example.worshipsound.models;

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

    @Override
    public String toString() {
        return "DeezerResponse{" +
                "songs=" + (songs != null ? songs.size() : 0) +
                ", total=" + total +
                '}';
    }
}
