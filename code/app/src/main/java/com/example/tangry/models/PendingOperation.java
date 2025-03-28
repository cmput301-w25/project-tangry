package com.example.tangry.models;

import com.google.gson.Gson;
import java.util.UUID;

public class PendingOperation {
    public enum OperationType {
        CREATE,
        UPDATE,
        DELETE
    }

    private final String id;
    private final OperationType type;
    private final long timestamp;
    private final String postId;
    private final String postData;

    public PendingOperation(OperationType type, String postId, EmotionPost post) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.timestamp = System.currentTimeMillis();
        this.postId = postId;
        this.postData = post != null ? new Gson().toJson(post) : null;
    }

    public String getId() {
        return id;
    }

    public OperationType getType() {
        return type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getPostId() {
        return postId;
    }

    public EmotionPost getPost() {
        return postData != null ? new Gson().fromJson(postData, EmotionPost.class) : null;
    }
}