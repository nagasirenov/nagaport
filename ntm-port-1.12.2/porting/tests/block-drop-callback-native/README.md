# Native metadata-independent block item callbacks

Baseline: CP65 `5e6ba284ef32b46ad5f81cd1e1570c08b53cb24f`. This is a bounded callback migration, **not** full owner, full mod, launched-client, world-save or gameplay acceptance. No lighting policy changes are made.

## Production scope

- 83 existing `getItemDropped(int, Random, int)` declarations whose first parameter is not used in executable code become direct native `getItemDropped(IBlockState, Random, int)` overrides. The parameter name, every body byte, all random calls, item/null results, and RNG/fortune arguments are retained. No metadata is extracted, normalized or invented.
- GenericDrop gains its missing `@Override`; the others retain their existing annotation. No fake platform API, compatibility facade, new domain hook, dropped override, or Item.AIR substitution is introduced.
- Four internal call arguments become native states: SellafieldOre fortune and XP (2), StepEffect fortune (1), and Outgas fortune (1). All retain one virtual item callback invocation, identical receiver/RNG/fortune, and the original argument evaluation order. These owners have no affected scalar overriding descendant. Sellafield already has a complete 0–15 state property; StepEffect/Outgas use native metadata-zero state, and the selected item callbacks ignore metadata. Nothing here adds state properties to BlockMulti or BlockBase.
- Total: 84 production paths, 83 callbacks. The immutable selection, exact replacements and baseline archive are in `selection.json` and `baseline.zip`.

Eleven scalar declarations are deliberately untouched: eight metadata-consuming methods (Crop, Remap, Cap, DepthOre, ModDoor, NTMFlower, OreBasalt, Stalagmite), existing native-dispatched Ore/Cluster hooks, and RubberFarm's cross-owner delegation. DepthOre only uses metadata in a super call, but it is excluded rather than assuming that call safe. Eight selected BlockEnumMulti-family methods were initially considered as a separate batch; source inspection rejected that generalization because BlockMulti does not currently preserve arbitrary metadata. Only the metadata-unused subset is included here.

## Hierarchy and call closure

`source-proof.py` scans the entire source tree with comments and literals removed while retaining positions. It resolves explicit imports and fully qualified parent names, including native BlockDirt versus NTM BlockDirt. All current item-callback declarations are top-level, so no hidden anonymous/nested scalar override is bypassed. The 24 descendants of selected roots declare no item callback and inherit the new native override. No anonymous selected-block construction was found. Full results, all callback occurrences and the qualified class graph are in `attribution.json`.

The selected roots include GasBase, ChargeBase, MultiSlab, DummyOldBase and GraphiteDrilledBase. Their unmodified descendants are not falsely claimed to compile as whole classes. Existing generic Block-typed legacy callers in ModBlocks, Remap, ItemGlitch and four machines remain separate pre-existing migration roots. Native falling-block callers now dispatch the correct selected callback. No scalar API used by a current supported source caller was silently retained with a wrong native dispatcher.

## Reproduce

From `ntm-port-1.12.2`:

    python3 porting/tests/block-drop-callback-native/source-proof.py
    python3 porting/tests/block-drop-callback-native/run.py --java-home /path/to/java8 --classpath-file /path/to/native-classpath.txt --legacy-client /path/to/original-client.jar --mutants

The original client must have SHA256 `a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce`. No downloads, dependency installation or full Gradle build is required. Normal runs compare immutable source evidence, not regenerate a manifest. Bytewise forward and reverse proofs retain original CRLF lines; ordinary `git diff --check` flags those inherited CR characters and is not claimed clean.

## Executed evidence

The final test passes **1,600,490 assertions**, **268 identity/field cases**, four behavioral mutation controls and one compile-attribution control.

1. Every one of the 83 complete production callback methods compiles against real native Block/Item APIs in an explicitly bounded method fixture. Entire method bodies are extracted, not hand-reimplemented. Seven entire production files also compile: BlockNoDrop, BlockGenericDrop, BlockGeneric, BlockBase, RefStrings, IIcon and IIconRegister. No claim is made that the other 81 complete owner classes compile.
2. Every original counterpart is taken from the supplied original-pristine source. Exact source parity is checked against CP65, allowing only the already-applied uppercase vanilla Items/Blocks member spelling. Original methods compile as true overrides of **the actual 1.7.10 aji (Block)**, using actual old adb (Item) objects, Block/Item registries and ItemBlock objects. Only compiler-symbol bindings are translated to the original obfuscated names; no fake Minecraft/Forge class is supplied. Two nested class loaders keep unsigned test fixtures separate from the unchanged signed original JAR. The original JAR is never rewritten or stripped.
3. ModBlocks and ModItems tables are explicit test-only identity slots, not a full-mod registry initialization claim. Each referenced identity branch is tested both selected and unselected; slab `single` is tested null/non-null; GenericDrop's target is distinct from itself; lamp `getOff` methods are complete original methods. Fourteen raw old metadata values, six fortune values and 32 RNG seeds check item/null identity and exact RNG draw widths/post-state. The native state argument is deliberately varied between own/foreign states to prove the selected body does not inspect it.
4. Seven complete unchanged original helper/quantity methods are independently frozen in `original-collaborators.json`. Native StepEffect and Sellafield fortune methods and the genuine Outgas→NetherCoal method hierarchy compile and execute against actual Block dispatch. Fortune on/off, negative/zero/positive values, output multiplicities and RNG post-state are compared with actual original superclass execution.
5. Actual native `Block.getDrops` executes all method fixtures. Native and original item/count/RNG results agree for observable nonempty stacks. This is not a claim about each original owner's other drop-damage, custom getDrops, tile, growth, explosion or outgassing callbacks.
6. Complete production BlockNoDrop and BlockGenericDrop execute actual World.setBlockState, Chunk, World.destroyBlock and EntityItem creation. The null owner spawns nothing; GenericDrop spawns its configured item. A preloaded real-chunk World fixture bypasses lighting calculation and captures EntityItems. Complete-parent derived/super callbacks and Outgas's derived fortune callback are counted to reject missing/duplicate dispatch.
7. Actual original and native vanilla stone/dirt metadata-zero and five ore owners provide independent item/fortune/RNG checks. Nonzero old stone metadata is intentionally not asserted equivalent to 1.12's newly meaningful stone variants.
8. Mutants changing the fire RNG bound, bypassing virtual fortune dispatch, duplicating the item callback, and replacing null by self-drop are all rejected. Restoring the old scalar signature is rejected by native javac `@Override` attribution.

## Important boundaries

- Native Forge Block.getDrops can contain an **empty ItemStack** when the preserved callback returns null; the original null-filtered list need not contain an entry. This migration preserves the exact original callback result rather than silently substituting AIR. Tests compare the observable nonempty drops, separately check empty entries and execute actual no-drop world destruction. Raw list representation parity is **not claimed**.
- Sellafield's old `getExpDrop(IBlockAccess, int, int)` remains an unported XP lifecycle method with its pre-existing obsolete MathHelper member. Its item-call argument change is byte/source-proven but that whole XP method is **not native-compiled or runtime-certified**. Its unchanged XP formula is not claimed migrated here.
- Other old metadata, quantity(metadata,...), getDrops, harvest, renderer, growth, gas and machine callbacks remain unchanged and can still block whole-owner behavior. This patch establishes genuine native item callback dispatch only, with the stated fortune closure.
- Full production compilation and total diagnostic delta belong to the parent's integrated checkpoint. No full source build, mod JAR, client startup or full gameplay result is claimed by this evidence.

## Composition

The production Git patch targets the exact CP65 baseline. Main already has an independent BlockChargeBase edit. `apply-fragments.py` is supplied for that overlap: it checks every old fragment exactly, constructs all changes before writing, and proves its inverse restores every other byte. It supports a dry run (default), `--write`, and a JSON `--receipt`; rerunning recognizes the already-applied fragments. It never changes a baseline to make an audit pass. The recorded main dry run validated all 84 paths without writing main.
