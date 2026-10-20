# Bounded dungeon native world API migration

Baseline: `077492a6c31c910f34697783f1aa0cfabd8ff88e` (CP51). Production changes are confined to the 13 files under `src/main/java/com/hbm/world/dungeon/`. No resources, balance, build exclusions, platform classes, compatibility wrappers, registry owners, or other production files change.

## Delivered

- 4,762 of 12,054 active legacy placement calls now use genuine `World`, `BlockPos`, `IBlockState` and registered vanilla `Blocks` fields.
- 70 coordinate reads: `getTileEntity(BlockPos)` and `getBlockState(BlockPos).getBlock()` with the legacy block-read boundary retained.
- 5,260 active vanilla constant references are mapped. `fence` becomes `OAK_FENCE`, and `wooden_door` becomes `OAK_DOOR`; the other fields uppercase. Every field is verified against its actual legacy registry numeric identity, not inferred from capitalization alone.
- The 27 additional text matches for `.setBlock(` are comments, not executable placements. Comments are untouched.

## Safety of the mechanical rule

`PlacementAst.java` uses the JDK compiler's syntax tree and exact source positions, not a regex replacement. Its owner proof requires a parameter named `world` typed through the explicit native World import, no rebinding or same-name declaration inside the method, and no intervening nested class or lambda. Constants require the exact native Blocks import with no variable or class shadowing it.

Placement arguments are more tightly bounded than the initial AST purity check: coordinates are the method's primitive-int x/y/z parameters, optionally plus/minus integer literals; metadata and flags are integer literals; the block is a proven vanilla field. Calls must be four- or six-argument expression statements, so their boolean results are already ignored. All effectful calls, random helpers, nonliteral metadata/flags, custom owners, and more general coordinates remain untouched. Four-argument placement defaults of metadata 0 and flags 3 are confirmed in original client bytecode.

Each migrated statement has a fresh block containing one local BlockPos. The native validity check precedes state conversion and mutation. The original coordinate arithmetic (including `y + -4`, signed overflow, and call order) is preserved. All source slices outside explicitly recorded edit ranges remain byte-identical; reversing all edits reproduces the exact CP51 source. Bootstrap-initialized vanilla constants and proven-total metadata conversions introduce no new random draws or stateful argument evaluations. This rule does not generalize to nullable or custom block owners.

Legacy `ahb.d(int,int,int,aji,int,int)` rejects X/Z outside [-30,000,000,30,000,000) and Y outside [0,256). Native `World.setBlockState` itself checks Y and the target platform's debug-world condition, but not those X/Z bounds. The native `World.isValid(BlockPos)` guard preserves all legacy coordinate rejection. Native `getBlockState` also lacks the old horizontal check, so migrated block reads use an isValid conditional with AIR fallback; only proven pure parameter-coordinate expressions are evaluated twice. Both old and native tile lookup reject out-of-height positions with null. Internal target-platform debug-world policy, lighting, notification implementation and exception-report text remain native; this is not a claim of byte-for-byte engine equivalence.

## Proofs

`migrate.py` rebuilds the edits from pinned sources, executes both actual Minecraft registries, requires exact native metadata round trips, validates disjoint token ranges and the exact inverse, and compares the entire reconstructed result to every changed production file. `ownership.py` supplies parse-only negative fixtures for foreign/reassigned/nested receivers, increment/call arguments, shadowed constants, comments and strings. Those negative fixtures are explicitly not a platform compilation proof.

`streams.py` independently lexes the pristine original and CP51 sources and compares all 12,040 original placement expressions in the 11 generators with migrated placements. It then uses the pristine arguments as an independent integer/registry-ID/metadata/flag stream oracle. All 4,762 selected exact native statements compile against the real 1.12.2 platform, without any fake net.minecraft API or NTM collaborator stub. Execution against a native World subclass produces 18,212 matching ordered events over nine ordinary, negative, horizontal-boundary, invalid-height and integer-overflow origins. The test splits its extracted snippets into finite methods only; production algorithms are not split or rewritten.

A separate native World with real native Chunk instances exercises the actual World.setBlockState implementation: state storage, same-state false return, flag 3 versus flag 2 notifications, chest creation and metadata-5 orientation, and tile removal upon AIR replacement. There are 18,233 assertions in the combined native probe.

## Explicit remaining work

7,292 active placement calls remain legacy. The broader candidate audit additionally records out-of-rule read/mutation methods in `excluded.json`:

- 4,311 pure custom/dynamic block-owner calls need native metadata-owner proof.
- 2,961 effectful/unproven argument calls remain untouched, including random concrete and random bookshelf/brick selection.
- Seven vanilla placements carry invalid or non-round-tripping metadata (chest 0; nether wart 5; diamond block 5/8; lit redstone lamp 5). Their numbers are not silently normalized.
- Nineteen coordinate cases and twelve other coordinate/mutation methods are outside this narrow rule. These totals include non-placement methods.

Whole dungeon classes and world-generation entrypoints are NOT claimed to compile or run. Generator overrides, custom block owners, tile contents, spawners, chest loot, and random-helper closures still need porting. The selected placement stream test is not a whole-generator execution test. Only the coordinator runs the full Gradle build and records the genuine compiler-error delta.

## Reproduce

From the module directory, with the supplied JDK8, real native classpath, and original client jar:

    E=porting/tests/dungeon-world-native
    J=/workspace/shared/nagaport-toolchain/java8
    CP=$(cat /tmp/nagaport-ae2-native-classpath.txt)
    python "$E/ownership.py" --java-home "$J"
    python "$E/migrate.py" --module "$PWD" --java-home "$J" --classpath "$CP" --legacy-client /tmp/nagaport-legacy-client.jar
    python "$E/streams.py" --java-home "$J" --classpath "$CP"

`--apply` is only for rebuilding the reviewed migration in its explicitly supplied module; verification is the default. Sources in `sources.zip` are pinned by `source-hashes.json`; their original licenses are included. `manifest.json` pins every before/after source hash and candidate counts. The native registry/state mapping is in `native-mapping.tsv`.

Original client SHA-256: `a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce`.
Native Forge 1.12.2-14.23.5.2768 development jar SHA-256: `8c75037188f87bc62939eefa03c2480551509034f069728de012c09276f8f49e`.
