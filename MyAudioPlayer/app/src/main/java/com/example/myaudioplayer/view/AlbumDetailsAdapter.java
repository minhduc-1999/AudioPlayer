package com.example.myaudioplayer.view;

import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myaudioplayer.R;
import com.example.myaudioplayer.audiomodel.Album;
import com.example.myaudioplayer.audiomodel.Playlist;
import com.example.myaudioplayer.audiomodel.Song;

public class AlbumDetailsAdapter extends RecyclerView.Adapter<MusicAdapter.SongViewHolder> {
    private Context mContext;
    private Album album;
    private View view;

    public AlbumDetailsAdapter(Context mContext, Album album) {
        this.mContext = mContext;
        this.album = album;
    }

    @NonNull
    @Override
    public MusicAdapter.SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(mContext).inflate(R.layout.music_items, parent, false);
        return new MusicAdapter.SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MusicAdapter.SongViewHolder holder, final int position) {
        final Song song = album.getSongs().get(position);
        holder.file_name.setText(song.getTitle());
        byte[] image = getAlbumArt(song.getPath());
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

    }

    @Override
    public int getItemCount() {
        return album.getSongs().size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {
        ImageView album_image;
        TextView album_name;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            album_image = itemView.findViewById(R.id.music_img);
            album_name = itemView.findViewById(R.id.music_file_name);
        }
    }

    private byte[] getAlbumArt(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }
}
