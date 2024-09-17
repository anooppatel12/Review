package com.example.review;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.review.List.VideoList;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class UploadFragment extends Fragment {

    private static final int PICK_VIDEO_REQUEST = 1;
    private static final int PICK_THUMBNAIL_REQUEST = 2;

    private EditText videoTitle, videoDescription, videoCategory, productName, videoUrl, thumbnailUrl;
    private ImageView videoThumbnail;
    private Button selectVideoButton, selectThumbnailButton, uploadButton;
    private ProgressBar progressBar;
    private Uri videoUri, thumbnailUri;

    private FirebaseStorage storage;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload, container, false);

        videoTitle = view.findViewById(R.id.video_title);
        videoDescription = view.findViewById(R.id.video_description);
        productName = view.findViewById(R.id.product_name);
        videoCategory = view.findViewById(R.id.category);
        videoUrl = view.findViewById(R.id.video_url);
        thumbnailUrl = view.findViewById(R.id.thumbnail_url);
        videoThumbnail = view.findViewById(R.id.video_thumbnail);
        selectVideoButton = view.findViewById(R.id.select_video_button);
        selectThumbnailButton = view.findViewById(R.id.select_thumbnail_button);
        uploadButton = view.findViewById(R.id.upload_button);
        progressBar = view.findViewById(R.id.progress_bar);

        // Initialize Firebase instances
        storage = FirebaseStorage.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Videos");
        mAuth = FirebaseAuth.getInstance();

        selectVideoButton.setOnClickListener(v -> openVideoSelector());
        selectThumbnailButton.setOnClickListener(v -> openThumbnailSelector());
        uploadButton.setOnClickListener(v -> uploadVideo());

        return view;
    }

    private void openVideoSelector() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        startActivityForResult(intent, PICK_VIDEO_REQUEST);
    }

    private void openThumbnailSelector() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_THUMBNAIL_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            if (requestCode == PICK_VIDEO_REQUEST) {
                videoUri = data.getData();
                videoUrl.setText(videoUri.toString());
            } else if (requestCode == PICK_THUMBNAIL_REQUEST) {
                thumbnailUri = data.getData();
                thumbnailUrl.setText(thumbnailUri.toString());
                videoThumbnail.setImageURI(thumbnailUri);
            }
        }
    }

    private void uploadVideo() {
        String title = videoTitle.getText().toString().trim();
        String description = videoDescription.getText().toString().trim();
        String product = productName.getText().toString().trim();
        String videoUriString = videoUrl.getText().toString().trim();
        String thumbnailUriString = thumbnailUrl.getText().toString().trim();
        String category = videoCategory.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description) || TextUtils.isEmpty(product) ||
                TextUtils.isEmpty(videoUriString) || TextUtils.isEmpty(thumbnailUriString)) {
            Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();  // Get user ID
            Uri videoUri = Uri.parse(videoUriString);
            Uri thumbnailUri = Uri.parse(thumbnailUriString);

            // Firebase Storage reference for video and thumbnail
            StorageReference videoRef = storage.getReference().child("videos/" + System.currentTimeMillis() + ".mp4");
            StorageReference thumbnailRef = storage.getReference().child("thumbnails/" + System.currentTimeMillis() + ".jpg");

            // Upload video
            videoRef.putFile(videoUri)
                    .addOnSuccessListener(taskSnapshot -> videoRef.getDownloadUrl().addOnSuccessListener(videoDownloadUri -> {
                        String videoDownloadUrl = videoDownloadUri.toString();

                        // Upload thumbnail
                        thumbnailRef.putFile(thumbnailUri)
                                .addOnSuccessListener(taskSnapshot1 -> thumbnailRef.getDownloadUrl().addOnSuccessListener(thumbnailDownloadUri -> {
                                    String thumbnailDownloadUrl = thumbnailDownloadUri.toString();
                                    long timestamp = System.currentTimeMillis(); // Current timestamp

                                    // Save video details to Firebase Database
                                    saveVideoToDatabase(title, description, product, category, videoDownloadUrl, thumbnailDownloadUrl, timestamp, userId);
                                }))
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(getActivity(), "Thumbnail upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });

                    }))
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getActivity(), "Video upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getActivity(), "Please log in to upload videos.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveVideoToDatabase(String title, String description, String product, String category,
                                     String videoUrl, String thumbnailUrl, long timestamp, String userId) {
        VideoList video = new VideoList(title, description, product, category, videoUrl, thumbnailUrl, timestamp, userId);
        video.setUserId(userId); // Associate the video with the current user

        // Save video to Firebase Database under the current user's userId
        DatabaseReference userVideosRef = databaseReference.child(userId); // Reference to the user's node
        String videoId = userVideosRef.push().getKey();  // Generate unique videoId

        if (videoId != null) {
            userVideosRef.child(videoId).setValue(video)
                    .addOnSuccessListener(aVoid -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getActivity(), "Video uploaded successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getActivity(), "Failed to save video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
