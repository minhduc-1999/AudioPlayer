package com.example.myaudioplayer.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.example.myaudioplayer.R;
import com.example.myaudioplayer.audioservice.AudioService;

public class WaitingActivity extends AppCompatActivity {
    private  BroadcastReceiver broadcastReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String info = intent.getStringExtra("info");
                switch (info) {
                    case MainActivity.INIT_TASK_DONE:
                        finish();
                        break;
                    default:
                        break;
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter(MainActivity.INIT_TASK_DONE);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
    }
}