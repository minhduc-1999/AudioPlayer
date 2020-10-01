package com.example.myaudioplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

public class PlayAudioService extends Service {
    private static String SEND_CUR_POSITION = "send_cur_position";
    MediaPlayer player;
    IBinder binder;
    Uri uri;

    @Override
    public void onCreate() {
        player = MediaPlayer.create(this, uri);
        binder = new PlayAudioBinder();
        super.onCreate();
    }

    public PlayAudioService(Uri uri) {
        this.uri = uri;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        stopAudio();
        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        playAudio();
        return this.binder;
    }

    public class PlayAudioBinder extends Binder {
        public PlayAudioService getService() {
            return PlayAudioService.this;
        }
    }

    public void playAudio() {
        if (player != null) {
            player.start();
        }
    }

    public void pauseAudio() {
        if (player != null) {
            player.pause();
        }
    }

    @Override
    public void onDestroy() {
        player.release();
        super.onDestroy();
    }

    public void seekTo(int pos) {
        if (player != null) {
            player.seekTo(pos);
        }
    }

    public void stopAudio() {
        if (player != null) {
            player.stop();
        }
    }
}
