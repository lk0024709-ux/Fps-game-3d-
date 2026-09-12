package com.brfps.player;

import com.badlogic.gdx.utils.Array;
import com.brfps.items.Item;
import com.brfps.items.ItemType;
import com.brfps.util.Constants;
import com.brfps.weapons.Weapon;
import com.brfps.weapons.WeaponType;

/** Two weapon slots plus four item slots, exactly as in SPEC.md. */
public class PlayerInventory {

    private final Array<Weapon> weapons = new Array<Weapon>(true, Constants.MAX_WEAPONS);
    private final Array<Item> items = new Array<Item>(true, Constants.MAX_ITEMS);
    private int activeSlot;

    public Array<Weapon> getWeapons() {
        return weapons;
    }

    public Array<Item> getItems() {
        return items;
    }

    public Weapon getActiveWeapon() {
        if (weapons.size == 0) {
            return null;
        }
        if (activeSlot >= weapons.size) {
            activeSlot = 0;
        }
        return weapons.get(activeSlot);
    }

    public int getActiveSlot() {
        return activeSlot;
    }

    /** Adds a weapon when a slot is free; otherwise replaces the active one. */
    public boolean addWeapon(WeaponType type) {
        if (weapons.size < Constants.MAX_WEAPONS) {
            weapons.add(new Weapon(type));
            activeSlot = weapons.size - 1;
            return true;
        }
        weapons.set(activeSlot, new Weapon(type));
        return true;
    }

    /** Cycles to the next weapon slot. */
    public void switchWeapon() {
        if (weapons.size > 1) {
            activeSlot = (activeSlot + 1) % weapons.size;
        }
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
        weapons.clear();
        items.clear();
        activeSlot = 0;
    }
}
