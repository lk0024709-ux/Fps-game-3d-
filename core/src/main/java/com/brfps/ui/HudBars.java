package com.brfps.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.brfps.util.Constants;

/**
 * HP / armor / stamina bars on the left edge (HUD phase A2): a dark tray with a
 * coloured fill, a two-letter tag on the left and the "cur/max" value pinned to the
 * right edge. Replaces the "HP 100 | AR 0 | ST 85" text line. One builder and one
 * layout are reused for all three rows, so drawing allocates nothing (R6); every
 * size is viewport-relative (R46).
 */
public class HudBars {

    private static final String HP_TAG = "HP";
    private static final String AR_TAG = "AR";
    private static final String ST_TAG = "ST";

    private final GlyphLayout layout = new GlyphLayout();
    private final StringBuilder valueBuilder = new StringBuilder(16);

    /** Bar width in pixels for the current screen. */
    public static float barWidth(float width, float height) {
        return Math.min(width, height) * Constants.HUD_BAR_WIDTH;
    }

    /** Bar height in pixels for the current screen. */
    public static float barHeight(float width, float height) {
        return Math.min(width, height) * Constants.HUD_BAR_HEIGHT;
    }

    /** Top edge (batch y-up) of one row: 0 HP, 1 armor, 2 stamina. */
    public static float rowTop(int row, float width, float height) {
        float shortSide = Math.min(width, height);
        return height * Constants.HUD_BARS_TOP_Y
                - row * (shortSide * Constants.HUD_BAR_HEIGHT
                + shortSide * Constants.HUD_BAR_GAP);
    }

    /** Bottom edge (batch y-up) of the stamina row: side buttons hang below this. */
    public static float contentBottom(float width, float height) {
        return rowTop(2, width, height) - barHeight(width, height);
    }

    /** Draws all three bars; call between batch.begin() and batch.end(). */
    public void render(SpriteBatch batch, Texture white, BitmapFont font,
                       HudData data, float width, float height) {
        float x = Math.min(width, height) * Constants.TOUCH_MARGIN;
        float barW = barWidth(width, height);
        float barH = barHeight(width, height);
        drawRow(batch, white, font, HP_TAG, rowTop(0, width, height),
                x, barW, barH, data.health / Constants.MAX_HEALTH,
                data.health, (int) Constants.MAX_HEALTH, 0.898f, 0.224f, 0.208f);
        drawRow(batch, white, font, AR_TAG, rowTop(1, width, height),
                x, barW, barH, data.armor / Constants.MAX_ARMOR,
                data.armor, (int) Constants.MAX_ARMOR, 0.118f, 0.533f, 0.898f);
        int stamina = Math.round(data.stamina);
        drawRow(batch, white, font, ST_TAG, rowTop(2, width, height),
                x, barW, barH, data.stamina / Constants.STAMINA_MAX,
                stamina, (int) Constants.STAMINA_MAX, 0.992f, 0.847f, 0.208f);
    }

    /** One tray + fill + tag + value; top is the row's top edge in batch coords. */
    private void drawRow(SpriteBatch batch, Texture white, BitmapFont font, String tag,
                         float top, float x, float barW, float barH, float fraction,
                         int current, int max, float r, float g, float b) {
        float y = top - barH;
        if (fraction < 0f) {
            fraction = 0f;
        } else if (fraction > 1f) {
            fraction = 1f;
        }
        batch.setColor(0f, 0f, 0f, Constants.HUD_BAR_BG_ALPHA);
        batch.draw(white, x, y, barW, barH);
        batch.setColor(r, g, b, 0.92f);
        batch.draw(white, x, y, barW * fraction, barH);
        batch.setColor(1f, 1f, 1f, 1f);

        font.setColor(1f, 1f, 1f, 0.9f);
        layout.setText(font, tag);
        float textY = y + (barH + layout.height) * 0.5f;
        font.draw(batch, layout, x + 4f, textY);
        valueBuilder.setLength(0);
        valueBuilder.append(current).append('/').append(max);
        layout.setText(font, valueBuilder);
        font.draw(batch, layout, x + barW - layout.width - 4f,
                y + (barH + layout.height) * 0.5f);
    }
}
