package com.example.worshipsound.network;

import android.util.Log;

import com.example.worshipsound.models.DeezerResponse;
import com.example.worshipsound.models.Song;
import com.example.worshipsound.utils.SpiritualSongFilter;

import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Network manager specifically for spiritual/worship music searches
 */
public class SpiritualMusicNetworkManager {
    private static final String TAG = "SpiritualNetworkManager";
    private static SpiritualMusicNetworkManager instance;
    private final RetrofitClient retrofitClient;
    
    // Interface for spiritual search callbacks
    public interface SpiritualSearchCallback {
        void onSpiritualSongsFound(List<Song> songs, int totalFound, int filtered);
        void onNoSpiritualSongsFound(String message);
        void onError(String error);
        void onLoading(boolean isLoading);
    }
    
    private SpiritualMusicNetworkManager() {
        retrofitClient = RetrofitClient.getInstance();
    }
    
    public static synchronized SpiritualMusicNetworkManager getInstance() {
        if (instance == null) {
            instance = new SpiritualMusicNetworkManager();
        }
        return instance;
    }
    
    /**
     * Search for spiritual songs with enhanced filtering
     * @param query User's search query
     * @param callback Callback for results
     */
    public void searchSpiritualSongs(String query, SpiritualSearchCallback callback) {
        searchSpiritualSongs(query, 50, 0, callback);
    }
    
    /**
     * Search for spiritual songs with pagination
     * @param query User's search query
     * @param limit Number of results to return
     * @param index Starting index for pagination
     * @param callback Callback for results
     */
    public void searchSpiritualSongs(String query, int limit, int index, SpiritualSearchCallback callback) {
        if (callback != null) {
            callback.onLoading(true);
        }
        
        // Enhance query for better spiritual results
        String enhancedQuery = SpiritualSongFilter.enhanceQueryForSpiritual(query);
        
        Call<DeezerResponse> call = retrofitClient.getDeezerAPI().searchTracks(enhancedQuery, limit, index);
        
        call.enqueue(new Callback<DeezerResponse>() {
            @Override
            public void onResponse(Call<DeezerResponse> call, Response<DeezerResponse> response) {
                if (callback != null) {
                    callback.onLoading(false);
                }
                
                if (response.isSuccessful() && response.body() != null && response.body().hasData()) {
                    List<Song> allSongs = response.body().getSongs();
                    List<Song> spiritualSongs = SpiritualSongFilter.filterSpiritualSongs(allSongs);
                    
                    if (!spiritualSongs.isEmpty()) {
                        if (callback != null) {
                            callback.onSpiritualSongsFound(spiritualSongs, allSongs.size(), spiritualSongs.size());
                        }
                        Log.d(TAG, "Found " + spiritualSongs.size() + " spiritual songs from " + allSongs.size() + " total");
                    } else {
                        // Try fallback search with different spiritual terms
                        performFallbackSearch(query, callback);
                    }
                } else {
                    if (callback != null) {
                        callback.onError("Failed to search songs: " + response.code());
                    }
                    Log.e(TAG, "Search failed: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<DeezerResponse> call, Throwable t) {
                if (callback != null) {
                    callback.onLoading(false);
                    callback.onError("Network error: " + t.getMessage());
                }
                Log.e(TAG, "Network error", t);
            }
        });
    }
    
    /**
     * Get trending spiritual songs
     * @param callback Callback for results
     */
    public void getTrendingSpiritualSongs(SpiritualSearchCallback callback) {
        if (callback != null) {
            callback.onLoading(true);
        }
        
        // Use predefined spiritual search queries
        String[] spiritualQueries = SpiritualSongFilter.getSpiritualSearchQueries();
        Random random = new Random();
        String selectedQuery = spiritualQueries[random.nextInt(spiritualQueries.length)];
        
        Call<DeezerResponse> call = retrofitClient.getDeezerAPI().searchTracks(selectedQuery, 50, 0);
        
        call.enqueue(new Callback<DeezerResponse>() {
            @Override
            public void onResponse(Call<DeezerResponse> call, Response<DeezerResponse> response) {
                if (callback != null) {
                    callback.onLoading(false);
                }
                
                if (response.isSuccessful() && response.body() != null && response.body().hasData()) {
                    List<Song> allSongs = response.body().getSongs();
                    List<Song> spiritualSongs = SpiritualSongFilter.filterSpiritualSongs(allSongs);
                    
                    if (!spiritualSongs.isEmpty()) {
                        if (callback != null) {
                            callback.onSpiritualSongsFound(spiritualSongs, allSongs.size(), spiritualSongs.size());
                        }
                        Log.d(TAG, "Found " + spiritualSongs.size() + " trending spiritual songs");
                    } else {
                        // Try another spiritual query
                        retryWithDifferentSpiritualQuery(callback);
                    }
                } else {
                    if (callback != null) {
                        callback.onError("Failed to load trending songs");
                    }
                }
            }
            
            @Override
            public void onFailure(Call<DeezerResponse> call, Throwable t) {
                if (callback != null) {
                    callback.onLoading(false);
                    callback.onError("Network error: " + t.getMessage());
                }
                Log.e(TAG, "Network error loading trending", t);
            }
        });
    }
    
    /**
     * Search for high-quality spiritual songs (with minimum spiritual score)
     * @param query Search query
     * @param minimumScore Minimum spiritual score (0-100)
     * @param callback Callback for results
     */
    public void searchHighQualitySpiritualSongs(String query, int minimumScore, SpiritualSearchCallback callback) {
        searchSpiritualSongs(query, 100, 0, new SpiritualSearchCallback() {
            @Override
            public void onSpiritualSongsFound(List<Song> songs, int totalFound, int filtered) {
                // Further filter by spiritual score
                List<Song> highQualitySongs = songs.stream()
                    .filter(song -> SpiritualSongFilter.calculateSpiritualScore(song) >= minimumScore)
                    .collect(java.util.stream.Collectors.toList());
                
                if (!highQualitySongs.isEmpty()) {
                    if (callback != null) {
                        callback.onSpiritualSongsFound(highQualitySongs, totalFound, highQualitySongs.size());
                    }
                } else {
                    if (callback != null) {
                        callback.onNoSpiritualSongsFound("No high-quality spiritual songs found. Try a different search term.");
                    }
                }
            }
            
            @Override
            public void onNoSpiritualSongsFound(String message) {
                if (callback != null) {
                    callback.onNoSpiritualSongsFound(message);
                }
            }
            
            @Override
            public void onError(String error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }
            
            @Override
            public void onLoading(boolean isLoading) {
                if (callback != null) {
                    callback.onLoading(isLoading);
                }
            }
        });
    }
    
    /**
     * Perform fallback search if no spiritual songs found
     */
    private void performFallbackSearch(String originalQuery, SpiritualSearchCallback callback) {
        String[] fallbackQueries = {
            "gospel " + originalQuery,
            "worship " + originalQuery,
            "christian " + originalQuery,
            originalQuery + " praise",
            originalQuery + " hymn"
        };
        
        Random random = new Random();
        String fallbackQuery = fallbackQueries[random.nextInt(fallbackQueries.length)];
        
        Call<DeezerResponse> call = retrofitClient.getDeezerAPI().searchTracks(fallbackQuery, 30, 0);
        
        call.enqueue(new Callback<DeezerResponse>() {
            @Override
            public void onResponse(Call<DeezerResponse> call, Response<DeezerResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().hasData()) {
                    List<Song> allSongs = response.body().getSongs();
                    List<Song> spiritualSongs = SpiritualSongFilter.filterSpiritualSongs(allSongs);
                    
                    if (!spiritualSongs.isEmpty()) {
                        if (callback != null) {
                            callback.onSpiritualSongsFound(spiritualSongs, allSongs.size(), spiritualSongs.size());
                        }
                        Log.d(TAG, "Fallback search successful: " + spiritualSongs.size() + " spiritual songs");
                    } else {
                        if (callback != null) {
                            callback.onNoSpiritualSongsFound("No spiritual songs found for \"" + originalQuery + "\". Try searching for gospel, worship, or christian music.");
                        }
                    }
                } else {
                    if (callback != null) {
                        callback.onNoSpiritualSongsFound("No spiritual songs found for \"" + originalQuery + "\"");
                    }
                }
            }
            
            @Override
            public void onFailure(Call<DeezerResponse> call, Throwable t) {
                if (callback != null) {
                    callback.onNoSpiritualSongsFound("No spiritual songs found for \"" + originalQuery + "\"");
                }
                Log.e(TAG, "Fallback search failed", t);
            }
        });
    }
    
    /**
     * Retry with different spiritual query for trending songs
     */
    private void retryWithDifferentSpiritualQuery(SpiritualSearchCallback callback) {
        String[] spiritualQueries = SpiritualSongFilter.getSpiritualSearchQueries();
        Random random = new Random();
        String retryQuery = spiritualQueries[random.nextInt(spiritualQueries.length)];
        
        Call<DeezerResponse> call = retrofitClient.getDeezerAPI().searchTracks(retryQuery, 30, 0);
        
        call.enqueue(new Callback<DeezerResponse>() {
            @Override
            public void onResponse(Call<DeezerResponse> call, Response<DeezerResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().hasData()) {
                    List<Song> allSongs = response.body().getSongs();
                    List<Song> spiritualSongs = SpiritualSongFilter.filterSpiritualSongs(allSongs);
                    
                    if (!spiritualSongs.isEmpty()) {
                        if (callback != null) {
                            callback.onSpiritualSongsFound(spiritualSongs, allSongs.size(), spiritualSongs.size());
                        }
                        Log.d(TAG, "Retry successful: " + spiritualSongs.size() + " spiritual songs");
                    } else {
                        if (callback != null) {
                            callback.onNoSpiritualSongsFound("No spiritual songs available at the moment");
                        }
                    }
                } else {
                    if (callback != null) {
                        callback.onNoSpiritualSongsFound("No spiritual songs available at the moment");
                    }
                }
            }
            
            @Override
            public void onFailure(Call<DeezerResponse> call, Throwable t) {
                if (callback != null) {
                    callback.onNoSpiritualSongsFound("No spiritual songs available at the moment");
                }
                Log.e(TAG, "Retry failed", t);
            }
        });
    }
}
