package com.example.myaudioplayer.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myaudioplayer.R;
import com.example.myaudioplayer.audiointerface.OnFavoriteChangeListener;
import com.example.myaudioplayer.audiomodel.Song;
import com.example.myaudioplayer.helper.SongDiffCallBack;

import java.util.ArrayList;

import static com.example.myaudioplayer.helper.Helper.getEmbeddedArt;


public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.SongViewHolder> {
    private ArrayList<OnFavoriteChangeListener> listeners = new ArrayList<>();
    private Context mContext;
    private ArrayList<Song> songs;
    private int source_type;

    public MusicAdapter(Context mContext, int source) {
        this.mContext = mContext;
        this.songs = new ArrayList<>();
        this.source_type = source;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.music_items, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final SongViewHolder holder, final int position) {
        if (position >= 0 && position < songs.size()) {
            final Song song = songs.get(position);
            holder.file_name.setText(song.getTitle());
            holder.artist_name.setText(song.getArtist());
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
                    bundle.putInt("position", position);
                    bundle.putInt("source", source_type);
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
    }

    void updateList(ArrayList<Song> musicFilesArrayList) {
        final SongDiffCallBack diffCallback =
                new SongDiffCallBack(this.songs, musicFilesArrayList);
        final DiffUtil.DiffResult diffResult
                = DiffUtil.calculateDiff(diffCallback);
        this.songs.clear();
        this.songs.addAll(musicFilesArrayList);
        diffResult.dispatchUpdatesTo(this);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }


    public static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView file_name;
        TextView artist_name;
        ImageView album_art;
        ImageView favorite;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            file_name = itemView.findViewById(R.id.music_file_name);
            album_art = itemView.findViewById(R.id.music_img);
            artist_name = itemView.findViewById(R.id.music_artist_name);
            favorite = itemView.findViewById(R.id.favorite);
        }

        public void changeFavoriteColor(Context context, boolean favorite) {
            if (favorite)
                this.favorite.getDrawable().setColorFilter(context.getResources().getColor(R.color.progress), PorterDuff.Mode.SRC_IN);
            else
                this.favorite.getDrawable().setColorFilter(context.getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        }
    }

    public void addListener(OnFavoriteChangeListener toAdd) {
        listeners.add(toAdd);
    }
}
