package com.example.blockpuzzle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    GameView gameView;
    TextView scoreText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameView = findViewById(R.id.gameView);
        scoreText = findViewById(R.id.scoreText);

        gameView.setGameOverListener(score -> {
            new AlertDialog.Builder(this)
                    .setTitle("Game Over")
                    .setMessage("Final Score: " + score)
                    .setCancelable(false)
                    .setPositiveButton("Restart", (d, w) -> restartGame())
                    .show();
        });

        gameView.setScoreListener(new GameView.ScoreListener() {
            @Override
            public void onScoreChanged(int score) {
                scoreText.setText("Score: "+score);
            }
        });

    }

    private void restartGame() {
        recreate();
    }

}