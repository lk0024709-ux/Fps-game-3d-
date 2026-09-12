# SPEC — Battle Royale FPS (Android)

**Genre:** Battle Royale FPS
**Players per match:** 1 human + 9 AI bots (later: scale toward 49 bots)
**Match duration:** ~10 minutes, or until 1 player is left
**View:** First person, 75° FOV
**Online features:** none. Offline only — no login, no store, no cloud save.

---

## Core Loop

1. Spawn in the arena with no weapon.
2. Loot weapons and items from the ground.
3. Fight bots, survive.
4. The safe zone shrinks over time, forcing players together.
5. Last player alive wins.

---

## Player

| Property | Value |
|---|---|
| Walk speed | 4 m/s |
| Sprint speed | 7 m/s |
| Crouch speed | 2 m/s |
| Health | 100 |
| Armor | 100 (absorbs 50% of incoming damage) |
| Gravity | -9.8 m/s² |
| Jump height | 1.2 m |
| Eye height | 1.6 m (1.0 m crouched) |
| Carry capacity | 2 weapons, 4 items |
| Pickup | Auto on proximity (1.5 m) + manual button |

---

## Weapons

| Weapon | Damage | Fire rate | Magazine | Range | Type |
|---|---|---|---|---|---|
| Pistol | 15 | 300 ms | 12 | 40 m | hitscan |
| SMG | 12 | 80 ms | 30 | 30 m | hitscan |
| Rifle | 25 | 150 ms | 30 | 80 m | hitscan |
| Shotgun | 60 | 800 ms | 6 | 15 m | hitscan |
| Sniper | 90 | 1500 ms | 5 | 200 m | projectile with drop |

Implemented in `core/.../weapons/WeaponType.java` — that enum is the single
source of truth for these numbers.

---

## Items

| Item | Effect | Use time |
|---|---|---|
| Medkit | +50 HP | 3 s |
| Armor Vest | +50 armor | 1.5 s |
| Ammo Box | Refills current weapon | 0.5 s |
| Grenade | 80 damage in a 5 m radius | throwable |

---

## Safe Zone

- Starts at 60 m radius.
- Shrinks one step every 90 s (to 60% of the previous target radius, floor 5 m).
- Outside the zone: 5 damage per second.
- Visual: blue translucent wall.

---

## Enemy AI (bots)

- State machine: `patrol → chase → attack → flee` (flee below 25 HP).
- Spots the player within 30 m → chase. Within 15 m → shoot.
- Accuracy: 40% at mid range (≤30 m), 20% at long range.
- Bot HP 100, no armor.
- Bot weapons: random from pistol / SMG / rifle.

---

## UI / HUD

- Crosshair (center, reacts on hit)
- Health bar + armor bar (bottom-left)
- Ammo counter (bottom-right, `24 / 30`)
- Minimap (top-left) with the safe-zone circle
- Player counter (top-center, `7 alive`)
- Kill feed (top-right)
- Virtual joystick (left, movement)
- Touch-look area (right half, drag to aim; pitch clamped -80°..+80°)
- Action buttons (right): fire (large, bottom-right), jump, crouch, reload,
  pickup, weapon switch

---

## Match Flow

`Splash → Main Menu → Lobby (loadout) → Game → Pause → Result`

**Lobby (simplified):** choose 2 weapons from the unlocked list (starts with
pistol only), choose a character skin (placeholder), Start Match.

---

## Art / Performance Rules

- Low-poly, stylized and colorful — not realistic.
- Textures max 512×512, power-of-two only, PNG with alpha.
- Simple Lambert-style lighting. No PBR.
- No real-time shadows — fake blob shadow under characters.
- Target 60 FPS on Snapdragon 660-class hardware, 30 FPS minimum.
- Pixel ratio capped at 1.5.
- Object pooling for bullets, particles and damage numbers.
- Frustum culling on all models.
- Never allocate inside the render loop.

## 3D Model Format Rules

- `.g3dj` (JSON) during development, `.g3db` for release builds only.
- Pipeline: Blender → FBX (scale 0.01) → `fbx-conv` → `.g3dj`.
- First person loads only `arms_<weapon>.g3dj` (hands + weapon).
- Bots use `enemy_body.g3dj`.
- A missing model falls back to `ModelBuilder.createBox` and is listed below.
- The deprecated `.g3d` format is never used.

---

## Missing Assets

Everything below is currently a procedural placeholder (box / colored quad /
silent stub). Replace with CC0 low-poly art (Kenney, Quaternius, Meshy) — never
use Free Fire's own assets.

**Models (`assets/models/`)** — all missing, `ModelBuilder.createBox` fallback:
`arms_pistol.g3dj`, `arms_rifle.g3dj`, `arms_smg.g3dj`, `arms_shotgun.g3dj`,
`arms_sniper.g3dj`, `enemy_body.g3dj`, `arena.g3dj`, `loot_crate.g3dj`,
`tree.g3dj`

**Textures (`assets/textures/`)** — all missing, tinted 2×2 white fallback:
`wall.png`, `floor.png`, `grass.png`, `enemy_skin.png`, `crosshair.png`,
`muzzle_flash.png`

**Sounds (`assets/sounds/`)** — all missing, `SoundManager` no-ops:
`pistol_fire.ogg`, `rifle_fire.ogg`, `smg_fire.ogg`, `shotgun_fire.ogg`,
`sniper_fire.ogg`, `reload.ogg`, `hit.ogg`, `enemy_death.ogg`, `footstep.ogg`,
`pickup.ogg`

**UI (`assets/ui/`)** — missing, libGDX default `BitmapFont` used instead:
`font.fnt`, `font.png`

---

## Milestone Status

| Milestone | Scope | Status |
|---|---|---|
| M1 | Project skeleton + CI green | in progress |
| M2 | 3D arena + first-person camera | not started |
| M3 | Player movement + touch controls | not started |
| M4 | Weapons + shooting | not started |
| M5 | Loot system + inventory | not started |
| M6 | Enemy bots + combat | not started |
| M7 | Battle royale mechanics | not started |
| M8 | UI polish + lobby | not started |
| M9 | Optimization + release prep | not started |
| M10 | Release signing (on request only) | not started |
