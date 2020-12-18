package com.example.myaudioplayer.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.myaudioplayer.audiomodel.Album;
import com.example.myaudioplayer.audiomodel.Library;
import com.example.myaudioplayer.audiomodel.Playlist;
import com.example.myaudioplayer.audiomodel.Song;
import com.example.myaudioplayer.audioservice.AudioService;

public class PlaylistViewModel extends BaseViewModel {
    public PlaylistViewModel(@NonNull Application application) {
        super(application);
        setState(playlist.getState());
        setShuffle(playlist.isShuffle());
        setRepeat(playlist.isRepeat());
        Song currSong = playlist.getNowSong();
        if (currSong != null)
            this.curSong.postValue(playlist.getNowSong());
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

    public void setShuffle(Boolean isShuffle) {
        this.isShuffle.setValue(isShuffle);
        playlist.setShuffle(isShuffle);
    }

    public void setRepeat(Boolean isRepeat) {
        this.isRepeat.setValue(isRepeat);
        playlist.setRepeat(isRepeat);
    }

    public Song nextSong() throws Exception {
        Song temp = playlist.nextSong();
        if (temp != null)
        {
            curSong.postValue(temp);
            return temp;
        }
            state.postValue(Playlist.STATE_NONE);
            throw new Exception("Queue empty");
    }

    public Song preSong() {
        Song temp = playlist.preSong();
        if (temp != null)
            curSong.postValue(temp);
        return temp;
    }

    public Song play(int index) {
        playlist.setState(Playlist.STATE_PLAY);
        if (playlist.setNowSong(index))
            return playlist.getNowSong();
        return null;
    }

    public void setQueue(int source, String name, String artist) {
        if (source == playlist.getCurrentSource())
            return;
        switch (source) {
            case Playlist.PLAYLIST_SOURCE_SONG:
                playlist.setQueue(library.getAllSongs());
                break;
            case Playlist.PLAYLIST_SOURCE_ALBUM:
                Album album = library.getAlbum(name, artist);
                if (album != null)
                    playlist.setQueue(album.getSongs());
                else
                    playlist.setQueue(library.getAllSongs());
                break;
            case Playlist.PLAYLIST_SOURCE_FAVORITE:
                playlist.setQueue(library.getFavoriteSongs());
                break;
            default:
                break;
        }
        playlist.setCurrentSource(source);
    }

    public String getCurrentAlbumQueue() {
        return playlist.getCurrentAlbumQueue();
    }

    public String getCurrentAlbumArtistQueue() {
        return playlist.getCurrentAlbumArtistQueue();
    }

    public int getCurrentSource() {
        return playlist.getCurrentSource();
    }

    //use for restore state
    public int getCurDuration() {
        return playlist.getCurDuration();
    }

    public void setCurDuration(int cur) {
        playlist.setCurDuration(cur);
    }

    public void setCurSong(String path) {
        boolean res = playlist.setNowSong(path);
        if (res)
            curSong.postValue(playlist.getNowSong());
        else
            state.postValue(Playlist.STATE_NONE);
    }

    public Song getCurrentSong() {
        return playlist.getNowSong();
    }

}
