# Handoff Log

Newest entry at the **bottom**. Keep the last 10 entries here; move older ones to
`HANDOFF_ARCHIVE.md` (create that file the first time an entry is evicted).

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
CI: run listed in the M9 entry below (docs-only commit, no code path touched).

---

## M9 (master) = M2b (repo) — Town + village buildings

Date: 2026-09-13
Model: Claude (Arena session `01a098fd`)
Status: ✅ Complete
Track: A (world & content)

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
· C master M2 — editor view (grid + XYZ gizmo + free-fly camera), currently parked. Suggested model: GPT.
· D master M18 (repo M2d) — props, bridge, village well, crop fields. Suggested model: Gemini.
Notes for next model:
· Do not inline world geometry into `Arena.java` (289/300 lines, R13) — follow the `BuildingFactory` + `BuildingBatcher` split.
· `MeshKit` uses short indices: never let one mesh pass 32 767 vertices; `BuildingBatcher` flushes at `Constants.BUILDING_VERTEX_LIMIT` (24 000).
· `FlatShader` is package-private in `com.brfps.world`; new world renderers must live in that package or take an already-bound `ShaderProgram` (do **not** create a second program — `Mesh.render` does not bind).
· No JDK/SDK in the sandbox: compile-check by CI only (R18), and keep to libGDX 1.12.1 APIs you can verify (R10).
CI: <filled in the docs commit after this push>
