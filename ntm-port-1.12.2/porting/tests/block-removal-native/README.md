# Six native machine-removal callbacks

Base: `112cf44057e5cd1d1173561e66dbcca97edc9bdb` (sealed CP71).

## Production boundary

MachineICFPress, MachineSatLinker, MachineReactorControl, MachineTurbine,
MachineSiren and MachineSatDock directly extend native BlockContainer. Their
legacy breakBlock metadata is used only when forwarding to super, in both pristine
and CP71. There are no named or anonymous source descendants. No general base,
BlockDummyable domain bridge, state-mapping owner, other callback or helper changes.

Each callback now overrides `(World, BlockPos, IBlockState)`. Coordinates come from
the supplied position. The comparator's owner comes from supplied OLD state, not
World.getBlockState: native Chunk has already stored the replacement by callback
time. There is deliberately no getMetaFromState call and no invented metadata
mapping for these otherwise unported owners. The exact callback bodies retain
all inventory reads, counts, RNG order, slot order, NBT copying, keepInventory
guards and exception propagation. Comparator invocation is the native equivalent;
super dispatch is still last. ICF's non-ISidedInventory early return remains before
super; the other five retain their original cast failure and null-tile behavior.

Old BlockContainer (`ajc`, SRG func_149749_a) invokes Block then World.removeTileEntity
(`ahb.p`). Native BlockContainer does the same with BlockPos. The saved disassembly
includes both real implementations and native Chunk's virtual dispatch with old
IBlockState. Native Chunk also has a later shouldRefresh removal path: observing
an eventual removed tile alone would NOT prove that super was preserved. The
additional direct native callback test and lost-super mutation address that gap.

## Reproduction

From the module directory, with already available native dependencies:

    python3 porting/tests/block-removal-native/run.py \
      --java-home "$JAVA8_HOME" \
      --classpath-file "$NATIVE_CLASSPATH_FILE" \
      --legacy-client "$LEGACY_CLIENT_JAR" --mutants

No downloads or source-set exclusions. The runner uses a temporary output directory.
The supplied classpath must contain the genuine cached Forge/Minecraft 1.12.2 API.
Optional `--keep DIR` preserves generated fixtures for inspection.

`selection.json` contains the exact CP71 old/new method fragments and whole-file
before/after SHA-256 hashes. The runner checks complete inverse files, not merely
selected tokens; unrelated edits are rejected. `original-audit.json` identifies
pristine source and helper hashes. `scope-audit.json` records callback occurrences
with scope depth/receiver text and the exact qualified selected owners. Selected
owners resolve explicitly to net.minecraft.block.BlockContainer; all named ancestry
and anonymous selected-owner constructions are checked. Existing explicit old
calls in multiblocks/PWR/ICF/FEL remain outside this family and unchanged.

## Results and limits

4,383 runtime assertions pass. Four negative runtime controls reject lost super,
new-state comparator owner, lost ICF early return, and duplicate inventory drops.

The runtime compiles six COMPLETE production callback method slices verbatim,
not complete production owner classes. Fixture containers provide only a native
BlockContainer constructor, controlled RNG/keep fields, and test inventory tiles.
The test uses genuine World.setBlockState, genuine loaded Chunk storage and
virtual Block.breakBlock, plus direct native dispatch for edge cases. No fake
net.minecraft/Forge APIs exist. Real BlockContainer super and real tile removal
execute. The native World subclass supplies bounded chunks and captures entity
spawns/comparator/removal observations; generation, saves, network, and entity
simulation are not exercised. Client Chunk behavior is also tested.

The independent comparison invokes the existing production ItemStackUtil.spillItems
method body verbatim, SHA-pinned, with real native inventory/ItemStack/EntityItem/NBT
APIs. The whole utility class is not compiled. This comparison establishes the
preserved spill sequence against that existing helper, NOT an independently run
1.7.10 world. The old engine evidence is genuine disassembly, not old runtime tests.

Tests include 180 engine replacement scenarios (six owners, five stack counts,
three seeds, spawn acceptance/rejection), two tagged slots with an intervening
empty slot, exact RNG/entity position/motion, old owner, depletion, per-drop NBT
copy, item totals, comparator-before-super order, keepInventory, null/empty slots,
wrong/missing tiles and spawn exceptions. Exceptions are intentionally not wrapped
in a new finally block. Orientation/rendering/other owner callbacks, production
tiles and full inventory/drop lifecycle readiness remain unproven. No full-mod
build, game launch, dedicated-server or gameplay acceptance is claimed.

For a separately changed integration tree, `apply-fragments.py --module DIR`
validates the six exact old methods and their byte-reversible replacements;
`--write` applies them, and `--receipt FILE` records whole-file before/after hashes.
It preserves every byte outside these methods. The main runner deliberately pins
whole CP71 files; its whole-file gate must not be represented as passing on a
later composed owner. Review intervening changes and run the isolated CP71 suite
alongside integration compilation rather than weakening that invariant silently.
