package com.example.myaudioplayer.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import androidx.lifecycle.MutableLiveData;

import com.example.myaudioplayer.audiomodel.Album;
import com.example.myaudioplayer.audiomodel.Library;
import com.example.myaudioplayer.audiomodel.Song;


import java.util.ArrayList;

public class LibraryViewModel extends BaseViewModel {

    private Library library = Library.getInstance();
    private MutableLiveData<ArrayList<Song>> songs = new MutableLiveData<>();
    private MutableLiveData<ArrayList<Album>> albums = new MutableLiveData<>();

    public LibraryViewModel(@NonNull Application application) {
        super(application);
        songs.setValue(new ArrayList<Song>());
        albums.setValue(new ArrayList<Album>());
    }

    public MutableLiveData<ArrayList<Song>> getSongs() {
        return songs;
    }

    public MutableLiveData<ArrayList<Album>> getAlbums() {
        return albums;
    }

    public void loadLocalSong()
    {
        library.loadAllSong(getApplication().getApplicationContext());
        songs.postValue(library.getAllSongs());
        albums.postValue(library.getAlbums());
    }

    public Album getAlbumByName(String name, String artist)
    {
        return library.getAlbum(name, artist);
    }
}
