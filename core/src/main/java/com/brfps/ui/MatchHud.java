package com.brfps.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.brfps.util.Assets;
import com.brfps.util.Constants;

/**
 * The match HUD, extracted from GameScreen so that screen stays inside the 300-line
 * limit (R13): crosshair, ammo counter, health/armor/stance, performance line and —
 * in debug builds — the position, collider and shooting readout plus a control hint.
 * Each string is rebuilt only when a value it shows actually changes, so the render
 * loop allocates nothing (R6, R30).
 */
public class MatchHud {

    private static final String STAND_LABEL = "STAND";
    private static final String CROUCH_LABEL = "CROUCH";
    private static final String AIR_LABEL = "AIR";
    private static final String RELOAD_LABEL = "RELOADING ";

    private final Crosshair crosshair = new Crosshair();
    private final GlyphLayout layout = new GlyphLayout();
    private final StringBuilder statsBuilder = new StringBuilder(48);
    private final StringBuilder ammoBuilder = new StringBuilder(48);
    private final StringBuilder perfBuilder = new StringBuilder(48);
    private final StringBuilder debugBuilder = new StringBuilder(96);

    private String statsString = "";
    private String ammoString = "";
    private String perfString = "";
    private String debugString = "";
    private int lastHealth = -1;
    private int lastArmor = -1;
    private String lastStance = "";
    private int lastMagazine = -1;
    private int lastReserve = -1;
    private int lastReloadTenths = -1;
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

    /** Draws the whole HUD; call between batch.begin() and batch.end(). */
    public void render(SpriteBatch batch, Assets assets, HudData data,
                       float worldWidth, float worldHeight) {
        BitmapFont font = assets.font();
        font.getData().setScale(1f);

        if (!data.loadFailed) {
            crosshair.render(batch, assets.white(), assets.circle(),
                    worldWidth, worldHeight, data.reloading);
        }

        font.setColor(0.05f, 0.08f, 0.12f, 1f);
        layout.setText(font, buildStatsString(data));
        font.draw(batch, layout, 12f, worldHeight - 12f);
        layout.setText(font, buildPerfString(data));
        font.draw(batch, layout, worldWidth - layout.width - 12f, worldHeight - 12f);

        font.getData().setScale(Constants.HUD_AMMO_SCALE);
        font.setColor(1f, 1f, 1f, 0.92f);
        layout.setText(font, buildAmmoString(data));
        font.draw(batch, layout, (worldWidth - layout.width) * 0.5f,
                worldHeight * Constants.HUD_AMMO_Y + layout.height);

        if (Constants.DEBUG_TOOLS_ENABLED && !data.loadFailed) {
            font.getData().setScale(1f);
            font.setColor(0.05f, 0.08f, 0.12f, 1f);
            layout.setText(font, buildDebugString(data));
            font.draw(batch, layout, worldWidth - layout.width - 12f, worldHeight - 32f);
            layout.setText(font, data.helpText);
            font.draw(batch, layout, 12f, worldHeight - 32f);
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

    /** "HP 100 | AR 0 | STAND". */
    private String buildStatsString(HudData data) {
        if (data.loadFailed) {
            return "";
        }
        String stance = !data.onGround ? AIR_LABEL
                : (data.crouching ? CROUCH_LABEL : STAND_LABEL);
        if (data.health != lastHealth || data.armor != lastArmor || stance != lastStance) {
            lastHealth = data.health;
            lastArmor = data.armor;
            lastStance = stance;
            statsBuilder.setLength(0);
            statsBuilder.append("HP ").append(data.health)
                    .append(" | AR ").append(data.armor)
                    .append(" | ").append(stance);
            statsString = statsBuilder.toString();
        }
        return statsString;
    }

    /** "PISTOL 12 / 60", or "PISTOL RELOADING 74%" while the magazine changes. */
    private String buildAmmoString(HudData data) {
        if (data.loadFailed) {
            return "";
        }
        int reloadTenths = Math.round(data.reloadProgress * 10f);
        if (data.ammoInMagazine != lastMagazine || data.reserveAmmo != lastReserve
                || reloadTenths != lastReloadTenths) {
            lastMagazine = data.ammoInMagazine;
            lastReserve = data.reserveAmmo;
            lastReloadTenths = reloadTenths;
            ammoBuilder.setLength(0);
            ammoBuilder.append(data.weaponName).append(' ');
            if (data.reloading) {
                ammoBuilder.append(RELOAD_LABEL).append(reloadTenths * 10).append('%');
            } else {
                ammoBuilder.append(data.ammoInMagazine)
                        .append(" / ").append(data.reserveAmmo);
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

    /** Feet position, collider size and shot statistics (debug builds only). */
    private String buildDebugString(HudData data) {
        int x = Math.round(data.positionX);
        int z = Math.round(data.positionZ);
        int yTenths = Math.round(data.positionY * 10f);
        int hitDecimeters = Math.round(data.lastHitDistance * 10f);
        if (x != lastX || yTenths != lastY || z != lastZ || data.shotsFired != lastShots
                || data.shotsOnTarget != lastHits || hitDecimeters != lastHitDecimeters) {
            lastX = x;
            lastY = yTenths;
            lastZ = z;
            lastShots = data.shotsFired;
            lastHits = data.shotsOnTarget;
            lastHitDecimeters = hitDecimeters;
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
