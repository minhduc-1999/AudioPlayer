package com.example.myaudioplayer.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.WindowManager;

import com.example.myaudioplayer.R;
import com.example.myaudioplayer.audioservice.AudioService;
import com.example.myaudioplayer.viewmodel.LibraryViewModel;

public class WaitingActivity extends AppCompatActivity {
    private LibraryViewModel libraryViewModel;
    boolean hasPermission;
    public static final int REQUEST_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        libraryViewModel = ViewModelProviders.of(this).get(LibraryViewModel.class);
        hasPermission = false;
        while (!hasPermission)
        {
            permission();
        }
        Thread welcomeThread = new Thread() {

            @Override
            public void run() {
                try {
                    super.run();
                    libraryViewModel.loadLocalSong();
                    sleep(5000);  //Delay of 5 seconds
                } catch (Exception e) {

                } finally {
                    Intent i = new Intent(WaitingActivity.this,
                            MainActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        };
        welcomeThread.start();
    }
    private void permission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(WaitingActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        } else {
            hasPermission = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            hasPermission = true;
        } else {
            ActivityCompat.requestPermissions(WaitingActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
    }
}