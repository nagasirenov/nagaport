# Native CT-metal structure placements

Incremental baseline: `ac268a58` (full commit in manifest.json), following the completed 431 concrete and 1,298 sand/light-brick batches. Apply the 1,298 batch first. Neither completed placement set is repeated or modified here.

## Existing visual gate remains open

This is placement-only reuse of unchanged CP49 CT owners/models. Native destruction overlays still lose CTLightQuad midpoint/center interpolation and differ from the original override-material policy. This batch does not accept that difference, approve lighting/pixel parity, activate a new rendering policy, or claim full client screenshots/item rendering. See the pinned sibling ct-native/README.md. All owner/context/loader/model/resource bytes remain unchanged.

## Scope and native dispatch

**1,054** metadata0/flags3 calls in seven generators use the existing DungeonToolbox.placeUnorientedStructureBlock helper. Twelve actual mutable instance fields select precisely four BlockDecoCT owners: deco_steel, deco_tungsten, deco_red_copper and deco_lead. targets.json lists exact field-to-owner mappings. The generators are Barrel, DesertAtom002/003, Ruin002, Silo and Spaceship/Spaceship2. deco_titanium is actually BlockOre and its278 calls are explicitly excluded. Other custom owners and nonzero metadata calls are untouched.

No helper behavior changes: only its Javadoc description broadens. Both existing helper method bodies, including placeRandomConcrete, stay byte-identical. The helper keeps current field identity, receiver/coordinate/block evaluation order, null receiver timing, original pre-chunk bounds, delayed actual native state resolution, fixed flags3 and ignored return values. It does not cache fields, replace them with initializers, reject unknown identities or expose an arbitrary Block/metadata World emulator. The audited call inventory, not a generic type assumption, supports the extension.

## Owners, storage and drops

- Complete actual BlockDecoCT, CTContext, CT and IBlockCT compile unchanged. Four selected exact constructors plus the actual rusty-steel mixed-join partner are compiled verbatim. They preserve Material.IRON, hardness5, resistance10 (native getter6), translation identity, tab identity, allowFortune=false flag state and metadata-zero default.
- **1,572 genuine native checks** cover all16 state round trips and IDs, real World/Chunk writes, no-op/replacement/removal, no tile entities, actual drops/pick, quantity/fortune RNG, and compressed Anvil persistence of80 positions. The original1.7 client executes independent resistance/drop oracles. Only meta0 is selected for placement, although these CT owners genuinely retain all16 metadata values.
- All five registration call sites explicitly choose standard ItemBlock. The fixture performs native block/item registration and assigns the original name derived from the translation key. Full ModBlocks/legacy GameRegistry initialization is not claimed executed.
- The bounded NTM static collaborator has only the real steel/rusty identity slots needed by unchanged canConnect; no block logic or platform API is replaced. Exact original constructor slices instantiate all actual owners. Six prior Generic constructors remain only as support for unchanged Library fixtures, not additional migration scope.
- A fresh reused native CT owner/loader integration run passes **133,065 assertions**, including all30 variants, metadata, mixed joins, actual state/item loader routes, original resource availability and wrong-owner rejection. All64 original CT PNGs compare byte-for-byte. Existing fixture limits remain: renderer singleton initialization is bypassed in the existing integration harness, reactor render hooks and wire/localization collaborators are bounded, and it is not full gameplay.

The old ct-native/owners.py blanket CP48 reactor breakBlock source assertion now fails on legitimate intervening unrelated migrations. reuse-ct-owners.py leaves that frozen script unchanged, verifies its hash and scopes source-mechanics invariance to this batch's complete BlockDecoCT methods. It reruns the existing full native owner/loader tests without claiming unrelated reactor source invariance. It does not relax any CT geometry or model assertion.

## Placement and source proof

- All1,054 use sites are attributed to native World, primitive coordinates and the exact twelve declaring fields; metadata/flags literals and statement-only return use are required.
- **22** attribution fixtures cover all twelve allowed declarations plus local/parameter/inherited shadows, foreign receivers, used results, invalid metadata/flags/types.
- Whole-source conservative lexical closure checks mutable declarations, references, sole exact constructor assignments and absence of field writes/subclasses/external accesses; **nine mutants** are rejected. Reflection/arbitrary external mods remain outside this closure.
- **727 native checks and15,804 ordered events** compare nine complete unsplit selected method sequences at nine coordinate contexts, four permutations of the actual CT owners, null receivers, exact argument/exception timing and six semantic mutants. A native conversion witness checks state resolution occurs once only after bounds/null checks. Counts include rejected-mutant execution, not full-method coverage percentages.
- Actual selected sequence Code sizes: **931,5740,4147,8605,1685,218,707,8466,2273 bytes**. The existing shared helper's original method-size justification remains the preceding604-call Java8 code-too-large failure. These are selected-sequence sizes, not complete generator sizes; whole generator compilation/final sizes remain unverified.
- Seven exact source inverses,1,054 per-site mutation controls and the helper-comment inverse preserve other bytes. Complete DungeonToolbox compiles against the same explicit bounded Library/static-fields/MetaBlock fixtures. No full Library/ModBlocks/game build is claimed.

## Reproduce

From module, using cached Java8/native dependencies:

    J=/workspace/shared/nagaport-toolchain/java8
    CP_FILE=/tmp/nagaport-ae2-native-classpath.txt
    python3 porting/tests/ct-palette-native/check-owner-closure.py
    python3 porting/tests/ct-palette-native/ownership.py --java-home "$J" --classpath-file "$CP_FILE"
    python3 porting/tests/ct-palette-native/migrate.py --java-home "$J" --classpath-file "$CP_FILE"
    python3 porting/tests/ct-palette-native/source-proof.py
    python3 porting/tests/ct-palette-native/owners.py --java-home "$J" --classpath-file "$CP_FILE" --legacy-client /tmp/nagaport-legacy-client.jar
    python3 porting/tests/ct-palette-native/reuse-ct-owners.py --java-home "$J" --classpath "$(cat "$CP_FILE")" --original-root /workspace/shared/nagaport/ntm-original-pristine
    python3 porting/tests/ct-palette-native/prove.py --java-home "$J" --classpath-file "$CP_FILE" --legacy-client /tmp/nagaport-legacy-client.jar

Owner/source archives and reused evidence remain pinned in owner-manifest.json and dependency-hashes.json. The sparse checkout restored only unchanged required tracked PNGs, not new assets or full copies. No Gradle/downloads/source exclusions/platform replacements/push occurred.

Use merge.py --module MODULE to check selected lines; add --apply --receipt RECEIPT.json to compose them and the helper comment. It refuses a tree without the prior helper batch. The merger was tested on the exact incremental baseline and reproduced all eight current production files byte-for-byte. On a later integrated tree, compose its receipt rather than weakening frozen source manifests.
