package com.example.myaudioplayer.audiomodel;
import androidx.lifecycle.MutableLiveData;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;

public class Album {
    private String name;
    private String date;
    private ArrayList<Song> songs;
    private String artist;

    public Album(String name, String date, String artist) {
        this.name = name;
        this.date = date;
        this.artist = artist;
        songs = new ArrayList<>();
    }

    public String getArtist() {
        return artist;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String yearRelease) {
        this.date = yearRelease;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Song> getSongs() {
        return songs;
    }

    public void setSongs(ArrayList<Song> songs) {
        this.songs = songs;
    }
}
