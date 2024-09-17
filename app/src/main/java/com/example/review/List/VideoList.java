package com.example.review.List;

public class VideoList {
    private String title;
    private String description;
    private String productName;
    private String category;
    private String videoUrl;
    private String thumbnailUrl;
    private long uploadTime; // For timestamp
    private String userId; // New field for the user ID

    // Constructor
    public VideoList(String title, String description, String productName, String category, String videoUrl, String thumbnailUrl, long uploadTime, String userId) {
        this.title = title;
        this.description = description;
        this.productName = productName;
        this.category = category;
        this.videoUrl = videoUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.uploadTime = uploadTime;
        this.userId = userId; // Assign user ID
    }

    public VideoList() {
        // Default constructor required for calls to DataSnapshot.getValue(VideoList.class)
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public long getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(long uploadTime) {
        this.uploadTime = uploadTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
