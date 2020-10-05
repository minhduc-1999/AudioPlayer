package com.example.myaudioplayer;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.MutableLiveData;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.myaudioplayer.audiomodel.MusicFiles;
import com.example.myaudioplayer.audioservice.AudioService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    public static final int REQUEST_CODE = 1;
    static ArrayList<MusicFiles> playlists;
    static ArrayList<MusicFiles> albums = new ArrayList<>();

    private RelativeLayout nowPlayingCollapse;
    private ProgressBar seekBar;
    private ImageView cover_art;
    private TextView song_name;
    private TextView artist_name;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    private boolean isBound = false;
    private boolean isNowPlayingTabShow = false;

    private ImageView pre_btn;
    private ImageView next_btn;
    private ImageView play_pause_btn;

    private AudioService audioService;
    private BroadcastReceiver broadcastReceiver;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permission();
        registerBroadcastReceiver();
        doStartAudioService();
        initView();
        initEventListener();

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

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        doStopAudioService();
        super.onDestroy();
    }

    private void doStartAudioService() {
        Intent serviceIntent = new Intent(this, AudioService.class);
        startService(serviceIntent);
        bindService();
    }

    private void doStopAudioService() {
        unbindService(serviceConnection);
        Intent serviceIntent = new Intent(this, AudioService.class);
        stopService(serviceIntent);
    }

    private void bindService() {
        Intent serviceIntent = new Intent(this, AudioService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioService.AudioBinder binder = (AudioService.AudioBinder) service;
            audioService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            audioService = null;
            isBound = false;
        }
    };

    private void registerBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String info = intent.getStringExtra("info");
                switch (info) {
                    case AudioService.BRC_AUDIO_CHANGE:
                        setMetaData(audioService.getCurSong());
                        //play_pause_btn.setImageResource(R.drawable.ic_round_pause_24);
                        break;
                    case AudioService.BRC_PLAYING_STATE_CHANGE:
                        String state = audioService.getState();
                        if (state.equals(AudioService.STATE_PLAY)) {
                            play_pause_btn.setImageResource(R.drawable.ic_round_pause_24);
                            if (isNowPlayingTabShow == false) {
                                nowPlayingCollapse.setVisibility(View.VISIBLE);
                                isNowPlayingTabShow = true;
                            }
                        } else if (state.equals( AudioService.STATE_PAUSE)) {
                            play_pause_btn.setImageResource(R.drawable.ic_round_play_arrow_24);
                            if (isNowPlayingTabShow == false)
                                if (isNowPlayingTabShow == false) {
                                    nowPlayingCollapse.setVisibility(View.VISIBLE);
                                    isNowPlayingTabShow = true;
                                }
                        } else {
                            if (isNowPlayingTabShow == true) {
                                nowPlayingCollapse.setVisibility(View.GONE);
                                isNowPlayingTabShow = false;
                            }
                        }
                        break;

                    default:
                }

            }
        };
        IntentFilter intentFilter = new IntentFilter(AudioService.BRC_SERVICE_FILTER);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
    }

    private void initView() {
        nowPlayingCollapse = findViewById(R.id.now_playing_collapse);
        this.song_name = findViewById(R.id.song_name);
        this.artist_name = findViewById(R.id.song_artist);
        this.cover_art = findViewById(R.id.cover_art);
        this.next_btn = findViewById(R.id.id_next);
        this.pre_btn = findViewById(R.id.id_pre);
        this.play_pause_btn = findViewById(R.id.play_pause);
        this.seekBar = findViewById(R.id.seekBar);
    }

    private void initEventListener() {
        nowPlayingCollapse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBound) {
                    Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
                    if (audioService != null) {
                        String state = audioService.getState();
                        intent.putExtra("sender", AudioService.PLAYLIST_SOURCE_NONE);
                        intent.putExtra("state", state);

                    } else {
                        intent.putExtra("createService", true);
                    }
                    startActivity(intent);
                }
            }
        });
        pre_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBound) {
                    audioService.preSong();
                }
            }
        });
        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBound) {
                    audioService.nextSong();
                }
            }
        });
        play_pause_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBound) {
                    if (audioService.getState() == AudioService.STATE_PLAY) {
                        audioService.playPauseAudio();
                        //play_pause_btn.setImageResource(R.drawable.ic_round_play_arrow_24);
                    } else {
                        audioService.playPauseAudio();
                        //play_pause_btn.setImageResource(R.drawable.ic_round_pause_24);
                    }
                }
            }
        });
    }

    private void setMetaData(MusicFiles musicFiles) {
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

    private void permission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        } else {
            playlists = getAllAudio(this);
            initViewPager();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            playlists = getAllAudio(this);
            initViewPager();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
    }

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
        tabLayout.getTabAt(0).setText(viewPagerAdapter.getPageTitle(0));
        tabLayout.getTabAt(1).setText(viewPagerAdapter.getPageTitle(1));
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

    public static ArrayList<MusicFiles> getAllAudio(Context context) {
        ArrayList<String> duplicate = new ArrayList<>();
        ArrayList<MusicFiles> tempAudioList = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media._ID
        };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String album = cursor.getString(0);
                String title = cursor.getString(1);
                String duration = cursor.getString(2);
                String path = cursor.getString(3);
                String artist = cursor.getString(4);
                String id = cursor.getString(5);
                MusicFiles musicFiles = new MusicFiles(path, title, artist, album, duration, id);

                tempAudioList.add(musicFiles);
                if (!(duplicate.contains((album)))) {
                    albums.add(musicFiles);
                    duplicate.add(album);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        return tempAudioList;
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
        ArrayList<MusicFiles> myFiles = new ArrayList<>();
        for (MusicFiles song : playlists) {
            if (song.getTitle().toLowerCase().contains(userInput)) {
                myFiles.add(song);
            }
        }
        SongsFragment.musicAdapter.updateList(myFiles);
        return true;
    }
}