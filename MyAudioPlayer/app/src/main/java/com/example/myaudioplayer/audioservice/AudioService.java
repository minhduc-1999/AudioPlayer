package com.example.myaudioplayer.audioservice;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.myaudioplayer.notification.NotificationReceiver;
import com.example.myaudioplayer.view.MainActivity;

import com.example.myaudioplayer.R;

import com.example.myaudioplayer.audiomodel.Song;
import com.example.myaudioplayer.view.PlayerActivity;

import java.io.IOException;

import static com.example.myaudioplayer.view.MainActivity.MCHANNEL;
import static com.example.myaudioplayer.audiomodel.Playlist.*;


public class AudioService extends Service implements MediaPlayer.OnCompletionListener {
    public static final String BRC_SERVICE_FILTER = "BRC_SERVICE_ACTION";
    public static final String BRC_AUDIO_CHANGE = "BRC_AUDIO_CHANGE";
    public static final String BRC_AUDIO_COMPLETED = "BRC_AUDIO_COMPLETED";
    public static final String BRC_PLAYING_STATE_CHANGE = "BRC_PLAYING_STATE_CHANGE";
    public static final String PLAYBUTTON = "PLAYBUTTON";
    public static final String PREBUTTON = "PREBUTTON";
    public static final String NEXTBUTTON = "NEXTBUTTON";

    private Song curSong;
    private int state;
    private MediaPlayer mediaPlayer;

    public Song getCurSong() {
        return curSong;
    }

    private IBinder mBinder;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void changeAudio(Song song) throws IOException {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            this.state = STATE_NONE;
        }
        curSong = song;
        Uri uri = Uri.parse(song.getPath());
//        mediaPlayer = MediaPlayer.create(this, uri);
//        mediaPlayer.setOnCompletionListener(this);
//        mediaPlayer.start();

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
        mediaPlayer.setDataSource(getApplicationContext(), uri);
        mediaPlayer.prepare();
        mediaPlayer.start();
        setState(STATE_PLAY);
        sendServiceBroadcast(BRC_SERVICE_FILTER, BRC_AUDIO_CHANGE);
    }

    public AudioService() {

    }

    public int getState() {
        return state;
    }

    @Override
    public void onCreate() {
        mBinder = new AudioBinder();
        state = STATE_NONE;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
        if (mediaPlayer != null)
            mediaPlayer.release();
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    public void seekTo(int pos) {
        if (mediaPlayer != null)
            mediaPlayer.seekTo(pos);
    }

    public void playPauseAudio() {
        if (mediaPlayer == null || state == STATE_NONE)
            return;
        if (state == STATE_PAUSE) {
            mediaPlayer.start();
            setState(STATE_PLAY);
        } else {
            mediaPlayer.pause();
            setState(STATE_PAUSE);
        }
    }

    public int getCurrentDuration() {
        if (mediaPlayer != null)
            try {
                return mediaPlayer.getCurrentPosition() / 1000;
            } catch (IllegalStateException e) {
                return 0;
            }

        return 0;
    }

    private void setState(int state) {
        if (this.state == state)
            return;
        this.state = state;
        if(state == STATE_PLAY)
            showNotification(R.drawable.ic_round_pause_24);
        else if(state == STATE_PAUSE)
            showNotification(R.drawable.ic_round_play_arrow_24);
        sendServiceBroadcast(BRC_SERVICE_FILTER, BRC_PLAYING_STATE_CHANGE);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        sendServiceBroadcast(BRC_SERVICE_FILTER, BRC_AUDIO_COMPLETED);
    }

    private void sendServiceBroadcast(String filter, String info) {
        Intent intent = new Intent();
        intent.setAction(filter);
        intent.putExtra("info", info);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public class AudioBinder extends Binder {
        public AudioService getService() {
            return AudioService.this;
        }
    }

    public void showNotification(int playPauseBtn) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(curSong.getPath());
        byte[] art = retriever.getEmbeddedPicture();
        Bitmap bitmap;
        if (art != null) {
            bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
        } else {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.music_default);
        }
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.setAction(MainActivity.OPEN_PLAYING_BAR);
        Bundle bundle = new Bundle();
        bundle.putInt("curDuration", this.getCurrentDuration());
        bundle.putInt("totalDuration", Integer.parseInt(curSong.getDuration()) / 1000);
        intent.putExtras(bundle);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
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
                    .setContentTitle(curSong.getTitle())
                    .setContentText(curSong.getArtist())
                    .addAction(R.drawable.ic_round_skip_previous_24, "Previous", prevPendingIntent)
                    .addAction(playPauseBtn, "Play", playPendingIntent)
                    .addAction(R.drawable.ic_round_skip_next_24, "Next", nextPendingIntent)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle())
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setOngoing(true)
                    .setLocalOnly(true)
                    .setNotificationSilent()
                    .setContentIntent(contentIntent)
                    .setLocalOnly(true);
        }
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, mBuilder.build());
    }
}
