package com.example.worshipsound.fragments;

import android.app.AlertDialog;
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
import com.example.worshipsound.models.Song;
import com.example.worshipsound.utils.MediaPlayerManager;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Fragment for displaying user's playlists and liked songs
 */
public class PlaylistFragment extends Fragment implements SongAdapter.OnSongClickListener {
    private static final String TAG = "PlaylistFragment";
    
    // UI Components
    private TabLayout tabLayout;
    private RecyclerView rvPlaylistSongs;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView tvEmptyState, tvPlaylistTitle;
    private Button btnRetry;
    
    // Adapters and Data
    private SongAdapter playlistAdapter;
    private List<Song> playlistSongs;
    
    // Utilities
    private MediaPlayerManager mediaPlayerManager;
    private SongDAO songDAO;
    private ExecutorService executorService;
    
    // Playlist management
    private List<String> availablePlaylists;
    private String currentPlaylist = "Liked Songs";
    private int currentTabPosition = 0;

    public PlaylistFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize utilities
        mediaPlayerManager = MediaPlayerManager.getInstance();
        songDAO = SongDAO.getInstance(requireContext());
        executorService = Executors.newSingleThreadExecutor();
        
        // Initialize data
        playlistSongs = new ArrayList<>();
        availablePlaylists = new ArrayList<>();
        
        Log.d(TAG, "PlaylistFragment created");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playlist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        setupTabLayout();
        setupClickListeners();
        
        // Load initial data
        loadAvailablePlaylists();
        
        Log.d(TAG, "PlaylistFragment view created");
    }

    /**
     * Initialize all view components
     */
    private void initializeViews(View view) {
        tabLayout = view.findViewById(R.id.tab_layout);
        rvPlaylistSongs = view.findViewById(R.id.rv_playlist_songs);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        progressBar = view.findViewById(R.id.progress_bar);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);
        tvPlaylistTitle = view.findViewById(R.id.tv_playlist_title);
        btnRetry = view.findViewById(R.id.btn_retry);
        
        // Set initial title
        tvPlaylistTitle.setText(currentPlaylist);
    }

    /**
     * Setup RecyclerView for playlist songs
     */
    private void setupRecyclerView() {
        playlistAdapter = new SongAdapter(requireContext(), playlistSongs);
        playlistAdapter.setOnSongClickListener(this);
        
        rvPlaylistSongs.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPlaylistSongs.setAdapter(playlistAdapter);
        rvPlaylistSongs.setHasFixedSize(true);
    }

    /**
     * Setup swipe to refresh functionality
     */
    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::refreshCurrentPlaylist);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.primary,
                R.color.primary_variant
        );
    }

    /**
     * Setup tab layout for different playlists
     */
    private void setupTabLayout() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTabPosition = tab.getPosition();
                if (currentTabPosition < availablePlaylists.size()) {
                    currentPlaylist = availablePlaylists.get(currentTabPosition);
                    tvPlaylistTitle.setText(currentPlaylist);
                    loadPlaylistSongs(currentPlaylist);
                    Log.d(TAG, "Selected playlist: " + currentPlaylist);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Not needed
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Refresh current playlist
                refreshCurrentPlaylist();
            }
        });
    }

    /**
     * Setup click listeners
     */
    private void setupClickListeners() {
        btnRetry.setOnClickListener(v -> loadPlaylistSongs(currentPlaylist));
    }

    /**
     * Load available playlists from database
     */
    private void loadAvailablePlaylists() {
        executorService.execute(() -> {
            try {
                List<String> playlists = songDAO.getPlaylistNames();
                
                requireActivity().runOnUiThread(() -> {
                    availablePlaylists.clear();
                    availablePlaylists.addAll(playlists);
                    updateTabLayout();
                    
                    // Load first playlist
                    if (!availablePlaylists.isEmpty()) {
                        currentPlaylist = availablePlaylists.get(0);
                        tvPlaylistTitle.setText(currentPlaylist);
                        loadPlaylistSongs(currentPlaylist);
                    } else {
                        showEmptyState("No playlists found");
                    }
                    
                    Log.d(TAG, "Loaded " + playlists.size() + " playlists");
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading playlists", e);
                requireActivity().runOnUiThread(() -> 
                    showEmptyState("Error loading playlists: " + e.getMessage())
                );
            }
        });
    }

    /**
     * Update tab layout with available playlists
     */
    private void updateTabLayout() {
        tabLayout.removeAllTabs();
        
        for (String playlist : availablePlaylists) {
            TabLayout.Tab tab = tabLayout.newTab();
            tab.setText(playlist);
            tabLayout.addTab(tab);
        }
        
        // Select first tab
        if (tabLayout.getTabCount() > 0) {
            TabLayout.Tab firstTab = tabLayout.getTabAt(0);
            if (firstTab != null) {
                firstTab.select();
            }
        }
    }

    /**
     * Load songs from specific playlist
     */
    private void loadPlaylistSongs(String playlistName) {
        showLoading(true);
        hideEmptyState();
        
        executorService.execute(() -> {
            try {
                List<Song> songs = songDAO.getSongsByPlaylist(playlistName);
                
                requireActivity().runOnUiThread(() -> {
                    showLoading(false);
                    updatePlaylistSongs(songs);
                    Log.d(TAG, "Loaded " + songs.size() + " songs from " + playlistName);
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading playlist songs", e);
                requireActivity().runOnUiThread(() -> {
                    showLoading(false);
                    showEmptyState("Error loading songs: " + e.getMessage());
                });
            }
        });
    }

    /**
     * Refresh current playlist
     */
    private void refreshCurrentPlaylist() {
        loadPlaylistSongs(currentPlaylist);
        
        // Stop refresh animation after a delay
        swipeRefreshLayout.postDelayed(() -> {
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 1000);
    }

    /**
     * Update playlist songs list
     */
    private void updatePlaylistSongs(List<Song> songs) {
        if (songs != null && !songs.isEmpty()) {
            playlistSongs.clear();
            playlistSongs.addAll(songs);
            playlistAdapter.notifyDataSetChanged();
            hideEmptyState();
            
            Toast.makeText(requireContext(), "Loaded " + songs.size() + " songs", Toast.LENGTH_SHORT).show();
        } else {
            showEmptyState("No songs in " + currentPlaylist);
        }
    }

    /**
     * Show loading state
     */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvPlaylistSongs.setVisibility(show ? View.GONE : View.VISIBLE);
        btnRetry.setVisibility(View.GONE);
    }

    /**
     * Show empty state
     */
    private void showEmptyState(String message) {
        tvEmptyState.setText(message);
        tvEmptyState.setVisibility(View.VISIBLE);
        rvPlaylistSongs.setVisibility(View.GONE);
        btnRetry.setVisibility(View.VISIBLE);
    }

    /**
     * Hide empty state
     */
    private void hideEmptyState() {
        tvEmptyState.setVisibility(View.GONE);
        rvPlaylistSongs.setVisibility(View.VISIBLE);
        btnRetry.setVisibility(View.GONE);
    }

    /**
     * Show confirmation dialog for song removal
     */
    private void showRemoveConfirmation(Song song, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Remove Song")
                .setMessage("Remove \"" + song.getTitle() + "\" from " + currentPlaylist + "?")
                .setPositiveButton("Remove", (dialog, which) -> removeSongFromPlaylist(song, position))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Remove song from current playlist
     */
    private void removeSongFromPlaylist(Song song, int position) {
        executorService.execute(() -> {
            boolean removed = songDAO.removeSong(song.getId(), currentPlaylist);
            
            requireActivity().runOnUiThread(() -> {
                if (removed) {
                    playlistSongs.remove(position);
                    playlistAdapter.notifyItemRemoved(position);
                    playlistAdapter.notifyItemRangeChanged(position, playlistSongs.size());
                    
                    Toast.makeText(requireContext(), "Song removed from " + currentPlaylist, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Removed song: " + song.getTitle());
                    
                    // Show empty state if no songs left
                    if (playlistSongs.isEmpty()) {
                        showEmptyState("No songs in " + currentPlaylist);
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to remove song", Toast.LENGTH_SHORT).show();
                }
            });
        });
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
        // For playlist fragment, like click removes the song from playlist
        showRemoveConfirmation(song, position);
    }

    @Override
    public void onMenuClick(Song song, int position) {
        // Show options menu for the song
        new AlertDialog.Builder(requireContext())
                .setTitle(song.getTitle())
                .setItems(new String[]{"Remove from " + currentPlaylist, "Play", "Song Details"}, 
                    (dialog, which) -> {
                        switch (which) {
                            case 0: // Remove
                                showRemoveConfirmation(song, position);
                                break;
                            case 1: // Play
                                onPlayClick(song, position);
                                break;
                            case 2: // Details
                                showSongDetails(song);
                                break;
                        }
                    })
                .show();
        
        Log.d(TAG, "Menu clicked for song: " + song.getTitle());
    }

    /**
     * Show song details dialog
     */
    private void showSongDetails(Song song) {
        String details = "Title: " + song.getTitle() + "\n" +
                        "Artist: " + song.getArtistName() + "\n" +
                        "Album: " + song.getAlbumTitle() + "\n" +
                        "Duration: " + song.getDurationFormatted() + "\n" +
                        "Playlist: " + currentPlaylist;
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Song Details")
                .setMessage(details)
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh current playlist when fragment becomes visible
        if (!currentPlaylist.isEmpty()) {
            loadPlaylistSongs(currentPlaylist);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "PlaylistFragment view destroyed");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        Log.d(TAG, "PlaylistFragment destroyed");
    }
}