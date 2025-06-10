package com.example.worshipsound.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * SQLite database helper for WorshipSound app
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    
    // Database info
    private static final String DATABASE_NAME = "worship_sound.db";
    private static final int DATABASE_VERSION = 1;
    
    // Table names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_SONGS = "songs";
    public static final String TABLE_PLAYLISTS = "playlists";
    
    // User table columns
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_FIRST_NAME = "first_name";
    public static final String COLUMN_LAST_NAME = "last_name";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_IS_DARK_THEME = "is_dark_theme";
    
    // Song table columns
    public static final String COLUMN_SONG_ID = "song_id";
    public static final String COLUMN_DEEZER_ID = "deezer_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_ARTIST = "artist";
    public static final String COLUMN_ALBUM = "album";
    public static final String COLUMN_DURATION = "duration";
    public static final String COLUMN_PREVIEW_URL = "preview_url";
    public static final String COLUMN_ALBUM_COVER = "album_cover";
    public static final String COLUMN_IS_LIKED = "is_liked";
    public static final String COLUMN_PLAYLIST_NAME = "playlist_name";
    public static final String COLUMN_ADDED_AT = "added_at";
    
    // Create table statements
    private static final String CREATE_USER_TABLE = 
        "CREATE TABLE " + TABLE_USERS + " (" +
        COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COLUMN_USERNAME + " TEXT UNIQUE NOT NULL, " +
        COLUMN_EMAIL + " TEXT UNIQUE NOT NULL, " +
        COLUMN_PASSWORD + " TEXT NOT NULL, " +
        COLUMN_FIRST_NAME + " TEXT, " +
        COLUMN_LAST_NAME + " TEXT, " +
        COLUMN_CREATED_AT + " INTEGER NOT NULL, " +
        COLUMN_IS_DARK_THEME + " INTEGER DEFAULT 0" +
        ");";
    
    private static final String CREATE_SONG_TABLE = 
        "CREATE TABLE " + TABLE_SONGS + " (" +
        COLUMN_SONG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COLUMN_DEEZER_ID + " INTEGER NOT NULL, " +
        COLUMN_TITLE + " TEXT NOT NULL, " +
        COLUMN_ARTIST + " TEXT NOT NULL, " +
        COLUMN_ALBUM + " TEXT, " +
        COLUMN_DURATION + " INTEGER, " +
        COLUMN_PREVIEW_URL + " TEXT, " +
        COLUMN_ALBUM_COVER + " TEXT, " +
        COLUMN_IS_LIKED + " INTEGER DEFAULT 0, " +
        COLUMN_PLAYLIST_NAME + " TEXT, " +
        COLUMN_ADDED_AT + " INTEGER NOT NULL, " +
        "UNIQUE(" + COLUMN_DEEZER_ID + ", " + COLUMN_PLAYLIST_NAME + ")" +
        ");";
    
    private static final String CREATE_PLAYLIST_TABLE = 
        "CREATE TABLE " + TABLE_PLAYLISTS + " (" +
        "playlist_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "name TEXT UNIQUE NOT NULL, " +
        "description TEXT, " +
        "created_at INTEGER NOT NULL, " +
        "song_count INTEGER DEFAULT 0" +
        ");";

    private static DatabaseHelper instance;

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Get singleton instance of DatabaseHelper
     */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database tables");
        
        try {
            db.execSQL(CREATE_USER_TABLE);
            db.execSQL(CREATE_SONG_TABLE);
            db.execSQL(CREATE_PLAYLIST_TABLE);
            
            // Insert default playlists
            insertDefaultPlaylists(db);
            
            Log.d(TAG, "Database tables created successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error creating database tables", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        
        // Drop existing tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLISTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SONGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        
        // Recreate tables
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    /**
     * Insert default playlists
     */
    private void insertDefaultPlaylists(SQLiteDatabase db) {
        long currentTime = System.currentTimeMillis();
        
        String insertLiked = "INSERT INTO " + TABLE_PLAYLISTS + 
            " (name, description, created_at) VALUES " +
            "('Liked Songs', 'Your favorite worship songs', " + currentTime + ");";
            
        String insertWorship = "INSERT INTO " + TABLE_PLAYLISTS + 
            " (name, description, created_at) VALUES " +
            "('Worship Favorites', 'Collection of worship songs', " + currentTime + ");";
            
        String insertGospel = "INSERT INTO " + TABLE_PLAYLISTS + 
            " (name, description, created_at) VALUES " +
            "('Gospel Classics', 'Classic gospel songs', " + currentTime + ");";

        try {
            db.execSQL(insertLiked);
            db.execSQL(insertWorship);
            db.execSQL(insertGospel);
            Log.d(TAG, "Default playlists inserted");
        } catch (Exception e) {
            Log.e(TAG, "Error inserting default playlists", e);
        }
    }

    /**
     * Get database instance for external access
     */
    public SQLiteDatabase getDatabase() {
        return getWritableDatabase();
    }
}
