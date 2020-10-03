package com.example.myaudioplayer.audioservice;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;

import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.myaudioplayer.PlayerActivity;

import java.io.IOException;

public class AudioService extends Service implements MediaPlayer.OnCompletionListener {
    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_PAUSE = "ACTION_PASUE";
    public static final String BRC_SERVICE_FILTER = "BRC_SERVICE";


    private boolean isPlaying;

    private MediaPlayer mediaPlayer;

    private IBinder mBinder;

    public AudioService() {

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate() {
        /*mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );*/
        //mediaPlayer.setOnPreparedListener(this);
        mBinder = new AudioBinder();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        /*if (intent.getAction().equals(ACTION_PLAY)) {
            Uri uri = Uri.parse(intent.getStringExtra("uri"));
            changeAudio(uri);
        }*/
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        mediaPlayer.release();
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    public void seekTo(int pos) {
        if (mediaPlayer != null )
            mediaPlayer.seekTo(pos);
    }

    public void playPauseAudio(String action) {
        if(mediaPlayer == null)
            return;
        if (action.equals(ACTION_PLAY)) {
            mediaPlayer.start();
            isPlaying = true;
        }
        else
            mediaPlayer.pause();
        isPlaying = false;
    }

    public int getCurrentDuration() {
        if (mediaPlayer != null)
            return mediaPlayer.getCurrentPosition();
        return -1;
    }

    public boolean isPlaying() {
        if (mediaPlayer != null)
            return mediaPlayer.isPlaying();
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void changeAudio(Uri uri) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(this, uri);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.start();
        isPlaying = true;
        //prepareAudio(uri);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        sendServiceBroadcast(BRC_SERVICE_FILTER);
    }

    private void sendServiceBroadcast(String filter) {
        Intent intent = new Intent();
        intent.setAction(filter);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public class AudioBinder extends Binder {
        public AudioService getService() {
            return AudioService.this;
        }
    }
}
