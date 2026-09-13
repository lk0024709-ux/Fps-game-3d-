package com.brfps.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.utils.Disposable;
import com.brfps.util.Constants;

/**
 * Placeholder gunshot: one short generated beep per shot, so the muzzle flash is not
 * silent until the real sound pass lands (master M13 replaces this whole class with
 * {@code SoundManager} + {@code WeaponType.fireSoundPath()} OGG files). The sample is
 * synthesised once at construction — a damped sine plus an octave, exponential decay —
 * and streamed on a single low-priority daemon thread, because {@code AudioDevice.write}
 * blocks for real time and must never stall the render loop (R6, R31).
 */
public class ShotBeep implements Disposable {

    private final short[] samples = new short[Constants.BEEP_SAMPLES];
    private final Object lock = new Object();
    private final Thread worker;
    private boolean pending;
    private boolean disposed;
    private boolean available;

    public ShotBeep() {
        buildSamples();
        available = Gdx.audio != null;
        worker = new Thread(this::run, "ShotBeep");
        worker.setDaemon(true);
        worker.setPriority(Thread.MIN_PRIORITY);
        worker.start();
    }

    /** One 16-bit mono crack: sharp attack, exponential decay, fundamental + octave. */
    private void buildSamples() {
        double step = (2d * Math.PI * Constants.BEEP_FREQUENCY) / Constants.BEEP_SAMPLE_RATE;
        double decay = Constants.BEEP_DECAY_PER_SECOND / Constants.BEEP_SAMPLE_RATE;
        for (int i = 0; i < samples.length; i++) {
            double phase = step * i;
            double wave = Math.sin(phase) + Constants.BEEP_HARMONIC * Math.sin(2d * phase);
            double envelope = Math.exp(-decay * i);
            if (i < Constants.BEEP_ATTACK_SAMPLES) {
                envelope *= (double) i / Constants.BEEP_ATTACK_SAMPLES; // no click on set
            }
            double value = wave * envelope * Constants.BEEP_GAIN;
            samples[i] = (short) Math.max(-32767d, Math.min(32767d, value * 32767d));
        }
    }

    /** Requests one beep. Costs a monitor notify; the audio is written off-thread. */
    public void play() {
        if (!Constants.SOUND_ENABLED || !available) {
            return;
        }
        synchronized (lock) {
            if (disposed) {
                return;
            }
            pending = true; // coalesces a fast spray into one crack per frame at most
            lock.notify();
        }
    }

    private void run() {
        while (true) {
            synchronized (lock) {
                while (!pending && !disposed) {
                    try {
                        lock.wait();
                    } catch (InterruptedException ignored) {
                        return;
                    }
                }
                if (disposed) {
                    return;
                }
                pending = false;
            }
            writeBeep();
        }
    }

    /** Streams the whole sample; the device closes itself, so shots never pile up. */
    private void writeBeep() {
        AudioDevice device = null;
        try {
            device = Gdx.audio.newAudioDevice(Constants.BEEP_SAMPLE_RATE, true);
            if (device != null) {
                device.setVolume(Constants.BEEP_VOLUME);
                device.writeSamples(samples, 0, samples.length);
            }
        } catch (RuntimeException e) {
            available = false; // a device that cannot be created stays off (R25 stub)
            Gdx.app.error("ShotBeep", "audio device unavailable: " + e.getMessage());
        } finally {
            if (device != null) {
                device.dispose();
            }
        }
    }

    @Override
    public void dispose() {
        synchronized (lock) {
            disposed = true;
            lock.notifyAll();
        }
        available = false;
    }
}
