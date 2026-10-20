# Checkpoint 97: native pillar owner, atlas/model lifecycle and metadata

Based on sealed CP96 f44d3ffc5ad3bfa0afcfd93d035e51bdecde2de4. Fresh full offline compilation **FAILS with 9,989 diagnostics**, seventeen fewer than CP96: 17 reports removed, zero new reports. All **4,073 generated Java inputs** byte-match frozen production. Two existing Java files and one new native model handler changed. This meets the user's conditional publication threshold, not full build or game readiness. The separate protective box-click candidate is NOT included.

## Complete bounded owner family

BlockPillar retains native Block inheritance. Its original texture/icon state is now NTM-owned, with protected inherited field visibility preserved. Eleven actual construction chains restore the existing owner's texture setter without changing argument order, constructor identity, sound/material/gameplay or registry fields. The family comprises eight exact BlockPillar instances and three BlockPillarPWR instances; the latter adds only its existing tooltip contract, which is unchanged.

A dedicated native model handler provides real Forge state/item model registration, atlas callback/loading and native cube baking. Original texture precedence, alternate-icon registration and face selection remain. In particular, the original selects the top icon for BOTH top and bottom; the separately registered alternate icon is not substituted as an unrequested visual fix. Item model selection supports arbitrary raw metadata rather than only a finite inventory lookup table.

The old world retained all four metadata bits even though the pillar textures ignore them. Native states now preserve all sixteen values, with the original nibble conversion, while retaining original placement/drop behavior. This also preserves the inherited metadata domain used by excluded lock/writing descendants. The earlier visual-only candidate that collapsed metadata to zero was corrected before freezing this source. Five other registered subclasses are not silently registered through the plain pillar handler; their custom native integration remains separate. Their inherited field/callback access was audited, not discarded.

## Evidence and limits

The completed genuine native test passes **18,631 assertions**, including eleven construction/registration chains, actual Forge state/item model routing, all sixteen world metadata states, raw item metadata, 22 pristine-identical original PNGs stitched/uploaded to native GL, original RenderBlocks face XYZ/UV/winding comparison and two resource reloads including higher-priority asset replacement. These are native integration tests, not a launched Minecraft client/server or whole-mod runtime claim. Source, resource, descendant and completed-run hashes are retained under `../tests/native-pillar-textures/`.

The preceding read-only audit reconciled all 373 CP95 texture diagnostics and proved the simple BlockBase receiver-cast family was already exhausted. This checkpoint ports a real remaining owner/model root rather than hiding caller diagnostics behind an unimplemented texture method.

An exact aggregate boundary reconstructs CP96 with the new handler absent only in the historical test view. Historical manifests/checker hashes remain intact; the thermal chain, CP96 historical stack and affected waste-terrain sound guard pass. The actual full compilation includes the new handler, with no source exclusions or stubs.

## Build interruption and reproduction

The initial offline build attempt produced only a partial log and no exit receipt. A poll was denied with an absent historical Mojang-approval premise. Read-only checks found no remaining process; after the actual earlier approvals were supplied, the single poll retry reported an unknown process. The partial log is retained. One fresh identical offline build then completed normally in 38 seconds with no further denial. No alternate download route or dependency regeneration was used. Only the terminal log supplies the 9,989 count.

From ntm-port-1.12.2 with the existing verified cache:

```sh
/workspace/shared/nagaport-toolchain/run-gradle.sh --offline -I /workspace/shared/nagaport-toolchain/restore-official-repositories.init.gradle -I porting/compile-diagnostics-expanded.init.gradle clean compileJava --console=plain
python3 porting/checkpoint97/verify-build.py
python3 porting/checkpoint97/compare-diagnostics.py
python3 porting/tests/check-thermal-family-source.py
python3 porting/checkpoint97/check-historical-composition.py
python3 porting/tests/native-pillar-textures/check-source.py
```

CP96 is durable in Library version24. Published main remains CP75 until the parent verifies the separately authorized publication; no push has occurred here. This exact sealed snapshot qualifies for that requested milestone, without force push or unrelated WIP. Native box-click protection is now narrowly approved but still separate ongoing work; earlier pending-policy text records its historical status only. Full compilation/startup, machinery/energy/radiation/save/network integration and other explicit gates remain unfinished. No proprietary binaries are packaged. Windows clean-baseline/conflict/CRLF safeguards remain unchanged.
