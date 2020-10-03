package com.example.myaudioplayer.audioservice;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.myaudioplayer.audiomodel.MusicFiles;

import java.util.ArrayList;
import java.util.Random;

public class AudioService extends Service implements MediaPlayer.OnCompletionListener {
    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_PAUSE = "ACTION_PASUE";
    public static final String BRC_SERVICE_FILTER = "BRC_SERVICE";
    public static final String BRC_AUDIO_CHANGE = "BRC_AUDIO_CHANGE";
    public static final String BRC_AUDIO_COMPLETE = "BRC_AUDIO_COMPLETE";

    private ArrayList<MusicFiles> mPlaylist;
    private int curSongPos;
    private boolean isPlaying;
    private boolean isShuffle, isRepeat;

    private MediaPlayer mediaPlayer;

    public boolean isShuffle() {
        return isShuffle;
    }

    public void setShuffle(boolean shuffle) {
        isShuffle = shuffle;
    }

    public boolean isRepeat() {
        return isRepeat;
    }

    public void setRepeat(boolean repeat) {
        isRepeat = repeat;
    }

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

    public void changeAudio(int pos) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        Uri uri = Uri.parse(mPlaylist.get(pos).getPath());
        mediaPlayer = MediaPlayer.create(this, uri);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.start();
        isPlaying = true;
        curSongPos = pos;
        sendServiceBroadcast(BRC_SERVICE_FILTER, BRC_AUDIO_CHANGE);
        //prepareAudio(uri);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        nextSong();
    }

    private void sendServiceBroadcast(String filter, String info) {
        Intent intent = new Intent();
        intent.setAction(filter);
        intent.putExtra("info", info);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void setPlaylist(ArrayList<MusicFiles> playlist)
    {
        this.mPlaylist = playlist;
    }

    public void nextSong() {
        int position = -1;
        if (isShuffle && !isRepeat) {
            position = getRandom(mPlaylist.size() - 1);
        } else if (!isShuffle && !isRepeat) {
            position = (curSongPos + 1) % mPlaylist.size();
        }
        curSongPos = position;
        changeAudio(curSongPos);
    }

    public void preSong() {
        int position = -1;
        if (isShuffle && !isRepeat) {
            position = getRandom(mPlaylist.size() - 1);
        } else if (!isShuffle && !isRepeat) {
            position = (curSongPos - 1 + mPlaylist.size()) % mPlaylist.size();
        }
        curSongPos = position;
        changeAudio(curSongPos);
    }

    public MusicFiles getCurSong() {
        return mPlaylist.get(curSongPos);
    }

    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i + 1);
    }
    public class AudioBinder extends Binder {
        public AudioService getService() {
            return AudioService.this;
        }
    }
}
