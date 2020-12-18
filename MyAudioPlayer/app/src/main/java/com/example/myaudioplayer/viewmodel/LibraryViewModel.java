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
    private MutableLiveData<ArrayList<Song>> favoriteLists = new MutableLiveData<>();

    public LibraryViewModel(@NonNull Application application) {
        super(application);
        songs.setValue(library.getAllSongs());
        albums.setValue(library.getAlbums());
        favoriteLists.setValue(library.getFavoriteSongs());
    }

    public MutableLiveData<ArrayList<Song>> getSongs() {
        return songs;
    }

    public MutableLiveData<ArrayList<Album>> getAlbums() {
        return albums;
    }

    public MutableLiveData<ArrayList<Song>> getFavoriteLists() {
        return favoriteLists;
    }

    public void loadLocalSong(int order) {
        library.loadAllSong(getApplication().getApplicationContext(), order);
        //songs.postValue(library.getAllSongs());
        //albums.postValue(library.getAlbums());
    }

    public Album getAlbumByName(String name, String artist) {
        return library.getAlbum(name, artist);
    }

    public void sortLibrary(final int order) {
        Thread sortThread = new Thread() {
            @Override
            public void run() {
                super.run();
                if (library.sortSongs(order)) {
                    songs.postValue(library.getAllSongs());
                }
            }
        };
        sortThread.start();

    }

    public void setFavoriteList(String favorite) {
        library.setFavoriteSongs(favorite);
    }
    public String enCodeFavorite(){
        return library.enCodeFavorite();
    }
}
