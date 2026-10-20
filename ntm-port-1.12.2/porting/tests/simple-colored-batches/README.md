# Native constant-per-batch POSITION_COLOR family

Base: `c878491b82ae4b5c50051b6806a143f856435027` (sealed POSITION_TEX evidence
on CP70). This layer does not edit any first-family production/evidence file.

## Scope and behavior

10 files, 14 complete batch sites, 62 static vertex expressions. Each local
Tessellator has exactly one explicit color activation after begin and before its
first vertex, held constant throughout that batch. Colors may differ between
calls/outer loop iterations; this is constant **per batch**, not necessarily a
compile-time literal. No mid-batch color changes, mixed UV/normal/brightness,
late activation, stale attributes, helper escapes or borrowed batches qualify.

The native singleton owns the same draw calls. Its actual BufferBuilder uses
POSITION_COLOR, the original primitive mode, positions/order and explicit color
on every endVertex. The RBMK batch retains its complete inner quad loop, with
one color assignment outside that loop. No GL calls, texture/lighting choices,
matrices, control flow or geometric expressions are altered. There is no new
production helper, broad compatibility facade or renderer failure fallback.

Packed RGB expressions are evaluated once at the former setter location and
split into masked R/G/B bytes; alpha remains 255, regardless of the source int's
high byte or sign. Float channels use the original Java semantics: multiply by
255.0F, cast to int (truncate toward zero, Java NaN/infinity conversion), then
clamp the int to [0,255]. Native integer color receives those channels; native
float color is deliberately not relied upon to clamp. The chosen float
expressions have no side effects. The one packed getter is evaluated once,
not once per vertex. No GL current color is substituted for the vertex array.

## Evidence

The manifest pins complete before/after file hashes and reversible line edits.
`check_source.py` exports `FILES` and `reverse_source(relative_path, text)` for
composition before earlier source guards. It needs no baseline Git object at
replay, passes unrelated source through unchanged, checks restricted local uses,
start/color/vertex/draw ownership, original argument spellings, the single
reviewed inner loop and deterministic forward conversion. 70 mutations are
rejected. All ten reversals were independently compared with the sealed base
Git object; all surrounding source, including GL commands, is unchanged.

`run.py` compiles the 14 extracted complete batch bodies against actual native
1.12.2 / Forge 2768 / MCP stable_39 and Java 8. Corresponding old bodies invoke
the official 1.7.10 `bmh` implementation through reflection. The old jar hash is
pinned to `a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce`.
It is test input only, not distributed or a new production dependency.

Four typed boundary environments and three translations cover 168 production
batch executions, including the complete RBMK inner loop. Additional tests
apply the exact reviewed production float conversion transform to 64 controlled
channel-boundary batches: negative/over-one inputs, fractional truncation,
negative zero, NaN and infinities. Packed tests cover 0, 0x123456, 0xFF804020 and
-1. Overall: 232 batch comparisons, 1144 vertices, 464 actual draws.

Before upload, tests compare XYZ float bits and the complete RGBA packed word of
every vertex, old attribute flags, native 16-byte format stride and primitive
mode/count. They then execute the actual old draw and actual native Tessellator
draw through a real Mesa surfaceless EGL context, comparing post-draw GL current
color, vertex/color array enable state, texture/blend/lighting/cull state,
shade model and depth mask, and checking native owner reset and GL errors.

Native Forge postDraw additionally invalidates GlStateManager's cached color;
its resetColor implementation does not issue glColor or force white. This is
native cache bookkeeping, not a fabricated GL color restoration. Actual GL
current-color outcomes match the original on the tested driver. No cross-driver
promise about unspecified OpenGL current-array state is made. No production
state restoration commands were added to hide a difference.

The existing offscreen-context test and material-sprite class are compiled
unchanged as support. Their hashes and the reused POSITION_TEX argument parser
are pinned by `support-hashes.json`. These are extracted batch tests, not full
owning GUI/renderer/class linkage tests, framebuffer/visual comparisons or an
in-game smoke test. Environment fixtures replace only values at batch entry;
world/entity mechanics and textures are not simulated. The coordinator owns
full-project compile and residual diagnostics.

## Reproduction

From `ntm-port-1.12.2`:

    python3 porting/tests/simple-colored-batches/run.py --source-only
    python3 porting/tests/simple-colored-batches/run.py \
      --java-home /path/to/java8 \
      --classpath "<resolved native project classpath>" \
      --legacy-client /path/to/official-1.7.10-client.jar

Runner paths are repository-relative or supplied as arguments, with temporary
build output. No downloads/installations or source exclusions. Actual draw tests
require the existing Linux Mesa EGL/JNA/LWJGL environment; only that platform was
run. The source-only guard has no native driver dependency. `proof.log` records
the successful result.
