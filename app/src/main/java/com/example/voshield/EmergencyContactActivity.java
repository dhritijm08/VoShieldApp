package com.example.voshield;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class EmergencyContactActivity extends AppCompatActivity {

    private static final int PICK_CONTACT_REQUEST = 1;
    private static final String TAG = "EmergencyContacts";

    private ArrayList<String> contactList;
    private ArrayAdapter<String> adapter;
    private String selectedContact = null;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contact);

        // Ask for contacts permission
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_CONTACTS}, 1);

        sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);

        Button btnAdd = findViewById(R.id.btnAdd);
        Button btnRemove = findViewById(R.id.btnRemove);
        Button btnBack = findViewById(R.id.btnBack);
        ListView listViewContacts = findViewById(R.id.listViewContacts);

        // Load saved contacts
        contactList = new ArrayList<>(getSavedContacts());

        // Adapter with off-black text
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_single_choice,
                contactList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (view instanceof CheckedTextView) {
                    ((CheckedTextView) view).setTextColor(Color.parseColor("#212121")); // off-black
                }
                return view;
            }
        };

        listViewContacts.setAdapter(adapter);
        listViewContacts.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // Button actions
        btnAdd.setOnClickListener(v -> addContact());
        btnRemove.setOnClickListener(v -> removeSelectedContact());
        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        listViewContacts.setOnItemClickListener((parent, view, position, id) -> {
            selectedContact = contactList.get(position);
            Log.d(TAG, "Selected contact: " + selectedContact);
        });
    }

    private void addContact() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT_REQUEST);
    }

    private void removeSelectedContact() {
        if (selectedContact != null) {
            contactList.remove(selectedContact);
            saveContacts();
            adapter.notifyDataSetChanged();
            selectedContact = null;
            Toast.makeText(this, "Contact removed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Please select a contact to remove", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_CONTACT_REQUEST &&
                resultCode == Activity.RESULT_OK &&
                data != null) {

            Uri contactUri = data.getData();
            String contactDetails = getContactDetails(contactUri);

            if (contactDetails != null) {
                if (!contactList.contains(contactDetails)) {
                    contactList.add(contactDetails);
                    saveContacts();
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Contact added: " + contactDetails, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Contact already exists", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to retrieve contact details", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("Range")
    private String getContactDetails(Uri uri) {
        String name = null;
        String phoneNumber = null;

        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                phoneNumber = cursor.getString(cursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.NUMBER));

                // Clean up phone number
                if (phoneNumber != null) {
                    phoneNumber = phoneNumber.replaceAll("[^0-9]", "");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting contact details", e);
        }

        return (name != null && phoneNumber != null)
                ? name + " - " + phoneNumber
                : null;
    }

    private void saveContacts() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> contactSet = new HashSet<>(contactList);
        editor.putStringSet("emergency_contacts", contactSet);
        editor.apply();
    }

    private Set<String> getSavedContacts() {
        return sharedPreferences.getStringSet("emergency_contacts", new HashSet<>());
    }
}