# Checkpoint 96: bounded box transfers, terrain sounds and ordered crane boxes

Based on sealed CP95 58f7f0c93cb4f5814d552e003ec9c7e2179a115f. Fresh full offline compilation **FAILS with 10,006 diagnostics**, eight fewer than CP95. Twelve reports were removed; four existing texture-setter failures became visible on unchanged terrain construction chains after their sound calls became valid. All **4,072 generated Java inputs** byte-match frozen production: eight existing files and one new helper. This does not meet the user's 10,000-or-fewer publication milestone and is not a playable JAR or launched game.

## Item-backed transfers without payload normalization

Two box transfer methods use the domain-local ItemInventoryTransfer helper, preserving null-domain vacancy rather than overwriting every native-empty destination. The complete original two-pass algorithm retains signed int arithmetic/overflow, aliases, slot iteration/restrictions, callback order, full registered item/damage/tag payload and exactly-zero cleanup. The single SlotNonRetarded quantity accessor is native under its existing native getHasStack branch. Global InventoryUtil/ItemStackUtil and ItemInventory itself remain unchanged.

11,370 assertions pass in each MCP/SRG field layout, plus four executable negative-payload mutants, three complete-file inverses, nine drift controls and eleven protected pins. The differential oracle executes the original merge method's bytecode adapted to native representation boundaries; it is explicitly **not** a complete old-engine execution. Full helper/base/slots and bounded container method projections use genuine native APIs. Source stacks hidden by native getHasStack are not consumed, but full original-invalid-source transfer/display equivalence is not claimed. Public edge-only identity snapshots follow native capability serialization and current registry resolution. Exact limits: `../tests/native-box-containers/README.md`.

**Ordinary slotClick and outer right-click activation remains unchanged and excluded.** Native click modes can overwrite retained invalid/nonpositive payload; a separate user decision on a safe refusal policy is pending. This checkpoint neither activates that known loss nor implements an unapproved gate. Existing obsolete callbacks still fail compilation, so box GUIs are not claimed complete. Rebar's original held-index anomaly and toolbox moveRows remain separate. No proposal-only dispatch patch is integrated.

## Owned terrain sound calls

Seven WasteEarth/WasteLog/WasteLeaves registrations use narrow builders on the actual native-derived NTM owners. Original fresh-constructor identity, sound references, chain position, texture calls and all gameplay bodies remain. Genuine native construction projections pass 386 assertions and nine compiled runtime mutants; twelve source controls, protected-access checks and exact inverse/replay pass. Complete owner/gameplay linkage is not claimed. See `../tests/waste-terrain-sound-builders-cp96/`.

## Two proven ordered crane bounding boxes

Only two TileEntityCraneGrabber factory selectors become native AABB constructors. Typed local-definition/write proof establishes finite coordinate bounds and exact ordered endpoint additions; no generic assumption that constructor normalization is harmless. Original/native extracted-expression checks pass 3,764,682 assertions; both constructor bindings and all frozen CP95 source identities are verified. Integer-overflow and unresolved door/NaN cases remain untouched. These bounded arithmetic/engine tests are not full crane gameplay. See `../tests/native-aabb-finite-locals/`.

## Composition and reproduction

An exact aggregate inverse reconstructs CP95 before running prior historical guards. The new helper is omitted only from that historical test view, never from the actual compilation. Prior source hashes/manifests remain pinned. Source-only and runtime receipts distinguish full support classes from explicit projections.

From ntm-port-1.12.2, with the existing verified cache:

```sh
/workspace/shared/nagaport-toolchain/run-gradle.sh --offline -I /workspace/shared/nagaport-toolchain/restore-official-repositories.init.gradle -I porting/compile-diagnostics-expanded.init.gradle clean compileJava --console=plain
python3 porting/checkpoint96/verify-build.py
python3 porting/checkpoint96/compare-diagnostics.py
python3 porting/tests/check-thermal-family-source.py
python3 porting/checkpoint96/check-historical-composition.py
python3 porting/tests/native-box-containers/check_source.py
python3 porting/tests/waste-terrain-sound-builders-cp96/check-source.py
python3 porting/tests/native-aabb-finite-locals/source.py
```

The fresh build completed normally in 38 seconds without a permission denial. CP95 is durable in Library version23. Published main remains CP75 eefb4c1f6f344e4fcb15efb7e6cb6a370419a570; only a later exact sealed snapshot at 10,000 or fewer diagnostics qualifies for the user's conditional publication instruction, with no force push/new credential grant implied. Full compilation/startup and machinery/energy/radiation/save/network acceptance remain unfinished. No proprietary binaries are redistributed. Windows clean-baseline, conflict/CRLF and user-file preservation instructions remain unchanged.
