package com.brfps.items;

/** One carried consumable stack. */
public class Item {

    private final ItemType type;
    private int count;

    public Item(ItemType type) {
        this(type, 1);
    }

    public Item(ItemType type, int count) {
        this.type = type;
        this.count = Math.max(1, count);
    }

    public ItemType getType() {
        return type;
    }

    public int getCount() {
        return count;
    }

    public void add(int amount) {
        count += amount;
    }

    /** Consumes one unit. Returns true when the stack is now empty. */
    public boolean consumeOne() {
        count--;
        return count <= 0;
    }
}
