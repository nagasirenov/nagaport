# Native armor material and repair registration

Baseline CP89: `42dea8e6e3241d0c4ce4ba9c5459fab2bdfdc99b`.
Production ownership is exactly three files: `MainRegistry.java`, `ModItemsArmor.java`,
and `ModItems.java`. No helper, gameplay owner, inventory texture, worn model,
resource, constructor or armor-slot callback changes in this patch.

## Finite production change

31 actual `EnumHelper.addArmorMaterial` calls use the native seven-argument API.
The original names, durability factors and enchantability are unchanged. Damage
arrays change from original HEAD/CHEST/LEGS/FEET order to native
FEET/LEGS/CHEST/HEAD order. Native durability lookup already performs the matching
slot translation: old bases `[11,16,15,13]` become native `[13,15,16,11]`.
Neither multiplication is widened or clamped. Euphemium's factor 15,000,000 gives
165,000,000 / 240,000,000 / 225,000,000 / 195,000,000 durability in original slot
order; it does not overflow. Additional signed-int overflow probes execute both
real engines and demonstrate parity at the boundary.

30 original public repair-Item assignments become assignments to Forge's actual
public `repairMaterial` field with `new ItemStack(exactOriginalItem, 1,
OreDictionary.WILDCARD_VALUE)`. The wildcard is necessary: the original armor
predicate compares only Item identity, not metadata. No repair field is synthesized
for Euphemium. The material's repair stack has no NBT. Public-field assignment,
rather than `setRepairItem`, deliberately retains the old ability to reassign the
repair material without native setter's extra single-write restriction. There is
no added rejection or repair-method override.

All material creation and repair assignment sites stay in place. MainRegistry's
16 static creations precede item initialization; its 15 repair assignments remain
later in PreLoad after `ModItems.mainRegistry()`. The 14 local armor materials
retain interleaved creation/repair order, and Liquidator still follows
`ModItemsArmor.init()`. Two distinct `HBM_TRENCH` enums remain separate with
10 versus 0 enchantability; they are neither renamed nor deduplicated. Aus3's
material and repair assignment remain even though no constructor consumes it.

## Required native parameters and actual consumer audit

`consumer-audit.json` records all 138 original custom-material construction sites,
28 concrete/ancestor owners, exact original/pristine constructor forwarding,
texture callback bodies, and source hashes. The three registration files are the
complete custom-material reference closure.

- Texture name is `leather`, not an invented HBM resource. Every relevant original
  root constructor passed renderIndex 0, and the real original RenderBiped maps
  that index to the vanilla leather resource when no custom texture is supplied.
  Native LayerBipedArmor's fallback is checked against real original RenderBiped
  for every material and slot. The custom enum is NOT the LEATHER enum: native
  leather color/overlay logic remains off. All existing explicit worn textures,
  custom model ownership and inventory texture declarations are byte-unchanged.
- Equip sound is the ordinary native `ITEM_ARMOR_EQUIP_GENERIC`. The old material
  had no equip-sound field. This supplies new 1.12 engine equip feedback, not an
  invented leather/metal sound classification. Existing NTM step/jump/fall sounds
  are untouched. No claim of old/new audio parity is made.
- Toughness is `0.0F`, adding no per-material toughness bonus. 1.7.10 had no
  toughness statistic. Native combat formulas remain an engine boundary; exact
  material damage reduction numbers do not imply complete combat parity.

## Exact repair initialization versus native repair eligibility

The initialization migration preserves the exact repair reference, original
assignment point, mutability and item-only matching for normal positive native
stacks, including nonzero metadata and NBT. It does not establish universal
repair-predicate parity for malformed/empty stacks. Nor does it assert that a
previous furnace-only empty-stack decision grants mod-wide approval.

`repair-engine-boundaries.tsv` executes the original unmodified ItemArmor repair
predicate and native ItemArmor using their genuine IRON materials. This isolates
already-inherited engine behavior rather than a production registration helper:

- Positive canonical metadata 0, 17 and 65535 repair in both engines. Negative
  constructor metadata in the probes is clamped to 0 by both engines and repairs.
- Zero and negative counts retain the original Item reference, so old ItemArmor
  accepts them. Native ItemStack is empty and exposes AIR, so native armor rejects.
- A constructor metadata of Integer.MAX_VALUE keeps the old Item but makes the
  native stack empty. Both public damage setters can restore metadata to zero,
  yet the native constructor's cached-empty bit can still expose AIR while
  isEmpty() now returns false; native armor still rejects that malformed state.
- Setting a previously valid native stack's metadata to Integer.MAX_VALUE via
  the public API leaves its cached item identity intact but makes isEmpty() true;
  native armor rejects it. Restoring metadata through the public API repairs the
  live stack. Restoring a zero count using setCount refreshes the cache correctly.
- An unconfigured original material plus a malformed null-item candidate matches
  `null == null`; native EMPTY repair fallback rejects. Euphemium's intentional
  lack of repair assignment is preserved, not reinterpreted as broad permission
  to change unrelated null behavior.

No native private stack fields are read or written in these tests or production.
These differences come from native ItemArmor/OreDictionary/ItemStack and exist
without the material-registration change. Assigning a count-1 wildcard stack
introduces no additional matching restriction for the actual non-null repair
items. The native repair representation is necessarily a mutable ItemStack,
whereas the old field was an Item; external code may now mutate that stack's
metadata/count. This patch does not add such mutations or recreate the old engine.

## Evidence and limits

- `migrate.py` generates only the 61 finite changes plus seven missing import
  lines, preserving every other source byte and line ending. It proves exact
  whole-file inverse to immutable CP89. `restore_base(path, currentBytes)` is a
  strict integration helper: it accepts only this exact complete result and
  returns CP89 bytes. Historical source guards must compose this inverse instead
  of repinning historical fingerprints. `audit.py` rejects 12 source mutations.
- Original/pristine source proves every selected creation and repair expression
  was inherited unchanged. The archived official Forge1614 ItemArmor patch shows
  customCraftingMaterial's default null and exact getter reference semantics.
- Bounded native javac reports 61 original slice errors (31 old EnumHelper
  overloads and 30 removed fields), then compiles the migrated slices cleanly.
  This is not a full compiler census; the coordinator owns full-root diagnostics.
- `run.py` executes 3,056 assertions with genuine Forge EnumHelper dynamic enum
  creation. Exact production material/repair statements are sliced into a fixture,
  with distinct real native Item objects for the named repair fields. It exercises
  actual native ItemArmor instances constructed before repair assignment. These
  are not complete NTM armor subclasses or complete registry owner initialization.
- Original material constructors, ItemArmor constructors/stat getters and
  RenderBiped fallback methods execute unmodified original-client jar bytecode.
  Java 8 ReflectionFactory invokes the real private original enum constructor;
  it does not invent an enum or implement the formulas. Native creation uses the
  real EnumHelper, not ReflectionFactory in the test. The original unmodified
  client does not contain Forge's custom repair field; that exact old field
  extension is audited from official Forge1614 source, not simulated in bytecode.
- No platform stubs, broad common build, new download, install, dependency
  regeneration, client/server launch, GPU draw, full gameplay or audio acceptance
  is claimed. Runtime inputs and hashes are in `runtime-inputs.json`.

## Reproduce with cached official inputs

From the project root:

```
python3 porting/tests/armor-material-registration/migrate.py
python3 porting/tests/armor-material-registration/audit.py
python3 porting/tests/armor-material-registration/run.py \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath-file /workspace/shared/nagaport-toolchain/native-classpath.txt \
  --legacy-jar /tmp/nagaport-legacy-client.jar
```
