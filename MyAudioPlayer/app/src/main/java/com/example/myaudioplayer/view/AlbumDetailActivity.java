package com.example.myaudioplayer.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myaudioplayer.R;
import com.example.myaudioplayer.audiointerface.OnFavoriteChangeListener;
import com.example.myaudioplayer.audiomodel.Album;
import com.example.myaudioplayer.audiomodel.Song;
import com.example.myaudioplayer.viewmodel.LibraryViewModel;

import static com.example.myaudioplayer.helper.AnimationHelper.ImageAnimation;

public class AlbumDetailActivity extends AppCompatActivity implements OnFavoriteChangeListener {
    private LibraryViewModel libraryViewModel;

    private RecyclerView recyclerView;
    private ImageView albumPhoto, btn_back;
    private Album album;
    private TextView album_name, album_artist;
    private AlbumDetailsAdapter albumDetailsAdapter;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_detail);
        libraryViewModel = ViewModelProviders.of(this).get(LibraryViewModel.class);
        registerLiveDataListenner();

        recyclerView = findViewById(R.id.recyclerView);
        albumPhoto = findViewById(R.id.albumPhoto);
        album_name = findViewById(R.id.album_name);
        album_artist = findViewById(R.id.album_artist);
        btn_back = findViewById(R.id.back_btn);

        Bundle bundle = getIntent().getExtras();

        String albumName = bundle.getString("albumName", "");
        String artist = bundle.getString("artist", "");

        album_name.setText(albumName);
        album_artist.setText(artist);

        album = libraryViewModel.getAlbumByName(albumName, artist);

        byte[] image = getAlbumArt(album.getSongs().get(0).getPath());
        if (image != null) {
            ImageAnimation(this, albumPhoto, image, false);
            //Glide.with(this).load(image).into(albumPhoto);
        } else {
            ImageAnimation(this, albumPhoto, R.drawable.background, false);
            //Glide.with(this).load(R.drawable.background).into(albumPhoto);
        }
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void registerLiveDataListenner() {

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!(album.getSongs().size() < 1)) {
            albumDetailsAdapter = new AlbumDetailsAdapter(this, album);
            albumDetailsAdapter.addListener(this);
            recyclerView.setAdapter(albumDetailsAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        }
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (!(album.getSongs().size() < 1)) {
//            albumDetailsAdapter = new AlbumDetailsAdapter(this, album);
//            recyclerView.setAdapter(albumDetailsAdapter);
//            recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
//        }
//    }

    private byte[] getAlbumArt(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }

    @Override
    public void OnFavoriteChange(Song song) {
        libraryViewModel.changeFavorite(song);
    }
}