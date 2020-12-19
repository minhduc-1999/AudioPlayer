package com.example.myaudioplayer.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myaudioplayer.R;
import com.example.myaudioplayer.audiointerface.OnFavoriteChangeListener;
import com.example.myaudioplayer.audiomodel.Song;
import com.example.myaudioplayer.viewmodel.LibraryViewModel;

import java.util.ArrayList;

import static com.example.myaudioplayer.audiomodel.Playlist.PLAYLIST_SOURCE_SONG;

public class SongsFragment extends Fragment implements OnFavoriteChangeListener {

    private RecyclerView recyclerView;
    private MusicAdapter musicAdapter;
    private LibraryViewModel libraryViewModel;
    private TextView text_list;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_songs, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        libraryViewModel = ViewModelProviders.of(getActivity()).get(LibraryViewModel.class);
        registerLiveDataListenner();

        text_list = view.findViewById(R.id.text_list);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        musicAdapter = new MusicAdapter(getContext(), PLAYLIST_SOURCE_SONG);
        musicAdapter.addListener(this);
        recyclerView.setAdapter(musicAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));

        updateList(libraryViewModel.getSongs().getValue());
        setTextSongNum(musicAdapter.getItemCount());

    }

    private void registerLiveDataListenner() {
        libraryViewModel.getSongs().observe(this, new Observer<ArrayList<Song>>() {
            @Override
            public void onChanged(ArrayList<Song> songs) {
                if(musicAdapter != null){
                    musicAdapter.updateList(songs);
                    setTextSongNum(musicAdapter.getItemCount());
                }
            }
        });
    }

    public void setTextSongNum(int sl) {
        text_list.setText("Playlists (" + sl + ")");
    }

    @Override
    public void OnFavoriteChange(Song song) {
        libraryViewModel.changeFavorite(song);
    }

    public void updateList(ArrayList<Song> list) {
        musicAdapter.updateList(list);
    }

}