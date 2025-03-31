package com.example.tangry.models;

import com.google.firebase.Timestamp;

import java.io.Serializable;

/**
 * Represents a comment on an emotion post.
 * Each comment includes a username, the comment text, and a timestamp.
 */
public class Comment implements Serializable {
    private String username;
    private String text;
    private Timestamp timestamp;

    /**
     * Required no-argument constructor for Firestore deserialization.
     */
    public Comment() {}

    /**
     * Constructs a new Comment object with the specified username and text.
     * Automatically sets the timestamp to the current time.
     *
     * @param username The username of the commenter.
     * @param text     The comment text.
     */
    public Comment(String username, String text) {
        this.username = username;
        this.text = text;
        this.timestamp = Timestamp.now();
    }

    /**
     * Returns the username of the commenter.
     *
     * @return The username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username of the commenter.
     *
     * @param username The username to set.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the comment text.
     *
     * @return The comment text.
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the comment text.
     *
     * @param text The comment text to set.
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Returns the timestamp of the comment.
     *
     * @return The timestamp.
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp of the comment.
     *
     * @param timestamp The timestamp to set.
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Returns a formatted string representation of the comment.
     *
     * @return A string showing the username, comment text, and timestamp.
     */
    @Override
    public String toString() {
        return username + ": " + text + " [" + timestamp.toDate().toString() + "]";
    }
}
