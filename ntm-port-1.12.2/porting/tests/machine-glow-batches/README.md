# Machine glow POSITION_COLOR batches, CP88

Baseline: sealed CP87 `c2f60149e9a9fb836bade505fe566d40a54ef3cc`.

## Delivered scope

Exactly four machine renderer files: `RenderSmallReactor`, `RenderSolarBoiler`,
`RenderDemonLamp`, and `RenderBatteryREDD`. Four independently owned quad sites,
56 static vertex expressions, 18 color-setter sites. Complete inner loops and
batch draw locations remain in place. Mid-batch alpha transitions become explicit
native integer color at each `BufferBuilder.pos(...).color(...).endVertex()`.
The original Tessellator singleton still owns each draw.

Every color expression is evaluated once at its former setter position, not at
each vertex. Float channels retain `float * 255.0F`, Java integer conversion,
then clamp to `[0,255]`, including exceptional inputs in the boundary test.
Geometry, order/winding, vector rotations, control flow, random/time expression
sites, GL states/matrices, model rendering and callback signatures are unchanged.
There is no compatibility facade, dropped visual, global GL workaround, extra
attribute array, renderer stub or new production helper.

## Actual evidence

`check_source.py` exports `FILES` and `reverse_source(path, text)` for composition.
The manifest reverses each complete source file exactly, then deterministically
replays the finite transform. Thirty-two changed-format, mode, endpoint, color
and conversion mutations are rejected. The separate surrounding-source equality
check excludes only reviewed local statements and added imports. All untouched
callbacks, helpers, GL/matrix operations and RNG/clock sites remain identical.
The four exact ClientProxy registrations are checked by `callback-closure.json`;
Tessellator locals have a closed grammar and do not escape to another renderer,
helper or caller-owned batch. Existing native seven-argument TESR signatures are
not remigrated. Battery sparkle/zap helpers and their external paths are unchanged.
This is source callback closure, not a claim of full owner-class runtime linkage.

`run.py` compiles the extracted complete batch bodies against cached actual
1.12.2/Forge 2768/MCP stable_39 and runs them with Java 8. Corresponding original
bodies invoke actual official 1.7.10 `bmh` through reflection. The retained old jar
hash is `a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce`.
`engine-hashes.json` pins the relevant actual native engine class bytes.
The original engine jar is test input only and is not distributed.

Six typed boundary environments and three translations produce 72 paired
machine streams. The reactor stream includes the complete outer shell loop,
with one counted random evaluation at each original setter; both sides receive
the same seeded random stream. Scalar batch-entry fixtures replace only flux,
speed, distance and input timing values, not world or renderer implementations.
DemonLamp uses genuine native Vec3d and BatteryREDD compiles the unchanged real
Vec3NT support source. Tests include negative/zero/positive battery span, the
original empty draw and the complete loop geometry. Sixty-four additional
channel-boundary streams cover truncation/clamping, negatives, excess values,
negative zero, NaN and infinities using the exact production conversion template.

Total: **136 paired streams, 424 batch comparisons, 12,496 vertices, 848 actual
driver draws**. Each vertex's XYZ float bits and complete RGBA word match the
original; mode, count and native 16-byte format are checked. Both actual upload
paths run on a real Mesa surfaceless EGL compatibility context. Post-draw current
color/normal, texture coordinates on both texture units, array enables, texture,
lighting, blend, cull, alpha, shade/depth state and GL errors are compared. Native
owner reset and matching RNG call counts are checked. Native Forge color-cache
invalidation is not a fabricated GL color restore.

This proof is explicitly little-endian Linux/Mesa only. No cross-platform or
cross-driver assertion about unspecified OpenGL current-array state is made.
The reused actual offscreen-context test and material-sprite helper are compiled
unchanged and hash-pinned. No in-game framebuffer, pixels, model/texture resource
load, full renderer lifecycle or full-project build is claimed. The coordinator
owns integrated compile and remaining renderer diagnostics.

## Explicit excluded furnace boundary

`RenderFurnaceSteel.java` is byte-identical to sealed CP87 and hash-pinned as an
unchanged support boundary. Its old batch needs color, brightness 240 and normal
+X, without any UV0 array. A direct native format built from POSITION_3F,
COLOR_4UB, TEX_2S (UV1), NORMAL_3B and PADDING_1B is rejected by the actual cached
engine at `VertexFormat.addElement(TEX_2S)` with
`IndexOutOfBoundsException: Index: 1, Size: 0` before upload. The retained runner
reproduces and verifies this exact failure. Native `VertexFormat.addElement`
uses `uvOffsetsById.add(element.getIndex(), offset)`, so UV1 cannot be the first
UV element. The checked bytecode excerpt is in `format-boundary.javap`.

No dummy UV0, shader-visible attribute, reflection/list mutation, persistent GL
lightmap override or discarded normal was used to force it into this family.
The furnace remains explicitly unimplemented here. Its raw old brightness word
240 means low 16 bits 240 and high 16 bits 0 on the tested little-endian engine.
Native SHORT `lightmap(first,second)` writes second then first; a future genuine
solution must preserve that word, signed-byte normals, and original absence of
UV0, not assume the API argument order. No mixed-furnace parity is claimed.

## Replay

From `ntm-port-1.12.2`:

    python3 porting/tests/machine-glow-batches/run.py --source-only
    python3 porting/tests/machine-glow-batches/run.py \
      --java-home /path/to/java8 --classpath "<cached native classpath>" \
      --legacy-client /path/to/official-1.7.10-client.jar

Build products are temporary. No download, install, cache change, full build,
main-worktree modification or push is part of the runner.
