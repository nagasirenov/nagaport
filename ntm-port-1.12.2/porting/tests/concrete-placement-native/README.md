# Native static concrete placements

## Historical scope notice (CP75, 2026-10-20)

This README records the initial CP54 inline-placement slice, not the current remaining-work list. The 917-call Silo.generate_r0 exclusion below was subsequently resolved by the narrow existing DungeonToolbox.placeRandomConcrete helper. See `../silo-concrete-native/README.md`, current `src/main/java/com/hbm/world/generator/DungeonToolbox.java`, `src/main/java/com/hbm/world/dungeon/Silo.java`, and `porting/status.md`. CP75 d3e012b1ee72c0170cb8e4ff89bfabadb25ac553 already contains that fix. Do not migrate or claim those917 calls again. The historical original code-too-large rejection remains genuine evidence; full surrounding generator/gameplay readiness remains unclaimed. Use the latest frozen compiler log, not historical exclusions, to choose new work.

Pinned input CP54: `0732d318302d807ce8579878a0cdde6b18bbba86`.
Production changes: **1,595 statements in five existing dungeon files**, six original method groups. No block owner, metadata property, Library helper, item registration, renderer, asset, build setting, or dependency changed.

## Selection and preservation

Actual Java 8 attribution proves native World receiver, primitive int arguments, statement-only ignored return, and the public static zero-argument `com.hbm.lib.Library.getRandomConcrete` executable. Only literal metadata **0** and flags **3** qualify. The original static helper returns four exact BlockGeneric instances; see the sibling `generic-metadata-native` owner audit/probe. It is not a public virtual random selector, arbitrary ModBlocks field or generic custom block assumption.

Native state conversion of all four existing owners round-trips metadata 0 without new properties. Nonzero raw metadata does NOT round-trip and is not in this patch. DetCord's partial state override blocks a safe blanket BlockBase metadata property; that change was deliberately not made.

Every replacement captures receiver, x, y, z, concrete selector result, metadata and flags exactly once in original left-to-right order. It retains null timing after argument evaluation and the original pre-chunk coordinate bounds before state conversion. Original random selection, including a draw at invalid coordinates or before a null-world exception, is preserved. Direct native `setBlockState` keeps native engine lifecycle behavior; there is no World compatibility helper or forged platform API.

## Real method-size exclusion

Original candidate set was 2,512. Java 8 actually rejected the group of **917** expansions from `Silo.generate_r0` with `code too large`. That complete method remains unchanged, not split artificially in production or the probe. The final six selected method groups compile without splitting selected statements across artificial methods. The preserved `method-size-rejection.log` records the original failure. This is still selected-expression compilation, not compilation of entire surrounding generators; remaining unrelated unported statements can impose further whole-method constraints when those owners eventually compile.

## Runtime evidence

`prove.py` compiles the actual complete BlockBase, BlockGeneric, RefStrings and icon interfaces, four verbatim concrete construction statements, and the exact Library random field/helper body. The helper's only fixture namespace change is `ModBlocks.` to `ConcreteConstructionSlice.`; generated placement statements likewise name that helper slice. MainRegistry's creative-tab/logging/random fixture supplies construction context only, not content behavior. Actual Forge registries assign distinct real block identities.

For every selected original method group, the entire selected native statement sequence compiles as one method. Independent original-expression calls and generated native calls run at nine coordinate contexts (ordinary, negative, bounds, invalid Y and integer overflow), resetting the actual Java Random seed before both paths. They compare full ordered coordinates/block IDs/metadata/flags and resulting Random state. Null-world paths compare exception and Random consumption too. The original bounds oracle executes the exact 23-instruction prefix extracted from the pinned original client `ahb.d` method, stopping at its chunk boundary.

Result: **308 checks**, **5,637 ordered placement events**, **six rejected behavioral mutants**, plus **13 attribution fixtures**. The count includes adversarial and mutation checks. Mutants cover null timing, X and Y upper bounds, flag evaluation, duplicate selector evaluation and wrong flags. Native World subclasses record the selected writes; actual World/Chunk storage, compressed persistence, models and drops are separately exercised in `generic-metadata-native`. Original full World/Chunk execution, complete generators and in-game world generation are not claimed.

## Reproduce

First run the sibling `generic-metadata-native/README.md` owner/source probe; its frozen owner/helper evidence is a prerequisite, not supplied by placement attribution alone.

From the module, with the supplied Java 8 JDK and cached native classpath:

    python3 porting/tests/concrete-placement-native/ownership.py --java-home "$J" --classpath-file "$CP_FILE"
    python3 porting/tests/concrete-placement-native/migrate.py --java-home "$J" --classpath-file "$CP_FILE"
    python3 porting/tests/concrete-placement-native/prove.py --java-home "$J" --classpath "$(cat "$CP_FILE")" --legacy-client "$LEGACY"

`migrate.py` default reruns ownership attribution against archived complete CP54 inputs and regenerates every replacement, demanding exact changed source bytes. `--apply` only accepts exact CP54 inputs. `selection.json`, `sources.zip` and hash manifests preserve that input and exact edits. No full Gradle build is invoked.

For integration into a later tree:

    python3 porting/tests/concrete-placement-native/merge.py --module MODULE
    python3 porting/tests/concrete-placement-native/merge.py --module MODULE --apply --receipt RECEIPT.json

The merger requires each selected original line to be unchanged, preserving prior unrelated edits through an exact inverse receipt. Source verification on a later integrated tree must compose that receipt, not replace the original pinned assertion with a weaker one.
