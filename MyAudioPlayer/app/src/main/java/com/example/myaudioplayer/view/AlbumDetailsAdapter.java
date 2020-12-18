package com.example.myaudioplayer.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myaudioplayer.R;
import com.example.myaudioplayer.audiointerface.OnFavoriteChangeListener;
import com.example.myaudioplayer.audiomodel.Album;
import com.example.myaudioplayer.audiomodel.Playlist;
import com.example.myaudioplayer.audiomodel.Song;
import com.example.myaudioplayer.helper.SongDiffCallBack;

import java.util.ArrayList;

import static com.example.myaudioplayer.helper.Helper.getEmbeddedArt;

public class AlbumDetailsAdapter extends RecyclerView.Adapter<MusicAdapter.SongViewHolder> {
    private Context mContext;
    private Album album;
    private View view;
    private ArrayList<Song> songs;
    private ArrayList<OnFavoriteChangeListener> listeners = new ArrayList<>();
    public AlbumDetailsAdapter(Context mContext, Album album) {
        this.mContext = mContext;
        this.album = album;
        songs = new ArrayList<>();
        songs.addAll(album.getSongs());
    }

    @NonNull
    @Override
    public MusicAdapter.SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(mContext).inflate(R.layout.music_items, parent, false);
        return new MusicAdapter.SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MusicAdapter.SongViewHolder holder, final int position) {
        final Song song = songs.get(position);
        holder.file_name.setText(song.getTitle());
        holder.changeFavoriteColor(mContext, song.isFavorite());
        byte[] image = getEmbeddedArt(song.getPath());
        if (image != null) {
            Glide.with(mContext).asBitmap().load(image).into(holder.album_art);
        } else {
            Glide.with(mContext).load(R.drawable.music_default).into(holder.album_art);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PlayerActivity.class);
                intent.setAction(MainActivity.PLAY_NEW_SONG);
                Bundle bundle = new Bundle();
                bundle.putInt("source", Playlist.PLAYLIST_SOURCE_ALBUM);
                bundle.putInt("position", position);
                bundle.putString("albumName", album.getName());
                bundle.putString("artist", album.getArtist());
                intent.putExtras(bundle);
                mContext.startActivity(intent);
            }
        });
        holder.favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(OnFavoriteChangeListener fli : listeners) {
                    fli.OnFavoriteChange(song);
                    holder.changeFavoriteColor(mContext, song.isFavorite());
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public void addListener(OnFavoriteChangeListener toAdd) {
        listeners.add(toAdd);
    }
    void updateList(ArrayList<Song> musicFilesArrayList) {
        final SongDiffCallBack diffCallback =
                new SongDiffCallBack(songs, musicFilesArrayList);
        final DiffUtil.DiffResult diffResult
                = DiffUtil.calculateDiff(diffCallback);
        this.songs.clear();
        this.songs.addAll(musicFilesArrayList);
        diffResult.dispatchUpdatesTo(this);
    }
}
