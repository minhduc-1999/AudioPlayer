package com.example.myaudioplayer.audiomodel;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.Comparator;

public class Library {
    public static final int SORT_BY_NAME = 4000;
    public static final int SORT_BY_DATE = 5000;
    public static final int SORT_NONE = 3000;
    private static final Library _instance = new Library();

    public static Library getInstance() {
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

    public void setFavoriteSongs(String favorite) {
        if (favorite.equals(""))
            return;
        String[] list = favorite.split("[\t]");
        for (String item : list) {
            Song song = getSongByPath(item);
            if (song != null && !favoriteSongs.contains(song))
            {
                song.setFavorite(true);
                this.favoriteSongs.add(song);
            }

        }
    }

    public Album getAlbum(String name, String artist) {
        for (Album album : albums) {
            if (album.getName().equals(name) && (album.getArtist().equals(artist) || artist.equals("")))
                return album;
        }
        return null;
    }

    public void loadAllSong(Context context, int sortOrder) {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String order;
        switch (sortOrder) {
            case SORT_BY_NAME:
                order = MediaStore.MediaColumns.TITLE + " ASC";
                break;
            case SORT_BY_DATE:
                order = MediaStore.MediaColumns.DATE_ADDED + " ASC";
                break;
            default:
                order = null;
                break;
        }
        String[] projection = {
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATE_ADDED
        };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, order);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String albumName = cursor.getString(0);
                String title = cursor.getString(1);
                String duration = cursor.getString(2);
                String path = cursor.getString(3);
                String artist = cursor.getString(4);
                String id = cursor.getString(5);
                String date = cursor.getString(6);

                Song song = new Song(path, title, artist, albumName, duration, date, id);
                allSongs.add(song);
                Album album = getAlbum(albumName, artist);
                if (album == null) {
                    album = new Album(albumName, date, artist);
                    album.getSongs().add(song);
                    albums.add(album);
                } else {
                    album.getSongs().add(song);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    public boolean sortSongs(int order) {
        switch (order) {
            case SORT_BY_DATE:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    allSongs.sort(new Comparator<Song>() {
                        @Override
                        public int compare(Song o1, Song o2) {
                            int s1 = Integer.parseInt(o1.getDate());
                            int s2 = Integer.parseInt(o2.getDate());
                            if (s1 > s2)
                                return 1;
                            if (s1 < s2)
                                return -1;
                            return 0;
                        }
                    });
                }
                break;
            case SORT_BY_NAME:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    allSongs.sort(new Comparator<Song>() {
                        @Override
                        public int compare(Song o1, Song o2) {
                            return o1.getTitle().compareTo(o2.getTitle());
                        }
                    });
                }
                break;
            default:
                return false;
        }
        return true;
    }

    public String enCodeFavorite() {
        String res = "\t";
        for (Song song : favoriteSongs) {
            res += song.getPath() + "\t";
        }
        //res = res.substring(1, res.length() - 1);
        return res;
    }

    public Song getSongByPath(String path) {
        for (Song song : allSongs) {
            if (song.getPath().equals(path))
                return song;
        }
        return null;
    }

    public void changFavorite(Song song) {
        song.changeFavorite();
        if(song.isFavorite())
        {
            if(!favoriteSongs.contains(song))
            favoriteSongs.add(song);
        }
        else
        {
            if(favoriteSongs.contains(song))
                favoriteSongs.remove(song);
        }
    }
}
