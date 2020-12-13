package com.example.myaudioplayer.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.myaudioplayer.R;
import com.example.myaudioplayer.audiomodel.Album;
import com.example.myaudioplayer.viewmodel.LibraryViewModel;

public class AlbumDetailActivity extends AppCompatActivity {
    private LibraryViewModel libraryViewModel;

    private RecyclerView recyclerView;
    private ImageView albumPhoto;
    private Album album;
    private AlbumDetailsAdapter albumDetailsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_detail);
        libraryViewModel = ViewModelProviders.of(this).get(LibraryViewModel.class);
        registerLiveDataListenner();

        recyclerView = findViewById(R.id.recyclerView);
        albumPhoto = findViewById(R.id.albumPhoto);

        Bundle bundle = getIntent().getExtras();

        String albumName = bundle.getString("albumName", "");
        String artist = bundle.getString("artist", "");

        album = libraryViewModel.getAlbumByName(albumName, artist);

        byte[] image = getAlbumArt(album.getSongs().get(0).getPath());
        if (image != null) {
            Glide.with(this).load(image).into(albumPhoto);
        } else {
            Glide.with(this).load(R.drawable.music_default).into(albumPhoto);
        }
    }

    private void registerLiveDataListenner() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!(album.getSongs().size() < 1)) {
            albumDetailsAdapter = new AlbumDetailsAdapter(this, album);
            recyclerView.setAdapter(albumDetailsAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
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