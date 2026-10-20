# Isolated ItemBlockBase native subset

No main repository writes, commit or push. Base is CP37 main-tree sources.

## Actual implemented behavior

The complete production ItemBlockBase now extends native 1.12.2 ItemBlock, uses its protected `block`, overrides `getTranslationKey(ItemStack)`, keeps the real IBlockMulti subtype/override-name contract, and uses native EnumRarity constants. Fence now implements IBlockMulti in CP37, so its existing post-key override remains reachable without a fence-specific branch. Original display translation still uses real I18nUtil and I18nClient; no test-only localization implementation replaces these.

Native tooltip entry overrides `(ItemStack, World, List<String>, ITooltipFlag)` and dispatches into the existing mod's player callback overload, preserving descendant virtual dispatch and ordinary-then-persistent order. It passes Minecraft's viewing player and flag.isAdvanced(). It does not infer a player from World or drop persistent NBT. `hasKey` semantics and the original persistent compound identity remain unchanged. Null client player outside a world remains possible, not fabricated; arbitrary transitive callbacks are not certified. The provider interfaces were not mass rewritten.

Removed legacy icon and item-color methods. Selected scaffold/fence icon replacement is verified with the actual complete ItemBlockBase and native model loader/mesher, not an anonymous ItemBlock. Native client Item color event registers only audited native exact-class Block/Scaffold/Fence owners under hbm, delegating state metadata and tint index to actual BlockColors. Their inherited original item render color is white; native missing-handler -1 is the correct no-tint sentinel. No hardcoded white function pretends to implement colored plants. Registered nonwhite native callback tests prove dispatch plumbing only, not plant parity.

## Reproduce

All probes now live under `porting/tests/itemblock-native`. Run `python3 porting/tests/itemblock-native/run.py --module MODULE --java-home JDK8 --classpath NATIVE_CP`. Before integration additionally pass `--source-root STAGING`; after integration it defaults to MODULE. The two model probes take these same arguments and self-compile actual root classes into private temporary directories. There are no hardcoded module, JDK, classpath file, or staged classes paths. Original sources are resolved from MODULE/../ntm-original-pristine, consistent with the existing original-oracle suites.

Results: root 149 assertions; scaffold 338,907 assertions and 56,832 native vertices; structural 208,475 assertions plus 8 replacement-owner assertions. Original oracle/resource suites remain unchanged in main. Only their staged test ItemBlock construction now uses actual ItemBlockBase.

See migration-manifest.json for exact production/original hashes, fixture boundaries and remaining gates. The three descendant files are narrow fixes: Lore native block/name/rarity symbols; Remap native block/count; BlastInfo unchanged body and full native overload dispatch test. Lore and Remap now compile completely with declared unrelated NTM identity/field collaborators and execute all Lore special branches/rarities plus Remap count/meta/NBT replacement semantics. Exact original descendant bodies are verified after enumerated native symbol renames. StorageCrate's independent right-click/container migration remains out of scope.

Do not advertise global ItemBlockBase parity or full-mod compilation: production legacy registry root, plant tint callbacks/model tint indices, other owner models and StorageCrate APIs remain open. No current CP37 worktree mutation is required to inspect this result.

Caller audit: see `porting/tests/itemblock-native/REMOVED_METHOD_CALLERS.md` and its raw source-reference inventory for exact unresolved icon/color consumers. No silent assumption that arbitrary Item callbacks are now safe.

LegacyBaseModelHandler regression check: the existing model registry excludes non-BlockBase ItemBlockBase owners; its color code already skipped the old root method and uses the same block render-color fallback. Block item icon routing directly uses the block icon. No adaptation required. See check-legacy-route.py and audit for exact source evidence. ItemBlockColoredConcrete is a fifth, indirect descendant with a separate still-legacy naming gate.
