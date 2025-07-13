package com.example.voshield;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.util.HashSet;
import java.util.Set;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText loginEmail, loginPassword;
    private Button loginButton;
    private TextView signupRedirect;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Check if already logged in
        prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        if (prefs.getBoolean("isLoggedIn", false)) {
            redirectToMenu();
            return;
        }

        // Initialize UI components
        loginEmail = findViewById(R.id.loginEmail);
        loginPassword = findViewById(R.id.loginPassword);
        loginButton = findViewById(R.id.loginButton);
        signupRedirect = findViewById(R.id.signupRedirect);

        // Set click listeners
        loginButton.setOnClickListener(v -> attemptLogin());
        signupRedirect.setOnClickListener(v -> redirectToSignUp());
    }

    private void attemptLogin() {
        String email = loginEmail.getText().toString().trim();
        String password = loginPassword.getText().toString().trim();

        if (!validateInputs(email, password)) return;

        Set<String> registeredEmails = prefs.getStringSet("registeredEmails", new HashSet<>());

        if (registeredEmails.contains(email)) {
            String savedPassword = prefs.getString("password_" + email, null);

            if (password.equals(savedPassword)) {
                handleSuccessfulLogin(email);
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Account not found", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInputs(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            loginEmail.setError("Email is required");
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            loginEmail.setError("Enter a valid email address");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            loginPassword.setError("Password is required");
            return false;
        }
        return true;
    }

    private void handleSuccessfulLogin(String email) {
        prefs.edit()
                .putBoolean("isLoggedIn", true)
                .putString("loggedInEmail", email)
                .apply();

        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MenuActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void redirectToMenu() {
        startActivity(new Intent(this, MenuActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void redirectToSignUp() {
        startActivity(new Intent(this, SignUpActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}