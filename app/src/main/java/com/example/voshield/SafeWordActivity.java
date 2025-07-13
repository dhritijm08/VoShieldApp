package com.example.voshield;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SafeWordActivity extends AppCompatActivity {

    private EditText safeWordInput;
    private TextView voiceRecognitionTitle;
    private SharedPreferences sharedPreferences;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safe_word);

        // Initialize views
        safeWordInput = findViewById(R.id.safeWordInput);
        Button confirmButton = findViewById(R.id.confirm_button);
        Button backButton = findViewById(R.id.back_button);
        voiceRecognitionTitle = findViewById(R.id.voice_recognition_title);

        // SharedPreferences for app-wide use
        sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);

        // Show existing safe word if it exists
        String currentSafeWord = sharedPreferences.getString("safe_word", "");
        if (!currentSafeWord.isEmpty()) {
            safeWordInput.setText(currentSafeWord);
        }

        // Save the safe word
        confirmButton.setOnClickListener(v -> {
            String safeWord = safeWordInput.getText().toString().trim();
            if (!safeWord.isEmpty()) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("safe_word", safeWord.toLowerCase()); // Normalize
                boolean saved = editor.commit();

                if (saved) {
                    Log.d("SafeWord", "Safe word saved: " + safeWord);
                    safeWordInput.setEnabled(false);
                    voiceRecognitionTitle.setText("Safe word set successfully!");
                    Toast.makeText(this, "Safe word saved!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to save safe word", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please enter a safe word", Toast.LENGTH_SHORT).show();
            }
        });

        backButton.setOnClickListener(v -> {
            startActivity(new Intent(SafeWordActivity.this, MenuActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });
    }
}