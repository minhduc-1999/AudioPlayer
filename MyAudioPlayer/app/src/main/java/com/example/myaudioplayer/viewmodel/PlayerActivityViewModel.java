package com.example.myaudioplayer.viewmodel;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.myaudioplayer.audiomodel.MusicFiles;

import java.util.ArrayList;
import java.util.Random;

public class PlayerActivityViewModel extends AndroidViewModel {
    private MutableLiveData<ArrayList<MusicFiles>> listSong = new MutableLiveData<>();
    private MutableLiveData<Integer> curPos = new MutableLiveData<>();

    public MutableLiveData<ArrayList<MusicFiles>> getListSong() {
        return listSong;
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
