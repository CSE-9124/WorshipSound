package com.example.worshipsound.utils;

import com.example.worshipsound.models.Song;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Utility class for filtering spiritual/worship songs from API responses
 */
public class SpiritualSongFilter {
    private static final String TAG = "SpiritualSongFilter";
    
    // Keywords that indicate spiritual/worship music
    private static final String[] SPIRITUAL_KEYWORDS = {
        // Primary spiritual terms
        "gospel", "worship", "christian", "spiritual", "praise", "hymn", 
        "jesus", "christ", "god", "lord", "holy", "church", "prayer", 
        "blessed", "faith", "salvation", "hallelujah", "alleluia", "amen",
        
        // Religious concepts
        "divine", "sacred", "sanctuary", "temple", "grace", "mercy", 
        "forgiveness", "redemption", "resurrection", "cross", "heaven",
        "angel", "miracle", "glory", "eternal", "spirit", "soul",
        
        // Worship related
        "sing", "rejoice", "celebrate", "proclaim", "testify", "witness",
        "fellowship", "congregation", "ministry", "pastor", "priest",
        
        // Indonesian spiritual terms
        "rohani", "pujian", "ibadah", "kristiani", "kristen", "yesus",
        "tuhan", "doa", "gereja", "injil", "kasih", "iman", "berkat"
    };
    
    // Artists known for spiritual/worship music
    private static final String[] SPIRITUAL_ARTISTS = {
        // International
        "hillsong", "bethel", "elevation", "planetshakers", "jesus culture",
        "chris tomlin", "casting crowns", "mercyme", "skillet", "switchfoot",
        "third day", "newsboys", "kutless", "thousand foot krutch", "tobymac",
        "lecrae", "lauren daigle", "for king and country", "we came as romans",
        "august burns red", "as i lay dying", "demon hunter", "underoath",
        
        // Indonesian
        "true worshippers", "symphony worship", "jpcc worship", "gms",
        "nikita", "franky sihombing", "giving my best", "agnus dei",
        "the overtunes", "sidney mohede", "sari simorangkir", "dewi sandra"
    };
    
    // Albums/collections that are typically spiritual
    private static final String[] SPIRITUAL_ALBUMS = {
        "worship", "praise", "gospel", "hymn", "spiritual", "christian",
        "church", "faith", "devotion", "sacred", "holy", "blessed"
    };
    
    /**
     * Filter a list of songs to only include spiritual/worship songs
     * @param songs List of songs to filter
     * @return Filtered list containing only spiritual songs
     */
    public static List<Song> filterSpiritualSongs(List<Song> songs) {
        if (songs == null || songs.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Song> spiritualSongs = new ArrayList<>();
        
        for (Song song : songs) {
            if (isSpiritualSong(song)) {
                spiritualSongs.add(song);
            }
        }
        
        return spiritualSongs;
    }
    
    /**
     * Check if a song is spiritual/worship music
     * @param song Song to check
     * @return true if the song is considered spiritual
     */
    public static boolean isSpiritualSong(Song song) {
        if (song == null) {
            return false;
        }
        
        // Check title
        String title = song.getTitle();
        if (title != null && containsSpiritualKeywords(title)) {
            return true;
        }
        
        // Check artist name
        String artistName = song.getArtistName();
        if (artistName != null && (containsSpiritualKeywords(artistName) || isSpiritualArtist(artistName))) {
            return true;
        }
        
        // Check album title
        String albumTitle = song.getAlbumTitle();
        if (albumTitle != null && containsSpiritualKeywords(albumTitle)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if text contains spiritual keywords
     * @param text Text to check
     * @return true if contains spiritual keywords
     */
    private static boolean containsSpiritualKeywords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        
        String lowerText = text.toLowerCase(Locale.getDefault());
        
        for (String keyword : SPIRITUAL_KEYWORDS) {
            if (lowerText.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if artist is known for spiritual music
     * @param artistName Artist name to check
     * @return true if artist is known for spiritual music
     */
    private static boolean isSpiritualArtist(String artistName) {
        if (artistName == null || artistName.trim().isEmpty()) {
            return false;
        }
        
        String lowerArtist = artistName.toLowerCase(Locale.getDefault());
        
        for (String spiritualArtist : SPIRITUAL_ARTISTS) {
            if (lowerArtist.contains(spiritualArtist.toLowerCase()) || 
                spiritualArtist.toLowerCase().contains(lowerArtist)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get spiritual search queries for better API results
     * @return Array of spiritual search terms
     */
    public static String[] getSpiritualSearchQueries() {
        return new String[]{
            "gospel worship", "christian praise", "spiritual hymn", 
            "jesus worship", "christ praise", "holy spirit",
            "worship songs", "praise and worship", "christian music",
            "gospel music", "contemporary christian", "church songs",
            "hillsong worship", "bethel music", "elevation worship",
            "worship instrumental", "praise team", "christian rock",
            "rohani kristen", "lagu pujian", "musik worship",
            "lagu gereja", "praise indonesia", "worship indonesia"
        };
    }
    
    /**
     * Enhance search query with spiritual context
     * @param originalQuery User's original search query
     * @return Enhanced query with spiritual keywords
     */
    public static String enhanceQueryForSpiritual(String originalQuery) {
        if (originalQuery == null || originalQuery.trim().isEmpty()) {
            return "worship christian gospel spiritual praise";
        }
        
        // If query already contains spiritual terms, return as is
        if (containsSpiritualKeywords(originalQuery)) {
            return originalQuery;
        }
        
        // Add spiritual context to the query
        return originalQuery + " worship christian gospel spiritual";
    }
    
    /**
     * Calculate spiritual score for a song (0-100)
     * Higher score means more likely to be spiritual
     * @param song Song to score
     * @return Spiritual score (0-100)
     */
    public static int calculateSpiritualScore(Song song) {
        if (song == null) {
            return 0;
        }
        
        int score = 0;
        
        // Check title (40 points max)
        String title = song.getTitle();
        if (title != null) {
            score += countSpiritualKeywords(title) * 10;
            if (score > 40) score = 40;
        }
        
        // Check artist (30 points max)
        String artist = song.getArtistName();
        if (artist != null) {
            if (isSpiritualArtist(artist)) {
                score += 30;
            } else {
                score += countSpiritualKeywords(artist) * 5;
                if (score > 30) score = 30;
            }
        }
        
        // Check album (30 points max)
        String album = song.getAlbumTitle();
        if (album != null) {
            score += countSpiritualKeywords(album) * 5;
            if (score > 30) score = 30;
        }
        
        return Math.min(score, 100);
    }
    
    /**
     * Count spiritual keywords in text
     * @param text Text to analyze
     * @return Number of spiritual keywords found
     */
    private static int countSpiritualKeywords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        
        String lowerText = text.toLowerCase(Locale.getDefault());
        int count = 0;
        
        for (String keyword : SPIRITUAL_KEYWORDS) {
            if (lowerText.contains(keyword.toLowerCase())) {
                count++;
            }
        }
        
        return count;
    }
}
