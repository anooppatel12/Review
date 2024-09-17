package com.example.review.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.review.List.VideoList;
import com.example.review.R;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private Context context;
    private List<VideoList> videoList;
    private OnItemClickListener listener;

    public VideoAdapter(Context context, List<VideoList> videoList) {
        this.context = context;
        this.videoList = videoList;
    }

    public VideoAdapter() {

    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.video_item, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        VideoList video = videoList.get(position);
        if (video != null) {
            holder.title.setText(video.getTitle() != null ? video.getTitle() : "No Title");
            holder.description.setText(video.getDescription() != null ? video.getDescription() : "No Description");

            // Load the video thumbnail using Glide
            Glide.with(context)
                    .load(video.getThumbnailUrl())
                    .placeholder(R.drawable.placeholder)  // Ensure you have a placeholder image in drawable
                    .into(holder.thumbnail);

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateVideos(List<VideoList> videos) {
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {

        TextView title, description;
        ImageView thumbnail;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.video_title);
            description = itemView.findViewById(R.id.video_description);
            thumbnail = itemView.findViewById(R.id.video_thumbnail);
        }
    }
}
