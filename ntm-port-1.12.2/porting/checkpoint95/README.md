# Checkpoint 95: native block placement contracts

Based on sealed CP94 d754432647909d6840333059865d834f87bd4a7f. Fresh full offline compilation **FAILS with 10,014 diagnostics**, 47 fewer than CP94: 47 removed reports, zero new reports. All **4,071 generated Java inputs** byte-match frozen production. The finite family changes 29 existing Java files, with no new production files. This remains above the user's 10,000-or-fewer publication milestone. No playable JAR, startup or game acceptance is claimed.

## Scope and preservation

Fifteen owner signatures and 31 legacy callers now use actual native World/BlockPos placement. Four required support hooks and two leaves/opacity pairs retain actual owner predicates. Existing original placement/support formulas, deliberate short circuits and identity tests remain. Landmine's support predicate remains the exact two original fence identities, not a widened instanceof rule. Kelp's two support identities and all seven owned stay delegations remain. Existing metadata/stay internals, deliberately mismatched OilWell replaceability and unrelated hardness migration are not silently included.

Native superclass placement intentionally follows the native platform contract. Original and native BlockVine inherit their base placement method. Native World state access has its Y bounds but not the old horizontal guard; explicit NTM support reads retain their domain helper. Native default replaceability rereads state material, while the original default used its own material; native BlockAir avoids that second read. These documented engine differences are not a claim of universal old/new world-read equivalence.

## Verification

The integrated default native runner passed **627 assertions**, original engine bytecode/Forge-hook inventory, 29 exact full-file inverses and 87 source mutations. Six real production support classes compile completely against genuine native dependencies. Fifteen changed owner methods and two inherited descendants execute as exact method projections with named identity/stay fixtures; this does not claim complete migrated owner classes. All 22 native placement owners and 41 native calls are audited. The original frozen CP93 attribution separately reproduces 10,164→10,117; the present combined build result is the distinct 10,014 above.

Detailed contracts, pins, fixture boundaries and commands are in `../tests/native-block-placement/README.md`. The actual checked-in Inventory Tweaks annotation is compiled into isolated temporary output for attribution, with source/class pins; it is not a substitute API. No dependency regeneration or new downloads were performed.

The new exact aggregate boundary and temporary historical view preserve prior manifests/checker hashes. Placement inverses run before all prior CP94/CP93/CP92 layers. Historical thermal, normal-stream, air, normal-cube and press-cache checks are retained rather than repinned. See the source-history receipts and logs for precise controls.

## Reproduce

From ntm-port-1.12.2 with the existing verified offline cache:

```sh
/workspace/shared/nagaport-toolchain/run-gradle.sh --offline -I /workspace/shared/nagaport-toolchain/restore-official-repositories.init.gradle -I porting/compile-diagnostics-expanded.init.gradle clean compileJava --console=plain
python3 porting/checkpoint95/verify-build.py
python3 porting/checkpoint95/compare-diagnostics.py
python3 porting/tests/native-block-placement/run.py
python3 porting/tests/check-thermal-family-source.py
python3 porting/checkpoint95/check-historical-composition.py
```

The fresh combined build completed normally in 42 seconds, with no permission denial. CP94 is durably preserved in Library version22. Published main remains CP75 eefb4c1f6f344e4fcb15efb7e6cb6a370419a570. The user authorized publication of a future sealed snapshot once a verified full compile reaches 10,000 or fewer diagnostics, subject to normal authentication/review and no force push; this checkpoint does not meet that condition. Next coherent work is box container/outer item native dispatch, not arbitrary count chasing. Full build, startup, energy/machines/radiation, saves/network integration and prior explicit gates remain open. No proprietary binaries are packaged. Windows clean-baseline instructions and stop-on-conflict/CRLF safeguards remain unchanged.
