# Native Forge event accessor reads

Exact baseline: CP54 `0732d318302d807ce8579878a0cdde6b18bbba86`.
Production: 268 qualified compiler-attributed reads in 25 existing files:
142 `LivingEvent.entityLiving`, 75 `WorldEvent.world`, 51 `EntityEvent.entity`.
Only field-name tokens become the genuine native getter invocation. No wrapper,
platform replacement, declaration, write, cancellation call, listener registration,
priority, ordering, control flow, or other API is changed. Existing CRLF is retained.

Original Forge 1.7.10-1614 userdev source and embedded binaries establish public
final payload fields assigned directly from constructor arguments. Actual native
Forge getters are precisely ALOAD 0 / GETFIELD / ARETURN. Native constructors and
getters preserve reference and null identity; they do not copy or normalize.
The source scanner resolves receiver ancestry against native classes, rejects
legacy-field shadows, accessor declarations and reachable source overrides,
unknown/type-name receivers, assignments, compound/unary uses, and all unqualified
references. All 67 resolved dependency JARs are inspected with ASM for matching
accessor declarations in the relevant event descendant hierarchies. None override.

As with any virtual accessor migration, unknown external mod event subclasses may
override a getter. This is native API adoption within the resolved dependency/source
closure, not a promise of equality for arbitrary external overrides. A runtime
negative witness explicitly demonstrates the distinction.

## Reproduce

From module root:

    python3 porting/tests/native-event-accessors/run.py \
      --java-home /workspace/shared/nagaport-toolchain/java8 \
      --classpath-file /tmp/nagaport-ae2-native-classpath.txt \
      --forge1614 /tmp/nagaport-forge1614-userdev.jar --verify-attribution

No downloads, Gradle invocation or new production dependencies are used. Native
runtime probes allocate genuine MC objects using test-only Unsafe to avoid starting
a server; this is not a production compatibility substitute. Bootstrap is native.

Checks cover native owner/type/finality, exact getter bytecode, reference identity,
null payloads, derived LivingEvent shared identity, once-only receiver evaluation,
receiver exception and null timing, unchanged existing cancellation flags, and
resolved binary override closure. An unchanged complete production
ChunkRadiationHandlerSimple.receiveWorldUnload method is extracted and compiled
with actual World/WorldEvent; five checks cover client no-op, exact server-world
removal while retaining other identities, repeat unload, null payload and null event.
This does NOT compile the entire radiation handler or execute radiation simulation.

Raw headless Forge events do not receive LaunchWrapper EventTransformer's
cancellation metadata. The tests deliberately do not assert a cancellable event-bus
lifecycle. Getter bytecode is read-only and cancellation code is untouched; no game
tick, event dispatch, client/server launch or full handler lifecycle is certified.

33 compiler fixtures reject writes, compounds, shadows, declared and reachable
accessor overrides, unknown/unrelated receivers while retaining genuine reads and
once-evaluated receiver expressions. Source inverse checks verify every unchanged
byte against the input hash. Full post-migration native declaring-method attribution
must reproduce exactly the same 268 UTF-16 token positions and receiver/path/accessor inventory. Four pre-existing native getter sites are outside the changed positions and are excluded, not counted as migrated. Expected
unrelated attribution errors are reported, not hidden or called compilation success.

## Integration across unrelated overlapping edits

Prefer contextual production patch if it applies cleanly. Never copy entire source
files over other workers' changes. For token-only integration, first copy this test
directory, compile EventAccessorScanner with Java 8 tools.jar and run it three times
on the CURRENT destination source list and native classpath, with these trailing args:

    net.minecraftforge.event.entity.EntityEvent entity getEntity net.minecraft.entity.Entity
    net.minecraftforge.event.entity.living.LivingEvent entityLiving getEntityLiving net.minecraft.entity.EntityLivingBase
    net.minecraftforge.event.world.WorldEvent world getWorld net.minecraft.world.World

Capture each TSV output separately, then run migrate.py with those three filenames.
It requires the exact approved receiver/path/accessor inventory before editing only
UTF-16 field token spans. It records new before/after hashes to retain unrelated edits.
Re-run run.py --verify-attribution on the integrated tree. A second migration fails
rather than silently changing an already migrated or changed inventory. The original
CP54 manifest remains provenance; integrated hashes describe the later input tree.

Final isolated result: all checks passed, including 268 exact-position native getter
re-attributions and four pre-existing unrelated getters excluded. Each attribution
run reported 25,894 errors versus baseline 26,162, a reduction of exactly 268.
This is javac analysis, not a whole-module build. All 25 before-file hashes were
independently compared to CP54 git blobs and match. `git diff --check` reports
pre-existing CRLF line endings on changed lines; migration preserves them exactly.
