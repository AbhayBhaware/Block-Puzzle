package com.abhay.blockblastify;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_GAME_MODE = "EXTRA_GAME_MODE";
    public static final String EXTRA_LEVEL = "EXTRA_LEVEL";

    private String gameMode = "classic";
    private int currentLevel = 1;
    private int targetScore = 0;
    private boolean isLevelCompleted = false;

    private GameView gameView;
    private TextView scoreText;
    private TextView modeBadgeText;
    private LinearLayout levelProgressLayout;
    private TextView levelTargetText;
    private TextView levelPercentText;
    private ProgressBar levelProgressBar;

    private CardView menubtn;
    private View dimBackground;

    private static final String PREFS_NAME = "BlockPuzzlePrefs";
    private static final String KEY_HIGH_SCORE = "high_score";
    private static final String KEY_COINS = "coins";
    private static final String KEY_MUSIC_ON = "music_on";
    private static final String KEY_SOUND_ON = "sound_on";

    private TextView coinText;
    private int coins;
    private SharedPreferences prefs;
    private int highScore;

    private int displayedScore = 0;
    private android.animation.ValueAnimator scoreAnimator;
    private SoundManager soundManager;

    private AdView mAdView;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Intent extras
        Intent intent = getIntent();
        if (intent != null) {
            String mode = intent.getStringExtra(EXTRA_GAME_MODE);
            if (mode != null) {
                gameMode = mode;
            }
            currentLevel = intent.getIntExtra(EXTRA_LEVEL, 1);
        }

        // Initialize Banner Ad
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // Load Interstitial Ad
        loadInterstitialAd();

        gameView = findViewById(R.id.gameView);
        scoreText = findViewById(R.id.scoreText);
        modeBadgeText = findViewById(R.id.modeBadgeText);
        levelProgressLayout = findViewById(R.id.levelProgressLayout);
        levelTargetText = findViewById(R.id.levelTargetText);
        levelPercentText = findViewById(R.id.levelPercentText);
        levelProgressBar = findViewById(R.id.levelProgressBar);
        menubtn = findViewById(R.id.menuButton);
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

        // Configure Game Mode (Classic vs Level)
        if ("level".equalsIgnoreCase(gameMode)) {
            modeBadgeText.setText("LEVEL " + currentLevel);
            levelProgressLayout.setVisibility(View.VISIBLE);
            targetScore = LevelManager.getTargetScore(currentLevel);
            int obstacleCount = LevelManager.getObstacleCount(currentLevel);

            highScoreText.setText("Goal: " + targetScore);
            levelTargetText.setText("Target: " + targetScore);
            levelPercentText.setText("0%");
            levelProgressBar.setProgress(0);

            gameView.initLevel(currentLevel, obstacleCount);
        } else {
            modeBadgeText.setText("CLASSIC");
            levelProgressLayout.setVisibility(View.GONE);
            highScoreText.setText(String.valueOf(highScore));
            gameView.setLevelMode(false, 1);
        }

        findViewById(R.id.btnBackMain).setOnClickListener(v -> {
            soundManager.playClick();
            showQuitGameDialog();
        });

        gameView.setCoinListener(earnedCoins -> {
            coins += earnedCoins;
            prefs.edit().putInt(KEY_COINS, coins).apply();
            animateCoins(coins);
            showCoinEarnEffect(earnedCoins);
        });

        menubtn.setOnClickListener(v -> {
            soundManager.playClick();
            popupmenu();
        });

        gameView.setGameOverListener(score -> {
            if (isLevelCompleted) return;

            // Participation reward
            coins += 5;

            if ("classic".equalsIgnoreCase(gameMode) && score > highScore) {
                highScore = score;
                prefs.edit().putInt(KEY_HIGH_SCORE, score).apply();
                coins += 20; // bonus
                Toast.makeText(this, "New High Score! +20 coins", Toast.LENGTH_SHORT).show();
            }

            prefs.edit().putInt(KEY_COINS, coins).apply();
            coinText.setText(String.valueOf(coins));
            if ("classic".equalsIgnoreCase(gameMode)) {
                highScoreText.setText(String.valueOf(highScore));
            }

            showGameOverPopup(score);
        });

        gameView.setScoreListener(new GameView.ScoreListener() {
            @Override
            public void onScoreChanged(int newScore) {
                // Cancel previous animation if running
                if (scoreAnimator != null && scoreAnimator.isRunning()) {
                    scoreAnimator.cancel();
                }

                scoreAnimator = android.animation.ValueAnimator.ofInt(displayedScore, newScore);
                scoreAnimator.setDuration(350);
                scoreAnimator.addUpdateListener(animation -> {
                    displayedScore = (int) animation.getAnimatedValue();
                    scoreText.setText("Score: " + displayedScore);
                });
                scoreAnimator.start();

                // Check Level Win Condition
                if ("level".equalsIgnoreCase(gameMode)) {
                    updateLevelProgress(newScore);

                    if (newScore >= targetScore && !isLevelCompleted) {
                        isLevelCompleted = true;
                        int rewardCoins = LevelManager.getRewardCoins(currentLevel);
                        coins += rewardCoins;
                        prefs.edit().putInt(KEY_COINS, coins).apply();
                        LevelManager.completeLevel(MainActivity.this, currentLevel);

                        soundManager.playCoinEarn();
                        showLevelClearedPopup(currentLevel, rewardCoins);
                    }
                }
            }
        });
    }

    private void updateLevelProgress(int currentScore) {
        if (targetScore <= 0) return;
        int percent = Math.min(100, (int) ((currentScore / (float) targetScore) * 100));
        levelProgressBar.setProgress(percent);
        levelPercentText.setText(percent + "%");
    }

    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, "ca-app-pub-9423620095795922/7175160232", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        mInterstitialAd = null;
                    }
                });
    }

    private void restartGame() {
        recreate();
    }

    public void popupmenu() {
        View popupView = getLayoutInflater().inflate(R.layout.menu_pannel, null);
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        ImageView musicIcon = popupView.findViewById(R.id.musicIcon);
        ImageView soundIcon = popupView.findViewById(R.id.soundIcon);

        boolean isSoundOn = prefs.getBoolean(KEY_SOUND_ON, true);
        soundIcon.setImageResource(isSoundOn ? R.drawable.soundon : R.drawable.soundoff);

        boolean isMusicOn = prefs.getBoolean(KEY_MUSIC_ON, true);
        musicIcon.setImageResource(isMusicOn ? R.drawable.musicon : R.drawable.musicoff);

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

        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);

        popupWindow.setOnDismissListener(() -> {
            dimBackground.animate().alpha(0f).setDuration(200)
                    .withEndAction(() -> dimBackground.setVisibility(View.GONE));
        });

        popupView.findViewById(R.id.closeButton).setOnClickListener(v -> {
            soundManager.playClick();
            popupWindow.dismiss();
        });

        View.OnClickListener menuClickListener = view -> {
            int id = view.getId();

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
            } else if (id == R.id.restartlinearlayout) {
                Toast.makeText(MainActivity.this, "Game Restarted", Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
                restartGame();
            }
        };

        popupView.findViewById(R.id.musiclinearlayout).setOnClickListener(menuClickListener);
        popupView.findViewById(R.id.soundlinearlayout).setOnClickListener(menuClickListener);
        popupView.findViewById(R.id.homelinearlayout).setOnClickListener(menuClickListener);
        popupView.findViewById(R.id.restartlinearlayout).setOnClickListener(menuClickListener);
    }

    private void showGameOverPopup(int finalScore) {
        View popupView = getLayoutInflater().inflate(R.layout.gameover_pannel, null);
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.88);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                width,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                false
        );

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setElevation(24);
        popupWindow.setOutsideTouchable(false);

        dimBackground.setAlpha(0f);
        dimBackground.setVisibility(View.VISIBLE);
        dimBackground.animate().alpha(1f).setDuration(250);

        TextView gameOverTitle = popupView.findViewById(R.id.gameOverTitle);
        TextView scoreValue = popupView.findViewById(R.id.gameOverScoreValue);
        TextView coinsValue = popupView.findViewById(R.id.gameOverCoinsValue);
        CardView continueBtn = popupView.findViewById(R.id.continuebtn);
        CardView watchAdBtn = popupView.findViewById(R.id.watchAdBtn);
        CardView restartBtn = popupView.findViewById(R.id.restartBtn);

        if ("level".equalsIgnoreCase(gameMode)) {
            gameOverTitle.setText("LEVEL FAILED");
        } else {
            gameOverTitle.setText("GAME OVER");
        }

        scoreValue.setText(String.valueOf(finalScore));
        coinsValue.setText(String.valueOf(coins));

        // 1. Continue with 200 coins
        continueBtn.setOnClickListener(v -> {
            soundManager.playClick();
            if (coins >= 200) {
                coins -= 200;
                prefs.edit().putInt(KEY_COINS, coins).apply();

                animateCoins(coins);
                showCoinDeductionEffect(200);
                soundManager.playCoinSpend();

                popupWindow.dismiss();
                dimBackground.setVisibility(View.GONE);

                // Revive from where user lost!
                gameView.performRevive();
            } else {
                Toast.makeText(this, "Need 200 coins! Watch an ad to continue for free 🎬", Toast.LENGTH_SHORT).show();
            }
        });

        // 2. Watch Ad to Continue (Free Revive)
        watchAdBtn.setOnClickListener(v -> {
            soundManager.playClick();
            if (mInterstitialAd != null) {
                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        mInterstitialAd = null;
                        loadInterstitialAd();
                        popupWindow.dismiss();
                        dimBackground.setVisibility(View.GONE);
                        gameView.performRevive();
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        mInterstitialAd = null;
                        loadInterstitialAd();
                        popupWindow.dismiss();
                        dimBackground.setVisibility(View.GONE);
                        gameView.performRevive();
                    }
                });
                mInterstitialAd.show(MainActivity.this);
            } else {
                Toast.makeText(this, "Resuming game...", Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
                dimBackground.setVisibility(View.GONE);
                gameView.performRevive();
                loadInterstitialAd();
            }
        });

        // 3. Restart Game (or Retry Level)
        restartBtn.setOnClickListener(v -> {
            soundManager.playClick();
            popupWindow.dismiss();
            dimBackground.setVisibility(View.GONE);

            // Show interstitial ad on game over restart if available
            if (mInterstitialAd != null) {
                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        mInterstitialAd = null;
                        restartGame();
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        mInterstitialAd = null;
                        restartGame();
                    }
                });
                mInterstitialAd.show(MainActivity.this);
            } else {
                restartGame();
            }
        });

        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
    }

    private void showLevelClearedPopup(int level, int coinsEarned) {
        View popupView = getLayoutInflater().inflate(R.layout.dialog_level_cleared, null);
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.88);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                width,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                false
        );

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setElevation(24);
        popupWindow.setOutsideTouchable(false);

        dimBackground.setAlpha(0f);
        dimBackground.setVisibility(View.VISIBLE);
        dimBackground.animate().alpha(1f).setDuration(250);

        TextView subtitle = popupView.findViewById(R.id.levelClearedSubtitle);
        TextView rewardText = popupView.findViewById(R.id.levelRewardCoinsText);
        View btnNext = popupView.findViewById(R.id.btnNextLevel);
        View btnReplay = popupView.findViewById(R.id.btnReplayLevel);
        View btnHome = popupView.findViewById(R.id.btnHomeFromLevel);

        subtitle.setText("Level " + level + " Complete! 🌟");
        rewardText.setText("+" + coinsEarned + " COINS");

        btnNext.setOnClickListener(v -> {
            soundManager.playClick();
            popupWindow.dismiss();
            dimBackground.setVisibility(View.GONE);

            if (level < LevelManager.TOTAL_LEVELS) {
                Intent nextIntent = new Intent(MainActivity.this, MainActivity.class);
                nextIntent.putExtra(EXTRA_GAME_MODE, "level");
                nextIntent.putExtra(EXTRA_LEVEL, level + 1);
                startActivity(nextIntent);
                finish();
            } else {
                Toast.makeText(MainActivity.this, "🎉 You have completed all levels! You are a master!", Toast.LENGTH_LONG).show();
                finish();
            }
        });

        btnReplay.setOnClickListener(v -> {
            soundManager.playClick();
            popupWindow.dismiss();
            dimBackground.setVisibility(View.GONE);
            restartGame();
        });

        btnHome.setOnClickListener(v -> {
            soundManager.playClick();
            popupWindow.dismiss();
            dimBackground.setVisibility(View.GONE);
            finish();
        });

        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
    }

    private void showCoinDeductionEffect(int amount) {
        TextView floatingText = new TextView(this);
        floatingText.setText("-" + amount);
        floatingText.setTextColor(Color.RED);
        floatingText.setTextSize(18f);
        floatingText.setTypeface(null, android.graphics.Typeface.BOLD);

        int[] location = new int[2];
        coinText.getLocationOnScreen(location);

        floatingText.setX(location[0] + coinText.getWidth() / 2f);
        floatingText.setY(location[1] - 20);

        ((android.view.ViewGroup) findViewById(android.R.id.content)).addView(floatingText);

        floatingText.animate()
                .translationYBy(-100f)
                .alpha(0f)
                .setDuration(800)
                .withEndAction(() -> ((android.view.ViewGroup) findViewById(android.R.id.content)).removeView(floatingText))
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

        ((android.view.ViewGroup) findViewById(android.R.id.content)).addView(floatingText);

        floatingText.animate()
                .translationYBy(-100f)
                .alpha(0f)
                .setDuration(800)
                .withEndAction(() -> ((android.view.ViewGroup) findViewById(android.R.id.content)).removeView(floatingText))
                .start();
    }

    private void animateCoins(int newCoins) {
        int startValue = 0;
        try {
            startValue = Integer.parseInt(coinText.getText().toString());
        } catch (Exception ignored) {}

        android.animation.ValueAnimator animator = android.animation.ValueAnimator.ofInt(startValue, newCoins);
        animator.setDuration(400);
        animator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            coinText.setText(String.valueOf(animatedValue));
        });
        animator.start();
    }

    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
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
                .setMessage("Are you sure you want to quit?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(true)
                .setPositiveButton("Yes", (dialog, which) -> finish())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
