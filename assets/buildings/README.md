# `assets/buildings/` — house models for M2b.5

Starter pack for the **M2b.5 house asset integration** (see `SPEC.md → House Asset
Integration — M2b.5` and the plan at the bottom of `HANDOFF.md`). Nothing in `core/`
reads this folder yet: the game still renders the procedural `BuildingFactory`
geometry, and the APK is unaffected apart from the 536 KB these files add to
`android/assets`.

## What is here

Four **CC0** houses from Kenney's *City Kit Suburban (2.0)*, renamed to this project's
`BuildingType` ids, one folder each, with a measured `info.txt`:

```
assets/buildings/
├── house_small/          house_small.glb         63.8 KB    770 tris
│   ├── Textures/colormap.png   (11.5 KB, shared atlas)
│   └── info.txt
├── house_medium/         house_medium.glb        96.3 KB   1174 tris   <- PHASE 1 test
├── house_two_storey/     house_two_storey.glb   143.9 KB   1757 tris
└── shop/                 shop.glb               163.6 KB   2062 tris
```

Total 536 KB, 5 763 triangles for the four. Every model is glTF 2.0 binary, one mesh,
one primitive, one material, no animation, no skinning, `UNSIGNED_SHORT` indices and
`POSITION / NORMAL / TANGENT / TEXCOORD_0` — i.e. exactly what `gdx-gltf` 2.2.1 (already
a pinned dependency) loads directly, with no conversion step.

## Licence (keep this text with the assets)

```
City Kit Suburban (2.0)
Created/distributed by Kenney (www.kenney.nl)
Creation date: 23-04-2025 20:22

License: (Creative Commons Zero, CC0)
http://creativecommons.org/publicdomain/zero/1.0/

You can use this content for personal, educational, and commercial purposes.
Support by crediting 'Kenney' or 'www.kenney.nl' (this is not a requirement)
```

Downloaded on 2026-09-13 through `api.github.com` from the mirror
[`ronmurphy/CityBuilder`](https://github.com/ronmurphy/CityBuilder)
(`models/City Kit - Suburban/Models/GLB format/`), because `kenney.nl` is not reachable
from the AI sandbox. Original page: <https://kenney.nl/assets/city-kit-suburban>.
CC0 needs no attribution; the credit is kept here anyway, and this project never uses
any commercial game's assets (README).

## Three things measured before download (the M2b.5 risk list)

1. **The texture is NOT embedded.** Every one of these GLBs declares
   `images[0].uri = "Textures/colormap.png"`, so the PNG must stay in that subfolder next
   to the `.glb` or the model loads untextured. That is the ⚠️ case from the checklist —
   Kenney's GLB export keeps the atlas external.
   Good news: it is **one 512×512 atlas shared by the whole kit** (all four copies here
   are byte-identical), inside the project's 512 cap (R12), and the UVs of each building
   occupy a small sub-rectangle of it. So at runtime one texture can serve all 32
   buildings → one material → they can share a single `ModelBatch` pass.
2. **The scale is not 1 unit = 1 m.** A house is 1.3 units wide. Cross-checking the same
   author's Roads kit (street light 0.67 units ≈ 5 m, traffic cone 0.08 units ≈ 0.7 m)
   puts **1 unit ≈ 0.14 m**, so the raw kit needs ≈7.2× before it is even human-sized,
   and then a further per-type factor to match the `BuildingType` footprints:

   | id | raw bbox (units) | at kit scale 7.2 | target footprint | suggested uniform scale | result |
   |---|---|---|---|---|---|
   | `house_small` | 1.30 × 0.74 × 0.92 | 9.4 × 5.3 × 6.6 m | 6 × 5 m, wall 3.2 | **0.64** | 6.0 × 3.4 × 4.2 m |
   | `house_medium` | 1.30 × 0.83 × 1.03 | 9.4 × 6.0 × 7.4 m | 6 × 5 m, wall 3.2 | **0.64** | 6.0 × 3.8 × 4.8 m |
   | `house_two_storey` | 1.76 × 1.24 × 1.03 | 12.7 × 8.9 × 7.4 m | 7 × 6 m, wall 6.4 | **0.55** | 7.0 × 4.9 × 4.1 m |
   | `shop` | 1.31 × 1.16 × 1.41 | 9.4 × 8.4 × 10.2 m | 9 × 6 m, wall 3.8 | **0.59** | 5.6 × 4.9 × 6.0 m |

   These are starting numbers, not answers: Phase 1 must confirm them in the editor view
   against a **6 × 4 × 6 m debug reference box**. Per-type factors belong in the layout
   JSON / `BuildingType`, never hardcoded in a loader (R7, R23).
3. **Orientation is unverified.** The nodes carry no rotation, and
   `BuildingFactory` / `BuildingCollider` treat local **+Z as the front** (rotated by the
   layout's `rotation`). If Kenney's front is −Z, every doorway faces away from the road
   and the walkable gap in the collision box no longer matches the art.

## Adding your own buildings

Drop one folder per building, using the same shape, and write an `info.txt` with at least
`Building`, `Source`, `Author`, `License`, `Size`, `Texture`, `Triangles`,
`Dimensions (WxHxD)` and `Interior`. `Dimensions` is the field the scale check needs.

| Format | Loads directly? | What to do |
|---|---|---|
| `.glb` | ✅ | nothing (keep any external texture next to it) |
| `.gltf` | ✅ | keep the `.bin` and the textures in the same folder |
| `.fbx` | ❌ | `fbx-conv` → `.g3dj` (R24), or re-export from Blender |
| `.obj` | ⚠️ | static only, and `ObjLoader` must be verified against 1.12.1 first (R10) |
| `.blend` | ❌ | export to `.glb` |

The whole kit has 41 GLBs (21 building types + fences, paths, driveways, details, 2.6 MB
total) and the mirror also carries Kenney's **City Kit Commercial**, **Industrial** and
**Roads** packs — the industrial one is the obvious source for master M16/M17
(`warehouse`, `factory`, `barracks` are already in `BuildingType` but unplaced).
