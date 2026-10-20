# Checkpoint 87: native equipment, destruction and item-name hooks

Baseline CP86 fc2cd2dba34caba0dba74ed288f95107ab4dc979. Fresh full offline compilation FAILS with **11,645 diagnostics**, down 142 from 11,787. All **4,068** generated Java inputs byte-match frozen source. Production scope is **78 existing files plus two narrow NTM-owned helpers**. This is not a playable JAR or a launched client/server.

## Equipment and armor

44 reads across 14 files preserve separate contracts: equipment lookup dispatches virtually through native slots; player armor lookup reads through the inventory armor hook. Only canonical ItemStack.EMPTY becomes null. Noncanonical zero/negative/tagged/AIR/null-item objects remain identical, without copying or broad isEmpty normalization. The current source closure proves 34 equipment reads use slots 0..4, ten armor reads use 0..3, and all 95 relevant parsed calls/forwarding wrappers remain within those domains. No offhand or energy/protection/radiation formula changes. The three machine charger/refueler reads retain their exact surrounding code.

24 concrete mob writes and three MAINHAND drop-chance calls use native slots. All RHS expressions, aliases, counts, NBT, float bits and order remain unchanged. Six generic writes and Dummy's dynamic armor write are deferred because their broader dispatch/sound contract is not established. Actual Forge1614 patches fix the original player setter indexing; vanilla-only behavior was not mistaken for the mod's baseline. Native player writes can invoke a sound hook even for EMPTY, so they are not silently included.

2,165 original/native read assertions and 339 write assertions pass. Complete selected expressions compile and execute against actual native classes; armor predicate bodies and worn-set expression are executed, and hazard traversal instruments only its downstream callback. This is not full radiation/energy or entity-constructor acceptance. Ownership, slot closure, negative baselines and mutation controls are retained in equipment-reads-native and equipment-writes-native. Resolving ArmorUtil's old missing equipment method exposes a pre-existing private stackSize access at line 122; it remains unchanged, explaining one offset in the net reduction.

## Destruction

60 attributed calls in 50 files now use WorldBlockDestruction. It retains original X/Z [-30000000,30000000) and Y [0,256) bounds, evaluates receiver/coordinates/drop flag once in original order, then delegates once to actual native World.destroyBlock. Exact existing expressions, including NukeCustom's original X-for-Z argument, are not repaired incidentally. Native event 2001 → optional drop → AIR mutation and final boolean are preserved; removal may return false after a listener preempts it.

6,659 runtime/evaluation assertions cover actual old/native World+Chunk, both flags, all 16 metadata values, bounds, event/drop/removal order and native stone spawning one cobblestone. Eight behavior mutants and ten negative attribution controls fail as expected. Complete TileEntityDummy and BlockBeamBase sources compile and execute in the bounded fixture; other affected generators/machines and lighting/network integration are not claimed fully tested. The fixture suppresses unrelated lighting propagation and uses allocation bypass for unrelated startup, as explicitly documented in native-destroy-block.

Approved ordinary native-engine boundary: foreign Block.isAir overrides follow 1.12. The real AE2 rv6-stable-7 BlockPaint reports air despite non-AIR material, so native destruction leaves it rather than using the old material-only rule. Audited NTM owners agree with the original behavior. No extra runtime state probe, rejection or foreign-owner workaround was added. This is not universal cross-version engine parity.

## Item translation hooks and audited exclusions

11 owned hooks now override native Item.getTranslationKey(ItemStack), with the exact original key formulas and one corresponding super call. All 16 old ItemDye strings match native EnumDyeColor keys; the existing masked metadata conversion stays exact, including signed/large values. The 12-class closure includes ItemScraps. 19,911 original/native assertions pass. Complete ItemBlockNamedMeta and ItemBlockMeta compile and execute; other hook projections are not whole-owner construction tests. Dynamic ItemStack naming callers, textures, models and registry declarations are unchanged.

ItemBookLore is entirely unchanged and deferred: numeric NBT k=73 yields string "73" under the old engine but empty under native getString, changing its fallback. The runtime test caught that boundary; no unsupported parity claim or new NBT policy was introduced.

The sound audit is evidence only. Original Forge1614 makes setStepSound public via an AT; current native setSoundType is protected, so 113 external registry calls cannot be blindly renamed. Existing legal source-owned builders are valid future precedents. No dependency regeneration, reflection or sound substitution occurs here. Historical metadata guidance is marked superseded where BlockLayering was already completed.

## Integration and reproduction

All 78 existing source inverses, two new helper hashes and 240 drift mutations pass before the historical guard chain. Independently tested read/write/destruction token inverses compose back to 67 exact CP86 whole files, including shared EntityFBI; item-hook/collaborator hashes match their isolated tests. The write patch's context overlap was handled only after complete invocation-count preflight, with exact prior-byte inverse receipt, not whole-file overwrite.

From the module:

    python3 porting/checkpoint87/check-family-composition.py
    python3 porting/tests/check-thermal-family-source.py
    python3 porting/tests/equipment-reads-native/run.py --composed-source
    python3 porting/tests/equipment-writes-native/run.py --composed-source --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /workspace/shared/nagaport-toolchain/native-classpath.txt --legacy-client /tmp/nagaport-legacy-client.jar --forge-userdev /tmp/nagaport-forge1614-userdev.jar
    /workspace/shared/nagaport-toolchain/run-gradle.sh --offline -I /workspace/shared/nagaport-toolchain/restore-official-repositories.init.gradle -I porting/compile-diagnostics-expanded.init.gradle clean compileJava --console=plain

The composed modes first enforce the pinned aggregate source and independent composition proof, then execute actual current native expressions. Isolated full attribution uses its own manifest-defined tree, not misleading offsets from a sibling-edited tree. Family READMEs retain exact fixtures, commands and limitations.

An initial shell path mistake started a pre-write compile. Source stayed frozen until its process ended; its poll was interrupted by the historical stale pending-Mojang-permission premise. That incomplete log is preserved, not counted. Existing process absence was checked; actual dated user grants were reconciled, the denied poll retried once (unknown process), and the same offline command ran on the final composed source to terminal FAIL 11,645. No alternative network route, dependency change, source exclusion or fake success was used.

Published GitHub main remains CP75 eefb4c1f6f344e4fcb15efb7e6cb6a370419a570. CP86 was durably preserved in Library version14 before this stage. No new push. The cumulative package replaces previous recovery archives and applies only to the exact backed-up clean published baseline; stop on conflicts or CRLF refusal. Windows execution is untested. Potion ID/config/save policy remains isolated. Complete build/startup, machines, energy, radiation, synchronization and saves remain unfinished.
