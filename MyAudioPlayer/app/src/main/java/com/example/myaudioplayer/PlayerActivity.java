package com.example.myaudioplayer;

import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Random;

import static com.example.myaudioplayer.AlbumDetailsAdapter.albumFiles;
import static com.example.myaudioplayer.MainActivity.musicFiles;
import static com.example.myaudioplayer.MainActivity.repeatBoolean;
import static com.example.myaudioplayer.MainActivity.shuffleBoolean;

public class PlayerActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener {
    TextView song_name, artist_name, duration_played, duration_total;
    ImageView cover_art, nextBtn, preBtn, backBtn, shuffleBtn, repeatBtn, menuBtn;
    FloatingActionButton playPauseBtn;
    SeekBar seekBar;
    int position = -1;
    static ArrayList<MusicFiles> listSong;
    static Uri uri;
    static MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Thread playThread, preThread, nextThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        initView();
        getIntentMethod();
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress * 1000);
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
                if (mediaPlayer != null) {
                    int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                    duration_played.setText(formattedTime(mCurrentPosition));
                }
                handler.postDelayed(this, 1000);
            }
        });
        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shuffleBoolean)
                {
                    shuffleBoolean = false;
                    shuffleBtn.setImageResource(R.drawable.ic_round_shuffle_24_off);
                }
                else
                {
                    shuffleBoolean = true;
                    shuffleBtn.setImageResource(R.drawable.ic_round_shuffle_24_on);
                }
            }
        });
        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (repeatBoolean)
                {
                    repeatBoolean = false;
                    repeatBtn.setImageResource(R.drawable.ic_round_repeat_24_off);
                }
                else
                {
                    repeatBoolean = true;
                    repeatBtn.setImageResource(R.drawable.ic_round_repeat_24_on);
                }
            }
        });
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
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            if (shuffleBoolean && !repeatBoolean) {
                position = getRandom(listSong.size() - 1);
            }
            else if (!shuffleBoolean && !repeatBoolean) {
                position = (position + 1) % listSong.size();
            }
            uri = Uri.parse(listSong.get(position).getPath());
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            mediaPlayer.setOnCompletionListener(this);
            metaData(uri);
            //seekbar + runOnUIThread
            //playPauseBtn.setBackgroundResource(R.drawable.ic_round_pause_24);
            mediaPlayer.start();
        }
        else
        {
            mediaPlayer.stop();
            mediaPlayer.release();
            if (shuffleBoolean && !repeatBoolean) {
                position = getRandom(listSong.size() - 1);
            }
            else if (!shuffleBoolean && !repeatBoolean) {
                position = (position + 1) % listSong.size();
            }
            uri = Uri.parse(listSong.get(position).getPath());
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            mediaPlayer.setOnCompletionListener(this);
            metaData(uri);
            //seekbar + runOnUIThread
            //playPauseBtn.setBackgroundResource(R.drawable.ic_round_play_arrow_24);
        }
    }

    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i + 1);
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
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            if (shuffleBoolean && !repeatBoolean) {
                position = getRandom(listSong.size() - 1);
            }
            else if (!shuffleBoolean && !repeatBoolean) {
                position = (position - 1 + listSong.size()) % listSong.size();
            }
            uri = Uri.parse(listSong.get(position).getPath());
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            mediaPlayer.setOnCompletionListener(this);
            metaData(uri);
            //seekbar + runOnUIThread
            //playPauseBtn.setBackgroundResource(R.drawable.ic_round_pause_24);
            mediaPlayer.start();
        }
        else
        {
            mediaPlayer.stop();
            mediaPlayer.release();
            if (shuffleBoolean && !repeatBoolean) {
                position = getRandom(listSong.size() - 1);
            }
            else if (!shuffleBoolean && !repeatBoolean) {
                position = (position - 1 + listSong.size()) % listSong.size();
            }
            uri = Uri.parse(listSong.get(position).getPath());
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            mediaPlayer.setOnCompletionListener(this);
            metaData(uri);
            //seekbar + runOnUIThread
            //playPauseBtn.setBackgroundResource(R.drawable.ic_round_play_arrow_24);
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
        if (mediaPlayer.isPlaying()) {
            playPauseBtn.setImageResource(R.drawable.ic_round_play_arrow_24);
            mediaPlayer.pause();
            /*seek bar + runOnUIThread*/
        }
        else {
            playPauseBtn.setImageResource(R.drawable.ic_round_pause_24);
            mediaPlayer.start();
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
        position = getIntent().getIntExtra("position", -1);
        String sender = getIntent().getStringExtra("sender");
        if (sender != null && sender.equals("albumDetails"))
        {
            listSong = albumFiles;
        }
        else
        listSong = musicFiles;
        if (listSong != null) {
            playPauseBtn.setImageResource(R.drawable.ic_round_pause_24);
            uri = Uri.parse(listSong.get(position).getPath());
        }
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.start();
        //seekBar.setMax(mediaPlayer.getDuration() / 1000);
        metaData(uri);
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

    private void metaData(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        int durationTotal = Integer.parseInt(listSong.get(position).getDuration()) / 1000;
        duration_total.setText(formattedTime(durationTotal));
        song_name.setText(listSong.get(position).getTitle());
        artist_name.setText(listSong.get(position).getArtist());
        seekBar.setMax(mediaPlayer.getDuration() / 1000);
        byte[] art = retriever.getEmbeddedPicture();
        if (art != null) {
            Glide.with(this).asBitmap().load(art).into(cover_art);
        } else {
            Glide.with(this).asBitmap().load(R.drawable.music_default).into(cover_art);
        }

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        nextBtnClick();
        if (mediaPlayer != null)
        {
            mediaPlayer.start();
        }
    }
}