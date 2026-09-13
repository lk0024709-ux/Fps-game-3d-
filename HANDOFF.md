# Handoff Log

Newest entry at the **bottom**. Keep the last 10 entries here; move older ones to
`HANDOFF_ARCHIVE.md` (create that file the first time an entry is evicted).

### Numbering convention (user decision, 2026-09-13 — do not deviate)

- Entry title format: `## M<master> (master) = M<repo> (repo) — <description>`.
  When a milestone has no repo letter (it is new in the master sequence), write
  `## M<master> (master) — <description>` and say so in the Sequence line.
- Every entry carries a `Sequence:` line in the form
  **`M3 (master) = after M2a (repo)`** — i.e. the master id being worked on and the
  milestone that precedes it. This is what stops a new model from re-doing or
  skipping work when the two schemes disagree.
- Historical entries (M0, M1, M2a) keep their repo ids in the title; they are never
  renumbered. New entries use master ids (M9, M10, M3, …).
- Translation source: `SPEC.md → Milestone Mapping Table`. Track (A = world/content,
  B = feel/combat/systems) is stated in every new entry, because R1 applies inside a
  track, not across tracks.

Template (also in `RULES.md` → Handoff protocol):

```
## M<number> — <description>
Date: <YYYY-MM-DD>
Model: <DeepSeek | GPT | Claude | Gemini | Grok | not recorded>
Status: ✅ Complete | ⚠️ Partial | ❌ Reverted
Files added:
· <path>
Files modified:
· <path>
What works:
· <1-2 lines>
What's pending:
· <1-2 lines>
Known issues:
· <1-2 lines>
Next milestone: M<number+1> — <description>
Suggested model: <name>
Notes for next model:
· <1-2 lines>
CI: <run id / green-red>
```

> Entries M1 and M2a were **reconstructed from git history, CI runs and SPEC.md**
> during M0 (the log did not exist before then). Details are accurate for files and
> status; the model name was never recorded, and Arena agent sessions do not
> disclose their underlying model.

---

## M2a — Island terrain, zone markers, roads, JSON map layout

Date: 2026-09-12
Model: not recorded (Arena session `01a09671`)
Status: ✅ Complete

Files added:
· `assets/maps/index.json`, `assets/maps/island.layout`
· `core/.../world/Arena.java` (283 lines), `FlatShader.java`, `MeshKit.java`, `MapLayout.java` (261 lines), `MapRegistry.java`, `SpawnPoint.java`, `SafeZone.java`
· `core/.../util/ScreenshotUtil.java`

Files modified:
· `core/.../screens/GameScreen.java` — became a world viewer with an orbiting debug camera + `FPS | DC | Tri` HUD + one-shot screenshot after 5 s
· `core/.../util/Constants.java` — map, debug-visual and orbit-camera constants
· `SPEC.md` — milestone table, multi-map system section, layout JSON schema, missing-assets update

What works:
· 300×300 m island rendered procedurally: 40×40 terrain segments with per-vertex ground colors (grass / dry_grass / dirt / concrete / asphalt), beach ring, surrounding water plane, translucent river strip, 4 roads, 5 zone marker posts, 10 spawn markers.
· Maps are pure data: `MapRegistry` reads `index.json`, `MapLayout` parses a `.layout` file, unknown JSON fields are ignored → a new map needs no code change (R23/R42).
· Baseline ≈3.2k triangles, 7 draw calls; render path allocates nothing (R6/R30).

What's pending:
· `buildings[]` and `props[]` are empty arrays; river is a flat translucent strip (no carved channel); terrain is flat (no heightfield); no chunking/LOD/culling.

Known issues:
· Three separate debug booleans in `Constants` (`DEBUG_SHOW_ZONE_MARKERS`, `DEBUG_SHOW_SPAWN_MARKERS`, `DEBUG_SHOW_CHUNK_GRID`) — R43 wants one; consolidate when the real HUD/quality tiers land.
· `Arena.java` is at 283/300 lines (R13) — the next world feature must go into a new class, not inline.

Next milestone: **M9 (master) = M2b (repo)** — town + village buildings. The user parked master M2 (editor view) on 2026-09-13 and chose buildings first.
Suggested model: GPT (architecture) or DeepSeek (geometry)
Notes for next model:
· Batch all buildings into ONE opaque mesh via `MeshKit` (draw-call budget R28) and keep new geometry code out of `Arena.java` (R13).
· No JDK/Android SDK inside the AI sandbox → verify every build through CI only (R18).
· On-device check: `adb shell run-as com.brfps cat debug/screenshot.png > shot.png`
CI: run `34707186894` (main, merge of PR #2) — ✅ green

---

## M0 — Add project documentation files (RULES.md, HANDOFF.md, SPEC sections)

Date: 2026-09-13
Model: Claude (Arena session `01a098fd`)
Status: ✅ Complete

Files added:
· `RULES.md` — all 50 rules (R1–R50), handoff protocol, workflow, model-strength table
· `HANDOFF.md` — this log, with M1 and M2a back-filled from git/CI

Files modified:
· `SPEC.md` — added: milestone numbering cross-reference (master M1–M47 ↔ repo ids), new repo rows (M2f, M2g, M2h, M3d, M6d, M6e, M11b), `## Known Issues`, `## Performance Budget`
· `README.md` — Documentation section now links `SPEC.md`, `RULES.md`, `HANDOFF.md`

What works:
· All three mandatory docs exist, so the Section 2 read order (SPEC → RULES → HANDOFF → README → git log → CI) is now possible for any model.
· Milestone numbering conflict between the master prompt and the repo is resolved with an explicit cross-reference table instead of a risky renumber.

What's pending:
· Nothing in M0 itself — this was a docs-only milestone (no Java, no build-affecting change).

Known issues:
· Repo has no `ai/`, `debug/`, `ui/`, `audio/`, `input/`, `graphics/`, `progression/`, `save/` packages yet; `enemies/BotDifficulty.java` will move to `ai/BotProfile.java` at M7c (R35/R36).
· Several classes referenced by SPEC (SoundManager, LootSpawner, ChunkManager, BotController) do not exist yet — tracked in SPEC → Known Issues.

Next milestone: **M9 (master) = M2b (repo)** — town + village buildings
Suggested model: GPT (architecture) or DeepSeek (geometry)
Notes for next model:
· Numbering rule added to `RULES.md`: old commits keep repo ids (`M1`, `M2a`), new commits use master ids (`M9`, `M10`, …), docs always show both.
· Master M2 (editor view: grid + XYZ gizmo + free-fly camera) is **parked** by user decision — build it only when the user asks.
· Two tracks run in parallel (world/content vs. feel/combat); R1 applies inside a track.
CI: run `34738539768` — ✅ green (docs-only commit, no code path touched).

---

## M9 (master) = M2b (repo) — Town + village buildings

Date: 2026-09-13
Model: Claude (Arena session `01a098fd`)
Status: ✅ Complete
Track: A (world & content)
Sequence: **M9 (master) = M2b (repo) = after M2a (repo)** — next in the master
  sequence is M10, but Track B (M2–M8) has not started; see *Next milestone* below.

Files added:
· `core/.../world/BuildingType.java` (111 lines) — the fixed 8-building kit: footprint, wall height, roof style, plinth, interior flag, palette
· `core/.../world/BuildingFactory.java` (279 lines) — local-space geometry emitter (plinth, 4 walls, doorway + lintel, window bands, pitched/flat roof, interior, awning / temple tower / chimney)
· `core/.../world/BuildingBatcher.java` (92 lines) — bakes all buildings into the fewest static meshes, renders with the caller's bound program, counts DC/tris

Files modified:
· `assets/maps/island.layout` — `buildings[]` filled with 32 entries (20 town + 12 village), `version` 1 → 2
· `core/.../world/Arena.java` (289 lines) — owns a `BuildingBatcher`, draws it in the opaque pass before water, adds its counters to the HUD numbers, disposes it
· `core/.../util/Constants.java` — wall thickness, door, window, sill, roof pitch/overhang/slab, floor offset, floating-face threshold, vertex flush limit (R7)
· `SPEC.md`, `RULES.md`, `HANDOFF.md`, `README.md` — numbering rule, mapping table, known issues, performance budget, doc links

What works:
· Island now has a town (10 small houses + 6 two-storey houses + 4 shops on a road grid, one of them riverside) and a village (8 huts + 1 temple on a plinth with a stepped tower + 3 houses) — 32 buildings, all procedural: zero assets, zero textures.
· Large types are walkable shells (open doorway, floor slab, partition, mid floor on 5 m+ walls); small houses and huts are closed shells with a door panel — user decision 2026-09-13.
· Cost: **32 buildings = 6 188 baked vertices / 3 094 triangles in 1 mesh = +1 draw call** (world total 8 DC, ~6.3k tris — budgets are 80 / 80 000).
· Placement validated offline: no building-building overlap, no clash with road strips, the river band or spawn points, all footprints inside their zone (min gap 0.25 m to the river, 0.81 m to a road).
· Unknown `buildings[].type` in a layout is logged and skipped, so a bad map can never crash the game (R25).

What's pending:
· No collision: the camera still flies through walls (master M3 / repo M3c). No per-building culling or LOD (master M19 / repo M2e).
· Industrial + military buildings (`warehouse`, `factory`, `barracks` are already in the kit but unplaced) — master M16/M17 (repo M2c).
· Props, village well, crop fields, bridge — master M18 (repo M2d).

Known issues:
· `house_small` at (100, 6) is 0.25 m from the river band; move it or shape the bank when master M8/M19 carves the channel to −1.5 m.
· Shop awnings overhang 1.5 m and temple steps 1.8 m in front of the wall — keep that margin when placing new buildings.
· Interiors are unlit (flat vertex colors, one light direction baked as per-face shading); real lighting is master M21.

Next milestone: user's choice (R4 — wait for "go"). Candidates:
· **A (recommended)** master M3 (repo M3a–M3c) — first-person camera + movement + touch controls → the game becomes playable. Suggested model: DeepSeek.
· B master M16 + M17 (repo M2c) — industrial zone + military base. Suggested model: DeepSeek/Gemini.
· C master M18 (repo M2d) — props, bridge, village well, crop fields. Suggested model: Gemini.
· (master M2 — editor view — was done next; see the entry below.)
Notes for next model:
· Do not inline world geometry into `Arena.java` (289/300 lines, R13) — follow the `BuildingFactory` + `BuildingBatcher` split.
· `MeshKit` uses short indices: never let one mesh pass 32 767 vertices; `BuildingBatcher` flushes at `Constants.BUILDING_VERTEX_LIMIT` (24 000).
· `FlatShader` is package-private in `com.brfps.world`; new world renderers must live in that package or take an already-bound `ShaderProgram` (do **not** create a second program — `Mesh.render` does not bind).
· No JDK/SDK in the sandbox: compile-check by CI only (R18), and keep to libGDX 1.12.1 APIs you can verify (R10).
CI: run `34738539768` — ✅ green, `assembleDebug` 57 s, artifact `brfps-debug-apk` uploaded

---

## M2 (master) — Editor view: world grid + XYZ gizmo + free-fly camera

Date: 2026-09-13
Model: Claude (Arena session `01a098fd`)
Status: ✅ Complete
Track: tooling (master Phase 0). No repo letter existed for this one, so the commit
  uses the master id only.
Sequence: **M2 (master) = after M9 (master) = M2b (repo)** — the user asked for the
  editor view before the Track B gameplay milestones (M3+).

Files added (all in `com.brfps.debug`, R44):
· `debug/DebugShader.java` (56 L) — position + packed-color line shader, bindable to a camera or an arbitrary Matrix4
· `debug/WorldGrid.java` (63 L) — 10 m minor / 50 m major lines + bright center cross, baked once as GL_LINES
· `debug/AxisGizmo.java` (45 L) — origin axes, 12 m, X red / Y green / Z blue
· `debug/CornerAxisIndicator.java` (75 L) — orientation gizmo drawn into a small corner glViewport using only the camera's rotation
· `debug/EditorCamera.java` (292 L) — free-fly controller: keys + mouse on desktop, left-half drag joystick / right-half look / UP-DN buttons on touch
· `debug/DebugOverlay.java` (88 L) — owns the shader + the three visuals, the CAM toggle hit test, and the draw-call counter

Files modified:
· `screens/GameScreen.java` (292 L) — toggles orbit ↔ editor (F1 / CAM button), delegates the camera, renders the overlay after the world, HUD shows `X Y Z yaw pitch` + context help + UP/DN/CAM buttons
· `util/Constants.java` — `DEBUG_TOOLS_ENABLED` master switch (R43) + 18 editor constants (speeds, sensitivity, grid steps, gizmo sizes — all screen sizes are fractions of min(w,h), R46)
· `world/Arena.java` (292 L) — the three debug-marker flags are now AND-ed with `DEBUG_TOOLS_ENABLED`, so one boolean really does turn all debug visuals off
· `SPEC.md` — new *Editor View (master M2)* section with the control table, M2 marked ✅, known issues 13–14, performance budget note
· `README.md` — debug/editor controls documented

What works:
· Fly anywhere over the island: WASD/arrows + Q/E + Shift (12 / 45 m/s), right-mouse-drag to look, F1 to flip between the orbit viewer and the editor.
· Touch-first (R45): drag the left half to fly (drag vector = direction + speed, 70 px = full deflection), drag the right half to look, UP/DN buttons for altitude, CAM button top-left to switch modes. Multi-pointer safe (4 pointers tracked, each classified once on press).
· Grid, origin axes and a corner orientation gizmo show where you are; the HUD prints integer `X Y Z yaw pitch` and only rebuilds its strings when a rounded value changes, so the render path stays allocation-free (R6, R30).
· Cost: +3 draw calls in editor mode only (8 → 11); all line geometry baked once (grid 62 lines, gizmos 3 lines each).

What's pending:
· The editor cannot place buildings — layouts are still hand-edited JSON. A pick/place tool would be a separate milestone (not in the master list).
· No first-person player yet: master M3 (repo M3a–M3c) is the next Track B step.

Known issues:
· CAM button (top-left) and UP/DN (bottom-right) occupy spots the minimap (master M35) and fire button (master M5a) will want; they vanish with `DEBUG_TOOLS_ENABLED`.
· The corner gizmo sets `glViewport` and restores it to the full screen; anything drawn after it must call `viewport.apply()` (GameScreen's HUD does).
· Editor mode widens the far plane to 900 m and restores 300 m on exit — keep that pairing if you add another camera mode.

Next milestone: user's choice (R4 — wait for "go"). Candidates:
· **A (recommended)** master M3 (repo M3a–M3c) — first-person camera + movement + touch controls → playable. Suggested model: DeepSeek.
· B master M4 + M5 (repo M5a–M5c) — shooting, hitscan, hit feedback. Needs M3 first. Suggested model: DeepSeek.
· C master M16 + M17 (repo M2c) — industrial zone + military base. Suggested model: DeepSeek/Gemini.
· D master M18 (repo M2d) — props, bridge, village well, crop fields. Suggested model: Gemini.
Notes for next model:
· Debug code stays in `com.brfps.debug`; `DebugShader` is package-private there, exactly like `FlatShader` in `com.brfps.world` — do not try to share them across packages.
· Never create a second shader program for meshes drawn inside an existing pass: `Mesh.render` does not bind, so bind once and pass the program (see `BuildingBatcher.render(ShaderProgram)`).
· `MeshKit` lives in `com.brfps.world` and is public — reuse it for any new baked line/triangle geometry instead of writing new mesh code.
· No JDK/SDK in the sandbox: CI is the only compiler (R18).
· **libGDX API lesson (this milestone broke CI once):** `Vector3` has **no**
  `addScaled()` — the scaled add is `add(Vector3 v, float scalar)` / `mulAdd(...)`.
  Verify a method exists in the 1.12.1 sources before using it (R10); prefer
  `set().scl()` + `add()` on a reused scratch vector when in doubt.
· The Arena sandbox can be re-cloned between turns: local commits may vanish while
  the working tree survives. Before committing, run `git fetch` + compare
  `git log --oneline HEAD` with `git ls-remote origin`, and if the branch was reset,
  `git reset --mixed <remote tip>` (keeps files) instead of force-pushing.
CI: runs `34743333496` (push) + `34743335371` (PR #3) — ✅ green after one fix
  commit (`34743245545`/`34743247848` were ❌ red: `cannot find symbol addScaled`).

---

## M3 (master) = M3a, M3b, M3c (repo) — First-person camera + movement + touch controls + building collision

Date: 2026-09-13
Model: Claude (Arena session `01a098fd`)
Status: ✅ Complete — CI green on the first push of this milestone
Track: **B** (feel / combat / systems).
Sequence: **M3 (master) = after M2 (master) = M3a–M3c (repo)** — the three repo rows
  M3a (touch controls), M3b (first-person camera) and M3c (movement + collision) are
  one milestone in the master list, so they ship as one commit pair.

Files added:
· `player/Player.java` (71 L) — feet position, vertical velocity, stance enum, reused `PlayerStats`
· `player/MovementController.java` (109 L) — wish direction from yaw + input, walk/sprint/crouch, jump, gravity, step-up, wall push-out, map bounds
· `player/FirstPersonCamera.java` (76 L) — yaw/pitch look, stance eye height, `direction = (cosYaw·cosPitch, sinPitch, sinYaw·cosPitch)`
· `world/BuildingCollider.java` (182 L) — 173 oriented boxes baked once from the layout into two flat `float[]`; circle-vs-OBB push-out + `groundHeightAt()`
· `input/InputState.java` (40 L) — one mutable per-frame snapshot (R6)
· `input/InputManager.java` (58 L) — `MultitouchScreen` ⇒ touch, else desktop; also owns `lookSensitivity()` and the HUD help line
· `input/DesktopInputHandler.java` (44 L) — WASD/arrows, RMB-drag look, Space/Shift/Ctrl-C
· `input/TouchInputHandler.java` (151 L) — 4 pointer slots routed claim-on-press to stick / look area / JUMP / CRCH, and draws the widgets
· `ui/VirtualJoystick.java` (105 L) — thumb stick that appears where the thumb lands, clamps to radius, sprint at ≥ 92% deflection
· `ui/TouchLookArea.java` (57 L) — drag accumulator, consumed exactly once per frame
· `debug/EditorScreen.java` (291 L) — the old orbit + free-fly viewer moved out of `GameScreen` unchanged (M2 deliverable preserved)

Files modified:
· `screens/GameScreen.java` (292 L) — rewritten as the playable first-person screen: spawn on the first layout spawn point, look → move → collide → apply camera → render world → HUD
· `screens/MainMenuScreen.java` (175 L) — PLAY and EDITOR buttons (rects laid out in `resize()`, cheap hit tests), ENTER/SPACE and F1 shortcuts, version + player-count line
· `util/Constants.java` (148 L) — 14 M3 constants (JUMP_VELOCITY 4.85, PLAYER_RADIUS 0.35, STEP_UP_HEIGHT 0.6, MAX_STEP_DELTA 0.05, BOUNDS_MARGIN 1, TOUCH_LOOK_SENSITIVITY 0.22, SPRINT_STICK_DEFLECTION 0.92, JOYSTICK_RADIUS/HOME_X/HOME_Y, TOUCH_BUTTON_SIZE, TOUCH_MARGIN, CIRCLE_TEXTURE_SIZE) + 4 menu constants
· `util/Assets.java` (75 L) — `circle()`: a generated 64×64 filled disc (`Pixmap.fillCircle`, blending None) for the stick and buttons, disposed with the rest
· `SPEC.md` — new *First-Person Controls (master M3)* section, known issues 1/2/11/13 rewritten, M3 ✅ in both tables, next candidates re-lettered, M3 cost note in the budget
· `README.md` — *Playing the game* control table; editor view documented as the menu's EDITOR button

What works:
· You spawn in the town plaza (75, 0) and can walk, sprint, crouch and jump around the island in first person; eye height switches 1.6 m ↔ 1.0 m with stance.
· Buildings are solid: walls, the two door jambs and interior partitions stop the player, doorways (1.1 m / 2.2 m) stay walkable, roofs are not climbable.
· Touch-first (R45): the stick appears where the left thumb lands, the right half aims, JUMP/CRCH sit bottom-right; a pointer keeps whatever widget it first touched, so thumbs never fight. Desktop: WASD + RMB-drag (left button stays free for shooting in M4) + Space/Shift/Ctrl.
· Temple plinth (0.5 m) and its steps (0.25 m) are walkable *ground*, not walls: `groundHeightAt()` + `STEP_UP_HEIGHT` 0.6 m step assist.
· Cost: **+0 draw calls, +0 triangles** (world stays at 8 / ≈6 300). Collision is 173 boxes ≈ 4.8 KB of floats built once at load; the touch widgets are 3 quads in the existing HUD batch from one generated 64×64 texture.
· HUD rebuilds its strings only when a rounded value changes (`HP | AR | STAND/CROUCH/AIR`, `FPS | DC | Tri`, and in debug builds `X Z Y | BOX n` + a control hint), so the render loop still allocates nothing (R6, R30).

What's pending:
· No crosshair, no shooting, no damage — master M4 + M5 (repo M5a–M5c) is the natural next step and is now unblocked.
· No head bob, footsteps or sprint FOV — master M6. Basic touch controls shipped here; polish (dead zone, sensitivity setting, haptics) is still master M7.
· Bots need a player to shoot at: master M10 (repo M7a) is now unblocked too.

Known issues:
· **`GameScreen` is at 292/300 lines (R13).** M4 must move the HUD out into its own class (e.g. `ui/MatchHud.java`) before adding weapon code — do not grow this file.
· Collision is height-blind by design (walls are infinite for the player): the player cannot jump onto a roof, but neither can anything else. When M8 adds hills, `groundHeightAt()` must become terrain-aware — it is the single place ground height is decided.
· Water, the river and the map edge are not hazards yet: the player is clamped at ±149 m and can walk on the sea plane. Drowning/swimming is master M31 (landing) / M40 territory.
· Verified by simulation, not by running the game (no JDK in the sandbox): 171 wall + 2 plinth boxes; spawn has 10.5 m clearance to the nearest building; walking into `house_two_storey` (86, 6) stops at 3.85 m from its centre = hd 3 + radius 0.35 and slides along the wall; walking through six doorways ends up inside; plinth probes read 0.5 m on the slab, 0.25 m on the steps, 0 m outside.

Next milestone: user's choice (R4 — wait for "go"). Candidates:
· **A (recommended)** master M4 + M5 (repo M5a–M5c) — crosshair, hitscan raycast, damage, muzzle flash, recoil, screen shake → the game becomes a shooter. Suggested model: DeepSeek.
· B master M16 + M17 (repo M2c) — industrial zone + military base (kit already has warehouse/factory/barracks). Suggested model: DeepSeek/Gemini.
· C master M18 (repo M2d) — props, bridge, village well, crop fields. Suggested model: Gemini.
· D master M8 (repo M2e) — hills heightmap + carved river channel + heightfield collision. Suggested model: Gemini.
Notes for next model:
· **The collider must keep mirroring `BuildingFactory`.** Boxes are generated from the same `BuildingType` dimensions and the same local space (local +Z is the front, rotated by `def.rotation`). If you change a wall, door width or partition in the factory, change it here too, or the player will walk through geometry / get stuck in mid-air walls.
· Yaw convention is shared with `EditorCamera`: yaw 0 = +X, yaw 90 = +Z, `yaw += dX·sensitivity`, `pitch -= dY·sensitivity`. Keep it — the corner orientation gizmo and any future minimap depend on it. Strafe right is `(-sinYaw, 0, cosYaw)` = `cross(direction, up)`.
· Jump velocity is derived, not guessed: `sqrt(2 · 9.8 · JUMP_HEIGHT 1.2) = 4.85 m/s`. If `JUMP_HEIGHT` changes, recompute `JUMP_VELOCITY`.
· `MAX_STEP_DELTA` (0.05 s) clamps the movement step — keep it, it is what stops a post-load stall from tunnelling the player through a wall.
· Adding a screen: `MainMenuScreen` disposes itself before `game.setScreen(...)`; both world screens capture one screenshot 5 s in (`debug/screenshot.png` editor, `debug/firstperson.png` gameplay) and `GameScreen.show()` calls `setCatchBackKey(true)` so BACK returns to the menu on Android.
· The circle texture is generated at runtime (`Assets.circle()`), so no PNG was added and the APK size is unchanged (8.6 MB debug).
· No JDK/SDK in the sandbox: CI is the only compiler (R18). This milestone compiled first try — the M2 lesson stuck: verify libGDX 1.12.1 APIs before use (R10), and prefer `set().scl()` + `add()` on reused scratch vectors over methods you cannot check.
CI: runs `34748242247` (push, code+SPEC+README) + `34748244207` (PR #3) — ✅ green, 62 s,
  artifact `brfps-debug-apk` uploaded; the docs-only follow-up verified again in
  `34748334936` (push) + `34748337590` (PR #3) — ✅ green. PR #3: OPEN, MERGEABLE, 11 commits.

---

## M4 (master) = M5a, M5b (repo) — Shooting: hitscan raycast, bullet holes, crosshair, ammo HUD

Date: 2026-09-13
Model: Claude (Arena session `01a098fd`)
Status: ✅ Complete — CI green on the first push of this milestone
Track: **B** (feel / combat / systems).
Sequence: **M4 (master) = after M3 (master) = M5a–M5b (repo)** — repo M5a (crosshair +
  fire button + ammo counter) and M5b (hitscan + damage) ship together; repo M5c
  (screen shake, muzzle flash, hit marker) is master M5 and is still open.

Files added:
· `weapons/WeaponController.java` (95 L) — ticks the gun, casts one ray per shot, spawns the decal, counts shots; auto-reloads an empty magazine
· `world/RayHit.java` (62 L) — reused ray result (distance, point, unit normal, surface kind); `accept()` keeps the nearest candidate
· `world/ColliderBoxes.java` (137 L) — box baking split out of `BuildingCollider` so both files stay under 300 lines; boxes now carry baseY/topY (8 floats each)
· `world/ImpactDecals.java` (133 L) — 64 bullet holes in ONE dynamic mesh: ring buffer, only the newest quad's 16 floats re-uploaded per shot, 1 draw call
· `ui/Crosshair.java` (37 L) — four ticks + dot, dims while reloading
· `ui/HudData.java` (41 L) — per-frame HUD value snapshot (same pattern as `InputState`)
· `ui/MatchHud.java` (192 L) — every HUD string, rebuilt only when a shown value changes

Files modified:
· `world/BuildingCollider.java` (230 L) — now the query side only: `resolve`, `groundHeightAt`, and the new `raycast` (slab test per wall over its real height range + plinth tops + flat ground inside the map bounds)
· `world/Arena.java` (298 L) — `worldShader()` so decals reuse the already-bound program (never a second one: `Mesh.render` does not bind)
· `world/MeshKit.java` (126 L) — `attributes()` so the decal mesh shares the vertex layout
· `util/Math3D.java` — `direction()` rewritten to the project's yaw convention (0 = +X, 90 = +Z) and now the ONLY look-direction formula; `FirstPersonCamera` and `EditorCamera` both call it
· `util/Constants.java` (164 L) — 12 M4 constants (decal size/offset, fire-button size, ammo HUD scale/position, 5 crosshair values); no `STARTING_RESERVE_AMMO` — the reserve is three magazines, computed in `Weapon`
· `weapons/Weapon.java` (90 L) — only `getReloadProgress()` added; the M1 file is otherwise untouched
· `input/InputState.java` — `fire` + `reload`; `DesktopInputHandler` — LMB fire, `R` reload; `TouchInputHandler` (188 L) — large **FIRE** button plus JUMP/CRCH, all three laid out from one set of helpers so hit tests and drawing cannot drift apart; `InputManager` — help text
· `screens/GameScreen.java` (232 L, was 292) — HUD moved to `ui.MatchHud`, weapon + decals wired in
· `SPEC.md`, `README.md` — Shooting section, weapons table (reload + recoil columns), UI/HUD "now vs finalized-by" table, known issues 16–18, M4 cost note

What works:
· Spawn with a Pistol (12 in the magazine, 36 in reserve), aim through the crosshair, hold LMB / **FIRE** to shoot at the weapon's fire rate, `R` or auto-reload when empty; the ammo counter and the dimmed crosshair show the reload.
· Shots are hitscan against the same boxes the player collides with: walls (over their real height, so a shot can pass over a hut), door jambs, interior partitions, plinth tops and the flat ground inside ±150 m. Nearest hit wins, the rest of the ray is discarded.
· Every impact leaves a 14 cm bullet hole oriented to the surface, pushed 2 cm off it, oldest overwritten after 64. All holes draw in **one** extra draw call (8 → 9), ≤ 128 triangles.
· Verified by simulation (no JDK in the sandbox): wall shot stops at 7.50 m with normal (−1,0,0); straight down = GROUND 1.60 m; 45° down = 2.26 m; doorway centre passes the jambs and hits the back wall at 17.80 m; 1 m off centre hits the jamb at 12.00 m; temple plinth = PLINTH 0.50 m; range 5 m cuts a 7.5 m shot; sky and beyond-the-edge shots hit nothing; decal basis gives `u × v == n` for wall and ground normals.
· `GameScreen` is back to 232/300 lines, so M5 has room for recoil and shake (R13).

What's pending:
· Damage is not applied to anything — there are no bots yet (master M10 / repo M7a). Insert the bot test **before** the world test in `WeaponController.castShot`.
· No recoil, muzzle flash, hit marker or screen shake → master M5 (repo M5c). `WeaponType.recoil()` already holds the degrees-per-shot values (pistol 1.2 … sniper 6.0).
· No gun sounds (master M13; `WeaponType.fireSoundPath()` already builds the paths) and no arms model (master M20; `armsModelPath()`).
· One ray per weapon: shotgun pellets and the sniper's projectile drop are master M22. Ammo boxes, weapon pickup and switching are master M12 (`PlayerInventory` from M1 already handles two slots).

Known issues:
· Decals live on world surfaces only; a bot hit will need its own impact feedback (blood/spark) in master M10.
· The bottom-right corner now holds three buttons (FIRE 0.18, JUMP 0.13, CRCH 0.13 of the short side). On a narrow phone in landscape that row is ~44% of the width — master M5a/M34 should re-lay it out before adding reload/pickup/switch buttons.
· `raycast` walks all 171 wall boxes per shot. Fine at 12.5 shots/s; if bots shoot too (master M10), add a broad-phase (grid or the existing per-box circle reject) before scaling to 49 bots.
· Ground is the flat `y = 0` plane plus plinth tops. When master M8 adds hills, `BuildingCollider.castGround` is the single place to change.

Next milestone: user's choice (R4 — wait for "go"). Candidates:
· **A (recommended)** master M5 (repo M5c) — screen shake + muzzle flash + hit marker + recoil kick using `WeaponType.recoil()`. Suggested model: DeepSeek.
· B master M10 (repo M7a) — 3 bots that patrol and shoot back; finally applies damage. Suggested model: DeepSeek/Gemini.
· C master M16 + M17 (repo M2c) — industrial zone + military base. Suggested model: DeepSeek/Gemini.
· D master M8 (repo M2e) — hills heightmap + carved river + heightfield collision. Suggested model: Gemini.
Notes for next model:
· **Check whether a class already exists before writing it.** The M1 skeleton already shipped `weapons/WeaponType`, `weapons/Weapon`, `player/PlayerInventory`, `items/*`, `enemies/BotDifficulty`, `world/SafeZone`, `util/Math3D` and `util/ObjectPool`. This milestone briefly overwrote `WeaponType`/`Weapon` with new versions — the old single-arg `new Weapon(type)` is what `PlayerInventory` calls, so CI would have failed. They were restored from HEAD before pushing. `ls core/src/main/java/com/brfps/*/` first, then extend instead of replacing.
· `util/Math3D.direction(yaw, pitch, out)` is now the single look-direction formula and uses the project convention (yaw 0 = +X, yaw 90 = +Z). It previously used a different one (yaw 0 = −Z) and nothing called it. Bots and turrets must use it too, or their aim will not match what the player sees.
· The sandbox has no JDK, so `/home/user/check_java.py` (recreate it if the workspace was re-cloned) statically checks: line limits, balanced braces, `com.brfps.*` imports, `Constants.*` fields, static calls into our classes, and **constructor arity** — that last check is what would have caught the `Weapon` overwrite.
· Decals must render with `arena.worldShader()` after `arena.render(camera)`; a second `ShaderProgram` corrupts GL state because `Mesh.render` does not bind.
· `ImpactDecals` uses a dynamic mesh (`new Mesh(false, …)` + `updateVertices`); keep the pool at `Constants.DECAL_POOL_SIZE` and never grow it per shot.
· Verified against libGDX 1.12.1 sources via `gh api repos/libgdx/libgdx/contents/<path>?ref=1.12.1` (raw.githubusercontent is blocked in the sandbox): `Mesh(boolean,int,int,VertexAttributes)`, `setVertices`, `setIndices`, `updateVertices(int,float[],int,int)`, `render(ShaderProgram,int)`, `Pixmap.Blending`/`setBlending`/`fillCircle` all exist as used (R10).
CI: runs `34748918185` (push) + `34748918959` (PR #3) — ✅ green, artifact `brfps-debug-apk` uploaded. PR #3: OPEN, MERGEABLE, 14 commits.

---

## M5 (master) = M5c (repo) — Game feel: full-recovery recoil, camera shake, muzzle flash, hit marker, placeholder gunshot

Date: 2026-09-13
Model: Claude (Arena session `01a09a2f`)
Status: ✅ Complete
Track: **B** (feel / combat / systems).
Sequence: **M5 (master) = after M4 (master) = M5c (repo)** — the user approved the M5
  plan with five changes on 2026-09-13 and said go; all five are in this commit.

Files added:
· `weapons/RecoilState.java` (123 L) — the transient aim offset: climbs while the trigger is held (slow 3.0/s bleed), decays to **exactly zero** after release, saturates at 14° pitch / 4° yaw, ramps 1.25× from shot 4, deterministic left-right sway + ±0.08° jitter
· `weapons/WeaponFeel.java` (54 L) — one shot → recoil kick + proportional shake; also the bloom value the crosshair reads (extracted from `GameScreen` ahead of the limit, as the user asked)
· `player/ScreenShake.java` (77 L) — additive impulses, 14×/s exponential decay, fixed-frequency noise offset + 0.6× roll
· `ui/MuzzleFlash.java` (83 L) — screen-space glow + squashed star + white core, 70 ms, per-shot rotation and scale jitter
· `ui/HitMarker.java` (72 L) — four diagonal ticks, 160 ms, sliding outward as they fade
· `audio/ShotBeep.java` (110 L) — new `com.brfps.audio` package: 2048 samples (93 ms) of mono PCM synthesised once, streamed on one low-priority **daemon** thread because `AudioDevice.writeSamples` blocks for real time
· `HANDOFF_ARCHIVE.md` — created now (R48's "keep the last 10" rule): the M1 entry moved there to make room for this one

Files modified:
· `screens/GameScreen.java` (265 L) — `updateGameplay()` encodes the effect order the user called critical: `input → view.look → movement(base yaw) → feel.update → view.apply(recoil+shake+roll) → weapons.update → onShot()`; `onShot()` fans one shot event out to recoil, shake, flash, marker and beep
· `player/FirstPersonCamera.java` (86 L) — `apply(player, yawOffset, pitchOffset, rollDegrees)`: offsets are added to the look direction for one frame only and **never** written into `yaw`/`pitch`, which is what makes recovery exact; roll rotates `camera.up` around the direction
· `weapons/WeaponController.java` (114 L) — `firedThisFrame()` + `getLastShotRecoil()`: the single shot event every effect hangs off (no trigger polling, so reload and dry fire cannot kick)
· `ui/Crosshair.java` (45 L) — `bloom` parameter widens the tick gap up to 2.5× while the recoil is alive
· `ui/MatchHud.java` (208 L) — draws the flash and the marker in the same batch, passes the bloom, and the debug line grows `| kick +1.2` while the gun is climbing
· `ui/HudData.java` — `crosshairBloom`, `hitMarker`, `muzzleFlash`, `recoilPitch`
· `util/Assets.java` — `glow()`: a generated 64×64 radial gradient (quadratic alpha falloff, POT and inside the 512 cap, R12/R25) — still no image file on disk
· `BrFpsGame.java` — owns and disposes the one `ShotBeep`
· `util/Constants.java` (225 L) — 40 M5 constants: recoil (recovery per degree 0.055 + min 0.08 / max 0.40, speed 7.5, hold bleed 3.0, ramp shot 3 × 1.25, pitch cap 14°, yaw alternate 0.12 / jitter 0.08 / cap 4°, bloom 2.5×), shake (0.20 per recoil degree, cap 3.0°, decay 14/s, 1400 °/s noise, roll 0.6), flash (70 ms, anchor 0.50/0.38, size 0.22, star 0.85/0.16, warm colour), marker (160 ms, gap 0.012, spread 0.008), beep (22050 Hz, 2048 samples, 760 Hz + 0.35 octave, decay 42/s, gain 0.45, volume 0.6) and `SOUND_ENABLED`
· `SPEC.md` — new **Game Feel (master M5)** section (effect order, full-recovery rules, the M20 world-space note the user asked for, shake, flash, beep), new **House Asset Integration — M2b.5 (planned)** section, M5 ✅ in both tables, M2b.5 row added to the mapping table, known issues 1 + 18 rewritten and 19–22 added, M5 cost in the budget, Missing Assets updated
· `README.md` — new **Gun feel** subsection under *Playing the game*

What works:
· Firing now feels like firing: the view climbs by the weapon's recoil (pistol 1.2°/shot) with a left-right sway that widens down the burst, harder from shot 4; the crosshair blooms open; a warm flash pops for 70 ms; a short crack plays; any impact flashes a hit marker for 160 ms; the camera shakes and rolls proportionally (shotgun 4.5° kick ⇒ 0.9° of shake, pistol ⇒ 0.24°).
· **Full recovery, as specified**: let go and the aim returns to exactly the point you were holding — verified 0.0000° of leftover pitch *and* yaw for all five weapons at 60/30/20 Hz. Nothing is transferred into the stored aim, so there is no partial-transfer constant to argue about.
· Sprays stay controllable: SMG plateaus at 5.5° after ~10 rounds (held-bleed equilibrium), rifle at 5.0°, and the 14° cap means an endless spray cannot end up pointing at the sky. Recovery time scales with the weapon: pistol 0.17 s, shotgun 0.32 s, sniper 0.40 s.
· Cost: **+0 draw calls, +0 triangles** — flash (4 quads), marker (4 quads) and crosshair all draw inside the existing HUD batch with one new generated 64×64 glow; ~40 float ops per frame, zero allocation (R6/R30). APK still 8.6 MB: no image and no sound file was added.

What's pending:
· Arms model, viewmodel kick and the **world-space** muzzle attach point: master M20 (SPEC records the shift, the flash class survives as the fallback).
· Bullet spread: recoil moves the camera and the ray follows it, but there is no spread cone yet — master M22. `RecoilState.intensity()` is already the 0..1 value a cone should read.
· Real gun samples per weapon: master M13 deletes `audio/ShotBeep` entirely.
· Head bob, footsteps and sprint FOV: master M6 — they should reuse the same `apply()` offsets.
· The hit marker only ever means "the world took a bullet hole" until bots exist (master M10).

Known issues:
· The flash is anchored to a fixed screen fraction (0.50, 0.38), so it reads as detached while crouching or looking straight down. Deliberate until M20 (SPEC known issue 19).
· One beep for all five weapons, and requests coalesce, so an SMG spray sounds like a buzz rather than 12.5 distinct cracks. Placeholder by design (known issue 20).
· Feel constants are simulated, not play-tested: the sandbox has no JDK and no device. All of them live in one `Constants` block, so tuning is a one-file change after the user plays it (R7).

Next milestone: user's choice (R4 — wait for "go"). Candidates:
· **A (recommended)** master M6 — head bob + footsteps + sprint FOV (Track B continues; the user's own schedule puts it 3 days out).
· **B** master **M2b.5** — house asset integration, planned below, starts the moment the user's asset drop lands (one building first).
· C master M10 (repo M7a) — 3 bots that patrol and shoot back; finally applies damage.
· D master M16 + M17 (repo M2c) — industrial zone + military base.
Notes for next model:
· **Never write recoil, shake or bob into `FirstPersonCamera.yaw/pitch`.** All transient view offsets go through `apply(player, yawOffset, pitchOffset, rollDegrees)`; that is the whole mechanism behind the user's full-recovery requirement, and a single `yaw +=` in the wrong place silently breaks it.
· **Keep the effect order** in `GameScreen.updateGameplay()`: `look → movement(base yaw) → recoil → shake → apply → weapons`. Reordering makes movement inherit the kick (the player drifts when firing) or makes the shot ray use last frame's aim twice.
· One shot event only: `WeaponController.firedThisFrame()` + `getLastShotRecoil()`. Do not poll the trigger for effects, or reload and dry fire will kick the camera.
· Pure-math classes stay libGDX-free where possible (`RecoilState` uses only `MathUtils`, `ScreenShake` likewise) so they can be ported and simulated: `/home/user/sim_m5.py` is a 1:1 Python port and prints peak climb, settle time and leftover offset per weapon per frame rate. Re-run it after changing any recoil constant.
· `/home/user/check_java.py` (recreate it if the workspace was re-cloned) statically checks line limits, bracket balance, `com.brfps.*` imports, `Constants.*` fields, constructor arity, method existence **and arity** on our own types — 57 classes, 0 errors before this push. Run it before every push; CI is still the only real compiler (R18).
· Verified against libGDX 1.12.1 sources through `gh api` (raw.githubusercontent and repo1.maven.org are both blocked in this sandbox; **codeload.github.com works**, so whole-repo tarballs are downloadable if you ever need more than single files): `AudioDevice.writeSamples(short[],int,int)` / `setVolume(float)`, `Audio.newAudioDevice(int,boolean)`, `Pixmap.drawPixel(int,int,int)` / `setBlending`, `SpriteBatch.draw(Texture,x,y,originX,originY,w,h,scaleX,scaleY,rotation)` (the 9-float overload), `Vector3.rotate(float,float,float,float)`, `MathUtils.clamp/random/sinDeg`.
· `audio` is a new package: keep every placeholder SFX there so master M13 can replace the whole package with `SoundManager` in one diff.

CI: <filled in after the push — see the commit message and the PR checks>

---

## Plan: M2b.5 (master, new) — house asset integration — NOT STARTED, do not commit yet

Written on 2026-09-13 at the user's request ("M5 ke baad M2b.5 plan karo — abhi commit
nahi"). Track A insert between M9 and M16. Scope also lives in
`SPEC.md → House Asset Integration — M2b.5 (planned)`; this is the working checklist.

**Gate: three answers still missing from the user** — (1) file **format**, (2) **how many**
buildings, (3) **source + licence**. Format decides the loader:

| Format | Loads directly? | Work needed |
|---|---|---|
| `.glb` | ✅ yes | none — `gdx-gltf` 2.2.1 is already a pinned dependency |
| `.gltf` | ✅ yes | none (keep the `.bin` and textures next to it) |
| `.fbx` | ❌ no | `fbx-conv` → `.g3dj` (R24), or export from Blender |
| `.obj` | ⚠️ static only | no animation, and libGDX needs `ObjLoader` (verify the 1.12.1 API first) |
| `.blend` | ❌ no | export to `.glb` from Blender |

Sandbox note for whoever runs it: `kenney.nl`, `quaternius.com` and `sketchfab.com` are
**not reachable from the AI sandbox**, and Maven Central is blocked too; `github.com`,
`api.github.com` and `codeload.github.com` are. A GitHub mirror of the Quaternius +
Kenney free packs (`beep2bleep/FreeAssetsByKenneyNLandQuaternius`) was checked on
2026-09-13: 919 `.fbx` + 748 `.blend` building models, but its only 59 `.glb` files are
Kenney *Platformer Kit* blocks — so **GLB building packs are not sitting on GitHub**; the
user's own download from kenney.nl / quaternius.com is the realistic source. If the drop
arrives as `.fbx`, either convert with `fbx-conv` (R24's documented pipeline) or ask the
user to re-export as `.glb` — do not add a new dependency to read FBX (R15).

**Expected layout** (user's machine: `D:\brfps-assets\buildings\`; in-repo target
`assets/buildings/<name>/<name>.glb` + `info.txt`):

```
assets/buildings/house_medium/house_medium.glb
assets/buildings/house_medium/info.txt   ← Building, Source URL, Author, License,
                                            Size, Texture, Triangles, Dimensions WxHxD,
                                            Interior yes/no
```

`Dimensions` is the field that matters most: it is what the scale check compares against.

**Phase 1 — ONE building (house_medium), one location, one screenshot.**
1. Load with `Gltf.loadModel(Gdx.files.internal("buildings/house_medium/house_medium.glb"))`
   — verify that exact call against the gdx-gltf 2.2.1 sources before using it (R10).
2. Assume **1 unit = 1 m**; draw a **6 × 4 × 6 m debug reference box** next to the model
   (`MeshKit` + the debug package, gated by `DEBUG_TOOLS_ENABLED`) and compare. 60 m tall
   ⇒ scale 0.1; 0.6 m ⇒ scale 10. Put the factor in the layout JSON, never hardcoded.
3. Check texture embedding: a `.glb` from Kenney/Quaternius/Sketchfab normally embeds it;
   a `.gltf` does not — a separate PNG must sit in the same folder with the same base name.
4. Check orientation: local **+Z must stay the front** (`BuildingType`/`BuildingCollider`
   both assume it, rotated by `def.rotation`), otherwise every doorway faces sideways.
5. Collision: keep the **existing solid boxes** from `BuildingCollider`/`ColliderBoxes`
   sized by `BuildingType`. No interior collision in this milestone — that refinement
   comes after master M8 (heightfields). If the art's footprint differs from the box,
   scale the box, do not hand-place walls.
6. Re-use the existing `buildings[]` coordinates in `assets/maps/island.layout` unchanged
   for that one entry (data-only, R23/R42).
7. Deliverable: an **editor-view screenshot** (menu → EDITOR, `debug/screenshot.png`) with
   the reference box visible, plus the DC/tri delta in the HUD.

**Phase 2 — the other 31**, only after Phase 1 is signed off by the user.
· Draw calls are the risk: 32 separate models = 32+ draw calls against a budget of 80
  (R28) on top of the 9 a match already uses. Plan for **instancing per building type**
  (`ModelInstance` sharing one `Model` + one `ModelBatch`) or a merged static mesh, and
  record the measured number here before committing.
· Keep the procedural `BuildingFactory` behind a constant (e.g. `USE_MODEL_BUILDINGS`) so
  a missing model falls back to the generated geometry instead of an empty lot (R25).
· Textures must be ≤ 512×512 POT (R12 / SPEC's 512 cap): re-pack anything bigger.
· APK budget < 40 MB (8.6 MB today): CC0 low-poly GLBs are usually 50–500 KB each, so 32
  buildings should land well inside, but measure and write the number into the budget table.

**Definition of done:** the island's 32 placements render with real models, no building
is floating/sunken/mis-scaled against its 6×4×6 reference, the player still collides with
all of them (walk the town in first person), doorways still line up with the walkable
gaps, draw calls stay under 60, and both an editor-view and a first-person screenshot are
attached to the PR.
