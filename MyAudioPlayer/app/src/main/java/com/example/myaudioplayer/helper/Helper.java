package com.example.myaudioplayer.helper;

import android.media.MediaMetadataRetriever;

public class Helper {
    public static byte[] getEmbeddedArt(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }
}
