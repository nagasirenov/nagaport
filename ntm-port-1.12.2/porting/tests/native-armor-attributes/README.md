# Native armor attribute API, CP91

Baseline: CP90 `0be133c9fb9f07053e26f081371487e641c95214`.

## Finite rule and mechanics boundary

Three armor attribute overrides (Diesel, Desh, Liquidator) now implement native
`getItemAttributeModifiers(EntityEquipmentSlot)`. Each first delegates to real
native ItemArmor, retaining armor/toughness only for the matching worn slot, then
adds the unchanged original custom payload for the original five equipment
positions. OFFHAND, a newly introduced sixth position, does not gain the custom
payload. Original payloads were slotless: held Desh genuinely applied its speed
penalty. Gating that custom modifier to its own armor slot would change behavior.
Every original UUID, name, double amount, operation, and old piece-index selection
is unchanged. LiquidatorMask inherits the Liquidator callback unchanged.

Nine manual maps now call the native Item bridge with MAINHAND and their original
stack argument. These are temporary/manual custom maps, not worn protection maps:
NCRPA, Envsuit, DNT and WD40, Iron, Servos, Insert, V1, Health. Current owned
hierarchies contain no intervening attribute override. Native ItemArmor returns
no armor/toughness in MAINHAND, just as old ItemArmor's attribute map was empty.
The bridge remains virtual. Calling the armor-slot overload instead creates a
real bug: non-sprinting removal strips chest defense (8 becomes 0 in the test).
Insert's neutral-speed null return, all servo identity/piece predicates, UUID
arrays, float arithmetic, shared sprint UUID, sprint/full-set/identity gates and
remove-before-apply ordering remain unchanged.

Native armor/toughness attributes are the 1.12 platform's representation of worn
protection, not an invented NTM bonus. The old platform counted ItemArmor defense
separately from these maps. Native damage/armor/toughness formulas and NBT slot
filtering are not claimed numerically identical to the old game. No compensating
stats, armor balance adjustments, equipment cache fixes, or offhand gameplay
extensions are introduced. The earlier material family owns material values
(including zero custom toughness). Ordinary native ItemStack NBT replacement and
slot filtering are retained; malformed NBT/approval-sensitive behavior is excluded.

## Evidence and replay

From `ntm-port-1.12.2` with the existing Java 8/cache:

```sh
python3 porting/tests/native-armor-attributes/check_source.py
python3 porting/tests/native-armor-attributes/engine/run.py
python3 porting/tests/native-armor-attributes/run.py --mutations
python3 porting/tests/native-armor-slots/attribute_composition.py
python3 porting/tests/native-armor-slots/check_source.py
```

- Strict inverse: 12 complete files return byte-for-byte to CP90, seven untouched
  ancestor/UUID/domain dependencies, global attribute API census, 36 drift controls.
- Full-source compiler attribution: all 4,070 production units, 10,831 to 10,819
  diagnostics; exactly 12 removed, zero added. Three true native overrides and
  nine Item bridge owners verified. No mod class files emitted or Gradle invoked.
  `attribute.py` reproduces the isolated attribution; `before/after.tsv`, logs,
  full diagnostics and `attribution-delta.json` preserve the receipt.
- Actual old/native engine oracle: 68 assertions; complete original three armor
  classfiles produce 12 maps and 16 exact payload tuples. Native runtime compares
  every tuple through actual ItemStack dispatch across all five old-equivalent
  slots. See `engine/README.md` for symbol-remapping and constructor-bypass limits.
- Native runtime: 10,517 assertions. Nine complete production method projections
  (three override methods, six getModifiers methods), three exact speed slices,
  actual native Item/ItemArmor/ItemStack/AttributeMap, complete ArmorSlotOrder.
  Five native materials, four piece identities, all six query slots, old UUIDs,
  modifier names/amounts/operations, held/offhand boundaries, native protection,
  virtual calls in all nine manual contexts, neutral null, servo branches and
  repeated sprint/full-set transitions are checked.
- Eleven behavioral mutants are killed, including own-slot-only custom modifiers,
  new offhand effects, discarded native protection, wrong UUID index, wrong amount,
  worn protection in temporary maps, bypassed virtual dispatch, neutral null change,
  inverted sprint predicate and removed Envsuit full-set predicate.
- Prior slot-family guard composes only this exact pinned inverse before applying
  its original inverse and original protected pins. Sixty original/composed/drift
  controls pass. Existing material inverse and all protected hashes stay unchanged.

The generated Java file is intentionally a projection, not a complete armor-class
or game build. The speed context supplies booleans and a real native attribute
map; it is not a pretend EntityPlayer. No world tick, complete onArmorTick,
potion/flight/water/rendering, full mod lifecycle, full common build, or gameplay
claim is made. Untouched method code outside the exact speed slices is covered
by the complete-file inverse, not runtime-tested here.

## Remaining separate owners

`excluded-attribute-sites.json` records exact untouched declarations/calls:
ItemMirrorTool, ItemRBMKTool, ItemSwordAbility, ItemToolAbility, ItemToolingWeapon,
WeaponSpecial, GunB92, ItemCrucible, and ModEventHandler's held-stack query.
Native tool/weapon inherited attack-damage/attack-speed maps and stack-sensitive
virtual behavior need their own bounded semantic rule. They are not migrated by
blindly inserting a slot. Generic equipment caches, including ModEventHandler's
previousEquipment logic, and the old unresolved generic setter behavior remain
separate roots.
