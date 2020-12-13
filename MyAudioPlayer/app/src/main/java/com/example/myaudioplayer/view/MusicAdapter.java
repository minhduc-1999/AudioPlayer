package com.example.myaudioplayer.view;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myaudioplayer.R;
import com.example.myaudioplayer.audiomodel.Playlist;
import com.example.myaudioplayer.audiomodel.Song;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.SongViewHolder> {
    private Context mContext;
    private ArrayList<Song> mFiles;

    public MusicAdapter(Context mContext, ArrayList<Song> songs) {
        this.mContext = mContext;
        this.mFiles = songs;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.music_items, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, final int position) {
        if(position >= 0 && position < mFiles.size()) {
            Song file = mFiles.get(position);
            holder.file_name.setText(file.getTitle());
            holder.artist_name.setText(file.getArtist());
            byte[] image = getAlbumArt(file.getPath());
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
                    bundle.putInt("source", Playlist.PLAYLIST_SOURCE_SONG);
                    intent.putExtras(bundle);
                    mContext.startActivity(intent);
                }
            });
            holder.menu_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    PopupMenu popupMenu = new PopupMenu(mContext, v);
                    popupMenu.getMenuInflater().inflate(R.menu.popup, popupMenu.getMenu());
                    popupMenu.show();
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.delete:
                                    Toast.makeText(mContext, "Delete Clicked!", Toast.LENGTH_SHORT).show();
                                    deleteFile(position, v);
                                    break;
                            }
                            return true;
                        }
                    });
                }
            });
        }
    }

    private void deleteFile(int position, View v) {
        Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.parseLong(mFiles.get(position).getId()));
        File file = new File(mFiles.get(position).getPath());
        boolean deleted = file.delete();
        if (deleted) {
            mContext.getContentResolver().delete(contentUri, null, null);
            mFiles.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mFiles.size());
            Snackbar.make(v, "Song Deleted", Snackbar.LENGTH_LONG).show();
        }
        else {
            Snackbar.make(v, "Can't delete this song", Snackbar.LENGTH_LONG).show();
        }


    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }


    public static class SongViewHolder extends RecyclerView.ViewHolder{
        TextView file_name;
        TextView artist_name;
        ImageView album_art, menu_more;
        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            file_name = itemView.findViewById(R.id.music_file_name);
            album_art = itemView.findViewById(R.id.music_img);
            menu_more = itemView.findViewById(R.id.menu_more);
            artist_name = itemView.findViewById(R.id.music_artist_name);
        }
    }

    private byte[] getAlbumArt(String uri)
    {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }

    void updateList(ArrayList<Song> musicFilesArrayList) {
        mFiles = new ArrayList<>();
        mFiles.addAll(musicFilesArrayList);
        notifyDataSetChanged();
    }
}
