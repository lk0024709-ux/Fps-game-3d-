package com.brfps.ui;

/**
 * One frame of HUD values: filled by the screen, read by {@link MatchHud}. A plain
 * mutable snapshot so the HUD never allocates and never has to reach into gameplay
 * classes itself (R6) — the same pattern as {@code input.InputState}.
 */
public class HudData {

    // --- player ---
    public int health;
    public int armor;
    public boolean crouching;
    public boolean onGround;
    public float positionX;
    public float positionY;
    public float positionZ;

    // --- weapon ---
    public String weaponName;
    public int ammoInMagazine;
    public int reserveAmmo;
    public boolean reloading;
    public float reloadProgress;

    // --- world / performance ---
    public int framesPerSecond;
    public int drawCalls;
    public int triangles;
    public int colliderBoxes;
    public int decals;

    // --- shooting results (debug readout) ---
    public int shotsFired;
    public int shotsOnTarget;
    public float lastHitDistance;

    // --- static text owned by the screen ---
    public String helpText;
    public boolean loadFailed;
}
