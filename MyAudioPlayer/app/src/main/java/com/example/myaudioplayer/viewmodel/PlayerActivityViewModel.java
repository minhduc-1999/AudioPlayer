package com.example.myaudioplayer.viewmodel;

import android.app.Application;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.myaudioplayer.audiomodel.MusicFiles;
import com.example.myaudioplayer.audioservice.AudioService;

import java.util.ArrayList;
import java.util.Random;

public class PlayerActivityViewModel extends AndroidViewModel {

    private MutableLiveData<AudioService.AudioBinder> mBinder = new MutableLiveData<>();
    private MutableLiveData<MusicFiles> curSong = new MutableLiveData<>();

    public MutableLiveData<MusicFiles> getCurSong() {
        return curSong;
    }

    public ServiceConnection getServiceConnection() {
        return serviceConnection;
    }

    private ServiceConnection serviceConnection = new ServiceConnection () {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioService.AudioBinder binder = (AudioService.AudioBinder) service;
            mBinder.postValue(binder);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBinder.postValue(null);
        }
    };

    public MutableLiveData<AudioService.AudioBinder> getmBinder() {
        return mBinder;
    }

    public PlayerActivityViewModel(@NonNull Application application) {
        super(application);
    }

}
