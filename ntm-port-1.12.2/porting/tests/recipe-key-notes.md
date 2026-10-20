# Complete native recipe-key root

## Scope and source provenance

Complete production `RecipesCommon.java` is compiled unchanged by the probe against the actual Forge 1.12.2-14.23.5.2768/MCP stable_39 platform. It includes both array helpers and the full AStack, ComparableStack, NBTStack, OreDictStack and MetaBlock definitions. No extracted key implementation, fake Minecraft/Forge API, production stub, source exclusion, balance change or old-API shim is used.

Baseline checkpoint18: `2cc79304086a82ddb5af1c32e74024b2db2ba05e`.
- Original whole-source SHA256: `7ea7aacf6803d4f78c6fc5f2633637ddcde50661cf60595471cb98d948a9d050`.
- Final native whole-source SHA256: `d10c390506e248b05e3eed10a60521e6ec4050a8b75d7187cbcddad3ff9179b2`.
- `recipe-key-source-manifest.json` stores exact before/after UTF-8 hashes and reversible character-offset edits. `check-recipe-key-source.py` exports `FILES` and `reverse_source(relative, text)` for the parent's historical audit integration. Known source drift fails closed; unrelated paths pass through. Six mutation checks include String-hash, singular-count, exact-class equality and NBT-alias changes. Neither the delivered source audit nor runner requires Git, a local original checkout, or private baseline objects. No reversed obsolete source is compiled.

API changes: native count getter/setter, NBT getter/setter/key set/isEmpty, getTranslationKey, Item.REGISTRY and Block.REGISTRY. Both registry hashes deliberately use registry membership lookup followed by ResourceLocation.toString().hashCode(), not ResourceLocation.hashCode() or item.getRegistryName(). Registered names keep the original String hash and Java signed-int overflow. Dynamic numeric IDs remain only in ordering, as before.

## Preserved contracts and anomalies

- Array helpers retain nullable arrays/holes. copyStackArray copies stacks/NBT; objectToStackArray intentionally aliases accepted ItemStacks and filters nonstacks. Native EMPTY remains a nonnull empty object, not a substituted nullable hole.
- Public domain stacksize remains an arbitrary signed int. Constructor/copy fields, metadata and enum ordinals, makeSingular's same-object mutation, ordering priorities, fixed-list plain extraction and mutable ore extraction remain.
- ComparableStack strict-class equals retains wildcard metadata equality, even though wildcard/specific hashes differ. Ordering still ignores size. NBTStack inherits equality/hash that intentionally ignore NBT values, while remaining a distinct class from ComparableStack. The resulting wildcard map anomaly, NBT-key replacement and mutable-key cache invalidation are tested, not repaired.
- NBTStack.withNBT and ItemStack construction retain NBT aliases. Key copy and toStack deep-copy tags. Matching is a top-level required-key subset with exact tag type/value and exact nested compounds, not recursive subset matching. Null/empty constraints remain distinct stored values. Additional top-level tags are ignored. NBTStack(null) still throws unlike ComparableStack(null).
- ComparableStack(null) still uses ModItems.nothing. Null/unregistered Item hashing still mutates the key to nothing, retains distinct logging and stack-dump paths, and honors the silent config. Unknown Block hashing has no such fallback and still throws without mutating the key. Native actual Forge registry tests cover both named-but-unregistered and unnamed Items/Blocks.
- OreDictStack.toStacks retains the live dictionary list. Ordered extraction copies entries, sets requested counts, delegates wildcard entries to the proxy, and preserves returned proxy aliases/counts. Empty cycling uses ModItems.nothing; a nonempty display with cycle=0 still throws. No client GUI display behavior is claimed.
- Singular HashMap tests cover item/meta separation, count/NBT independence and cached zero results. The thermal-family probe separately compiles and executes the complete actual FuelHandler cache; this isolated runner does not replace that integration test.

## Native EMPTY and nonpositive domain-key boundary

Native empty stacks expose AIR/0 through getItem/getCount even when a separately allocated zero/negative stack internally retains another item and signed count. Converting such an already-depleted stack to a key is necessarily lossy through public native APIs. No reflection, private-field recovery, invented replacement item or clamped balance rule is added. Direct key fields retain their signed count. Tests show independent nonpositive materialization, NBT retention, and later native resurrection of a separately allocated stack; no shared ItemStack.EMPTY is modified.

All three matchesRecipe implementations now treat native EMPTY as absent alongside null. This avoids native OreDictionary.getOreIDs rejecting empty input when size is ignored or a requirement is nonpositive; valid positive stack matching is unchanged.

ComparableStack.getDictKeys preserves virtual toStack dispatch for positive keys. For nonpositive domain-key counts it uses copy(1).toStack(), without mutating the key or caller-owned stack; the original dictionary lookup did not depend on count. A null or native-empty result returns no names before calling OreDictionary. A positive virtual toStack override returning null retains the original Forge1614 no-keys contract and has a dedicated regression test. This preserves count-independent registered identity/meta lookup for built-in ComparableStack and NBTStack, including negative counts and NBT aliases.

Pinned original Forge1614 getOreIDs source is recorded in `../continuation-2026-10-19/recipe-key-original-ore-evidence.log`, including the official source URL and artifact/file hashes. It reads item identity, registry name and getItemDamage, not count or NBT directly. Registry inheritance/native wrapper evidence is in `recipe-key-registry-evidence.log`: Forge1614 inherits the original reverse-name map returning null for unknown identities; the actual native Forge wrapper likewise delegates to ForgeRegistry.getKey. Vanilla's defaulted registry superclass alone is not sufficient evidence for the Forge runtime contract.

Known subclass source inspection: in addition to NBTStack, the only production direct ComparableStack subclass is RBMKRodDisassemblyHandler.ComparableStackHeat. Its toStack only decorates yield/xenon/core/hull NBT; ItemRBMKRod's setters only write those double tags and ItemRBMKRod has no getDamage/getMetadata override. Its original ore IDs therefore do not depend on that decoration, including a hypothetical externally-mutated nonpositive key count. Its constructors create size1 keys, and no direct getDictKeys call on this subtype exists in the inspected source. This is source-derived equivalence, not a full RBMK/NEI compile or execution claim. Inspected untouched source snapshots:
- `src/main/java/com/hbm/handler/nei/RBMKRodDisassemblyHandler.java`: SHA256 `65b9b8c05ba692f9dce4edcefc655c11ce19afe6468d89ac92e542989734fc92`.
- `src/main/java/com/hbm/items/machine/ItemRBMKRod.java`: SHA256 `6ac8bf81d1115746b6379b02a347b9e5d122ac4009c11c052d1bc5a3f3922c15`.

Arbitrary external nonpositive-key subclasses with custom toStack/copy behavior are not claimed universally identical: inherited copy(1) may drop an unknown subtype. Positive override dispatch is explicitly exercised using a controlled metadata-changing subclass and preserved exactly. Do not broaden this into a general old-ItemStack emulation layer.

## Reproduction and evidence

From the project root, with JDK8 and the platform classpath supplied by the target environment:

    python3 porting/tests/check-recipe-key-source.py
    python3 porting/tests/probe-recipe-key.py --java-home JAVA8 --classpath NATIVE_CLASSPATH

Local completed command used `/workspace/shared/nagaport-toolchain/java8` and the contents of `porting/continuation-2026-10-19/native-probe-classpath.txt`. The runner uses explicit UTF-8, native .exe selection and os.pathsep. On Windows supply the local JDK and semicolon-delimited classpath; Windows itself has not been executed.

Final native run: 1,055 assertions PASS, exact source audit PASS, complete RecipesCommon compile PASS. `../continuation-2026-10-19/recipe-key-native-checks.log` records the final source hash. Diagnostic error messages in that log are intentional tests of the preserved logging branches, not test failures. Unchecked/deprecation warnings are original API shape, not replacements.

Only three narrow NTM collaborators are supplied: GeneralConfig.enableSilentCompStackErrors; ModItems.nothing; MainRegistry.logger (actual Log4j) and a controlled proxy.getSubItems. These do not claim full registration, localization, proxy, client display, complete recipe families or mod/gameplay acceptance. No full Gradle build was run by this workstream; the parent owns the common build and source-input checks.
