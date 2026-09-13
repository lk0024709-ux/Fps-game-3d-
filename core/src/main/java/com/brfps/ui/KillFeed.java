package com.brfps.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.brfps.util.Constants;

/**
 * Kill feed, top-right below the perf line (HUD phase A3): the last four kills as
 * "killer &gt; victim", newest on top, each fading out over its final second and gone
 * after five. Empty renders nothing. Kill events arrive with the match loop (master
 * M11); until then {@link #addKill} is the API bots will call. One layout and one
 * builder serve every line, so rendering allocates nothing (R6).
 */
public class KillFeed {

    private final String[] killers = new String[Constants.KILL_FEED_COUNT];
    private final String[] victims = new String[Constants.KILL_FEED_COUNT];
    private final float[] remaining = new float[Constants.KILL_FEED_COUNT];
    private final GlyphLayout layout = new GlyphLayout();
    private final StringBuilder lineBuilder = new StringBuilder(64);
    private int head;

    /** Records one kill (call from the match loop, never from the render path). */
    public void addKill(String killer, String victim) {
        killers[head] = killer;
        victims[head] = victim;
        remaining[head] = Constants.KILL_FEED_TIME;
        head = (head + 1) % Constants.KILL_FEED_COUNT;
    }

    /** Ticks the entry timers. */
    public void update(float delta) {
        for (int i = 0; i < Constants.KILL_FEED_COUNT; i++) {
            if (remaining[i] > 0f) {
                remaining[i] -= delta;
                if (remaining[i] < 0f) {
                    remaining[i] = 0f;
                }
            }
        }
    }

    /** Draws the live entries, newest first; call between batch.begin()/end(). */
    public void render(SpriteBatch batch, BitmapFont font, float width, float height) {
        float shortSide = Math.min(width, height);
        float lineH = shortSide * Constants.KILL_FEED_LINE_HEIGHT;
        float top = height - shortSide * Constants.KILL_FEED_TOP;
        float right = width - shortSide * Constants.TOUCH_MARGIN;
        for (int n = 0; n < Constants.KILL_FEED_COUNT; n++) {
            int index = (head - 1 - n + Constants.KILL_FEED_COUNT * 2)
                    % Constants.KILL_FEED_COUNT;
            if (killers[index] == null || remaining[index] <= 0f) {
                continue;
            }
            float alpha = remaining[index] < 1f ? remaining[index] : 1f;
            font.setColor(1f, 1f, 1f, 0.92f * alpha);
            lineBuilder.setLength(0);
            lineBuilder.append(killers[index]).append(" > ").append(victims[index]);
            layout.setText(font, lineBuilder);
            font.draw(batch, layout, right - layout.width, top - n * lineH);
        }
    }
}
