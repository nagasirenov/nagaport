# Native layer-state family

Exact production/evidence base: CP73 a4739afa41966b75e2012bccd7df919e51d5c3a0.
One complete production owner: `com.hbm.blocks.generic.BlockLayering`.
The original/current census has no subclasses and exactly four registrations:
foam_layer (SNOW), sand_boron_layer (SAND), leaves_layer (LEAVES), oil_spill (GROUND).
No changes to the registrations, unrelated owners, or external dependencies.

## Preserved owner behavior

- A 16-state property and native codec preserve every stored metadata nibble,
  including high-bit identities. Bounds and quantity use `(meta & 7)`, but the
  non-exempt replacement predicate compares unmasked `meta >= 7`.
- Selection height is `(1 + (meta & 7))/8`; collision height is `(meta & 7)/8`.
  Metadata zero intentionally has a non-null zero-height collision box.
  Native collision boxes are local coordinates and the real native state/Block
  dispatch offsets them into world coordinates.
- No normal drops. The native sentinel is Items.AIR, not old null: native
  Block.getDrops checks Items.AIR and would otherwise append empty ItemStacks.
  Quantity remains one through eight and ignores fortune as in the original.
- Foam, oil and leaves always replaceable. Sand retains its material predicate.
- Ice and packed ice never support layers. Leaves, opaque movement-blocking
  supports, the two special debris types, and same-owner metadata 7/15 do.
  The inherited leaves-material predicate also makes leaves_layer support other
  layers at *any* height; this original detail is not “corrected”.
- Unsupported neighbor updates remove without drops. Harvest delegates to the
  actual native superclass before unconditional final removal. Disabled melting
  remains disabled. Upward faces always render; other sides delegate natively.
- Every layer remains nonopaque/non-full and exposes no solid attachment face,
  even at selection height 1. Native BlockFaceShape.UNDEFINED prevents the new
  face API's default SOLID from manufacturing an attachment face.
- World reads/removals reuse the existing coordinate-domain helpers. No new
  universal World or IBlockAccess shim was introduced.

## Reproduction

From repository root, using existing cached Java 8 and native Forge classpath:

    JAVA_HOME=/path/to/java8 NATIVE_CLASSPATH_FILE=/path/to/native-classpath.txt \
      ntm-port-1.12.2/porting/tests/layering-native/run.sh /tmp/layering-proof
    JAVA_HOME=/path/to/java8 NATIVE_CLASSPATH_FILE=/path/to/native-classpath.txt \
      python3 ntm-port-1.12.2/porting/tests/layering-native/mutations.py \
      "$PWD" /tmp/layering-proof

The run compiles the **entire unchanged production owner**, WorldBlockState and
WorldAirRemoval against real cached Forge/Minecraft classes. It does not replace
Minecraft APIs. Only NTM ModBlocks' four reference fields and the two special
support types are minimal test collaborators. Their full owner behavior and the
complete production ModBlocks registry are not compiled by this bounded probe.

The test uses an actual World subclass with real Chunk storage, real get/set
state and removal, a small in-memory chunk provider and disabled light engine.
No native World setBlockState, Chunk state storage, native IBlockState dispatch,
native Block harvesting/drop loop, EntityPlayer or state codec is substituted.
The player fixture records calls then invokes native superclass implementations.
The chunk provider does not simulate full server scheduling or chunk loading.

3,247 assertions pass: all four identities and all 16 metadata states, genuine
Chunk storage and encoded block/state IDs, original formula extraction, native
collision list dispatch, no drops, solidity/side culling, support acceptance,
real neighbor dispatch/removal, superclass harvest ordering and final removal,
client unconditional removal, stored nibble normalization, World boundary reads,
and replacement using the actual foreign-state decoder. Eight compiling semantic
mutants are executed and rejected. Source pin:
`9e4cb92100d5c1ed74af2d1b19522f78919c31e77bd6a34f968053d390ae2313`.
Original bounds, quantity and replacement arithmetic are extracted from that
pinned file into an independent formula oracle. This is not an original-game
execution and is not represented as such.

## Explicit boundaries and next work

- Normal native supplied-state dispatch is implemented. Original geometry hooks
  read live metadata; these tests certify matching actual World/Chunk states,
  not deliberately stale/custom supplied states after the world changes.
- WorldInAJar's arbitrary raw-short metadata is not certified by this codec.
  No universal claim is made for IBlockAccess implementations or unported owners.
- XFactoryTool's two increment lambdas remain unchanged. Their cached block
  identity is separated from the later live metadata read by virtual callbacks,
  including support.isLeaves. Full lambda control flow and destination-owner
  codecs require separate evidence; this family does not license a cached-state
  substitution or blanket migration of those reads/writes.
- Native inherited superclass harvest/drop semantics are used, including native
  framework events/stat/exhaustion behavior. No claim of bit-identical old/native
  vanilla engine implementations or plugin callback side effects is made.
- No model JSON, item rendering, atlas, asset migration, complete registry build,
  complete game, dedicated server launch or visual gameplay is proved here.
- The full mod still has pre-existing compiler errors; isolated owner success is
  not a full-mod build or runnable-JAR claim. CP74 work elsewhere is not included.
