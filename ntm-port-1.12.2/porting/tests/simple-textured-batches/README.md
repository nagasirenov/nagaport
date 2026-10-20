# Native POSITION_TEX batch family

Base: `ea933e28fe3a1a9a60e9d438411a02182b2ab22a` (sealed CP70).

## Production scope

15 source files, 16 local singleton acquisitions, 22 complete quad batch sites,
108 explicit position/UV vertex expressions. The manifest is the complete file
list. Each local tessellator is non-escaping and has only start-quads, UV vertex,
and draw uses; no helper-owned or borrowed batches are migrated.

Each acquisition now obtains the actual native Tessellator singleton and its
BufferBuilder. Each batch uses `begin(7, DefaultVertexFormats.POSITION_TEX)`;
each vertex writes `pos(...).tex(...).endVertex()`. Draw remains on the same
Tessellator owner, in the same place. No new production abstraction is added.
Existing mesh, particle, bullet and item helpers were inspected; none is needed
for direct, uncolored position/UV batch emissions.

The migrated families are armor overlays (six), GUI batches (four classes),
fog, meteor, danger diamond, sky chainloader and satellite. More complex files
with multiple tessellator variables, helper escapes or other attributes were
excluded rather than partly inferred. This is not a universal tessellator
facade or a general automatic rewrite of legacy batches.

All vertex argument spellings and evaluation expressions are retained (spacing
at the UV split is normalized). UV arguments at these sites are side-effect-free
arithmetic/local reads, so the split builder calls do not introduce observable
interleaving. GL calls, texture binding, current GL color, shader/lighting state,
loops, branching, matrices, dimensions, vertex order, quad mode, draw order and
surrounding calculations are byte-for-byte unchanged after reversing the
mechanical edits. No color array is added: these old batches never enabled one.
Thus no color integer clamping or float normalization change is involved.
Likewise there is no normal, lightmap, missing attribute, late UV activation or
stale-slot policy decision. Every vertex supplies UV, starting with vertex zero.

## Evidence and boundaries

`manifest.json` pins full before/after file SHA-256 values and position-specific
line edits. `check_source.py` exports `FILES` and
`reverse_source(relative_path, text)` for composition with prior historical
source guards. Unrelated files pass through unchanged. No Git object is required
for reproduction. It also checks the restricted local use grammar, batch
boundaries/ownership/counts, position/UV argument equivalence and a deterministic
forward transform. 75 deliberate mutations are rejected. Full reconstructed
sources were independently checked against the sealed CP70 Git object.

`run.py` compiles 22 extracted native production batch bodies against actual
Minecraft 1.12.2 / Forge 14.23.5.2768 / MCP stable_39 APIs with Java 8. For each it
also executes the corresponding unchanged baseline emission expressions using
the actual official 1.7.10 `bmh` Tessellator bytecode, via a test-only reflection
endpoint adapter (not a replacement implementation). The old client jar's
SHA-256 is pinned to
`a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce`.
It is inspection/test input only, not a runtime dependency or distributed asset.

Four controlled batch-boundary input environments and three translations yield
264 comparisons and 1296 vertices. Tests compare all five raw IEEE-754 float
words per vertex (XYZUV), old enabled attributes, native format/20-byte stride,
vertex count, primitive mode and finished byte length. Fixtures retain the
actual scalar types. Resolution is a bounded width/height fixture, not a fake
Minecraft client. Both sides are captured before driver submission; the old
private constructor is reflected to isolate each test buffer without a GL draw.

These are extracted emission tests, not compilation of the full owning renderer,
GUI or armor classes. They do not test calling-class linkage, actual GL upload,
texture availability, camera/world state, gameplay or in-game pixels. Source
reversal proves unchanged surrounding code, not its pre-existing correctness.
The coordinator owns full-project compilation and residual-error accounting.

## Portable reproduction

From the `ntm-port-1.12.2` project root:

    python3 porting/tests/simple-textured-batches/run.py --source-only
    python3 porting/tests/simple-textured-batches/run.py \
      --java-home /path/to/java8 \
      --classpath "<resolved native project classpath>" \
      --legacy-client /path/to/official-1.7.10-client.jar

No downloads, install steps, external stubs, source exclusions or repository
absolute paths are used by the tests. Java executable suffix and classpath
separator follow the host OS; native Windows execution has not been run.
`proof.log` contains the successful local result.
