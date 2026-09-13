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
