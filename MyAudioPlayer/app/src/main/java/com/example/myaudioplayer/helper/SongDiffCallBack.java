package com.example.myaudioplayer.helper;

import androidx.recyclerview.widget.DiffUtil;

import com.example.myaudioplayer.audiomodel.Song;

import java.util.ArrayList;

public class SongDiffCallBack extends DiffUtil.Callback {
    private ArrayList<Song> oldList;
    private ArrayList<Song> newList;

    public SongDiffCallBack(ArrayList<Song> oldList, ArrayList<Song> newList) {
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
        return oldList.get(oldItemPosition).getId() == newList.get(newItemPosition).getId();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        Song oldItem = oldList.get(oldItemPosition);
        Song newItem = newList.get(newItemPosition);
        return oldItem.isFavorite() == newItem.isFavorite();
    }
}
