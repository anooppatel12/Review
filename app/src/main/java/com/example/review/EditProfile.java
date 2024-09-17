package com.example.review;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditProfile extends AppCompatActivity {

    private EditText editUsername, editPhone;
    private Button saveButton;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        editUsername = findViewById(R.id.edit_username);
        editPhone = findViewById(R.id.edit_phone);
        saveButton = findViewById(R.id.save_button);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());

        saveButton.setOnClickListener(v -> saveUserProfile());
    }

    private void saveUserProfile() {
        String username = editUsername.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (!TextUtils.isEmpty(username)) {
            databaseReference.child("username").setValue(username);
        }

        if (!TextUtils.isEmpty(phone)) {
            databaseReference.child("phone").setValue(phone);
        }

       redirectToProfile();
    }

    private void redirectToProfile() {
        Intent intent = new Intent(EditProfile.this, MainActivity.class);
        intent.putExtra("fragment", "ProfileFragment");
        startActivity(intent);
        finish();
    }
}
