# Metal fence dungeon placements

Exact incremental baseline: 777f4f877acf0dd6ccd7c73da604a55576530837 (after the separate ore callback and titanium batches). Main coordinator CP60 contains that prior chain; this worktree deliberately was not rebased over independently edited main fragments.

456 direct ModBlocks.fence_metal placements: 411 DesertAtom002 and 45 DesertAtom003. All are standalone World calls with metadata0/flags3. Only these two production files change. Existing DungeonToolbox, placeRandomConcrete, BlockMetalFence, ItemBlockBase, model handlers and resources remain unchanged.

The actual mutable static field is evaluated at each original argument position. Java8 attributes the genuine native World receiver, Block field type and exact ModBlocks field owner; a bounded ModBlocks declaration supplies that one real identity slot while unrelated unresolved initialization is excluded from this attribution-only fixture, not from production. Eleven negative/positive controls reject local/parameter and class-name shadowing, wrong owner, wrong receiver, used returns, non-int coordinates and wrong metadata/flags. All 4,050 tracked source files are scanned for the sole exact constructor assignment, real ItemBlockBase registration and absence of BlockMetalFence descendants. This does not convert the static field into an immutable cached owner.

The native test compiles complete actual BlockMetalFence, ItemBlockBase and their NTM interfaces, plus complete DungeonToolbox. Bounded collaborators supply the two ModBlocks identity fields, actual native creative tab, localization keys and the existing direct tile lookup contract. The exact production fence constructor is extracted unchanged. The custom fence-gate identity fixture is an ordinary native iron Block; no complete custom gate behavior is certified. The unexercised Library concrete selector throws, while all six actual direction fields are retained. No Minecraft or Forge substitutes are present.

Owner checks exercise all 16 metadata values, native global state IDs, actual World/Chunk placement, modulo-two drops, real ItemBlockBase creative and placement dispatch, absence of tile entities, compressed native Anvil persistence and actual World.destroyBlock spawning native EntityItem stacks. Nine neighbor identities exercise the corrected same-owner/custom-gate/oak-gate/opaque-full-non-gourd predicate, and all 16 connection masks retain collision height1 rather than vanilla1.5. Three runtime mutations reject flattened drop metadata, restored vanilla connectivity, and changed constructor default metadata.

The previously corrected structural owner/model sources and all six declared resource/model inputs are hash-identical to structural-production-scope.json. Prior 208,475 structural assertions and eight replacement-owner assertions are reused, not rerun or relabeled as fresh evidence. In particular no native AO/pixel equivalence or pending CP37 lighting acceptance is added. The old MetalFence fallback regression is not restored.

Selected-expression native/original streams cover all 456 calls at nine origins including overflow and bounds, both live mutable-field identities (fence and a stone mutation control), null world, effectful receiver/arguments, exception timing and RNG continuation. Bounds are executed from the actual original World bytecode prefix. The stone substitution only proves uncached field dispatch; it does not extend production selection scope. The complete selected sequences compile unsplit to Code lengths 5547, 6512 and 1384 bytes. This is not a full generator or launched-game claim. Earlier structure-palette-native records the actual direct-expansion method-size failure motivating the unchanged domain helper.

Reproduce:

    python3 porting/tests/fence-palette-native/source-proof.py
    python3 porting/tests/fence-palette-native/check-owner-closure.py
    python3 porting/tests/fence-palette-native/ownership.py --java-home /path/to/java8 --classpath-file /path/to/classpath.txt
    python3 porting/tests/fence-palette-native/migrate.py --java-home /path/to/java8 --classpath-file /path/to/classpath.txt
    python3 porting/tests/fence-palette-native/prove.py --java-home /path/to/java8 --classpath-file /path/to/classpath.txt --legacy-client /path/to/original-client.jar --mutants

For main integration, use merge.py --module <module> first, then --apply --receipt <json>. It rejects changed placement lines and produces an exact inverse receipt preserving independent edits. Do not rerun migrate.py --apply on later integrated sources.
