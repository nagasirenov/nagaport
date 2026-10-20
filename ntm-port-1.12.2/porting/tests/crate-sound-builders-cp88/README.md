# CP88 candidate: eleven crate construction sound builders

Isolated exact source base: sealed CP86 `fc2cd2dba34caba0dba74ed288f95107ab4dc979`. This is a genuine bounded production migration, separate from CP87's negative direct-rename audit. It is not a full-build or complete crate-gameplay acceptance result.

## Production change and preservation boundary

Three production files change: ModBlocks and the two existing NTM owners BlockCrate and BlockStorageCrate. Each owner gains a six-line `withSoundType(SoundType)` method which directly invokes the genuine protected native `super.setSoundType(sound)` and returns `this`. No generic Block wrapper, reflection-based setter, access transformer, native/cache modification, new sound mapping, resource, registry or dependency change is introduced.

Exactly eleven named fresh-constructor registry sites change:

- BlockCrate: crate, crate_weapon, crate_lead, crate_metal, crate_red.
- BlockStorageCrate: crate_iron, crate_steel, crate_desh, crate_tungsten, crate_template, safe.

The first two retain `SoundType.WOOD`; the other nine retain the exact `ModSoundTypes.legacyMetal` reference. The complete preceding receiver `new Owner(material).setTranslationKey(name)` is cast back to its proven fresh-constructor owner at the original sound-call position. The native name setter returns its receiver, and neither owner nor its native superclass overrides that method, so these eleven casts are safe. No claim is made for arbitrary Block values or unknown subclass chains. The material/name argument and constructor evaluation order, sound argument evaluation, and subsequent hardness/resistance/tab/texture chain order are unchanged. The added method is not used by any other site, including subclasses.

The two existing constructors consist only of `super(material)` and remain byte-identical. Every loot, opening, storage, inventory, tile, lock, harvest, drop, rendering, texture and other gameplay method remains byte-for-byte identical. Exact whole-file inversion of the three changes restores CP86. All other 4,063 production Java files are byte-identical to CP86, including every sound definition and all 102 remaining registry `setStepSound` calls. Original CRLF in ModBlocks is preserved.

## Fresh evidence

- Complete JDK8 parse/attribution of 4,066 production units before and after, with zero parse errors and no class generation. All eleven new builders, constructor identities, casts, exact sound arguments and subsequent native method bindings resolve. The error-message multiset changes from 11,787 to 11,781: eleven old sound errors disappear, five pre-existing `setBlockTextureName` errors become visible, and every other diagnostic message remains unchanged. This is not a full build or a claim that textures now work.
- 457 finite native assertions execute all eleven exact production construction chains through their existing native setters. For the five loot crates only, each slice stops immediately before the still-unported texture call. All six storage construction expressions run in full. Actual native BlockFalling/BlockContainer constructors, Block setter/getter behavior and SoundType objects are used.
- Tests establish material, name, hardness, native resistance, creative tab/null, exact assigned sound identity, both ordinary/contextual sound getters, all five sound events, volume/pitch, same-instance returns, custom/null references, untouched unrelated blocks, complete chain order, and receiver/name/sound exception ordering.
- The unchanged original-bytecode sound oracle passes its 12 assertions. `legacyMetal` remains stone break/place/step with volume 1 and pitch 1.5, not modern `SoundType.METAL`. Existing custom grate/pipe/mork and envelope definitions are byte-identical and are not newly accepted as an audio transport migration.
- Six runtime negative controls, independently for each owner: dropped assignment, wrong argument and null return. Every mutant compiles first, then is rejected at runtime. Compiler failure never counts as a runtime kill.
- Ten in-memory source negative controls reject assignment/return/argument mistakes, early sound relocation, foreign-owner casting, constructor changes, unrelated crate/storage mechanics edits and sound-definition changes.
- Two negative compilation probes confirm arbitrary native Block still has no public sound builder and its native setter remains protected. Native setter bytecode and superclass bindings are inspected. The authentic cached Forge1614 access resource establishes why original external setter calls were public; the current native artifact is not transformed.
- Production patch replay passes dry-run, exact forward application, duplicate-apply rejection, reverse dry-run and byte-exact whole-file reversal. No main-worktree file is modified by these tests.

## Honest fixture scope

These are explicitly bounded NTM-only construction fixtures, not compiled complete production crate classes. Exact current production constructors and builder bodies are extracted into same-named NTM fixture owners with their genuine native superclasses. Unrelated crate methods/fields are excluded. BlockStorageCrate's required abstract tile callback is an explicit test-only fail-fast body that throws if invoked; no test invokes it. A small test-only MainRegistry supplies creative-tab constants and the existing sound RNG input. Complete production ModSoundType, ModSoundTypes, ModSoundEvents and RefStrings compile unchanged. All Minecraft/Forge classes are authentic, not stubs. The unchanged existing Forge launcher and original-bytecode oracle are reused as test infrastructure, not production access bypasses.

Loot/storage functionality, whole-owner compilation, GUI/model/texture loading, audible output, launched client/server, and full-mod build/runtime remain unverified. This package does not activate any unrelated potion or sound-transport policy. The coordinator owns the later integration build and acceptance decision.

## Reproduce using existing cached inputs only

Run from the module directory. No network, Gradle, dependency regeneration or full build is used.

    python3 porting/tests/crate-sound-builders-cp88/check-source.py
    python3 porting/tests/crate-sound-builders-cp88/check-source-mutants.py
    python3 porting/tests/crate-sound-builders-cp88/check-replay.py
    python3 porting/tests/crate-sound-builders-cp88/check-access.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /workspace/shared/nagaport-toolchain/native-classpath.txt --forge-userdev-jar /tmp/nagaport-forge1614-userdev.jar
    python3 porting/tests/crate-sound-builders-cp88/run-attribution.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /workspace/shared/nagaport-toolchain/native-classpath.txt
    python3 porting/tests/crate-sound-builders-cp88/check-attribution.py
    python3 porting/tests/crate-sound-builders-cp88/run-native.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /workspace/shared/nagaport-toolchain/native-classpath.txt --legacy-jar /tmp/nagaport-legacy-client.jar
    python3 porting/tests/crate-sound-builders-cp88/check-mutants.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /workspace/shared/nagaport-toolchain/native-classpath.txt --legacy-jar /tmp/nagaport-legacy-client.jar

`production.patch.gz` contains the byte-exact three-file source patch (deterministic gzip; decompress before ordinary git apply). The isolated source guard is intentionally pinned to CP86; if later independent production changes are composed, do not silently weaken/rebase this proof. Preserve or explicitly account for the independent changes in a coordinator integration receipt.

## Separately delivered CP88 composition adapter

The default `check-source.py` behavior remains the complete CP86 all-production-source inverse. It is not relaxed for an implicitly composed tree. `run-native.py` now requires this source guard before any fixture compilation.

An explicit `--composed-source` option on either script is accepted only through these additional gates:

1. The real `porting/tests/check-native-shoot-sync-machine-source.py` and `porting/tests/native-shoot-sync-machine-source-manifest.json` must exist. The adapter independently checks the manifest SHA-256 against the aggregate script's explicit pin, requires the sealed CP87 baseline `c2f60149e9a9fb836bade505fe566d40a54ef3cc`, and runs the aggregate's `verify` against the current module. There is no fallback aggregate, unpinned override or automatic source substitution.
2. The aggregate must account for all three existing crate-family files. Its complete-file reverse view must equal both their sealed CP87 versions and original CP86 versions. This family was unchanged by CP87.
3. Replaying only the eleven registry substitutions and the two exact builder additions must reconstruct the actual current three complete files. The original family formula/owner checks, exact whole-file inverses, and isolated tested source hashes must pass. Any additional edit within these three files is rejected rather than silently projected away. The unchanged sound classes and reused infrastructure hashes remain required as well.
4. Native fixtures are extracted from the actual current constructor/builder files and actual current registry lines. Reconstructed historical bytes are used only for comparison, never as native fixture input. The same source gate also precedes requested runtime mutation controls.

The adapter was delivered before the final aggregate existed. Its isolated default native rerun still passed 457 native plus 12 original assertions; complete three-file reconstruction and three whole-file drift negatives passed; composed mode correctly refused the missing gate. That refusal is not a composed acceptance result. The coordinator must finalize the pinned aggregate, then run:

    python3 porting/tests/crate-sound-builders-cp88/check-source.py --composed-source
    python3 porting/tests/crate-sound-builders-cp88/check-composition-adapter.py
    python3 porting/tests/crate-sound-builders-cp88/run-native.py --composed-source --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /workspace/shared/nagaport-toolchain/native-classpath.txt --legacy-jar /tmp/nagaport-legacy-client.jar

The earlier expensive attribution evidence is retained, not rerun or relabeled as fresh composed attribution. This adapter changes evidence/runner files only, not production source.
