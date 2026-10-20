# Checkpoint 81: native particles, machine signals and furnace quantities

Baseline: sealed CP80 a0dc7d14f453a1d6f01b0d5abdcd455358bf2714. This checkpoint changes 79 production Java files. Fresh offline full compilation completed with **12,471 diagnostics**, versus 12,619 at CP80 (148 fewer). All 4,065 generated Java inputs are byte-identical to current sources. The complete historical source guard passes, including 79 exact CP80 inverses and 237 rejected source mutations. This is still a FAILED whole-mod build: no runnable JAR or launched game claim.

## Scope and bounded native evidence

- 115 literal World particle names across 67 files become native enum constants. 282 native checks cover actual World dispatch and the complete extracted ExplosionEffectStandard client method. Dynamic/custom particle argument paths remain untouched. Existing user permission allows native particle simplification only, not general model or gameplay changes.
- Nine paired comparator capability/value callbacks use native block states and positions. Value bodies, metadata/core gates and arithmetic remain unchanged. 81,033 native assertions cover 20,160 cases; three behavioral mutants and original legacy overrides are rejected. Owner/domain fixtures are explicit, not full machine-world execution.
- Two radar owners migrate capability, weak and strong power callbacks plus three obsolete block-access reads. 57,858 assertions cover 11,520 cases and 240 exact production tile power formula checks through real World power dispatch. Side orientation, virtual forwarding, metadata gates, lookup ordering and integer arithmetic are retained. Four mutants are rejected. Complete radar scanning/ticking remains untested.
- Complete production DiFurnace, MachineBase and LoadedBase execute in a genuine native harness with controlled NTM surroundings. 504 assertions pass on both genuine MCP ItemStack and the same native class with its private quantity field/references mapped to the official SRG name. Seven production mutants are rejected. This is field-layout/native behavior proof, not an obfuscated game launch.

## Approved furnace-local semantics

The user approved native-empty treatment for zero/negative stacks and separately malformed native-empty fuel, inputs and recipe results in this furnace. Invalid second recipe results are rejected before output/input mutation, preserving original lookup count and already-advanced fuel/progress timing. No shared inventory policy or recipe/balance change is implied.

Dynamic-nonempty, stale-cache repaired aliases are NOT included in that approval. Their old raw quantity behavior is preserved by a private lazy read-only quantity field fallback at the three capacity/merge reads. Native nonzero getCount is the fast path. Official MCP/SRG mappings and actual Forge ReflectionHelper/transformer evidence are retained. No reflective writes, global stack mutation or fake platform API. Repaired raw64 plus3 is rejected as before; signed overflow, aliases, last fuel item/bucket identity and ordinary positive processing are tested. See tests/difurnace-raw-reader/REPORT.md and the superseded historical scope audit for precise boundaries.

## Reproduce

From ntm-port-1.12.2 in the restored cloud toolchain:

    /workspace/shared/nagaport-toolchain/run-gradle.sh --offline -I /workspace/shared/nagaport-toolchain/restore-official-repositories.init.gradle -I porting/compile-diagnostics-expanded.init.gradle clean compileJava --console=plain
    python3 porting/tests/check-thermal-family-source.py
    python3 porting/tests/difurnace-native/run.py
    python3 porting/tests/radar-power-native/run.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /tmp/nagaport-ae2-native-classpath.txt --allow-unrelated

Other bounded suites and their source/dependency hashes are under tests/world-particle-literals and tests/comparator-callback-native. Full-compile.log, diagnostics.json, source-inputs.json and source-guards.log record this exact combined snapshot. Interrupted-compile.log records an earlier tool-interrupted attempt, not a compiler result; a fresh offline retry subsequently completed. No denied ModMaven or synthetic deobfuscation download was retried.

## Continuation and delivery

Current public main remains CP75 eefb4c1f6f344e4fcb15efb7e6cb6a370419a570. No push occurred. The cumulative archive replaces earlier CP76–80 recovery patches and includes local history objects; never stack cumulative patches or overwrite later user changes. Use a fresh exact-baseline checkout, git apply --check first, stop on conflicts/CRLF refusal; no reset/force/automatic normalization. Windows commands are documented in the archive; Windows execution is untested.

A custom-potion registry candidate is isolated separately and NOT integrated: legacy configurable numeric IDs, toxin JSON and old-world effect identity require a coherent migration contract. Do not silently ignore or reject those settings. Remaining largest compile families include missing World/block APIs, native callback overrides, 812 private stackSize accesses, inventory NonNullList boundaries and rendering APIs. No diagnostic count establishes subsystem runtime readiness. Machine topology, energy network, player/dropped-item radiation and full client/server/save integration remain unfinished; existing visual compatibility gates remain in force.
