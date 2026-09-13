# SPEC — Battle Royale FPS (Android)

> Living document (R47). Read together with [`RULES.md`](RULES.md) (all 50 rules)
> and [`HANDOFF.md`](HANDOFF.md) (what the last model did and what comes next).

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

**Maps (`assets/maps/`)** — no assets needed: maps are procedural JSON
(`index.json` + `*.layout`); geometry is generated vertex-colored by `Arena`
with no textures or models. The river is a translucent strip until M2e carves
the channel; the bridge arrives in M2d.

**Models (`assets/models/`)** — all missing, procedural fallback in use:
`arms_pistol.g3dj`, `arms_rifle.g3dj`, `arms_smg.g3dj`, `arms_shotgun.g3dj`,
`arms_sniper.g3dj`, `enemy_body.g3dj`, `arena.g3dj`, `loot_crate.g3dj`,
`tree.g3dj`. Buildings need **no** model files: since M9 the whole 8-building kit
is generated as vertex-colored geometry by `world/BuildingFactory.java`
(R25 placeholder strategy). Real building art only arrives with the M20 asset swap.

**Map content still missing** (procedural, no files needed): village **well** and
**crop fields** belong to the prop kit (master M18 / repo M2d), as does the bridge
over the river.

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

## Known Issues

Everything here is known and intentional unless marked **bug**. Each item names the
milestone that fixes it (R49).

1. **Not playable yet.** `GameScreen` is a world *viewer*: an orbiting debug camera
   over the island. There is no first-person player, no shooting, no bots. Those
   arrive in M3a–M3c, M5a–M5c and M7a.
2. **No collision.** Terrain is flat and buildings (M2b) are visual-only shells
   until M3c — the camera walks straight through walls. Expected, not a bug.
3. **Debug overlays are ON and split across three booleans** in `Constants`:
   `DEBUG_SHOW_ZONE_MARKERS`, `DEBUG_SHOW_SPAWN_MARKERS`, `DEBUG_SHOW_CHUNK_GRID`.
   R43 requires a single boolean; consolidate when quality tiers land (M10).
4. **Doc ↔ code drift.** SPEC mentions systems that do not exist yet: `SoundManager`
   (M6d), `LootSpawner` (M6a), `ChunkManager` (M2e), `BotController` (M7a),
   `LocalSaveManager` (M6c). `enemies/BotDifficulty.java` is a placeholder for
   `ai/BotProfile.java` — the package moves at M7c (R35/R36).
5. **No local toolchain in the AI sandbox** (no JDK, no Android SDK): every build is
   verified through GitHub Actions only (R18). Never claim a build passed without a
   CI run id.
6. **Texture size rule conflict.** R12 allows up to 2048; SPEC caps textures at
   512×512 for mobile. Decision: the **512 cap stands**; R12 still enforces
   power-of-two. Only UI atlases may exceed it, and only with the user's approval.
7. **`assets/maps/desert.layout` does not exist** (M12). `index.json` lists one map.
8. **River is a translucent strip**, not a carved channel; the bridge is missing.
   Channel + heightfield: M2e. Bridge: M2d.
9. **`ScreenshotUtil` writes to app-internal storage** 5 s after entering the world
   (no permission needed). It is a dev aid and must be disabled in release builds (R43).
10. **Milestone numbering existed in two schemes** (this repo's lettered ids and the
    master prompt's M1–M47). Decided by the user on 2026-09-13: old commits keep
    their repo ids, **new commits use master ids**, and docs always write both
    (`M9 (master) = M2b (repo)`). See *Milestone Numbering* + *Milestone Mapping Table*.
11. **Buildings are visual-only (M9).** 32 buildings are baked into **one** mesh
    (6 188 vertices / 3 094 triangles) by `BuildingBatcher`; there is no collision,
    no occlusion culling and no LOD yet — those arrive with master M3 (repo M3c) and
    master M19 (repo M2e). Interiors exist only for the large types
    (`house_two_storey`, `shop`, `temple`, and later `warehouse`/`factory`/
    `barracks`) — user decision 2026-09-13; small houses and huts are closed shells
    with a door panel.
12. **One house sits on the river bank.** `house_small` at (100, 6) is 0.25 m from
    the river's 12 m band. Intentional for now; it must be moved or the bank must be
    shaped when master M8/M19 (repo M2e) carves the channel to −1.5 m.
13. **Shop awnings overhang 1.5 m** past the front wall (and temple steps 1.8 m).
    Placement was validated against roads/river/spawns including that overhang, so
    keep the same margin when adding buildings: `python3`-style clearance ≥ 0 m from
    any road strip, river band or spawn point.

## Performance Budget

Hard limits (R28–R31) and how the budget is spent. Measured values come from the
in-game `FPS | DC | Tri` HUD.

| Resource | Limit | M2a | M9 actual | Reserved plan |
|---|---|---|---|---|
| Draw calls / frame | < 80 | 7 | **8** | terrain 1 · roads 1 · water 1 · river 1 · markers 2 · chunk grid 1 · buildings **1** · props 3 · bots 10 · weapon arms 2 · safe-zone wall 1 · decals/particles 4 · HUD ~8 ⇒ **≈37**, headroom ≈43 |
| Visible triangles | < 80 000 | ≈3 200 | **≈6 300** | terrain 4 000 (18 000 after the M19/M2e heightfield) · buildings **3 094 measured** (12 000 reserved for M16/M17) · props 15 000 · bots 9 000 · weapons 3 000 · safe zone 2 000 · misc 4 000 ⇒ **≈63 000 worst case** |
| Baked vertices | short indices ⇒ ≤ 32 767 per mesh | n/a | **6 188** (1 mesh, flush limit 24 000 in `Constants.BUILDING_VERTEX_LIMIT`) | each new zone batch flushes automatically before the limit |
| Frame allocations | 0 | 0 | 0 | all meshes baked at load via `MeshKit`; render path reuses `Color`/`Vector3` fields; bullets, particles and damage numbers pooled (R32) |
| FPS | desktop 60, Android ≥ 30 (SD 660-class) | n/a (viewer only) | n/a (viewer only) | pixel ratio ≤ 1.5; frustum culling per chunk (R33); LOD from M19/M2e |
| Texture memory | 512×512 max, POT, PNG | 0 (vertex colors) | 0 (vertex colors) | shared atlas per material family; no per-building textures before the M20 asset swap |
| APK size | keep < 40 MB | 8.6 MB (debug) | 8.6 MB (no assets added) | `.g3db` + compressed audio in release (M44–M47 / repo M13) |

Budget rule: if a milestone would push draw calls over 60 or triangles over 70 000,
batch more aggressively or cut detail **before** committing, and record the number in
HANDOFF.md.

## Milestone Numbering (read this first)

Two numbering schemes exist in this project's history. Both are valid; this is how
they are used so that no model ever has to guess:

1. **Historical commits keep their repo ids.** `M0`, `M1` and `M2a` are already
   merged with lettered ids (`M2a: island terrain, zone markers, roads, JSON map
   layout`). Old commits, tags and PR titles are never renamed or rewritten.
2. **Every new milestone uses the master-prompt id M1–M47.** Commits, PRs, SPEC rows
   and HANDOFF entries from 2026-09-13 onward are numbered in the master sequence
   (example: `M9: town and village buildings`).
3. **Docs always show both ids together** — `M9 (master) = M2b (repo)` — so a note
   written under either scheme can be matched in one lookup. `HANDOFF.md` entries
   are titled that way and add a `Sequence:` line, e.g.
   **`M3 (master) = after M2a (repo)`**.
4. The **Milestone Mapping Table** below is the single translation source.
5. **Two tracks run in parallel**, because the repo started with the world while the
   master prompt starts with game feel. The user chose this order on 2026-09-13, so
   R1 ("never skip milestones") applies **inside a track**, not across tracks:
   - **Track A — world & content:** M9 → M16 → M17 → M18 → M19 → M20 → M21
   - **Track B — feel, combat & systems:** M2 → M3 → M4 → M5 → M6 → M7 → M8 → M10 → M11 → …

## Milestone Status

### Done

| Git id | Master id | Scope | Result |
|---|---|---|---|
| M0 | — (docs task) | SPEC + RULES + HANDOFF documentation | ✅ 2026-09-13 |
| M1 | M1 | Project skeleton + CI green | ✅ CI green, `brfps-debug-apk` 8.6 MB |
| M2a | part of M8 + M14 | Island terrain, zone markers, roads, JSON map layout | ✅ 3.2k tris, 7 draw calls |
| **M9** | **M9** (= repo M2b) | **Town + village buildings** | ✅ 2026-09-13 — 32 buildings, 3 094 tris, **1** draw call |

### Next candidates — waiting for the user's "go" (R4)

| Pick | Master id | Repo id | Scope | Blocked by |
|---|---|---|---|---|
| **A (recommended)** | M3 | M3a, M3b, M3c | First-person camera + walk/sprint/jump/crouch + touch controls — makes the game playable | nothing |
| B | M16 + M17 | M2c | Industrial zone + military base (continues Track A) | nothing |
| C | M2 | — | Editor view: world grid + XYZ gizmo + free-fly camera (parked by the user on 2026-09-13) | nothing |
| D | M18 | M2d | Props: trees, cars, barrels, crates, bridge, village well + crop fields | nothing |

M14/M15 ("full town" / "full village") are **substantially covered by M9**: the town
already has 20 buildings on a road grid and the village 12 (8 huts + temple + 3
houses). What is still missing from those two rows is props, the village well and
crop fields — all of which belong to M18.

### Milestone Mapping Table

Repo ids are the lettered ones used by the first three commits; master ids are the
flat M1–M47 list from the project's master prompt. "—" means the master milestone
has no separate repo row (its work is folded into the listed one).

| Master | Repo | Scope (master wording) | Status |
|---|---|---|---|
| M1 | M1 | Skeleton + CI | ✅ |
| M2 | — | Editor view (grid + XYZ axes + free camera) | parked by user |
| M3 | M3a, M3b, M3c | First-person camera + movement | next (option A) |
| M4 | M5a, M5b | Shooting + raycast + hit feedback | not started |
| M5 | M5c | Screen shake + muzzle flash + hit marker | not started |
| M6 | — (folded into M3c) | Head bob + footsteps + sprint FOV | not started |
| M7 | M3a | Touch controls polish | not started |
| M8 | M2e | Hills terrain + noise heightmap + lighting | not started |
| M9 | **M2b** | 5+ buildings with interiors → town + village kit | ✅ done |
| M10 | M7a | 3 bots (patrol + shoot) | not started — needs M3, M4 |
| M11 | M8 | Match loop (win/lose/restart) | not started |
| M12 | M6a, M6b | Loot + inventory | not started |
| M13 | — (new: sound pass) | Sound pass (all SFX via `SoundManager`) | not started |
| M14 | M2b (+ M2d props) | Full town: 20 buildings + roads + props | buildings ✅, props pending |
| M15 | M2b (+ M2d props) | Full village: 12 huts + well + temple | buildings ✅, well/fields pending |
| M16 | M2c | Industrial zone | not started |
| M17 | M2c | Military base | not started |
| M18 | M2d | Props (trees, cars, barrels, crates, bridge) | not started |
| M19 | M2e | Chunk loading + LOD + frustum culling | not started |
| M20 | — (new: asset swap) | Real asset swap (Kenney/Quaternius CC0) | not started |
| M21 | — (new: lightmaps) | Baked lighting + lightmaps | not started |
| M22 | M5a, M5b | 5 weapons | not started |
| M23 | M6a | Items (medkit, armor, grenade, ammo box) | not started |
| M24 | M8 | Safe zone (shrink, damage, visual) | not started |
| M25 | M7c | Bot rank profiles (16 params × 7 ranks) | not started |
| M26 | M7b | Bot behavior tree | not started |
| M27 | M7d | Bot advanced tactics | not started |
| M28 | M9b | Rank + RP system | not started |
| M29 | M9b | Level + XP system | not started |
| M30 | M6c | Save system (Preferences) | not started |
| M31 | M4 | Landing system (plane, parachute, drop) | not started |
| M32 | M9a | Main menu | not started |
| M33 | M9a | Lobby (loadout, character, map select) | not started |
| M34 | M5a | In-game HUD | not started |
| M35 | M5a | Minimap + safe zone circle | not started |
| M36 | M9c | Kill feed + damage numbers | not started |
| M37 | M9c | Post-match screen | not started |
| M38 | M10 | Graphics tiers | not started |
| M39 | M11 | Optimization pass | not started |
| M40 | M12 | Second map (desert, JSON only) | not started |
| M41 | M12 | Multi-map system | not started |
| M42 | — (new: audio polish) | Audio polish (music, ambient, 3D spatial) | not started |
| M43 | — (new: balance pass) | Balance pass (weapons, bots, difficulty) | not started |
| M44 | M13a | Release signing (keystore, Actions, AAB) | not started |
| M45 | M13b | Play Store prep | not started |
| M46 | M13c | Beta testing + fix pass | not started |
| M47 | M13d | Launch | not started |
| — | M14+ | Real PvP | explicitly out of scope (R8) |

---

## Multi-Map System (since M2a)

- Building kit is fixed (8 buildings + 7 props) and reused across all maps.
  Since M9 the 8 building ids are: `house_small`, `house_two_storey`, `shop`,
  `hut`, `temple`, `warehouse`, `factory`, `barracks` — sizes, heights, roof style,
  plinth and palette per type live in `world/BuildingType.java`; shared dimensions
  (wall thickness, door, window, roof pitch/overhang/slab, floor offset) live in
  `util/Constants.java` (R7). Geometry is generated by `world/BuildingFactory.java`
  and batched into one mesh per map by `world/BuildingBatcher.java`.
- Every map is pure data: `assets/maps/index.json` lists the maps,
  `assets/maps/<id>.layout` describes each one. Adding a new map must not
  require code changes.
- `MapRegistry` (`world/`) parses the index; `MapLayout` (`world/`) parses a
  layout file. Unknown JSON fields are ignored (forward-compatible). An unknown
  `buildings[].type` is logged and skipped, never fatal (R25).

### Layout JSON schema (center origin: (0,0) is the map center, range ±size/2)

| Field | Type | Notes |
|---|---|---|
| `id`, `name`, `version` | string/string/int | identity |
| `size` | float | full map side length in meters (island: 300) |
| `terrain` | string | theme label (`tropical`, later `desert`, `snow`) |
| `water` | bool | render the surrounding water plane |
| `waterLevel` | float | water plane height |
| `waterMargin` | float | how far water extends past the terrain edge |
| `beachWidth` | float | sandy ring inside the terrain edge |
| `zones[]` | array | `id`, `name`, `x`, `z`, `width`, `depth`, `ground` |
| `roads[]` | array | `from: [x,z]`, `to: [x,z]`, `width` |
| `rivers[]` | array | `points: [[x,z]...]` polyline, `width`, `depth` (channel carved in M2e; bridge in M2d) |
| `buildings[]` | array | `type`, `x`, `z`, `rotation` (yaw in degrees, local +Z = front). Filled since M9: 32 on the island (20 town + 12 village) |
| `props[]` | array | `type`, `x`, `z`, `rotation`, `scale` (filled from master M18 / repo M2d) |
| `spawnPoints[]` | array | `x`, `z`, `zone` (zone tag is advisory) |
| `lootSpawns[]` | array | `x`, `z` (used from M6a) |

`zones[].ground` is a fixed enum mapped to colors in `Arena.GROUND_COLORS`
(map-specific data lives in Arena, not Constants): `grass`, `dry_grass`,
`dirt`, `concrete`, `asphalt`. Unknown ground types fall back to grass with an
error log — a new map can never crash the game.

### Map performance rules

- Draw calls budget < 80; visible triangles budget < 80,000.
- M2a baseline: 40×40 terrain segments (~3.2k tris, 7 draw calls).
- M9 baseline: **~6.3k tris, 8 draw calls** — the 32 buildings cost exactly one
  extra draw call because `BuildingBatcher` bakes them into a single mesh.
  Raise segmentation in master M19 (repo M2e) together with the heightfield,
  never on flat maps.
- Building placement rule (validated for M9): a building's footprint **including**
  its front overhang (shop awning 1.5 m, temple steps 1.8 m, temple plinth 0.8 m all
  round) must not overlap another building, a road strip, the river band or a spawn
  point, and should stay inside its zone rectangle.
- Debug visuals toggle in `Constants.java`: `DEBUG_SHOW_ZONE_MARKERS`
  (set false after master M5), `DEBUG_SHOW_SPAWN_MARKERS` (remove after master M31),
  `DEBUG_SHOW_CHUNK_GRID` (preview of the M19/M2e 10×10 × 30 m chunking).
- Verification on-device: `GameScreen` shows `FPS | DC | Tri` top-right and
  saves `debug/screenshot.png` (app-internal storage, no permissions) 5 s after
  entering. Pull it with: `adb shell run-as com.brfps cat debug/screenshot.png > shot.png`
