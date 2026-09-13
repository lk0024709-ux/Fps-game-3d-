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
    public static final float SIT_EYE_HEIGHT = 0.8f;
    public static final float PRONE_EYE_HEIGHT = 0.5f;
    public static final float WALK_SPEED = 4f;
    public static final float JOG_SPEED = 5.5f;
    public static final float SPRINT_SPEED = 7f;
    public static final float CROUCH_SPEED = 2f;
    public static final float PRONE_SPEED = 1f;
    public static final float SIT_SPEED = 0f;
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
    public static final float JOYSTICK_JOG_DEFLECTION = 0.55f; // stick past this = jog
    public static final float SPRINT_STICK_DEFLECTION = 0.92f; // stick pushed this far = sprint
    public static final float JOYSTICK_RADIUS = 0.11f;
    public static final float JOYSTICK_HOME_X = 0.17f; // idle stick centre, fraction of width
    public static final float JOYSTICK_HOME_Y = 0.74f; // idle stick centre, y-down fraction
    public static final float TOUCH_BUTTON_SIZE = 0.13f;
    public static final float TOUCH_MARGIN = 0.03f;
    public static final int CIRCLE_TEXTURE_SIZE = 64; // generated disc for touch widgets

    // --- M34 (master) HUD phase A1: sprint stamina + sprint FOV ---
    public static final float STAMINA_MAX = 100f;
    public static final float STAMINA_DRAIN_PER_SECOND = 15f; // while really sprinting
    public static final float STAMINA_REGEN_PER_SECOND = 20f; // anything but sprinting
    public static final float STAMINA_RELOCK_LEVEL = 30f; // sprint re-enables past this
    public static final float SPRINT_FOV_DEGREES = 85f;
    public static final float FOV_TRANSITION_TIME = 0.3f; // seconds, base <-> sprint FOV

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

    // --- M5 (master) game feel: recoil, shake, muzzle flash, hit marker, beep ---
    // Per-weapon kick in degrees lives in weapons/WeaponType.recoil() (R7); these are
    // the shape of the recovery and the strength of the feedback around it.
    // Recoil is FULL recovery (user decision 2026-09-13): the offset is transient and
    // decays to zero, never written back into the camera's yaw/pitch, so a spray climbs
    // and letting go returns the crosshair to exactly where the player was aiming.
    public static final float RECOIL_RECOVERY_PER_DEGREE = 0.055f; // s of recovery per kick degree
    public static final float RECOIL_RECOVERY_MIN = 0.08f; // floor, so a pistol still snaps back
    public static final float RECOIL_RECOVERY_MAX = 0.40f; // ceiling, so a sniper cannot hang forever
    public static final float RECOIL_RECOVERY_SPEED = 7.5f; // exponential rate over the recovery time
    public static final float RECOIL_HOLD_TIME = 0.06f; // a shot this recent still counts as spraying
    public static final float RECOIL_HOLD_DECAY_PER_SECOND = 3.0f; // slow bleed while the trigger is held
    public static final int RECOIL_RAMP_START_SHOT = 3; // burst shot after which the climb ramps
    public static final float RECOIL_RAMP_SCALE = 1.25f; // multiplier on the kick while ramping
    public static final float RECOIL_PITCH_MAX = 14f; // degrees the pattern may climb at most
    public static final float RECOIL_YAW_ALTERNATE = 0.12f; // left-right sway per shot, in kicks
    public static final float RECOIL_YAW_RANDOM = 0.08f; // degrees of jitter added to the sway
    public static final float RECOIL_YAW_MAX = 4f; // degrees the sway may drift at most
    public static final float RECOIL_BLOOM_MAX = 2.5f; // crosshair gap grows this many times
    // Shake is proportional to the weapon's recoil, so the shotgun kicks harder.
    public static final float SHAKE_PER_RECOIL_DEGREE = 0.20f; // degrees of shake per kick degree
    public static final float SHAKE_MAX_AMPLITUDE = 3.0f;
    public static final float SHAKE_DECAY_PER_SECOND = 14f;
    public static final float SHAKE_FREQUENCY = 1400f; // degrees per second of noise phase
    public static final float SHAKE_ROLL_SCALE = 0.6f; // camera roll as a fraction of amplitude
    public static final float SHAKE_EPSILON = 0.01f; // below this the shake is considered over
    // Muzzle flash is SCREEN-SPACE until master M20 moves it to the weapon's world-space
    // muzzle attach point (see SPEC -> Game Feel). Position is a fraction of the screen
    // and size a fraction of min(width, height) (R46).
    public static final float MUZZLE_FLASH_TIME = 0.07f; // seconds the flash is visible
    public static final float MUZZLE_FLASH_X = 0.5f; // gun is bottom-centre until arms exist
    public static final float MUZZLE_FLASH_Y = 0.38f; // y-up fraction of the screen height
    public static final float MUZZLE_FLASH_SIZE = 0.22f; // glow diameter
    public static final float MUZZLE_FLASH_CORE_SCALE = 0.35f; // white core vs. glow
    public static final float MUZZLE_FLASH_STAR_LENGTH = 0.85f; // star arm vs. glow
    public static final float MUZZLE_FLASH_STAR_THICKNESS = 0.16f;
    public static final float MUZZLE_FLASH_JITTER = 0.85f; // smallest per-shot scale factor
    public static final float MUZZLE_FLASH_ALPHA = 0.9f;
    public static final float MUZZLE_FLASH_R = 1.0f; // warm flash colour
    public static final float MUZZLE_FLASH_G = 0.82f;
    public static final float MUZZLE_FLASH_B = 0.45f;
    public static final int MUZZLE_FLASH_TEXTURE_SIZE = 64; // generated radial glow (POT, R12)
    public static final float HIT_MARKER_TIME = 0.16f;
    public static final float HIT_MARKER_GAP = 0.012f; // distance from the screen centre
    public static final float HIT_MARKER_SPREAD = 0.008f; // extra travel while it fades
    public static final float HIT_MARKER_LENGTH = 0.018f;
    public static final float HIT_MARKER_THICKNESS = 0.003f;
    public static final float HIT_MARKER_ALPHA = 0.95f;
    // Placeholder gunshot beep (master M13 replaces it with real OGG samples).
    public static final boolean SOUND_ENABLED = true; // master audio switch until SoundManager
    public static final int BEEP_SAMPLE_RATE = 22050;
    public static final int BEEP_SAMPLES = 2048; // 0.093 s of mono 16-bit PCM
    public static final float BEEP_FREQUENCY = 760f; // Hz fundamental (plus one octave)
    public static final float BEEP_HARMONIC = 0.35f; // octave mix, 0..1
    public static final float BEEP_DECAY_PER_SECOND = 42f; // exponential envelope rate
    public static final int BEEP_ATTACK_SAMPLES = 24; // fade-in, avoids the opening click
    public static final float BEEP_GAIN = 0.45f; // mix level, keeps the sum below clipping
    public static final float BEEP_VOLUME = 0.6f; // device volume, 0..1

    // --- Main menu (master M3 wiring; scene2d UI arrives at master M44) ---
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
