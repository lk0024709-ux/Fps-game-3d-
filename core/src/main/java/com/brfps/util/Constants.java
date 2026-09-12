package com.brfps.util;

/** Tunable game constants. Values come straight from SPEC.md. */
public final class Constants {

    private Constants() {
    }

    // --- Identity ---
    public static final String GAME_TITLE = "Battle Royale FPS";
    public static final String VERSION = "0.1.0";

    // --- Rendering ---
    public static final float FOV_DEGREES = 75f;
    public static final float NEAR_PLANE = 0.1f;
    public static final float FAR_PLANE = 300f;
    public static final float MAX_PIXEL_RATIO = 1.5f;

    // --- Arena ---
    public static final float ARENA_SIZE = 120f;

    // --- Player ---
    public static final float EYE_HEIGHT = 1.6f;
    public static final float CROUCH_EYE_HEIGHT = 1.0f;
    public static final float WALK_SPEED = 4f;
    public static final float SPRINT_SPEED = 7f;
    public static final float CROUCH_SPEED = 2f;
    public static final float GRAVITY = -9.8f;
    public static final float JUMP_HEIGHT = 1.2f;
    public static final float MAX_HEALTH = 100f;
    public static final float MAX_ARMOR = 100f;
    public static final float ARMOR_ABSORB = 0.5f;
    public static final int MAX_WEAPONS = 2;
    public static final int MAX_ITEMS = 4;
    public static final float PICKUP_RADIUS = 1.5f;

    // --- Camera look ---
    public static final float PITCH_MIN = -80f;
    public static final float PITCH_MAX = 80f;
    public static final float LOOK_SENSITIVITY = 0.15f;

    // --- Safe zone ---
    public static final float ZONE_START_RADIUS = 60f;
    public static final float ZONE_SHRINK_INTERVAL = 90f;
    public static final float ZONE_SHRINK_FACTOR = 0.6f;
    public static final float ZONE_MIN_RADIUS = 5f;
    public static final float ZONE_DAMAGE_PER_SECOND = 5f;

    // --- Match ---
    public static final int BOT_COUNT = 9;
    public static final int TOTAL_PLAYERS = BOT_COUNT + 1;
    public static final float MATCH_DURATION = 600f;

    // --- Bots ---
    public static final float BOT_SIGHT_RANGE = 30f;
    public static final float BOT_ATTACK_RANGE = 15f;
    public static final float BOT_FLEE_HEALTH = 25f;

    // --- Items ---
    public static final float MEDKIT_HEAL = 50f;
    public static final float MEDKIT_USE_TIME = 3f;
    public static final float ARMOR_VEST_AMOUNT = 50f;
    public static final float GRENADE_DAMAGE = 80f;
    public static final float GRENADE_RADIUS = 5f;

    // --- Pools ---
    public static final int BULLET_POOL_SIZE = 128;
    public static final int DECAL_POOL_SIZE = 64;
}
