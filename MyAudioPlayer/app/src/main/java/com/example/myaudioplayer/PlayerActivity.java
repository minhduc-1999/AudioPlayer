package com.example.myaudioplayer;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.palette.graphics.Palette;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myaudioplayer.audiomodel.MusicFiles;
import com.example.myaudioplayer.audioservice.AudioService;
import com.example.myaudioplayer.viewmodel.PlayerActivityViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Random;

import static com.example.myaudioplayer.AlbumDetailsAdapter.albumFiles;

import static com.example.myaudioplayer.MainActivity.repeatBoolean;
import static com.example.myaudioplayer.MainActivity.shuffleBoolean;
import static com.example.myaudioplayer.MusicAdapter.mFiles;

public class PlayerActivity extends AppCompatActivity {
    private PlayerActivityViewModel viewModel;
    private AudioService audioService;

    private BroadcastReceiver broadcastReceiver;

    TextView song_name, artist_name, duration_played, duration_total;
    ImageView cover_art, nextBtn, preBtn, backBtn, shuffleBtn, repeatBtn, menuBtn;
    FloatingActionButton playPauseBtn;
    SeekBar seekBar;
    private int startPosition;
    private Handler handler = new Handler();
    private Thread playThread, preThread, nextThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        viewModel = ViewModelProviders.of(this).get(PlayerActivityViewModel.class);
        registerLiveDataListenner();

        registerBroadcastReceiver();

        startAudioService();
        initView();

        getIntentMethod();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && audioService != null) {
                    audioService.seekTo(progress * 1000);
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
                if (shuffleBoolean) {
                    shuffleBoolean = false;
                    shuffleBtn.setImageResource(R.drawable.ic_round_shuffle_24_off);
                } else {
                    shuffleBoolean = true;
                    shuffleBtn.setImageResource(R.drawable.ic_round_shuffle_24_on);
                }
            }
        });
        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (repeatBoolean) {
                    repeatBoolean = false;
                    repeatBtn.setImageResource(R.drawable.ic_round_repeat_24_off);
                } else {
                    repeatBoolean = true;
                    repeatBtn.setImageResource(R.drawable.ic_round_repeat_24_on);
                }
            }
        });
    }

    private void registerBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                    nextBtnClick();
            }
        };
        IntentFilter intentFilter = new IntentFilter(AudioService.BRC_SERVICE_FILTER);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
    }

    private void registerLiveDataListenner() {
        viewModel.getCurPos().observe(this, new Observer<Integer>() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onChanged(Integer integer) {
                if (integer != -1) {
                    if(audioService != null) {
                        Uri uri = viewModel.getAudioFileUri(integer);
                        audioService.changeAudio(uri);
                        setMetaData(viewModel.getMetadata(integer));
                    }
                }
            }
        });
        viewModel.getmBinder().observe(this, new Observer<AudioService.AudioBinder>() {
            @Override
            public void onChanged(AudioService.AudioBinder audioBinder) {
                if (audioBinder != null) {
                    audioService = audioBinder.getService();
                    viewModel.getCurPos().setValue(startPosition);
                }
                else
                {
                    audioService = null;
                }
            }
        });
    }

    private void startAudioService() {
        Intent serviceIntent = new Intent(this, AudioService.class);
        startService(serviceIntent);
        bindService();
    }

    private void bindService() {
        Intent serviceIntent = new Intent(this, AudioService.class);
        bindService(serviceIntent, viewModel.getServiceConnection(), Context.BIND_AUTO_CREATE);
    }

    private void setMetaData(MusicFiles musicFiles) {
        seekBar.setProgress(0);
        duration_played.setText(formattedTime(0));
        int durationTotal = Integer.parseInt(musicFiles.getDuration()) / 1000;
        duration_total.setText(formattedTime(durationTotal));
        song_name.setText(musicFiles.getTitle());
        artist_name.setText(musicFiles.getArtist());
        seekBar.setMax(durationTotal);

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
        viewModel.nextSong(shuffleBoolean, repeatBoolean);
        playPauseBtn.setImageResource(R.drawable.ic_round_pause_24);
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
        viewModel.preSong(shuffleBoolean, repeatBoolean);
        playPauseBtn.setImageResource(R.drawable.ic_round_pause_24);
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
        if (audioService.isPlaying()) {
            playPauseBtn.setImageResource(R.drawable.ic_round_play_arrow_24);
            audioService.playPauseAudio(AudioService.ACTION_PAUSE);
            /*seek bar + runOnUIThread*/
        } else {
            playPauseBtn.setImageResource(R.drawable.ic_round_pause_24);
            audioService.playPauseAudio(AudioService.ACTION_PLAY);
            /*seek bar + runOnUIThread*/
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
        int position = getIntent().getIntExtra("position", -1);
        String sender = getIntent().getStringExtra("sender");
        if (sender != null && sender.equals("albumDetails")) {
            viewModel.getListSong().setValue(albumFiles);
        } else
            viewModel.getListSong().setValue(mFiles);
        if(viewModel.getListSong().getValue() != null)
            playPauseBtn.setImageResource(R.drawable.ic_round_pause_24);
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
        menuBtn = findViewById(R.id.menu_btn);
        playPauseBtn = findViewById(R.id.play_pause);
        seekBar = findViewById(R.id.seekBar);
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
}