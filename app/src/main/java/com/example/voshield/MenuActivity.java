package com.example.voshield;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.telephony.SmsManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class MenuActivity extends AppCompatActivity {

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 100;
    private static final int SMS_PERMISSION_REQUEST_CODE = 101;
    private SharedPreferences preferences;
    private Button btnVoiceRecognition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        preferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);

        if (!preferences.getBoolean("isLoggedIn", false)) {
            startActivity(new Intent(this, LoginActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
            return;
        }

        btnVoiceRecognition = findViewById(R.id.btnVoiceRecognition);
        Button btnEmergencyContact = findViewById(R.id.btnEmergencyContact);
        Button btnSafeWord = findViewById(R.id.btnSafeWord);
        Button btnSafetyInstructions = findViewById(R.id.btnSafetyInstructions);
        Button btnProfile = findViewById(R.id.btnProfile);

        btnVoiceRecognition.setOnClickListener(v -> startVoiceRecognition());

        btnEmergencyContact.setOnClickListener(v -> {
            startActivity(new Intent(this, EmergencyContactActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        btnSafeWord.setOnClickListener(v -> {
            startActivity(new Intent(this, SafeWordActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        btnSafetyInstructions.setOnClickListener(v -> {
            startActivity(new Intent(this, SafetyManualActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        btnProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your safe word...");

        try {
            startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
        } catch (Exception e) {
            Snackbar.make(findViewById(android.R.id.content), "Voice recognition not supported", Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            handleVoiceResult(data);
        }
    }

    private void handleVoiceResult(Intent data) {
        ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        if (result != null && !result.isEmpty()) {
            String spokenWord = result.get(0).toLowerCase().trim();
            String savedSafeWord = preferences.getString("safe_word", "").toLowerCase().trim();

            if (spokenWord.equals(savedSafeWord)) {
                checkEmergencyContacts();
            } else {
                Snackbar.make(findViewById(android.R.id.content), "Safe word mismatch.", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void checkEmergencyContacts() {
        Set<String> contacts = preferences.getStringSet("emergency_contacts", new HashSet<>());
        if (contacts.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), "No emergency contacts set.", Snackbar.LENGTH_LONG).show();
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        SMS_PERMISSION_REQUEST_CODE);
            } else {
                sendEmergencyMessages(contacts);
            }
        }
    }

    private void sendEmergencyMessages(Set<String> contacts) {
        String message = "🚨 VoShield App: Emergency Alert! This contact needs help.";
        SmsManager smsManager = SmsManager.getDefault();

        for (String contact : contacts) {
            String[] parts = contact.split(" - ");
            if (parts.length >= 2) {
                String number = parts[1].trim();
                try {
                    smsManager.sendTextMessage(number, null, message, null, null);
                } catch (Exception e) {
                    Snackbar.make(findViewById(android.R.id.content), "Failed to send to: " + number, Snackbar.LENGTH_SHORT).show();
                }
            }
        }

        Snackbar.make(findViewById(android.R.id.content), "Emergency alert sent", Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_REQUEST_CODE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkEmergencyContacts();
            } else {
                Snackbar.make(findViewById(android.R.id.content), "SMS permission required to send alerts", Snackbar.LENGTH_LONG).show();
            }
        }
    }
}