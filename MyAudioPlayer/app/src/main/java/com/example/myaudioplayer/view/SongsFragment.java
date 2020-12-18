package com.example.myaudioplayer.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myaudioplayer.R;
import com.example.myaudioplayer.audiointerface.OnFavoriteChangeListener;
import com.example.myaudioplayer.audiomodel.Playlist;
import com.example.myaudioplayer.audiomodel.Song;
import com.example.myaudioplayer.audioservice.AudioService;
import com.example.myaudioplayer.notification.NotificationReceiver;
import com.example.myaudioplayer.viewmodel.LibraryViewModel;

import java.util.ArrayList;

import static com.example.myaudioplayer.audiomodel.Playlist.PLAYLIST_SOURCE_SONG;

public class SongsFragment extends Fragment implements OnFavoriteChangeListener {

    RecyclerView recyclerView;
    MusicAdapter musicAdapter;
    private LibraryViewModel libraryViewModel;
    TextView text_list;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_songs, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);

        libraryViewModel = ViewModelProviders.of(getActivity()).get(LibraryViewModel.class);
        registerLiveDataListenner();

        musicAdapter = new MusicAdapter(getContext(), libraryViewModel.getSongs().getValue(), PLAYLIST_SOURCE_SONG);
        musicAdapter.addListener(this);
        recyclerView.setAdapter(musicAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        recyclerView.setHasFixedSize(true);

        text_list = view.findViewById(R.id.text_list);
        return view;
    }

    private void registerLiveDataListenner() {
        libraryViewModel.getSongs().observe(this, new Observer<ArrayList<Song>>() {
            @Override
            public void onChanged(ArrayList<Song> songs) {
                musicAdapter.updateList(songs);
                setTextSongNum(musicAdapter.getItemCount());
            }
        });
    }

    public void setTextSongNum(int sl)
    {
        text_list.setText("Playlist (" + sl +")");
    }

    @Override
    public void OnFavoriteChange(Song song) {
        libraryViewModel.changeFavorite(song);
    }
}