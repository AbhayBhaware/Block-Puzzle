package com.example.blockpuzzle;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

public class SoundManager {

    private SoundPool soundPool;

    private int placeSound;
    private int dropSound;
    private int clearSound;
    private int gameOverSound;
    private int coinSpendSound;
    private int coinEarnSound;



    public SoundManager(Context context) {

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(audioAttributes)
                .build();

        placeSound = soundPool.load(context, R.raw.place, 1);
        dropSound = soundPool.load(context, R.raw.drop, 1);
        clearSound = soundPool.load(context, R.raw.clear, 1);
        gameOverSound = soundPool.load(context, R.raw.game_over, 1);
        coinSpendSound = soundPool.load(context, R.raw.spend_coin, 1);
        coinEarnSound = soundPool.load(context, R.raw.coinearned, 1);


    }

    public void playPlace() {
        soundPool.play(placeSound, 1, 1, 0, 0, 1);
    }

    public void playDrop() {
        soundPool.play(dropSound, 1, 1, 0, 0, 1);
    }

    public void playClear() {
        soundPool.play(clearSound, 1, 1, 0, 0, 1);
    }

    public void playGameOver() {
        soundPool.play(gameOverSound, 1, 1, 0, 0, 1);
    }
    public void playCoinSpend() {
        soundPool.play(coinSpendSound, 1, 1, 0, 0, 1);
    }
    public void playCoinEarn() {
        soundPool.play(coinEarnSound, 1, 1, 0, 0, 1);
    }



    public void release() {
        soundPool.release();
    }
}
