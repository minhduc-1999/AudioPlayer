package com.example.myaudioplayer.view;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myaudioplayer.R;
import com.example.myaudioplayer.audiomodel.Song;
import com.example.myaudioplayer.viewmodel.LibraryViewModel;

import java.util.ArrayList;

public class SongsFragment extends Fragment {

    RecyclerView recyclerView;
    static MusicAdapter musicAdapter;
    private LibraryViewModel libraryViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_songs, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);

        libraryViewModel = ViewModelProviders.of(getActivity()).get(LibraryViewModel.class);
        registerLiveDataListenner();

        musicAdapter = new MusicAdapter(getContext(), libraryViewModel.getSongs().getValue());
        recyclerView.setAdapter(musicAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        recyclerView.setHasFixedSize(true);
        return view;
    }

    private void registerLiveDataListenner() {
        libraryViewModel.getSongs().observe(this, new Observer<ArrayList<Song>>() {
            @Override
            public void onChanged(ArrayList<Song> songs) {
                musicAdapter.updateList(songs);
            }
        });
    }
}