package com.example.blockpuzzle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameView = findViewById(R.id.gameView);
        scoreText = findViewById(R.id.scoreText);
        menubtn=findViewById(R.id.menuButton);
        dimBackground = findViewById(R.id.dimBackground);

        menubtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupmenu();
            }
        });

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

    public void popupmenu()
    {

        View popupView=getLayoutInflater().inflate(R.layout.menu_pannel,null);

                PopupWindow popupWindow=new PopupWindow(
                        popupView,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        true
                );

                popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                popupWindow.setElevation(10);

        dimBackground.setAlpha(0f);
        dimBackground.setVisibility(View.VISIBLE);
        dimBackground.animate().alpha(1f).setDuration(200);

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
                    popupWindow.dismiss();

                    dimBackground.animate().alpha(0f).setDuration(200)
                            .withEndAction(() -> dimBackground.setVisibility(View.GONE));

                };
                popupView.findViewById(R.id.musiclinearlayout).setOnClickListener(menuClickListner);
                popupView.findViewById(R.id.soundlinearlayout).setOnClickListener(menuClickListner);
                popupView.findViewById(R.id.homelinearlayout).setOnClickListener(menuClickListner);
                popupView.findViewById(R.id.restartlinearlayout).setOnClickListener(menuClickListner);

    }

}