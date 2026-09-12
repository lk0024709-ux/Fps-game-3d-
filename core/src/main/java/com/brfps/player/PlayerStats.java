package com.brfps.player;

import com.brfps.util.Constants;

/** Health, armor and match score for the local player or a bot. */
public class PlayerStats {

    private float health = Constants.MAX_HEALTH;
    private float armor;
    private int kills;
    private boolean alive = true;

    public float getHealth() {
        return health;
    }

    public float getArmor() {
        return armor;
    }

    public int getKills() {
        return kills;
    }

    public boolean isAlive() {
        return alive;
    }

    public void addKill() {
        kills++;
    }

    public void heal(float amount) {
        health = Math.min(Constants.MAX_HEALTH, health + amount);
    }

    public void addArmor(float amount) {
        armor = Math.min(Constants.MAX_ARMOR, armor + amount);
    }

    /**
     * Applies damage. Armor soaks up ARMOR_ABSORB of the incoming hit while it
     * lasts. Returns the health actually lost.
     */
    public float damage(float amount) {
        if (!alive || amount <= 0f) {
            return 0f;
        }
        float toHealth = amount;
        if (armor > 0f) {
            float absorbed = amount * Constants.ARMOR_ABSORB;
            float used = Math.min(armor, absorbed);
            armor -= used;
            toHealth = amount - used;
        }
        float before = health;
        health -= toHealth;
        if (health <= 0f) {
            health = 0f;
            alive = false;
        }
        return before - health;
    }

    public void reset() {
        health = Constants.MAX_HEALTH;
        armor = 0f;
        kills = 0;
        alive = true;
    }
}
