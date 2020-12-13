package com.example.myaudioplayer.audiomodel;

import java.util.ArrayList;
import java.util.Random;

public class Playlist {
    public static final int PLAYLIST_SOURCE_SONG = 10000;
    public static final int PLAYLIST_SOURCE_ALBUM = 10001;
    public static final int PLAYLIST_SOURCE_NONE = 10002;
    public static final int STATE_PLAY = 20000;
    public static final int STATE_PAUSE = 20001;
    public static final int STATE_NONE = 20002;

    private static final Playlist _instance = new Playlist();
    public static Playlist getInstance()
    {
        return _instance;
    }
    private Playlist() {
        queue = new ArrayList<>();
        curPos = -1;
        isRepeat = false;
        isShuffle = false;
        state = STATE_NONE;
        currentSource = PLAYLIST_SOURCE_NONE;
    }

    private ArrayList<Song> queue;
    private int curPos;
    private boolean isShuffle, isRepeat;
    private int currentSource;
    private int state;

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getCurrentSource() {
        return currentSource;
    }

    public void setCurrentSource(int currentSource) {
        this.currentSource = currentSource;
    }



    public ArrayList<Song> getQueue() {
        return queue;
    }

    public void setQueue(ArrayList<Song> queue) {
        this.queue = queue;
    }

    public int getCurPos() {
        return curPos;
    }

    public void setCurPos(int curPos) {
        if(curPos >=0 && curPos < queue.size())
            this.curPos = curPos;
    }

    public boolean isShuffle() {
        return isShuffle;
    }

    public void setShuffle(boolean shuffle) {
        isShuffle = shuffle;
    }

    public boolean isRepeat() {
        return isRepeat;
    }

    public void setRepeat(boolean repeat) {
        isRepeat = repeat;
    }

    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i + 1);
    }

    public Song nextSong()
    {
        int position = -1;
        if (isShuffle && !isRepeat) {
            position = getRandom(queue.size() - 1);
        } else if (!isShuffle && !isRepeat) {
            position = (curPos + 1) % queue.size();
        }
        if (position != -1)
            curPos = position;
        if(curPos >=0 && curPos < queue.size())
            return queue.get(curPos);
        return null;
    }
    public Song preSong()
    {
        int position = -1;
        if (isShuffle && !isRepeat) {
            position = getRandom(queue.size() - 1);
        } else if (!isShuffle && !isRepeat) {
            position = (curPos - 1 + queue.size()) % queue.size();
        }
        if (position != -1)
            curPos = position;
        if(curPos >=0 && curPos < queue.size())
            return queue.get(curPos);
        return null;
    }
    public Song getCurSong()
    {
        return queue.get(curPos);
    }
}
