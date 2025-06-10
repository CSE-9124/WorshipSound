package com.example.worshipsound.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.worshipsound.R;
import com.example.worshipsound.adapters.SongAdapter;
import com.example.worshipsound.database.SongDAO;
import com.example.worshipsound.models.DeezerResponse;
import com.example.worshipsound.models.Song;
import com.example.worshipsound.network.RetrofitClient;
import com.example.worshipsound.network.SpiritualMusicNetworkManager;
import com.example.worshipsound.utils.MediaPlayerManager;
import com.example.worshipsound.utils.SpiritualSongFilter;
import com.example.worshipsound.utils.ThemeManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Home fragment displaying trending spiritual songs and user recommendations
 */
public class HomeFragment extends Fragment implements SongAdapter.OnSongClickListener {
    private static final String TAG = "HomeFragment";
    
    // UI Components
    private TextView tvWelcome, tvTrendingTitle, tvEmptyState;
    private RecyclerView rvTrendingSongs;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private Button btnRetry;
    
    // Adapters and Data
    private SongAdapter trendingAdapter;
    private List<Song> trendingSongs;
    
    // Utilities
    private ThemeManager themeManager;
    private MediaPlayerManager mediaPlayerManager;
    private SongDAO songDAO;
    private ExecutorService executorService;
    
    // API and Network
    private RetrofitClient retrofitClient;
    private SpiritualMusicNetworkManager spiritualNetworkManager;
    private final String[] spiritualKeywords = {
        "gospel", "worship", "christian", "spiritual", "praise", 
        "hymn", "jesus", "god", "church", "prayer"
    };

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize utilities
        themeManager = ThemeManager.getInstance(requireContext());
        mediaPlayerManager = MediaPlayerManager.getInstance();
        songDAO = SongDAO.getInstance(requireContext());
        retrofitClient = RetrofitClient.getInstance();
        spiritualNetworkManager = SpiritualMusicNetworkManager.getInstance();
        executorService = Executors.newSingleThreadExecutor();
        
        // Initialize data
        trendingSongs = new ArrayList<>();
        
        Log.d(TAG, "HomeFragment created");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        setupClickListeners();
        
        // Load initial data
        loadTrendingSongs();
        
        Log.d(TAG, "HomeFragment view created");
    }

    /**
     * Initialize all view components
     */
    private void initializeViews(View view) {
        tvWelcome = view.findViewById(R.id.tv_welcome);
        tvTrendingTitle = view.findViewById(R.id.tv_trending_title);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);
        
        rvTrendingSongs = view.findViewById(R.id.rv_trending_songs);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        progressBar = view.findViewById(R.id.progress_bar);
        btnRetry = view.findViewById(R.id.btn_retry);
        
        // Set welcome message
        String username = themeManager.getUserDisplayName();
        tvWelcome.setText("Welcome back, " + username + "!");
    }

    /**
     * Setup RecyclerView for trending songs
     */
    private void setupRecyclerView() {
        trendingAdapter = new SongAdapter(requireContext(), trendingSongs);
        trendingAdapter.setOnSongClickListener(this);
        
        rvTrendingSongs.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTrendingSongs.setAdapter(trendingAdapter);
        rvTrendingSongs.setHasFixedSize(true);
    }

    /**
     * Setup swipe to refresh functionality
     */
    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::refreshTrendingSongs);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.primary,
                R.color.primary_variant
        );
    }

    /**
     * Setup click listeners
     */
    private void setupClickListeners() {
        btnRetry.setOnClickListener(v -> loadTrendingSongs());
    }

    /**
     * Load trending spiritual songs from Deezer API
     */
    private void loadTrendingSongs() {
        showLoading(true);
        hideEmptyState();
        
        spiritualNetworkManager.getTrendingSpiritualSongs(new SpiritualMusicNetworkManager.SpiritualSearchCallback() {
            @Override
            public void onSpiritualSongsFound(List<Song> songs, int totalFound, int filtered) {
                showLoading(false);
                updateTrendingSongs(songs);
                Log.d(TAG, "Loaded " + filtered + " spiritual songs from " + totalFound + " total");
            }
            
            @Override
            public void onNoSpiritualSongsFound(String message) {
                showLoading(false);
                showEmptyState(message);
                Log.w(TAG, "No spiritual songs found: " + message);
            }
            
            @Override
            public void onError(String error) {
                showLoading(false);
                handleNetworkError(error);
                Log.e(TAG, "Error loading spiritual songs: " + error);
            }
            
            @Override
            public void onLoading(boolean isLoading) {
                // Loading state is handled by the outer showLoading calls
            }
        });
    }

    /**
     * Refresh trending songs
     */
    private void refreshTrendingSongs() {
        spiritualNetworkManager.getTrendingSpiritualSongs(new SpiritualMusicNetworkManager.SpiritualSearchCallback() {
            @Override
            public void onSpiritualSongsFound(List<Song> songs, int totalFound, int filtered) {
                swipeRefreshLayout.setRefreshing(false);
                updateTrendingSongs(songs);
                Toast.makeText(requireContext(), "Songs refreshed! Found " + filtered + " spiritual songs", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Refreshed with " + filtered + " spiritual songs from " + totalFound + " total");
            }
            
            @Override
            public void onNoSpiritualSongsFound(String message) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(requireContext(), "No spiritual songs found, try again", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Refresh failed: " + message);
            }
            
            @Override
            public void onError(String error) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(requireContext(), "Failed to refresh songs", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Refresh error: " + error);
            }
            
            @Override
            public void onLoading(boolean isLoading) {
                // Refresh loading is handled by SwipeRefreshLayout
            }
        });
    }

    /**
     * Update trending songs list
     */
    private void updateTrendingSongs(List<Song> songs) {
        if (songs != null && !songs.isEmpty()) {
            trendingSongs.clear();
            trendingSongs.addAll(songs);
            trendingAdapter.notifyDataSetChanged();
            hideEmptyState();
            
            // Check which songs are already liked
            executorService.execute(() -> {
                for (Song song : trendingSongs) {
                    boolean isLiked = songDAO.isSongLiked(song.getId());
                    song.setLiked(isLiked);
                }
                
                requireActivity().runOnUiThread(() -> trendingAdapter.notifyDataSetChanged());
            });
        } else {
            showEmptyState("No songs available");
        }
    }

    /**
     * Get random spiritual keyword for API search
     */
    private String getRandomSpiritualKeyword() {
        Random random = new Random();
        return spiritualKeywords[random.nextInt(spiritualKeywords.length)];
    }

    /**
     * Show loading state
     */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvTrendingSongs.setVisibility(show ? View.GONE : View.VISIBLE);
        btnRetry.setVisibility(View.GONE);
    }

    /**
     * Show empty state
     */
    private void showEmptyState(String message) {
        tvEmptyState.setText(message);
        tvEmptyState.setVisibility(View.VISIBLE);
        rvTrendingSongs.setVisibility(View.GONE);
        btnRetry.setVisibility(View.VISIBLE);
    }

    /**
     * Hide empty state
     */
    private void hideEmptyState() {
        tvEmptyState.setVisibility(View.GONE);
        rvTrendingSongs.setVisibility(View.VISIBLE);
        btnRetry.setVisibility(View.GONE);
    }

    /**
     * Handle network errors
     */
    private void handleNetworkError(String message) {
        showEmptyState(message);
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }

    // SongAdapter.OnSongClickListener implementation

    @Override
    public void onSongClick(Song song, int position) {
        // Handle song item click (show details, etc.)
        Toast.makeText(requireContext(), "Selected: " + song.getTitle(), Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Song clicked: " + song.getTitle());
    }

    @Override
    public void onPlayClick(Song song, int position) {
        // Handle play button click
        if (song.getPreviewUrl() != null && !song.getPreviewUrl().isEmpty()) {
            mediaPlayerManager.playSong(song);
            Toast.makeText(requireContext(), "Playing: " + song.getTitle(), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Playing song: " + song.getTitle());
        } else {
            Toast.makeText(requireContext(), "No preview available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLikeClick(Song song, int position) {
        // Handle like button click
        executorService.execute(() -> {
            boolean wasLiked = song.isLiked();
            
            if (wasLiked) {
                // Remove from liked songs
                boolean removed = songDAO.removeSong(song.getId(), "Liked Songs");
                song.setLiked(!removed);
                
                requireActivity().runOnUiThread(() -> {
                    if (removed) {
                        Toast.makeText(requireContext(), "Removed from liked songs", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Song removed from liked: " + song.getTitle());
                    }
                    trendingAdapter.notifyItemChanged(position);
                });
            } else {
                // Add to liked songs
                song.setLiked(true);
                song.setPlaylistName("Liked Songs");
                long result = songDAO.insertSong(song);
                
                requireActivity().runOnUiThread(() -> {
                    if (result > 0) {
                        Toast.makeText(requireContext(), "Added to liked songs", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Song added to liked: " + song.getTitle());
                    } else {
                        song.setLiked(false); // Revert on failure
                        Toast.makeText(requireContext(), "Failed to like song", Toast.LENGTH_SHORT).show();
                    }
                    trendingAdapter.notifyItemChanged(position);
                });
            }
        });
    }

    @Override
    public void onMenuClick(Song song, int position) {
        // Handle menu button click (show popup menu for more options)
        Toast.makeText(requireContext(), "Menu for: " + song.getTitle(), Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Menu clicked for song: " + song.getTitle());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "HomeFragment view destroyed");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        Log.d(TAG, "HomeFragment destroyed");
    }
}