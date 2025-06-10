package com.example.worshipsound.network;

import android.util.Log;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

/**
 * Singleton class for creating and managing Retrofit instance
 */
public class RetrofitClient {
    private static final String TAG = "RetrofitClient";
    private static final String BASE_URL = "https://api.deezer.com/";
    private static RetrofitClient instance;
    private final Retrofit retrofit;
    private final DeezerAPI deezerAPI;

    private RetrofitClient() {
        // Create HTTP logging interceptor for debugging
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(
                message -> Log.d(TAG, "HTTP: " + message)
        );
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Create OkHttp client with timeout and logging
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .build();

        // Create Retrofit instance
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Create API service
        deezerAPI = retrofit.create(DeezerAPI.class);
        
        Log.d(TAG, "RetrofitClient initialized with base URL: " + BASE_URL);
    }

    /**
     * Get singleton instance of RetrofitClient
     * @return RetrofitClient instance
     */
    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    /**
     * Get Deezer API service instance
     * @return DeezerAPI service
     */
    public DeezerAPI getDeezerAPI() {
        return deezerAPI;
    }

    /**
     * Get Retrofit instance
     * @return Retrofit instance
     */
    public Retrofit getRetrofit() {
        return retrofit;
    }

    /**
     * Get base URL
     * @return Base URL string
     */
    public String getBaseUrl() {
        return BASE_URL;
    }
}
