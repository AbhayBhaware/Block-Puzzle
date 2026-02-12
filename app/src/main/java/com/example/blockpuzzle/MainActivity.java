package com.example.blockpuzzle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
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
        getWindow().setNavigationBarColor(Color.parseColor("#7DBAEA"));


        setContentView(R.layout.activity_main);

        gameView = findViewById(R.id.gameView);
        scoreText = findViewById(R.id.scoreText);
        menubtn=findViewById(R.id.menuButton);
        dimBackground = findViewById(R.id.dimBackground);
        soundManager = new SoundManager(this);




        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        coinText = findViewById(R.id.coinText);
        coins = prefs.getInt(KEY_COINS, 0);
        coinText.setText(String.valueOf(coins));


        TextView highScoreText = findViewById(R.id.highScoreText);
        highScore = prefs.getInt(KEY_HIGH_SCORE, 0);
        highScoreText.setText(String.valueOf(highScore));


        gameView.setCoinListener(earnedCoins -> {
            coins += earnedCoins;
            coinText.setText(String.valueOf(coins));

            prefs.edit().putInt(KEY_COINS, coins).apply();
        });



        menubtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

                PopupWindow popupWindow=new PopupWindow(
                        popupView,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        false
                );

                popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                popupWindow.setElevation(10);

        //getWindow().setStatusBarColor(Color.TRANSPARENT);
        //getWindow().setNavigationBarColor(Color.TRANSPARENT);


        dimBackground.setAlpha(0f);
        dimBackground.setVisibility(View.VISIBLE);
        dimBackground.animate().alpha(1f).setDuration(200);

        dimBackground.setOnClickListener(view -> {
            // Do nothing – just block outside touches
        });

                int xoffset=(int)(getResources().getDisplayMetrics().density * -75);
                popupWindow.showAtLocation(
                        findViewById(android.R.id.content),
                        Gravity.CENTER,
                        0,
                        0
                );

        popupView.findViewById(R.id.closeButton).setOnClickListener(v -> {
            popupWindow.dismiss();

            dimBackground.animate().alpha(0f).setDuration(200)
                    .withEndAction(() -> dimBackground.setVisibility(View.GONE));

            getWindow().setStatusBarColor(Color.parseColor("#7DBAEA"));
            getWindow().setNavigationBarColor(Color.parseColor("#7DBAEA"));
        });

                View.OnClickListener menuClickListner = view-> {
                    int id=view.getId();

                    if (id==R.id.musiclinearlayout)
                    {
                        Toast.makeText(MainActivity.this, "Music clicked", Toast.LENGTH_SHORT).show();
                    } else if (id==R.id.soundlinearlayout)
                    {
                        Toast.makeText(MainActivity.this, "Sound Clicked", Toast.LENGTH_SHORT).show();
                    } else if (id==R.id.homelinearlayout)
                    {
                        Toast.makeText(MainActivity.this, "Home Clicked", Toast.LENGTH_SHORT).show();
                    } else if (id==R.id.restartlinearlayout)
                    {
                        Toast.makeText(MainActivity.this, "Restart clicked", Toast.LENGTH_SHORT).show();
                    }

                };
                popupView.findViewById(R.id.musiclinearlayout).setOnClickListener(menuClickListner);
                popupView.findViewById(R.id.soundlinearlayout).setOnClickListener(menuClickListner);
                popupView.findViewById(R.id.homelinearlayout).setOnClickListener(menuClickListner);
                popupView.findViewById(R.id.restartlinearlayout).setOnClickListener(menuClickListner);

    }

    private void showGameOverPopup() {

        View popupView = getLayoutInflater().inflate(R.layout.gameover_pannel, null);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
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
            popupWindow.dismiss();
            dimBackground.setVisibility(View.GONE);
            restartGame();
        });

        continueBtn.setOnClickListener(v -> {
            if (coins>=200)
            {
                int oldCoins = coins;
                coins -= 200;

                prefs.edit().putInt(KEY_COINS, coins).apply();

                animateCoins(coins);  // smooth counting
                showCoinDeductionEffect(200); // floating -200
                soundManager.playCoinSpend(); // coin sound


                popupWindow.dismiss();
                dimBackground.setVisibility(View.GONE);

                gameView.resumeGame();

                gameView.generateBlocks();

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

    private void animateCoins(int newCoins) {

        android.animation.ValueAnimator animator =
                android.animation.ValueAnimator.ofInt(coins, newCoins);

        animator.setDuration(400);

        animator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            coinText.setText(String.valueOf(animatedValue));
        });

        animator.start();
    }





}