package com.brfps.weapons;

import com.brfps.player.ScreenShake;
import com.brfps.util.Constants;

/**
 * Everything a shot does to the camera (master M5, repo M5c): the transient recoil
 * offset and the additive camera shake, plus the crosshair bloom derived from them.
 * Extracted from {@code screens.GameScreen} so that screen keeps its headroom under the
 * 300-line limit (R13); the on-screen widgets (muzzle flash, hit marker) stay in
 * {@code ui} and are fired by the screen from the same shot event.
 *
 * <p>The screen must call this in the fixed effect order
 * <b>look -> movement -> recoil -> shake -> camera apply -> weapons</b>: movement reads
 * the base yaw before any offset exists, and the offsets are applied to the camera
 * after movement so a shot can never push the player around.
 */
public class WeaponFeel {

    private final RecoilState recoil = new RecoilState();
    private final ScreenShake shake = new ScreenShake();

    /**
     * Runs the feedback for one frame.
     *
     * @param triggerHeld keeps a spray's recoil alive until the player lets go
     */
    public void update(float delta, boolean triggerHeld) {
        recoil.update(delta, triggerHeld);
        shake.update(delta);
    }

    /**
     * Applies one shot: the weapon's kick in degrees becomes a recoil offset and a
     * proportional shake impulse ({@code Constants.SHAKE_PER_RECOIL_DEGREE}).
     */
    public void onShot(float recoilDegrees) {
        recoil.kick(recoilDegrees);
        shake.add(recoilDegrees * Constants.SHAKE_PER_RECOIL_DEGREE);
    }

    public RecoilState getRecoil() {
        return recoil;
    }

    public ScreenShake getShake() {
        return shake;
    }

    /** 0 settled, 1 full kick — widens the crosshair while the gun climbs. */
    public float getBloom() {
        return recoil.intensity();
    }
}
