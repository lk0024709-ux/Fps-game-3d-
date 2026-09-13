package com.brfps.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.brfps.util.Constants;

/**
 * Left-thumb movement stick for touch play (R45): it appears wherever the thumb
 * lands, reports a normalized -1..1 vector, and draws itself from a circle texture.
 * Every size is a fraction of min(width, height) so it scales on any screen (R46).
 */
public class VirtualJoystick {

    /** Base centre in screen coords (y down); doubles as the thumb anchor. */
    private float baseX;
    private float baseY;

    /** Knob centre in screen coords (y down). */
    private float knobX;
    private float knobY;

    /** Normalized deflection handed to the movement code. */
    private float vectorX;
    private float vectorY;

    private boolean active;

    /** Thumb pressed: the stick appears at that point. */
    public void begin(float x, float y) {
        baseX = x;
        baseY = y;
        knobX = x;
        knobY = y;
        vectorX = 0f;
        vectorY = 0f;
        active = true;
    }

    /** Thumb moved: deflection is clamped to the stick radius. */
    public void drag(float x, float y, float radius) {
        float dx = x - baseX;
        float dy = y - baseY;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length > radius) {
            float scale = radius / length;
            dx *= scale;
            dy *= scale;
        }
        knobX = baseX + dx;
        knobY = baseY + dy;
        vectorX = dx / radius;
        vectorY = -dy / radius; // screen y grows downward; forward is up
        active = true;
    }

    /** Thumb lifted. */
    public void end() {
        active = false;
        vectorX = 0f;
        vectorY = 0f;
    }

    public boolean isActive() {
        return active;
    }

    public float getX() {
        return vectorX;
    }

    public float getY() {
        return vectorY;
    }

    /** Deflection length, 0..1; used for the sprint threshold. */
    public float magnitude() {
        return (float) Math.sqrt(vectorX * vectorX + vectorY * vectorY);
    }

    /** Stick radius in pixels for the current screen. */
    public static float radius(float screenWidth, float screenHeight) {
        return Math.min(screenWidth, screenHeight) * Constants.JOYSTICK_RADIUS;
    }

    /** Draws base and knob; idle, the stick sits at its home position. */
    public void render(SpriteBatch batch, Texture circle, float screenWidth, float screenHeight) {
        float radius = radius(screenWidth, screenHeight);
        float cx = active ? baseX : screenWidth * Constants.JOYSTICK_HOME_X;
        float cy = active ? baseY : screenHeight * Constants.JOYSTICK_HOME_Y;
        batch.setColor(1f, 1f, 1f, active ? 0.30f : 0.16f);
        draw(batch, circle, cx, screenHeight - cy, radius * 2f);

        float kx = active ? knobX : cx;
        float ky = active ? knobY : cy;
        batch.setColor(1f, 1f, 1f, active ? 0.55f : 0.28f);
        draw(batch, circle, kx, screenHeight - ky, radius * 0.9f);

        batch.setColor(1f, 1f, 1f, 1f);
    }

    /** Draws the circle texture centred on a point in batch (y-up) coords. */
    private static void draw(SpriteBatch batch, Texture circle, float cx, float cy, float size) {
        batch.draw(circle, cx - size * 0.5f, cy - size * 0.5f, size, size);
    }
}
