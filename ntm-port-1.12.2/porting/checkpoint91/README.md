# Checkpoint 91: scoped press retention, native equipment and inventory boundaries

Based on CP90 0be133c9fb9f07053e26f081371487e641c95214. Fresh full offline compilation **FAILS with 10,583 diagnostics**, down **248** from 10,831. All **4,071 generated Java inputs** byte-match frozen production: **66 existing Java files and one new domain helper**. No playable JAR or launched client/server is claimed.

## Corrective legacy press lifecycle

The CP90 assessment exposed an existing CP43 mapping defect: native metadata state promotion could replace a press tile whereas original metadata-only writes retained it. Only MachinePress and MachineEPress promotions now open a per-tile one-shot expected-transition scope around the inherited callback. It requires the exact tile, world, position, owner, old/new canonical states and thread. The permission is consumed before native notifications and cleared on rejected/no-op writes, reentrancy and exceptions. Other writes still delegate unchanged to native shouldRefresh. No broad same-block suppression, NBT-copy reconstruction, global permission, chunk queue manipulation or private engine-cache mutation is used.

Complete production Press/EPress block owners, BlockDummyable callbacks and inventory bases execute on real native World/Chunk. 1,539 native scoped checks, 104 retained counterexample checks, 224 actual original all-nibble checks and 11 rejected guard mutants pass. Processing-heavy tiles are explicitly limited to exact constructors/fields/lifecycle projections; their recipes, energy and processing are not falsely claimed complete. The original raw cached metadata can differ after wrapping values (for example old raw16 versus native0); the bounded consumer audit finds no differing enumerated gameplay/render/sync output, but inherited crash diagnostics can expose it. This native cache boundary is documented, not hidden by reflection.

## Native slot coordinate fields

54 compiler-attributed field tokens across thirteen machine GUI/SlotLayer owners use native public mutable xPos/yPos. No receiver, arithmetic, argument, slot content, validation policy or other control flow is changed. Unqualified SlotLayer writes explicitly use this.yPos, avoiding local-name capture. Sixteen typed shadow/static/nested/local/Unicode controls and whole-tree re-attribution pass; exact full-file inverses reject all 54 omitted-token controls.

23,411 actual original/native Slot and complete SlotLayer assertions preserve constructor coordinates, visibility, validity, layer selection, 9999 hidden y, restoration of the original y after external mutation and preservation of externally changed x. Complete original mod bytecode is symbolically relinked to actual original vanilla Slot without changing method instructions; Forge1614 patches do not change the coordinate fields. Three executable mutants are rejected. This is not complete GUI rendering or inventory gameplay validation.

## Closed colored renderer batches

Nine owners, eleven nonescaping scopes, twelve batch sites, 89 vertex expressions and 41 color setters migrate to actual native POSITION_COLOR/POSITION_TEX_COLOR. Original line-strip/triangle/fan/quad modes, all geometry, loops, RNG progression, GL/material/animation context and exact color conversions remain. CloudTom's original infinite j=0 UVs are retained rather than silently repaired. Commented code, borrowed streams and the unresolved FurnaceSteel UV1-only batch remain untouched.

316 paired full-loop/channel-boundary streams produce 8,962 batches, 45,448 vertices and 17,924 actual original/native Mesa draws. Nine exact full-file inverses and 54 mutations pass. These are extracted closed streams, not complete owner linkage or an in-game framebuffer claim.

## Native armor modifier dispatch

Three armor modifier declarations become actual native Item overrides and preserve the exact custom UUID/name/value/operation payload for the original five equipment positions. Native armor/toughness contributions remain in their proper native equipped slot. New OFFHAND does not receive the old custom payload. Original held Desh really applies its speed penalty; restricting all custom effects to the armor's own slot would silently change that behavior.

Nine manually constructed temporary-effect maps use the native MAINHAND bridge with the unchanged original stack, preserving their empty seed and virtual dispatch. Querying the worn slot instead would import protection into the temporary speed-removal map and strip armor when sprint state changes. The bounded mutant actually demonstrates that failure, rather than relying only on compilation. Tool/weapon inherited damage/attack-speed behavior remains a separate root. 10,517 native assertions, 68 original/native engine assertions, 16 actual original payload tuples, 11 runtime mutants and 60 strict composition controls pass. Exact projection/full-class scopes and actual old/native modifier tuples are documented with the family evidence; this is not complete combat balance or armor gameplay acceptance.

## Replaceability owner and caller closure

Thirty same-target calls use the genuine native IBlockAccess/BlockPos overload; six constant-return NTM owner overrides migrate coherently. 11,080 bounded runtime checks cover actual native registry states, complete BlockLayering, exact six-owner method projections and dispatch/evaluation controls. Original JAR bytecode and the sixteen-owner/descendant closure establish the unchanged true/false policies. All thirty receiver expressions resolve to actual Block before/after. Coordinates retain one-time left-to-right evaluation; there is no old API shim.

Two OilWell calls are deliberately not migrated: the receiver is read from different coordinates than the old context arguments. Native fallback looks up material from the context's block state, whereas old Forge fallback used receiver material, so a blind conversion changes existing NTM behavior. Other native fallback lookups are documented engine behavior, not claimed universal equivalence for foreign worlds or mismatched receivers. No attempt is made to silently fix the OilWell coordinates. Unfinished air/placement/hardness families remain separate.

## Recovery and remaining work

Published GitHub main remains CP75 eefb4c1f6f344e4fcb15efb7e6cb6a370419a570. CP90 was durably preserved in Library version18. Cumulative archives target the exact published clean baseline and replace earlier recovery archives rather than stacking patches. Never overwrite dirty user work, automatically normalize CRLF, force/reset or push without authorization. Windows execution remains untested. Full build/startup, actual machine/energy/radiation integration, saves, networking and prior explicit gates remain unfinished.

## Combined verification commands

From ntm-port-1.12.2:

    python3 porting/tests/check-thermal-family-source.py
    python3 porting/tests/legacy-press-promotion/audit.py
    python3 porting/tests/legacy-press-promotion/cache-audit/scan.py --compose-colored --compose-replaceability
    python3 porting/tests/native-block-replaceability/check_source.py
    python3 porting/tests/native-armor-attributes/check_source.py
    python3 porting/tests/native-armor-slots/check_source.py
    python3 porting/tests/native-slot-coordinates/check_source.py
    python3 porting/tests/native-slot-coordinates/attribute.py
    python3 porting/tests/native-slot-coordinates/run.py
    python3 porting/tests/remaining-colored-batches/check_source.py
    /workspace/shared/nagaport-toolchain/run-gradle.sh --offline -I /workspace/shared/nagaport-toolchain/restore-official-repositories.init.gradle -I porting/compile-diagnostics-expanded.init.gradle clean compileJava --console=plain
    python3 porting/checkpoint91/verify-build.py

The aggregate guard reverses all 66 existing full files and pins the new helper, rejects 201 mutations, and composes the complete historical source chain. Source guards are not runtime tests. The full build ran offline to terminal FAIL without a retry. Individual family READMEs retain the exact runtime commands, source/engine hashes and fixture limitations.

The line-insensitive diagnostic delta removes 253 earlier diagnostics and exposes five still-unported func_146978_c GUI hit-test calls after their coordinate arguments resolve, giving the net 248 reduction. Those calls were already present; this batch does not claim complete GUI compilation. compare-diagnostics.py reproduces the comparison.
