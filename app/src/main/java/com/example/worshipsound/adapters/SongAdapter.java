package com.example.worshipsound.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.worshipsound.R;
import com.example.worshipsound.models.Song;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying songs in a list
 */
public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    
    private List<Song> songs;
    private final Context context;
    private OnSongClickListener listener;

    /**
     * Interface for handling song item clicks
     */
    public interface OnSongClickListener {
        void onSongClick(Song song, int position);
        void onPlayClick(Song song, int position);
        void onLikeClick(Song song, int position);
        void onMenuClick(Song song, int position);
    }

    public SongAdapter(Context context) {
        this.context = context;
        this.songs = new ArrayList<>();
    }

    public SongAdapter(Context context, List<Song> songs) {
        this.context = context;
        this.songs = songs != null ? songs : new ArrayList<>();
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.bind(song, position);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    /**
     * Update the songs list
     */
    public void updateSongs(List<Song> newSongs) {
        this.songs.clear();
        if (newSongs != null) {
            this.songs.addAll(newSongs);
        }
        notifyDataSetChanged();
    }

    /**
     * Add songs to the existing list
     */
    public void addSongs(List<Song> newSongs) {
        if (newSongs != null && !newSongs.isEmpty()) {
            int startPosition = this.songs.size();
            this.songs.addAll(newSongs);
            notifyItemRangeInserted(startPosition, newSongs.size());
        }
    }

    /**
     * Add a single song
     */
    public void addSong(Song song) {
        if (song != null) {
            this.songs.add(song);
            notifyItemInserted(this.songs.size() - 1);
        }
    }

    /**
     * Remove a song at specific position
     */
    public void removeSong(int position) {
        if (position >= 0 && position < songs.size()) {
            songs.remove(position);
            notifyItemRemoved(position);
        }
    }

    /**
     * Update a specific song
     */
    public void updateSong(int position, Song song) {
        if (position >= 0 && position < songs.size() && song != null) {
            songs.set(position, song);
            notifyItemChanged(position);
        }
    }

    /**
     * Clear all songs
     */
    public void clearSongs() {
        int size = songs.size();
        songs.clear();
        notifyItemRangeRemoved(0, size);
    }

    /**
     * Get song at specific position
     */
    public Song getSong(int position) {
        if (position >= 0 && position < songs.size()) {
            return songs.get(position);
        }
        return null;
    }

    /**
     * Get all songs
     */
    public List<Song> getSongs() {
        return new ArrayList<>(songs);
    }

    /**
     * Set click listener
     */
    public void setOnSongClickListener(OnSongClickListener listener) {
        this.listener = listener;
    }

    /**
     * ViewHolder class for song items
     */
    class SongViewHolder extends RecyclerView.ViewHolder {
        private final ImageView albumCover;
        private final TextView songTitle;
        private final TextView artistName;
        private final TextView albumTitle;
        private final TextView duration;
        private final ImageButton playButton;
        private final ImageButton likeButton;
        private final ImageButton menuButton;
        private final View itemContainer;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            
            albumCover = itemView.findViewById(R.id.iv_album_cover);
            songTitle = itemView.findViewById(R.id.tv_song_title);
            artistName = itemView.findViewById(R.id.tv_artist_name);
            albumTitle = itemView.findViewById(R.id.tv_album_title);
            duration = itemView.findViewById(R.id.tv_duration);
            playButton = itemView.findViewById(R.id.btn_play);
            likeButton = itemView.findViewById(R.id.btn_like);
            menuButton = itemView.findViewById(R.id.btn_menu);
            itemContainer = itemView.findViewById(R.id.song_item_container);
        }

        public void bind(Song song, int position) {
            // Set song information
            songTitle.setText(song.getTitle());
            artistName.setText(song.getArtistName());
            albumTitle.setText(song.getAlbumTitle());
            duration.setText(song.getDurationFormatted());

            // Load album cover image
            if (song.getAlbumCover() != null && !song.getAlbumCover().isEmpty()) {
                Picasso.get()
                        .load(song.getAlbumCover())
                        .placeholder(R.drawable.ic_music_placeholder)
                        .error(R.drawable.ic_music_placeholder)
                        .fit()
                        .centerCrop()
                        .into(albumCover);
            } else {
                albumCover.setImageResource(R.drawable.ic_music_placeholder);
            }

            // Set like button state
            updateLikeButton(song.isLiked());

            // Set click listeners
            itemContainer.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSongClick(song, position);
                }
            });

            playButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPlayClick(song, position);
                }
            });

            likeButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLikeClick(song, position);
                }
            });

            menuButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMenuClick(song, position);
                }
            });
        }

        private void updateLikeButton(boolean isLiked) {
            if (isLiked) {
                likeButton.setImageResource(R.drawable.ic_favorite_filled);
                likeButton.setContentDescription("Unlike song");
            } else {
                likeButton.setImageResource(R.drawable.ic_favorite_border);
                likeButton.setContentDescription("Like song");
            }
        }
    }
}
