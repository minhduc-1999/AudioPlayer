package com.example.myaudioplayer.view;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.example.myaudioplayer.R;
import com.example.myaudioplayer.audiomodel.Album;
import com.example.myaudioplayer.audiomodel.Library;
import com.example.myaudioplayer.audiomodel.Playlist;
import com.example.myaudioplayer.audiomodel.Song;
import com.example.myaudioplayer.audioservice.AudioService;
import com.example.myaudioplayer.notification.NotificationReceiver;
import com.example.myaudioplayer.viewmodel.LibraryViewModel;
import com.example.myaudioplayer.viewmodel.PlaylistViewModel;
import com.google.android.material.tabs.TabLayout;

import java.io.IOException;
import java.util.ArrayList;

import static com.example.myaudioplayer.audioservice.AudioService.BRC_SERVICE_FILTER;
import static com.example.myaudioplayer.helper.AnimationHelper.ImageAnimation;
import static com.example.myaudioplayer.helper.Helper.getEmbeddedArt;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    //action type
    public static final String PLAY_NEW_SONG = "PLAY_NEW_SONG";
    public static final String OPEN_PLAYING_BAR = "OPEN_PLAYING_BAR";
    //
    //Viewmodel
    private LibraryViewModel libraryViewModel;
    private PlaylistViewModel playlistViewModel;

    //Tablayout detail
    public static final int SONG_FRAGMENT = 100;
    public static final int FAVORITE_FRAGMENT = 200;
    public static final int ALBUM_FRAGMENT = 300;
    private int nowFragment;

    //for sorting
    private int sortOrder;

    //view component
    private RelativeLayout nowPlayingBar;
    private ProgressBar seekBar;
    private ImageView cover_art;
    private TextView song_name;
    private TextView artist_name;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    //Action bar view
    SearchView searchView;
    ImageView btn_option;
    ////////////////////////

    //Control bar
    private ImageView pre_btn;
    private ImageView next_btn;
    private ImageView play_pause_btn;
    //////////////

    //service + broadcast
    private AudioService audioService;
    private BroadcastReceiver broadcastReceiver;
    private boolean isBound = false;
    ///////////////
    //Fragment
    SongsFragment songsFragment;
    FavoriteFragment favoriteFragment;
    AlbumFragment albumFragment;
    ///
    private Handler handler = new Handler();

    public static final String MCHANNEL = "MCHANNEL";

    private Thread workerThread;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        androidx.appcompat.app.ActionBar actionbar = this.getSupportActionBar();

        actionbar.setDisplayHomeAsUpEnabled(false);
        actionbar.setDisplayShowHomeEnabled(false);
        actionbar.setDisplayShowTitleEnabled(false);
        actionbar.setDisplayUseLogoEnabled(false);
        actionbar.setHomeButtonEnabled(false);
        actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionbar.setDisplayShowCustomEnabled(true);
        actionbar.setCustomView(R.layout.custom_action_bar);
//        actionbar.setElevation(0);
        initView();
        initViewPager();

        sortOrder = getIntent().getIntExtra("sortOrder", Library.SORT_NONE);

        libraryViewModel = ViewModelProviders.of(this).get(LibraryViewModel.class);
        playlistViewModel = ViewModelProviders.of(this).get(PlaylistViewModel.class);
        registerLiveDataListenner();
        registerBroadcastReceiver();

        doStartAudioService();

        initEventListener();
        createNotificationChannel();
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (audioService != null && isBound) {
                    int mCurrentPosition = audioService.getCurrentDuration();
                    seekBar.setProgress(mCurrentPosition);
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    private void registerLiveDataListenner() {
        libraryViewModel.getmBinder().observe(this, new Observer<AudioService.AudioBinder>() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onChanged(AudioService.AudioBinder audioBinder) {
                if (audioBinder != null) {
                    audioService = audioBinder.getService();
                    isBound = true;
                    Song song = playlistViewModel.getCurrentSong();
                    if (song != null) {
                        try {
                            audioService.changeAudio(song);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        audioService.playPauseAudio();
                        int curDuration = playlistViewModel.getCurDuration();
                        audioService.seekTo(curDuration * 1000);
                        seekBar.setProgress(curDuration);
                    }
                } else {
                    audioService = null;
                    isBound = false;
                }
            }
        });
        playlistViewModel.getCurSong().observe(this, new Observer<Song>() {
            @Override
            public void onChanged(Song song) {
                if (song != null)
                    setMetaData(song);
            }
        });
        playlistViewModel.getState().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer s) {
                if (s == Playlist.STATE_PLAY) {
                    play_pause_btn.setImageResource(R.drawable.ic_round_pause_24);
                    nowPlayingBar.setVisibility(View.VISIBLE);
                } else if (s == Playlist.STATE_PAUSE) {
                    play_pause_btn.setImageResource(R.drawable.ic_round_play_arrow_24);
                    nowPlayingBar.setVisibility(View.VISIBLE);
                } else
                    nowPlayingBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        saveAppState();
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
            public void onReceive(Context context, final Intent intent) {
                workerThread = new Thread() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void run() {
                        super.run();
                        String action = intent.getAction();
                        if (action.equals(BRC_SERVICE_FILTER)) {
                            String info = intent.getStringExtra("info");
                            switch (info) {
                                case AudioService.BRC_AUDIO_CHANGE:
                                    playlistViewModel.getCurSong().postValue(audioService.getCurSong());
                                    break;
                                case AudioService.BRC_PLAYING_STATE_CHANGE:
                                    playlistViewModel.setState(audioService.getState());
                                    break;
                                case AudioService.BRC_AUDIO_COMPLETED:
                                    try {
                                        audioService.changeAudio(playlistViewModel.nextSong());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } catch (Exception e) {
                                        //makeToast(e.getMessage(), Toast.LENGTH_SHORT);
                                        //Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                default:
                                    break;
                            }
                        } else if (action.equals(NotificationReceiver.BRC_NOTIFY_FILTER)) {
                            String job = intent.getStringExtra("job");
                            switch (job) {
                                case NotificationReceiver.BRC_NOTIFY_NEXT:
                                    try {
                                        audioService.changeAudio(playlistViewModel.nextSong());
                                    } catch (Exception e) {
                                        //makeToast(e.getMessage(), Toast.LENGTH_SHORT);
                                        //Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                case NotificationReceiver.BRC_NOTIFY_PRE:
                                    try {
                                        audioService.changeAudio(playlistViewModel.preSong());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case NotificationReceiver.BRC_NOTIFY_PLAY:
                                    audioService.playPauseAudio();
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                };
                workerThread.start();
            }
        };
        IntentFilter intentFilter = new IntentFilter(AudioService.BRC_SERVICE_FILTER);
        intentFilter.addAction(NotificationReceiver.BRC_NOTIFY_FILTER);
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

        //init action bar view
        searchView = getSupportActionBar().getCustomView().findViewById(R.id.search_bar);
        searchView.setOnQueryTextListener(this);
        btn_option = getSupportActionBar().getCustomView().findViewById(R.id.btn_option);
        btn_option.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, v);
                popupMenu.getMenuInflater().inflate(R.menu.action_bar_option, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.by_date:
                                if (sortOrder != Library.SORT_BY_DATE) {
                                    sortOrder = Library.SORT_BY_DATE;
                                    libraryViewModel.sortLibrary(sortOrder);
                                } else
                                    makeToast("Library've been sorted by date", Toast.LENGTH_SHORT);
                                    //Toast.makeText(MainActivity.this, "Library've been sorted by date", Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.by_name:
                                if (sortOrder != Library.SORT_BY_NAME) {
                                    sortOrder = Library.SORT_BY_NAME;
                                    libraryViewModel.sortLibrary(sortOrder);
                                } else
                                    makeToast("Library've been sorted by name", Toast.LENGTH_SHORT);
                                    //Toast.makeText(MainActivity.this, "Library've been sorted by name", Toast.LENGTH_SHORT).show();
                                break;
                        }
                        return true;
                    }
                });
            }
        });
        //

    }

    private void initEventListener() {
        nowPlayingBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBound) {
                    Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
                    intent.setAction(OPEN_PLAYING_BAR);
                    Bundle bundle = new Bundle();
                    bundle.putInt("curDuration", audioService.getCurrentDuration());
                    bundle.putInt("totalDuration", Integer.parseInt(playlistViewModel.getCurrentSong().getDuration()) / 1000);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });
        pre_btn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                if (isBound) {
                    try {
                        audioService.changeAudio(playlistViewModel.preSong());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        next_btn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                if (isBound) {
                    try {
                        audioService.changeAudio(playlistViewModel.nextSong());
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        play_pause_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBound) {
                    audioService.playPauseAudio();
                }
            }
        });
    }

    private void setMetaData(final Song musicFiles) {
        int durationTotal = Integer.parseInt(musicFiles.getDuration()) / 1000;
        seekBar.setMax(durationTotal);
        int curDuration = 0;
        if (audioService != null)
            curDuration = audioService.getCurrentDuration();
        seekBar.setProgress(curDuration);
        song_name.setText(musicFiles.getTitle());
        artist_name.setText(musicFiles.getArtist());

//        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//        retriever.setDataSource(musicFiles.getPath());
//        byte[] art = retriever.getEmbeddedPicture();
        byte[] art = getEmbeddedArt(musicFiles.getPath());
        //Bitmap bitmap;
        if (art != null) {
            //bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            ImageAnimation(this, cover_art, art, true);
            //Glide.with(MainActivity.this).asBitmap().load(bitmap).into(cover_art);
        } else {
            //bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.background);
            ImageAnimation(this, cover_art, R.drawable.background, true);
            //Glide.with(MainActivity.this).asBitmap().load(R.drawable.music_default).into(cover_art);
        }

    }

    private void initViewPager() {
        viewPager = findViewById(R.id.viewpager);
        tabLayout = findViewById(R.id.tab_layout);

        songsFragment = new SongsFragment();
        albumFragment = new AlbumFragment();
        favoriteFragment = new FavoriteFragment();
        nowFragment = SONG_FRAGMENT;
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragments(songsFragment, "Songs");
        viewPagerAdapter.addFragments(albumFragment, "Albums");
        viewPagerAdapter.addFragments(favoriteFragment, "Favorite");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_round_music_note_24);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_round_library_music_24);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_round_favorite_24);

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tab.getIcon().setColorFilter(getResources().getColor(R.color.progress), PorterDuff.Mode.SRC_IN);
                int pos = tab.getPosition();
                switch (pos) {
                    case 0:
                        nowFragment = SONG_FRAGMENT;
                        break;
                    case 2:
                        nowFragment = FAVORITE_FRAGMENT;
                        break;
                    case 1:
                        nowFragment = ALBUM_FRAGMENT;
                        break;
                    default:
                        break;
                }
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

    public void makeToast(final String message, final int time){
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, time).show();
            }
        });
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(final String newText) {
        Thread filterThread = new Thread() {
            @Override
            public void run() {
                super.run();
                String userInput = newText.toLowerCase();
                ArrayList<Song> songs = new ArrayList<>();
                switch (nowFragment)
                {
                    case FAVORITE_FRAGMENT:
                        for (Song song : libraryViewModel.getFavoriteLists().getValue()) {
                            if (song.getTitle().toLowerCase().contains(userInput)) {
                                songs.add(song);
                            }
                        }
                        favoriteFragment.updateList(songs);
                        break;
                    case SONG_FRAGMENT:
                        for (Song song : libraryViewModel.getSongs().getValue()) {
                            if (song.getTitle().toLowerCase().contains(userInput)) {
                                songs.add(song);
                            }
                        }
                        songsFragment.updateList(songs);
                        break;
                    case ALBUM_FRAGMENT:
                        ArrayList<Album> albums = new ArrayList<>();
                        for (Album album : libraryViewModel.getAlbums().getValue()) {
                            if (album.getName().toLowerCase().contains(userInput)) {
                                albums.add(album);
                            }
                        }
                        albumFragment.updateList(albums);
                        break;
                    default:
                        break;
                }
            }
        };
        filterThread.start();
        //SongsFragment.musicAdapter.updateList(myFiles);
        return true;
    }

//    @Override
//    protected void onSaveInstanceState(@NonNull Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putBoolean("shuffle", playlistViewModel.getIsShuffle().getValue());
//        outState.putBoolean("repeat", playlistViewModel.getIsRepeat().getValue());
//        outState.putInt("currentDuration", audioService.getCurrentDuration());
//        outState.putString("curSong", audioService.getCurSong().getPath());
//        outState.putInt("source", playlistViewModel.getCurrentSource());
//        String albumName;
//        String artist;
//        albumName = playlistViewModel.getCurrentAlbumQueue();
//        if (!albumName.equals(""))
//            outState.putString("albumName", albumName);
//        artist = playlistViewModel.getCurrentAlbumArtistQueue();
//        if (!artist.equals(""))
//            outState.putString("artist", artist);
//    }
//
//    @Override
//    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        int curDuration;
//        String curSong;
//        String albumName;
//        String artist;
//        int playlistSource;
//        boolean shuffle = savedInstanceState.getBoolean("shuffle", false);
//        boolean repeat = savedInstanceState.getBoolean("repeat", false);
//        curDuration = savedInstanceState.getInt("currentDuration", -1);
//        curSong = savedInstanceState.getString("curSong", "");
//        playlistSource = savedInstanceState.getInt("source", -1);
//        albumName = savedInstanceState.getString("albumName", "");
//        artist = savedInstanceState.getString("artist", "");
//        playlistViewModel.setQueue(playlistSource, albumName, artist);
//        playlistViewModel.setState(Playlist.STATE_PAUSE);
//        playlistViewModel.setCurDuration(curDuration);
//        playlistViewModel.setCurPos(curSong);
//        playlistViewModel.setShuffle(shuffle);
//        playlistViewModel.setRepeat(repeat);
//    }

    public void saveAppState() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("MusicPlayerSetting", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("shuffle", playlistViewModel.getShuffle());
        editor.putBoolean("repeat", playlistViewModel.getRepeat());
        editor.putString("favorite", libraryViewModel.enCodeFavorite());
        editor.putInt("currentDuration", audioService.getCurrentDuration());
        if (audioService.getCurSong() != null)
            editor.putString("curSong", audioService.getCurSong().getPath());
        editor.putInt("source", playlistViewModel.getCurrentSource());
        editor.putInt("sortOrder", sortOrder);
        String albumName;
        String artist;
        albumName = playlistViewModel.getCurrentAlbumQueue();
        if (!albumName.equals(""))
            editor.putString("albumName", albumName);
        artist = playlistViewModel.getCurrentAlbumArtistQueue();
        if (!artist.equals(""))
            editor.putString("artist", artist);
        editor.apply();
        //Toast.makeText(this, "App Setting saved!", Toast.LENGTH_LONG).show();
    }
}