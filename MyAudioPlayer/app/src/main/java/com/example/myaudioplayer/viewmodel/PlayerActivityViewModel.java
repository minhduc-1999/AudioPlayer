package com.example.myaudioplayer.viewmodel;

import android.app.Application;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.myaudioplayer.audiomodel.MusicFiles;
import com.example.myaudioplayer.audioservice.AudioService;

import java.util.ArrayList;
import java.util.Random;

public class PlayerActivityViewModel extends AndroidViewModel {

    private MutableLiveData<AudioService.AudioBinder> mBinder = new MutableLiveData<>();
    private MutableLiveData<ArrayList<MusicFiles>> listSong = new MutableLiveData<>();
    private MutableLiveData<Integer> curPos = new MutableLiveData<>();

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

    public MutableLiveData<ArrayList<MusicFiles>> getListSong() {
        return listSong;
    }

    public MutableLiveData<AudioService.AudioBinder> getmBinder() {
        return mBinder;
    }

    public MutableLiveData<Integer> getCurPos() {
        return curPos;
    }

    public PlayerActivityViewModel(@NonNull Application application) {
        super(application);
        curPos.setValue(-1);
    }

    public Uri getAudioFileUri(int pos) {
        return Uri.parse(listSong.getValue().get(pos).getPath());
    }

    public MusicFiles getMetadata(int pos) {
        return listSong.getValue().get(pos);
    }

    public void nextSong(boolean isShuffle, boolean isRepeat) {
        int position = -1;
        if (isShuffle && !isRepeat) {
            position = getRandom(listSong.getValue().size() - 1);
        } else if (!isShuffle && !isRepeat) {
            position = (curPos.getValue() + 1) % listSong.getValue().size();
        }
        curPos.setValue(position);
    }

    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i + 1);
    }

}
