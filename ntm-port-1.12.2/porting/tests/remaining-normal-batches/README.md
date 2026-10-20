# Closed native normal streams

Base: CP92 `0675e599e5ee31a60776b560af792372df191790`.

## Scope and compiler root

This is a frozen six-owner migration of real, already-present normal attributes.
It uses native singleton Tessellator/BufferBuilder streaming and the native
POSITION_TEX_NORMAL / POSITION_TEX_COLOR_NORMAL formats, plus POSITION_COLOR for
companion batches which originally have no normal or texture coordinates.

| Owner/scopes | Sites | Vertex expressions | CP92 stream diagnostics |
| --- | ---: | ---: | ---: |
| RenderExcavator.render | 2 | 32 | 43 |
| RenderChemical.renderGasFire, renderGasCloud, renderAmatBeam | 3 | 24 | 42 |
| RenderRadarScreen.render | 3 | 12 | 21 |
| SkyProviderCelestial.renderSmoke, renderGlow | 2 | 8 | 14 |
| SkyProviderThatmo.renderSmoke, renderGlow | 2 | 8 | 14 |
| ItemRenderAberrator.renderFirstPerson | 1 | 4 | 7 |
| Total | 13 | 88 | 141 |

There are ten nonescaping local acquisitions/scopes, seventeen normal setters
and thirteen color setters. `diagnostic-impact.json` pins the CP92 compiler-log
hash and all 141 targeted diagnostic locations/symbols. This is the pre-change
root impact, not a replacement for the parent's fresh full common compilation.
Other diagnostics in these owners, such as old RenderManager access and icon
lookups, are not claimed fixed.

Every selected batch already writes its own normal before its first vertex.
Normal setter arguments are preserved as typed float locals at their original
position, then submitted through BufferBuilder.normal for each original vertex.
They are the original axis constants, not generated face normals or a lighting
workaround. Geometry, UV expressions, color/alpha arithmetic and setter order
are unchanged. Packed colors keep masking and alpha clamping; float colors keep
float multiplication, Java conversion and clamping. The gas fire still evaluates
Color.getRGB followed by its original Math.max alpha expression exactly once.
No UV1/lightmap, extra UV0, default color or normal is invented. Native padding
is not an attribute and is not asserted equal to old unused padding.

## Source preservation

`manifest.json` is an exact full-file inverse to CP92. `check_source.py` exports
FILES/reverse_source for older layered guards, verifies each full original with
git show, replays the finite transform, then independently checks every changed
statement and every byte between those statements. All other methods and
imports, GL/matrix/texture calls, clock/RNG evaluations, loops, branches and
animation calculations remain unchanged. Both SHA and independent statement
checks reject 145 source mutations; twelve manifest-hash mutations also fail.

The default Git diff reports one whitespace warning on RenderChemical's
unchanged tab-only line between begin and its first color setter. The enclosing
large hunk removes/re-adds that original line. Full-file inverse and independent
surroundings checks prove those bytes are unchanged; no whitespace normalization
was performed merely to silence that diagnostic.

`normal-contract.javap` retains actual official legacy normal-setter conversion,
the actual native BufferBuilder encoder and Forge postDraw contract. The legacy
normal is stored as three signed bytes using float*127, Java float-to-int and
byte narrowing. Native BYTE encoding agrees for every selected axis input.
Both actual uploaders disable the normal array after drawing; neither conversion
invents a new current normal. Existing current GL color/normal semantics remain
important for the no-color/no-normal batches and are checked below.

## Finite native/driver gate

`run.py` validates exact support/engine hashes and compiles extracted complete
regions against installed Java 8/native 1.12.2. The other side reflects into the
actual official 1.7.10 client `bmh` singleton, setters, begin, vertex emission,
translation and draw. Its SHA-256 is
`a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce`.
The existing MaterialTextureAtlasSpriteNativeTest context is reused unchanged.
No owner-class stubs, universal compatibility shim or VBO path is introduced.

Boundary reads become typed scalar/array fixture inputs. Chemical calculations
execute original float/double age arithmetic, real java.awt.Color, native Vec3d
and actual seeded Random. RadarEntry's actual int posX/posZ/blipLevel contract is
hash-pinned; only those three field reads are projected to int-array entries in
the fixture, not to fabricated entities or tile implementations. The full radar
loop and linked/empty-entry/unlinked branches execute. Texture-manager lookups
at the boundary select real fixture texture objects while the production
identities and ordering remain protected by the full-file audit.

Six boundary environments times three translations (0, .125, -16777216.25)
times ten methods give 180 paired executions:

| Region | Paired batches | Vertices |
| --- | ---: | ---: |
| Excavator, both four-face streams/material branch | 36 | 576 |
| Chemical, all three methods | 54 | 432 |
| Radar linked/noise/entry branches | 27 | 240 |
| Both providers' smoke/glow | 72 | 288 |
| Aberrator full sixteen-quad loop | 288 | 1152 |
| Total | 477 | 2688 |

Per-format paired counts: POSITION_COLOR 30, POSITION_TEX_NORMAL 405,
POSITION_TEX_COLOR_NORMAL 42. Fixtures retain negative/zero boundary values,
including zero chemical max age and radar range -1, with the original resulting
IEEE arithmetic. Gas cloud consumes exactly two nextInt calls; unlinked radar
consumes one. Both sides start from identical RNG state.

The actual native and original buffers match all XYZ/UV float words, RGBA bytes,
three signed normal bytes, counts and modes. Normal padding is masked, not
fabricated. Both real uploaders execute: 954 nonempty engine submissions to
Mesa, with no GL errors and singleton counts reset. Post-draw and final state
checks compare current color/normal, UV0/UV1, vertex/color/normal/texture client
arrays, texture units/bindings/enables, blend/alpha/depth/cull/fog, shade model and
model/projection/texture matrices. Inputs vary lighting, blending, current
attributes and matrices rather than assuming white/default GL state.

Retained platform: little-endian Linux, `4.5 (Compatibility Profile) Mesa
25.0.7-2+deb13u1`. This is a bounded stream and driver-state gate, not full owner
linkage, startup, framebuffer equality or in-game visual validation.

## Reproduce offline

From the project root:

```sh
python3 porting/tests/remaining-normal-batches/run.py --source-only
python3 porting/tests/remaining-normal-batches/run.py \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath "$(cat /workspace/shared/nagaport-toolchain/native-classpath.txt)" \
  --legacy-client /tmp/nagaport-legacy-client.jar
```

The runner compiles only the retained fixture/support classes in a temporary
directory, extracts existing installed LWJGL natives there and disables Mesa
shader-cache writes. No downloads, dependency/cache changes or full common
build are performed by this evidence runner.

## Explicit exclusions

FurnaceSteel's UV1-only stream remains parked. FurnaceCombination and weapon
fireballs with combined normal/lightmap attributes are not folded into this
scope. SmallBlockPronter, RBMK, Pylon and other escaped/borrowed streams remain
excluded, as do armor and already-native bodies. The celestial renderer's
borrowed skybox/shader paths are untouched. There is no expansion into unrelated
render APIs or workaround for absent native formats.
