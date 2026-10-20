# Checkpoint 94: item-backed inventory, owned sound builders and Celestial rendering

Based on sealed CP93 d2f171750be2f0d156a019f25fa781db39db308a. Fresh full offline compilation **FAILS with 10,061 diagnostics**, 103 fewer than CP93. All **4,071 generated Java inputs** byte-match frozen production; 13 existing Java files changed, no new production files. The line-insensitive comparison removes 106 reports and exposes three existing `setBlockTextureName(String)` failures on unchanged pillar construction chains after their sound calls become valid. This is not a successful mod build or runnable/game-tested JAR.

## Inventory closure

Nine files migrate the actual shared ItemInventory base, four real nested descendants and naming/usability callers. Native IInventory vacancy is separated from nullable internal storage. Original signed arithmetic, upper-only clamp, reference aliases, list/slotN persistence, compression threshold and ejection formulas remain. Raw owner NBT presence is not masked by native nonpositive count. The final positive ejected chunk captures identity before shrinking. An edge-only public NBT identity snapshot plus the real item's full damage accessor preserves registered payloads otherwise masked by native empty handling. Capability serialization/registry remapping and engine handling of invalid EntityItem damage are explicit native boundaries, not full old-engine equivalence.

The source gate verifies nine complete-file inverses, 27 mutation controls, 15 protected pins, four descendants, 37 native hooks and 56 typed calls. Original/native engine probes, full current base and actual native Slot/World/Player/EntityItem callbacks are included. Four complete nested declarations use explicitly limited outer identities and crate projections. These are bounded tests, not full outer item/container/GUI linkage or server gameplay. Detailed assertions, MCP/SRG layout checks and exact scopes: `../tests/native-item-inventory/README.md`. Copied bag inventories and global ItemStackUtil remain unchanged. Native container ClickType/transfer/right-click integration remains next work.

## Owned sound builders

Three files close ten original sound setter calls through narrow methods on the actual BlockLogNT and BlockRotatablePillar owners. Fresh constructor identity, original sound references and chain evaluation order remain unchanged. No global wrapper, reflection or access-transformer changes. Evidence includes 387 native construction assertions, runtime/source negative controls and exact inverses. Pillar chains beyond their still-unported texture calls are not claimed fully compiled. See `../tests/log-pillar-sound-builders-cp94/`.

## Celestial rendering

One complete owner migrates six closed scopes, five acquisitions, thirteen batch sites, 54 vertex expressions, three color setters and six private helper calls. POINTS/QUADS/fans, shader order, UV/color absence, formulas and RNG order remain explicit. Evidence includes 90 paired executions, 264 batches, 10,257 vertices, actual original/native Mesa submissions and original shader assets. LoadedShader dispatch remains an explicit boundary projection, not complete renderer/game linkage. Exact inverses and 76 source mutations pass. See `../tests/remaining-sky-batches/`.

## Source-history composition

The CP94 aggregate boundary verifies all thirteen complete-file inverses and round trips, with 39 rejected mutations. The historical thermal guard chain passes. A temporary historical source view composes the unchanged normal-stream, air and press-cache guards while preserving the original CP93 checker bytes and hashes. Normal-stream retains 145 source and 12 hash mutation controls, air retains its 18 CP93 and 42 historical controls, and the press cache retains all 518 occurrences. This does not repin or erase earlier evidence. Run `python3 porting/checkpoint94/check-historical-composition.py`; see its receipt for exact view construction.

## Interruption and reproducibility

The first attempt left only a 1,295-byte log and no exit receipt; it is retained as `full-compile-interrupted.log`. Guardian reported an approval-review rejection threshold, but the underlying latest rejected action/reason was not available. Do not attribute that specific interruption to earlier stale permission premises. No Java/Gradle process remained. WIP was preserved separately before any retry. Following explicit user approval for this precise fresh offline build, the normal tool launch and polls completed without denial; the terminal build took 38 seconds. No download, push or bypass occurred.

From the module, with the existing verified cache:

```sh
/workspace/shared/nagaport-toolchain/run-gradle.sh --offline -I /workspace/shared/nagaport-toolchain/restore-official-repositories.init.gradle -I porting/compile-diagnostics-expanded.init.gradle clean compileJava --console=plain
python3 porting/checkpoint94/verify-build.py
python3 porting/checkpoint94/compare-diagnostics.py
python3 porting/tests/check-thermal-family-source.py
python3 porting/tests/native-item-inventory/check_source.py
python3 porting/tests/log-pillar-sound-builders-cp94/check-source.py --family-only
python3 porting/tests/remaining-sky-batches/check_source.py
```

Published main remains CP75 eefb4c1f6f344e4fcb15efb7e6cb6a370419a570; no new push. CP93 is durable in Library version21. The isolated 29-file placement family is NOT included here. Full build/startup, machinery/energy/radiation, save/network integration and prior explicit policy/compatibility gates remain unfinished. No proprietary dependency binaries are packaged. Windows instructions remain clean-baseline-only, stop on conflicts/CRLF, never force/reset or overwrite user changes.
