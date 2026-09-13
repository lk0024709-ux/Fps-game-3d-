# RULES — android-fps-battle-royale

All 50 project rules. Every model (DeepSeek, GPT, Claude, Gemini, Grok) and every
human contributor follows these. If a rule and a convenience disagree, the rule wins.
If a rule and reality disagree, stop and ask the user — do not silently break it.

Rule ids are stable: `R1`…`R50`. Reference them in commits, handoff notes and PRs
(e.g. `M2b: batch buildings into one mesh (R28)`).

---

## CRITICAL — never break

| # | Rule |
|---|---|
| R1 | Do NOT skip milestones. Complete one, CI green, then the next. |
| R2 | One milestone = one or a few commits. Small diffs. |
| R3 | CI must be green before new code. If `main` is red, fix it first. |
| R4 | Plan before a milestone (files, deps, placeholders, time, risks). Wait for the user's "go". |
| R5 | If stuck after 2 attempts, STOP and ask the user. No third silent try. |
| R6 | Zero allocation in the render loop. Reuse objects (`Vector3`, `Color`, arrays). |
| R7 | `util/Constants.java` is the single source of truth for gameplay numbers. |
| R8 | No PvP, no networking, no login, no cloud save. Offline only. "PvP" in this project always means player vs NPC bots. |

## Technical

| # | Rule |
|---|---|
| R9 | Pin ALL dependency versions. Never `latest`, never `+`. |
| R10 | Check libGDX 1.12.1 docs before using an API. Do not invent methods. |
| R11 | No `TODO` stubs in committed code. Either it works or it is not committed. |
| R12 | Textures power-of-two only (256, 512, 1024, 2048). Project cap is **512×512** for mobile — see SPEC → Known Issues. |
| R13 | File ≤ 300 lines. Split into a helper class when longer. |
| R14 | One-line Javadoc per class (plus per public method where non-obvious). |
| R15 | Never add a dependency without asking the user first. |

## Git & Build

| # | Rule |
|---|---|
| R16 | Commit format: `M<number><sub?>: <description>` (e.g. `M9: town and village buildings`, `M0: add project documentation files`). See *Milestone numbering* below for which scheme a new commit uses. |
| R17 | Never commit `build/`, `.gradle/`, `local.properties`, `*.apk`, `*.aab`, `*.jks`, `*.keystore`. |
| R18 | If no JDK/Android SDK locally, push and verify via CI. Never skip verification. |
| R19 | If a build exceeds 15 min, add more Gradle caching. |
| R20 | Read the CI log after every push. Fix red before writing new code. |

## Milestone numbering (user decision, 2026-09-13)

Two schemes exist in this repo's history. Use them like this — guessing is not allowed:

1. **Old commits keep repo ids.** `M0`, `M1`, `M2a` are merged with lettered ids.
   Never rename, renumber or rewrite history for them.
2. **New milestones use master ids M1–M47.** Every new commit, PR title, SPEC row and
   HANDOFF entry uses the master number: `M9: town and village buildings`.
3. **Docs always show both**, in the form `M9 (master) = M2b (repo)`. HANDOFF entries
   are titled that way and carry a `Sequence:` line such as
   **`M3 (master) = after M2a (repo)`** — master id first, then what precedes it.
4. `SPEC.md → Milestone Mapping Table` is the single translation source. Read it before
   choosing the next milestone; update it when a milestone completes.
5. **Two parallel tracks** (world/content first in this repo, game feel first in the
   master prompt) mean R1 applies *inside* a track: never skip a milestone of the track
   you are working on, and say which track a milestone belongs to in the HANDOFF entry.

## Scope

| # | Rule |
|---|---|
| R21 | `AndroidManifest.xml` permissions: NONE. Not even `INTERNET`. |
| R22 | Debug build only until the release-signing milestone (repo M13 / master M44). |
| R23 | New maps are data only — no map code changes (JSON layout + `index.json`). |
| R24 | Model format: `.g3dj` in development, `.g3db` in release. Never the deprecated `.g3d`. |
| R25 | Missing asset → procedural placeholder (box / colored quad / silent stub) + a note in SPEC → Missing Assets. |
| R26 | No cloud sync. libGDX `Preferences` API only. |
| R27 | No real-time shadows below the ULTRA tier. Fake blob shadows under characters. |

## Performance

| # | Rule |
|---|---|
| R28 | Draw calls < 80. |
| R29 | Visible triangles < 80,000. |
| R30 | Frame allocations = 0. |
| R31 | Desktop 60 FPS, Android 30 FPS minimum (Snapdragon 660-class). |
| R32 | Object pooling for bullets, particles, damage numbers (`util/ObjectPool.java`). |
| R33 | Frustum culling on all chunks. |
| R34 | Quality tiers LOW / MEDIUM / HIGH / ULTRA with auto-detect + manual override. |

## AI & Bots

| # | Rule |
|---|---|
| R35 | All bot parameters live in `ai/BotProfile.java` (16 params × 7 ranks). |
| R36 | Behavior tree, not a state machine. |
| R37 | Rank-based difficulty scaling (Bronze → Grandmaster). |
| R38 | Match bot distribution: −1 rank 30%, same rank 40%, +1 rank 20%, +2 boss 10%. |
| R39 | Fake bot names come from `util/RandomNames.java`. No real player names, no brand names. |

## Save & Data

| # | Rule |
|---|---|
| R40 | libGDX `Preferences` API for all saves (`save/LocalSaveManager.java`). |
| R41 | Auto-save after every match. |
| R42 | Map data lives in layout JSON (`assets/maps/island.layout`, `desert.layout`). |

## UI / UX

| # | Rule |
|---|---|
| R43 | Debug overlays OFF in release, controlled by a single boolean. |
| R44 | All debug-only code lives in the `com.brfps.debug` package. |
| R45 | Touch controls are first-class, keyboard/mouse secondary. |
| R46 | No hardcoded UI pixel positions — everything viewport-relative. |

## Documentation

| # | Rule |
|---|---|
| R47 | `SPEC.md` is a living document. Update it in every milestone. |
| R48 | `HANDOFF.md` gets a new entry at the end of every milestone. |
| R49 | SPEC → Missing Assets and SPEC → Known Issues stay current. |

## Communication

| # | Rule |
|---|---|
| R50 | Reply to this user in plain Hindi (non-technical). Format: kya bana rahe ho (1 line) · kitni files · kitna time · kya todne ka risk hai. |

---

## Handoff protocol (R48, detail)

Append to `HANDOFF.md` at the end of every milestone:

```
## M<number> — <description>
Date: <YYYY-MM-DD>
Model: <DeepSeek | GPT | Claude | Gemini | Grok>
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

Keep the last 10 entries in `HANDOFF.md`; move older ones to `HANDOFF_ARCHIVE.md`
(create that file the first time an entry is evicted).

## Workflow (R1–R5, detail)

1. **READ** — `SPEC.md`, `RULES.md`, `HANDOFF.md`, `README.md`, `git log --oneline -20`, CI status on `main`.
2. **IDENTIFY** — next milestone = last completed + 1 (see the milestone table in SPEC.md), or ask the user.
3. **PLAN** — files to create, files to modify, new dependencies (ask first), placeholder strategy, time estimate, risk areas, questions. Then **wait for "go"**.
4. **CODE** — follow all 50 rules, commit with the R16 format, push the working branch.
5. **VERIFY** — read the CI log. Red → fix and re-push (max 2 attempts), then stop and ask (R5).
6. **HANDOFF** — update SPEC.md (milestone ✅, Missing Assets, Known Issues) and append the HANDOFF.md entry; commit `M<n>: update docs after milestone`.
7. **WAIT** — never auto-start the next milestone.

## Model strengths (suggestions, not hard assignments)

| Model | Strength | Suggested milestones (repo ids) |
|---|---|---|
| DeepSeek | Logic, math, algorithms | M5b, M5c, M3d, M2e, M7a–M7d, M6a |
| GPT | Architecture, planning | M2f, M2b, M8, M9a–M9b, M4, M6c |
| Claude | Long context, refactors | M3a, M6d, M2h, M7c, M6c, M9c |
| Gemini | Research, docs, visual assets | M2d, M2g, M12, M13 |
| Grok | Debugging, optimization | M2e, M9c, M11, M6e, M13 |
