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

| Weapon | Damage | Fire rate | Magazine | Range | Reload | Recoil | Type |
|---|---|---|---|---|---|---|---|
| Pistol | 15 | 300 ms | 12 | 40 m | 1.4 s | 1.2° | hitscan |
| SMG | 12 | 80 ms | 30 | 30 m | 1.8 s | 1.0° | hitscan |
| Rifle | 25 | 150 ms | 30 | 80 m | 2.2 s | 1.6° | hitscan |
| Shotgun | 60 | 800 ms | 6 | 15 m | 2.6 s | 4.5° | hitscan |
| Sniper | 90 | 1500 ms | 5 | 200 m | 3.0 s | 6.0° | projectile with drop |

Implemented in `core/.../weapons/WeaponType.java` — that enum is the single
source of truth for these numbers (fire rate is stored as the interval in seconds,
recoil as the vertical kick in degrees per shot). The enum has carried the reload and
recoil values since M1; this table only started listing them with master M4, and since
**master M5** the recoil column is what the camera actually does — see *Game Feel*.
`armsModelPath()` / `fireSoundPath()` in the same enum point at assets that do not
exist yet (arms: master M20, sounds: master M13 — until then a generated beep stands
in for every gun).

Since master M4 the player spawns with a **Pistol** and a reserve of three magazines
(36 rounds, `Weapon`'s constructor; ammo boxes arrive with master M12). An empty
magazine reloads itself, `R` reloads on demand. Every weapon fires **one** hitscan ray
today: the shotgun's per-pellet spread and the sniper's projectile drop both arrive
with master M22 (all five weapons), which is what the enum's `isProjectile()` flag is
for.

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

Where each element is **today** (master M5) and which milestone finalizes it. The
bottom-right corner belongs to the touch buttons, so the ammo counter sits
bottom-center instead of bottom-right; the real scene2d HUD (master M34/M44) may
re-decide that.

| Element | Now | Finalized by |
|---|---|---|
| Crosshair (center) | 4 ticks + dot from `ui/Crosshair`, dims while reloading, **blooms open with the recoil offset** (M5) | M34 |
| Hit marker | 4 diagonal ticks over the crosshair, 160 ms, on any world impact (`ui/HitMarker`, M5) | M36 (kill/damage colours) |
| Muzzle flash | screen-space glow + star, 70 ms, anchored at a fixed screen fraction (`ui/MuzzleFlash`, M5) | **M20** — moves to the weapon's world-space muzzle attach point |
| Health / armor / stance | top-left text `HP 100 | AR 0 | STAND` | M34 (bars) |
| Ammo counter | bottom-center text `Pistol 12 / 36`, `Pistol RELOADING 70%` | M34 |
| Virtual joystick | left half, appears under the thumb (`ui/VirtualJoystick`) | M7 (polish) |
| Touch-look area | right half drag to aim, pitch clamped −80°..+80° | M7 |
| Action buttons | **FIRE** (large), **JUMP**, **CRCH** bottom-right | M5a layout, M12 adds pickup/switch |
| Perf line `FPS | DC | Tri` (+ `holes`) | top-right, debug builds | removed for release (R43) |
| Position / collider / shot / recoil readout + control hint | top-right + top-left second lines, debug builds | removed for release |
| Minimap (top-left) with safe-zone circle | — | M35 |
| Player counter (top-center, `7 alive`) | — | M11 |
| Kill feed (top-right) | — | M11 |
| Reload / pickup / weapon-switch buttons | auto-reload + `R` key for now | M12, M22 |

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
`tree.g3dj`. Buildings need **no** model files today: since M9 the whole 8-building kit
is generated as vertex-colored geometry by `world/BuildingFactory.java`
(R25 placeholder strategy), and that is still what renders.

**Buildings (`assets/buildings/`) — first real art, unused so far.** Four CC0 houses from
Kenney's *City Kit Suburban (2.0)* landed on 2026-09-13 as the M2b.5 starter pack
(`house_small`, `house_medium`, `house_two_storey`, `shop`, 536 KB, each folder with its
`Textures/colormap.png` and a measured `info.txt`; licence + numbers in
`assets/buildings/README.md`). **No code loads them yet** — the procedural kit keeps
rendering until M2b.5 Phase 1 imports `house_medium` and verifies scale, texture,
orientation and collision against a 6 × 4 × 6 m debug reference box. Still missing after
that: the other 28 placements, the `hut` and `temple` replacements, and everything for
`warehouse` / `factory` / `barracks` (Kenney's *City Kit Industrial* is the candidate,
master M16/M17).

**Map content still missing** (procedural, no files needed): village **well** and
**crop fields** belong to the prop kit (master M18 / repo M2d), as does the bridge
over the river.

**Textures (`assets/textures/`)** — all missing, tinted 2×2 white fallback:
`wall.png`, `floor.png`, `grass.png`, `enemy_skin.png`, `crosshair.png`,
`muzzle_flash.png` (since master M5 the flash uses a **generated** 64×64 radial glow,
`Assets.glow()`, so this file is optional — only needed if art direction wants a
shaped flash at master M20)

**Sounds (`assets/sounds/`)** — all missing. Since master M5 the gunshot is a
**generated** 93 ms beep (`audio/ShotBeep`, one daemon thread, no file on disk) which
stands in for every weapon until `SoundManager` loads the real samples at master M13:
`pistol_fire.ogg`, `rifle_fire.ogg`, `smg_fire.ogg`, `shotgun_fire.ogg`,
`sniper_fire.ogg`, `reload.ogg`, `hit.ogg`, `enemy_death.ogg`, `footstep.ogg`,
`pickup.ogg`

**UI (`assets/ui/`)** — missing, libGDX default `BitmapFont` used instead:
`font.fnt`, `font.png`

---

## Known Issues

Everything here is known and intentional unless marked **bug**. Each item names the
milestone that fixes it (R49).

1. **Playable, shooting and it now feels like a gun — but nothing to shoot.** Since
   master M5 a shot climbs the view, shakes the camera, flashes, beeps, blooms the
   crosshair and confirms an impact with a hit marker. Damage is still not applied to
   anything: there are no bots (master M10 / repo M7a), no loot (M12) and no match loop
   (M11), so the hit marker can only ever mean "a wall took a bullet hole". Real gun
   sounds are master M13; arms, a viewmodel kick and a world-space muzzle flash are
   master M20.
2. **Collision covers buildings only.** 173 CPU-side boxes (walls, door jambs,
   partitions, temple plinth + steps) stop the player; the terrain is still flat, the
   river and sea are not blockers, props do not exist yet, and roofs are unreachable.
   Heightfield collision: master M8 (repo M2e). Prop collision: master M18 (M2d).
3. **Debug overlays are ON**, but since master M2 they all hang off one master switch:
   `Constants.DEBUG_TOOLS_ENABLED` (R43) gates the zone/spawn markers, the chunk grid,
   the editor view and the screenshot helper's visibility. Flip that one boolean to
   false for release. The three sub-flags still exist and should be folded away when
   quality tiers land (master M38 / repo M10).
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
11. **Buildings have collision but no culling (M9 + M3).** 32 buildings are baked into
    **one** mesh (6 188 vertices / 3 094 triangles) by `BuildingBatcher`, and
    `BuildingCollider` mirrors the same geometry as 173 oriented boxes for the player.
    There is no occlusion culling and no LOD yet — those arrive with master M19
    (repo M2e). Interiors exist only for the large types
    (`house_two_storey`, `shop`, `temple`, and later `warehouse`/`factory`/
    `barracks`) — user decision 2026-09-13; small houses and huts are closed shells
    with a door panel, so their doorway is blocked by a visual panel **and** by the
    two jambs only where the panel is: a shell building is not enterable by design.
12. **One house sits on the river bank.** `house_small` at (100, 6) is 0.25 m from
    the river's 12 m band. Intentional for now; it must be moved or the bank must be
    shaped when master M8/M19 (repo M2e) carves the channel to −1.5 m.
13. **Editor view screen furniture is temporary.** The CAM button sits top-left (the
    minimap wants that spot at master M35) and the UP/DN altitude buttons sit
    bottom-right — the same corner the gameplay JUMP/CRCH buttons already use (master
    M3). They never overlap in practice because the editor and the match are separate
    screens, but the real HUD (master M34/M44) owns that corner from now on. Editor
    furniture is debug-only and disappears with `DEBUG_TOOLS_ENABLED`.
14. **Editor mode costs 3 extra draw calls** (grid, world axes, corner gizmo) — 11
    total instead of 8. Editor mode only, never in a match.
15. **Shop awnings overhang 1.5 m** past the front wall (and temple steps 1.8 m).
    Placement was validated against roads/river/spawns including that overhang, so
    keep the same margin when adding buildings: `python3`-style clearance ≥ 0 m from
    any road strip, river band or spawn point.
16. **Every weapon fires exactly one ray.** The shotgun's pellet spread and the
    sniper's projectile drop are master M22 (all five weapons); until then
    `WeaponType.projectile` is data only and the shotgun is a single 60-damage hitscan.
17. **Bullet holes are world-only and finite.** 64 decals in a ring buffer (the oldest
    is overwritten), placed on walls, plinths and ground — never on a bot, because
    bots arrive with master M10 and will need their own impact feedback.
18. **`ui.MatchHud` owns all HUD text** and `screens/GameScreen` only fills
    `ui.HudData`. Keep it that way: GameScreen was at 292/300 lines before the split,
    and R13 caps every file at 300. It sits at **265/300 after master M5** — the feel
    math went to `weapons.WeaponFeel` + `RecoilState` + `player.ScreenShake` and the
    widgets to `ui.MuzzleFlash` + `ui.HitMarker` precisely to keep that headroom.
19. **The muzzle flash is screen-space, not attached to a gun** (master M5). There is no
    arms model yet, so the flash is drawn at a fixed fraction of the screen
    (`MUZZLE_FLASH_X/Y` = 0.50/0.38) in the HUD batch. **It moves to the weapon's
    world-space muzzle attach point at master M20**; until then it will look detached
    while crouching or looking straight down. Recorded here so nobody "fixes" it twice.
20. **The gunshot is a generated beep, not a sample** (`audio/ShotBeep`, master M5): one
    93 ms damped sine, identical for all five weapons, streamed from a daemon thread
    because `AudioDevice.writeSamples` blocks. Master M13 replaces the whole class with
    `SoundManager` + `WeaponType.fireSoundPath()`. `Constants.SOUND_ENABLED` mutes it.
    Beep requests coalesce, so at most one crack plays per frame — an SMG spray sounds
    like a buzz rather than 12.5 distinct shots, which is acceptable for a placeholder.
21. **Recoil is the only accuracy penalty.** The offset moves the camera and the ray
    follows the camera, so a climbing spray genuinely misses — but there is no
    per-bullet cone, no bloom-driven spread and no first-shot-accuracy rule. That is
    master M22 (five weapons + spread), and `RecoilState.intensity()` is already the
    0..1 value a spread cone should read.
22. **Roll and offsets are applied to `camera.up`, never to the stored aim.**
    `FirstPersonCamera.apply(yawOffset, pitchOffset, roll)` adds them for one frame only.
    Anything new that wants to move the view (head bob M6, damage punch, vehicle shake)
    must go through the same call — writing into `yaw`/`pitch` would break the full
    recovery guarantee the user asked for.

## Performance Budget

Hard limits (R28–R31) and how the budget is spent. Measured values come from the
in-game `FPS | DC | Tri` HUD.

| Resource | Limit | M2a | M9 actual | Reserved plan |
|---|---|---|---|---|
| Draw calls / frame | < 80 | 7 | **8** (11 in editor mode: +grid, +axes, +corner gizmo) | terrain 1 · roads 1 · water 1 · river 1 · markers 2 · chunk grid 1 · buildings **1** · props 3 · bots 10 · weapon arms 2 · safe-zone wall 1 · decals/particles 4 · HUD ~8 ⇒ **≈37**, headroom ≈43 |
| Visible triangles | < 80 000 | ≈3 200 | **≈6 300** | terrain 4 000 (18 000 after the M19/M2e heightfield) · buildings **3 094 measured** (12 000 reserved for M16/M17) · props 15 000 · bots 9 000 · weapons 3 000 · safe zone 2 000 · misc 4 000 ⇒ **≈63 000 worst case** |
| Baked vertices | short indices ⇒ ≤ 32 767 per mesh | n/a | **6 188** (1 mesh, flush limit 24 000 in `Constants.BUILDING_VERTEX_LIMIT`) | each new zone batch flushes automatically before the limit |
| Frame allocations | 0 | 0 | 0 | all meshes baked at load via `MeshKit`; render path reuses `Color`/`Vector3` fields; bullets, particles and damage numbers pooled (R32) |
| FPS | desktop 60, Android ≥ 30 (SD 660-class) | n/a (viewer only) | n/a (viewer only) | pixel ratio ≤ 1.5; frustum culling per chunk (R33); LOD from M19/M2e |
| Texture memory | 512×512 max, POT, PNG | 0 (vertex colors) | 0 (vertex colors) | shared atlas per material family; no per-building textures before the M20 asset swap |
| APK size | keep < 40 MB | 8.6 MB (debug) | 8.6 MB (no assets added) | `.g3db` + compressed audio in release (M44–M47 / repo M13) |

Budget rule: if a milestone would push draw calls over 60 or triangles over 70 000,
batch more aggressively or cut detail **before** committing, and record the number in
HANDOFF.md.

M3 cost: **0 extra draw calls, 0 extra triangles, 0 extra textures on disk.** Building
collision is 173 boxes in two flat `float[]` arrays built once at load (≈5.5 KB), and
the touch widgets are 3 quads from a generated 64×64 circle texture drawn in the
existing HUD batch. The first-person HUD is text-only for now.

M4 cost: **+1 draw call and ≤ 128 triangles** from the moment the first bullet hole
exists (8 → 9 draw calls in a match). All 64 decals live in one dynamic mesh (4 KB of
floats, 384 indices built once) and only the 16 floats of the newest quad are
re-uploaded per shot. Crosshair and the four touch buttons reuse the generated circle
and white textures — still no image files, APK unchanged. A shot costs one pass over
171 wall boxes plus 2 plinth boxes, i.e. at most 12.5 shots/s × 173 slab tests for an
SMG; no allocation anywhere in the path (R6).

M5 cost: **+0 draw calls, +0 triangles, +0 image files, ≈16 KB of VRAM.** The muzzle
flash (4 quads), the hit marker (4 quads) and the bloomed crosshair all draw inside the
existing HUD batch, using one new generated 64×64 radial glow (`Assets.glow()`) plus the
white texture that already existed. Recoil and shake are ~40 float operations per frame
with no allocation (R6), and the placeholder beep is 4 KB of PCM synthesised once at
startup on one daemon thread — no sound file ships. Measured in CI: the M5 artifact
`brfps-debug-apk` is **8 624 100 bytes (8.62 MB)**, the same as M4. The first-person
screen stays at 9 draw calls / ≈6.3k triangles in a match.

M2b.5 pre-drop cost (assets on disk, nothing loads them yet): **+536 KB** of CC0 house
GLBs in `assets/buildings/` (4 models + 4 copies of one 11.5 KB shared 512×512 atlas).
Measured in CI (run `34751622623`): the artifact went 8 624 100 → **8 766 987 bytes
(8.77 MB)**, still far inside the 40 MB cap. Phase 2 must
deduplicate the atlas at runtime (one texture for all buildings) and watch the triangle
budget: 32 × ≈1 400 tris ≈ 45k against the 80k limit (R29).

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
   - **Track A — world & content:** M9 ✅ → M16 → M17 → M18 → M19 → M20 → M21
   - **Track B — feel, combat & systems:** M2 ✅ → M3 ✅ → M4 ✅ → M5 ✅ → **M6** → M7 → M8 → M10 → M11 → …
   - **Track A insert (user request, 2026-09-13):** `M2b.5` — swap the procedural
     buildings for the user's downloaded house assets. It sits between M9 and M16 in
     Track A and starts only after the asset drop lands (see *House Asset Integration*).

## Milestone Status

### Done

| Git id | Master id | Scope | Result |
|---|---|---|---|
| M0 | — (docs task) | SPEC + RULES + HANDOFF documentation | ✅ 2026-09-13 |
| M1 | M1 | Project skeleton + CI green | ✅ CI green, `brfps-debug-apk` 8.6 MB |
| M2a | part of M8 + M14 | Island terrain, zone markers, roads, JSON map layout | ✅ 3.2k tris, 7 draw calls |
| **M9** | **M9** (= repo M2b) | **Town + village buildings** | ✅ 2026-09-13 — 32 buildings, 3 094 tris, **1** draw call |
| **M2** | **M2** (no repo letter) | **Editor view** — world grid + XYZ axis gizmo + free-fly camera + corner orientation gizmo | ✅ 2026-09-13 — +3 draw calls in editor mode only |
| **M3** | **M3** (= repo M3a, M3b, M3c) | **First-person camera + movement + touch controls + building collision** | ✅ 2026-09-13 — +0 draw calls, 173 collision boxes, game is now walkable |
| **M4** | **M4** (= repo M5a, M5b) | **Shooting** — hitscan raycast, pistol, magazine/reload, bullet-hole decals, crosshair, FIRE button, ammo HUD | ✅ 2026-09-13 — +1 draw call / +128 tris max, HUD split into `ui.MatchHud` |
| **M5** | **M5** (= repo M5c) | **Game feel** — full-recovery recoil, camera shake + roll, screen-space muzzle flash, hit marker, crosshair bloom, placeholder gunshot beep | ✅ 2026-09-13 — **+0 draw calls**, +0 triangles, one generated 64×64 glow texture, `WeaponFeel` extracted so `GameScreen` stays at 265/300 |

### Next candidates — waiting for the user's "go" (R4)

| Pick | Master id | Repo id | Scope | Blocked by |
|---|---|---|---|---|
| **A (recommended)** | M6 | — (folded into M3c) | Head bob + footsteps + sprint FOV (Track B feel continues) | nothing (M5 done) |
| B | **M2b.5** (new) | — | **House asset integration**: replace the procedural placeholder buildings with the user's downloaded models, one building first, then all 32 | user's asset drop (format + count pending) |
| C | M10 | M7a | 3 bots that patrol and shoot back — finally something to damage | nothing (M3+M4 done) |
| D | M16 + M17 | M2c | Industrial zone + military base (Track A; warehouse/factory/barracks already in the kit) | nothing |
| E | M8 | M2e | Hills: noise heightmap + carved river channel + heightfield collision | nothing |

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
| M2 | — (debug package) | Editor view (grid + XYZ axes + free camera) | ✅ done |
| M3 | M3a, M3b, M3c | First-person camera + movement (+ touch controls, + building collision) | ✅ done |
| M4 | M5a, M5b | Shooting + hitscan raycast + bullet holes + crosshair/ammo HUD | ✅ done |
| M5 | M5c | Screen shake + muzzle flash + hit marker + recoil | ✅ done |
| M6 | — (folded into M3c) | Head bob + footsteps + sprint FOV | next (option A) |
| M7 | M3a | Touch controls **polish** (dead zone, sensitivity setting, haptics) — basic touch controls already shipped inside M3 | not started |
| M8 | M2e | Hills terrain + noise heightmap + lighting | not started |
| M9 | **M2b** | 5+ buildings with interiors → town + village kit | ✅ done |
| **M2b.5** | — (new, user request) | House **asset** integration: real models replace the procedural kit, one building first then all 32 | planned, waiting for the asset drop |
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

## First-Person Controls (master M3, repo M3a–M3c)

`GameScreen` is the playable screen; `debug/EditorScreen` keeps the old orbit/free-fly
viewer behind the main menu's **EDITOR** button (debug builds only).

| Control | Desktop | Touch |
|---|---|---|
| Move | `W A S D` or arrows | virtual stick on the **left half** (appears where the thumb lands) |
| Look / aim | hold **right mouse button** + drag | drag anywhere on the **right half** |
| Sprint | hold `Shift` (7 m/s) | push the stick to ≥ 92% deflection |
| Crouch | `Ctrl` or `C` (2 m/s, eye 1.0 m) | **CRCH** button (bottom-right) |
| Jump | `Space` (1.2 m ⇒ 4.85 m/s) | **JUMP** button (bottom-right) |
| Leave the match | `Esc` / `Back` | `Back` |

| Class | Package | Job |
|---|---|---|
| `Player` | `player` | feet position, vertical velocity, stance (`STAND`/`CROUCH`), `PlayerStats` |
| `MovementController` | `player` | wish direction from yaw + input, speeds, jump, gravity, step-up, wall push-out, map bounds |
| `FirstPersonCamera` | `player` | yaw/pitch look + stance eye height → shared `PerspectiveCamera` |
| `BuildingCollider` | `world` | 173 oriented boxes baked once from the layout; circle-vs-OBB resolve + `groundHeightAt` |
| `InputState` | `input` | one mutable frame snapshot (never allocated per frame) |
| `InputManager` | `input` | picks the scheme (`MultitouchScreen` ⇒ touch) and forwards |
| `DesktopInputHandler` | `input` | keyboard + right-button mouse drag |
| `TouchInputHandler` | `input` | routes 4 pointer slots to stick / look / JUMP / CRCH, draws the widgets |
| `VirtualJoystick`, `TouchLookArea` | `ui` | thumb stick and drag-to-aim accumulator |

Rules worth keeping:

- **Pointer routing is claim-on-press**: a pointer keeps the widget it first touched
  until it lifts, so a thumb on the stick never also aims, and the two buttons win over
  the half-screen areas.
- **Collision boxes mirror `BuildingFactory` exactly** (same local space, same wall
  thickness, front wall split into two jambs around `doorWidth()`), so a doorway that
  is visually open is also walkable and interiors stay enterable. Lintels and roofs are
  deliberately not colliders — the player cannot climb onto a roof.
- **Plinths are walkable ground**, not walls: `groundHeightAt` returns the temple
  plinth (0.5 m) and its front steps (0.25 m), and `STEP_UP_HEIGHT` (0.6 m) lets the
  player walk up without jumping.
- **`MAX_STEP_DELTA` (0.05 s) clamps the frame step** so a stall after a load cannot
  tunnel the player through a wall.
- Yaw 0 looks along +X and yaw 90 along +Z — the same convention as `EditorCamera`, so
  the corner orientation gizmo and any future minimap keep working unchanged.
- All sizes are fractions of `min(screenWidth, screenHeight)` (R46): stick radius 0.11,
  buttons 0.13, margin 0.03, idle stick at (0.17, 0.74).
- The HUD draws `HP | AR | STAND/CROUCH/AIR` top-left, `FPS | DC | Tri` top-right, and
  (debug builds) `X Z Y | BOX n` plus a one-line control hint. Strings are rebuilt only
  when a displayed value changes, so the render loop allocates nothing (R6).

## Shooting (master M4, repo M5a–M5b)

One hitscan ray per shot, cast from the camera through the crosshair, tested against
the same boxes the player collides with.

| Class | Package | Job |
|---|---|---|
| `WeaponType` | `weapons` | the five SPEC weapons: damage, fire interval, magazine, range, reload time, recoil, projectile flag (unchanged since M1) |
| `Weapon` | `weapons` | one gun instance: magazine, reserve ammo, fire cooldown, reload timer (+ `getReloadProgress()` for the HUD) |
| `WeaponController` | `weapons` | ticks the gun, casts the shot ray, spawns the decal, counts shots |
| `RayHit` | `world` | reused ray result: distance, point, unit normal, surface kind |
| `BuildingCollider.raycast` | `world` | slab test per wall box over its real height range + plinth tops + flat ground |
| `ColliderBoxes` | `world` | bakes the boxes (8 floats each) from the layout — split out of `BuildingCollider` to respect the 300-line limit |
| `ImpactDecals` | `world` | 64 bullet holes in one dynamic mesh: ring buffer, 1 draw call, 16 floats re-uploaded per shot |
| `Crosshair`, `MatchHud`, `HudData` | `ui` | crosshair, all HUD text, and the per-frame value snapshot the HUD reads |

| Control | Desktop | Touch |
|---|---|---|
| Fire | hold **left mouse button** | hold **FIRE** (large, bottom-right) |
| Reload | `R` | automatic when the magazine empties |

Rules worth keeping:

- **The ray stops at the weapon's range** and only reports the *nearest* hit
  (`RayHit.accept` keeps the minimum), so walls, plinth tops and ground can be tested
  in any order.
- **Walls are tested over their real height range** (`baseY..topY`), so a shot can pass
  over a hut and a decal never floats above a wall it did not hit. Player collision, in
  contrast, is deliberately height-blind.
- **Ground hits are limited to the map bounds** (`±size/2`); beyond the island a shot
  simply flies. When master M8 adds hills, `castGround` is the one place to replace the
  flat `y = 0` plane with heightfield sampling.
- **Decals are a ring buffer of 64** (`Constants.DECAL_POOL_SIZE`): the oldest hole is
  overwritten, all of them draw in one call with the world's already-bound shader
  (`Arena.worldShader()` — never create a second program, `Mesh.render` does not bind).
- **Decal orientation** uses `u = normalize(cross(normal, helper))`, `v = cross(normal, u)`
  with `helper = +Y` unless the normal is near-vertical, then `+X`; corners are written
  CCW seen from the shooter, so `cross(u, v) == normal`.
- **`Math3D.direction(yaw, pitch, out)` is now the only look-direction formula** in the
  project (it used to disagree with the cameras). Bots, turrets and grenade throws must
  call it too, or aim will not match what the player sees.
- Damage is *not* applied yet: there is nothing to shoot. Master M10 (bots) inserts a
  bot test before the world test in `WeaponController.castShot`, and master M5 adds the
  hit marker, muzzle flash, recoil and screen shake.

## Game Feel (master M5, repo M5c)

Recoil, camera shake, muzzle flash, hit marker and a placeholder gunshot, all driven by
**one shot event**: `WeaponController.firedThisFrame()` is true on exactly the frame a
round left the barrel, and `GameScreen.onShot()` fans it out.

| Class | Package | Job |
|---|---|---|
| `WeaponFeel` | `weapons` | owns the two camera effects and turns one shot into recoil + shake |
| `RecoilState` | `weapons` | the transient aim offset in degrees: climb while spraying, full recovery after |
| `ScreenShake` | `player` | additive, exponentially decaying noise offset + camera roll |
| `MuzzleFlash` | `ui` | screen-space glow / core / four-point star, 70 ms, generated radial texture |
| `HitMarker` | `ui` | four diagonal ticks over the crosshair, 160 ms, they slide outward as they fade |
| `Crosshair` | `ui` | now also blooms: the gap between the ticks widens up to 2.5× with the recoil |
| `ShotBeep` | `audio` | placeholder crack, synthesised once and streamed on a daemon thread |

### Effect order (critical — do not reorder)

```
input.update → view.look → movement.update(base yaw) → feel.update (recoil, shake)
            → view.apply(recoil + shake + roll) → weapons.update → onShot (flash, marker, beep)
```

Movement must read the **base** yaw, before any offset exists, or a shot would shove the
player sideways; the offsets are applied to the camera afterwards, so they can only ever
move the view. `GameScreen.updateGameplay()` is the single place that encodes this order.

### Recoil = full recovery (user decision 2026-09-13)

Free Fire style, **not** the CS-style permanent transfer:

- The offset lives in `RecoilState` and is added to the look direction inside
  `FirstPersonCamera.apply(yawOffset, pitchOffset, roll)`. It is **never** written back
  into `yaw`/`pitch`, so there is no partial transfer to tune, and recovery is exact —
  after a spray the crosshair sits on precisely the point the player was aiming at.
- While the trigger is held the offset only bleeds at `RECOIL_HOLD_DECAY_PER_SECOND`
  (3.0/s) and every shot re-arms the recovery clock, so a spray **climbs**.
- After the last shot the offset decays exponentially over
  `recoil° × 0.055 s + 0.08 s`, clamped to 0.08–0.40 s: pistol snaps back in ≈0.17 s,
  sniper takes ≈0.40 s.
- Shot 4+ of a burst kicks 1.25× harder (`RECOIL_RAMP_*`); the climb saturates at
  `RECOIL_PITCH_MAX` 14° and the horizontal sway at `RECOIL_YAW_MAX` 4°, so an endless
  spray can never leave the player looking at the sky.
- Horizontal sway is a deterministic left-right pattern that widens down the burst plus
  ±0.08° of jitter — a readable spray shape rather than pure noise.
- Because the ray is cast from the camera, the bullet goes where the crosshair has
  climbed to: the visual climb **is** the accuracy loss. Explicit bullet spread is a
  later milestone (master M22, five weapons).

Simulated off device (`python3 /home/user/sim_m5.py`, 1:1 port of the two classes) at
60/30/20 Hz: SMG 30-round spray peaks at 5.5° and is back to exactly 0.0000° 0.18 s
after release; rifle plateaus at 5.0°; pistol (slow fire rate) never exceeds 2.4°.
Frame-rate independence comes from the exponential decay, not from per-frame constants.

### Screen shake

`shake = recoil° × 0.20` per shot, capped at 3.0°, decaying 14×/s, rendered as a
fixed-frequency (1400 °/s) noise offset plus 0.6× that amplitude of camera **roll**
(roll tilts `camera.up` around the look direction, so it never contaminates the aim).
Impulses add and re-seed, so overlapping shots never cancel out. Master M6 (head bob,
footsteps) will add its own low-amplitude offsets through the same `apply()` call.

### Muzzle flash — screen space today, world space at M20

The flash is a HUD-batch quad pair (glow + squashed star + white core) anchored at
`MUZZLE_FLASH_X/Y` = (0.50, 0.38) of the screen, because the game has **no arms model
yet**. **Recorded decision: at master M20 (real asset swap, `arms_<weapon>.g3dj`) the
flash moves to the weapon's world-space muzzle attach point** and becomes a world quad
(or a small emissive mesh) parented to that bone; this class then survives only as the
fallback for missing arms. The radial texture is generated at startup
(`Assets.glow()`, 64×64, quadratic alpha falloff), so no `muzzle_flash.png` ships yet.

### Placeholder gunshot (master M13 replaces it)

`audio/ShotBeep` synthesises 2048 samples (93 ms) of mono 16-bit PCM once — a damped
760 Hz sine plus an octave — and streams it through `Gdx.audio.newAudioDevice` on a
single low-priority **daemon** thread, because `AudioDevice.writeSamples` blocks for
real time and must never stall the render loop (R6/R31). Requests coalesce under one
monitor, so a 12.5 shot/s spray cannot queue up. If no audio device can be created the
class disables itself and logs once (R25 silent-stub rule). `Constants.SOUND_ENABLED`
is the master switch until `SoundManager` exists; `WeaponType.fireSoundPath()` already
builds the real OGG paths for master M13.

## House Asset Integration — M2b.5 (planned, not started)

User decision 2026-09-13: the procedural building kit from M9 gets replaced by real
downloaded house models. This is a **Track A insert** between M9 and M16, and it does
not start until the assets are on disk. Plan of record:

**Input needed from the user** (three questions, still open): the file **format**
(`.glb`/`.gltf` load directly through the already-pinned `gdx-gltf` 2.2.1; `.fbx` needs
`fbx-conv`; `.obj` is static only; `.blend` must be exported first), **how many**
buildings, and **where they came from** (licence: CC0 preferred — Kenney City Kit,
Quaternius, KayKit). Note that R24 says `.g3dj`/`.g3db`; loading `.glb` at runtime is a
deliberate deviation the user asked for, and `fbx-conv` stays the route if a conversion
pass is ever wanted.

**Part 1 of the asset drop is already in the repo** (2026-09-13, assets only, no code
reads it yet): `assets/buildings/` holds four **CC0** houses from Kenney's *City Kit
Suburban (2.0)* renamed to `BuildingType` ids — `house_small`, `house_medium`,
`house_two_storey`, `shop` — 536 KB total, one folder each with a measured `info.txt`,
plus `assets/buildings/README.md` carrying the licence text and all the numbers below.
`kenney.nl` is unreachable from the AI sandbox; they came through `api.github.com` from a
mirror of the pack. Measured before download, and all three are exactly the risks this
plan was written around:

| Risk | Measured |
|---|---|
| Texture embedding | **Not embedded**: `images[0].uri = "Textures/colormap.png"`, so the PNG must stay in that subfolder. It is **one 512×512 atlas shared by the whole kit** (the four copies are byte-identical), inside the R12 cap, each building using a small UV sub-rectangle ⇒ one texture can serve all 32 buildings, which is what makes one material / one batch possible |
| Scale | **Not 1 unit = 1 m** — a house is 1.3 units wide. Cross-checked against the same author's Roads kit (street light 0.67 units ≈ 5 m, traffic cone 0.08 ≈ 0.7 m) ⇒ **1 unit ≈ 0.14 m**: ≈7.2× to reach human scale, then a per-type factor to hit the `BuildingType` footprints (`house_small` 0.64 → 6.0 × 3.4 × 4.2 m, `house_medium` 0.64 → 6.0 × 3.8 × 4.8 m, `house_two_storey` 0.55 → 7.0 × 4.9 × 4.1 m, `shop` 0.59 → 5.6 × 4.9 × 6.0 m). Starting numbers — the 6 × 4 × 6 m debug box decides |
| Orientation | **Unverified**: nodes carry no rotation and our factory/collider treat local +Z as the front. Must be eyeballed in Phase 1 |

Also measured: 770–2062 triangles and 988–3010 vertices per house (short indices safe),
one mesh / one primitive / one material, `POSITION NORMAL TANGENT TEXCOORD_0`, no
animation, no skinning, generator "UnityGLTF", `KHR_texture_transform`. The full kit is
41 GLBs (2.6 MB); the mirror also carries Kenney's **Commercial**, **Industrial** and
**Roads** packs, which is the obvious source for master M16/M17.
Budget warning: 32 buildings × ≈1 400 tris ≈ **45k triangles**, more than half of the
80k budget (R29) on top of today's 6.3k ⇒ Phase 2 must use the kit's `low-detail-*`
variants (8–26 KB, ≈200 tris) for distant buildings or bring LOD (master M19) forward.
`house_medium` is also **not** an existing `BuildingType` id: Phase 1 either maps that GLB
onto `house_small` or adds a 9th type (one enum row + layout data, R7/R23) — the user
decides.

**Phase 1 — one building, verified before anything else.** Import a single
`house_medium`, place it at one existing layout coordinate, and check four things side
by side with a **6 × 4 × 6 m debug reference box** drawn next to it (1 unit = 1 m
assumed; a 60 m giant gets 0.1 scale, a 0.6 m dwarf gets 10×). Also verify texture
embedding (a separate PNG must sit in the same folder with the same base name), the
front-face orientation (local +Z must stay the front, or every doorway faces the wrong
way) and the collision box. Deliverable: an editor-view screenshot.

**Phase 2 — the other 31**, only after Phase 1 is signed off. Re-use the existing
`buildings[]` JSON coordinates from `assets/maps/island.layout` unchanged, keep the
`BuildingType` footprint/height data as the collision source, and batch or instance so
the draw-call budget survives (32 individual models = 32+ draw calls unless they are
instanced or merged — that is the main performance risk of this milestone).

**Collision rule for now:** solid AABB/OBB from the existing `BuildingCollider` boxes.
Walkable interiors against real models only arrive with master M3c-style refinement
after M8 (heightfields); M2b.5 must not change the collider's box generation, or the
player will fall through floors that are still procedural.

## Editor View (master M2)

A debug camera for inspecting the map, in `com.brfps.debug` (R44) and gated by
`Constants.DEBUG_TOOLS_ENABLED` (R43). Toggle it with **F1** on desktop or by tapping
the **CAM** button top-left on a phone.

| Control | Desktop | Touch |
|---|---|---|
| Switch camera | `F1` | tap **CAM** (top-left) |
| Move | `W A S D` or arrows | drag anywhere on the **left half** (drag = direction + speed) |
| Altitude | `E` up, `Q` down | **UP** / **DN** buttons (bottom-right) |
| Look | hold **right mouse button** + drag | drag anywhere on the **right half** |
| Fast | hold `Shift` (45 m/s vs 12 m/s) | full drag deflection |
| Leave the world | `Esc` / `Back` | `Back` |

What is drawn in editor mode: a 10 m minor / 50 m major grid over the whole map with a
bright center cross, an XYZ axis gizmo at the world origin (X red, Y green, Z blue,
12 m long), a corner orientation gizmo bottom-left showing the camera's current axes,
and a HUD line with `X Y Z yaw pitch` plus the control help.

Notes for whoever builds the real HUD: yaw 0 looks along +X and yaw 90 along +Z
(`EditorCamera.direction`), pitch is clamped by `Constants.PITCH_MIN/PITCH_MAX`, the
far plane widens to 900 m in editor mode and returns to 300 m in orbit mode, and every
on-screen rectangle is a fraction of `min(screenWidth, screenHeight)` (R46).

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
