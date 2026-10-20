# Silo concrete-only native placement

Baseline: CP55 `2fa015374be0e330d2541e5bfe9d4b96e380aa05`.

Production changes are exactly 917 original static concrete placement statements in `Silo.generate_r0`, plus one narrow concrete-only method on existing NTM world-generation owner `DungeonToolbox`. The independent 1,595-site concrete patch is NOT included. Neither generic World emulation nor an arbitrary Block/metadata/flags adapter is introduced. `generateBox` is not reused: it has different RNG ownership and flags.

## Semantics and class closure

The original World receiver and each original coordinate expression become the first four static-call arguments, evaluated once, left-to-right. Their exact source bytes and statement ordering are retained. The helper then calls unchanged `Library.getRandomConcrete()` once, before the null receiver check and the exact original pre-chunk bounds. It places native metadata 0 with flags 3 and ignores the native boolean result. The removed literal metadata/flags expressions had no evaluation effects. Selection still occurs for out-of-range coordinates and before the original null-world exception.

`DungeonToolbox` extends Object, has no fields, static initializer or initializing interfaces, and the actual complete compiled class has no `<clinit>`. Invoking it therefore introduces no static-initializer effects before Library selection. Its existing unrelated methods do not run. The new helper depends only on the existing Library selector, Block, BlockPos and World native APIs. Actual javap attribution checks those native owners and exactly one selector invocation. This is not a method with a generic block argument or an arbitrary legacy call signature.

Library remains byte-identical to the archived CP55 source. Its complete seven static field initializers (Random followed by six ForgeDirection aliases) and exact selector body are compiled in an explicitly bounded Library fixture. The complete real ForgeDirection enum is compiled. Only the selector's ModBlocks qualifier changes to the previously proved four-construction slice. Library's unrelated methods and full ModBlocks initializer are NOT claimed compiled. Their initialization relationship is not changed by the production patch. The actual complete DungeonToolbox source compiles verbatim against native APIs and the verbatim complete nested MetaBlock source body; RecipesCommon's unrelated outer members are an explicit fixture boundary, not a production stub. Neither nested MetaBlock nor outer RecipesCommon is actively initialized by the new helper.

The four actual BlockGeneric owner/metadata-zero/native-persistence proofs and 13 attribution fixtures are reused from `generic-metadata-native` and `concrete-placement-native`, pinned by hashes. This does not generalize to other blocks or metadata. The fixture MainRegistry only supplies the same construction context as those existing proofs.

## Tests and limits

- Exact original attribution: all 917 calls have real native World receiver, primitive int coordinates, the static zero-argument Library selector, ignored statement result and literal metadata 0 / flags 3.
- All 917 transformed statements compile as ONE unsplit Java method. Actual compiled Code size: **14,274 bytes**. The complete production DungeonToolbox compiles in the same javac invocation. No artificial method splitting hides the JVM limit that rejected direct expansion in the prerequisite's `method-size-rejection.log`.
- Nine coordinate contexts compare full ordered coordinates/block identities/metadata/flags and final RNG state, including overflow, bounds and null paths. The bounds oracle executes the real original client's 23-instruction placement prefix.
- Effectful receivers/coordinate arguments, receiver mutation, exceptions at every evaluated argument and the selector, invalid-coordinate/null combinations, and RNG consumption are checked against actual production helper calls.
- Six behavioral mutants are rejected: missing null check, wrong X limit, wrong Y limit, flags 2, duplicate selector draw, and premature null check.
- Result: **632 checks**, **3,648 ordered native placement events**. Native return values alternate to show they remain ignored.

This proves the selected sequence and complete helper owner with the documented dependency slices. It does NOT prove full Silo compilation, whole-generator behavior, whole mod build, client/dedicated-server startup, or in-game world generation. CP55 Silo still has 469 unrelated legacy setBlock calls after this patch alone; sibling changes can alter that number. The actual surrounding method includes unported placements and other code. Its final whole-method bytecode size must be measured again once those statements are genuinely ported. The 14,274 figure is ONLY the complete 917-site sequence, not whole generate_r0.

## Reproduce

From the module directory:

    python3 porting/tests/silo-concrete-native/prove.py \
      --prerequisites /path/to/module/porting/tests \
      --java-home /workspace/shared/nagaport-toolchain/java8 \
      --classpath-file /tmp/nagaport-ae2-native-classpath.txt \
      --legacy-client /tmp/nagaport-legacy-client.jar

The prerequisites directory must contain the two pinned existing concrete proofs. This script verifies exact complete production file hashes by default. `--integrated` allows independently migrated OTHER Silo methods while still demanding byte-exact transformed generate_r0 and exact DungeonToolbox. It does not certify unrelated edits. The baseline archive and manifest preserve CP55 before/after bytes; no source package or class is excluded from the production build, and no platform class is replaced.
