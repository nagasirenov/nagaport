# Four texture-side-effect naming families

Baseline: `d5ef20a8f1cb65fbd06faa86a4901870c8bab3f2`.
Target: genuine Minecraft 1.12.2 / Forge 14.23.5.2768, Java 8, stable_39.
This is the finite custom-setter exclusion recorded by the historical
`native-translation-name-chains` proof, not another migration of its completed
fresh-name tokens.

## Production scope

Exactly five production files, with all other bytes retained:

- Four owners: ItemBlowtorch, ItemBoltgun, ItemModMinecart, ItemCraftingDegradation.
  Their override and super invocation use native `setTranslationKey(String)`.
  Each still calls the base first, then declares exactly `hbm:` plus the **raw
  supplied parameter**, then returns `this` as Item. No naming or texture argument
  is normalized, recomputed from a getter, dropped, or moved into callers.
- Twelve ModItems calls use that virtual native setter. Four ItemTooling calls,
  ItemToolingWeapon, and ItemDefuser inherit the ItemCraftingDegradation override.
  All three descendant files remain byte-identical. All constructor arguments,
  following builders, wrapping helpers, registrations and variable assignments
  retain their original order. In particular wrench_archineer's later explicit
  `hbm:wrench_archineer_hd` declaration still replaces its automatic declaration.
- Two ItemModMinecart icon-prefix readers consume the same explicit native texture
  declaration, preserving all four bases and five overlays in their original
  order. Its existing layered icon callbacks remain the model owner: the native
  single-texture fallback correctly declines it. No cart base image or generated
  single-layer model is invented.

The existing NativeItemTextureRegistry and NativeItemTextureModelHandler were
read alongside their item-foundation-native proof before reuse, and remain
byte-identical. The helper's validation adds no rejection for this setter:
`this` is nonnull and concatenating `"hbm:"` with any String (including null or
empty) always produces a nonnull, nonblank texture declaration. Actual production
names map to the same valid resource paths. A later explicit declaration and a
later renaming retain the original last-write ordering.

## Evidence

Run from the module root:

    python3 porting/tests/texture-side-effect-naming/run.py --attribute

The runner uses only the existing cached classpath and Java 8; it fetches nothing.

- Exact source-inverse guard for all five files against the sealed baseline,
  including CRLF preservation in ModItems. The four original setter bodies also
  match ntm-original-pristine exactly before transformation. Unchanged descendant
  and helper hashes and the 20 final original PNG hashes are in manifest.json.
- Full 4,065-source compiler attribution in before.tsv and after.tsv verifies
  the seven-class closure, all twelve unchanged receiver/argument expressions,
  exact custom/inherited destinations, and all four native Item super calls.
  Native error diagnostics are 12,887 before and 12,873 after: 14 fewer on this
  isolated baseline. These are attribution observations, not a full build or
  distinct-defect count.
- 491 native runtime assertions pass. The probe compiles **exact extracted production method bodies**
  into explicitly bounded owner shells on genuine Minecraft/Forge. It compiles
  complete unmodified production texture/model helpers, RefStrings and icon
  interfaces. There are no fake platform classes or NTM collaborator stubs.
  Constructors and unrelated owner methods are intentionally not compiled or
  simulated. All seven family inheritance paths, native Item return/raw key
  identity and prefixes, null/empty/unusual parameters, repeated naming, explicit
  texture overwrite, subclass interception and fluent evaluation order are tested.
- The oracle compares raw results with the actual original client's obfuscated
  Item name setter/getter and texture setter/getter, not a hand-written legacy
  platform fixture. It uses the same exact original setter source sequence.
- The actual production NativeItemTextureModelHandler eligibility predicate is
  exercised with registered representatives of all seven classes. The real cart
  registerIcons body and enums produce the original nine ordered layers. All 20
  final PNGs decode successfully and match pristine source byte for byte.

## Explicit limits

This certifies naming/texture declaration behavior and its consumers, not whole
owner compilation, full ModItems initialization, complete item gameplay, a client
or dedicated-server launch, GPU rendering, or visual acceptance. The owner shells
are method-slice scaffolding and must never be presented as runtime proof of
constructors or unrelated gameplay methods. Existing item-foundation-native event,
baking, model-ownership and reload evidence was audited but is not claimed as new
assertions here.

ItemModMinecart's legacy stack-name, creative, gameplay and layered icon callback
integration still has native compile/integration gates, as do other unrelated
methods on these owners. The cart remains excluded from generic model replacement;
this patch does not disguise those gates by dropping callbacks, layers or assets.
No resources, mechanics, recipes, balances, registry declarations or build
exclusions change. The coordinator owns integrated full-build and client gates.

Use `git -c core.whitespace=cr-at-eol diff --check` for the preserved CRLF source.
