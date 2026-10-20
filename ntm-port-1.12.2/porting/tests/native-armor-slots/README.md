# CP90 native armor constructor and slot-consumer root

Frozen baseline: CP89 `42dea8e6e3241d0c4ce4ba9c5459fab2bdfdc99b`.

## Production boundary

42 existing files receive 104 finite edits; one NTM-domain helper, `ArmorSlotOrder`, is added. This is not a Minecraft/Forge compatibility shim and does not preserve an obsolete platform API.

- All eight direct ItemArmor constructor roots retain their NTM integer input signatures, material arguments, render index 0, constructor-tail order and other arguments. Only their native super-call maps head0/chest1/legs2/feet3 to HEAD/CHEST/LEGS/FEET.
- All 152 source constructions retain the complete original arguments. 148 pass a literal 0..3; four gas-mask creations have the original fixed-head no-argument constructor. All 24 intermediate constructor forwards remain exact. There are 32 owned ItemArmor-lineage constructors in total.
- 51 actual ItemArmor.armorType field references are converted back to the NTM head-first domain at the consumer. This keeps UUID table indices, radiation coefficients, servo decisions, chest-only dosimeter gating and inventory-render branch selection unchanged. The 52nd field reference is Dummy's direct native armor slot.
- All 39 owned native hooks really override Item: 24 getArmorModel, 13 getArmorTexture, two isValidArmor. Model hooks accept the prepared native default model, preserve null-as-fallback, use the original model-array order and retain original cache allocation and every per-model type. The Liquidator super-call forwards the native slot/default model. Four explicit hook calls use the native descriptor; the manual jetpack call supplies null for its unused new default-model argument.
- EntityDummy's concrete EntityLiving equipment write remains virtual, retains the complete RHS/copy expression and writes the same piece slot. No generic EntityLivingBase/player setters are changed.
- The armor table's native callback maps its head-first row ordinal. Its physical inventory index becomes mainInventory.size()+3-i: native InventoryPlayer appends offhand, so getSizeInventory()-1-i incorrectly starts at offhand. Row positions/order and the surrounding validity method are unchanged.

The helper accepts only the bounded four-piece armor domain. It deliberately rejects hand slots and invalid integers rather than claiming to recreate obsolete platform out-of-domain behavior. Ordinary in-repository constructor and callback inputs are exhaustively audited. Material registration is a separate sibling root; no ModItemsArmor, ModItems or MainRegistry source is edited here.

## Three different slot domains

Constructor/model/mod ordinals are HEAD0/CHEST1/LEGS2/FEET3. Native armor inventory indices are FEET0/LEGS1/CHEST2/HEAD3. The integer slots of ISpecialArmor remain the latter domain in both versions and are not transformed. In particular, original ArmorAsbestos.getArmorDisplay really returns 3/8/6/3 in inventory order; this migration does not silently correct or reverse it.

All model implementation files and original renderStandard geometry/texture/draw bodies are unchanged. Only the argument reaching the existing head-first render method is mapped. The previous native-armor-models evidence remains a separate bounded GL/model test, not a full visual-acceptance claim for this batch.

## Source and compiler evidence

- source-manifest.json and check_source.py: exact complete-file inverse to immutable CP89 for all 42 edited files, pinned complete helper, 126 drift controls, 127 protected registration/model/domain files. No broad replacement or body pruning is permitted.
- ArmorSlotScanner.java / before.tsv / after.tsv: independent full-tree native javac attribution, without emitting mod classes or invoking a whole-mod build. Every selected field resolves to native ItemArmor.armorType; all 39 methods are genuine native overrides after migration; every direct super-call resolves to the genuine native ItemArmor constructor; all 75 helper calls resolve its exact typed methods.
- constructor-domain.json: full compiler-attributed construction and forwarding closure, not only diagnostic-line matches. The owned NTM public constructor signatures and every argument remain unchanged.
- Full-tree analysis errors fall from 11,151 to 11,052. diagnostic-delta.json identifies 99 removed errors and no newly exposed errors, ignoring line offsets. This is not a successful full compilation.

## Runtime evidence and exact limits

`engine/run.py` executes unmodified original 1.7.10 ItemArmor/ArmorMaterial bytecode and actual native ItemArmor, plus actual concrete living-equipment methods: 393 assertions across 20 builtin material/piece constructor pairs, 16 mob writes and four paired virtual dispatch cases. Material/render/repair identity, durability/reduction, aliases/NBT/counts, empty removal and untouched hands are checked. Test subclasses retain real superclass storage. Unsafe only skips world/AI construction for the mob storage probe; constructors under test really execute. Three helper runtime mutants are rejected. Original Forge1614/FML patches and mappings are audited and pinned in engine/; custom material registration is outside this oracle.

`run.py` passes 35,432 assertions:

- All 39 exact complete old/new callback bodies run as explicitly named projections. Native bodies dispatch through actual Item and actual ForgeHooksClient. Old bodies use test-only legacy-signature source-oracle bases, not a claim that original Forge is running in the native process. All model-array selections, cache identity, item-identity branches, textures/layers and null fallback are compared.
- All eight exact complete native constructor bodies execute as renamed constructor projections, including the FSB texture registration tail. These projections are not complete gameplay owner classes.
- Complete production MaskOfInfamy, ModArmor and ArmorAshGlasses compile and execute. Complete ArmorModHandler compiles and its applicability predicate is exercised for every armor slot and all 16 modifier-mask combinations, with an explicit NTM ItemArmorMod boolean fixture. ModItems fields are explicit native-Item identity fixtures, not full content registration.
- Complete production ModelArmorBase, ModelGlasses, ModelArmorHEV and ModelRendererObj compile. The real Forge worn-model hook invokes complete ArmorAshGlasses and actual ModelGlasses; the HEV hook projection constructs actual ModelArmorHEV. Other NTM model dependencies are declared instrumented fixtures. The ResourceManager fixture supplies null meshes and explicit resource identities; no GL draw or resource loading is asserted.
- The exact complete radiation registration method checks original positive-only append, receiver identity, dynamic coefficient selection and multiplication. All 12 complete production AttributeModifier construction expressions retain the original slot-specific UUID tables. No claim is made that the still-unported surrounding attribute APIs execute.
- A real InventoryPlayer and native Slot exercise all four armor-table row identities, exact ordinary indices 39..36, validation agreement and offhand exclusion. The old total-size expression is reproduced as an offhand-misselection control. The complete ContainerArmorTable is not compiled.
- Six production semantic mutants are rejected: inventory-index model lookup, wrong boots-valid slot, reversed head radiation, wrong UUID index, lost native virtual model hook, and offhand-based armor-table indexing.

## Intentionally remaining work

The three no-argument getItemAttributeModifiers overrides in ArmorDiesel, ArmorLiquidator and ArmorDesh remain unported except their proven UUID index conversion. Nine old super.getAttributeModifiers(stack) calls remain: ArmorNCRPA, ArmorEnvsuit, ArmorDNT, ItemModWD40, ItemModIron, ItemModServos, ItemModInsert, ItemModV1 and ItemModHealth. Native armor introduces protection/toughness attribute behavior, so blindly inserting a slot would not prove original protection or speed-toggle semantics. That is a separate root.

Unrelated legacy event/modelArmor integration, potion APIs, tooltip APIs, armor-table background icons, complete gameplay owner compilation, numerical damage parity, client/server startup, saves and full-game worn visual acceptance remain unclaimed. Native ISpecialArmor adds upstream damage/display differences documented in engine/contract-audit.md.

## Reproduce with cached inputs

From ntm-port-1.12.2:

    python3 porting/tests/native-armor-slots/check_source.py
    python3 porting/tests/native-armor-slots/attribute.py
    python3 porting/tests/native-armor-slots/engine/run.py
    python3 porting/tests/native-armor-slots/run.py

The source guard is intentionally strict for this isolated family tree. Combining separately owned registration/material changes requires an exact known sibling inverse before the protected CP89 hashes are checked; do not repin arbitrary combined bytes or skip the guard. No downloads, installs, dependency/cache changes, full project build or push are used.

## Strict CP90 material-family composition

`material_composition.py` permits precisely the three independently reviewed material-registration changes in protected ModItemsArmor, ModItems and MainRegistry. It first pins the sibling migration implementation to SHA-256 `15274a584ed63348b57c98a7e8e34611829f9d0a43621432c00937437b5b9575`, then calls its `restore_base`. That inverse regenerates the exact approved complete after-file from immutable CP89, rejects any unrelated byte, and returns the original bytes. Those returned bytes still must match this family's unchanged protected hashes. Exact CP89 files remain accepted without requiring the sibling evidence to be present.

No production source or protected manifest hash is changed. Unknown files, arbitrary combined drift, changed repair metadata/material arrays and drift in the inverse implementation are rejected. `material-composition-receipt.json` and `combined-source-proof.log` record all three actual integrated hashes, their exact restored CP89 hashes and 21 positive/negative composition controls. The ordinary 126 whole-file drift controls and all 127 protected files still pass. `isolated-composition-proof.log` verifies standalone behavior is retained. The material audit itself needs no change for this composition.

To verify an integrated tree from this isolated evidence directory:

    python3 porting/tests/native-armor-slots/check_source.py --source-root /path/to/integrated/ntm-port-1.12.2
    python3 porting/tests/native-armor-slots/material_composition.py --source-root /path/to/integrated/ntm-port-1.12.2

When these files are installed in the integrated tree, all existing commands work unchanged. Historical compiler receipts remain the isolated before/after attribution; this adapter does not relabel them as a new combined compilation.
