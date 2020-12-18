package com.example.myaudioplayer.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myaudioplayer.R;
import com.example.myaudioplayer.audiomodel.Album;
import com.example.myaudioplayer.audiomodel.Song;
import com.example.myaudioplayer.viewmodel.LibraryViewModel;

import java.util.ArrayList;

public class AlbumFragment extends Fragment {
    RecyclerView recyclerView;
    AlbumAdapter albumAdapter;
    private LibraryViewModel libraryViewModel;
    TextView text_albumNum;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_album, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);

        libraryViewModel = ViewModelProviders.of(getActivity()).get(LibraryViewModel.class);
        registerLiveDataListenner();

        recyclerView.setHasFixedSize(true);
        albumAdapter = new AlbumAdapter(getContext());
        recyclerView.setAdapter(albumAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        text_albumNum = view.findViewById(R.id.text_album_num);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateList(libraryViewModel.getAlbums().getValue());
    }

    private void registerLiveDataListenner() {
        libraryViewModel.getAlbums().observe(this, new Observer<ArrayList<Album>>() {
            @Override
            public void onChanged(ArrayList<Album> albums) {
                albumAdapter.updateList(albums);
                setTextAlbumNum(albumAdapter.getItemCount());
            }
        });
    }
    public void setTextAlbumNum(int sl)
    {
        text_albumNum.setText("Album (" + sl +")");
    }

    public void updateList(ArrayList<Album> albums) {
        albumAdapter.updateList(albums);
    }
}