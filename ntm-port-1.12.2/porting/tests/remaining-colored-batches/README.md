# Remaining owned colored renderer streams

Base: sealed CP90 `0be133c9fb9f07053e26f081371487e641c95214`.

## Frozen production family

Nine owners, eleven complete local scopes, twelve active batch sites,
89 vertex expressions and 41 color-setter sites:

- RenderForceField and RenderMachineForceField: both sphere-grid methods.
- RenderPumpjack: complete animated linkage batch.
- RenderSpear and RenderBFA: complete local flash fan scopes.
- RenderFlare: complete local flash fan scope.
- RenderBlackHole: local jet fans only.
- RenderSiegeLaser: both dart triangle/quad batches.
- RenderCloudTom: complete textured cloud shell.

Each native Tessellator singleton owns its real BufferBuilder and the same draw
positions. Original line-strip (3), triangle (4), triangle-fan (6) and quad (7)
modes remain unchanged. All scopes use POSITION_COLOR except CloudTom, which
uses POSITION_TEX_COLOR. No normal or lightmap attribute is added, and no missing
UV is invented. Every active batch sets its color before its first vertex.
CloudTom has UV on every vertex, including its original infinite values from
1D / j at j == 0. Those values are not sanitized or replaced.

Each original color expression is evaluated once at its setter site. Reviewed
float inputs contain only arithmetic/local reads; each channel retains float
multiplication by 255.0F, Java float-to-int conversion, then clamp to [0,255].
Native integer color is used rather than assuming the native float overload
clamps. Packed RGB retains masked low-byte channels, ignoring a signed/high byte;
opaque alpha is 255. Explicit integer alpha is clamped exactly as in original
bmh. Existing old setter implementations were checked against actual retained
1.7.10 bytecode; color-contract.javap records their conversion and packing.

The finite transform carries the last explicit setter's resulting channel locals
to each vertex. It does not reevaluate the color inputs at vertices, substitute
GL current color, invent late-prefix attributes, or move animation/material
calculations. Selected locals cannot escape to borrowed helpers. Comments are
lexically excluded from rewriting: the apparent third forcefield line batch
and Maxwell beam batch remain comments, not newly activated rendering.

All geometry/UV arithmetic, numeric types, winding, complete loops, branching,
clock/RNG sites, Vec3d/Vec3NT calculations, GL/matrix/color/texture statements,
shader behavior and draw order remain unchanged. No production helper or
compatibility facade, VBO, new persistent state, replacement renderer,
resource change or normal/lightmap workaround was introduced.

## Exact source gate

manifest.json pins complete before/after file SHA-256s and exact inverse edits.
check_source.py exports FILES and reverse_source(relative, text) for composing
historical guards. All nine inverses were independently compared with sealed
CP90 Git objects. Replay requires no Git object. A finite method whitelist and
closed local-call grammar deterministically reproduce the full transformation.
The guard independently compares all untouched lines within complete scopes,
including comments and all surrounding GL/material/RNG/animation code, and
compares all original/native vertex arguments. Fifty-four endpoint, ownership,
format and draw mutations are rejected. Full-file hashes also pin every other
method and import, including explicitly excluded old rendering paths.

## Actual runtime gate

run.py compiles extracted complete-loop production regions against actual cached
Minecraft 1.12.2 / Forge 2768 / MCP stable_39 with Java 8. The paired original
regions execute the actual official 1.7.10 bmh through reflection. Both engines
use their actual singleton and actual streaming uploader; no replacement
Tessellator, mocked uploader, recorder-as-engine or owner-class stubs are used.
Native engine bytecode and unchanged support sources are hash-pinned. The old
client's SHA-256 is
`a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce`.
It is test input only, neither distributed nor a port dependency.

Six typed environments and three translations execute 198 paired production
streams: full forcefield latitude/longitude grids (including zero-loop inputs),
all Pumpjack inner/outer linkage loops, both complete 64-fan flash loops, Flare's
original age-dependent count and complete RNG progression, both jet fans, both
siege-dart batches and CloudTom's entire 320-vertex shell. Genuine native Vec3d
and unchanged real Vec3NT code execute the vector operations. RNG instrumentation
uses the real seeded java.util.Random algorithm and checks every float call.
Production totals: 8,844 batch comparisons, 44,976 vertices, 17,688 actual draws.

An additional 118 streams apply the same finite conversion transform to 64
float-channel boundaries and 54 signed-RGB/integer-alpha combinations. They
cover negative/overrange values, negative zero, truncation, NaN and infinities,
Integer.MIN_VALUE and Integer.MAX_VALUE. Overall: **316 paired streams, 8,962
batch comparisons, 45,448 vertices and 17,924 actual driver draws**.

Every vertex's XYZ/UV raw float words and full RGBA word match. The tests verify
original attribute activation, native exact format and 16/24-byte strides,
primitive modes, per-stream draw counts, both owner resets and exact RNG counts.
Both upload paths run on real Mesa surfaceless EGL. Post-draw and final region
states compare current color/normal, UV0/UV1, texture bindings/enables, client
arrays, lighting, blending, cull, alpha/fog/depth state, and model/projection/
texture matrices. GL errors must remain zero. Native Forge color-cache
invalidation is allowed to remain native; no fake glColor restore is added.
The comparison of unspecified current-array state is scoped to this tested
little-endian Linux/Mesa driver, not asserted for all platforms/drivers.

Typed fixtures replace only input scalars at the extraction boundary, including
entity age/player ticks/position values and the already-read laser color.
CloudTom's actual bindTexture lookup is outside the driver fixture boundary;
its full geometry loops and texture-matrix movement remain inside. Other model,
world, material lookup, RenderHelper lifecycle and caller setup remain unchanged
in source, not replaced by mocks. Two controlled test textures are created and
deleted in the context. No actual game material load, owner-class linkage,
world simulation, full client lifecycle, framebuffer/pixel parity or full build
is claimed. The coordinator owns integrated compilation and diagnostic counts.

## Explicit exclusions

FurnaceSteel remains byte-identical and hash-pinned; its UV1-only format issue
is not bypassed. BlackHole's borrowed/overridable disc/swirl/flare color helpers,
Torex's borrowed Cloudlet/Flash helpers, mixed lightmap/normal renderers and all
armor remain unchanged. This is a finite local family, not a universal legacy
Tessellator rewrite or a rendering-polish task.

## Replay

From ntm-port-1.12.2:

    python3 porting/tests/remaining-colored-batches/run.py --source-only
    python3 porting/tests/remaining-colored-batches/run.py \
      --java-home /path/to/java8 --classpath "<cached native classpath>" \
      --legacy-client /path/to/official-1.7.10-client.jar

Build products are temporary. No downloads, installation, full build, dependency
or cache changes, main-worktree edits or push occur in this runner.
