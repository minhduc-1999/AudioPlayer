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
import android.widget.TextView;

import com.example.myaudioplayer.R;
import com.example.myaudioplayer.audiomodel.Playlist;
import com.example.myaudioplayer.audiomodel.Song;
import com.example.myaudioplayer.viewmodel.LibraryViewModel;

import java.util.ArrayList;

public class FavoriteFragment extends Fragment {

    private RecyclerView recyclerView;
    private MusicAdapter musicAdapter;
    private LibraryViewModel libraryViewModel;
    private TextView text_list;

    public FavoriteFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_favorite, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);

        libraryViewModel = ViewModelProviders.of(getActivity()).get(LibraryViewModel.class);
        registerLiveDataListenner();

        musicAdapter = new MusicAdapter(getContext(), libraryViewModel.getFavoriteLists().getValue(), Playlist.PLAYLIST_SOURCE_FAVORITE);
        recyclerView.setAdapter(musicAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        recyclerView.setHasFixedSize(true);

        text_list = view.findViewById(R.id.text_list);
        return view;
    }

    private void registerLiveDataListenner() {
        libraryViewModel.getFavoriteLists().observe(this, new Observer<ArrayList<Song>>() {
            @Override
            public void onChanged(ArrayList<Song> songs) {
                musicAdapter.updateList(songs);
                setTextFavoriteNum(musicAdapter.getItemCount());
            }
        });
    }

    public void setTextFavoriteNum(int sl)
    {
        text_list.setText("Favorite (" + sl +")");
    }
}