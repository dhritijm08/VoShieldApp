package com.example.voshield;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class EditActivity extends AppCompatActivity {

    private ImageView profileImage;
    private TextView btnEditPhoto;
    private EditText editName, editEmail;
    private Button btnSave, btnBack;

    private SharedPreferences prefs;
    private Uri selectedImageUri;
    private String loggedInEmail;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    try {
                        getContentResolver().takePersistableUriPermission(
                                selectedImageUri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );

                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                getContentResolver(),
                                selectedImageUri
                        );
                        profileImage.setImageBitmap(bitmap);

                        prefs.edit()
                                .putString("profileImageUri_" + loggedInEmail, selectedImageUri.toString())
                                .apply();

                        Log.d("EditActivity", "Image selected and saved: " + selectedImageUri);
                    } catch (IOException e) {
                        Log.e("EditActivity", "Error loading image", e);
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        profileImage.setImageResource(R.drawable.ic_user_placeholder);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        profileImage = findViewById(R.id.profileImage);
        btnEditPhoto = findViewById(R.id.btnEditPhoto);
        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);

        editEmail.setEnabled(false);
        editEmail.setFocusable(false);
        editEmail.setClickable(false);

        prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        loggedInEmail = prefs.getString("loggedInEmail", null);
        if (loggedInEmail == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            return;
        }

        loadUserData();

        btnEditPhoto.setOnClickListener(v -> openImagePicker());
        btnSave.setOnClickListener(v -> saveUserData());
        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }

    private void loadUserData() {
        String name = prefs.getString("name_" + loggedInEmail, "");
        String email = loggedInEmail;
        String imageUriString = prefs.getString("profileImageUri_" + loggedInEmail, null);

        editName.setText(name);
        editEmail.setText(email);

        if (imageUriString != null) {
            try {
                selectedImageUri = Uri.parse(imageUriString);
                getContentResolver().openInputStream(selectedImageUri).close();
                profileImage.setImageURI(selectedImageUri);
                Log.d("EditActivity", "Successfully loaded existing image");
            } catch (Exception e) {
                Log.e("EditActivity", "Lost permission for URI, resetting", e);
                prefs.edit().remove("profileImageUri_" + loggedInEmail).apply();
                profileImage.setImageResource(R.drawable.ic_user_placeholder);
            }
        } else {
            profileImage.setImageResource(R.drawable.ic_user_placeholder);
        }
    }

    private void saveUserData() {
        String name = editName.getText().toString().trim();

        if (name.isEmpty()) {
            editName.setError("Name cannot be empty");
            return;
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("name_" + loggedInEmail, name);

        if (selectedImageUri != null) {
            editor.putString("profileImageUri_" + loggedInEmail, selectedImageUri.toString());
        }

        if (editor.commit()) {
            setResult(RESULT_OK);
            Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        } else {
            Toast.makeText(this, "Failed to save profile", Toast.LENGTH_SHORT).show();
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        imagePickerLauncher.launch(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}