/**
 * EmotionProvider.java
 *
 * This class provides a static method to supply a list of sample Emotion objects.
 * These sample emotions can be used for populating UI elements, such as dropdown menus
 * or RecyclerViews, in the application.
 *
 * Outstanding Issues:
 * - We considering externalizing the list of emotions (e.g., via a configuration file) for easier customization.
 */

package com.example.tangry.models;

import com.example.tangry.R;
import java.util.Arrays;
import java.util.List;

public class EmotionProvider {

    /**
     * Returns a list of sample Emotion objects.
     *
     * @return a List of Emotion instances representing different emotions.
     */
    public static List<Emotion> getSampleEmotions() {
        return Arrays.asList(
                new Emotion("Angry",     R.drawable.ic_angry_selector,     R.color.colorAngry),
                new Emotion("Confused",  R.drawable.ic_confused_selector,  R.color.colorConfused),
                new Emotion("Disgust",   R.drawable.ic_disgust_selector,   R.color.colorDisgust),
                new Emotion("Fear",      R.drawable.ic_fear_selector,      R.color.colorFear),
                new Emotion("Happiness", R.drawable.ic_happiness_selector, R.color.colorHappiness),
                new Emotion("Sadness",   R.drawable.ic_sadness_selector,   R.color.colorSadness),
                new Emotion("Shame",     R.drawable.ic_shame_selector,     R.color.colorShame),
                new Emotion("Surprise",  R.drawable.ic_surprise_selector,  R.color.colorSurprise)
        );
    }
}
