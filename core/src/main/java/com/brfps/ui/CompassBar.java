package com.brfps.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.brfps.util.Constants;

/**
 * Compass strip, top-centre (HUD phase A3): the eight direction letters slide past a
 * gold centre caret as the aim turns, the nearest letter lights up, and the alive
 * counter sits below the strip. North is -Z, matching the minimap. One layout and
 * one builder are reused for every letter and the counter (R6); the strip is
 * viewport-relative (R46).
 */
public class CompassBar {

    private static final String[] NAMES = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
    /** Aim yaw per letter (yaw 0 = +X east, 90 = +Z south). */
    private static final float[] YAWS = {270f, 315f, 0f, 45f, 90f, 135f, 180f, 225f};

    private final GlyphLayout layout = new GlyphLayout();
    private final StringBuilder aliveBuilder = new StringBuilder(16);
    private String aliveString = "";
    private int lastAlive = -1;

    /** Strip width in pixels for the current screen. */
    public static float stripWidth(float width, float height) {
        return width * Constants.COMPASS_WIDTH;
    }

    /** Strip height in pixels for the current screen. */
    public static float stripHeight(float width, float height) {
        return Math.min(width, height) * Constants.COMPASS_HEIGHT;
    }

    /** Strip left edge in pixels (batch coords). */
    public static float stripLeft(float width, float height) {
        return (width - stripWidth(width, height)) * 0.5f;
    }

    /** Strip top edge in pixels (batch y-up coords). */
    public static float stripTop(float width, float height) {
        return height - Math.min(width, height) * Constants.TOUCH_MARGIN;
    }

    /** Draws the strip + alive counter; call between batch.begin() and batch.end(). */
    public void render(SpriteBatch batch, Texture white, BitmapFont font,
                       HudData data, float width, float height) {
        float stripW = stripWidth(width, height);
        float stripH = stripHeight(width, height);
        float left = stripLeft(width, height);
        float top = stripTop(width, height);
        float cx = left + stripW * 0.5f;

        batch.setColor(0f, 0f, 0f, Constants.HUD_BAR_BG_ALPHA);
        batch.draw(white, left, top - stripH, stripW, stripH);
        batch.setColor(1f, 0.9f, 0.25f, 0.95f); // gold caret: the current heading
        batch.draw(white, cx - 1f, top - 5f, 2f, 5f);
        batch.setColor(1f, 1f, 1f, 1f);

        font.getData().setScale(0.85f);
        float pxPerDegree = stripW / Constants.COMPASS_RANGE_DEGREES;
        for (int i = 0; i < NAMES.length; i++) {
            float delta = wrap180(YAWS[i] - data.yawDegrees);
            float x = cx + delta * pxPerDegree;
            if (Math.abs(x - cx) > stripW * 0.5f - 10f) {
                continue;
            }
            boolean nearest = true;
            for (int j = 0; j < NAMES.length; j++) {
                if (Math.abs(wrap180(YAWS[j] - data.yawDegrees)) < Math.abs(delta) - 0.01f) {
                    nearest = false;
                    break;
                }
            }
            if (nearest) {
                font.setColor(1f, 0.9f, 0.25f, 0.95f);
            } else {
                font.setColor(1f, 1f, 1f, 0.8f);
            }
            layout.setText(font, NAMES[i]);
            font.draw(batch, layout, x - layout.width * 0.5f,
                    top - stripH + (stripH + layout.height) * 0.5f - 2f);
        }

        font.getData().setScale(1f);
        layout.setText(font, buildAliveString(data));
        font.setColor(1f, 1f, 1f, 0.9f);
        font.draw(batch, layout, cx - layout.width * 0.5f,
                top - stripH - 4f);
    }

    /** "10 ALIVE", rebuilt only when the count changes (R6). */
    private String buildAliveString(HudData data) {
        if (data.aliveCount != lastAlive) {
            lastAlive = data.aliveCount;
            aliveBuilder.setLength(0);
            aliveBuilder.append(data.aliveCount).append(" ALIVE");
            aliveString = aliveBuilder.toString();
        }
        return aliveString;
    }

    /** Wraps degrees into -180..180. */
    private static float wrap180(float degrees) {
        float wrapped = (degrees + 540f) % 360f - 180f;
        if (wrapped < -180f) {
            wrapped += 360f;
        }
        return wrapped;
    }
}
