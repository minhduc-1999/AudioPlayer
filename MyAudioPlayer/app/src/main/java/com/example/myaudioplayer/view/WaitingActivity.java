package com.example.myaudioplayer.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.myaudioplayer.R;
import com.example.myaudioplayer.audiomodel.Library;
import com.example.myaudioplayer.audiomodel.Playlist;
import com.example.myaudioplayer.viewmodel.LibraryViewModel;
import com.example.myaudioplayer.viewmodel.PlaylistViewModel;

import static java.lang.Thread.sleep;

public class WaitingActivity extends AppCompatActivity {
    private LibraryViewModel libraryViewModel;
    private PlaylistViewModel playlistViewModel;
    private int sortOrder;
    private int curDuration;
    private String curSong;
    private int source;
    private String albumName;
    private String artist;
    private boolean shuffle;
    private boolean repeat;
    private String favorite;
    //permission
    public static final int REQUEST_CODE = 1;
    private boolean hasPermission;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //hasPermission = false;

        permission();
//        if (!hasPermission) {
//            finish();
//            System.exit(0);
//        }

    }

    private void doIfHasPermisstion() {
        libraryViewModel = ViewModelProviders.of(this).get(LibraryViewModel.class);
        playlistViewModel = ViewModelProviders.of(this).get(PlaylistViewModel.class);
        Thread welcomeThread = new Thread() {
            @Override
            public void run() {
                try {
                    super.run();
                    loadAppState();
                    libraryViewModel.loadLocalSong(sortOrder);
                    setRestoredState();
                    sleep(5000);  //Delay of 5 seconds
                } catch (Exception e) {

                } finally {
                    Intent i = new Intent(WaitingActivity.this,
                            MainActivity.class);
                    i.putExtra("sortOrder", sortOrder);
                    startActivity(i);
                    finish();
                }
            }
        };
        welcomeThread.start();
    }

    private void permission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestStoragePermission();
        } else {
            //hasPermission = true;
            doIfHasPermisstion();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0)
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    hasPermission = true;
                    doIfHasPermisstion();
                } else
                    ActivityCompat.requestPermissions(WaitingActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
    }

    public void loadAppState() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("MusicPlayerSetting", Context.MODE_PRIVATE);

        if (sharedPreferences != null) {
            sortOrder = sharedPreferences.getInt("sortOrder", Library.SORT_NONE);
            curDuration = sharedPreferences.getInt("currentDuration", 0);
            curSong = sharedPreferences.getString("curSong", "");
            source = sharedPreferences.getInt("source", Playlist.PLAYLIST_SOURCE_SONG);
            albumName = sharedPreferences.getString("albumName", "");
            artist = sharedPreferences.getString("artist", "");
            shuffle = sharedPreferences.getBoolean("shuffle", false);
            repeat = sharedPreferences.getBoolean("repeat", false);
            favorite = sharedPreferences.getString("favorite", "");
        } else {
            sortOrder = Library.SORT_NONE;
            curDuration = 0;
            curSong = "";
            source = Playlist.PLAYLIST_SOURCE_SONG;
            albumName = "";
            artist = "";
            shuffle = false;
            repeat = false;
            favorite = "";
        }
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("Permission is needed to access music files from your device...")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(WaitingActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
    }

    public void setRestoredState() {
        libraryViewModel.setFavoriteList(favorite);
        if (curSong != "")
            playlistViewModel.setState(Playlist.STATE_PAUSE);
        else
            playlistViewModel.setState(Playlist.STATE_NONE);
        playlistViewModel.setQueue(source, albumName, artist);
        playlistViewModel.setCurDuration(curDuration);
        playlistViewModel.setCurSong(curSong);
        playlistViewModel.setRepeat(repeat);
        playlistViewModel.setShuffle(shuffle);
    }
}