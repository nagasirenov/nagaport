# Native World tile lookup: finite CP52 migration

Baseline e0d4369. Exactly **579 invocations in 301 production files** change from
`receiver.getTileEntity(x, y, z)` to
`receiver.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z))`.
Receivers and each coordinate expression are retained byte-for-byte. No helper,
lookup cache, formula, tile data, or control flow is replaced. All unrelated bytes,
including CRLF, round-trip to the pinned before-file hashes. No imports needed.

## Compiler scope, rather than receiver spelling

`TileLookupScanner.java` uses Java 8 javac attribution against the actual target
classpath. It accepts only variable/field receivers with exact declared native
`net.minecraft.world.World` type and three primitive-int coordinate expressions.
Error-typed expression descendants are rejected even when an arithmetic parent
recovers as int. The native World member set must expose exactly one getTileEntity
method, taking BlockPos. Post-migration attribution independently confirms the
selected method belongs to World and the constructor is BlockPos(int,int,int).
Every selected invocation appears in both compiler scans. Source type spelling
alone, an imported short name, or a matching variable name is not proof.

579 accepted sites: 438 `world`, 104 `this.world`, 24 `p_149749_1_`, 10
`p_149695_1_`, and one each `worldIn`, `worldObj`, `transporterWorld`.
No complex receiver expressions are selected. Runtime virtual dispatch remains
intact; the static receiver bound does not guarantee arbitrary third-party World
subclasses preserve vanilla behavior.

Negative attribution controls reject IBlockAccess, nonplatform types, subclass
static types/custom overloads, boxed/double arguments, unresolved arithmetic,
complex receivers, already-native calls, comments and literals. Two intended
positive controls pass. All source files participate in attribution; this is NOT
a successful compilation of the port. Original/target analysis records 32469 /
31890 expected errors in other unfinished APIs, exactly 579 fewer. No full Gradle
build was run by this worker and no source-set exclusions were introduced.

## Original and native semantics

`method-bytecode-evidence.txt` contains pinned actual javap method excerpts.
Original official client SHA256:
`a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce`.
Original joined.srg maps ahb=World, ahr=ChunkCache, apx=Chunk, aor=TileEntity.
Target Forge 1.12.2-14.23.5.2768 stable_39 project JAR SHA256:
`8c75037188f87bc62939eefa03c2480551509034f069728de012c09276f8f49e`.
No dependency JARs are distributed here.

- Both World implementations reject y<0 or y>=256 and do not reject horizontal
  world-border coordinates in this method. Neither adds a loaded-chunk guard.
- Both prioritize valid pending tiles while processing, otherwise ask the chunk,
  then fall back to a valid matching pending tile. Invalid pending tiles are skipped.
- Original uses x>>4/z>>4 and x&15/z&15. Native preserves arithmetic-shift chunk
  selection and exact int BlockPos coordinates. No floor(double), clamp, mutable
  position, or simplification to this.pos is introduced.
- Original chunk lookup creates a missing tile for a tile-bearing block. Native
  World invokes Chunk with IMMEDIATE, retaining creation and provider effects.
- Original World explicitly tolerates a chunk provider returning null. Native
  World dereferences its provided chunk. This is an **unsupported engine/provider
  compatibility difference**, not fixed here and not evidence of gameplay parity
  for contract-breaking custom providers. No user approval of a mechanics change
  is inferred. A new guard would itself change native/provider effects.
- **All IBlockAccess calls remain unported in this batch.** In particular, original
  ChunkCache directly indexes its chunk array and calls the creating Chunk lookup;
  native ChunkCache bounds-checks and defaults to CHECK, not IMMEDIATE. Blindly
  wrapping its coordinates would silently change creation and out-of-cache behavior.
- Java evaluates the receiver, then x/y/z left-to-right once each, then invokes
  the method. A null receiver still evaluates the coordinate expressions first;
  an argument exception prevents later arguments and dispatch. New BlockPos adds
  allocation before coordinate evaluation; no OutOfMemoryError-equivalence claim
  is made. Integer overflow and exceptions are not normalized.

## Runtime and production-method evidence

`probe.py` runs 98 real native tests: native WorldServer/WorldClient providers and
Chunk maps; negative and positive chunk boundaries; y=-1/0/64/255/256; horizontal
border and signed-int extrema; exact tile identity; unloadQueued effects; pending
precedence/fallback/invalid skipping; missing tile; native chest immediate creation;
client absent chunk fallback; excluded ChunkCache CHECK/out-of-bounds behavior;
virtual dispatch and null/argument-exception evaluation order.

Unsafe only bypasses unrelated server/client startup constructors and populates
native storage. Native lookup methods and actual Chunk implementations are not
replaced. ObservedWorld overrides lookup ONLY for expression-order checks. No real
server startup, disk chunk generation, network transport, fullmod gameplay or save
compatibility claim is made.

Seven additional tests compile and execute the **complete extracted method bodies**
from TileEntityRBMKActiveBase.isUseableByPlayer and
BlockStorageCrate.getComparatorInputOverride, hosted on a native TileEntity test
shell. They check identity rejection, the unchanged 128 squared-distance boundary,
and native empty/full inventory comparator behavior. Whole RBMK/crate classes are
NOT compiled by this probe. javap confirms two int constructors and two native
World calls in those extracted methods.

## Exact guards and composition

`audit.py --strict` verifies all before/after whole-file hashes and all 579 exact
invocation inverses, with 579 deliberate fragment-drift negative controls. Default
fragment mode permits unrelated separately audited edits but pins the exact full
invocations and their occurrence counts/ordinals within each file. `restore(rel,
text)` is an inverse bridge for older source guards; it never changes other bytes.
The original manifest stays pinned; do not regenerate historical baselines.

One explicitly approved composition is recorded inside manifest.json: the three
stack.stackTagCompound reads in ItemDesignatorArtyRange's lookup arguments may
already be stack.getTagCompound(). This alternative exact before/after invocation
is supported by the three compiler-attributed NBT read proofs and the SHA256 of
the NBT worker's immutable manifest. No generic replacement, arbitrary argument
rewrite, or acceptance of other live differences occurs. No entity-world overlap
was found in these 579 original selected calls. Any later math/count rewrite
inside another selected invocation must receive its own explicit composition.

The standalone strict view uses CP52 original NBT spelling; an integrated fragment
view keeps the independently migrated NBT getters intact. A whole-file strict check
is intentionally not valid after unrelated integration. Non-strict --attribute
verifies all selected calls but permits newly resolved candidates outside this
finite batch; strict --attribute also pins the complete original candidate set.

`merge.py --target /path/to/ntm-port-1.12.2` preflights all exact source fragments
and evidence files without writing. Add --apply to perform only those fragment
edits and copy this new evidence directory. It does not overwrite production files
from the worker snapshot. It rejects conflicts before any write. On current main,
the dry run passed with the explicit single NBT composition. This worker made no
main-tree writes.

## Reproduce without Gradle or downloads

From the project root:

```
python3 porting/tests/native-tile-lookup/audit.py --strict
python3 porting/tests/native-tile-lookup/probe.py \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)" --strict --attribute
```

For an integrated tree omit --strict. The optional --attribute pass analyzes all
production Java twice in temporary storage, reversing only this finite migration
for its original-side scan. Native tests and controls run without --attribute.
Paths/classpath separators and Java executables are configurable; actual Windows
execution is untested. Existing historical field/tick manifests are not modified.
