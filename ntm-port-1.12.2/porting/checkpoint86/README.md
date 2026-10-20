# Checkpoint 86: native player inventory boundaries and owner-local render range

Baseline CP85 abe006a0845275e8be52417967ac8ebf6f283a52. Production scope: 47 existing Java files and one new NTM-owned read helper. Fresh full offline compilation FAILS with 11,787 diagnostics, down 153 from CP85; all 4,066 generated compiler inputs byte-match the frozen source. This is not a playable JAR or client/server acceptance.

## Player inventory family

83 direct indexed reads and 19 sizes migrate to native InventoryPlayer's fixed 36-slot main and 4-slot armor lists. The narrow PlayerInventoryRead helper performs one list read and maps only identity-equal ItemStack.EMPTY to null. All other references, including noncanonical zero/negative/AIR/null-item/invalid-NBT stacks, remain unchanged. No `.isEmpty()` normalization, copy, inventory view, global Forge shim or offhand behavior is introduced. Receiver/list field evaluation precedes index evaluation. The Faraday armor check retains its exact captured-list identity and all eight repeated reads and short-circuit predicates.

21 statement-only writes use native NonNullList.set. Five literal null removals become canonical EMPTY, fifteen constructors and one native copy RHS remain exactly unchanged. List/index/RHS evaluation order and alias/count/NBT behavior are preserved. Genuine cached Forge copy() creates a distinct stack even for noncanonical zero-count input; no assumed vanilla normalization was introduced. The folder packet's unrelated raw stackSize expression is still unported: its replacement boundary is tested with a supplied RHS value, not falsely represented as complete packet compilation.

Ten legacy foreach loops normalize only the loop-local canonical EMPTY to null before their byte-unchanged original bodies. No slot is mutated by that adaptation. This prevents the hazard armor loop from admitting an absent native slot. HEVBattery already has native handling and is not touched. This does not establish inventory radiation correctness, shielding, dropped-item emissions or in-game hazard callbacks. No emission, decay, protection or energy formula changes.

Checks on the actual integrated source: 1,277 native read/expression/Faraday assertions, 1,309 native write/removal/evaluation assertions, and 4,534 native foreach-boundary assertions. Real native InventoryPlayer, ItemStack and NBT are used. Complete Faraday body has an instrumented predicate only; other consumer handlers are not claimed fully executed. All 110 migrated read expressions and 20 complete native write expressions compile; the one packet raw-count RHS remains explicitly outside that compilation. Typed before/after ownership, negative baseline compilation, mutation controls and actual native bytecode establish the bounded API contracts. Independent read/write/foreach inverses compose to exactly 36 original CP85 files plus the pinned helper. No overlapping worker source was overwritten.

Whole-array InventoryUtil consumers with mutation/copy/zero-template behavior remain deferred, as do FurnaceSpace's unrelated inventory, other equipment APIs, native stack identity differences and complete server/save/radiation integration.

## Render-range family

Thirty old per-entity renderDistanceWeight accesses cannot be replaced by native setters: 1.12 made that property global/static. Eight owner-local range overrides instead preserve the original range formula. Four owners retain private per-instance state because their direct-super constructor paths sometimes keep the original default 1.0 rather than the explicit multiplier 10. Other proven constants remain per-owner. Existing throwable range overrides already ignore five old writes; those ineffective constructor assignments are removed, not redirected to a global setter. A repeated inherited BOTPrime assignment is likewise redundant.

67,035 native assertions and 5,580 actual original-engine differential range decisions pass, including threshold, NaN/infinity and instance-isolation cases. Native Render/RenderGlobal virtual call paths and repository constructor callbacks are audited. The complete engine census covers 1,803 original and 4,876 native classes; source closure covers the exact 4,065-unit CP85 baseline. Current source hashes match the tested 18 selected/closure files. This is scoped repository dispatch preservation, not a restoration of the removed externally mutable public field API, complete entity construction/gameplay, or in-game visual validation. Exact final evidence is in tests/entity-render-distance-native.

## Reproduce and remaining work

From the module:

    python3 porting/checkpoint86/check-inventory-composition.py
    python3 porting/tests/check-thermal-family-source.py
    python3 porting/tests/player-inventory-reads/run.py --composed-source --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /workspace/shared/nagaport-toolchain/native-classpath.txt
    python3 porting/tests/player-inventory-loops/run.py --composed-source
    python3 porting/tests/player-inventory-writes/run.py --attribute --composed-source
    /workspace/shared/nagaport-toolchain/run-gradle.sh --offline -I /workspace/shared/nagaport-toolchain/restore-official-repositories.init.gradle -I porting/compile-diagnostics-expanded.init.gradle clean compileJava --console=plain

The composed modes retain pinned whole-source verification and exact selected tokens; they do not pretend the isolated family's whole-file hashes cover sibling edits. Standalone family source guards apply to their isolated manifest-defined source views. The selected API fixes remove 154 old diagnostics. Resolving ItemRebarPlacer’s array-length expression exposes one previously blocked whole-array InventoryUtil argument mismatch, making the net reduction 153; that consumer is explicitly deferred, not silently converted or excluded. All 47 existing-file inverses plus the new helper and 144 rejected drift mutations pass before the historical source guard chain.

The first offline build was interrupted by the same stale pending-bootstrap approval premise, not a Java result. Its partial log is retained. Already-granted official Mojang and 47 runtime-library permission was reconciled, the missing process confirmed and the same offline command restarted. No alternative network route, source exclusion or dependency change was introduced. CURRENT.md explains the actual resolved permission state; ModMaven and synthetic lookup restrictions remain unchanged.

CP85 was durably preserved in Library version 13 before this stage. Published main remains CP75 eefb4c1f6f344e4fcb15efb7e6cb6a370419a570. No new push. The cumulative recovery package replaces previous recovery archives and applies only to that exact backed-up clean baseline; stop on conflicts/CRLF refusal. Windows execution is untested. Potion ID/config/save policy remains isolated and unresolved. Full mod compilation, startup, machine/energy/radiation behavior and saves are unfinished.
