package com.example.myaudioplayer.helper;

import androidx.recyclerview.widget.DiffUtil;

import com.example.myaudioplayer.audiomodel.Album;

import java.util.ArrayList;

public class AlbumDiffCallBack extends DiffUtil.Callback {
    private ArrayList<Album> oldList;
    private ArrayList<Album> newList;

    public AlbumDiffCallBack(ArrayList<Album> oldList, ArrayList<Album> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        Album oldItem = oldList.get(oldItemPosition);
        Album newItem = newList.get(newItemPosition);
        return oldItem.getName().equals(newItem.getName()) && oldItem.getArtist().equals(newItem.getArtist());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        Album oldItem = oldList.get(oldItemPosition);
        Album newItem = newList.get(newItemPosition);
        return oldItem.getSongs().size() == newItem.getSongs().size();
    }
}
