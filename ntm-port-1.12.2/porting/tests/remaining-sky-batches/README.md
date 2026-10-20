# Remaining celestial stream ownership closure

Base: CP93 `d2f171750be2f0d156a019f25fa781db39db308a`.

## Finite production graph

Only SkyProviderCelestial changes. The selected graph is render, renderSunset,
renderStars, renderSun, renderSwarm and private renderSkyboxSide. It contains
13 static begin/draw sites, 54 vertex expressions, three color setters and five
local singleton acquisitions. The exact CP93 compiler log has 75 diagnostics on
these legacy stream operations; `diagnostic-impact.json` pins those locations
and the log hash. This is targeted root impact, not a post-change full-build
result. Fourteen other owner diagnostics, including private lightmap access,
camera APIs and RenderManager lookup, are deliberately not claimed fixed.

The seemingly borrowed skybox path has a closed ownership contract:

- renderStars obtains the singleton and passes it to renderSkyboxSide exactly
  six times, in side order 4, 1, 0, 5, 2, 3.
- renderSkyboxSide is private. These are its only in-source calls.
- The caller has no active begin/draw stream. Each helper invocation owns one
  complete begin, four textured vertices, draw sequence.
- The helper keeps its existing Tessellator parameter and computes the same UV
  atlas coordinates. It obtains that owner's native BufferBuilder locally. No
  caller-open batch or inherited vertex attribute crosses the method boundary.

The other selected locals do not escape. renderSun calls renderSwarm only after
its local batches are complete; the nested method retains its own singleton
acquisition and three separately owned POINTS batches. No shared compatibility
object, new persistent state or generalized renderer facade is introduced.

Native formats are per-batch POSITION, POSITION_TEX and POSITION_COLOR. POINTS,
TRIANGLE_FAN and QUADS remain their original modes. Absent UV, color, normal and
lightmap remain absent, including shaders that read current GL color/texture
coordinates. No normal, default UV, default color or lightmap is fabricated.
Original geometry, UVs, packed/float color arithmetic, RNG evaluation order,
shader/uniform calls, matrices, texture/resource lookups and clock evaluations
remain unchanged outside the reviewed native statements.

## Source and inverse proof

`manifest.json` retains exact full-file before/after hashes and inverse hunks.
`check_source.py` reconstructs the whole CP93 file and compares it with git show,
replays the bounded transform, independently audits every changed statement and
all intervening bytes, and verifies the six private helper calls and signature.
It rejects 76 source mutations through both the hash and independent audit and
two manifest-hash mutations. Previously migrated methods are left byte-identical.
FILES/reverse_source are exported for layered historical source guards.

The worktree is non-cone and contains only this source owner, the small native
context/support files, shader source/assets and this evidence directory. No broad
porting history or build tree was copied into it.

## Actual native/legacy streams and GLSL driver gate

`run.py` checks engine/support hashes, compiles exact extracted closed regions
against installed Java 8/native 1.12.2, and reflects into official 1.7.10 `bmh`
for the other side's actual singleton, setters, begin, vertices, draw and
translation. The old-client SHA-256 is
`a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce`.
The established MaterialTextureAtlasSpriteNativeTest offscreen context is reused
unchanged. No fake Minecraft engine or owner classes are supplied.

World/entity/material inputs at extraction boundaries become explicitly typed
scalars, arrays or a real native Vec3d. Texture-manager lookups select real test
texture objects. Unchanged pre-existing sky display-list calls use a valid empty
fixture list at that boundary; this gate does not re-prove that earlier list's
geometry. All six private skybox calls execute the exact helper UV/vertex body.
The complete sunset fan and 1024-point dust loop execute, including real seeded
Random.nextFloat/nextGaussian and the original applyAnaglyph helper.

The original hash-pinned default.vert/blackhole.frag and swarm.vert/swarm.frag
assets actually compile, link and run on Mesa. For the loaded shader boundary,
production Shader.java's inspected GL dispatch is projected to the corresponding
GL20 operations; the exact methods are retained in loaded-shader-contract.txt.
This is not compilation of the full Shader resource-loader/logging/fallback
class. The source audit protects its original call sites and expressions.
Program use/restore and every uniform operation are recorded and compared in
order. Fixtures vary the previously bound program among zero and both live
programs. Current program and both programs' iTime values are checked with the
rest of post-draw state.

The actual swarm asset has no iOffset uniform. The driver returns location -1;
the original per-point setter calls are retained and executed as legal no-ops,
not removed or reinterpreted. Per-execution uniform counts are asserted: two for
the destroyed-sun branch, and one plus nonnegative swarmCount for a swarm call.
The three separate swarm batches and their original uniform/vertex ordering are
preserved, including empty batches and the shader's gl_VertexID behavior.

Six environments times three translations (0, .125, -16777216.25) times five
outer executions give 90 paired executions. Helpers execute within their actual
caller ordering, covering all six production scopes:

| Region, including nested helpers | Paired batches | Vertices |
| --- | ---: | ---: |
| Horizon and planetary overlay | 24 | 192 |
| Sunset fan and optional charged dust | 24 | 9486 |
| Six skybox sides when stars are visible | 72 | 288 |
| Destroyed/ordinary sun, including nested swarm | 90 | 249 |
| Standalone swarm | 54 | 42 |
| Total | 264 | 10257 |

Per-format paired counts are POSITION 111, POSITION_TEX 132 and POSITION_COLOR
21. Visibility/null-color/pressure boundaries and swarm counts 0, 1, 2, 3, 8 and
-1 are exercised. Each charged-dust execution consumes four nextFloat and 3072
nextGaussian calls, preserving all three argument evaluations for every point.
Zero altitude/division and NaN input boundaries retain original IEEE results.

Actual buffers match raw XYZ/UV float words, RGBA bytes, counts, modes and absent
attributes. Both real engines execute 438 nonempty submissions and 90 empty
submissions. Empty submissions assert identical pre/post driver state and no
client-array activation; every owner count resets and every GL error check is
clear. State comparisons cover current color/normal, UV0/UV1, client arrays,
texture units/bindings/enables, RGB and alpha blend factors, alpha/depth/cull/fog,
shade model, model/projection/texture matrices and the shader state above.

Retained platform: little-endian Linux, `4.5 (Compatibility Profile) Mesa
25.0.7-2+deb13u1`. This is finite source, actual buffer and driver-state evidence.
It is not full owner/Shader linkage, game startup, framebuffer equality or a
claim of in-game sky correctness.

## Reproduce offline

From the project root:

```sh
python3 porting/tests/remaining-sky-batches/run.py --source-only
python3 porting/tests/remaining-sky-batches/run.py \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath "$(cat /workspace/shared/nagaport-toolchain/native-classpath.txt)" \
  --legacy-client /tmp/nagaport-legacy-client.jar
```

Only fixture/support classes compile in a temporary directory. Installed LWJGL
natives are extracted there; Mesa shader-cache writes are disabled. No network,
dependency/cache update, full common build or push is performed by the runner.

UV1-only FurnaceSteel, other mixed-lightmap streams, armor, foreign/escaped
stream graphs and unresolved private-field access remain explicitly excluded.
