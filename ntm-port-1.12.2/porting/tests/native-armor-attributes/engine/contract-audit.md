# Item/armor attribute engine contract audit

Engine contract audit of CP90-derived source and supplied 1.7.10/Forge 1614 + native 1.12.2 artifacts. Only this test/evidence subtree is written. No production edit, installation, network download, or full mod build. `capture.py` regenerates the concise bytecode/patch evidence from the supplied artifacts.

## Decision on proposed finite migration

The proposed rule is supported for the three custom-map overrides and nine manual map calls:

1. Diesel/Desh/Liquidator should use native `getItemAttributeModifiers(slot)`, seed from native `super.getItemAttributeModifiers(slot)`, and preserve the original custom payload in MAINHAND and all four ARMOR slots. Excluding OFFHAND is an explicit compatibility boundary for a slot that did not exist in the old engine, not an old-engine fact.
2. The nine manual map callers may use `super.getAttributeModifiers(MAINHAND, unchangedStack)`. This preserves the actual Forge virtual bridge and supplies the same empty seed for all nine known owners without injecting worn armor/toughness.
3. Do not replace the manual calls with `stack.getAttributeModifiers(slot)`: that switches receiver from the modification item/armor object to the stack's item and enables NBT replacement that the original direct item call did not perform.

## Verified engine semantics

- Old Item.getItemAttributeModifiers() returns a fresh empty HashMultimap. Forge 1614 deprecates that hook, adds Item.getAttributeModifiers(ItemStack), and its default uses virtual `this.getItemAttributeModifiers()`. Forge's ItemStack patch changes its non-NBT branch to `getItem().getAttributeModifiers(this)`. Thus the unpatched vanilla client is not the complete dispatch contract. See `forge-hooks.excerpts.txt` and `engine-methods.excerpts.txt`.
- Old EntityLivingBase.onUpdate's server equipment loop is exactly index 0 through 4. It removes the previous stack's slotless modifiers and applies the new stack's slotless modifiers when equipment changes. EntityPlayer.getEquipmentInSlot(0) returns the held item; indices 1–4 return armorInventory[index - 1]. There is no armor-type check in this dispatch. Forge's patch adds an early living-update event cancellation hook, not a different equipment map or slot filter. See old onUpdate/player excerpts.
- Native Item preserves that virtual bridge as getAttributeModifiers(EntityEquipmentSlot, ItemStack) → getItemAttributeModifiers(EntityEquipmentSlot). Native living-equipment updates iterate all enum slots, including OFFHAND, and pass the actual slot to both old/new ItemStack map queries. Forge also adds the equipment-change event. See native bytecode/patch excerpts.
- Native ItemArmor seeds Item's empty map, then adds ARMOR and ARMOR_TOUGHNESS only when queried slot == armorType. Both are operation 0; UUID is ARMOR_MODIFIERS[slot.getIndex()]. Values are damageReduceAmount and toughness. MAINHAND therefore returns empty for these ordinary armor types. Legacy ItemArmor declares no attribute hook and its Forge patch does not add one. See native ItemArmor bytecode and complete legacy ItemArmor declaration in `owner-hierarchies.txt`.
- Both versions use an AttributeModifiers list in ItemStack NBT as a replacement, not a merge with item defaults. Even an empty list suppresses all defaults. The old parser ignores Slot. Native parser includes an entry only when Slot is absent/not a string or exactly equals queried slot.getName(); a nonmatching string is excluded. Both require nonzero most/least UUID halves. Native also checks a parsed null modifier. The item hook is called only when the list tag is absent. See complete old/native ItemStack method excerpts.

## Concrete counterexamples

- Hold Desh armor in the original main hand: its -0.025 movement-speed operation-1 custom modifier is applied. An own-armor-slot-only migration drops it. Diesel similarly supplies +0.25 knockback operation 1; Liquidator supplies +100 knockback operation 0 and -0.1 speed operation 1. All are keyed by the armor item's original armor index, not the equipment slot. Wrong-slot armor placed through direct equipment setters also used the same custom map, so preserving all four armor queries is faithful at this engine boundary.
- Put NCRPA chest armor on a non-sprinting player. If its manually removed speed map is seeded with native super.getAttributeModifiers(CHEST, stack), it contains the real ARMOR and ARMOR_TOUGHNESS modifiers, so the existing unconditional remove removes chest defense as well as sprint speed. The original seed was empty and removed only its speed entry. Envsuit and DNT have the same contamination risk. MAINHAND avoids it while retaining virtual dispatch.
- Put an empty AttributeModifiers list on custom armor. Native ItemStack returns no defaults at all; calling the item hook directly and merging the result back would wrongly restore custom/baseline modifiers. Put a list entry with Slot="head" on an armor item held in MAINHAND: old ItemStack applies it; native correctly filters it out. This is an engine-version difference outside the item-hook migration.

## Nine-owner shadow audit and limitations

ArmorNCRPA, ArmorEnvsuit, ArmorDNT → ArmorFSBPowered → ArmorFSB → ItemArmor. ItemModWD40, ItemModInsert, ItemModHealth, ItemModIron, ItemModServos, ItemModV1 → ItemArmorMod → Item. None of these twelve mod owner/intermediate classes declares either item attribute hook in original artifact declarations or current source. Source-wide search found no named subclasses of the nine owners. Source hashes and original class declarations are recorded in `owner-hierarchies.txt`.

No concrete counterexample to the proposed MAINHAND seed exists in this finite inspected hierarchy. This is not a guarantee for future/third-party subclasses that override slot-aware hooks: such a hook can deliberately return custom MAINHAND data. Retaining virtual dispatch rather than replacing with HashMultimap.create() preserves extension behavior better. Tool/weapon hooks remain explicitly outside this audit.
