package com.example.blockpuzzle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

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

public class MainActivity extends AppCompatActivity {

    GameView gameView;
    TextView scoreText;

    CardView menubtn;

    View dimBackground;

    private static final String PREFS_NAME = "BlockPuzzlePrefs";
    private static final String KEY_HIGH_SCORE = "high_score";

    private static final String KEY_COINS = "coins";
    private static final String KEY_MUSIC_ON = "music_on";
    private static final String KEY_SOUND_ON = "sound_on";

    TextView coinText;
    int coins;

    SharedPreferences prefs;
    int highScore;

    int displayedScore = 0;
    android.animation.ValueAnimator scoreAnimator;
    SoundManager soundManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setNavigationBarColor(Color.parseColor("#0D1231"));


        setContentView(R.layout.activity_main);

        gameView = findViewById(R.id.gameView);
        scoreText = findViewById(R.id.scoreText);
        menubtn=findViewById(R.id.menuButton);
        dimBackground = findViewById(R.id.dimBackground);
        soundManager = new SoundManager(this);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        boolean isSoundOn = prefs.getBoolean(KEY_SOUND_ON, true);
        soundManager.setSoundEnabled(isSoundOn);

        gameView.setSoundEnabled(isSoundOn);






        coinText = findViewById(R.id.coinText);
        coins = prefs.getInt(KEY_COINS, 0);
        coinText.setText(String.valueOf(coins));


        TextView highScoreText = findViewById(R.id.highScoreText);
        highScore = prefs.getInt(KEY_HIGH_SCORE, 0);
        highScoreText.setText(String.valueOf(highScore));


        gameView.setCoinListener(earnedCoins -> {

            coins += earnedCoins;

            prefs.edit().putInt(KEY_COINS, coins).apply();

            animateCoins(coins);          // smooth counting
            showCoinEarnEffect(earnedCoins);  // ADD THIS LINE
        });




        menubtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundManager.playClick();
                popupmenu();
            }
        });

        gameView.setGameOverListener(score -> {

            // Participation reward
            coins += 5; // participation

            if (score > highScore) {
                highScore = score;
                prefs.edit().putInt(KEY_HIGH_SCORE, score).apply();

                coins += 20; // bonus
                Toast.makeText(this, "New High Score! +20 coins", Toast.LENGTH_SHORT).show();
            }

            prefs.edit().putInt(KEY_COINS, coins).apply();
            coinText.setText(String.valueOf(coins));
            highScoreText.setText(String.valueOf(highScore));



            showGameOverPopup();


        });


        gameView.setScoreListener(new GameView.ScoreListener() {
            @Override
            public void onScoreChanged(int newScore) {

                // Cancel previous animation if running
                if (scoreAnimator != null && scoreAnimator.isRunning()) {
                    scoreAnimator.cancel();
                }

                scoreAnimator = android.animation.ValueAnimator.ofInt(displayedScore, newScore);
                scoreAnimator.setDuration(350); // smooth counting

                scoreAnimator.addUpdateListener(animation -> {
                    displayedScore = (int) animation.getAnimatedValue();
                    scoreText.setText("Score: " + displayedScore);
                });

                scoreAnimator.start();
            }
        });


    }

    private void restartGame() {
        recreate();
    }

    public void popupmenu()
    {

        View popupView=getLayoutInflater().inflate(R.layout.menu_pannel,null);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        ImageView musicIcon = popupView.findViewById(R.id.musicIcon);

        ImageView soundIcon = popupView.findViewById(R.id.soundIcon);


        boolean isSoundOn = prefs.getBoolean(KEY_SOUND_ON, true);

        if (isSoundOn) {
            soundIcon.setImageResource(R.drawable.soundon);
        } else {
            soundIcon.setImageResource(R.drawable.soundoff);
        }

        boolean isMusicOn = prefs.getBoolean(KEY_MUSIC_ON, true);

        if (isMusicOn) {
            musicIcon.setImageResource(R.drawable.musicon);
        } else {
            musicIcon.setImageResource(R.drawable.musicoff);
        }

        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);

        PopupWindow popupWindow=new PopupWindow(
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
            // Do nothing – just block outside touches
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

        popupView.findViewById(R.id.closeButton).setOnClickListener(v -> {
            soundManager.playClick();
            popupWindow.dismiss();
        });

        View.OnClickListener menuClickListner = view-> {
            int id=view.getId();

            if (id == R.id.musiclinearlayout) {
                soundManager.playClick();

                boolean currentState = prefs.getBoolean(KEY_MUSIC_ON, true);
                boolean newState = !currentState;

                prefs.edit().putBoolean(KEY_MUSIC_ON, newState).apply();

                if (newState) {
                    MusicManager.startMusic(MainActivity.this);
                    musicIcon.setImageResource(R.drawable.musicon);
                    Toast.makeText(MainActivity.this, "Music ON 🎵", Toast.LENGTH_SHORT).show();
                } else {
                    MusicManager.pauseMusic();
                    musicIcon.setImageResource(R.drawable.musicoff);
                    Toast.makeText(MainActivity.this, "Music OFF 🔇", Toast.LENGTH_SHORT).show();
                }
            } else if (id == R.id.soundlinearlayout) {
                soundManager.playClick();

                boolean currentState = prefs.getBoolean(KEY_SOUND_ON, true);
                boolean newState = !currentState;

                prefs.edit().putBoolean(KEY_SOUND_ON, newState).apply();

                soundManager.setSoundEnabled(newState);
                gameView.setSoundEnabled(newState);

                if (newState) {
                    soundIcon.setImageResource(R.drawable.soundon);
                    Toast.makeText(MainActivity.this, "Sound ON 🔊", Toast.LENGTH_SHORT).show();
                } else {
                    soundIcon.setImageResource(R.drawable.soundoff);
                    Toast.makeText(MainActivity.this, "Sound OFF 🔇", Toast.LENGTH_SHORT).show();
                }
            } else if (id == R.id.homelinearlayout) {
                popupWindow.dismiss();
                showQuitGameDialog();
            } else if (id==R.id.restartlinearlayout)
            {
                Toast.makeText(MainActivity.this, "Game Restarted", Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
                restartGame();
            }

        };
        popupView.findViewById(R.id.musiclinearlayout).setOnClickListener(menuClickListner);
        popupView.findViewById(R.id.soundlinearlayout).setOnClickListener(menuClickListner);
        popupView.findViewById(R.id.homelinearlayout).setOnClickListener(menuClickListner);
        popupView.findViewById(R.id.restartlinearlayout).setOnClickListener(menuClickListner);

    }

    private void showGameOverPopup() {

        View popupView = getLayoutInflater().inflate(R.layout.gameover_pannel, null);

        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                width,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                false
        );

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setElevation(20);
        popupWindow.setOutsideTouchable(false);

        // Dim background
        dimBackground.setAlpha(0f);
        dimBackground.setVisibility(View.VISIBLE);
        dimBackground.animate().alpha(1f).setDuration(250);

        CardView continueBtn = popupView.findViewById(R.id.continuebtn);

        popupView.findViewById(R.id.restartBtn).setOnClickListener(v -> {
            soundManager.playClick();
            popupWindow.dismiss();
            dimBackground.setVisibility(View.GONE);
            restartGame();
        });

        continueBtn.setOnClickListener(v -> {
            if (coins>=200)
            {
                coins -= 200;

                prefs.edit().putInt(KEY_COINS, coins).apply();

                animateCoins(coins);  // smooth counting
                showCoinDeductionEffect(200); // floating -200
                soundManager.playCoinSpend(); // coin sound
                soundManager.playClick();


                popupWindow.dismiss();
                dimBackground.setVisibility(View.GONE);

                gameView.performRevive();
            }
            else
            {
                Toast.makeText(this, "Insufficient Coins", Toast.LENGTH_SHORT).show();
            }
        });

        popupWindow.showAtLocation(
                findViewById(android.R.id.content),
                Gravity.CENTER,
                0,
                0
        );
    }

    private void showCoinDeductionEffect(int amount) {

        TextView floatingText = new TextView(this);
        floatingText.setText("-" + amount);
        floatingText.setTextColor(Color.RED);
        floatingText.setTextSize(18f);
        floatingText.setTypeface(null, android.graphics.Typeface.BOLD);

        // Position near coinText
        int[] location = new int[2];
        coinText.getLocationOnScreen(location);

        floatingText.setX(location[0] + coinText.getWidth() / 2f);
        floatingText.setY(location[1] - 20);

        // Add to root layout
        ((android.view.ViewGroup) findViewById(android.R.id.content))
                .addView(floatingText);

        // Animate upward + fade
        floatingText.animate()
                .translationYBy(-100f)
                .alpha(0f)
                .setDuration(800)
                .withEndAction(() ->
                        ((android.view.ViewGroup) findViewById(android.R.id.content))
                                .removeView(floatingText)
                )
                .start();
    }
    private void showCoinEarnEffect(int amount) {

        TextView floatingText = new TextView(this);
        floatingText.setText("+" + amount);
        floatingText.setTextColor(Color.parseColor("#FFD700"));
        floatingText.setTextSize(18f);
        floatingText.setTypeface(null, android.graphics.Typeface.BOLD);

        int[] location = new int[2];
        coinText.getLocationOnScreen(location);

        floatingText.setX(location[0] + coinText.getWidth() / 2f);
        floatingText.setY(location[1] - 20);

        ((android.view.ViewGroup) findViewById(android.R.id.content))
                .addView(floatingText);

        floatingText.animate()
                .translationYBy(-100f)
                .alpha(0f)
                .setDuration(800)
                .withEndAction(() ->
                        ((android.view.ViewGroup) findViewById(android.R.id.content))
                                .removeView(floatingText)
                )
                .start();
    }


    private void animateCoins(int newCoins) {

        int startValue = Integer.parseInt(coinText.getText().toString());

        android.animation.ValueAnimator animator =
                android.animation.ValueAnimator.ofInt(startValue, newCoins);

        animator.setDuration(400);

        animator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            coinText.setText(String.valueOf(animatedValue));
        });

        animator.start();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundManager != null) {
            soundManager.release();
        }
    }


    @Override
    public void onBackPressed() {
        showQuitGameDialog();
    }

    private void showQuitGameDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Quit Game")
                .setMessage("Are you sure you want to quit the game?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(true)
                .setPositiveButton("Yes", (dialog, which) -> finish())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

}
