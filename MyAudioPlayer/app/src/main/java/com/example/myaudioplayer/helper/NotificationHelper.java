package com.example.myaudioplayer.helper;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class NotificationHelper {
    public static void send(Context context, String filter, String job)
    {
        Intent intent = new Intent();
        intent.setAction(filter);
        intent.putExtra("job", job);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
    public static void send(Context context, String filter, String[] jobName, String[] action)
    {
        Intent intent = new Intent();
        intent.setAction(filter);
        Bundle bundle = new Bundle();
        for(int i = 0; i < jobName.length; i++)
        {
            bundle.putString(jobName[i], action[i]);
        }
        intent.putExtras(bundle);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
