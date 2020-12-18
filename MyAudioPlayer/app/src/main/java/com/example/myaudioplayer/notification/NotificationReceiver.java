package com.example.myaudioplayer.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static com.example.myaudioplayer.audioservice.AudioService.NEXTBUTTON;
import static com.example.myaudioplayer.audioservice.AudioService.PLAYBUTTON;
import static com.example.myaudioplayer.audioservice.AudioService.PREBUTTON;
import static com.example.myaudioplayer.helper.NotificationHelper.send;

public class NotificationReceiver extends BroadcastReceiver {
    public static final String BRC_NOTIFY_FILTER = "BRC_NOTIFY_ACTION";
    public static final String BRC_NOTIFY_NEXT = "BRC_NOTIFY_NEXT";
    public static final String BRC_NOTIFY_PRE = "BRC_NOTIFY_PRE";
    public static final String BRC_NOTIFY_PLAY = "BRC_NOTIFY_PLAY";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction()!= null)
        {
            switch ( (intent.getAction()))
            {
                case PLAYBUTTON:
                    //Toast.makeText(context, "PlayButton click", Toast.LENGTH_SHORT).show();
                    send(context, BRC_NOTIFY_FILTER, BRC_NOTIFY_PLAY);
                    break;
                case PREBUTTON:
                    //Toast.makeText(context, "pre button click", Toast.LENGTH_SHORT).show();
                    send(context, BRC_NOTIFY_FILTER, BRC_NOTIFY_PRE);
                    break;
                case NEXTBUTTON:
                    //Toast.makeText(context, "next button click", Toast.LENGTH_SHORT).show();
                    send(context, BRC_NOTIFY_FILTER, BRC_NOTIFY_NEXT);
                    break;
            }
        }

    }

}
