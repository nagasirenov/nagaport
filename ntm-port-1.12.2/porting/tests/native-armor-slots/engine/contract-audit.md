# Native armor-slot engine/Forge contract audit

Read-only audit of baseline CP89 `42dea8e6`. Evidence was read from existing engine/Forge/mod archives, not inferred from compatibility stubs. No production source changes, downloads, installs, project builds, or pushes were performed. The later bounded runtime extension compiles only the production ArmorSlotOrder helper, an oracle, and test-only tracking descendants with javac. `inputs.sha256.json` records exact archive paths and hashes. `capture-engine-evidence.py` regenerates the core bytecode/source excerpts using the existing Java 8 `javap` tool.

## Slot domains: do not conflate them

| Meaning | Head | Chest | Legs | Feet |
|---|---:|---:|---:|---:|
| 1.7.10 `ItemArmor.armorType`, ctor third argument | 0 | 1 | 2 | 3 |
| 1.7.10 Forge armor model/texture/validity callback int | 0 | 1 | 2 | 3 |
| 1.7.10 armor inventory index and `ISpecialArmor` int | 3 | 2 | 1 | 0 |
| 1.12.2 `EntityEquipmentSlot` | HEAD | CHEST | LEGS | FEET |
| 1.12.2 `EntityEquipmentSlot.getIndex()` for armor | 3 | 2 | 1 | 0 |
| 1.12.2 armor inventory index and `ISpecialArmor` int | 3 | 2 | 1 | 0 |
| 1.12.2 `EntityEquipmentSlot.getSlotIndex()` | 4 | 3 | 2 | 1 |
| 1.12.2 enum ordinal | 5 | 4 | 3 | 2 |

`getIndex()` is not a legacy constructor/callback integer. `getSlotIndex()` is not an armor inventory index. Enum ordinal is neither domain. A legacy armor-type conversion may use `3 - slot.getIndex()` only after proving the slot is an ARMOR slot; hand slots have overlapping indices 0 and 1.

Evidence:
- `old-obfuscation-mappings.txt`: Forge1614 mapping identifies `abb` as ItemArmor, `abd` as ArmorMaterial, `abb.b` as armorType, `yx.b` as armorInventory. Fields CSV explicitly documents HEAD0/CHEST1/LEGS2/FEET3.
- `old-item-armor.javap.txt`: `abb(abd,int,int)` stores argument 3 directly in `abb.b`, passes it to material damage reduction and durability, then sets max damage.
- `old-inventory-slots.javap.txt`: ContainerPlayer creates armor GUI slot k at inventory index `getSizeInventory() - 1 - k`; InventoryPlayer size is main inventory length + 4, and getStackInSlot subtracts main length before indexing armorInventory. Thus armor index = 3 - legacy type. ContainerPlayer's child verifies `abb.b == k`. Forge's `old-ContainerPlayer.java.patch.txt` replaces this with the same k passed to isValidArmor.
- `native-equipment-slot.javap.txt`: actual enum constructor defines ARMOR FEET(index0,slotIndex1), LEGS(1,2), CHEST(2,3), HEAD(3,4), following two HAND enum values.
- `native-player-equipment.javap.txt`: actual native player uses armorInventory.get/set(slot.getIndex()).

## Material durability and reduction

Original material methods accept an integer and directly index their arrays; native methods accept EntityEquipmentSlot and index by getIndex().

- Original durability base array: `[11,16,15,13]` in HEAD,CHEST,LEGS,FEET order.
- Native durability base array: `[13,15,16,11]` in FEET,LEGS,CHEST,HEAD order.
- Preserve the durability multiplier, pass the correctly mapped native slot, and the native engine preserves per-piece durability automatically. Do not additionally reverse the durability multiplier or replace base values.
- Custom reduction arrays must reverse once: legacy `[head,chest,legs,feet]` becomes native `[feet,legs,chest,head]`. Example `[2,5,4,1]` becomes `[1,4,5,2]`; `[3,8,6,3]` becomes `[3,6,8,3]`. Symmetric end points can hide chest/legs corruption.
- Forge EnumHelper does not reorder arrays in either version. Old addArmorMaterial has `(name,durability,int[],enchantability)`; native has `(name,textureName,durability,int[],enchantability,soundOnEquip,toughness)` and forwards those arguments unchanged.

Evidence: `old-armor-material.javap.txt`, `native-armor-material.javap.txt`, both item armor bytecode files, and both `*-enum-helper.excerpt.java.txt` files. The original Forge ItemArmor patch changes crafting material fallback and equipment writing, not constructor/material index semantics.

## Native callbacks must actually override Item

Exact native public signatures, verified against the real forgeBin class:

- `boolean isValidArmor(ItemStack, EntityEquipmentSlot, Entity)`
- `String getArmorTexture(ItemStack, Entity, EntityEquipmentSlot, String)`
- `ModelBiped getArmorModel(EntityLivingBase, ItemStack, EntityEquipmentSlot, ModelBiped)`

The 1.7.10 model override took only `(EntityLivingBase, ItemStack, int)`; the new fourth parameter is the prepared default model. Every overriding child and super-call must change, as must direct/manual callback invocations. Keeping an int overload does not override the native hook and cannot restore native dispatch.

The native base `isValidArmor` delegates to `EntityLiving.getSlotForItemStack(stack) == armorType`. Model/texture base methods return null; ForgeHooksClient preserves null-as-default semantics in both versions. An override may therefore continue returning null if that was its original behavior. Item.getEquipmentSlot(ItemStack) is a separate customization hook; ItemArmor.getEquipmentSlot() is its intrinsic slot getter.

Evidence: `native-item-callbacks.javap.txt`, `old-Item.java.patch.excerpts.txt`, `native-Item.java.patch.excerpts.txt`, both Forge client hook excerpts. Original RenderPlayer/RenderBiped patches pass their legacy render-pass int; native LayerArmorBase passes an EntityEquipmentSlot and prepared model. Texture layer 2 corresponds to legs in both versions.

## ISpecialArmor keeps its integer armor-inventory contract

Do not migrate ISpecialArmor getProperties/getArmorDisplay/damageArmor slot parameters to enums or reverse their values.

Original Forge1614 EntityPlayer patch invokes ArmorProperties.ApplyArmor with `inventory.armorInventory`; its source loops over array index x, passes x into getProperties, stores x in ArmorProperties.Slot, and forwards Slot unchanged into damageArmor. ForgeHooks.getTotalArmorValue independently passes the same armorInventory index to getArmorDisplay.

Native Forge2768 invokes ArmorProperties.applyArmor with the same armorInventory list; its loops and callback arguments retain the same integer domain. The native interface even retains a TODO about converting the int slot. Changed array/list and null/EMPTY representations do not reverse the index.

Evidence: both `*-EntityPlayer.java.patch.excerpts.txt`, both `*-special-armor.excerpt.java.txt`, both `*-forge-hooks.excerpt.java.txt`, and the inventory-domain evidence above.

Original mod bytecode `ArmorAsbestos.getArmorDisplay` returns index0=3, index1=8, index2=6, index3=3. This is proven in `original-asbestos-display.javap.txt`. It looks chest/legs-reversed if read as a constructor type, but that original behavior must not be "fixed" by an armor-slot migration.

## Relevant upstream differences and limits

- Forge1614 ItemArmor patch fixes player equipment writes: right-click uses i+1, and dispenser removes the special player offset. Those changes concern equipment setter domain, not constructor/model/material domain; they do not justify changing the mapping above.
- Native ISpecialArmor adds handleUnblockableDamage default=false and gates getProperties for unblockable sources. Native armor calculation/display also incorporates armor/toughness attributes unlike Forge1614. These upstream behavioral changes are real but separate from slot translation; this audit does not claim cross-version numerical damage parity merely because callback slots match.
- The native getArmorModel hook must account for the supplied default model but need not mutate it if original code returned a custom model.
- This audit verifies bytecode, source and patch contracts plus the bounded runtime oracle below. It does not claim an in-game rendering/damage test, full mod build, or game boot.


## Bounded runtime oracle

Run `python porting/tests/native-armor-slots/engine/run.py` with the already-present Java 8 and native classpath defaults. Optional paths are exposed as CLI parameters. The original client and Forge1614 userdev hashes are pinned. Original signature metadata is removed into a temporary copy; class bytecode is unmodified and loaded through a separate URLClassLoader. Forge and FML patches are checked to confirm the real mob setter/getter used in the oracle are not changed by the original Forge baseline.

Results in `runtime-oracle.log`: 393 passing assertions; 20 actual original/native constructors (five built-in materials times four armor pieces); 16 concrete mob setter cases; 4 paired virtual-dispatch cases. Three runtime mutation controls all fail at the intended assertion: incorrect HEAD mapping, inventory-index/constructor-index confusion, and accepting MAINHAND as armor.

The constructors are genuinely executed, with material-reference retention, render index, exact builtin reduction/durability, material getter behavior, enchantability, and repair item identity checked. Original CLOTH corresponds to native LEATHER. The concrete EntityZombie instances are allocated without their world/AI constructors, and only real engine inventory fields are seeded. Real original EntityLiving and native EntityLiving setter/getter methods then run. Three count cases (3, 0, -4) retain stack/raw-count/NBT references; null-to-EMPTY removal is also tested. Both native hands remain untouched. Tracking descendants prove virtual dispatch is retained and still reaches real superclass storage. No Minecraft/Forge stand-ins are compiled or used.

`ArmorSlotOrder.java` is compiled directly from production and exercised in all constructor/setter cases, plus invalid integer, hand-slot, and null controls. Custom material registration is intentionally outside this oracle because another test owns it. The original player setter has a known Forge correction, so this oracle deliberately does not compare its unpatched vanilla runtime with native; that correction remains source-audited instead.
