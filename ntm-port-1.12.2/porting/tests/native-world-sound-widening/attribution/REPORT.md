# Existing-registry World sound primitive-widening attribution

## Result

Independent BEFORE and AFTER full javac attribution passed for **16 calls across 14 files using six existing sound names**. The family is exhaustive among remaining direct-literal calls to the existing 45-name WorldSoundEffects allowlist. No new names, registry entries, categories, helpers, boxed conversions, dynamic names, or entity overloads were selected.

- Exact baseline: `641d2641bf196265e34e3f6968dba795f1dd061b`, repository `/workspace/shared/nagaport-player-sound-cp59`.
- All **4,058 genuine production units** were loaded from a 6.22 MB compressed Git source archive and parsed/analyzed with Java 8. No broad copied source tree, substitute source fixtures, or class emission.
- Original scan covered all **243 remaining playSoundEffect member invocations**. Selection was by complete AST and attributed types, not regular-expression receiver/argument extraction.
- Exactly 14 selected calls have `(int,int,int,String,float,float)` arguments; two have `(double,int,double,String,float,float)`. All original receivers are exactly native World. Only primitive `int → double` widening is needed.
- Full native destination resolves to `com.hbm.sound.WorldSoundEffects.play(net.minecraft.world.World,double,double,double,java.lang.String,float,float)` for every selected call.
- Complete original receiver becomes argument zero; every original six-argument expression and attributed source type is preserved exactly, in order, with no additional evaluation or casts.
- All 14 source files differ from baseline only at the selected complete AST invocation ranges. Inverse rewriting yields the original full-file bytes and SHA-256 for each file. All 78 other files in the final 92-file sparse overlay are byte-identical to baseline.
- Full production source error count changes **17,474 → 17,458**, exactly the 16 independently reproduced original missing-method diagnostics. Every other diagnostic path/line/code/message multiset is unchanged. There are zero parse errors on either side.

This is an attribution and source-invariance proof, not a clean whole-mod build or a new runtime-equivalence claim. Existing runtime helper behavior belongs to its existing native proof. The separate player-sound 36-call migration remains untouched.

## Selected existing names

`hbm:player.vomit`, `hbm:turret.jeremy_fire`, `hbm:turret.jeremy_reload`, `hbm:weapon.flamethrowerShoot`, `hbm:weapon.fstbmbPing`, `hbm:weapon.missileTakeOff`.

The original ModSoundEvents static initializer was separately AST-parsed: its exact 45 names and categories match the existing World sound manifest. No new or changed registry policy is involved.

## Evidence

- `before-selected-manifest.json`: all original whole receivers/arguments/invocations, exact UTF-16 ranges, attributed types, full enclosing methods, diagnostics, proposed native destination, exhaustive exclusions, file hashes, and existing name metadata.
- `after-verification.json`: all actual resolved native calls, owner and method signature, original receiver/argument preservation, full after enclosing methods/ranges, file hashes and inverse reconstruction, and exact global diagnostic delta.
- `selected-calls.txt`: readable listing of all 16 original and destination calls with source paths/lines and types.
- `before-all.tsv`, `after.tsv`: raw javac AST attribution records.
- `before-all-diagnostics.tsv.gz`, `after-all-diagnostics.tsv.gz`: all original and final source diagnostics, deterministic gzip with raw SHA-256 provenance in `diagnostic-compression.json`.
- `before.log`, `after.log`, `before-verification.log`, `after-verification.log`: parser/analyzer/verifier outcomes.
- `existing-registry-ast.tsv`, `ModSoundEvents.baseline.java.txt`, `existing-world-sound-manifest.json`: frozen existing registry proof.
- `WorldSoundWideningAttribution.java`, `WorldEffectAllowlist.java`, `select_widening.py`, `verify_after.py`, `run-attribution.sh`: reproducible analysis and verification.
- `inputs.json`, `native-classpath.txt`, `classpath-inputs.json`: provenance and dependency hashes.

Run `bash run-attribution.sh /new/work/directory /path/to/sparse/src/main/java` to repeat. The work directory is mandatory and must not exist; every generated output is written there, never into the sealed evidence directory. The default overlay is the parent's supplied sparse production workspace. It must contain the 14 changed files; any extra Java overlay files must match this exact baseline. Place the fresh work directory under `/tmp` to keep compiler scratch off the shared filesystem.

The attribution worker made no production edits. The parent performed the supplied 16 invocation replacements; the worker independently verified them.
