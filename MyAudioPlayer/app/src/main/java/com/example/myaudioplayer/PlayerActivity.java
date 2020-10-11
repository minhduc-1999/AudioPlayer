package com.example.myaudioplayer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.example.myaudioplayer.audiomodel.MusicFiles;
import com.example.myaudioplayer.audioservice.AudioService;
import com.example.myaudioplayer.notification.NotificationReceiver;
import com.example.myaudioplayer.viewmodel.PlayerActivityViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import static com.example.myaudioplayer.AlbumDetailsAdapter.albumFiles;
import static com.example.myaudioplayer.MainActivity.ClOSEBUTTON;
import static com.example.myaudioplayer.MainActivity.MCHANNEL;
import static com.example.myaudioplayer.MainActivity.NEXTBUTTON;
import static com.example.myaudioplayer.MainActivity.PLAYBUTTON;
import static com.example.myaudioplayer.MainActivity.PREBUTTON;
import static com.example.myaudioplayer.MainActivity.albums;
import static com.example.myaudioplayer.MainActivity.playlists;

public class PlayerActivity extends AppCompatActivity {
    private PlayerActivityViewModel viewModel;
    private AudioService audioService;
    private boolean isBound = false;
    private BroadcastReceiver broadcastReceiver;

    TextView song_name, artist_name, duration_played, duration_total;
    ImageView cover_art, nextBtn, preBtn, backBtn, shuffleBtn, repeatBtn;
    FloatingActionButton playPauseBtn;
    SeekBar seekBar;
    //boolean isBindToCreatedService = false;
    String nextSource;
    private int startPosition;
    //private boolean isAlbumPlaylist;
    private Handler handler = new Handler();
    private Thread playThread, preThread, nextThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        viewModel = ViewModelProviders.of(this).get(PlayerActivityViewModel.class);
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
                    int mCurrentPosition = audioService.getCurrentDuration() / 1000;
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
                    if (audioService.isShuffle()) {
                        audioService.setShuffle(false);
                        shuffleBtn.setImageResource(R.drawable.ic_round_shuffle_24_off);
                    } else {
                        audioService.setShuffle(true);
                        shuffleBtn.setImageResource(R.drawable.ic_round_shuffle_24_on);
                    }
                }
            }
        });
        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBound) {
                    if (audioService.isRepeat()) {
                        audioService.setRepeat(false);
                        repeatBtn.setImageResource(R.drawable.ic_round_repeat_24_off);
                    } else {
                        audioService.setRepeat(true);
                        repeatBtn.setImageResource(R.drawable.ic_round_repeat_24_on);
                    }
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
            @Override
            public void onReceive(Context context, Intent intent) {
                String info = intent.getStringExtra("info");
                switch (info) {
                    case AudioService.BRC_AUDIO_CHANGE:
                        viewModel.getCurSong().setValue(audioService.getCurSong());
                        break;
                    case AudioService.BRC_PLAYING_STATE_CHANGE:
                        String state = audioService.getState();
                        if (state.equals(AudioService.STATE_PLAY))
                            playPauseBtn.setImageResource(R.drawable.ic_round_pause_24);
                        else
                            playPauseBtn.setImageResource(R.drawable.ic_round_play_arrow_24);
                    default:
                }

            }
        };
        IntentFilter intentFilter = new IntentFilter(AudioService.BRC_SERVICE_FILTER);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
    }

    private void registerLiveDataListenner() {
        viewModel.getmBinder().observe(this, new Observer<AudioService.AudioBinder>() {
            @Override
            public void onChanged(AudioService.AudioBinder audioBinder) {
                if (audioBinder != null) {
                    audioService = audioBinder.getService();
                    isBound = true;
                    String source = audioService.getPlaylist_source();
                    String state = audioService.getState();
                    if (!state.equals(AudioService.STATE_NONE)) {
                        if (nextSource.equals(AudioService.PLAYLIST_SOURCE_NONE))
                            setMetaData(audioService.getCurSong(), audioService.getCurrentDuration() / 1000);
                        else if (!nextSource.equals(source))
                        {
                             if (nextSource.equals(AudioService.PLAYLIST_SOURCE_SONG)) {
                                audioService.setPlaylist(playlists);
                                audioService.setPlaylist_source(AudioService.PLAYLIST_SOURCE_SONG);
                            } else {
                                audioService.setPlaylist(albumFiles);
                                audioService.setPlaylist_source(AudioService.PLAYLIST_SOURCE_ALBUM);
                            }
                            if (startPosition != audioService.getCurSongPos() && startPosition != -1)
                                audioService.changeAudio(startPosition);
                            else
                                setMetaData(audioService.getCurSong(), audioService.getCurrentDuration() / 1000);
                        }
                    } else if (startPosition != -1) {
                        if (nextSource.equals(AudioService.PLAYLIST_SOURCE_SONG)) {
                            if (source.equals(AudioService.PLAYLIST_SOURCE_ALBUM) || source.equals(AudioService.PLAYLIST_SOURCE_NONE)) {
                                audioService.setPlaylist(playlists);
                                audioService.setPlaylist_source(AudioService.PLAYLIST_SOURCE_SONG);
                            }
                        } else if (nextSource.equals(AudioService.PLAYLIST_SOURCE_ALBUM)) {
                            if (source.equals(AudioService.PLAYLIST_SOURCE_SONG) || source.equals(AudioService.PLAYLIST_SOURCE_NONE)) {
                                audioService.setPlaylist(albumFiles);
                                audioService.setPlaylist_source(AudioService.PLAYLIST_SOURCE_ALBUM);
                            }
                        }
                        audioService.changeAudio(startPosition);
                    }
                } else {
                    audioService = null;
                    isBound = false;
                }
            }
        });
        viewModel.getCurSong().observe(this, new Observer<MusicFiles>() {
            @Override
            public void onChanged(MusicFiles musicFiles) {
                if (musicFiles != null)
                    setMetaData(musicFiles, -1);
            }
        });
    }

    private void bindAudioService() {
        Intent serviceIntent = new Intent(this, AudioService.class);
        bindService(serviceIntent, viewModel.getServiceConnection(), Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        if (viewModel.getmBinder() != null) {
            unbindService(viewModel.getServiceConnection());
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
                    @Override
                    public void onClick(View v) {
                        nextBtnClick();
                    }
                });
            }
        };
        nextThread.start();
    }

    private void nextBtnClick() {
        if (isBound) {
            audioService.nextSong();
            //playPauseBtn.setImageResource(R.drawable.ic_round_pause_24);
        }
        showNotification(audioService.getCurSong(), R.drawable.ic_round_pause_24);

    }


    private void preThreadBtn() {
        preThread = new Thread() {
            @Override
            public void run() {
                super.run();
                preBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        preBtnClick();
                    }
                });
            }
        };
        preThread.start();
    }

    private void preBtnClick() {
        if (isBound) {
            audioService.preSong();
            //playPauseBtn.setImageResource(R.drawable.ic_round_pause_24);
        }
        showNotification(audioService.getCurSong(), R.drawable.ic_round_pause_24);

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
            if (audioService.getState() == AudioService.STATE_PLAY) {
                //playPauseBtn.setImageResource(R.drawable.ic_round_play_arrow_24);
                audioService.playPauseAudio();
                /*seek bar + runOnUIThread*/
            } else {
                //playPauseBtn.setImageResource(R.drawable.ic_round_pause_24);
                audioService.playPauseAudio();
                /*seek bar + runOnUIThread*/
            }
        }
    }
    public void showNotification (MusicFiles musicFiles,int playPauseBtn) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(musicFiles.getPath());
        byte[] art = retriever.getEmbeddedPicture();
        Bitmap bitmap;
        if (art != null) {
            bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            ImageAnimation(this, cover_art, bitmap);
        } else {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.music_default);
            ImageAnimation(this, cover_art, bitmap);
        }
        Intent intent = new Intent(this,PlayerActivity.class );
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent,0);
        Intent prevIntent = new Intent(this, NotificationReceiver.class).setAction(PREBUTTON);
        PendingIntent prevPendingIntent = PendingIntent.getBroadcast(this, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent playIntent = new Intent(this, NotificationReceiver.class).setAction(PLAYBUTTON);
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent nextIntent = new Intent(this, NotificationReceiver.class).setAction(NEXTBUTTON);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent closeIntent = new Intent (this, NotificationReceiver.class).setAction(ClOSEBUTTON);
        PendingIntent closePendingIntent = PendingIntent.getBroadcast(this, 0, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            mBuilder = new NotificationCompat.Builder(this, MCHANNEL)
                    .setSmallIcon(R.drawable.ic_round_queue_music_24)
                    .setLargeIcon(bitmap)
                    .setContentTitle(musicFiles.getTitle())
                    .setContentText(musicFiles.getArtist())
                    .addAction(R.drawable.ic_round_skip_previous_24, "Previous", prevPendingIntent)
                    .addAction(playPauseBtn, "Play", playPendingIntent)
                    .addAction(R.drawable.ic_round_skip_next_24, "Next", nextPendingIntent)
                    .addAction(R.drawable.ic_round_close_24, "Close", closePendingIntent)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle())
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setOngoing(true)
                    .setLocalOnly(true)
                    .setNotificationSilent()
                    .setContentIntent(contentIntent)
                    .setLocalOnly(true);
        }
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, mBuilder.build());
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
        int position = getIntent().getIntExtra("position", -1);
        nextSource = getIntent().getStringExtra("sender");
        //boolean isCreateService = getIntent().getBooleanExtra("createService", false);
        String state = getIntent().getStringExtra("state");
        /*if (playlist_source != null && playlist_source.equals(AudioService.PLAYLIST_SOURCE_ALBUM)) {
            this.isAlbumPlaylist = true;
        } else
            this.isAlbumPlaylist = false;*/
        /*if (!isCreateService) {
            isBindToCreatedService = true;
        } else {
            isBindToCreatedService = false;
        }*/
        if (state.equals(AudioService.STATE_PLAY))
            playPauseBtn.setImageResource(R.drawable.ic_round_pause_24);
        else
            playPauseBtn.setImageResource(R.drawable.ic_round_play_arrow_24);
        startPosition = position;
        /*startAudioService(viewModel.getListSong().getValue().get(position).getPath());
        setMetaData(viewModel.getMetadata(position));*/
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
    }

    public void ImageAnimation(final Context context, final ImageView imageView, final Bitmap bitmap) {
        Animation animOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        final Animation animIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Glide.with(context).load(bitmap).into(imageView);
                animIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                imageView.startAnimation(animIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imageView.startAnimation(animOut);
    }

    private void setMetaData(MusicFiles musicFiles, int progress) {
        int durationTotal = Integer.parseInt(musicFiles.getDuration()) / 1000;
        duration_total.setText(formattedTime(durationTotal));
        seekBar.setMax(durationTotal);
        if (progress == -1) {
            seekBar.setProgress(0);
            duration_played.setText(formattedTime(0));
        } else {
            seekBar.setProgress(progress);
            duration_played.setText(formattedTime(progress));
        }
        song_name.setText(musicFiles.getTitle());
        artist_name.setText(musicFiles.getArtist());


        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(musicFiles.getPath());
        byte[] art = retriever.getEmbeddedPicture();
        Bitmap bitmap;
        if (art != null) {
            bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            ImageAnimation(this, cover_art, bitmap);
        } else {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.music_default);
            ImageAnimation(this, cover_art, bitmap);
        }
    }


    /*private void metaData(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        int durationTotal = Integer.parseInt(listSong.get(position).getDuration()) / 1000;
        duration_total.setText(formattedTime(durationTotal));
        song_name.setText(listSong.get(position).getTitle());
        artist_name.setText(listSong.get(position).getArtist());
        seekBar.setMax(mediaPlayer.getDuration() / 1000);
        byte[] art = retriever.getEmbeddedPicture();
        Bitmap bitmap;
        if (art != null) {
            bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            ImageAnimation(this, cover_art, bitmap);
            *//*Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(@Nullable Palette palette) {
                    Palette.Swatch swatch = palette.getDominantSwatch();
                    if (swatch != null) {
                        ImageView gredient = findViewById(R.id.imageViewGradient);
                        RelativeLayout mContainer = findViewById(R.id.mContainer);
                        gredient.setBackgroundResource(R.drawable.gradient_bg);
                        mContainer.setBackgroundResource(R.drawable.main_bg);
                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{swatch.getRgb(), 0x00000000});
                        gredient.setBackground(gradientDrawable);
                        GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{swatch.getRgb(), swatch.getRgb()});
                        mContainer.setBackground(gradientDrawableBg);
                        song_name.setTextColor(swatch.getBodyTextColor());
                        artist_name.setTextColor(swatch.getBodyTextColor());
                    } else {
                        ImageView gredient = findViewById(R.id.imageViewGradient);
                        RelativeLayout mContainer = findViewById(R.id.mContainer);
                        gredient.setBackgroundResource(R.drawable.gradient_bg);
                        mContainer.setBackgroundResource(R.drawable.main_bg);
                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{0xff000000, 0x00000000});
                        gredient.setBackground(gradientDrawable);
                        GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{0xff000000, 0xff000000});
                        mContainer.setBackground(gradientDrawableBg);
                        song_name.setTextColor(Color.WHITE);
                        artist_name.setTextColor(Color.DKGRAY);
                    }
                }
            });*//*
        } else {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.music_default);
            ImageAnimation(this, cover_art, bitmap);
            //Glide.with(this).asBitmap().load(R.drawable.music_default).into(cover_art);
            *//*ImageView gredient = findViewById(R.id.imageViewGradient);
            RelativeLayout mContainer = findViewById(R.id.mContainer);
            gredient.setBackgroundResource(R.drawable.gradient_bg);
            mContainer.setBackgroundResource(R.drawable.main_bg);
            song_name.setTextColor(Color.WHITE);
            artist_name.setTextColor(Color.DKGRAY);*//*
        }
    }*/
}