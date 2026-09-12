package com.brfps.items;

import com.brfps.util.Constants;

/** Consumables that can be looted and carried (max 4, see SPEC.md). */
public enum ItemType {

    MEDKIT("Medkit", Constants.MEDKIT_HEAL, Constants.MEDKIT_USE_TIME),
    ARMOR_VEST("Armor Vest", Constants.ARMOR_VEST_AMOUNT, 1.5f),
    AMMO_BOX("Ammo Box", 0f, 0.5f),
    GRENADE("Grenade", Constants.GRENADE_DAMAGE, 0f);

    private final String displayName;
    private final float amount;
    private final float useTime;

    ItemType(String displayName, float amount, float useTime) {
        this.displayName = displayName;
        this.amount = amount;
        this.useTime = useTime;
    }

    public String displayName() {
        return displayName;
    }

    /** Heal / armor / damage magnitude depending on the type. */
    public float amount() {
        return amount;
    }

    public float useTime() {
        return useTime;
    }
}
