package com.brfps.player;

import com.badlogic.gdx.utils.Array;
import com.brfps.items.Item;
import com.brfps.items.ItemType;
import com.brfps.util.Constants;
import com.brfps.weapons.Weapon;
import com.brfps.weapons.WeaponCategory;
import com.brfps.weapons.WeaponType;

/**
 * Four weapon slots plus four item slots (SPEC: weapon slots). Slots 0-1 hold any
 * PRIMARY weapon, slot 2 holds the PISTOL, slot 3 holds a MELEE weapon — empty means
 * bare FIST, always selectable. Category routing only; the HUD box display is master
 * M34 phase A2 and ground pickup is master M12.
 */
public class PlayerInventory {

    private final Weapon[] slots = new Weapon[Constants.WEAPON_SLOT_COUNT];
    private final Array<Item> items = new Array<Item>(true, Constants.MAX_ITEMS);
    private int activeSlot;

    /** Slot count: two primaries, one pistol, one melee/fist. */
    public int getSlotCount() {
        return Constants.WEAPON_SLOT_COUNT;
    }

    /** Weapon in a slot, or null (slot 3 null = bare fists). Out of range = null. */
    public Weapon getSlot(int slot) {
        if (slot < 0 || slot >= Constants.WEAPON_SLOT_COUNT) {
            return null;
        }
        return slots[slot];
    }

    public int getActiveSlot() {
        return activeSlot;
    }

    /** Active weapon, or null when bare fists are selected (empty melee slot). */
    public Weapon getActiveWeapon() {
        return slots[activeSlot];
    }

    /** True when the active slot is bare fists (no melee weapon carried). */
    public boolean isFistActive() {
        return activeSlot == meleeSlot() && slots[activeSlot] == null;
    }

    /** Selects a slot; the melee slot is always allowed (fists are the fallback). */
    public void switchTo(int slot) {
        if (slot < 0 || slot >= Constants.WEAPON_SLOT_COUNT) {
            return;
        }
        if (slot != meleeSlot() && slots[slot] == null) {
            return;
        }
        activeSlot = slot;
    }

    /** Cycles to the next selectable slot (carried guns plus always-available fists). */
    public void switchWeapon() {
        for (int step = 1; step <= Constants.WEAPON_SLOT_COUNT; step++) {
            int slot = (activeSlot + step) % Constants.WEAPON_SLOT_COUNT;
            if (slot == meleeSlot() || slots[slot] != null) {
                activeSlot = slot;
                return;
            }
        }
    }

    /**
     * Routes a picked-up weapon into its category slot: primaries fill slot 0, then
     * 1, then replace the active primary (master M12 asks the player which one to
     * drop); pistols and melee swap in place. Fists are never picked up.
     */
    public boolean addWeapon(WeaponType type) {
        WeaponCategory category = type.category();
        if (category == WeaponCategory.PRIMARY) {
            for (int slot = 0; slot < Constants.PRIMARY_SLOTS; slot++) {
                if (slots[slot] == null) {
                    slots[slot] = new Weapon(type);
                    activeSlot = slot;
                    return true;
                }
            }
            int slot = isPrimarySlot(activeSlot) ? activeSlot : 0;
            slots[slot] = new Weapon(type);
            activeSlot = slot;
            return true;
        }
        if (category == WeaponCategory.PISTOL) {
            slots[pistolSlot()] = new Weapon(type);
            activeSlot = pistolSlot();
            return true;
        }
        if (category == WeaponCategory.MELEE) {
            slots[meleeSlot()] = new Weapon(type);
            activeSlot = meleeSlot();
            return true;
        }
        return false;
    }

    /** Pistol slot index, right after the primary slots. */
    public static int pistolSlot() {
        return Constants.PRIMARY_SLOTS;
    }

    /** Melee/fist slot index, right after the pistol slot. */
    public static int meleeSlot() {
        return Constants.PRIMARY_SLOTS + Constants.PISTOL_SLOTS;
    }

    private static boolean isPrimarySlot(int slot) {
        return slot >= 0 && slot < Constants.PRIMARY_SLOTS;
    }

    public Array<Item> getItems() {
        return items;
    }

    /** Stacks into an existing item of the same type, else takes a free slot. */
    public boolean addItem(ItemType type) {
        for (int i = 0; i < items.size; i++) {
            if (items.get(i).getType() == type) {
                items.get(i).add(1);
                return true;
            }
        }
        if (items.size >= Constants.MAX_ITEMS) {
            return false;
        }
        items.add(new Item(type));
        return true;
    }

    /** Removes one unit of the given item. Returns true when one was consumed. */
    public boolean consumeItem(ItemType type) {
        for (int i = 0; i < items.size; i++) {
            Item item = items.get(i);
            if (item.getType() == type) {
                if (item.consumeOne()) {
                    items.removeIndex(i);
                }
                return true;
            }
        }
        return false;
    }

    public void clear() {
        for (int i = 0; i < slots.length; i++) {
            slots[i] = null;
        }
        items.clear();
        activeSlot = 0;
    }
}
