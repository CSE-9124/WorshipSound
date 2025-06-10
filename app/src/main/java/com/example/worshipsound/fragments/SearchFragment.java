package com.example.worshipsound.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.worshipsound.R;
import com.example.worshipsound.adapters.SongAdapter;
import com.example.worshipsound.database.SongDAO;
import com.example.worshipsound.models.DeezerResponse;
import com.example.worshipsound.models.Song;
import com.example.worshipsound.network.RetrofitClient;
import com.example.worshipsound.utils.MediaPlayerManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragment for searching spiritual/worship songs using Deezer API
 */
public class SearchFragment extends Fragment implements SongAdapter.OnSongClickListener {
    private static final String TAG = "SearchFragment";
    
    // UI Components
    private EditText etSearch;
    private Button btnSearch, btnRetry;
    private RecyclerView rvSearchResults;
    private ProgressBar progressBar;
    private TextView tvEmptyState, tvSearchHint;
    
    // Adapters and Data
    private SongAdapter searchAdapter;
    private List<Song> searchResults;
    
    // Utilities
    private MediaPlayerManager mediaPlayerManager;
    private SongDAO songDAO;
    private ExecutorService executorService;
    
    // API and Network
    private RetrofitClient retrofitClient;
    private Call<DeezerResponse> currentCall;
    
    // Search state
    private String lastSearchQuery = "";
    private boolean isSearching = false;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize utilities
        mediaPlayerManager = MediaPlayerManager.getInstance();
        songDAO = SongDAO.getInstance(requireContext());
        retrofitClient = RetrofitClient.getInstance();
        executorService = Executors.newSingleThreadExecutor();
        
        // Initialize data
        searchResults = new ArrayList<>();
        
        Log.d(TAG, "SearchFragment created");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupRecyclerView();
        setupSearchInput();
        setupClickListeners();
        showSearchHint();
        
        Log.d(TAG, "SearchFragment view created");
    }

    /**
     * Initialize all view components
     */
    private void initializeViews(View view) {
        etSearch = view.findViewById(R.id.et_search);
        btnSearch = view.findViewById(R.id.btn_search);
        btnRetry = view.findViewById(R.id.btn_retry);
        
        rvSearchResults = view.findViewById(R.id.rv_search_results);
        progressBar = view.findViewById(R.id.progress_bar);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);
        tvSearchHint = view.findViewById(R.id.tv_search_hint);
    }

    /**
     * Setup RecyclerView for search results
     */
    private void setupRecyclerView() {
        searchAdapter = new SongAdapter(requireContext(), searchResults);
        searchAdapter.setOnSongClickListener(this);
        
        rvSearchResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSearchResults.setAdapter(searchAdapter);
        rvSearchResults.setHasFixedSize(true);
    }

    /**
     * Setup search input with text watcher
     */
    private void setupSearchInput() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                btnSearch.setEnabled(!query.isEmpty() && !isSearching);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });
    }

    /**
     * Setup click listeners
     */
    private void setupClickListeners() {
        btnSearch.setOnClickListener(v -> performSearch());
        btnRetry.setOnClickListener(v -> retryLastSearch());
        
        // Allow search on enter key
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            performSearch();
            return true;
        });
    }

    /**
     * Perform search with current query
     */
    private void performSearch() {
        String query = etSearch.getText().toString().trim();
        
        if (query.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a search term", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (query.equals(lastSearchQuery) && !searchResults.isEmpty()) {
            Toast.makeText(requireContext(), "Already showing results for: " + query, Toast.LENGTH_SHORT).show();
            return;
        }
        
        searchSongs(query);
    }

    /**
     * Retry the last search
     */
    private void retryLastSearch() {
        if (!lastSearchQuery.isEmpty()) {
            searchSongs(lastSearchQuery);
        } else {
            showSearchHint();
        }
    }

    /**
     * Search for songs using Deezer API
     */
    private void searchSongs(String query) {
        // Cancel previous search if ongoing
        if (currentCall != null && !currentCall.isCanceled()) {
            currentCall.cancel();
        }
        
        lastSearchQuery = query;
        showLoading(true);
        hideEmptyState();
        hideSearchHint();
        
        // Add spiritual context to search query
        String enhancedQuery = query + " worship spiritual gospel christian";
        
        currentCall = retrofitClient.getDeezerAPI().searchTracks(enhancedQuery, 50, 0);
        
        currentCall.enqueue(new Callback<DeezerResponse>() {
            @Override
            public void onResponse(@NonNull Call<DeezerResponse> call, @NonNull Response<DeezerResponse> response) {
                if (call.isCanceled()) return;
                
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    DeezerResponse deezerResponse = response.body();
                    
                    if (deezerResponse.hasData()) {
                        List<Song> songs = deezerResponse.getSongs();
                        updateSearchResults(songs);
                        Log.d(TAG, "Found " + songs.size() + " songs for query: " + query);
                    } else {
                        showEmptyState("No songs found for \"" + query + "\"");
                        Log.w(TAG, "No songs found for query: " + query);
                    }
                } else {
                    handleSearchError("Failed to search songs");
                    Log.e(TAG, "Search API request failed: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<DeezerResponse> call, @NonNull Throwable t) {
                if (call.isCanceled()) return;
                
                showLoading(false);
                handleSearchError("Network error: " + t.getMessage());
                Log.e(TAG, "Search network request failed", t);
            }
        });
        
        Log.d(TAG, "Searching for: " + enhancedQuery);
    }

    /**
     * Update search results
     */
    private void updateSearchResults(List<Song> songs) {
        if (songs != null && !songs.isEmpty()) {
            searchResults.clear();
            searchResults.addAll(songs);
            searchAdapter.notifyDataSetChanged();
            hideEmptyState();
            hideSearchHint();
            
            // Check which songs are already liked
            executorService.execute(() -> {
                for (Song song : searchResults) {
                    boolean isLiked = songDAO.isSongLiked(song.getId());
                    song.setLiked(isLiked);
                }
                
                requireActivity().runOnUiThread(() -> searchAdapter.notifyDataSetChanged());
            });
            
            Toast.makeText(requireContext(), "Found " + songs.size() + " songs", Toast.LENGTH_SHORT).show();
        } else {
            showEmptyState("No songs found");
        }
    }

    /**
     * Show loading state
     */
    private void showLoading(boolean show) {
        isSearching = show;
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSearch.setEnabled(!show && !etSearch.getText().toString().trim().isEmpty());
        btnRetry.setVisibility(View.GONE);
        
        if (show) {
            rvSearchResults.setVisibility(View.GONE);
        } else {
            rvSearchResults.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Show empty state
     */
    private void showEmptyState(String message) {
        tvEmptyState.setText(message);
        tvEmptyState.setVisibility(View.VISIBLE);
        rvSearchResults.setVisibility(View.GONE);
        btnRetry.setVisibility(View.VISIBLE);
    }

    /**
     * Hide empty state
     */
    private void hideEmptyState() {
        tvEmptyState.setVisibility(View.GONE);
        rvSearchResults.setVisibility(View.VISIBLE);
        btnRetry.setVisibility(View.GONE);
    }

    /**
     * Show search hint
     */
    private void showSearchHint() {
        tvSearchHint.setVisibility(View.VISIBLE);
        rvSearchResults.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.GONE);
        btnRetry.setVisibility(View.GONE);
    }

    /**
     * Hide search hint
     */
    private void hideSearchHint() {
        tvSearchHint.setVisibility(View.GONE);
    }

    /**
     * Handle search errors
     */
    private void handleSearchError(String message) {
        showEmptyState(message);
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }

    /**
     * Clear search results
     */
    private void clearSearchResults() {
        searchResults.clear();
        searchAdapter.notifyDataSetChanged();
        lastSearchQuery = "";
        showSearchHint();
    }

    // SongAdapter.OnSongClickListener implementation

    @Override
    public void onSongClick(Song song, int position) {
        Toast.makeText(requireContext(), "Selected: " + song.getTitle(), Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Song clicked: " + song.getTitle());
    }

    @Override
    public void onPlayClick(Song song, int position) {
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
                    searchAdapter.notifyItemChanged(position);
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
                    searchAdapter.notifyItemChanged(position);
                });
            }
        });
    }

    @Override
    public void onMenuClick(Song song, int position) {
        Toast.makeText(requireContext(), "Menu for: " + song.getTitle(), Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Menu clicked for song: " + song.getTitle());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        
        // Cancel ongoing search
        if (currentCall != null && !currentCall.isCanceled()) {
            currentCall.cancel();
        }
        
        Log.d(TAG, "SearchFragment view destroyed");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        Log.d(TAG, "SearchFragment destroyed");
    }
}