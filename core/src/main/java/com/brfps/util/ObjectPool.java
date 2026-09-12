package com.brfps.util;

import com.badlogic.gdx.utils.Array;

/**
 * Minimal fixed-capacity object pool. Pre-allocates everything up front so the
 * render loop never triggers GC.
 */
public class ObjectPool<T> {

    /** Factory used to pre-fill the pool. */
    public interface Factory<T> {
        T create();
    }

    /** Optional reset hook invoked when an object is returned. */
    public interface Resetter<T> {
        void reset(T obj);
    }

    private final Array<T> free;
    private final Resetter<T> resetter;

    public ObjectPool(int capacity, Factory<T> factory) {
        this(capacity, factory, null);
    }

    public ObjectPool(int capacity, Factory<T> factory, Resetter<T> resetter) {
        this.free = new Array<T>(false, capacity);
        this.resetter = resetter;
        for (int i = 0; i < capacity; i++) {
            free.add(factory.create());
        }
    }

    /** Returns a pooled object, or null when the pool is exhausted (never allocates). */
    public T obtain() {
        if (free.size == 0) {
            return null;
        }
        return free.pop();
    }

    public void release(T obj) {
        if (obj == null) {
            return;
        }
        if (resetter != null) {
            resetter.reset(obj);
        }
        free.add(obj);
    }

    public int available() {
        return free.size;
    }
}
