package com.example.review;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView profileImage;
    private TextView profileName, profileEmail, profilePhone;
    private Button changeProfileImage, editProfileButton, logoutButton;
    private Uri imageUri;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileImage = view.findViewById(R.id.profile_image);
        profileName = view.findViewById(R.id.profile_name);
        profileEmail = view.findViewById(R.id.profile_email);
        profilePhone = view.findViewById(R.id.profile_phone);
        changeProfileImage = view.findViewById(R.id.change_profile_image);
        editProfileButton = view.findViewById(R.id.edit_profile_button);
        logoutButton = view.findViewById(R.id.logout_button);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
            storageReference = FirebaseStorage.getInstance().getReference("ProfileImages").child(currentUser.getUid());

            // Load user details from Firebase
            loadUserDetails();

            // Change profile image
            changeProfileImage.setOnClickListener(v -> openFileChooser());

            // Edit profile button
            editProfileButton.setOnClickListener(v -> {
                // Handle edit profile functionality
                Intent intent = new Intent(getActivity(), EditProfile.class);
                startActivity(intent);
            });

            // Logout button
            logoutButton.setOnClickListener(v -> {
                mAuth.signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish(); // Close the profile activity
            });
        } else {
            // Handle the case where there is no logged-in user
            Toast.makeText(getContext(), "No user is logged in", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                profileImage.setImageBitmap(bitmap);
                uploadImageToFirebase();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImageToFirebase() {
        if (imageUri != null) {
            storageReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String imageUrl = uri.toString();
                                databaseReference.child("profileImage").setValue(imageUrl);
                                Toast.makeText(getContext(), "Profile picture updated", Toast.LENGTH_SHORT).show();
                            }))
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show());
        }
    }

    private void loadUserDetails() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Load username from Firebase Realtime Database
            databaseReference.child("username").get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    profileName.setText(task.getResult().getValue(String.class));
                } else {
                    profileName.setText("Username not set");
                }
            });

            // Set email and phone number
            profileEmail.setText(currentUser.getEmail());

            databaseReference.child("phone").get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    profilePhone.setText(task.getResult().getValue(String.class));
                } else {
                    profilePhone.setText("Phone number not set");
                }
            });

            // Load the profile image from Firebase
            storageReference.getDownloadUrl()
                    .addOnSuccessListener(uri -> Glide.with(this).load(uri).into(profileImage))
                    .addOnFailureListener(e -> {
                        // Handle the error if the profile image does not exist
                    });
        }
    }
}
