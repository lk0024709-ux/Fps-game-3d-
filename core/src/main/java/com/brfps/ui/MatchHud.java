package com.brfps.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.brfps.util.Assets;
import com.brfps.util.Constants;

/**
 * The match HUD, extracted from GameScreen so that screen stays inside the 300-line
 * limit (R13): crosshair (with the master M5 recoil bloom), muzzle flash, hit marker,
 * HP/armor/stamina bars, the four weapon boxes, ammo counter, stance, performance
 * line, compass + alive counter, minimap and kill feed — and in debug builds the
 * position, collider, recoil and shooting readout plus a control hint, stacked above
 * the stance. Each string is rebuilt only when a value it shows actually changes, so
 * the render loop allocates nothing (R6, R30).
 */
public class MatchHud {

    private static final String STAND_LABEL = "STAND";
    private static final String CROUCH_LABEL = "CROUCH";
    private static final String SIT_LABEL = "SIT";
    private static final String PRONE_LABEL = "PRONE";
    private static final String AIR_LABEL = "AIR";
    private static final String RELOAD_LABEL = "RELOADING ";

    private final Crosshair crosshair = new Crosshair();
    private final HudBars bars = new HudBars();
    private final WeaponPanel panel = new WeaponPanel();
    private final CompassBar compass = new CompassBar();
    private final GlyphLayout layout = new GlyphLayout();
    private final StringBuilder stanceBuilder = new StringBuilder(16);
    private final StringBuilder ammoBuilder = new StringBuilder(48);
    private final StringBuilder perfBuilder = new StringBuilder(48);
    private final StringBuilder debugBuilder = new StringBuilder(96);

    private String stanceString = "";
    private String ammoString = "";
    private String perfString = "";
    private String debugString = "";
    private String lastStance = "";
    private int lastMagazine = -1;
    private int lastReserve = -1;
    private int lastReloadTenths = -1;
    private boolean lastFist;
    private int lastFps = -1;
    private int lastDrawCalls = -1;
    private int lastTriangles = -1;
    private int lastDecals = -1;
    private int lastX = Integer.MIN_VALUE;
    private int lastY = Integer.MIN_VALUE;
    private int lastZ = Integer.MIN_VALUE;
    private int lastShots = -1;
    private int lastHits = -1;
    private int lastHitDecimeters = -1;
    private int lastRecoilTenths = -1;

    /**
     * Draws the whole HUD; call between batch.begin() and batch.end(). The shot widgets,
     * the kill feed and the minimap are owned by the screen and only drawn here, so all
     * screen-space feedback stays in one batch (R28).
     */
    public void render(SpriteBatch batch, Assets assets, HudData data,
                       MuzzleFlash flash, HitMarker marker, KillFeed feed,
                       MinimapView minimap, float worldWidth, float worldHeight) {
        BitmapFont font = assets.font();
        font.getData().setScale(1f);

        if (!data.loadFailed) {
            crosshair.render(batch, assets.white(), assets.circle(),
                    worldWidth, worldHeight, data.reloading, data.crosshairBloom);
            flash.render(batch, assets.glowRegion(), worldWidth, worldHeight);
            marker.render(batch, assets.whiteRegion(), worldWidth, worldHeight);
            bars.render(batch, assets.white(), font, data, worldWidth, worldHeight);
            panel.render(batch, assets.white(), font, data, worldWidth, worldHeight);
            compass.render(batch, assets.white(), font, data, worldWidth, worldHeight);
            if (minimap != null) {
                minimap.render(batch, assets.white(), assets.whiteRegion(),
                        assets.circle(), data, worldWidth, worldHeight);
            }
            feed.render(batch, font, worldWidth, worldHeight);
        }

        font.setColor(1f, 1f, 1f, 0.9f);
        layout.setText(font, buildStanceString(data));
        float rowTop = 12f + layout.height;
        font.draw(batch, layout, 12f, rowTop);
        font.setColor(0.05f, 0.08f, 0.12f, 1f);
        layout.setText(font, buildPerfString(data));
        font.draw(batch, layout, worldWidth - layout.width - 12f, worldHeight - 12f);

        font.getData().setScale(Constants.HUD_AMMO_SCALE);
        font.setColor(1f, 1f, 1f, 0.92f);
        layout.setText(font, buildAmmoString(data));
        font.draw(batch, layout, (worldWidth - layout.width) * 0.5f,
                worldHeight * Constants.HUD_AMMO_Y + layout.height);

        if (Constants.DEBUG_TOOLS_ENABLED && !data.loadFailed) {
            font.getData().setScale(1f);
            font.setColor(1f, 1f, 1f, 0.85f);
            layout.setText(font, buildDebugString(data));
            rowTop += 6f + layout.height;
            font.draw(batch, layout, 12f, rowTop);
            layout.setText(font, data.helpText);
            rowTop += 6f + layout.height;
            font.draw(batch, layout, 12f, rowTop);
        }

        if (data.loadFailed) {
            font.getData().setScale(1f);
            font.setColor(1f, 1f, 1f, 1f);
            layout.setText(font, "Map load failed - see logcat (BACK to return)");
            font.draw(batch, layout, (worldWidth - layout.width) * 0.5f,
                    worldHeight * 0.5f);
        }
        font.getData().setScale(1f);
    }

    /** "STAND" (bottom-left); HP/armor/stamina moved into bars in HUD phase A2. */
    private String buildStanceString(HudData data) {
        if (data.loadFailed) {
            return "";
        }
        String stance;
        if (!data.onGround) {
            stance = AIR_LABEL;
        } else if (data.crouching) {
            stance = CROUCH_LABEL;
        } else if (data.sitting) {
            stance = SIT_LABEL;
        } else if (data.prone) {
            stance = PRONE_LABEL;
        } else {
            stance = STAND_LABEL;
        }
        if (stance != lastStance) {
            lastStance = stance;
            stanceBuilder.setLength(0);
            stanceBuilder.append(stance);
            stanceString = stanceBuilder.toString();
        }
        return stanceString;
    }

    /**
     * "PISTOL 12 / 60", "PISTOL RELOADING 74%" while the magazine changes, or bare
     * "FIST" (fists never show ammo numbers).
     */
    private String buildAmmoString(HudData data) {
        if (data.loadFailed) {
            return "";
        }
        int reloadTenths = Math.round(data.reloadProgress * 10f);
        if (data.fistActive != lastFist || data.ammoInMagazine != lastMagazine
                || data.reserveAmmo != lastReserve || reloadTenths != lastReloadTenths) {
            lastFist = data.fistActive;
            lastMagazine = data.ammoInMagazine;
            lastReserve = data.reserveAmmo;
            lastReloadTenths = reloadTenths;
            ammoBuilder.setLength(0);
            ammoBuilder.append(data.weaponName);
            if (!data.fistActive) {
                ammoBuilder.append(' ');
                if (data.reloading) {
                    ammoBuilder.append(RELOAD_LABEL).append(reloadTenths * 10).append('%');
                } else {
                    ammoBuilder.append(data.ammoInMagazine)
                            .append(" / ").append(data.reserveAmmo);
                }
            }
            ammoString = ammoBuilder.toString();
        }
        return ammoString;
    }

    /** "FPS 60 | DC 9 | Tri 6.4k". */
    private String buildPerfString(HudData data) {
        if (data.framesPerSecond != lastFps || data.drawCalls != lastDrawCalls
                || data.triangles != lastTriangles || data.decals != lastDecals) {
            lastFps = data.framesPerSecond;
            lastDrawCalls = data.drawCalls;
            lastTriangles = data.triangles;
            lastDecals = data.decals;
            perfBuilder.setLength(0);
            perfBuilder.append("FPS ").append(data.framesPerSecond)
                    .append(" | DC ").append(data.drawCalls)
                    .append(" | Tri ").append(formatK(data.triangles));
            if (data.decals > 0) {
                perfBuilder.append(" | holes ").append(data.decals);
            }
            perfString = perfBuilder.toString();
        }
        return perfString;
    }

    /** Feet position, collider size, live recoil and shot statistics (debug builds). */
    private String buildDebugString(HudData data) {
        int x = Math.round(data.positionX);
        int z = Math.round(data.positionZ);
        int yTenths = Math.round(data.positionY * 10f);
        int hitDecimeters = Math.round(data.lastHitDistance * 10f);
        int recoilTenths = Math.round(data.recoilPitch * 10f);
        if (x != lastX || yTenths != lastY || z != lastZ || data.shotsFired != lastShots
                || data.shotsOnTarget != lastHits || hitDecimeters != lastHitDecimeters
                || recoilTenths != lastRecoilTenths) {
            lastX = x;
            lastY = yTenths;
            lastZ = z;
            lastShots = data.shotsFired;
            lastHits = data.shotsOnTarget;
            lastHitDecimeters = hitDecimeters;
            lastRecoilTenths = recoilTenths;
            debugBuilder.setLength(0);
            debugBuilder.append("X ").append(x)
                    .append("  Z ").append(z)
                    .append("  Y ").append(yTenths / 10).append('.')
                    .append(Math.abs(yTenths % 10))
                    .append(" | BOX ").append(data.colliderBoxes)
                    .append(" | hit ").append(hitDecimeters / 10).append('.')
                    .append(Math.abs(hitDecimeters % 10)).append(" m")
                    .append(" | shots ").append(data.shotsOnTarget).append('/')
                    .append(data.shotsFired);
            if (recoilTenths != 0) {
                debugBuilder.append(" | kick +").append(recoilTenths / 10).append('.')
                        .append(Math.abs(recoilTenths % 10));
            }
            debugString = debugBuilder.toString();
        }
        return debugString;
    }

    private static String formatK(int value) {
        if (value < 1000) {
            return String.valueOf(value);
        }
        return (value / 1000) + "." + ((value % 1000) / 100) + "k";
    }
}
