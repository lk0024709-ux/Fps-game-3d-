# android-fps-battle-royale

A 3D first-person battle-royale shooter for Android, built with libGDX.
Offline only: 1 human player versus 9 AI bots, shrinking safe zone, ground loot.

> Original assets only. This project never uses art, audio or code from any
> commercial game.

## Tech Stack

| Piece | Version |
|---|---|
| Engine | libGDX 1.12.1 (Java) |
| Language | Java 17 |
| Build | Gradle 8.7, Android Gradle Plugin 8.5.0 |
| Android | minSdk 24, targetSdk 34, compileSdk 34 |
| 3D physics | gdx-bullet 1.12.1 |
| Model loading | gdx-gltf 2.2.1 + libGDX g3d (`.g3dj`) |
| CI | GitHub Actions |

All dependency versions are pinned — no `+`, no `latest`.

## Modules

- `core/` — all gameplay code, platform independent (`com.brfps`)
- `android/` — Android launcher, manifest, resources
- `assets/` — models, textures, sounds, fonts (shared, served from `android/`)

## Build

```bash
./gradlew android:assembleDebug
# -> android/build/outputs/apk/debug/android-debug.apk
```

Install on a connected device:

```bash
adb install -r android/build/outputs/apk/debug/*.apk
```

You need JDK 17 and an Android SDK with platform 34 (`local.properties` should
point `sdk.dir` at it — that file is git-ignored).

## CI

`.github/workflows/build.yml` builds a debug APK on every push and pull request
and uploads it as the artifact **`brfps-debug-apk`** (14 day retention).
Release signing arrives in M10 and not before.

## Documentation

- [`SPEC.md`](SPEC.md) — full game design spec, stats tables, milestone status,
  milestone mapping table, performance budget, known issues, missing assets.
- [`RULES.md`](RULES.md) — the 50 project rules (R1–R50) plus the handoff protocol
  and the milestone numbering rule.
- [`HANDOFF.md`](HANDOFF.md) — what each previous model built, what works, what is
  pending and which milestone comes next. Read this first.

## Playing the game

The main menu's **PLAY** button starts the first-person screen: you spawn in the town
plaza and can walk the island, enter buildings through their doorways, climb the
temple plinth, sprint, crouch and jump.

| Action | Desktop | Phone |
|---|---|---|
| Move | `W A S D` / arrows | virtual stick — appears where your left thumb lands |
| Look / aim | hold **right mouse button** + drag | drag anywhere on the right half |
| **Fire** | hold **left mouse button** | hold **FIRE** (large, bottom-right) |
| Reload | `R` | automatic when the magazine empties |
| Sprint | hold `Shift` | push the stick all the way out |
| Crouch | `Ctrl` or `C` | **CRCH** button |
| Jump | `Space` | **JUMP** button |
| Back to menu | `Esc` / `Back` | `Back` |

You spawn with a pistol (12 rounds in the magazine, 36 in reserve). Shots are hitscan: they stop at the
weapon's range and leave a bullet hole in whatever they hit — walls, door jambs,
interior partitions, the temple plinth and the ground. Buildings are solid, doorways are
walkable, roofs are not climbable. The HUD shows a crosshair, `HP | AR | stance`, the
ammo counter and, in debug builds, `FPS | DC | Tri`, your position and shot statistics.
Details: `SPEC.md → First-Person Controls (master M3)` and `SPEC.md → Shooting
(master M4)`.

## Debug / Editor View

Set `Constants.DEBUG_TOOLS_ENABLED = false` for release builds — one switch turns off
the zone/spawn markers, the chunk grid, the editor screen and its overlays (R43).

The main menu's **EDITOR** button (debug builds only) opens the world viewer: an
orbiting overview camera, or press **F1** / tap **CAM** (top-left) to fly freely —
`WASD`/arrows to move, `Q`/`E` down/up, `Shift` fast, right-mouse-drag to look. On a
phone: drag the left half to fly, drag the right half to look, use the **UP**/**DN**
buttons for altitude. A 10 m/50 m grid, XYZ axis gizmo at the origin and a corner
orientation gizmo show where you are. Details: `SPEC.md → Editor View (master M2)`.

## Commit Convention

```
M<number>: <short description>
```

Example: `M9: town and village buildings`

New milestones use the master-prompt numbers (M1–M47). The first three commits used
this repo's older lettered ids (`M1`, `M2a`) and keep them; `SPEC.md → Milestone
Mapping Table` translates between the two, and docs write both (`M9 (master) =
M2b (repo)`).

## License

MIT — see [`LICENSE`](LICENSE).
