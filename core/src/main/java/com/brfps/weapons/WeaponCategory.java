package com.brfps.weapons;

/**
 * Which inventory slot a weapon belongs in: SMG, shotgun, MG, rifle, sniper and
 * marksman/DMR are all PRIMARY (slots 0-1), pistols are PISTOL (slot 2), blades and
 * blunt weapons are MELEE and bare hands are FIST (slot 3, the empty-melee fallback).
 */
public enum WeaponCategory {

    /** Any primary gun: SMG, SG, MG, rifle, sniper, DMR (Box 1-2). */
    PRIMARY,

    /** Pistols only: Glock, M1911, Desert Eagle class (Box 3). */
    PISTOL,

    /** Knife, pan, machete (Box 4 when carried). */
    MELEE,

    /** Unarmed default (Box 4 when no melee is carried, never picked up). */
    FIST
}
