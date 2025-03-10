package com.example.tangry.models;

public class Emotion {
    private final String name;
    private final int iconResId;
    private final int textColorResId;  // references a color resource

    public Emotion(String name, int iconResId, int textColorResId) {
        this.name = name;
        this.iconResId = iconResId;
        this.textColorResId = textColorResId;

    }

    public String getName() { return name; }
    public int getIconResId() { return iconResId; }
    public int getTextColorResId() { return textColorResId; }
}
