# Native NTM ranged and FBI melee tasks

## Frozen scope

Production commit `02ff835ffd13b43f3611f2727bc42ec865f311b7`, based on CP47 `7baaa1782f8e037a4a39c685622e0370c8def86d`.

Three existing owners receive only exact import/type/factory substitutions:

- `EntityCyberCrab`: original speed 0.5, intervals 60/80, radius 15.
- `EntityTaintCrab`: original speed 0.5, intervals 5/5, radius 50.
- `EntityFBI`: ranged priority 2, speed 1, intervals 20/25, radius 15; melee priority 3, player-only start, speed 1, long memory true.

Two complete, mod-owned native tasks implement the actual original decisions: `EntityAIRangedAttackNT` and `EntityAIFBIMelee`. They extend the real native `EntityAIBase`, use actual native entity/navigation/hand/path APIs, and are not fake Minecraft API classes. There is no general old-platform facade and no source exclusion. Original entity combat/projectile methods, attributes, drops and target tasks are untouched.

## Why direct native renames are not equivalent

The original official 1.7.10 `vd` (`EntityAIArrowAttack`) checks both range and visibility when its decremented cooldown reaches zero. The native 1.12 `EntityAIAttackRanged` checks only visibility at that boundary. It therefore fires outside the original radius. The test demonstrates that native mismatch and retains the original zero-tick early return followed by a negative-tick interval restart, unclamped interval ratio, clamped shot power, 20-tick visibility/navigation threshold and stale target/path continuation.

The original `uq` (`EntityAIAttackOnCollide`), with Forge 1614's actual patch, differs from native `EntityAIAttackMelee` in multiple material ways:

- The original constructor's target-class test accepts only `EntityPlayer` and subclasses at task start. It does not reapply the class filter on continuation/update.
- Forge 1614 unconditionally throttles path acquisition with a 4..10 timer. During that timer it returns true even when the last path was null.
- Forge 1614 unconditionally accumulates failed-path penalties. The native Forge task uses a private `canPenalize=false` default.
- Native melee can start without a path when the target is in reach; original cannot on the path-query tick.
- Native melee adds creative/spectator continuation/reset behavior; original retains its target and does not add those vetoes.
- Original attack condition is `attackTick <= 20`, not `<= 0`. Repeated nearby updates attack every tick with the counter reset to 20. This original quirk is intentionally preserved, not balanced or repaired.
- Original main-hand presence controls only swing animation, not the attack itself. Native EMPTY is the empty-hand representation; obsolete non-null zero-count stacks are not claimed to remain representable as equipped nonempty stacks.

The original reach calculation's float operation grouping is retained: `(width * 2F) * (width * 2F) + target.width`, promoted only after addition. Bounding-box minimum Y, not target position Y, is used where the original uses it.

## Actual proof and boundaries

`proof.log` records 372,976 native assertions plus 15 exact extracted-owner factory/registration assertions, all passing. Source guard validates three complete owner reversals, two complete helper hashes, and rejects nine owner drift mutations.

The two production AI classes compile in full. The three entity owner classes do **not** compile in this fixture. Only the complete two `arrowAI` method bodies and the two exact FBI `tasks.addTask` statements are extracted verbatim, compiled on actual native subclasses, and executed. Source hashes separately certify the owners' otherwise unchanged bytes. Existing entity lifecycle/equipment/navigation/sound and ranged-interface migration blockers are outside this finite batch.

For ranged differential testing, `OriginalRangedOracle.java` loads the actual original `vd.class` from an external official client JAR. ASM renames types/members and replaces its one bounding-box field read with the real native getter. It does not replace any branch, arithmetic operation, constant, timer or visibility/radius decision. The rehosted original AI and production task execute against matched actual native entities, native look helpers and native Path/PathNavigateGround subclasses over 61,440 deterministic scheduled updates. Visibility and path outcomes are deliberately controlled.

Melee proof is **not** described as an original-binary differential. Its oracle consists of the audited original `uq.class` instructions, original Forge 1614 patch and independently specified native behavioral cases. Cases cover player/dead/null eligibility, null-path cached-true throttle, RNG ordering, initial/accumulated/reset path penalties, strict distance/refresh thresholds, native home-coordinate floors, creative/spectator target retention, original every-close-update cadence, main-hand swing and bounding-box minima. The old unpatched class alone would be an incorrect Forge oracle.

No Minecraft/Forge classes are substituted. Native World allocation is controlled using Unsafe; navigation and visibility are real-type subclasses returning specified outcomes. There are no NTM collaborator stubs in this test. No actual path search, loaded server, client render, full entity tick loop, projectile operation, game launch or save acceptance is claimed.

## Reproduce

From the module root, with existing Java 8 and the actual native dependency classpath:

```sh
python3 porting/tests/native-ai-roots/check-mapping.py --module .
python3 porting/tests/native-ai-roots/run.py --module . \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)" \
  --legacy-client /tmp/nagaport-legacy-client.jar \
  --legacy-forge-userdev /tmp/nagaport-forge1614-userdev.jar
```

The runner verifies the exact original class identities and Forge patch before running. It uses portable Java executable suffixes and `os.pathsep`; Windows execution is not claimed. Original client/Forge JARs, transformed classfiles and dependencies are not distributed. No download or installation occurs. The full mod compile belongs to the coordinator and was not run in this worktree.

`check-mapping.py` exposes `reverse_source(rel, text)` for historical audit integration. It never writes reversed sources or compiles obsolete code. All production hashes and exact changes are in `source-manifest.json`.

See `original-api-evidence.txt` for reproducible provenance and material instruction boundaries. `pathfinder-pending.md` records the deliberately untouched remaining path root.
