/**
 * Emotion.java
 *
 * This model class represents an emotion with a name, an icon resource identifier,
 * and a text color resource identifier. It encapsulates the data used for displaying
 * different emotions in the application.
 *
 * Outstanding Issues:
 * - Currently, the model is immutable. If you require mutable instances or additional
 *   fields (e.g., description), consider updating this class accordingly.
 */

package com.example.tangry.models;

public class Emotion {
    private final String name;
    private final int iconResId;
    private final int textColorResId;  // references a color resource

    /**
     * Constructs a new Emotion instance.
     *
     * @param name           the name of the emotion
     * @param iconResId      the resource identifier for the emotion's icon
     * @param textColorResId the resource identifier for the text color associated with the emotion
     */
    public Emotion(String name, int iconResId, int textColorResId) {
        this.name = name;
        this.iconResId = iconResId;
        this.textColorResId = textColorResId;
    }

    /**
     * Returns the name of the emotion.
     *
     * @return the emotion name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the resource identifier for the emotion's icon.
     *
     * @return the icon resource identifier
     */
    public int getIconResId() {
        return iconResId;
    }

    /**
     * Returns the resource identifier for the text color associated with the emotion.
     *
     * @return the text color resource identifier
     */
    public int getTextColorResId() {
        return textColorResId;
    }
}
