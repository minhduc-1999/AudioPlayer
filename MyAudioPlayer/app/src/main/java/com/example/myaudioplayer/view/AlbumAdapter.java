package com.example.myaudioplayer.view;

import android.content.Context;
import android.content.Intent;
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

import java.util.ArrayList;

import static com.example.myaudioplayer.helper.Helper.getEmbeddedArt;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {
    private Context mContext;
    private ArrayList<Album> albums;
    private View view;

    public AlbumAdapter(Context mContext, ArrayList<Album> albums) {
        this.mContext = mContext;
        this.albums = albums;
    }

    @NonNull
    @Override
    public AlbumAdapter.AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(mContext).inflate(R.layout.album_item, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, final int position) {
        if (position >= 0 && position < albums.size()) {
            final Album album = albums.get(position);
            holder.album_name.setText(album.getName());
            byte[] image = getEmbeddedArt(album.getSongs().get(0).getPath());
            if (image != null) {
                Glide.with(mContext).asBitmap().load(image).into(holder.album_image);
            } else {
                Glide.with(mContext).load(R.drawable.music_default).into(holder.album_image);
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, AlbumDetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("albumName",album.getName());
                    bundle.putString("artist",album.getArtist());
                    intent.putExtras(bundle);
                    mContext.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    public class AlbumViewHolder extends RecyclerView.ViewHolder {
        ImageView album_image;
        TextView album_name;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            album_image = itemView.findViewById(R.id.album_image);
            album_name = itemView.findViewById(R.id.album_name);
        }
    }

    void updateList(ArrayList<Album> albums) {
        this.albums = new ArrayList<>();
        this.albums.addAll(albums);
        notifyDataSetChanged();
    }
}
