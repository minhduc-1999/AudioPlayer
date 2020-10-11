package com.example.myaudioplayer.audioservice;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaTimestamp;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.myaudioplayer.MainActivity;
import com.example.myaudioplayer.PlayerActivity;
import com.example.myaudioplayer.R;
import com.example.myaudioplayer.audiomodel.MusicFiles;
import com.example.myaudioplayer.notification.NotificationReceiver;

import java.util.ArrayList;
import java.util.Random;

import static com.example.myaudioplayer.MainActivity.MCHANNEL;
import static com.example.myaudioplayer.PlayerActivity.ImageAnimation;

public class AudioService extends Service implements MediaPlayer.OnCompletionListener {
    public static final String BRC_SERVICE_FILTER = "BRC_SERVICE";
    public static final String BRC_AUDIO_CHANGE = "BRC_AUDIO_CHANGE";
    public static final String BRC_PLAYING_STATE_CHANGE = "BRC_PLAYING_STATE_CHANGE";
    public static final String STATE_PLAY = "STATE_PLAY";
    public static final String STATE_PAUSE = "STATE_PAUSE";
    public static final String STATE_NONE = "STATE_NONE";
    public static final String PLAYLIST_SOURCE_SONG = "PLAYLIST_SOURCE_SONG";
    public static final String PLAYLIST_SOURCE_ALBUM = "PLAYLIST_SOURCE_ALBUM";
    public static final String PLAYLIST_SOURCE_NONE = "PLAYLIST_SOURCE_NONE";
    public static final String PLAYBUTTON = "PLAYBUTTON";
    public static final String PREBUTTON = "PREBUTTON";
    public static final String NEXTBUTTON = "NEXTBUTTON";

    private ArrayList<MusicFiles> mPlaylist;
    private int curSongPos;
    //private boolean isPlaying;
    private String state;
    private String playlist_source;
    private boolean isShuffle, isRepeat;
    BroadcastReceiver notificationReceiver;

    public String getPlaylist_source() {
        return playlist_source;
    }

    public int getCurSongPos() {
        return curSongPos;
    }

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

    public String getState() {
        return state;
    }

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
        state = STATE_NONE;
        playlist_source = PLAYLIST_SOURCE_NONE;
    }

    public void setPlaylist_source(String playlist_source) {
        this.playlist_source = playlist_source;
    }

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
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
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

    public void playPauseAudio() {
        if (mediaPlayer == null || state == STATE_NONE)
            return;
        if (state == STATE_PAUSE) {
            mediaPlayer.start();
            setState(STATE_PLAY);
            showNotification(R.drawable.ic_round_pause_24);

        } else {
            mediaPlayer.pause();
            setState(STATE_PAUSE);
            showNotification(R.drawable.ic_round_play_arrow_24);
        }
    }

    public int getCurrentDuration() {
        if (mediaPlayer != null)
            return mediaPlayer.getCurrentPosition();
        return -1;
    }

    private void setState(String state) {
        if (this.state == state)
            return;
        this.state = state;
        sendServiceBroadcast(BRC_SERVICE_FILTER, BRC_PLAYING_STATE_CHANGE);
    }

    public void changeAudio(int pos) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            setState(STATE_NONE);
        }
        Uri uri = Uri.parse(mPlaylist.get(pos).getPath());
        mediaPlayer = MediaPlayer.create(this, uri);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.start();
        setState(STATE_PLAY);
        curSongPos = pos;
        sendServiceBroadcast(BRC_SERVICE_FILTER, BRC_AUDIO_CHANGE);
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
        showNotification(R.drawable.ic_round_pause_24);
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
        showNotification(R.drawable.ic_round_pause_24);

    }

    public MusicFiles getCurSong() {
        return mPlaylist.get(curSongPos);
    }

    public void showNotification (int playPauseBtn) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(mPlaylist.get(curSongPos).getPath());
        byte[] art = retriever.getEmbeddedPicture();
        Bitmap bitmap;
        if (art != null) {
            bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
        } else {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.music_default);
        }
        Intent intent = new Intent(this,PlayerActivity.class );
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent,0);
        Intent prevIntent = new Intent(this, NotificationReceiver.class).setAction(PREBUTTON);
        PendingIntent prevPendingIntent = PendingIntent.getBroadcast(this, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent playIntent = new Intent(this, NotificationReceiver.class).setAction(PLAYBUTTON);
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent nextIntent = new Intent(this, NotificationReceiver.class).setAction(NEXTBUTTON);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder = new NotificationCompat.Builder(this, MCHANNEL)
                    .setSmallIcon(R.drawable.ic_round_queue_music_24)
                    .setLargeIcon(bitmap)
                    .setContentTitle(mPlaylist.get(curSongPos).getTitle())
                    .setContentText(mPlaylist.get(curSongPos).getArtist())
                    .addAction(R.drawable.ic_round_skip_previous_24, "Previous", prevPendingIntent)
                    .addAction(playPauseBtn, "Play", playPendingIntent)
                    .addAction(R.drawable.ic_round_skip_next_24, "Next", nextPendingIntent)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle())
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setOngoing(true)
                    .setLocalOnly(true)
                    .setNotificationSilent()
                    .setContentIntent(contentIntent)
                    .setLocalOnly(true);
        }
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, mBuilder.build());
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
