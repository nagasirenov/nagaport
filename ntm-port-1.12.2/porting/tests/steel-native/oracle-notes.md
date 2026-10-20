# Independent Steel wall/corner oracle

## Files and provenance

`build_oracle.py` generates `OriginalSteelOracle.java` by extracting complete original method declarations/bodies, not by reading any native production implementation. It validates every source and original texture against `source-manifest.json`. Either supply a pristine checkout or use `pristine-steel-sources.zip`, whose entries are byte-identical to that checkout. The bundled archive is deterministic and contains only the listed original source/asset files.

```python
import build_oracle
java = build_oracle.generate(pristine_path_or_None, cached_legacy_client_path_or_None)
```

The CLI is `python3 build_oracle.py --output /tmp/OriginalSteelOracle.java [--pristine ROOT] [--legacy-client JAR]`. The fixture compiles against the normal native classpath with Java 8. No download occurs. If supplied, the cached original 1.7.10 client must hash to `a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce`.

| Source | Independent evidence |
| --- | --- |
| `render/block/RenderSteelWall.java:16,34` | Inventory/world bounds, face calls, exact translation, ordinary RenderBlocks usage. |
| `render/block/RenderSteelCorner.java:17,52` | Inventory face omissions and order, three world pieces, RenderBlocksNT world usage. |
| `blocks/generic/DecoBlock.java:37,58,61,94,104,141,147` | Tool/sneak rotation, unconditional side rendering, no tile entity, yaw placement, bounds, collision envelope and filtered collision pieces. |
| `render/util/RenderBlocksNT.java:21,60,166` | `setWorld` clears mirror/flip flags; north/east full-texture UV corrections. |
| `world/gen/nbt/INBTBlockTransformable.java:23` | Structure rotation metadata mapping. |
| `blocks/ModBlocks.java:1846–1847` | Exact wall/corner identities, common `hbm:steel_wall` texture, iron material, hardness 5/15, resistance 15. |
| `main/ClientProxy.java:871–872` | Distinct original renderer registration. |
| Original client `blm.b(aji,int,int,int)` | Before render-type dispatch, bytecode offsets 14–28 call owner bounds update and renderer bounds-from-owner. |
| Original client `aji(awt)` and `azt.b(azt)` | Block constructor offsets 37–44 set initial full bounds; original AABB intersection compares all three axes strictly (touching-only is false). |

The JSON manifest supplies exact line numbers and SHA-256 hashes, including per-method hashes. Source paths in this table are relative to pristine `src/main/java/com/hbm/`.

## Fixture API

- `layout(corner, meta, item)` returns a fresh-owner original layout.
- `layout(corner, meta, item, priorBounds)` records the normal world-dispatch sequence with a specified preceding singleton-owner bounds value.
- `Layout.faces`: original call order. Each `Face` has `double[6] bounds`, side index 0–5, `nt`, and the explicit inventory normal. `Layout.boxes` contains `double[6]` world renderStandardBlock calls. Inventory is represented by its face calls rather than a fabricated cuboid list.
- `Layout.translation`: exact original GL translation. `starts`, `draws`, and `brightnessCalls` retain batch/brightness-call counts, not a lighting simulation.
- `LegacyFaces(File).face(face, sprite)` returns original vertex order, four XYZUV arrays. Ordinary faces execute the actual cached original RenderBlocks/Tessellator bytecode in an isolated loader. Corner-world north/east execute the extracted original RenderBlocksNT methods. Results use original float storage precision.
- `LegacyFaces.face(face, sprite, layout, item)` additionally converts original centered inventory GL coordinates into native block-centered coordinates by adding the recorded translation plus `(0.5,0.5,0.5)`. UV coordinates are never shifted with geometry.
- `selection(corner, meta[, priorBounds])` returns the original local selection bounds.
- `collision(corner, meta, x, y, z, mask[, priorBounds])` returns original filtered, world-offset collision pieces. `AxisAlignedBB.bounds()` returns the six coordinates.
- `placed(corner, float yaw)` returns a recorded `World` with `meta`, `writes`, and notification `flags`.
- `screw(corner, meta, sneaking, screwdriver)` returns `ScrewResult.handled` and `.world` with the same write fields.
- Extracted owner methods are accessible through `owner(corner)`; extracted structure rotation is `INBTBlockTransformable.transformMetaDeco(meta, mode)`.

The fixture uses minimal original-API call recorders for collaborators, not surrogate native Minecraft classes. Its six-coordinate AABB helper and initial full owner bounds were separately checked against the cached original client bytecode (`azt.b(azt)` and `aji(awt)`). Full native owner/model/handler tests must compile and invoke the real migrated owners separately. This oracle does not prove native lighting, model registration, resource reload, engine culling, or graphical acceptance.

## Geometry and inventory invariants

Wall world bounds for metadata 2/3/4/5, respectively:

- `[0,0,.875, 1,1,1]`
- `[0,0,0, 1,1,.125]`
- `[.875,0,0, 1,1,1]`
- `[0,0,0, .125,1,1]`

Each valid corner world state has three pieces; the collision list uses exactly the same order and coordinates:

- Meta 2: `[.25,0,.875, 1,1,1]`, `[0,0,.75, .25,1,1]`, `[0,0,0, .125,1,.75]`.
- Meta 3: `[0,0,0, .75,1,.125]`, `[.75,0,0, 1,1,.25]`, `[.875,0,.25, 1,1,1]`.
- Meta 4: `[.875,0,0, 1,1,.75]`, `[.75,0,.75, 1,1,1]`, `[0,0,.875, .75,1,1]`.
- Meta 5: `[0,0,.25, .125,1,1]`, `[0,0,0, .25,1,.25]`, `[.25,0,0, 1,1,.125]`.

All world faces render, including adjacent/internal faces: original `shouldSideBeRendered` is always true. Thus wall has 6 faces and valid corner has 18, regardless of neighbors.

Inventory ignores metadata entirely. Wall always uses meta-2 bounds, six explicit faces in `UP, DOWN, EAST, WEST, SOUTH, NORTH` order, and translation `(-.5,-.5,-.9375)`. Native-centered Z therefore spans `.4375..5625`, a `-.4375` mesh shift from raw bounds.

Corner inventory is not simply the metadata-4 world mesh: its piece call order is reversed, all faces use ordinary RenderBlocks, and two faces are omitted:

1. `[0,0,.875, .75,1,1]`: `UP, DOWN, WEST, SOUTH, NORTH` (EAST omitted).
2. `[.75,0,.75, 1,1,1]`: `UP, DOWN, EAST, WEST, SOUTH, NORTH`.
3. `[.875,0,0, 1,1,.75]`: `UP, DOWN, EAST, WEST, NORTH` (SOUTH omitted).

There are 16 inventory faces and translation `(-.5,-.5,-.5)`. Neither renderer issues an inventory rotation, scale, matrix push, or matrix pop. Both report `shouldRender3DInInventory == true`; the standard item camera orientation comes from external engine dispatch, not an extra renderer-local rotation. Each inventory renderer begins and draws exactly one quad batch.

## Exact UV distinction

For default zero UV-rotation/false flip state and in-unit bounds, actual original RenderBlocks execution establishes normalized texture coordinates:

- DOWN/UP: `(u,v)=(x,z)`.
- SOUTH: `(x,1-y)`.
- WEST: `(z,1-y)`.
- NORTH: `(minX+maxX-x,1-y)`.
- EAST: `(minZ+maxZ-z,1-y)`.

RenderBlocksNT changes NORTH to `(1-x,1-y)` and EAST to `(1-z,1-y)`, mirroring across the entire texture rather than just the selected bounds. The pristine extracted methods are the authority rather than this summary. Wall world and both inventory renderers use ordinary RenderBlocks; only corner world uses NT. A full cube cannot expose this distinction, so retain asymmetric/partial bounds and an offset atlas sprite in tests.

## Stateful invalid metadata and edge cases

Do not normalize invalid wall metadata silently. Original wall renderer and owner bounds switch both lack a default. The original owner is a registered singleton. Its initial bounds are full-block, but a preceding valid wall selection/collision/render query changes the singleton bounds. Normal render dispatch first asks that owner to update bounds and then copies them into the RenderBlocks caller; an invalid state preserves the last bounds. The oracle exposes `priorBounds` to distinguish fresh-full from each of the four valid predecessors. Inventory renderer bounds are local to its RenderBlocks and do not update the owner.

Corner selection and collision envelope remain full-block for every metadata. Invalid corner metadata has no renderStandardBlock calls and no collision pieces, even though the broad collision envelope is full. Do not turn invalid corner states into a visible corner or colliding cube.

Recommended finite coverage:

- Every saved nibble 0–15 for each owner in world and inventory modes.
- Invalid wall 0,1,6–15 after fresh full bounds and each valid predecessor; include selection, collision and extended-state snapshot stability.
- All 16 metadata × both sneak states × screwdriver/non-screwdriver. Valid clockwise cycle is `3→4→2→5→3`; sneaking reverses it. Screwdriver returns true even for invalid metadata but performs zero writes. Other tools return false and perform zero writes. Writes use notification flags 3.
- Placement cardinal values 0/90/180/270 map to 3/4/2/5, flags 2. Compare the exact extracted float-expression behavior at negative yaw, full-turn offsets, and `Math.nextDown/nextUp` of each 45-degree boundary. Avoid replacing the original float intermediate arithmetic with an idealized double equation.
- Collision masks outside, inside each arm, inside the empty center, touching-only, and straddling junctions; nonzero positive/negative block coordinates. Intersection is strict: mere boundary contact is excluded. Corner selection must still be full.
- Structure rotation all metadata × modes 0–3; out-of-range modes preserve metadata.
- No tile entity for either selected owner, non-opaque/non-normal cube, and all six side queries true.

## Independent development check

A temporary (uncommitted) oracle-only Java self-test compiled the generated fixture against the real native classpath and passed 13,783 checks: 520 original-source/client face evaluations, all 16 metadata for both shapes in world/inventory, exact inventory ordering/omissions, placement flags, tool/sneak/write counts, all invalid wall predecessor cases, collision offsets/contact exclusion, and asymmetric north/east vanilla-versus-NT UV differences. This is a fixture sanity check, not the migrated-production pass result. The production proof belongs to the focused runner's native tests.
