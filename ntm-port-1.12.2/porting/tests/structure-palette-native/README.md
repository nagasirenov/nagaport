# Native reinforced-sand / light-brick structure palette

Incremental baseline: `ab5c7ac1` (full commit in manifest.json), CP56 plus the completed 431-site Ruin palette proof. This batch does not change those completed edits or their evidence. Owner construction/source lineage is independently pinned to the existing CP54 Generic/Base archive and CP56 complete ModBlocks archive.

## Production scope

**1,298** fixed-metadata-zero/fixed-flags-three field placements:

- DesertAtom001.generate_r0: 604 reinforced_sand reads of Block3.
- DesertAtom002.generate_r00: 135 reinforced_sand reads of Block3 and 220 brick_light reads of Block6.
- DesertAtom002.generate_r01: 98 brick_light reads of Block6.
- DesertAtom003.generate_r00: 241 brick_light reads of Block6.

One domain helper is added to the existing DungeonToolbox: placeUnorientedStructureBlock(World, int, int, int, Block). This is restricted by the audited call inventory to these two unoriented BlockGeneric palette owners. It has no arbitrary metadata argument, configurable flags, owner substitution, runtime palette rejection or generic legacy World signature. Unknown/custom metadata owners are not authorized by this proof. Existing placeRandomConcrete and all preexisting Toolbox bytes remain unchanged.

Four fields remain package-private, mutable, nonfinal instance fields. Every placement reads its actual current field at the original evaluation point. Nothing is cached or replaced with the initializer. The helper arguments evaluate receiver, x/y/z and block once, left-to-right. Literal 0/3 removal has no evaluation effects. The helper checks null receiver and the exact original pre-chunk bounds before resolving the native state, calls real getStateFromMeta(0), and invokes real World.setBlockState with flags3. Return values remain ignored. The complete compiled DungeonToolbox has no static initializer or initializing superclass/interface behavior added by this helper.

## Genuine owner and resource evidence

run-owners.py extends the prior four-concrete probe with **both exact production construction statements**, retaining complete actual Generic/Base and rendering owners. Six real blocks are registered with Forge, not a fake ModBlocks API. The two new constructors retain names, hardness15/5, resistance40/20 (native getter24/12), ROCK material, creative-tab reference and original textures. All 16 metadata inputs are explicitly checked: only zero round-trips; nonzero collapse is documented, not certified.

Fresh run: **27,004 assertions**, including actual World/Chunk storage and compressed Anvil round trips for96 positions, real drop/pick/non-tile behavior, native state IDs, original 1.7 bytecode drop/resistance oracles, actual baked cube geometry, and both new original PNGs through two real atlas/GL reloads. Existing four-concrete RNG tests remain a regression only; the new placements consume no RNG. Exact source-only ItemBlockBlastInfo registration/metadata routes retain the prior fixture boundary, not a complete production item-class compilation claim. Full Library/ModBlocks initialization, full model event startup and complete game execution remain outside scope.

## Placement evidence

- Java8 native receiver/primitive/declaring-field/ignored-result attribution for all 1,298 sites; **18** positive/negative fixtures covering all four allowed fields, excluded same-name owners, local/parameter/inherited shadows, wrong types, metadata and flags.
- Conservative whole-source ownership closure: all four declarations/uses, two sole exact ModBlocks construction assignments, no reassignments/shadows/subclasses/external field accesses. **Nine mutation controls**. This is deliberately lexical closure, independent of use-site compiler attribution; arbitrary external mods/reflection remain uncertified.
- **223 native checks, 9,578 exact ordered placement events**, four unsplit selected method groups, nine coordinate contexts and two permutations of both actual native palette owners. Six semantic mutants are rejected. Evaluation probes include exceptions at every receiver/coordinate/block argument, captured receiver mutation, null/invalid coordinates, and a real native Block subclass witnessing that state conversion occurs exactly once only after bounds/null checks. Counts include mutation runs and are not a coverage percentage.
- Complete production DungeonToolbox compiles against native APIs, with exact bounded Library fields/selector and nested MetaBlock dependency fixtures. Library's unrelated methods and RecipesCommon's outer members are not claimed compiled.
- Actual Java8 rejects a **604-site direct expansion** as code too large. The four actual unsplit helper-call sequences compile to **19,102 / 10,924 / 3,039 / 7,626 bytes**. These are selected-sequence sizes, NOT complete surrounding generator sizes. Full generator compilation and final whole-method bytecode sizes remain gates.
- Three generator source inverses, 1,298 per-site mutation controls and complete DungeonToolbox inverse preserve all other bytes. Existing placeRandomConcrete is checked byte-for-byte.

No full generator/game/server, whole-mod compile, save-version import, arbitrary field replacement or nonzero BlockGeneric metadata preservation is claimed. No dependencies, assets, build exclusions or platform APIs were changed.

## Reproduce

From module, with cached Java8/native classpath:

    J=/workspace/shared/nagaport-toolchain/java8
    CP_FILE=/tmp/nagaport-ae2-native-classpath.txt
    python3 porting/tests/structure-palette-native/check-owner-closure.py
    python3 porting/tests/structure-palette-native/ownership.py --java-home "$J" --classpath-file "$CP_FILE"
    python3 porting/tests/structure-palette-native/migrate.py --java-home "$J" --classpath-file "$CP_FILE"
    python3 porting/tests/structure-palette-native/source-proof.py
    python3 porting/tests/structure-palette-native/run-owners.py --java-home "$J" --classpath-file "$CP_FILE" --legacy-jar /tmp/nagaport-legacy-client.jar --original-assets-jar /workspace/shared/nagaport-original-pathfinder/mods1710/HBM-NTM-.1.0.27_X5645_H261.jar
    python3 porting/tests/structure-palette-native/prove.py --java-home "$J" --classpath-file "$CP_FILE" --legacy-client /tmp/nagaport-legacy-client.jar

Normal owner runs compare source-manifest.json, never regenerate it. Only initial recording used --record-source-evidence. dependency-hashes.json pins reused probes, source archives and bounded dependency sources. The six tracked PNGs must be present; this sparse worktree restored just the needed original tracked files, without modifying them or sparse configuration.

`merge.py --module MODULE` checks all selected immutable original lines and adds only the new Toolbox helper. Add `--apply --receipt RECEIPT.json` to apply. Every current-source edit has an inverse; independent changes are retained. Default isolated source proofs remain frozen, and later integration must compose its receipt. No full Gradle run is necessary per batch; the coordinator owns combined compilation.
