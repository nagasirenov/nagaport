# Incremental machine/core nullable inventory NBT loading

Incremental base: `aab632a6140b1054f5d539f63aa8b127db88feb0`.
Ancestry: sealed CP70 `ea933e28fe3a1a9a60e9d438411a02182b2ab22a`, first-family
production `ba42d322dece18256995e1377cf3552e3d520cd5`, first-family evidence
`aab632a6140b1054f5d539f63aa8b127db88feb0`. Apply this production/evidence pair
after that first pair (or its unchanged integrated equivalent).

## Finite production scope

17 more loader calls reuse the unchanged `NullableInventoryStackNbt` boundary:
ForceField, KeyForge, MissileAssembly, RadGen, SatDock, SatLinker, Shredder, Siren,
Turbine, ReactorControl, RtgFurnace, SoyuzLauncher, WasteDrum, Watz,
RBMKSlottedBase, CrateBase and RequestNetworkContainer.

15 calls load `slots`, RadGen loads its owned `processing` array, and Watz loads
its owned `locks` array. ReactorControl and SoyuzLauncher inherit nullable slots
from the NTM MachineBase; the other selected arrays are directly owned. Each
selected source and original pristine source has nullable-entry consumers and
non-null save filtering. Every original load loop is independently matched against
the new loop with only the loader-expression substitution. No NonNullList, native
inventory API return boundary, packet synchronization singleton, or recipe/IMC
loader is adapted. Existing MachineBase and ItemStackUtil implementations remain
untouched. CrateBase CRLF line endings are preserved byte-for-byte.

Save list names (`items`, `progressing`, `locks`), signed-byte slot access, bounds,
iteration order, duplicate overwrite behavior, resets (or absence of resets),
RBMK diagnostic gate, RadGen progress validation/early return, Watz's existing
`slots.length` guard, superclass calls and unrelated machine state remain unchanged.
No topology, balance or save-schema edits. In particular, Watz's guard is not
silently changed to locks.length: its constructor sizes locks from slots, and
any preexisting mismatched-array behavior remains outside this change.

## Evidence and limitations

`reused-proof-inputs.json` pins the unchanged prior actual native/helper and actual
1.7.10 runtime evidence. Those 191 native and 123 original assertions are reused,
not counted as newly executed coverage. `verify_and_extract.py` checks their hashes
and the production helper hash before checking all 17 new callers, array-owner
source hashes, pristine hashes, exact whole-file reversal to the incremental base,
and pristine-loop equivalence.

The script then extracts three representative actual production loop bodies
(ForceField slots, RadGen processing, Watz locks) into a clearly named test host.
No loop statement is rewritten. The host supplies the array/list parameters;
processing and locks alias the supplied test array with equal-sized slot topology.
The original caller classes are not compiled or instantiated. This is explicit
source-slice runtime evidence, not whole-tile execution or a platform stub.

Observed: all source checks PASS; 21 new runtime checks on real mapped native
ItemStack/NBT and the unchanged, compiled production helper. They cover signed
out-of-range slot bytes, valid population, invalid registry records clearing old
entries, last-write-wins duplicates including invalid entries, default slot zero,
registered zero-count distinction and initially vacant arrays. Surrounding
conditional gates and other machine fields are protected by exact source reversal,
not claimed as runtime-tested. Existing machine compile errors, full build,
client/server startup and in-world gameplay remain outside this evidence.

Run `./run.sh` with Java 8 `JAVA_HOME` and actual mapped Forge 1.12.2
`MINECRAFT_CLASSPATH`. No new downloads, old-runtime rerun or installation needed.
