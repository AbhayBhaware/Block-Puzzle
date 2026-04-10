package com.abhay.blockblastify;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class HomeActivity extends AppCompatActivity {

    CardView classicbtn, settingbtn, levelbtn;
    View dimBackground;

    private static final String PREFS_NAME = "BlockPuzzlePrefs";
    private static final String KEY_HIGH_SCORE = "high_score";
    private static final String KEY_COINS = "coins";
    private static final String KEY_MUSIC_ON = "music_on";
    private boolean doubleBackToExitPressedOnce = false;
    com.abhay.blockblastify.SoundManager soundManager;
    private android.os.Handler backPressHandler = new android.os.Handler();
    private final Runnable resetBackPress = () -> doubleBackToExitPressedOnce = false;

    private static final String KEY_SOUND_ON = "sound_on";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setNavigationBarColor(Color.parseColor("#0D1231"));
        setContentView(R.layout.activity_home);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        boolean isMusicOn = prefs.getBoolean(KEY_MUSIC_ON, true);
        if (isMusicOn) {
            com.abhay.blockblastify.MusicManager.startMusic(this);
        }

        classicbtn = findViewById(R.id.classicbtn);
        settingbtn = findViewById(R.id.settingbtn);
        levelbtn = findViewById(R.id.levelbtn);
        dimBackground = findViewById(R.id.dimBackground);
        soundManager = new com.abhay.blockblastify.SoundManager(this);

        boolean isSoundOn = prefs.getBoolean(KEY_SOUND_ON, true);
        soundManager.setSoundEnabled(isSoundOn);


        TextView highScoreText = findViewById(R.id.highScoreHome);
        TextView coinText = findViewById(R.id.coinstextHome);

        int highScore = prefs.getInt(KEY_HIGH_SCORE, 0);
        int coins = prefs.getInt(KEY_COINS, 0);

        highScoreText.setText(String.valueOf(highScore));
        coinText.setText(String.valueOf(coins));

        classicbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundManager.playClick();
                animateButton(v);
                Intent i = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(i);
            }
        });

        settingbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundManager.playClick();
                animateButton(v);

                View popupView = getLayoutInflater().inflate(R.layout.settings_pannel, null);

                ImageView musicIcon = popupView.findViewById(R.id.musicIcon);
                ImageView soundIcon = popupView.findViewById(R.id.soundIcon);

                boolean isSoundOn = prefs.getBoolean(KEY_SOUND_ON, true);

                if (isSoundOn) {
                    soundIcon.setImageResource(R.drawable.soundon);
                } else {
                    soundIcon.setImageResource(R.drawable.soundoff);
                }

                boolean currentMusicState = prefs.getBoolean(KEY_MUSIC_ON, true);
                if (currentMusicState) {
                    musicIcon.setImageResource(R.drawable.musicon);
                } else {
                    musicIcon.setImageResource(R.drawable.musicoff);
                }

                int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);

                PopupWindow popupWindow = new PopupWindow(
                        popupView,
                        width,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        true
                );

                popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                popupWindow.setElevation(10);
                popupWindow.setOutsideTouchable(true);

                dimBackground.setAlpha(0f);
                dimBackground.setVisibility(View.VISIBLE);
                dimBackground.animate().alpha(1f).setDuration(200);

                dimBackground.setOnClickListener(view -> {
                    // block outside touches
                });

                popupWindow.showAtLocation(
                        findViewById(android.R.id.content),
                        Gravity.CENTER,
                        0,
                        0
                );

                popupWindow.setOnDismissListener(() -> {
                    dimBackground.animate().alpha(0f).setDuration(200)
                            .withEndAction(() -> dimBackground.setVisibility(View.GONE));
                });

                popupView.findViewById(R.id.closeButton).setOnClickListener(view -> {
                    soundManager.playClick();
                    popupWindow.dismiss();
                });

                View.OnClickListener menuClickListner = view -> {
                    int id = view.getId();

                    if (id == R.id.musiclinearlayout) {

                        soundManager.playClick();

                        boolean currentState = prefs.getBoolean(KEY_MUSIC_ON, true);
                        boolean newState = !currentState;

                        prefs.edit().putBoolean(KEY_MUSIC_ON, newState).apply();

                        if (newState) {
                            com.abhay.blockblastify.MusicManager.startMusic(HomeActivity.this);
                            musicIcon.setImageResource(R.drawable.musicon);
                            Toast.makeText(HomeActivity.this, "Music ON 🎵", Toast.LENGTH_SHORT).show();
                        } else {
                            com.abhay.blockblastify.MusicManager.pauseMusic();
                            musicIcon.setImageResource(R.drawable.musicoff);
                            Toast.makeText(HomeActivity.this, "Music OFF 🔇", Toast.LENGTH_SHORT).show();
                        }

                    } else if (id == R.id.soundlinearlayout) {

                        soundManager.playClick();
                        boolean currentState = prefs.getBoolean(KEY_SOUND_ON, true);
                        boolean newState = !currentState;

                        prefs.edit().putBoolean(KEY_SOUND_ON, newState).apply();

                        if (newState) {
                            soundIcon.setImageResource(R.drawable.soundon);
                            Toast.makeText(HomeActivity.this, "Sound ON 🔊", Toast.LENGTH_SHORT).show();
                        } else {
                            soundIcon.setImageResource(R.drawable.soundoff);
                            Toast.makeText(HomeActivity.this, "Sound OFF 🔇", Toast.LENGTH_SHORT).show();
                        }
                    }else if (id == R.id.sharelinearlayout) {

                        String shareMessage = "🎮 I'm playing this amazing Block Puzzle game!\n\n" +
                                "Can you beat my score? 😎🔥\n\n" +
                                "Download now:\n" +
                                "https://play.google.com/store/apps/details?id=" + getPackageName();

                        android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareMessage);

                        startActivity(android.content.Intent.createChooser(shareIntent, "Share via"));
                    } else if (id == R.id.rateuslinearlayout) {

                        String packageName = getPackageName();

                        try {
                            // Open Play Store app
                            startActivity(new android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse("market://details?id=" + packageName)
                            ));
                        } catch (android.content.ActivityNotFoundException e) {
                            // If Play Store not installed, open in browser
                            startActivity(new android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)
                            ));
                        }
                    }
                };

                popupView.findViewById(R.id.musiclinearlayout).setOnClickListener(menuClickListner);
                popupView.findViewById(R.id.soundlinearlayout).setOnClickListener(menuClickListner);
                popupView.findViewById(R.id.sharelinearlayout).setOnClickListener(menuClickListner);
                popupView.findViewById(R.id.rateuslinearlayout).setOnClickListener(menuClickListner);
            }
        });

        levelbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundManager.playClick();
                animateButton(v);
                Toast.makeText(HomeActivity.this, "Coming soon...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int coins = prefs.getInt(KEY_COINS, 0);
        int highScore = prefs.getInt(KEY_HIGH_SCORE, 0);

        TextView coinText = findViewById(R.id.coinstextHome);
        TextView highScoreText = findViewById(R.id.highScoreHome);

        coinText.setText(String.valueOf(coins));
        highScoreText.setText(String.valueOf(highScore));
    }

    private void animateButton(View view) {
        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(80)
                .withEndAction(() ->
                        view.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(80)
                );
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();

        backPressHandler.removeCallbacks(resetBackPress);
        backPressHandler.postDelayed(resetBackPress, 2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        backPressHandler.removeCallbacks(resetBackPress);
        if (soundManager != null) {
            soundManager.release();
        }
    }
}