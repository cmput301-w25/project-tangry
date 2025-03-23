package com.example.tangry.models;

public class User {
    private String username;
    private int karma;

    public User(String username, int karma) {
        this.username = username;
        this.karma = karma;
    }

    public String getUsername() {
        return username;
    }

    public int getKarma() {
        return karma;
    }
}
