package com.example.review;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.review.Adapter.VideoAdapter;
import com.example.review.List.VideoList;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyVideosFragment extends Fragment {

    private RecyclerView videosRecyclerView;
    private VideoAdapter videoAdapter;
    private ArrayList<VideoList> videoList;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_videos, container, false);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        videosRecyclerView = view.findViewById(R.id.recycler_view_videos);
        videosRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        videoList = new ArrayList<>();
        videoAdapter = new VideoAdapter(getContext(), videoList);
        videosRecyclerView.setAdapter(videoAdapter);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Videos");

        // Load videos for the current user from Firebase
        loadUserVideosFromFirebase();

        // Set up the click listener for video items
        videoAdapter.setOnItemClickListener(position -> {
            VideoList selectedVideo = videoList.get(position);
            Intent intent = new Intent(getActivity(), VideoPlayerActivity.class);
            intent.putExtra("videoUrl", selectedVideo.getVideoUrl()); // Make sure 'getVideoUrl' matches your model
            startActivity(intent);
        });

        return view;
    }

    private void loadUserVideosFromFirebase() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid(); // Get the current user's UID

            // Query only videos uploaded by the current user
            databaseReference.child(userId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    videoList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        VideoList video = snapshot.getValue(VideoList.class);
                        if (video != null) {
                            videoList.add(video);
                        }
                    }
                    videoAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error
                }
            });
        }
    }
}
