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

    // --- Map ---
    public static final float MAP_SIZE = 300f;

    // --- Debug tools: ONE master switch for release (R43), sub-flags below ---
    public static final boolean DEBUG_TOOLS_ENABLED = true; // flip to false for release builds

    // --- Map debug visuals (sub-flags of DEBUG_TOOLS_ENABLED) ---
    public static final boolean DEBUG_SHOW_ZONE_MARKERS = true; // set false after master M5
    public static final boolean DEBUG_SHOW_SPAWN_MARKERS = true; // remove after master M31
    public static final boolean DEBUG_SHOW_CHUNK_GRID = true; // preview of M19/M2e chunking
    public static final int TERRAIN_SEGMENTS = 40; // raise in M2e when heightfield lands
    public static final float ROAD_Y_OFFSET = 0.05f;
    public static final float RIVER_Y_OFFSET = 0.03f;
    public static final float SPAWN_MARK_Y_OFFSET = 0.04f;
    public static final float GRID_Y_OFFSET = 0.08f;
    public static final float MARKER_POST_HEIGHT = 2f;
    public static final float MARKER_POST_SIDE = 0.25f;
    public static final float WATER_ALPHA = 0.7f;

    // --- M2a debug orbit camera + screenshot ---
    public static final float ORBIT_RADIUS = 160f;
    public static final float ORBIT_HEIGHT = 120f;
    public static final float ORBIT_SPEED = 12f; // degrees per second
    public static final float SCREENSHOT_DELAY = 5f; // seconds into a world screen

    // --- M2 (master) editor view: free-fly camera, world grid, axis gizmos ---
    public static final float EDITOR_SPEED = 12f; // m/s walking speed in the editor
    public static final float EDITOR_SPRINT_SPEED = 45f; // m/s while Shift is held
    public static final float EDITOR_LOOK_SENSITIVITY = 0.25f; // degrees per pixel dragged
    public static final float EDITOR_TOUCH_RADIUS = 70f; // px of drag for full joystick deflection
    public static final float EDITOR_MIN_Y = 1.5f;
    public static final float EDITOR_MAX_Y = 220f;
    public static final float EDITOR_START_Y = 45f;
    public static final float EDITOR_FAR_PLANE = 900f; // editor flies far above the map
    public static final float GRID_MINOR_STEP = 10f;
    public static final float GRID_MAJOR_STEP = 50f;
    public static final float EDITOR_GRID_Y = 0.12f; // above the chunk grid (0.08) to avoid z-fighting
    public static final float AXIS_LENGTH = 12f;
    public static final float AXIS_GIZMO_Y = 0.2f;
    // Screen-space sizes are fractions of min(screenWidth, screenHeight) — R46.
    public static final float AXIS_INDICATOR_SIZE = 0.13f;
    public static final float EDITOR_MARGIN = 0.02f;
    public static final float EDITOR_BUTTON_SIZE = 0.10f;
    public static final float MODE_BUTTON_SIZE = 0.09f;

    // --- M2b buildings (shared dimensions; per-type sizes live in BuildingType) ---
    public static final float WALL_THICKNESS = 0.2f;
    public static final float DOOR_WIDTH = 1.1f;
    public static final float DOOR_HEIGHT = 2.1f;
    public static final float WINDOW_WIDTH = 1.0f;
    public static final float WINDOW_HEIGHT = 1.0f;
    public static final float WINDOW_SILL = 1.1f;
    public static final float ROOF_PITCH = 0.55f; // rise per half-depth
    public static final float ROOF_OVERHANG = 0.35f;
    public static final float ROOF_SLAB = 0.25f; // flat-roof slab thickness
    public static final float BUILDING_FLOOR_OFFSET = 0.06f; // avoids z-fighting with terrain
    public static final float FLOATING_FACE_MIN_Y = 0.05f; // boxes above this get a bottom face
    public static final int BUILDING_VERTEX_LIMIT = 24000; // flush a mesh before short-index overflow

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
    public static final float LOOK_SENSITIVITY = 0.15f; // degrees per pixel, mouse drag

    // --- M3 (master) first-person movement + touch controls ---
    // sqrt(2 * -GRAVITY * JUMP_HEIGHT) = 4.85 m/s for a 1.2 m jump.
    public static final float JUMP_VELOCITY = 4.85f;
    public static final float PLAYER_RADIUS = 0.35f; // collision circle around the feet
    public static final float STEP_UP_HEIGHT = 0.6f; // walk up ledges this tall without jumping
    public static final float MAX_STEP_DELTA = 0.05f; // clamped frame step; stops wall tunnelling
    public static final float BOUNDS_MARGIN = 1f; // kept inside the map edge
    public static final float TOUCH_LOOK_SENSITIVITY = 0.22f; // degrees per pixel, finger drag
    // Screen-space sizes are fractions of min(screenWidth, screenHeight) — R46.
    public static final float SPRINT_STICK_DEFLECTION = 0.92f; // stick pushed this far = sprint
    public static final float JOYSTICK_RADIUS = 0.11f;
    public static final float JOYSTICK_HOME_X = 0.17f; // idle stick centre, fraction of width
    public static final float JOYSTICK_HOME_Y = 0.74f; // idle stick centre, y-down fraction
    public static final float TOUCH_BUTTON_SIZE = 0.13f;
    public static final float TOUCH_MARGIN = 0.03f;
    public static final int CIRCLE_TEXTURE_SIZE = 64; // generated disc for touch widgets

    // --- M4 (master) shooting: hitscan, bullet holes, crosshair, ammo HUD ---
    // Weapon stats themselves live in weapons/WeaponType.java (R7) — damage, fire
    // interval, magazine, range, reload time, recoil and the projectile flag. The
    // starting reserve is three magazines, computed in weapons/Weapon. Only the decal
    // pool and the screen furniture are tuned here.
    public static final float DECAL_SIZE = 0.14f; // bullet hole quad, meters
    public static final float DECAL_OFFSET = 0.02f; // off the surface, avoids z-fighting
    public static final float FIRE_BUTTON_SIZE = 0.18f; // trigger is bigger than JUMP/CRCH
    public static final float HUD_AMMO_SCALE = 1.6f;
    public static final float HUD_AMMO_Y = 0.05f; // bottom-centre, fraction of height
    // Crosshair sizes are fractions of min(screenWidth, screenHeight) — R46.
    public static final float CROSSHAIR_GAP = 0.006f;
    public static final float CROSSHAIR_LENGTH = 0.012f;
    public static final float CROSSHAIR_THICKNESS = 0.0022f;
    public static final float CROSSHAIR_DOT_SCALE = 2f; // dot diameter in thicknesses
    public static final float CROSSHAIR_ALPHA = 0.85f;
    public static final float CROSSHAIR_RELOAD_ALPHA = 0.35f;

    // --- Main menu (master M3 wiring; scene2d UI arrives with master M44) ---
    public static final float MENU_BUTTON_HEIGHT = 0.10f; // fraction of the short screen side
    public static final float MENU_PLAY_Y = 0.50f; // bottom edge, fraction of height (y up)
    public static final float MENU_EDITOR_Y = 0.37f;
    public static final float MENU_HINT_Y = 0.20f;

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
