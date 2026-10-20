# Native mutable concrete palette placements

Exact baseline CP56 `149469c7403653ca6576515b730bc033a1574cfe`.

Production changes: **431 original six-argument placements**, only `Ruin001.generate_r0` (232) and `Ruin002.generate_r00` (199). Existing DungeonToolbox/placeRandomConcrete, assets, constructors, fields and all unrelated statements remain unchanged. No generic metadata property or legacy World adapter is introduced.

Four selected fields are genuine package-private, mutable, nonstatic, nonfinal instance fields: Ruin001.Block1/2/3 and Ruin002.Block1. They initialize from brick_concrete, brick_concrete_cracked and brick_concrete_broken. Java8 attribution checks the actual declaring field, native World receiver, primitive coordinates, ignored statement result, literal metadata0/flags3. Thirteen fixtures reject local/parameter shadows, foreign/inherited fields, nonnative receivers, wrong metadata/flags/types and used results. The full-source conservative closure audit separately checks declarations, constructors, all references/assignments and subclasses, with nine mutation controls. It is intentionally lexical, not a claim of whole-program semantic compilation; its coverage and conservative rejection rules are explicit in the script. Reflection, arbitrary external mods and future writes/overrides are outside the audited closure.

Every field is read at its original call position; nothing is replaced by its initializer or cached. Each expansion evaluates receiver, x/y/z, block, metadata and flags once in original order. It then checks null receiver and original pre-chunk coordinate bounds, and only then calls actual Block.getStateFromMeta and World.setBlockState. Metadata is never dropped. Unknown/nonzero owner states are not authorized by this proof. The exact native state/drop/tile semantics of these actual constructors are reused from the pinned generic-metadata-native prerequisite, freshly rerun here.

## Evidence

- 431 attributed selections and exact source regeneration; two whole-file inverse checks; 431 per-site source mutation controls.
- **362 native checks, 6,212 exact ordered placement events** across nine coordinate contexts and four permutations of the mutable fields among all four actual concrete owners. The stream fixtures retain the entire selected sequence of each method unsplit. They compare block identity, metadata, flags, coordinates, exception/argument timing and RNG post-state. Original bounds execute the actual pinned 1.7.10 World bytecode prefix.
- Six rejected behavioral mutants cover null timing, X/Y bounds, flag evaluation, duplicate selector evaluation and flags. Adversarial evaluation fixture exercises the shared exact expansion template; it is not a claim that production field reads invoke a random selector.
- Actual Java8 Code sizes for unsplit selected native sequences: **26,507 and 22,887 bytes**. Native bytecode attribution verifies all 431 getStateFromMeta/setBlockState invocations and retained instance-field reads. These are NOT sizes of complete surrounding generator methods. Other unported statements remain, so final whole-method size must be checked when complete generators compile.
- Fresh prerequisite run: **24,754 assertions**, real owner/state/World/Chunk/compressed Anvil persistence, drop/pick and two actual offscreen atlas/model reloads. Its existing explicit fixture boundaries remain in force; see sibling generic-metadata-native/README.md. No additional visual claim is made.

No full generator/game/server lifecycle, whole-mod build, saved-world import, unknown external field replacement or nonzero concrete metadata is certified. Selected streams are not complete generator executions.

## Reproduce

From module, using cached Java8 and native dependencies:

    J=/workspace/shared/nagaport-toolchain/java8
    CP_FILE=/tmp/nagaport-ae2-native-classpath.txt
    python3 porting/tests/dungeon-palette-native/check-owner-closure.py
    python3 porting/tests/dungeon-palette-native/ownership.py --java-home "$J" --classpath-file "$CP_FILE"
    python3 porting/tests/dungeon-palette-native/migrate.py --java-home "$J" --classpath-file "$CP_FILE"
    python3 porting/tests/dungeon-palette-native/source-proof.py
    python3 porting/tests/dungeon-palette-native/prove.py --java-home "$J" --classpath "$(cat "$CP_FILE")" --legacy-client /tmp/nagaport-legacy-client.jar

Prerequisites remain the sibling generic-metadata-native owner proof and vanilla-placement-expanded exact expansion template; dependency-hashes.json pins reused production and evidence. Follow generic-metadata-native/README.md to rerun full owner/WorldChunk proof. This worktree restored only four unchanged tracked concrete PNGs for that run.

`merge.py --module MODULE` checks current lines; add `--apply --receipt RECEIPT.json` to compose selected immutable fragments into later independently edited source. The merger verifies an exact inverse preserving all independent edits. Default migrate/source proofs require exact isolated result; later integration must compose the receipt, not weaken the frozen baseline.
