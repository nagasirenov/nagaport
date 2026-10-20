# Checkpoint 92: native tool attributes, captured block states and closed rendering batches

Based on CP91 f5ce86cd607379cc9810896cf91d1fee86ce6331. Fresh full offline compilation **FAILS with 10,366 diagnostics**, down **217** from 10,583. The line-insensitive diagnostic comparison finds 217 removed reports and zero newly introduced reports; this is not a gameplay regression oracle. All **4,071 generated Java inputs** byte-match the frozen sources. Production changes cover **34 existing Java files**, with no new production files. This is not a playable JAR or a launched client/server.

## Tool and weapon attributes

Eight remaining attribute overrides and the ballistic-gauntlet held-stack query use actual native slot-aware APIs. Original custom payloads apply to the five original equipment positions, not the new offhand. Replacement-map owners retain replacement behavior rather than accidentally importing native inherited damage/speed. Crucible's spent state remains empty. Inherited-only owners retain native virtual dispatch and stack/NBT overrides.

WeaponSpecial preserves the original exact float expression 4F + material damage before conversion to double, rather than accepting native Sword's one-point reduction or adding one after rounding. Only the native damage UUID is replaced; native attack speed and unrelated modifiers remain. Native 1.12 combat timing is a platform adaptation, not a claim of old-engine combat parity. Existing movement penalties are unchanged.

329,839 production-projection assertions, 191 actual-engine checks, six actual original maps containing seven compared tuples, ten rejected runtime mutants, nine complete-file inverses and 27 source drift controls pass. Exact complete-class versus method/constructor projection boundaries are documented in tests/native-tool-attributes. Original ToolAbility cannot execute as a complete class against the unpatched original engine because its Forge captureDrops dependency is absent; no stub is substituted and no full-owner claim is made.

## Captured-state air queries

Seventeen calls across fourteen files capture the actual IBlockState at the original existing read, retain the original Block local, then invoke the genuine native air query with that captured state and unchanged world/coordinates. There is no extra world read or guessed default state. Four DroneWaypoint receiver/context coordinate mismatches are deliberately preserved. Twenty-two inline reads and two Block-parameter entry points remain excluded, not silently simplified.

11,049 assertions cover 5,485 actual native registry states, complete BlockBeamBase, genuinely constructed AE2 BlockPaint, actual original bytecode/method projections and evaluation-order controls. Fourteen full-file inverses, 42 drift controls and attribution of all seventeen native calls pass. Foreign native state-sensitive contracts remain native platform behavior, not universal old-engine equivalence.

## Closed native render formats

Seven owners contain ten nonescaping scopes, eleven singleton acquisitions, 21 batch sites, 117 vertex expressions and seventeen active setters. Each batch uses its actual required POSITION, POSITION_TEX, POSITION_COLOR or POSITION_TEX_COLOR format. Geometry, modes, UVs, materials, animation and RNG progression remain intact. RenderBobble's pre-begin red setter was discarded by the original begin and is not mistakenly resurrected.

162 paired executions produce 3,639 batches, 802,272 exact vertex records, 7,266 actual nonempty Mesa submissions, twelve empty no-ops and 108 actual display-list compile/replays. Seven whole-file inverses, 142 independent source mutations and fourteen manifest mutations pass. These are bounded closed-stream/native-GL proofs, not complete owner linkage or in-game framebuffer validation. Escaping/borrowed streams and FurnaceSteel's UV1-only issue remain excluded.

## GUI hit regions

Five compiler-attributed names across four GUI owners change to native isPointInRegion. Receivers, all six integer arguments and surrounding control flow are untouched. The actual original/native protected methods have 42 identical instructions after symbolic field/constant-pool normalization. 165,157 actual calls cover origins, boundaries, negative sizes and signed overflow. Four exact file inverses, eight source mutants and strict composition with the prior slot-coordinate guard pass. Complete GUI rendering and input-loop acceptance remain unverified.

The prior press metadata consumer audit remains exact: a hash-pinned CP92 inverse restores four affected source files before checking the unchanged 518-occurrence inventory and historical artifact hashes; twelve drift mutants are rejected. No historical evidence is repinned.

## Combined build, recovery and limitations

The first offline build was interrupted at an execution check retaining an obsolete pending-permission premise. Its partial log is retained. After checking the process was gone and reconciling the actual user grants, the exact denied poll returned Unknown process; the same offline command was restarted and produced the terminal failure above. No alternate network route, new dependency lookup or access-control bypass was used.

Disk recovery removed only byte-identical, clean, tracked historical log copies from six inactive worktrees. Each matched its worktree Git object, sealed CP91 and the canonical working copy; unique/dirty/untracked files and dependency caches were preserved. Native sparse checkout made the copies recoverable through Git. The per-file receipt records 1,122,614,556 reclaimed bytes. This was not deletion of unique evidence.

Published GitHub main remains CP75 eefb4c1f6f344e4fcb15efb7e6cb6a370419a570. The cumulative recovery package targets that exact clean baseline and replaces earlier recovery patches, not stacking on top. Back up and stop on overlap/conflicts or CRLF refusal; never force/reset/normalize user files. Windows execution remains untested. No new push is authorized.

Full compilation, startup, complete machines/energy/radiation, saves and network integration remain unfinished. Prior explicit compatibility gates and isolated potion policy remain in force. The leading diagnostics include 6,826 cannot-find-symbol errors, 1,473 invalid overrides and 813 private stackSize accesses; these are compiler reports, not a count of distinct bugs.

## Reproduction

From ntm-port-1.12.2 with the verified restored toolchain/cache:

```sh
/workspace/shared/nagaport-toolchain/run-gradle.sh --offline -I /workspace/shared/nagaport-toolchain/restore-official-repositories.init.gradle -I porting/compile-diagnostics-expanded.init.gradle clean compileJava --console=plain
python3 porting/checkpoint92/verify-build.py
python3 porting/tests/check-thermal-family-source.py
python3 porting/tests/native-tool-attributes/check_source.py
python3 porting/tests/native-block-air-captures/check_source.py
python3 porting/tests/native-gui-hit-region/check_source.py
python3 porting/tests/remaining-format-batches/check_source.py
python3 porting/tests/legacy-press-promotion/cache-audit/scan.py --compose-colored --compose-replaceability --compose-cp92
```

Family READMEs contain bounded runtime commands and exact prerequisites. No proprietary Minecraft binaries are redistributed.
