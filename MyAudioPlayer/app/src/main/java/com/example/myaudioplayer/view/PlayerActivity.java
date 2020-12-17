package com.example.myaudioplayer.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.myaudioplayer.R;
import com.example.myaudioplayer.audiomodel.Playlist;
import com.example.myaudioplayer.audiomodel.Song;
import com.example.myaudioplayer.audioservice.AudioService;
import com.example.myaudioplayer.viewmodel.PlaylistViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;

import static com.example.myaudioplayer.helper.AnimationHelper.ImageAnimation;


public class PlayerActivity extends AppCompatActivity {
    private PlaylistViewModel playlistViewModel;
    public static AudioService audioService;
    private boolean isBound = false;
    private BroadcastReceiver broadcastReceiver;

    //Views
    TextView song_name, artist_name, duration_played, duration_total;
    ImageView cover_art, nextBtn, preBtn, backBtn, shuffleBtn, repeatBtn, art_background;
    FloatingActionButton playPauseBtn;
    SeekBar seekBar;
    //
    private int source;
    private int position;
    private String action;

    private Handler handler = new Handler();
    private Thread playThread, preThread, nextThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        playlistViewModel = ViewModelProviders.of(this).get(PlaylistViewModel.class);
        registerLiveDataListenner();
        registerBroadcastReceiver();
        initView();

        getIntentMethod();
        bindAudioService();
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && audioService != null) {
                    audioService.seekTo(progress * 1000);
                    duration_played.setText(formattedTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (audioService != null) {
                    int mCurrentPosition = audioService.getCurrentDuration();
                    seekBar.setProgress(mCurrentPosition);
                    duration_played.setText(formattedTime(mCurrentPosition));
                }
                handler.postDelayed(this, 1000);
            }
        });
        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBound) {
                    playlistViewModel.setShuffle(!playlistViewModel.getIsShuffle().getValue());
                }
            }
        });
        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBound) {
                    playlistViewModel.setRepeat(!playlistViewModel.getIsRepeat().getValue());
                }
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void registerBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onReceive(Context context, Intent intent) {
                String info = intent.getStringExtra("info");
                switch (info) {
                    case AudioService.BRC_AUDIO_CHANGE:
                        playlistViewModel.getCurSong().setValue(audioService.getCurSong());
                        break;
                    case AudioService.BRC_PLAYING_STATE_CHANGE:
                        playlistViewModel.setState(audioService.getState());
                        break;
                    case AudioService.BRC_AUDIO_COMPLETED:
                        try {
                            audioService.changeAudio(playlistViewModel.nextSong());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    default:
                }

            }
        };
        IntentFilter intentFilter = new IntentFilter(AudioService.BRC_SERVICE_FILTER);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
    }

    private void registerLiveDataListenner() {
        playlistViewModel.getmBinder().observe(this, new Observer<AudioService.AudioBinder>() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onChanged(AudioService.AudioBinder audioBinder) {
                if (audioBinder != null) {
                    audioService = audioBinder.getService();
                    isBound = true;
                    if (action.equals(MainActivity.PLAY_NEW_SONG)) {
                        Song temp = playlistViewModel.play(position);
                        if (temp != null) {
                            try {
                                audioService.changeAudio(temp);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        setMetaData(audioService.getCurSong());
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
        playlistViewModel.getIsRepeat().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    repeatBtn.setImageResource(R.drawable.ic_round_repeat_24_on);
                } else {
                    repeatBtn.setImageResource(R.drawable.ic_round_repeat_24_off);
                }
            }
        });
        playlistViewModel.getIsShuffle().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    shuffleBtn.setImageResource(R.drawable.ic_round_shuffle_24_on);
                } else {
                    shuffleBtn.setImageResource(R.drawable.ic_round_shuffle_24_off);
                }
            }
        });
        playlistViewModel.getState().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer s) {
                if (s.equals(Playlist.STATE_PLAY))
                    playPauseBtn.setImageResource(R.drawable.ic_round_pause_24);
                else
                    playPauseBtn.setImageResource(R.drawable.ic_round_play_arrow_24);
            }
        });
    }

    private void bindAudioService() {
        Intent serviceIntent = new Intent(this, AudioService.class);
        bindService(serviceIntent, playlistViewModel.getServiceConnection(), Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        if (playlistViewModel.getmBinder().getValue() != null) {
            unbindService(playlistViewModel.getServiceConnection());
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        playThreadBtn();
        nextThreadBtn();
        preThreadBtn();
        super.onResume();
    }

    private void nextThreadBtn() {
        nextThread = new Thread() {
            @Override
            public void run() {
                super.run();
                nextBtn.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onClick(View v) {
                        try {
                            nextBtnClick();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        nextThread.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void nextBtnClick() throws IOException {
        if (isBound) {
            audioService.changeAudio(playlistViewModel.nextSong());
        }

    }

    private void preThreadBtn() {
        preThread = new Thread() {
            @Override
            public void run() {
                super.run();
                preBtn.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onClick(View v) {
                        try {
                            preBtnClick();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        preThread.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void preBtnClick() throws IOException {
        if (isBound) {
            audioService.changeAudio(playlistViewModel.preSong());
        }
    }

    private void playThreadBtn() {
        playThread = new Thread() {
            @Override
            public void run() {
                super.run();
                playPauseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playPauseBtnClick();
                    }
                });
            }
        };
        playThread.start();
    }

    private void playPauseBtnClick() {
        if (isBound) {
            audioService.playPauseAudio();
        }
    }

    private String formattedTime(int mCurrentPosition) {
        String totalOut = "";
        String totalNew = "";
        String seconds = String.valueOf(mCurrentPosition % 60);
        String minutes = String.valueOf(mCurrentPosition / 60);
        totalOut = minutes + ":" + seconds;
        totalNew = minutes + ":0" + seconds;
        if (seconds.length() == 1) {
            return totalNew;
        }
        return totalOut;
    }

    private void getIntentMethod() {
        action = getIntent().getAction();
        if (action.equals(MainActivity.PLAY_NEW_SONG)) {
            Bundle bundle = getIntent().getExtras();
            position = bundle.getInt("position", -1);
            source = bundle.getInt("source", -1);
            String albumName = bundle.getString("albumName", "");
            String artist = bundle.getString("artist", "");
            playlistViewModel.setQueue(source, albumName, artist);
        } else {
            Bundle bundle = getIntent().getExtras();
            int totalDuration = bundle.getInt("totalDuration", 300);
            seekBar.setMax(totalDuration);
            int curDuration = bundle.getInt("curDuration", 0);
            seekBar.setProgress(curDuration);
            duration_played.setText(formattedTime(curDuration));
            duration_total.setText(formattedTime(totalDuration));
        }
    }

    private void initView() {
        song_name = findViewById(R.id.song_name);
        artist_name = findViewById(R.id.song_artist);
        duration_played = findViewById(R.id.duration_played);
        duration_total = findViewById(R.id.duration_total);
        cover_art = findViewById(R.id.cover_art);
        nextBtn = findViewById(R.id.id_next);
        preBtn = findViewById(R.id.id_pre);
        backBtn = findViewById(R.id.back_btn);
        shuffleBtn = findViewById(R.id.id_shuffle);
        repeatBtn = findViewById(R.id.id_repeat);
        playPauseBtn = findViewById(R.id.play_pause);
        seekBar = findViewById(R.id.seekBar);

        //background
        art_background = findViewById(R.id.art_background);
        //
    }


    private void setMetaData(Song musicFiles) {
        int durationTotal = Integer.parseInt(musicFiles.getDuration()) / 1000;
        duration_total.setText(formattedTime(durationTotal));
        seekBar.setMax(durationTotal);
        if (audioService != null) {
            int curDuration = audioService.getCurrentDuration();
            seekBar.setProgress(curDuration);
            duration_played.setText(formattedTime(curDuration));
        } else {
            duration_played.setText(formattedTime(0));
        }

        song_name.setText(musicFiles.getTitle());
        artist_name.setText(musicFiles.getArtist());


        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(musicFiles.getPath());
        byte[] art = retriever.getEmbeddedPicture();
        //Bitmap bitmap;
        if (art != null) {
            //bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            //Glide.with(this).load(bitmap).apply(RequestOptions.bitmapTransform(new CropCircleTranformation())).into(cover_art);
            ImageAnimation(this, cover_art, art, true);
            ImageAnimation(this, art_background, art, false);
        } else {
            //bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.background);
            //Glide.with(this).load(bitmap).apply(RequestOptions.bitmapTransform(new CropCircleTranformation())).into(cover_art);
            ImageAnimation(this, cover_art,  R.drawable.background, true);
            ImageAnimation(this, art_background,  R.drawable.background, false);
        }
    }
}