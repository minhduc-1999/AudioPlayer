package com.example.myaudioplayer.view;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.myaudioplayer.R;
import com.example.myaudioplayer.audiomodel.Playlist;
import com.example.myaudioplayer.audiomodel.Song;
import com.example.myaudioplayer.audioservice.AudioService;
import com.example.myaudioplayer.viewmodel.LibraryViewModel;
import com.example.myaudioplayer.viewmodel.PlaylistViewModel;
import com.google.android.material.tabs.TabLayout;


import java.util.ArrayList;

import static com.example.myaudioplayer.audioservice.AudioService.NEXTBUTTON;
import static com.example.myaudioplayer.audioservice.AudioService.PLAYBUTTON;
import static com.example.myaudioplayer.audioservice.AudioService.PREBUTTON;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    public static final String PLAY_NEW_SONG = "PLAY_NEW_SONG";
    public static final String OPEN_PLAYING_BAR = "OPEN_PLAYING_BAR";
    private LibraryViewModel libraryViewModel;
    private PlaylistViewModel playlistViewModel;

    //public static final int REQUEST_CODE = 1;

    private RelativeLayout nowPlayingBar;
    private ProgressBar seekBar;
    private ImageView cover_art;
    private TextView song_name;
    private TextView artist_name;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    private boolean isBound = false;

    private ImageView pre_btn;
    private ImageView next_btn;
    private ImageView play_pause_btn;

    private AudioService audioService;
    private BroadcastReceiver broadcastReceiver;

    private Handler handler = new Handler();

    public static final String MCHANNEL = "MCHANNEL";

    int curDuration;
    String curSong;
    String albumName;
    String artist;
    int playlistSource;
    boolean hasRestoreState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        libraryViewModel = ViewModelProviders.of(this).get(LibraryViewModel.class);
        playlistViewModel = ViewModelProviders.of(this).get(PlaylistViewModel.class);

        registerLiveDataListenner();
        //permission();
        registerBroadcastReceiver();
        doStartAudioService();
        initView();
        initViewPager();
        initEventListener();
        createNotificationChannel();
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (audioService != null && isBound) {
                    int mCurrentPosition = audioService.getCurrentDuration() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    private void registerLiveDataListenner() {
        libraryViewModel.getmBinder().observe(this, new Observer<AudioService.AudioBinder>() {
            @Override
            public void onChanged(AudioService.AudioBinder audioBinder) {
                if (audioBinder != null) {
                    audioService = audioBinder.getService();
                    isBound = true;
//                    if(hasRestoreState)
//                    {
//                        if (!curSong.equals("") && curDuration != -1) {
//                            playlistViewModel.setQueue(playlistSource, albumName, artist);
//                            Song song = playlistViewModel.getSongByPath(curSong);
//                            if(song!= null) {
//                                playlistViewModel.getCurSong().postValue(song);
//                                playlistViewModel.setState(Playlist.STATE_PAUSE);
//                                audioService.changeAudio(song);
//                            }
//                        }
//                    }
                } else {
                    audioService = null;
                    isBound = false;
                }
            }
        });
        playlistViewModel.getCurSong().observe(this, new Observer<Song>() {
            @Override
            public void onChanged(Song song) {
                setMetaData(song);
            }
        });
        playlistViewModel.getState().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer s) {
                if (s.equals(Playlist.STATE_PLAY)) {
                    play_pause_btn.setImageResource(R.drawable.ic_round_pause_24);
                    nowPlayingBar.setVisibility(View.VISIBLE);
                } else if (s.equals(Playlist.STATE_PAUSE)) {
                    play_pause_btn.setImageResource(R.drawable.ic_round_play_arrow_24);
                    nowPlayingBar.setVisibility(View.VISIBLE);
                } else
                    nowPlayingBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        doStopAudioService();
        super.onDestroy();
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mNotificationChannel = new NotificationChannel(MCHANNEL, "Music Channel", NotificationManager.IMPORTANCE_HIGH);
            mNotificationChannel.setDescription("Music Channel Description");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(mNotificationChannel);
        }
    }

    private void doStartAudioService() {
        Intent serviceIntent = new Intent(this, AudioService.class);
        startService(serviceIntent);
        bindService();
    }

    private void doStopAudioService() {
        unbindService(libraryViewModel.getServiceConnection());
        Intent serviceIntent = new Intent(this, AudioService.class);
        stopService(serviceIntent);
    }

    private void bindService() {
        Intent serviceIntent = new Intent(this, AudioService.class);
        bindService(serviceIntent, libraryViewModel.getServiceConnection(), Context.BIND_AUTO_CREATE);
    }

    private void registerBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String info = intent.getStringExtra("info");
                switch (info) {
                    case AudioService.BRC_AUDIO_CHANGE:
                        playlistViewModel.getCurSong().postValue(audioService.getCurSong());
                        break;
                    case AudioService.BRC_PLAYING_STATE_CHANGE:
                        playlistViewModel.setState(audioService.getState());
                        break;
                    case AudioService.BRC_AUDIO_COMPLETED:
                        audioService.changeAudio(playlistViewModel.nextSong());
                    default:
                }

            }
        };
        IntentFilter intentFilter = new IntentFilter(AudioService.BRC_SERVICE_FILTER);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
    }

    private void initView() {
        nowPlayingBar = findViewById(R.id.now_playing_collapse);
        this.song_name = findViewById(R.id.song_name);
        this.artist_name = findViewById(R.id.song_artist);
        this.cover_art = findViewById(R.id.cover_art);
        this.next_btn = findViewById(R.id.id_next);
        this.pre_btn = findViewById(R.id.id_pre);
        this.play_pause_btn = findViewById(R.id.play_pause);
        this.seekBar = findViewById(R.id.seekBar);
    }

    private void initEventListener() {
        nowPlayingBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBound) {
                    Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
                    int state = playlistViewModel.getState().getValue();
                    intent.setAction(OPEN_PLAYING_BAR);
                    intent.putExtra("curDuration", audioService.getCurrentDuration() / 1000);
                    intent.putExtra("totalDuration", Integer.parseInt(audioService.getCurSong().getDuration()) / 1000);
                    startActivity(intent);
                }
            }
        });
        pre_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBound) {
                    audioService.changeAudio(playlistViewModel.preSong());
                }
            }
        });
        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBound) {
                    audioService.changeAudio(playlistViewModel.nextSong());
                }
            }
        });
        play_pause_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBound) {
                    if (audioService.getState() == Playlist.STATE_PLAY) {
                        audioService.playPauseAudio();
                    } else {
                        audioService.playPauseAudio();
                    }
                }
            }
        });
    }

    private void setMetaData(Song musicFiles) {
        seekBar.setProgress(0);
        int durationTotal = Integer.parseInt(musicFiles.getDuration()) / 1000;
        song_name.setText(musicFiles.getTitle());
        artist_name.setText(musicFiles.getArtist());
        seekBar.setMax(durationTotal);

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(musicFiles.getPath());
        byte[] art = retriever.getEmbeddedPicture();
        Bitmap bitmap;
        if (art != null) {
            bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            Glide.with(this).asBitmap().load(bitmap).into(cover_art);
        } else {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.music_default);
            Glide.with(this).asBitmap().load(R.drawable.music_default).into(cover_art);
        }
    }

//    private void permission() {
//        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
//        } else {
//            libraryViewModel.loadLocalSong();
//            initViewPager();
//        }
//    }

    private void initViewPager() {
        viewPager = findViewById(R.id.viewpager);
        tabLayout = findViewById(R.id.tab_layout);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragments(new SongsFragment(), "Songs");
        viewPagerAdapter.addFragments(new AlbumFragment(), "Albums");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_round_music_note_24);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_round_library_music_24);
        //tabLayout.getTabAt(0).setText(viewPagerAdapter.getPageTitle(0));
        //tabLayout.getTabAt(1).setText(viewPagerAdapter.getPageTitle(1));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tab.getIcon().setColorFilter(getResources().getColor(R.color.progress), PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.getIcon().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    public static class ViewPagerAdapter extends FragmentPagerAdapter {
        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        public ViewPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        void addFragments(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        MenuItem menuItem = menu.findItem(R.id.search_option);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String userInput = newText.toLowerCase();
        ArrayList<Song> myFiles = new ArrayList<>();
        for (Song song : libraryViewModel.getSongs().getValue()) {
            if (song.getTitle().toLowerCase().contains(userInput)) {
                myFiles.add(song);
            }
        }
        SongsFragment.musicAdapter.updateList(myFiles);
        return true;
    }

    public class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                switch ((intent.getAction())) {
                    case PLAYBUTTON:
                        audioService.playPauseAudio();
                        break;
                    case PREBUTTON:
                        audioService.changeAudio(playlistViewModel.preSong());
                        break;
                    case NEXTBUTTON:
                        audioService.changeAudio(playlistViewModel.nextSong());
                        break;
                }
            }

        }
    }

//    @Override
//    protected void onSaveInstanceState(@NonNull Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putInt("currentDuration", audioService.getCurrentDuration());
//        outState.putString("curSong", audioService.getCurSong().getPath());
//        outState.putInt("source", playlistViewModel.getCurrentSource());
//        albumName = playlistViewModel.getCurrentAlbumQueue();
//        if (!albumName.equals(""))
//            outState.putString("albumName", albumName);
//        artist = playlistViewModel.getCurrentAlbumArtistQueue();
//        if (!artist.equals(""))
//            outState.putString("albumName", artist);
//    }
//
//    @Override
//    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        curDuration = savedInstanceState.getInt("currentDuration", -1);
//        curSong = savedInstanceState.getString("curSong", "");
//        playlistSource = savedInstanceState.getInt("source", -1);
//        albumName = savedInstanceState.getString("albumName", "");
//        artist = savedInstanceState.getString("artist", "");
//        hasRestoreState = true;
//    }
}