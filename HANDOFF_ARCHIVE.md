# Handoff Archive

Older entries evicted from `HANDOFF.md`, which keeps only the last 10 (R48).
Newest entry at the **bottom**. Format and numbering convention: see `HANDOFF.md`.

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

