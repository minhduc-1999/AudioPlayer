package com.example.myaudioplayer.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.myaudioplayer.audiomodel.Album;
import com.example.myaudioplayer.audiomodel.Library;
import com.example.myaudioplayer.audiomodel.Playlist;
import com.example.myaudioplayer.audiomodel.Song;

public class PlaylistViewModel extends BaseViewModel {
    public PlaylistViewModel(@NonNull Application application) {
        super(application);
        setState(playlist.getState());
        setShuffle(playlist.isShuffle());
        setRepeat(playlist.isRepeat());
    }
    private Playlist playlist = Playlist.getInstance();
    private Library library = Library.getInstance();
    private MutableLiveData<Song> curSong = new MutableLiveData<>();
    private MutableLiveData<Boolean> isShuffle = new MutableLiveData<>();
    private MutableLiveData<Boolean> isRepeat = new MutableLiveData<>();
    private MutableLiveData<Integer> state = new MutableLiveData<>();

    public MutableLiveData<Integer> getState() {
        return state;
    }

    public void setState(int state) {
        this.state.postValue(state);
        playlist.setState(state);
    }

    public MutableLiveData<Song> getCurSong() {
        return curSong;
    }

    public MutableLiveData<Boolean> getIsShuffle() {
        return isShuffle;
    }

    public MutableLiveData<Boolean> getIsRepeat() {
        return isRepeat;
    }

    public void setShuffle(Boolean isShuffle)
    {
        this.isShuffle.setValue(isShuffle);
        playlist.setShuffle(isShuffle);
    }

    public void setRepeat(Boolean isRepeat)
    {
        this.isRepeat.setValue(isRepeat);
        playlist.setRepeat(isRepeat);
    }

    public Song nextSong()
    {
        Song temp = playlist.nextSong();
        if (temp != null)
            curSong.postValue(temp);
        return temp;
    }
    public Song preSong()
    {
        Song temp = playlist.preSong();
        if (temp != null)
            curSong.postValue(temp);
        return temp;
    }
    public Song play(int index)
    {
        playlist.setState(Playlist.STATE_PLAY);
        playlist.setCurPos(index);
        this.curSong.postValue(playlist.getCurSong());
        return playlist.getCurSong();
    }
    public void setQueue(int source, String name, String artist)
    {
        if(source == playlist.getCurrentSource())
            return;
        if(source == Playlist.PLAYLIST_SOURCE_SONG)
            playlist.setQueue(library.getAllSongs());
        else if(source == Playlist.PLAYLIST_SOURCE_ALBUM) {
            Album album = library.getAlbum(name, artist);
            if(album != null)
                playlist.setQueue(album.getSongs());
            else
                playlist.setQueue(library.getAllSongs());
        }
        playlist.setCurrentSource(source);
    }
    public String getCurrentAlbumQueue()
    {
        return playlist.getCurrentAlbumQueue();
    }
    public String getCurrentAlbumArtistQueue()
    {
        return playlist.getCurrentAlbumArtistQueue();
    }
    public int getCurrentSource()
    {
        return playlist.getCurrentSource();
    }

    public void setCurrentSource(int source)
    {
        playlist.setCurrentSource(source);
    }

    public Song getSongByPath(String path)
    {
        return playlist.getSongByPath(path);
    }
}
