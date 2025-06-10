package com.example.worshipsound.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.worshipsound.models.Song;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Utility class for managing media playback using MediaPlayer
 */
public class MediaPlayerManager {
    private static final String TAG = "MediaPlayerManager";
    private static MediaPlayerManager instance;
    
    private MediaPlayer mediaPlayer;
    private final ExecutorService executorService;
    private final Handler mainHandler;
    private PlaybackListener playbackListener;
    
    private Song currentSong;
    private boolean isPlaying = false;
    private boolean isPrepared = false;
    private int currentPosition = 0;

    /**
     * Interface for playback events
     */
    public interface PlaybackListener {
        void onPlaybackStarted(Song song);
        void onPlaybackPaused(Song song);
        void onPlaybackStopped();
        void onPlaybackCompleted(Song song);
        void onPlaybackError(String error);
        void onPlaybackProgress(int currentPosition, int duration);
        void onBuffering(boolean isBuffering);
    }

    private MediaPlayerManager() {
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        initializeMediaPlayer();
    }

    /**
     * Get singleton instance of MediaPlayerManager
     */
    public static synchronized MediaPlayerManager getInstance() {
        if (instance == null) {
            instance = new MediaPlayerManager();
        }
        return instance;
    }

    /**
     * Initialize MediaPlayer with default settings
     */
    private void initializeMediaPlayer() {
        try {
            mediaPlayer = new MediaPlayer();
            
            // Set audio attributes for music playback
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build();
            mediaPlayer.setAudioAttributes(audioAttributes);
            
            // Set up listeners
            mediaPlayer.setOnPreparedListener(mp -> {
                isPrepared = true;
                Log.d(TAG, "MediaPlayer prepared for: " + (currentSong != null ? currentSong.getTitle() : "Unknown"));
                if (playbackListener != null) {
                    mainHandler.post(() -> playbackListener.onBuffering(false));
                }
            });
            
            mediaPlayer.setOnCompletionListener(mp -> {
                Log.d(TAG, "Playback completed for: " + (currentSong != null ? currentSong.getTitle() : "Unknown"));
                isPlaying = false;
                currentPosition = 0;
                if (playbackListener != null) {
                    mainHandler.post(() -> playbackListener.onPlaybackCompleted(currentSong));
                }
            });
            
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                String error = "MediaPlayer error: what=" + what + ", extra=" + extra;
                Log.e(TAG, error);
                isPlaying = false;
                isPrepared = false;
                if (playbackListener != null) {
                    mainHandler.post(() -> playbackListener.onPlaybackError(error));
                }
                return true;
            });
            
            mediaPlayer.setOnBufferingUpdateListener((mp, percent) -> {
                Log.d(TAG, "Buffering: " + percent + "%");
            });
            
            Log.d(TAG, "MediaPlayer initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing MediaPlayer", e);
        }
    }

    /**
     * Play a song from preview URL
     */
    public void playSong(Song song) {
        if (song == null || song.getPreviewUrl() == null || song.getPreviewUrl().isEmpty()) {
            Log.e(TAG, "Cannot play song: invalid song or preview URL");
            if (playbackListener != null) {
                mainHandler.post(() -> playbackListener.onPlaybackError("Invalid song or preview URL"));
            }
            return;
        }

        executorService.execute(() -> {
            try {
                // Stop current playback if any
                stopPlayback();
                
                currentSong = song;
                
                // Notify buffering started
                if (playbackListener != null) {
                    mainHandler.post(() -> playbackListener.onBuffering(true));
                }
                
                // Reset and configure MediaPlayer
                mediaPlayer.reset();
                mediaPlayer.setDataSource(song.getPreviewUrl());
                
                // Prepare asynchronously
                mediaPlayer.prepareAsync();
                
                // Wait for preparation and start playback
                mediaPlayer.setOnPreparedListener(mp -> {
                    isPrepared = true;
                    mp.start();
                    isPlaying = true;
                    
                    Log.d(TAG, "Started playing: " + song.getTitle());
                    
                    if (playbackListener != null) {
                        mainHandler.post(() -> {
                            playbackListener.onBuffering(false);
                            playbackListener.onPlaybackStarted(song);
                        });
                    }
                    
                    // Start progress tracking
                    startProgressTracking();
                });
                
            } catch (IOException e) {
                Log.e(TAG, "Error playing song: " + song.getTitle(), e);
                if (playbackListener != null) {
                    mainHandler.post(() -> playbackListener.onPlaybackError("Failed to load audio: " + e.getMessage()));
                }
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error playing song", e);
                if (playbackListener != null) {
                    mainHandler.post(() -> playbackListener.onPlaybackError("Playback error: " + e.getMessage()));
                }
            }
        });
    }

    /**
     * Pause current playback
     */
    public void pausePlayback() {
        if (mediaPlayer != null && isPlaying) {
            try {
                mediaPlayer.pause();
                isPlaying = false;
                currentPosition = mediaPlayer.getCurrentPosition();
                
                Log.d(TAG, "Playback paused");
                
                if (playbackListener != null) {
                    mainHandler.post(() -> playbackListener.onPlaybackPaused(currentSong));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error pausing playback", e);
            }
        }
    }

    /**
     * Resume paused playback
     */
    public void resumePlayback() {
        if (mediaPlayer != null && isPrepared && !isPlaying) {
            try {
                mediaPlayer.start();
                isPlaying = true;
                
                Log.d(TAG, "Playback resumed");
                
                if (playbackListener != null) {
                    mainHandler.post(() -> playbackListener.onPlaybackStarted(currentSong));
                }
                
                startProgressTracking();
            } catch (Exception e) {
                Log.e(TAG, "Error resuming playback", e);
            }
        }
    }

    /**
     * Stop current playback
     */
    public void stopPlayback() {
        if (mediaPlayer != null) {
            try {
                if (isPlaying) {
                    mediaPlayer.stop();
                }
                isPlaying = false;
                isPrepared = false;
                currentPosition = 0;
                currentSong = null;
                
                Log.d(TAG, "Playback stopped");
                
                if (playbackListener != null) {
                    mainHandler.post(() -> playbackListener.onPlaybackStopped());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error stopping playback", e);
            }
        }
    }

    /**
     * Toggle play/pause
     */
    public void togglePlayback() {
        if (isPlaying) {
            pausePlayback();
        } else if (isPrepared) {
            resumePlayback();
        }
    }

    /**
     * Seek to specific position in milliseconds
     */
    public void seekTo(int position) {
        if (mediaPlayer != null && isPrepared) {
            try {
                mediaPlayer.seekTo(position);
                currentPosition = position;
                Log.d(TAG, "Seeked to position: " + position);
            } catch (Exception e) {
                Log.e(TAG, "Error seeking to position", e);
            }
        }
    }

    /**
     * Start tracking playback progress
     */
    private void startProgressTracking() {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && isPlaying && isPrepared) {
                    try {
                        int currentPos = mediaPlayer.getCurrentPosition();
                        int duration = mediaPlayer.getDuration();
                        
                        if (playbackListener != null) {
                            playbackListener.onPlaybackProgress(currentPos, duration);
                        }
                        
                        // Update every 500ms
                        mainHandler.postDelayed(this, 500);
                    } catch (Exception e) {
                        Log.e(TAG, "Error tracking progress", e);
                    }
                }
            }
        });
    }

    /**
     * Set playback listener
     */
    public void setPlaybackListener(PlaybackListener listener) {
        this.playbackListener = listener;
    }

    /**
     * Remove playback listener
     */
    public void removePlaybackListener() {
        this.playbackListener = null;
    }

    // Getters for current state
    
    public boolean isPlaying() { return isPlaying; }
    public boolean isPrepared() { return isPrepared; }
    public Song getCurrentSong() { return currentSong; }
    public int getCurrentPosition() { 
        if (mediaPlayer != null && isPrepared) {
            try {
                return mediaPlayer.getCurrentPosition();
            } catch (Exception e) {
                return currentPosition;
            }
        }
        return currentPosition; 
    }
    
    public int getDuration() {
        if (mediaPlayer != null && isPrepared) {
            try {
                return mediaPlayer.getDuration();
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * Release resources
     */
    public void release() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
                mediaPlayer = null;
                isPlaying = false;
                isPrepared = false;
                currentSong = null;
                
                Log.d(TAG, "MediaPlayer resources released");
            } catch (Exception e) {
                Log.e(TAG, "Error releasing MediaPlayer", e);
            }
        }
        
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
