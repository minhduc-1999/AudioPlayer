package com.example.myaudioplayer.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static com.example.myaudioplayer.PlayerActivity.audioService;
import static com.example.myaudioplayer.audioservice.AudioService.NEXTBUTTON;
import static com.example.myaudioplayer.audioservice.AudioService.PLAYBUTTON;
import static com.example.myaudioplayer.audioservice.AudioService.PREBUTTON;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction()!= null)
        {
            switch ( (intent.getAction()))
            {
                case PLAYBUTTON:
                    audioService.playPauseAudio();
                    break;
                case PREBUTTON:
                    audioService.preSong();
                    break;
                case NEXTBUTTON:
                    audioService.nextSong();
                    break;
            }
        }

    }
}
