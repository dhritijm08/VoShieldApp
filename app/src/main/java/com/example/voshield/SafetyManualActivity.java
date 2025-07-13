package com.example.voshield;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class SafetyManualActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safety_manual);

        Button backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> {
            v.setAlpha(0.6f); // subtle click effect
            v.postDelayed(() -> v.setAlpha(1f), 200);

            Intent intent = new Intent(SafetyManualActivity.this, MenuActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            finish(); // Optional: removes current activity from back stack
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}