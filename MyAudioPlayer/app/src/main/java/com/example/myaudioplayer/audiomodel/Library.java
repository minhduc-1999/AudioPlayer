package com.example.myaudioplayer.audiomodel;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;

public class Library {

    private static final Library _instance = new Library();
    public static Library getInstance()
    {
        return _instance;
    }
    private ArrayList<Song> allSongs;
    private ArrayList<Album> albums;
    private ArrayList<Song> favoriteSongs;

    private Library() {
        albums = new ArrayList<>();
        allSongs = new ArrayList();
        favoriteSongs = new ArrayList<>();
    }

    public ArrayList<Song> getAllSongs() {
        return allSongs;
    }

    public void setAllSongs(ArrayList<Song> allSongs) {
        this.allSongs = allSongs;
    }

    public ArrayList<Album> getAlbums() {
        return albums;
    }

    public void setAlbums(ArrayList<Album> albums) {
        this.albums = albums;
    }

    public ArrayList<Song> getFavoriteSongs() {
        return favoriteSongs;
    }

    public void setFavoriteSongs(ArrayList<Song> favoriteSongs) {
        this.favoriteSongs = favoriteSongs;
    }

    public Album getAlbum(String name, String artist)
    {
        for (Album album: albums) {
            if(album.getName().equals(name) && (album.getArtist().equals(artist) || artist.equals("")))
                return album;
        }
        return null;
    }

    public void loadAllSong(Context context) {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.YEAR
        };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String albumName = cursor.getString(0);
                String title = cursor.getString(1);
                String duration = cursor.getString(2);
                String path = cursor.getString(3);
                String artist = cursor.getString(4);
                String id = cursor.getString(5);
                String year = cursor.getString(6);

                Song song = new Song(path, title, artist, albumName, duration,year, id);
                allSongs.add(song);
                Album album = getAlbum(albumName, artist);
                if (album == null) {
                    album = new Album(albumName , year, artist);
                    album.getSongs().add(song);
                    albums.add(album);
                }
                else
                {
                    album.getSongs().add(song);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
    }
}
