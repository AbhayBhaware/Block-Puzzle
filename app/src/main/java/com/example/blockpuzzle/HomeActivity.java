package com.example.blockpuzzle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

public class HomeActivity extends AppCompatActivity {

    CardView classicbtn, settingbtn, levelbtn;

    View dimBackground;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        classicbtn=findViewById(R.id.classicbtn);
        settingbtn=findViewById(R.id.settingbtn);
        levelbtn=findViewById(R.id.levelbtn);
        dimBackground=findViewById(R.id.dimBackground);

        classicbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateButton(v);
                Intent i=new Intent(HomeActivity.this, MainActivity.class);
                startActivity(i);
            }
        });

        settingbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateButton(v);
                View popupView=getLayoutInflater().inflate(R.layout.settings_pannel,null);

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

                popupView.findViewById(R.id.closeButton).setOnClickListener(view -> {
                    popupWindow.dismiss();

                    dimBackground.animate().alpha(0f).setDuration(200)
                            .withEndAction(() -> dimBackground.setVisibility(View.GONE));
                });


                View.OnClickListener menuClickListner = view-> {
                    int id=view.getId();

                    if (id==R.id.musiclinearlayout)
                    {
                        Toast.makeText(HomeActivity.this, "Music clicked", Toast.LENGTH_SHORT).show();
                    } else if (id==R.id.soundlinearlayout)
                    {
                        Toast.makeText(HomeActivity.this, "Sound Clicked", Toast.LENGTH_SHORT).show();
                    } else if (id==R.id.sharelinearlayout)
                    {
                        Toast.makeText(HomeActivity.this, "Share Clicked", Toast.LENGTH_SHORT).show();
                    } else if (id==R.id.rateuslinearlayout)
                    {
                        Toast.makeText(HomeActivity.this, "rate us clicked", Toast.LENGTH_SHORT).show();
                    }
                    popupWindow.dismiss();

                    dimBackground.animate().alpha(0f).setDuration(200)
                            .withEndAction(() -> dimBackground.setVisibility(View.GONE));
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
                animateButton(v);
                Toast.makeText(HomeActivity.this, "Coming soon...", Toast.LENGTH_SHORT).show();
            }
        });



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

}