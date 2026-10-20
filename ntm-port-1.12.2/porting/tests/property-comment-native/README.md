# Finite native Property comment migration

Base: CP88, `a65d3acbf05634c3cedbce5eff43ad0bd15cefdb`.

Production scope is exactly 27 substitutions from `receiver.comment = value;` to `receiver.setComment(value);`: CommonConfig 7, BombConfig 19, WeaponConfig 1. No helper adapter, new save call, comment text, category/key, default, value getter, bounds, policy or potion registration changes. `closure.json` records the whole module's native Property imports and `.comment` accesses: the other assignment belongs to GUIScreenBobmazon's own field, not Forge Property. The selected receivers are local genuine Property values from Configuration.get.

`selection.json`, `baseline.zip` and `source-proof.py` require the exact three output files and byte-exact inverses to CP88. The guard rejects 27 individually missing setter calls and five text/default/bounds/return/save mutations. It does not claim whole-project javac attribution.

## Native evidence

`run.py` compiles all three complete production owners with Java 8 and genuine Forge 2768 Configuration, ConfigCategory and Property. A logger-only MainRegistry fixture isolates the unrelated mod bootstrap; no Forge or Minecraft class is stubbed. It also compiles the three exact CP88 originals against genuine Forge 1614 binaries extracted from the already-available userdev jar. A third run uses genuine native classes with reflection implementing only the original field assignment, to separate migration effects from engine-version differences.

Each run passes 742 assertions. All seven CommonConfig methods exercise null, empty, single/multiple lines, blank lines, tabs and Unicode comments, with fresh/default, pre-existing custom and malformed values. Integer bounds metadata is retained; an existing out-of-range 45 is intentionally not clamped. Both full owner loaders exercise defaults, saved negative/boolean overrides and malformed input. Property field snapshots verify assignment changes only comment, preserves identity/null and does not set/clear changed flags. Saves, repeated saves and reload/reapply/save produce identical bytes for 21 cases. Comments are reapplied by the owners after reload, not assumed to persist in the parser's Property metadata.

Native setter versus native direct-field oracle: all 21 config files and the full value/default/comment/bounds/changed/static-field snapshots match exactly. Legacy versus native: all 21 files, returned/static values, defaults, comments, bounds, pre-save and reload snapshots match exactly. There is one explicitly retained engine difference: 2768 saving clears individual Property changed flags, unlike 1614 (198 differing post-save flags in these fixtures). Configuration remains changed in these fresh-category cases. No attempt is made to change engine save policy or claim cross-version flag equivalence.

Genuine native bytecode is checked: setComment consists only of `aload_0`, `aload_1`, `putfield comment`, `return`. Compiled complete owners contain exactly 27 native setter invocations. Compiling the unchanged originals against native APIs rejects exactly 27 private-field writes. Four runtime mutants (omitted helper assignment, corrupted literal, changed getter result and changed radius default) are rejected. The initial harness incorrectly expected Configuration.save to clear all changed state; native observation corrected that assertion and exposed the documented engine-version distinction. Production was not modified to accommodate it.

Limits: this is a complete-owner config-boundary test, not full mod compilation, GUI config editing, FML mod startup, arbitrary third-party Property/Configuration subclasses or live gameplay. It adds no gameplay/config policy. No downloads, cache edits, dependency changes or full common compile were performed.

## Reproduce

```sh
python3 source-proof.py
python3 run.py --java-home /path/to/java8 --classpath-file /path/to/native-classpath.txt --legacy-userdev /path/to/forge-1614-userdev.jar --report native-receipt.json --mutants
```

The script uses temporary outputs and local artifacts only. `native-receipt.json` records engine provenance, output hashes and the narrowly enumerated inherited flag difference. `native-test.log` records the successful complete run.
