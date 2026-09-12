package com.brfps.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.ScreenUtils;

import java.nio.ByteBuffer;

/** Writes a one-shot PNG screenshot of the current frame to app-internal storage (no permissions). */
public final class ScreenshotUtil {

    private ScreenshotUtil() {
    }

    /**
     * Captures the current framebuffer to a local file such as
     * "debug/screenshot.png". Call on the render thread after drawing.
     * Pull the file off-device with: adb shell run-as com.brfps cat debug/screenshot.png
     */
    public static void capture(String path) {
        try {
            Pixmap pixmap = ScreenUtils.getFrameBufferPixmap(
                    0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            flipVertically(pixmap);
            PixmapIO.writePNG(Gdx.files.local(path), pixmap);
            pixmap.dispose();
            Gdx.app.log("Screenshot", "saved " + path);
        } catch (Throwable t) {
            // A debug screenshot must never crash the game.
            Gdx.app.error("Screenshot", "capture failed", t);
        }
    }

    /** OpenGL framebuffers start at the bottom; flip rows for a normal PNG. */
    private static void flipVertically(Pixmap pixmap) {
        int width = pixmap.getWidth();
        int height = pixmap.getHeight();
        ByteBuffer pixels = pixmap.getPixels();
        int limit = pixels.limit();
        byte[] row = new byte[width * 4];
        for (int y = 0; y < height / 2; y++) {
            int topOffset = y * width * 4;
            int bottomOffset = (height - 1 - y) * width * 4;
            pixels.position(topOffset);
            pixels.get(row);
            ByteBuffer bottom = pixels.duplicate();
            bottom.position(bottomOffset).limit(bottomOffset + width * 4);
            pixels.position(topOffset);
            pixels.put(bottom);
            pixels.position(bottomOffset);
            pixels.put(row);
        }
        pixels.position(0);
        pixels.limit(limit);
    }
}
