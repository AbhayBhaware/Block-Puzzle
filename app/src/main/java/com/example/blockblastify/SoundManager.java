package com.example.blockblastify;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import com.abhay.blockblastify.R;

public class SoundManager {

    private boolean isSoundOn = true;

    private SoundPool soundPool;

    private int placeSound;
    private int dropSound;
    private int clearSound;
    private int gameOverSound;
    private int coinSpendSound;
    private int coinEarnSound;
    private int clickSound;



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
        clickSound = soundPool.load(context, R.raw.clicksound, 1);



    }

    public void playPlace() {
        playSound(placeSound);
    }

    public void playDrop() {
        playSound(dropSound);
    }

    public void playClear() {
        playSound(clearSound);
    }

    public void playGameOver() {
        playSound(gameOverSound);
    }

    public void playCoinSpend() {
        playSound(coinSpendSound);
    }

    public void playCoinEarn() {
        playSound(coinEarnSound);
    }

    public void playClick() {
        playSound(clickSound);
    }


    public void setSoundEnabled(boolean enabled) {
        isSoundOn = enabled;
    }


    private void playSound(int soundId) {
        if (soundPool != null && isSoundOn) {
            soundPool.play(soundId, 1, 1, 0, 0, 1);
        }
    }




    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}
