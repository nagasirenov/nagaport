# Native per-stack Item translation hooks, bounded CP87 batch

Baseline: `fc2cd2dba34caba0dba74ed288f95107ab4dc979` (sealed CP86).
Target: real Minecraft 1.12.2 / Forge 14.23.5.2768, stable_39, Java 8.

## Production scope

Exactly eleven existing `getUnlocalizedName(ItemStack)` hooks become actual native
`getTranslationKey(ItemStack)` overrides:

- ItemModMinecart, ItemBlockNamedMeta, ItemBlockColoredConcrete.
- ItemAmmoArty, ItemAmmoHIMARS, ItemMineralOre, ItemAutogen.
- ItemMetaUpgrade, ItemGear, ItemStampBook, ItemCrayon.

ItemAutogen's same-contract `super` call changes with its declaration. No caller
is redirected to a zero-argument getter. The distinction between ItemGear's
virtual `this` no-argument call and the other explicit `super` calls is retained.
All keys, prefixes, suffixes, locales, metadata operations, table order, nullable
override behavior, control flow and error behavior remain unchanged.

ColoredConcrete's removed `ItemDye.field_150923_a[index]` becomes
`EnumDyeColor.byDyeDamage(index).getTranslationKey()`. The existing production
`BlockConcreteColored.func_150032_b`/`func_150031_c` methods remain byte-identical:
the index is always `~metadata & 15`. Consequently the enum's out-of-range
fallback is unreachable, including for negative and extreme signed metadata.
All sixteen actual original array values equal the native enum keys, including
`lightBlue` and `silver`; this is not the enum's underscore-separated model name.

No setter, registry, texture, resource, model, armor recognition or dynamic
ItemStack naming caller is changed. No null/empty-stack normalization is added.
The existing IBlockMulti stack-name methods are a separate block-side contract
and are deliberately untouched.

## Evidence

Run from the module root, using only cached tools/dependencies:

    python3 porting/tests/native-item-stack-key-hooks/run.py --attribute

- Exact byte-for-byte expected-transform guards on all eleven production files,
  including unchanged bytes outside the selected substitutions. The original
  pristine hooks agree with the baseline hooks after already-completed naming
  aliases. Eleven unchanged ancestry/helper/excluded-owner files are hashed.
- Whole 4,066-source javac attribution establishes eleven actual Item overrides
  and twelve family classes. ItemScraps is the only owned descendant and inherits
  ItemAutogen's override unchanged. All relevant no-argument and stack-argument
  super destinations resolve to genuine native Item or ItemBlock methods.
- Attribution errors are 11,787 before and 11,775 after. These are compiler
  attribution diagnostics, not a clean build, distinct defect count or launch.
- 19,911 passing assertions execute complete selected hook bodies against actual
  native Item/ItemStack/ItemFood/ItemBlock and actual archived-client classes.
  They cover every production variant, ten raw base keys (null, empty, unusual
  prefixes, Unicode, spaces), signed metadata and integer extremes, explicit
  null-hook behavior, arithmetic overflow, null/empty arrays and null elements,
  virtual versus super dispatch, the real inherited ItemScraps path, the actual
  live ItemStack entry point, and all sixteen original dye keys.
- Complete unmodified ItemBlockMeta and the complete migrated ItemBlockNamedMeta
  compile together against the native classpath and execute with a virtual Block
  key provider. This whole-owner claim is limited to those two small classes.
- All other owners are explicitly named Native*/Legacy* **complete-hook
  projections**, not full-owner compilation. They include the entire hook body
  and its naming read surface. Artillery/rocket/mineral table fixtures retain
  every actual initializer's name and index, but do not execute their gameplay
  constructors, impacts, graphics or registration side effects. Their fixture
  manifest makes that boundary explicit. No platform Item or ItemStack stand-in
  is used. Native ItemCrayon really extends native ItemFood; its legacy projection
  really extends archived ItemFood (`acx`).
- Forge 1614 Item/ItemStack/ItemBlock/ItemDye patches are retained and hashed.
  They do not replace the relevant naming methods or the dye-name array. Forge's
  old ItemStack damage getter redirects through Item.getDamage; its base getter
  reads the same raw field as the archived vanilla getter. The selected owned
  hierarchy has no custom getDamage override. The runtime compares the base
  metadata behavior, not arbitrary third-party metadata interceptors.

## Explicit rejection and remaining gates

ItemBookLore is entirely unchanged from CP86. An actual original/native control
found a pre-existing semantic mismatch for malformed `k` tags:

- Original NBTTagCompound.getString with integer `k = 73` returns `"73"`, giving
  `book_lore.73` in the unchanged original hook.
- Native NBTTagCompound.getString returns `""`, giving `book_lore.test` in the
  baseline's native expression.

This batch does not hide that difference, migrate the hook while claiming full
NBT parity, or broaden into NBT-format compatibility work. A future bounded fix
must resolve this before migrating the excluded BookLore hook.

Positive-count stack dispatch is tested. Raw metadata is injected into real
stacks to exercise integer arithmetic without constructor/setter clamping. Values
above 65535 retain a synthetic cached identity and are only hook/fixture evidence,
not proof of normal invalid-stack dispatch. Zero/negative-count stacks are
used only for direct explicit hook calls, not as evidence that old and native
ItemStack identity/dispatch agree: native getItem can select AIR for an empty
stack. Those dynamic callers, including invalid-metadata callers, remain deliberately
unmigrated.

No full Gradle build, complete owner gameplay, client/dedicated-server launch,
rendering or whole-mod acceptance is claimed. The coordinator owns those gates.
