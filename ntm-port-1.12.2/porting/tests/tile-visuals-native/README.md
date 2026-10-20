# Native magma and multiblock-preview TESRs

Base: CP49 `caf22cf027f33f9d0d0be366454c2f54fdba7915`.

## Finite production ownership

Exactly three complete production files: `RenderMagma.java`, `RenderMultiblock.java`, and `RenderSoyuzMultiblock.java`, all under `src/main/java/com/hbm/render/tileentity/`.

No tile, machine mechanics, registry, texture, mesh, build-input, or shared helper changes. No source exclusions, Minecraft/Forge substitute classes, legacy RenderBlocks production adapter, or downloads. The existing three ClientProxy binding statements are extracted unchanged, compiled, and executed using the actual native `ClientRegistry` and `TileEntityRendererDispatcher`; the test then enters each complete native callback through the real dispatcher. This is not a token-only registration check, but does not certify complete ClientProxy initialization.

- Magma retains all original model/material selections, model-part draw order, orientation, runtime height-offset query, interpolated opposed DrillHead/Blades rotations, and strict `lavaHeight > 0.01F` gate. The nine lava tops use native POSITION_TEX_NORMAL vertices matching original `blm.renderFaceYPos` bytecode, including UV endpoints, winding, normal bytes, height, ordering, and nontrivial atlas placements. Native lava's complete decoded animation strip and metadata equal original vanilla, despite differing PNG compression.
- Multiblock retains the eight compact-launcher mini-cubes and the 80-pad-plus-11-scaffold large table. All four one-second scaffold phases retain original wall-clock selection, placement, and order.
- Soyuz retains all 1,155 original mini-cubes: 414 launcher, 294 concrete, and 447 scaffold cubes. RenderBlocks was only a material-name lookup in both preview classes. The original registrations and inheritance show fixed `struct_launcher`, `struct_scaffold`, and `concrete` textures. These same standalone PNG materials are bound directly, not replaced by native particle sprites or block/item models.
- Preview cubes retain original six separate quad batches, 24 float-derived positions/UVs, 180-degree Z rotation, alpha 0.75, culling and alpha-test behavior. No normal/color/lightmap attributes are invented.
- Existing tested `NativeProjectionScope` gives each native callback lexical matrix/GL/cache ownership, including exceptional exit. Draw-state comparisons remain against the original raw-GL callback. The port does not deliberately propagate the original callback's unrelated color/blend/lighting leakage to subsequent native renderers.

## Reproduce

From the module directory, with existing genuine Forge 1.12.2 dependencies and Java 8:

```
python3 porting/tests/tile-visuals-native/run.py \
  --java-home /path/to/java8 \
  --classpath "$NATIVE_CLASSPATH" \
  --legacy-client /path/to/original-minecraft-1.7.10.jar
python3 porting/tests/tile-visuals-native/check-mapping.py
```

Original client SHA256: `a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce`.

The original LGPL sources needed for independent oracles are bundled in `original-sources.zip`, with hashes in `scope.json`. An optional `--original-root /path/to/ntm-original-pristine` also compares the original asset bytes. No original Minecraft binary/assets are redistributed. The existing native classpath must include the original LWJGL Linux native JAR; the GL harness uses installed Mesa EGL. Python, Java, and classpath locations are supplied explicitly, without relying on this cloud workspace. Windows execution is not certified.

`--negative-control lava-uv`, `--negative-control preview-geometry`, and `--negative-control animation-direction` mutate only a temporary compiled copy. Each must fail its specific independent runtime assertion. Production files are never mutated. `--write-scope` is only for an intentional reviewed source freeze, not routine reproduction.

The source guard exposes `reverse_source(relative, text)` for composition before older historical audit layers. It checks exact complete native hashes, reconstructs the three CP49 files, proves the forward roundtrip, and rejects source/line-ending drift. It changes only the historical test view, not compilation inputs. There are no shared-file inversions or shared-file replacements in this slice.

## Verified evidence

Final positive run: 2,496,434 assertions, 100,872 original/native vertex comparisons, and 1,585,152 actual offscreen GL pixel-byte comparisons. These are test assertions, not defect counts or a completion percentage.

The complete three renderers and 15 unchanged native dependencies compile without platform replacements. Original world callback bodies are retained in separate oracle classes, with endpoint/name-only mapping to native holder types and an original Tessellator adapter. The already-ported original Magma inventory provider is removed only from the oracle, not production. Every legacy vertex/draw call executes the hash-pinned original `bmh` bytecode; lava faces additionally execute original `blm` bytecode. Recording native Tessellator subclasses delegate actual native draw behavior and capture its real BufferBuilder data before submission. The original `blm` is allocated without a game constructor solely to exercise its pure face method with explicit default non-AO/non-rotated bounds/icon state.

Tests cover all 16 magma metadata values, partial ticks 0/0.25/1, threshold boundary and nonzero heights, opposed interpolation, two atlas layouts, height offsets -2/0/3/7, native damage-stage/alpha arguments, exact group selection, all complete preview batches/materials/positions, all four real-clock large-table phases, actual original magma OBJ loaded into the unchanged native HFR VBO backend, representative native/original framebuffer equality, texture/model exceptions, buffer availability, and real/cache state restoration after mixed raw-GL/native callers. All 20 lava animation frames' source pixels and the entire frame/time sequence are compared, not a live animation clock.

## Explicit limits

NTM block registry identities, read-only tile fields/block metadata, and ResourceManager's holder are controlled fixtures. Model-state tests record named group calls; the GL phase additionally delegates them to the actual production native HFR VBO and unchanged original model/texture assets. Tile ticks, structure assembly, machine inventory/energy/fluid mechanics, packet synchronization, complete owners, full initialization, and server/save behavior are not compiled or certified by this probe. No production tile or synchronization behavior was changed.

The HFR mesh backend is reused and source-pinned, not independently re-certified against the entire old model parser/VBO implementation by this probe. Full old/native vertex comparisons here concern the explicit mini-cubes and lava tops. Framebuffer comparisons validate complete TESR transforms/materials against the original callback using that same already-migrated mesh backend. No loaded client/gameplay, shader-pack, arbitrary third-party renderer, resource-pack replacement, live atlas animation, or Windows runtime acceptance is claimed. This slice introduces no new world light solver, makes no global lighting replacement, and does not waive the separate CP37 or other checkpoint visual gates.
