package com.example.worshipsound.network;

import com.example.worshipsound.models.DeezerResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Retrofit interface for Deezer API endpoints
 */
public interface DeezerAPI {
    
    /**
     * Search for tracks using Deezer API
     * @param query Search query (e.g., "gospel", "worship", "christian", "spiritual")
     * @param limit Number of results to return (default: 25, max: 100)
     * @param index Starting index for pagination (default: 0)
     * @return Call object containing DeezerResponse
     */
    @GET("search")
    Call<DeezerResponse> searchTracks(
            @Query("q") String query,
            @Query("limit") int limit,
            @Query("index") int index
    );

    /**
     * Search for tracks with default pagination
     * @param query Search query
     * @return Call object containing DeezerResponse
     */
    @GET("search")
    Call<DeezerResponse> searchTracks(@Query("q") String query);

    /**
     * Search for specific spiritual/worship music categories
     * @param query Base search query
     * @param genre Additional genre filter
     * @param limit Number of results
     * @return Call object containing DeezerResponse
     */
    @GET("search")
    Call<DeezerResponse> searchByGenre(
            @Query("q") String query,
            @Query("genre") String genre,
            @Query("limit") int limit
    );

    /**
     * Search specifically for worship/spiritual music with enhanced filtering
     * @param limit Number of results to return
     * @param index Starting index for pagination
     * @return Call object containing DeezerResponse
     */
    @GET("search")
    Call<DeezerResponse> searchWorshipMusic(
            @Query("q") String query,
            @Query("limit") int limit,
            @Query("index") int index
    );

    /**
     * Search for Christian/Gospel music
     * @param limit Number of results
     * @return Call object containing DeezerResponse
     */
    @GET("search")
    Call<DeezerResponse> searchChristianMusic(
            @Query("q") String query,
            @Query("limit") int limit
    );

    /**
     * Search for contemporary worship songs
     * @param artistType Type of artist (e.g., "worship", "gospel", "christian")
     * @param limit Number of results
     * @return Call object containing DeezerResponse
     */
    @GET("search")
    Call<DeezerResponse> searchByWorshipArtist(
            @Query("q") String artistType,
            @Query("limit") int limit
    );
}
