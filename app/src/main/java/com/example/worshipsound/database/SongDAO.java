package com.example.worshipsound.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.worshipsound.models.Song;
import com.example.worshipsound.models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for managing songs and users in SQLite database
 */
public class SongDAO {
    private static final String TAG = "SongDAO";
    private final DatabaseHelper dbHelper;
    private static SongDAO instance;

    private SongDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * Get singleton instance of SongDAO
     */
    public static synchronized SongDAO getInstance(Context context) {
        if (instance == null) {
            instance = new SongDAO(context);
        }
        return instance;
    }

    // User-related methods
    
    /**
     * Insert a new user into the database
     */
    public long insertUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long userId = -1;
        
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_USERNAME, user.getUsername());
            values.put(DatabaseHelper.COLUMN_EMAIL, user.getEmail());
            values.put(DatabaseHelper.COLUMN_PASSWORD, user.getPassword());
            values.put(DatabaseHelper.COLUMN_FIRST_NAME, user.getFirstName());
            values.put(DatabaseHelper.COLUMN_LAST_NAME, user.getLastName());
            values.put(DatabaseHelper.COLUMN_CREATED_AT, user.getCreatedAt());
            values.put(DatabaseHelper.COLUMN_IS_DARK_THEME, user.isDarkTheme() ? 1 : 0);
            
            userId = db.insert(DatabaseHelper.TABLE_USERS, null, values);
            user.setId((int) userId);
            
            Log.d(TAG, "User inserted with ID: " + userId);
        } catch (Exception e) {
            Log.e(TAG, "Error inserting user", e);
        }
        
        return userId;
    }

    /**
     * Get user by username or email
     */
    public User getUser(String usernameOrEmail, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        User user = null;
        
        String selection = "(" + DatabaseHelper.COLUMN_USERNAME + " = ? OR " + 
                          DatabaseHelper.COLUMN_EMAIL + " = ?) AND " + 
                          DatabaseHelper.COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {usernameOrEmail, usernameOrEmail, password};
        
        try (Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                null,
                selection,
                selectionArgs,
                null, null, null
        )) {
            if (cursor.moveToFirst()) {
                user = createUserFromCursor(cursor);
                Log.d(TAG, "User found: " + user.getUsername());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting user", e);
        }
        
        return user;
    }

    /**
     * Check if username or email already exists
     */
    public boolean userExists(String username, String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        boolean exists = false;
        
        String selection = DatabaseHelper.COLUMN_USERNAME + " = ? OR " + 
                          DatabaseHelper.COLUMN_EMAIL + " = ?";
        String[] selectionArgs = {username, email};
        
        try (Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.COLUMN_USER_ID},
                selection,
                selectionArgs,
                null, null, null
        )) {
            exists = cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error checking if user exists", e);
        }
        
        return exists;
    }

    /**
     * Update user theme preference
     */
    public boolean updateUserTheme(int userId, boolean isDarkTheme) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_IS_DARK_THEME, isDarkTheme ? 1 : 0);
        
        String selection = DatabaseHelper.COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};
        
        int rowsAffected = db.update(DatabaseHelper.TABLE_USERS, values, selection, selectionArgs);
        
        Log.d(TAG, "Updated theme for user " + userId + ": " + isDarkTheme);
        return rowsAffected > 0;
    }

    // Song-related methods
    
    /**
     * Insert or update a song in the database
     */
    public long insertSong(Song song) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long songId = -1;
        
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_DEEZER_ID, song.getId());
            values.put(DatabaseHelper.COLUMN_TITLE, song.getTitle());
            values.put(DatabaseHelper.COLUMN_ARTIST, song.getArtistName());
            values.put(DatabaseHelper.COLUMN_ALBUM, song.getAlbumTitle());
            values.put(DatabaseHelper.COLUMN_DURATION, song.getDuration());
            values.put(DatabaseHelper.COLUMN_PREVIEW_URL, song.getPreviewUrl());
            values.put(DatabaseHelper.COLUMN_ALBUM_COVER, song.getAlbumCover());
            values.put(DatabaseHelper.COLUMN_IS_LIKED, song.isLiked() ? 1 : 0);
            values.put(DatabaseHelper.COLUMN_PLAYLIST_NAME, song.getPlaylistName() != null ? song.getPlaylistName() : "Liked Songs");
            values.put(DatabaseHelper.COLUMN_ADDED_AT, System.currentTimeMillis());
            
            songId = db.insertWithOnConflict(DatabaseHelper.TABLE_SONGS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            
            Log.d(TAG, "Song inserted/updated with ID: " + songId);
        } catch (Exception e) {
            Log.e(TAG, "Error inserting song", e);
        }
        
        return songId;
    }

    /**
     * Get all liked songs
     */
    public List<Song> getLikedSongs() {
        return getSongsByPlaylist("Liked Songs");
    }

    /**
     * Get songs by playlist name
     */
    public List<Song> getSongsByPlaylist(String playlistName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Song> songs = new ArrayList<>();
        
        String selection = DatabaseHelper.COLUMN_PLAYLIST_NAME + " = ?";
        String[] selectionArgs = {playlistName};
        String orderBy = DatabaseHelper.COLUMN_ADDED_AT + " DESC";
        
        try (Cursor cursor = db.query(
                DatabaseHelper.TABLE_SONGS,
                null,
                selection,
                selectionArgs,
                null, null, orderBy
        )) {
            while (cursor.moveToNext()) {
                Song song = createSongFromCursor(cursor);
                songs.add(song);
            }
            Log.d(TAG, "Retrieved " + songs.size() + " songs from playlist: " + playlistName);
        } catch (Exception e) {
            Log.e(TAG, "Error getting songs by playlist", e);
        }
        
        return songs;
    }

    /**
     * Check if song is already liked/saved
     */
    public boolean isSongLiked(long deezerId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        boolean isLiked = false;
        
        String selection = DatabaseHelper.COLUMN_DEEZER_ID + " = ? AND " + 
                          DatabaseHelper.COLUMN_IS_LIKED + " = 1";
        String[] selectionArgs = {String.valueOf(deezerId)};
        
        try (Cursor cursor = db.query(
                DatabaseHelper.TABLE_SONGS,
                new String[]{DatabaseHelper.COLUMN_SONG_ID},
                selection,
                selectionArgs,
                null, null, null
        )) {
            isLiked = cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error checking if song is liked", e);
        }
        
        return isLiked;
    }

    /**
     * Remove song from database
     */
    public boolean removeSong(long deezerId, String playlistName) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        String selection = DatabaseHelper.COLUMN_DEEZER_ID + " = ? AND " + 
                          DatabaseHelper.COLUMN_PLAYLIST_NAME + " = ?";
        String[] selectionArgs = {String.valueOf(deezerId), playlistName};
        
        int rowsDeleted = db.delete(DatabaseHelper.TABLE_SONGS, selection, selectionArgs);
        
        Log.d(TAG, "Removed song " + deezerId + " from " + playlistName + ": " + (rowsDeleted > 0));
        return rowsDeleted > 0;
    }

    /**
     * Get all available playlists
     */
    public List<String> getPlaylistNames() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<String> playlists = new ArrayList<>();
        
        try (Cursor cursor = db.query(
                DatabaseHelper.TABLE_PLAYLISTS,
                new String[]{"name"},
                null, null, null, null,
                "created_at ASC"
        )) {
            while (cursor.moveToNext()) {
                playlists.add(cursor.getString(0));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting playlist names", e);
        }
        
        return playlists;
    }

    /**
     * Clear all songs from database (for testing)
     */
    public void clearAllSongs() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_SONGS, null, null);
        Log.d(TAG, "All songs cleared from database");
    }

    // Helper methods
    
    private User createUserFromCursor(Cursor cursor) {
        User user = new User();
        user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID)));
        user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USERNAME)));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL)));
        user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PASSWORD)));
        user.setFirstName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FIRST_NAME)));
        user.setLastName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LAST_NAME)));
        user.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATED_AT)));
        user.setDarkTheme(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IS_DARK_THEME)) == 1);
        return user;
    }

    private Song createSongFromCursor(Cursor cursor) {
        long deezerId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DEEZER_ID));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITLE));
        String artist = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ARTIST));
        String album = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ALBUM));
        String previewUrl = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PREVIEW_URL));
        int duration = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DURATION));
        String albumCover = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ALBUM_COVER));
        
        Song song = new Song(deezerId, title, artist, album, previewUrl, duration, albumCover);
        song.setLiked(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IS_LIKED)) == 1);
        song.setPlaylistName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PLAYLIST_NAME)));
        
        return song;
    }
}
