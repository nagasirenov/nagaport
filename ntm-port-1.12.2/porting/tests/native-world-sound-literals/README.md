# Additional original resource-backed coordinate sounds

Exact isolated base: CP64 `27dceb0ef9e5815de3dc5f0494ca43aef9997370`. Local/withheld; no lighting, entity-position, category-policy or other pending approval changes.

## Scope

76 direct-literal `World.playSoundEffect` invocations across48 files use the **unchanged** native WorldSoundEffects boundary. Its receiver is moved whole to argument0; original argument expressions, separators, line breaks, evaluation order and numeric conversions remain. There are67 `(int,int,int,String,float,float)`,7 `(double,int,double,String,float,float)` and2 `(double,double,double,String,int,float)` calls. The latter explicitly supply volumes100 and10000; no entity-overload activation or inferred entity offset occurs.

42 additional original HBM resources are appended to ModSoundEvents' bounded world-effect declarations. Original62 events remain in their original declaration/insertion order; new names neither duplicate nor replace them. All42 have original category definitions and47 byte-identical original samples. Existing namespace qualification/native lowercase filenames are preserved, not new asset changes. No helper, packet, category subscriber, resource resolver, existing event declaration or resource file changes. The category bridge automatically recognizes only these newly owned names, preserving prior event cancellation/replacements and resource-pack categories through its existing contract.

All227 remaining coordinate-source calls were classified. Remaining151 are retained; exclusions include96 vanilla/foreign-name calls,29 previously owned native paths,22 dynamic/nonliteral calls, two missing definitions, two special triggers and attribution/type exclusions (some reasons overlap). Missing originals such as entity.vomit/pipefail are not guessed or made fail-fast. Dynamic names and vanilla aliases are not globally remapped. UpgradePlug, slicer and crateBreak retain their earlier native-category ownership; nullChopper/nullCrashing trigger handling is unchanged. The text-only fm sample names are inside nonliteral expressions, not migrated. Sleeping entity sound positions remain outside scope.

## Evidence

- Full genuine-source JavacTask.parse/analyze:4,062 units,227 original coordinate calls exhaustively scanned,76 selected whole World receivers and456 argument expressions/types verified. Every after binding targets actual `WorldSoundEffects.play(World,double,double,double,String,float,float)`. Full enclosing methods invert exactly. Diagnostics15878→15802: precisely76 old missing methods removed, no other path/line/code/message changes. **Not a successful whole-mod compilation.**
- 1,163 actual native registry/category/resource assertions cover all87 bounded world names,104 total owned events, repeated registration/identity, original sample accessors, cancellation/replacement, custom ownership and category reload. The existing test is derived with explicit45→87 and62→104 expectations; the historical proof file is not edited or falsely claimed unchanged.
- 62,408 old/native numeric/evaluation assertions use actual original World/WorldClient bytecode and native helper. These include5,149 migrated-shape callback comparisons,120 null/exception-order comparisons, and one explicitly labelled prior robustness comparison. Paired generated expressions retain nested receivers and exact Java primitive conversions.42 new names exercised; existing native lowercase-name normalization remains explicitly visible.
- Reused, unchanged callback test passes3,428 assertions: live-list mutations, silent client observer, no entity hook, native server radius/packet/recipient behavior, and native packet→client→SoundManager→Forge event category path. Unchanged packet differential passes13,332 real old/custom/registered-native encode/decode cases. Their source hashes are retained, not reimplemented platform stubs.
- Two complete production methods, BlastDoor.finishOpen and finishClose, compile and execute on real native TileEntity/World types, preserving state assignment, sound coordinates/values and assignment-before-null-failure. This is not complete door owner/gameplay compilation.
- sources.zip holds all48 baseline consumers and the original registry. Exact whole-file inverses plus unchanged helper/category/resolver hashes pass. Original sample/category provenance is in manifest.json. Reused runtime file hashes are pinned separately.

No whole game, audible playback, complete consumer owners, universal third-party World/listener equivalence, full-loop regression, GUI, machine balance or saved-world acceptance is claimed. The established helper's native subclass/wire/category boundaries remain as documented in native-world-sound/README.md. Existing loop objects remain resource-backed subclasses and do not meet the category bridge's exact PositionedSoundRecord gate; adding sound registrations means historical tests requiring some names to remain unregistered are not blindly reused.

## Reproduce

From repository root:

    python3 ntm-port-1.12.2/porting/tests/native-world-sound-literals/check-source.py --project "$PWD/ntm-port-1.12.2" --proof ntm-port-1.12.2/porting/tests/native-world-sound-literals/proof
    python3 ntm-port-1.12.2/porting/tests/native-world-sound-literals/run.py --project "$PWD/ntm-port-1.12.2" --resources "$PWD/ntm-port-1.12.2/src/main/resources" --out "$(mktemp -d /tmp/world-literal-runtime.XXXXXX)"
    python3 ntm-port-1.12.2/porting/tests/native-world-sound-literals/run-attribution.py --repo "$PWD" --out "$(mktemp -d /tmp/world-literal-attribution.XXXXXX)"

Cached Java8, native classpath and legacy jar paths default to the supplied toolchain; flags permit alternate paths. A sparse worker may pass --baseline-project pointing at the module retaining unchanged earlier probes, and --resources pointing at the canonical assets. No asset/cache copies, downloads or new dependencies.

The full-file guard is isolated. After independent consumer edits, invoke merge.py from this evidence directory with --project <target-module> --receipt <new-json>. It requires the exact original invocation fragments and unchanged registry baseline, preserves all other bytes with a complete inverse, and is a dry run unless --apply is given. Do not regenerate this historical whole-file manifest to hide unrelated changes.

select.py is the retained one-time historical selection/edit command at the isolated worker paths, not a replay command for an already migrated tree. Use run-attribution.py for nonmutating isolated reproduction. Generated proof output belongs outside this sealed directory.
