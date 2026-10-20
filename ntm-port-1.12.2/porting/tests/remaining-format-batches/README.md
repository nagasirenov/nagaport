# Remaining closed per-batch formats

Base: CP91 `f5ce86cd607379cc9810896cf91d1fee86ce6331`.

This finite conversion replaces legacy local singleton streams with the real
native `Tessellator.getInstance()` / `getBuffer()` / `begin()` / `endVertex()` /
`draw()` path. It adds no renderer shim or synthetic vertex attributes.

## Frozen production scope

| Owner and scopes | Static begin/draw sites | Vertex expressions | Formats |
| --- | ---: | ---: | --- |
| RenderNTMSkyboxImpact: initializeDisplayLists, renderStars, render | 9 | 49 | POSITION, POSITION_TEX, POSITION_COLOR |
| SkyProviderLaytheSunset: renderSunset | 3 | 6 | POSITION_COLOR |
| LoadingScreenRendererNT: setLoadingProgress | 2 | 12 | POSITION_TEX_COLOR, POSITION_COLOR |
| GaugeUtil: renderGauge, colorOuter drawSmoothGauge overload | 2 | 10 | POSITION_TEX, POSITION_COLOR |
| RenderBobble: renderOrigin | 1 | 24 | POSITION |
| RenderDeathBlast: doRender | 2 | 8 | POSITION |
| GUIMachineRadarNT: drawGuiContainerBackgroundLayer local map/sweep | 2 | 8 | POSITION_COLOR |

Totals: 7 owners, 10 closed method scopes, 11 local acquisitions, 21 sites,
117 static vertex expressions, 17 active color setters and one discarded
pre-begin color setter. Every batch selects its own actual attribute format.
Modes 4, 6 and 7 remain unchanged, including the death blast's two full eight-fan
loops and its original `rotateYaw(45)` evaluation. No native normals or lightmap
attributes are introduced.

Float color channels retain the old float multiplication, Java float-to-int
conversion and 0..255 clamp. Packed RGB retains masking and explicit alpha
clamping. Setter arguments are evaluated once, in original setter order.
Native per-vertex color uses these locals, not a later re-evaluation. Absent
color and UV remain absent, so current GL color/texture coordinates are retained
rather than replaced with white, zero UV, or another guessed value.

RenderBobble's pre-begin red setter did not color its vertices: old begin resets
hasColor. Actual old bytecode in `pre-begin-reset.javap` shows its arithmetic,
clamps, private field writes and ByteOrder.nativeOrder branch, with no external
rendering work, allocation or explicit exception path. It either returns when
color is disabled or changes only internal packed color/hasColor; begin clears
the flag before any vertex. Those retained packed bits are not uploaded without
the flag. Production retains the three typed primitive argument evaluations at
the same point and emits POSITION only. It does not call a native attribute
method before a valid begin or activate the discarded color.

The empty radar map is intentionally different at the *format declaration*
level: the new complete batch declares POSITION_COLOR, while the old batch never
sets its color flag when every byte is nonpositive. Both actual uploaders skip
empty draws without touching client arrays or current GL state. No default
color is invented to hide that difference.

## Source proof

`manifest.json` retains exact full-file before/after SHA-256 and inverse hunks.
`check_source.py` exports FILES/reverse_source for layered historical guards. It:

- reconstructs and compares all seven full originals against CP91 with git show;
- replays the bounded transform and verifies its complete metadata;
- independently checks the statement grammar, exact argument expressions,
  per-batch attributes, modes and setter lowering, including radar's same-line
  setter/vertex statements;
- reconstructs whole methods from the independent audit and checks every byte
  outside changed statements and the two necessary imports;
- rejects 142 source mutations through both the exact hash gate and the separate
  statement/surroundings audit, plus 14 manifest-hash mutations.

Thus matrix, texture/material lookup, GL color, animation/clock calls, branches,
loops, comments and outside methods remain byte-identical. This is not a Java
parser or a claim that the whole renderer is linked.

## Native and driver proof

`run.py` first performs the source guard, validates retained support/engine
hashes, then compiles exact extracted original/native regions against existing
Java 8 and native 1.12.2 classes. The old side loads official 1.7.10 client class
`bmh` by reflection, including its actual singleton, setters, begin, vertex
emission, draw and translation. The old client SHA-256 is
`a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce`.

The fixture uses the established MaterialTextureAtlasSpriteNativeTest offscreen
context unchanged, and initializes both actual engines' texture-unit state.
There are no fake Minecraft owner classes or copied engine implementations.
Real native MathHelper, Vec3d, Vec3NT, RenderHelper and GLAllocation execute where
used inside the selected regions. Owner/world/entity/texture-manager reads at
the extraction boundary become explicitly typed scalar/array fixture inputs;
texture lookups select two real test texture objects. The two wall-clock reads
in the impact satellite are separate fixed fixture values. Full-file proof,
not this fixture, protects the surrounding world lookups and resource identities.

Coverage is six boundary environments times three translations (0, .125 and
-16777216.25) times nine extracted executions, giving 162 paired executions.
The display-list execution includes renderStars at its original call position,
so all ten production scopes are exercised. Independent coverage arithmetic:

| Extracted execution | Paired batches | Vertices |
| --- | ---: | ---: |
| Actual impact display-list compilation | 3078 | 80496 |
| Impact sun/moon/digamma/satellite/horizon | 99 | 540 |
| Three full Laythe sunset fans | 54 | 972 |
| Loading background and optional progress | 33 | 192 |
| Gauge texture strip | 18 | 72 |
| Gauge needle | 18 | 108 |
| Bobble origin | 18 | 432 |
| Two death-blast loops | 288 | 1152 |
| Radar map and sweep | 33 | 718308 |
| Total | 3639 | 802272 |

Per-format paired counts are POSITION 3402, POSITION_TEX 90, POSITION_COLOR
129 and POSITION_TEX_COLOR 18. Radar includes hidden, all-zero, all-negative,
sparse (41 positive bytes), dense (40000 positive bytes), and mixed signed byte
maps (19812 positive bytes). Geometry/color exceptional inputs include zero gauge
count and NaN progress/alpha without changing the original formulas.

For each batch the oracle compares actual raw float position/UV words and RGBA
bytes, vertex count, draw mode, attributes and singleton reset. Both real engines
submit to Mesa: 7266 nonempty engine draw submissions, including display-list
compilation, and 12 empty submissions. Empty submissions additionally assert
identical pre/post driver state and absent old flags. Current GL color/normal,
UV0/UV1, client arrays, texture units/bindings/enables, blend/alpha/depth/cull/fog,
shade model and model/projection/texture matrices are compared outside list
compilation. During GL_COMPILE only nonmutating queries are used, so state
instrumentation cannot insert texture-unit switches into the list.

Display-list verification compiles the actual three-list layout with the real
GLAllocation allocator and actual engine submissions. The complete seeded star
loop performs 6000 float and 780 double RNG reads, emitting 3120 vertices. Upper
sky retains 169 separate quad draws; lower sky retains one 676-vertex draw.
Each original/native compiled list passes glIsList, executes glCallList from
identical reset state, matches full post-replay state and has no GL errors:
108 actual list replays. Lists and fixture textures are then deleted.

The retained run used `4.5 (Compatibility Profile) Mesa 25.0.7-2+deb13u1` on
little-endian Linux. This gate proves finite source preservation, actual buffer
behavior and driver execution/state parity. It does not claim full renderer
linkage, game startup, framebuffer equality or in-game visual correctness.

## Running without downloads

From the project root:

```sh
python3 porting/tests/remaining-format-batches/run.py --source-only
python3 porting/tests/remaining-format-batches/run.py \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath "$(cat /workspace/shared/nagaport-toolchain/native-classpath.txt)" \
  --legacy-client /tmp/nagaport-legacy-client.jar
```

The runner uses an isolated temporary directory for javac, extracted installed
LWJGL natives and execution. It disables Mesa shader-cache writes. It performs
no dependency resolution, downloads or full common build.

## Explicit exclusions

No armor, borrowed stream, mixed vertex-arity stream, UV1-only stream or normal
stream is expanded into this family. In particular RenderFurnaceSteel remains
unchanged and parked: the real native VertexFormat rejects its UV1-only shape
without UV0, and inventing UV0 is not an acceptable fix. OrreryPronter's escaped
singleton/RenderUtil call path and SkyProviderCelestial's borrowed skybox path
remain excluded. Existing already-native Radar helper and other migrated bodies
are untouched. Full common compilation remains a separate parent checkpoint.
