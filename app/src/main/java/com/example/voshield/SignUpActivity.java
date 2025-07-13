package com.example.voshield;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashSet;
import java.util.Set;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText emailInput, phoneInput, passwordInput;
    private MaterialCheckBox agreeTerms;
    private ProgressBar signupProgress;
    private Button signupButton;
    private TextView loginRedirect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize views
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        passwordInput = findViewById(R.id.passwordInput);
        agreeTerms = findViewById(R.id.agreeTerms);
        signupProgress = findViewById(R.id.signupProgress);
        signupButton = findViewById(R.id.signupButton);
        loginRedirect = findViewById(R.id.loginRedirect);

        // SignUp button logic
        signupButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (!validateInputs(email, phone, password)) return;

            signupProgress.setVisibility(View.VISIBLE);
            signupButton.setEnabled(false);

            // Simulate delay
            signupButton.postDelayed(() -> {
                signupProgress.setVisibility(View.GONE);
                signupButton.setEnabled(true);

                saveUserData(email, phone, password);

                Toast.makeText(this, "Sign Up Successful", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }, 2000);
        });

        // Redirect to Login screen
        loginRedirect.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });
    }

    // Validates all fields before sign-up
    private boolean validateInputs(String email, String phone, String password) {
        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter a valid email address");
            return false;
        }

        if (TextUtils.isEmpty(phone)) {
            phoneInput.setError("Phone number is required");
            return false;
        } else if (!phone.matches("^[6-9]\\d{9}$")) {
            phoneInput.setError("Enter a valid 10-digit Indian phone number");
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            return false;
        } else if (!isStrongPassword(password)) {
            passwordInput.setError("Password must include uppercase, lowercase, number & special char");
            return false;
        }

        if (!agreeTerms.isChecked()) {
            Toast.makeText(this, "Please agree to the terms & policy", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void saveUserData(String email, String phone, String password) {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Save user's email to a global list
        Set<String> registeredEmails = new HashSet<>(prefs.getStringSet("registeredEmails", new HashSet<>()));
        registeredEmails.add(email);
        editor.putStringSet("registeredEmails", registeredEmails);

        // Store user-specific details
        editor.putString("password_" + email, password);
        editor.putString("phone_" + email, phone);
        editor.putString("name_" + email, "New User"); // default name
        editor.apply();
    }

    // Checks password strength
    private boolean isStrongPassword(String password) {
        String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$";
        return password.matches(pattern);
    }
}