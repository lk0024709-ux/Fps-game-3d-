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

## M1 — Project skeleton, libGDX setup, CI debug APK workflow

Date: 2026-09-12
Model: not recorded (Arena session `01a0964f`)
Status: ✅ Complete

Files added:
· `.github/workflows/build.yml`, `.gitignore`, `LICENSE`, `README.md`, `SPEC.md`
· `build.gradle`, `settings.gradle`, `gradle.properties`, `gradlew`, `gradlew.bat`, `gradle/wrapper/*`
· `android/build.gradle`, `android/proguard-rules.pro`, `android/src/main/AndroidManifest.xml`
· `android/src/main/java/com/brfps/android/AndroidLauncher.java`, `android/src/main/res/**` (launcher icons, strings, styles)
· `core/build.gradle`
· `core/src/main/java/com/brfps/BrFpsGame.java`
· `core/.../screens/SplashScreen.java`, `MainMenuScreen.java`, `GameScreen.java`
· `core/.../util/Assets.java`, `Constants.java`, `Math3D.java`, `ObjectPool.java`
· `core/.../weapons/Weapon.java`, `WeaponType.java`
· `core/.../items/Item.java`, `ItemType.java`
· `core/.../player/PlayerStats.java`, `PlayerInventory.java`
· `core/.../enemies/BotDifficulty.java`
· `assets/{models,textures,sounds,ui}/.gitkeep`

Files modified:
· (none — first commit of the tree)

What works:
· `./gradlew android:assembleDebug` succeeds in CI; artifact `brfps-debug-apk` ≈8.6 MB (14-day retention).
· Splash → Main Menu → GameScreen(placeholder) → back, on both desktop and Android; all versions pinned (libGDX 1.12.1, gdx-bullet 1.12.1, gdx-gltf 2.2.1, Gradle 8.7, AGP 8.5.0, JDK 17, minSdk 24, target/compile 34).

What's pending:
· No gameplay yet — no player controller, no world, no bots.

Known issues:
· `gdx-gltf` 2.2.0 had no published JitPack artifact; pinned to **2.2.1** instead (R9).

Next milestone: M2a — island terrain + zone markers + roads + JSON map layout
Suggested model: DeepSeek (geometry / math)
Notes for next model:
· AndroidManifest has zero permissions and must stay that way (R21).
CI: run `34705239770` — ✅ green

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
