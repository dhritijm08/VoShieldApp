package com.example.voshield;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ImageView appLogo;
    private TextView appName, tagline;
    private SharedPreferences prefs;

    private static final int SPLASH_DURATION = 1500; // 1.5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        startSplashAnimations();
        goToNextScreenWithDelay();
    }

    private void initViews() {
        appLogo = findViewById(R.id.appLogo);
        appName = findViewById(R.id.appName);
        tagline = findViewById(R.id.tagline);
        prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
    }

    private void startSplashAnimations() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in);
        appLogo.startAnimation(fadeIn);
        appName.startAnimation(fadeIn);
        tagline.startAnimation(fadeIn);
    }

    private void goToNextScreenWithDelay() {
        new Handler().postDelayed(() -> {
            Intent intent = prefs.getBoolean("isLoggedIn", false)
                    ? new Intent(this, MenuActivity.class)
                    : new Intent(this, SignUpActivity.class);

            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, SPLASH_DURATION);
    }
}