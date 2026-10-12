package com.abhay.blockblastify;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private CardView classicbtn, settingbtn, levelbtn, leaderboardBtn, playerProfileCard;
    private TextView levelBtnText, homePlayerName;
    private View dimBackground;

    private static final String PREFS_NAME = "BlockPuzzlePrefs";
    private static final String KEY_HIGH_SCORE = "high_score";
    private static final String KEY_COINS = "coins";
    private static final String KEY_MUSIC_ON = "music_on";
    private static final String KEY_SOUND_ON = "sound_on";

    private boolean doubleBackToExitPressedOnce = false;
    private SoundManager soundManager;
    private final Handler backPressHandler = new Handler();
    private final Runnable resetBackPress = () -> doubleBackToExitPressedOnce = false;

    private PopupWindow activePopupWindow = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        boolean isMusicOn = prefs.getBoolean(KEY_MUSIC_ON, true);
        if (isMusicOn) {
            MusicManager.startMusic(this);
        }

        classicbtn = findViewById(R.id.classicbtn);
        settingbtn = findViewById(R.id.settingbtn);
        levelbtn = findViewById(R.id.levelbtn);
        leaderboardBtn = findViewById(R.id.leaderboardBtn);
        playerProfileCard = findViewById(R.id.playerProfileCard);
        levelBtnText = findViewById(R.id.levelBtnText);
        homePlayerName = findViewById(R.id.homePlayerName);
        dimBackground = findViewById(R.id.dimBackground);

        soundManager = new SoundManager(this);
        boolean isSoundOn = prefs.getBoolean(KEY_SOUND_ON, true);
        soundManager.setSoundEnabled(isSoundOn);

        TextView highScoreText = findViewById(R.id.highScoreHome);
        TextView coinText = findViewById(R.id.coinstextHome);

        int highScore = prefs.getInt(KEY_HIGH_SCORE, 0);
        int coins = prefs.getInt(KEY_COINS, 0);
        highScoreText.setText(String.valueOf(highScore));
        coinText.setText(String.valueOf(coins));

        updateProfileAndLevelText();

        // Player Name Change button
        playerProfileCard.setOnClickListener(v -> {
            soundManager.playClick();
            animateButton(v);
            showEditNameDialog(null);
        });

        // 1. Classic Button
        classicbtn.setOnClickListener(v -> {
            soundManager.playClick();
            animateButton(v);
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            intent.putExtra(MainActivity.EXTRA_GAME_MODE, "classic");
            startActivity(intent);
        });

        // 2. Levels Button
        levelbtn.setOnClickListener(v -> {
            soundManager.playClick();
            animateButton(v);
            showLevelsDialog();
        });

        // 3. Leaderboard Button
        leaderboardBtn.setOnClickListener(v -> {
            soundManager.playClick();
            animateButton(v);
            showLeaderboardDialog();
        });

        // 4. Settings Button
        settingbtn.setOnClickListener(v -> {
            soundManager.playClick();
            animateButton(v);
            showSettingsDialog(prefs);
        });
    }

    private void updateProfileAndLevelText() {
        String name = LeaderboardManager.getPlayerName(this);
        homePlayerName.setText(name);

        int unlockedLevel = LevelManager.getUnlockedLevel(this);
        levelBtnText.setText("Levels (Level " + unlockedLevel + ")");
    }

    private void showLevelsDialog() {
        View popupView = getLayoutInflater().inflate(R.layout.dialog_levels, null);
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.92);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                width,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );
        activePopupWindow = popupWindow;

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setElevation(16);
        popupWindow.setOutsideTouchable(true);

        dimBackground.setAlpha(0f);
        dimBackground.setVisibility(View.VISIBLE);
        dimBackground.animate().alpha(1f).setDuration(200);

        popupWindow.setOnDismissListener(() -> {
            activePopupWindow = null;
            dimBackground.animate().alpha(0f).setDuration(200)
                    .withEndAction(() -> dimBackground.setVisibility(View.GONE));
        });

        TextView subtitle = popupView.findViewById(R.id.levelsProgressSubtitle);
        int unlockedLevel = LevelManager.getUnlockedLevel(this);
        subtitle.setText("Unlocked: Level " + unlockedLevel + " / " + LevelManager.TOTAL_LEVELS);

        popupView.findViewById(R.id.levelsCloseBtn).setOnClickListener(v -> {
            soundManager.playClick();
            popupWindow.dismiss();
        });

        RecyclerView levelsRecycler = popupView.findViewById(R.id.levelsRecyclerView);
        levelsRecycler.setLayoutManager(new GridLayoutManager(this, 4));

        LevelsAdapter adapter = new LevelsAdapter(this, level -> {
            soundManager.playClick();
            popupWindow.dismiss();
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            intent.putExtra(MainActivity.EXTRA_GAME_MODE, "level");
            intent.putExtra(MainActivity.EXTRA_LEVEL, level);
            startActivity(intent);
        });

        levelsRecycler.setAdapter(adapter);
        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
    }

    private void showLeaderboardDialog() {
        View popupView = getLayoutInflater().inflate(R.layout.dialog_leaderboard, null);
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.92);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                width,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );
        activePopupWindow = popupWindow;

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setElevation(16);
        popupWindow.setOutsideTouchable(true);

        dimBackground.setAlpha(0f);
        dimBackground.setVisibility(View.VISIBLE);
        dimBackground.animate().alpha(1f).setDuration(200);

        popupWindow.setOnDismissListener(() -> {
            activePopupWindow = null;
            dimBackground.animate().alpha(0f).setDuration(200)
                    .withEndAction(() -> dimBackground.setVisibility(View.GONE));
        });

        popupView.findViewById(R.id.leaderboardCloseBtn).setOnClickListener(v -> {
            soundManager.playClick();
            popupWindow.dismiss();
        });

        TextView myRankBadge = popupView.findViewById(R.id.myRankBadge);
        TextView myPlayerName = popupView.findViewById(R.id.myPlayerName);
        TextView myScoreText = popupView.findViewById(R.id.myScoreText);
        View btnChangeName = popupView.findViewById(R.id.btnChangeName);
        RecyclerView recyclerView = popupView.findViewById(R.id.leaderboardRecyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<LeaderboardEntry> list = LeaderboardManager.getLeaderboard(this);
        LeaderboardAdapter adapter = new LeaderboardAdapter(this, list);
        recyclerView.setAdapter(adapter);

        // Find user's rank in list
        updateUserStickyCard(list, myRankBadge, myPlayerName, myScoreText);

        btnChangeName.setOnClickListener(v -> {
            soundManager.playClick();
            showEditNameDialog(() -> {
                // Refresh leaderboard list on name change
                List<LeaderboardEntry> updatedList = LeaderboardManager.getLeaderboard(HomeActivity.this);
                adapter.updateData(updatedList);
                updateUserStickyCard(updatedList, myRankBadge, myPlayerName, myScoreText);
            });
        });

        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
    }

    private void updateUserStickyCard(List<LeaderboardEntry> list, TextView rankBadge, TextView nameText, TextView scoreText) {
        String currentName = LeaderboardManager.getPlayerName(this);
        nameText.setText(currentName + " (You)");

        int userRank = -1;
        int userScore = 0;
        for (LeaderboardEntry entry : list) {
            if (entry.isUser()) {
                userRank = entry.getRank();
                userScore = entry.getScore();
                break;
            }
        }

        if (userRank > 0) {
            rankBadge.setText("#" + userRank);
            NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
            scoreText.setText("Best Score: " + nf.format(userScore) + " pts");
        } else {
            rankBadge.setText("#--");
            scoreText.setText("Play Classic to rank!");
        }
    }

    private void showEditNameDialog(Runnable onNameSaved) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_name, null);
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.88);

        PopupWindow popupWindow = new PopupWindow(
                dialogView,
                width,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setElevation(20);
        popupWindow.setOutsideTouchable(true);

        EditText nameInput = dialogView.findViewById(R.id.editPlayerNameInput);
        nameInput.setText(LeaderboardManager.getPlayerName(this));
        nameInput.selectAll();

        dialogView.findViewById(R.id.btnCancelName).setOnClickListener(v -> {
            soundManager.playClick();
            popupWindow.dismiss();
        });

        dialogView.findViewById(R.id.btnSaveName).setOnClickListener(v -> {
            soundManager.playClick();
            String newName = nameInput.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(HomeActivity.this, "Please enter a valid name", Toast.LENGTH_SHORT).show();
                return;
            }

            LeaderboardManager.setPlayerName(HomeActivity.this, newName);
            homePlayerName.setText(newName);
            Toast.makeText(HomeActivity.this, "Name updated to " + newName + " 🎉", Toast.LENGTH_SHORT).show();
            popupWindow.dismiss();

            if (onNameSaved != null) {
                onNameSaved.run();
            }
        });

        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
    }

    private void showSettingsDialog(SharedPreferences prefs) {
        View popupView = getLayoutInflater().inflate(R.layout.settings_pannel, null);

        ImageView musicIcon = popupView.findViewById(R.id.musicIcon);
        ImageView soundIcon = popupView.findViewById(R.id.soundIcon);

        boolean isSoundOn = prefs.getBoolean(KEY_SOUND_ON, true);
        soundIcon.setImageResource(isSoundOn ? R.drawable.soundon : R.drawable.soundoff);

        boolean currentMusicState = prefs.getBoolean(KEY_MUSIC_ON, true);
        musicIcon.setImageResource(currentMusicState ? R.drawable.musicon : R.drawable.musicoff);

        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                width,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );
        activePopupWindow = popupWindow;

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setElevation(10);
        popupWindow.setOutsideTouchable(true);

        dimBackground.setAlpha(0f);
        dimBackground.setVisibility(View.VISIBLE);
        dimBackground.animate().alpha(1f).setDuration(200);

        popupWindow.setOnDismissListener(() -> {
            activePopupWindow = null;
            dimBackground.animate().alpha(0f).setDuration(200)
                    .withEndAction(() -> dimBackground.setVisibility(View.GONE));
        });

        popupView.findViewById(R.id.closeButton).setOnClickListener(view -> {
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
                    MusicManager.startMusic(HomeActivity.this);
                    musicIcon.setImageResource(R.drawable.musicon);
                    Toast.makeText(HomeActivity.this, "Music ON 🎵", Toast.LENGTH_SHORT).show();
                } else {
                    MusicManager.pauseMusic();
                    musicIcon.setImageResource(R.drawable.musicoff);
                    Toast.makeText(HomeActivity.this, "Music OFF 🔇", Toast.LENGTH_SHORT).show();
                }
            } else if (id == R.id.soundlinearlayout) {
                soundManager.playClick();
                boolean currentState = prefs.getBoolean(KEY_SOUND_ON, true);
                boolean newState = !currentState;
                prefs.edit().putBoolean(KEY_SOUND_ON, newState).apply();

                soundManager.setSoundEnabled(newState);
                if (newState) {
                    soundIcon.setImageResource(R.drawable.soundon);
                    Toast.makeText(HomeActivity.this, "Sound ON 🔊", Toast.LENGTH_SHORT).show();
                } else {
                    soundIcon.setImageResource(R.drawable.soundoff);
                    Toast.makeText(HomeActivity.this, "Sound OFF 🔇", Toast.LENGTH_SHORT).show();
                }
            } else if (id == R.id.sharelinearlayout) {
                String shareMessage = "🎮 I'm playing this amazing Block Puzzle game!\n\n" +
                        "Can you beat my score? 😎🔥\n\n" +
                        "Download now:\n" +
                        "https://play.google.com/store/apps/details?id=" + getPackageName();

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                startActivity(Intent.createChooser(shareIntent, "Share via"));
            } else if (id == R.id.rateuslinearlayout) {
                String packageName = getPackageName();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=" + packageName)));
                } catch (android.content.ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
                }
            }
        };

        popupView.findViewById(R.id.musiclinearlayout).setOnClickListener(menuClickListener);
        popupView.findViewById(R.id.soundlinearlayout).setOnClickListener(menuClickListener);
        popupView.findViewById(R.id.sharelinearlayout).setOnClickListener(menuClickListener);
        popupView.findViewById(R.id.rateuslinearlayout).setOnClickListener(menuClickListener);

        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
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

        updateProfileAndLevelText();
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
        if (activePopupWindow != null && activePopupWindow.isShowing()) {
            activePopupWindow.dismiss();
            return;
        }

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