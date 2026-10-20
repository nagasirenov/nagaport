# Native ModItems registry/call boundary

Baseline: CP51 `077492a6c31c910f34697783f1aa0cfabd8ff88e`.
Production ownership: only `src/main/java/com/hbm/items/ModItems.java`.
Requires the companion native item-foundation change, particularly the real
`com.hbm.render.item.NativeItemTextureRegistry` and native setters in
`ItemCustomLore`, `ItemEnumMulti`, and `ItemAmmo`.

## Applied mapping

- 1,855 ordinary inherited/base setter calls use MCP stable_39 `setTranslationKey`.
- Three `ItemAmmo` setter calls use its actual migrated override, not a bypass.
- 2,075 zero-argument identity getters use native `getTranslationKey`.
- 1,731 executable texture calls become `NativeItemTextureRegistry.texture(receiver, originalExpression)`.
  This evaluates the exact original receiver before the texture argument, retains any outer cast,
  and returns the identical item. No texture names are inferred from registry names, corrected,
  deleted, or deduplicated. `polaroid` keeps its runtime `MainRegistry.polaroidID` expression.
- 102 explicit enum/tab/vanilla field bindings use their native constants: rarity, COMBAT/MISC/TOOLS,
  DIAMOND (the old diamond tool material was named EMERALD), FARMLAND, and GLASS_BOTTLE.
- Comments and every original line terminator remain unchanged: 7,395 CRLF and 15 standalone LF;
  one LF import line is added. Original trailing whitespace is retained, not reformatted.

`migrate.py` tokenizes Java while excluding comments, checks each receiver/cast/argument shape,
performs non-overlapping edits, and reverses every edit to the exact original bytes. It compares
production against the complete expected result, including its SHA-256. The manifest enumerates
all original texture arguments and deferred setters; no unreviewed regex pass over all sources is used.
The original GameRegistry queue and every registration expression/order remain intact except for
native identity getter names.

## Native execution evidence

`run.py` extracts 615 exact current production initialization statements whose constructors are
native Item, ItemFood, or ItemSoup, and their 613 exact registration statements in the original
registration-table order. The two original repeated assignments to powder_zinc and powder_lead
are deliberately retained. This produces 615 texture metadata identities but 613 final fields;
no production deduplication occurs. Model code must ignore the two unregistered superseded items.

The test compiles the complete production GameRegistry, RefStrings, and NativeItemTextureRegistry
against actual Forge 14.23.5.2768/MCP stable_39 classes under JDK 8. Forge's real event subscription
transformer and event bus flush actual Forge registries in Block-before-Item order. Assertions
cover each retained field's constructor type, translation/registry/texture identity, native stack
identity, original registration ordering, creative tab/null, maximum stack size, container item
identity, full-3D flag, and food healing/saturation/wolf flag.

The only non-production dependency fixture is MainRegistry with five distinct real native
CreativeTabs objects. There are no net.minecraft/net.minecraftforge test stand-ins. The generated
ModItems class is explicitly an extracted statement slice, not compilation of the original class.
`native-scope.json` identifies every extracted source statement with its hash and the original
construction and registration ordering. The full native test passes 12,273 assertions for 613
registered fields. No complete mod build, client launch, dedicated server launch, GL rendering,
or full creative-tab behavior is claimed by this test. Companion item-foundation tests own the
native model provider and special-model ownership checks.

## Deliberate unresolved boundaries

Twelve setter calls are retained because the owning overrides have not yet migrated:
ItemModMinecart (1), ItemCraftingDegradation (2 direct plus ItemDefuser, ItemToolingWeapon, and
four ItemTooling calls), ItemBlowtorch (2), and ItemBoltgun (1). They have texture side effects;
renaming callsites alone would silently bypass the overrides.

The local liquidator addArmorMaterial/customCraftingMaterial statements remain for the armor
family migration (native slot order, repair item and newly required material parameters).
The glyphid ItemLemon potion-effect call remains for the food family migration (old API uses
seconds while native PotionEffect expects ticks). All constructor arguments and balance values
are unchanged here. Whole ModItems compilation remains dependent on these and other unported
item-family classes; a coordinator owns full-root compile diagnostics.

## Reproduction

From the project root, using the existing local JDK/classpath only:

```
python3 porting/tests/item-registry-native/migrate.py
python3 porting/tests/item-registry-native/run.py \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)"
```

Before companion integration, add `--texture-source` pointing to its actual production helper.
The captured proof hashes that helper, rather than substituting a stub.
