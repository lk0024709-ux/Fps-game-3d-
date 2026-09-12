package com.brfps.enemies;

/** Bot skill presets: accuracy and reaction time (see SPEC.md, Enemy AI). */
public enum BotDifficulty {

    EASY(0.25f, 0.12f, 0.9f),
    NORMAL(0.40f, 0.20f, 0.6f),
    HARD(0.60f, 0.35f, 0.35f);

    private final float midRangeAccuracy;
    private final float longRangeAccuracy;
    private final float reactionTime;

    BotDifficulty(float midRangeAccuracy, float longRangeAccuracy, float reactionTime) {
        this.midRangeAccuracy = midRangeAccuracy;
        this.longRangeAccuracy = longRangeAccuracy;
        this.reactionTime = reactionTime;
    }

    public float midRangeAccuracy() {
        return midRangeAccuracy;
    }

    public float longRangeAccuracy() {
        return longRangeAccuracy;
    }

    /** Seconds before the bot reacts to spotting a target. */
    public float reactionTime() {
        return reactionTime;
    }

    /** Hit chance for the given distance in meters. */
    public float accuracyAt(float distance) {
        return distance <= 30f ? midRangeAccuracy : longRangeAccuracy;
    }
}
