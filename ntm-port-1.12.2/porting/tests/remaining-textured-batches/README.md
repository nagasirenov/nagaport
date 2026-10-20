# Remaining owned POSITION_TEX batches

Base: sealed CP89 `42dea8e6e3241d0c4ce4ba9c5459fab2bdfdc99b`.

## Frozen production scope

Seven owners, ten complete local scopes, 29 quad sites, 116 explicit XYZUV
expressions and ten singleton acquisitions:

- RenderCrucible.render: one molten surface.
- RenderStrandCaster.render: one molten surface.
- GUIScreenGuide.drawImage: one image quad.
- GUIMachineRadarNT.drawTexturedModalRectDouble: one image quad.
- SmallBlockPronter.renderSmolBlockAt: all six independent face draws.
- SkyProviderCelestial.renderCelestials/renderRings/renderDigamma/renderStation:
  thirteen body/ring/effect sites plus one/two/one sites respectively.
- SkyProviderThatmo.renderSpecialEffects: both local shockwave/flash draws.

The actual native Tessellator.getInstance()/getBuffer() owns each stream.
Each begin uses quad mode 7 and exactly POSITION_TEX; each vertex writes
pos(...).tex(...).endVertex(). Draw remains on its original owner and in its
original position. There is no production adapter, helper shim, extra attribute,
VBO, guessed UV, discarded visual, resource change or unrelated cleanup.

Selected locals never escape to another method or caller-owned batch. Every
vertex supplies UV starting at vertex zero. UV arguments are reviewed pure
arithmetic/local reads, so splitting the five-argument call does not interleave
an observable effect or possible arithmetic exception. All argument spellings
apart from whitespace, numeric types, vertex order/winding, complete loops,
branch conditions, texture bindings, shader calls, material/color/alpha values,
GL states/matrices, RNG calls, clock calls and animation formulas are unchanged.
The two destroyed-body qsize expressions still call random.nextDouble exactly
once at their original positions; no material or time computation was moved.

## Discarded pre-begin attributes

The crucible's normal, strand caster's normal/color, and guide image's
brightness/color setters occur BEFORE startDrawingQuads. They were not active
attributes of those original draws. Actual official 1.7.10 bmh bytecode is
retained in pre-begin-reset.javap: begin clears normal/color/UV/brightness and
no-color flags. Brightness and normal setters perform primitive field writes;
float color setters perform multiplication, Java casts, clamping,
ByteOrder.nativeOrder(), bit packing and field writes (or return when color is
disabled). None calls GL, allocates vertex storage or has an input-dependent
throw path. They do not change current GL color, current normal or lightmap.

Their primitive argument expressions remain in typed local declarations at
the original statements, in original order. No native attribute writer is
called before begin and no discarded attribute becomes active. Existing GL
color and lightmap calls remain where they were. In particular, the strand
caster retains its GL material tint, and the guide does not silently gain a
white color array or full-bright lightmap array. The runtime oracle invokes the
actual old setters and verifies all three flags remain absent after begin.
This is valid owned-batch behavior, not a guarantee for an already-malformed
nested Tessellator draw or external reflection into its private current fields.

## Evidence

check_source.py exports FILES and reverse_source(relative, text) for composition
with earlier source guards. manifest.json pins complete before/after SHA-256s
and exact inverse edits. All seven reconstructed full files independently equal
the sealed CP89 Git objects. Replay needs no Git object. The finite whitelist
transform is replayed deterministically; 49 mutations to endpoints, ownership,
mode and format are rejected. An independent surrounding-source comparison
pins every unchanged line in the complete selected scopes, including all GL,
material, clock and RNG evaluation order. Nonselected methods and imports are
covered by the complete-file hashes and exact finite transform.

run.py compiles all 29 extracted production batch bodies against actual cached
Minecraft 1.12.2 / Forge 14.23.5.2768 / MCP stable_39 APIs with Java 8. Paired
original bodies execute unchanged official 1.7.10 bmh methods through a test-only
reflection endpoint. Both use their actual singleton and real streaming draw
path, not a recorder replacement, copied Tessellator implementation or mocked
uploader. The original client SHA-256 is
`a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce`.
The old jar is test input only, not a distributed artifact or port dependency.
Native engine classes and unchanged context/helper source are hash-pinned.

Six typed batch-boundary environments and three translations yield **522 paired
streams, 522 batch comparisons, 2,088 vertices and 1,044 actual driver draws**.
Every XYZUV raw float word, primitive mode, vertex count, 20-byte native format,
old attribute absence and owner reset matches. The two actual qsize RNG call
sites retain matching counted calls and seeded values. Boundary values replace
only already-computed scalar inputs, not world, celestial, texture or renderer
implementations. The pre-begin typed declarations are extracted from production;
the old setter calls are extracted from the exact reconstructed sources.

Both actual upload paths run on a real Mesa surfaceless EGL compatibility
context. Controlled nonwhite/partial-alpha current colors, normals and texture
coordinates on both units vary independently of geometry. Tests compare
post-draw current color/normal, UV0/UV1, texture bindings and enables, client
arrays, lighting, blending, cull, alpha, fog, depth state and model/projection
matrices, and require no GL errors. Actual OpenGlHelper.initializeTextures is
called to initialize native texture-unit constants, as in the real client.
Two test textures are allocated and deleted within the context. These are
controlled driver-state fixtures, not actual game materials or framebuffer
acceptance. In particular, unspecified current-array behavior is only compared
on this little-endian Linux/Mesa driver, not asserted cross-platform.

No full owner-class linkage, game world, full sky rendering, resource loading,
framebuffer/pixel parity or full-project build is claimed. Source equality
proves unchanged surrounding behavior, not its pre-existing correctness. The
coordinator owns integrated compilation and remaining diagnostic accounting.

## Exclusions and replay

Mixed-color/normal/lightmap methods, borrowed singleton batches, and all other
methods remain unchanged. This includes SmallBlockPronter's caller-owned normal
path; RadarNT's colored sweeps; celestial horizon/sunset/sun/swarm/borrowed skybox
and normal-bearing smoke/glow; Thatmo's smoke/glow; and all armor owners.
RenderFurnaceSteel remains byte-identical to CP89 and hash-pinned. Its previously
proven UV1-only native format rejection is not bypassed with a dummy UV0,
normal/lightmap workaround, reflection or state override.

From ntm-port-1.12.2:

    python3 porting/tests/remaining-textured-batches/run.py --source-only
    python3 porting/tests/remaining-textured-batches/run.py \
      --java-home /path/to/java8 --classpath "<cached native classpath>" \
      --legacy-client /path/to/official-1.7.10-client.jar

Build products are temporary. The runner performs no downloads, installation,
full build, cache changes, main-worktree modification or push.
