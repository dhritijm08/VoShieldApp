package com.example.voshield;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profileImagePreview;
    private TextView editName, editEmail, appVersion;
    private Button logoutButton, btnBack, btnEdit;
    private SharedPreferences preferences;
    private Uri selectedImageUri = null;

    private final ActivityResultLauncher<Intent> editLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadUserData(); // Refresh after edit
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        preferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        initializeViews();
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData(); // Reload on resume
    }

    private void initializeViews() {
        profileImagePreview = findViewById(R.id.profileImagePreview);
        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        appVersion = findViewById(R.id.appVersion);
        logoutButton = findViewById(R.id.logoutButton);
        btnBack = findViewById(R.id.btnBack);
        btnEdit = findViewById(R.id.btnEdit);
    }

    private void setupClickListeners() {
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditActivity.class);
            editLauncher.launch(intent);
        });

        logoutButton.setOnClickListener(v -> showLogoutDialog());

        btnBack.setOnClickListener(v -> {
            finish(); // Just go back to menu
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }

    private void loadUserData() {
        String loggedInEmail = preferences.getString("loggedInEmail", null);
        if (loggedInEmail == null) {
            finish(); // No session found
            return;
        }

        String name = preferences.getString("name_" + loggedInEmail, "Your Name");
        String imageUriString = preferences.getString("profileImageUri_" + loggedInEmail, null);

        editName.setText(name);
        editEmail.setText(loggedInEmail);
        appVersion.setText("App Version 1.0");

        if (imageUriString != null) {
            try {
                selectedImageUri = Uri.parse(imageUriString);
                profileImagePreview.setImageURI(selectedImageUri);
                Log.d("ProfileActivity", "Profile image loaded");
            } catch (Exception e) {
                Log.e("ProfileActivity", "Error loading image", e);
                profileImagePreview.setImageResource(R.drawable.ic_user_placeholder);
            }
        } else {
            profileImagePreview.setImageResource(R.drawable.ic_user_placeholder);
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        preferences.edit()
                .remove("isLoggedIn")
                .remove("loggedInEmail")
                .apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}