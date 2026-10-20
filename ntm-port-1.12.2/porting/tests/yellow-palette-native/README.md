# YellowBarrel placement proof: reconstructed after workspace replacement

The final proof assets and their execution logs from the prior workspace were
lost. YellowStateProof.java and final prove.py guards/negative controls here are
reconstructed from retained task context, not recovered original files.
Native checks in this workspace are PENDING restored Java8 and native classpath.
Do not represent the previous reported 277 checks/682 events as a new rerun.
Preserved attribution.log predates reconstruction; it is not a new Java run.

Production baseline: CP75 d3e012b1ee72c0170cb8e4ff89bfabadb25ac553.
Exactly 105 original Block2 metadata-0/flags-3 statement placements become calls
to the existing DungeonToolbox.placeUnorientedStructureBlock helper: 9 in
DesertAtom001, 81 in DesertAtom002, 15 in DesertAtom003. Their mutable fields and
expression order remain intact. No production changes are made by this repair.
The 917 already migrated concrete placements are outside this proof scope.

## Reconstructed checks

verify-source.py performs dependency-free exact baseline/selection/current-byte
verification and dependency SHA256 checks. Its fresh output is in
reconstruction-source.log. This does not compile or execute Java.

ownership.py tests 15 Java8 attribution fixtures. migrate.py verifies actual
receiver, field owner, types, literal metadata/flags and statement position.
prove.py invokes migrate.py, then compiles whole actual YellowBarrel,
BlockDetonatable, BlockFlammable and BlockBase against real Minecraft/Forge.
YellowStateProof checks all sixteen actual states, metadata-zero flags-three
placement, bounds, collision boxes, explicit onBlockAdded scheduling and
updateTick radiation/rescheduling.

Selected original/native expression streams compile as four unsplit fixtures,
not whole dungeon owners. They compare ordered events across nine origins with
both the real registered YellowBarrel and a mutable STONE field substitution.
The original 1.7.10 client hash is pinned; its actual 23-instruction pre-chunk
bounds prefix provides the legacy predicate. Existing EvaluationProof covers
null timing, argument exceptions, RNG state, receiver mutation and conversion
timing. Six helper mutants must fail: missing null guard, inclusive x/y bounds,
wrong flags, wrong block and missing placement. Helper bytecode is inspected for
one native conversion and one placement call, with no static initializer.

## Explicit limits

collaborators.json contains bounded ModBlocks fields, EntityTNTPrimedBase,
ExplosionNukeGeneric and ChunkRadiationManager borrowed from the preserved
atlas proof. These are not complete production engines. Library direction fields
and the nested RecipesCommon.MetaBlock are source-derived; the unexercised
concrete selector fails closed. Actual ModBlocks construction is source-pinned,
but the harness registers the actual YellowBarrel constructor independently.
RecordingWorld records native state/flags with alternating return values; it
neither generates chunks nor dispatches placement hooks. The real hooks are
called separately. There is no full-mod build, game launch, whole-dungeon,
persistence, rendering or explosion-engine parity claim.

## Replay after dependency restoration

Run from ntm-port-1.12.2 with existing dependency paths:

```
python3 porting/tests/yellow-palette-native/verify-source.py
python3 porting/tests/yellow-palette-native/ownership.py --java-home "$JAVA8" --classpath-file "$CPFILE"
python3 porting/tests/yellow-palette-native/prove.py --java-home "$JAVA8" --classpath-file "$CPFILE" --legacy-client "$LEGACY_CLIENT"
```

Only fresh execution should create replacement native-proof.log,
ownership-proof.log, helper-bytecode.log and method-sizes.json. No old runtime
output has been fabricated as part of this reconstruction.
