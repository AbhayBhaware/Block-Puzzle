package com.example.blockblastify;

import android.content.Context;
import android.media.MediaPlayer;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.abhay.blockblastify.R;

public class MusicManager implements DefaultLifecycleObserver {

    private static MediaPlayer bgMusic;
    private static boolean shouldPlay = false;
    private static boolean isObserverAdded = false;

    public static void init(Context context) {
        if (bgMusic == null) {
            bgMusic = MediaPlayer.create(context.getApplicationContext(), R.raw.bg_music);
            if (bgMusic != null) {
                bgMusic.setLooping(true);
                bgMusic.setVolume(0.2f, 0.2f);
            }
        }

        if (!isObserverAdded) {
            ProcessLifecycleOwner.get().getLifecycle().addObserver(new MusicManager());
            isObserverAdded = true;
        }
    }

    public static void startMusic(Context context) {
        init(context);
        shouldPlay = true;

        if (bgMusic != null && !bgMusic.isPlaying()) {
            bgMusic.start();
        }
    }

    public static void pauseMusic() {
        if (bgMusic != null && bgMusic.isPlaying()) {
            bgMusic.pause();
        }
    }

    public static void stopMusic() {
        shouldPlay = false;

        if (bgMusic != null) {
            if (bgMusic.isPlaying()) {
                bgMusic.stop();
            }
            bgMusic.release();
            bgMusic = null;
        }
    }

    public static boolean isPlaying() {
        return bgMusic != null && bgMusic.isPlaying();
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        if (shouldPlay && bgMusic != null && !bgMusic.isPlaying()) {
            bgMusic.start();
        }
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        if (bgMusic != null && bgMusic.isPlaying()) {
            bgMusic.pause();
        }
    }
}