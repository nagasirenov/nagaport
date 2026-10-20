# Native tool/weapon attribute closure, CP92

Baseline: CP91 `f5ce86cd607379cc9810896cf91d1fee86ce6331`.

## Production boundary

Nine complete source files: eight remaining tool/weapon attribute owners and the
ballistic-gauntlet held-stack query in ModEventHandler. Eight callbacks become
true native slot-aware overrides; fourteen old damage-UUID references become the
same native UUID symbol. WeaponSpecial adds one reference for its precisely
preserved legacy damage. ItemToolAbility selects the real native two-argument
ItemTool constructor. No replacement framework, shim, source exclusion, broad
malformed-stack policy, new balance setting or attack-speed compensation exists.

The shared rule distinguishes genuine inherited maps from explicit replacement
maps:

- ItemSwordAbility, ItemToolAbility and ItemCrucible intentionally replace the
  entire original map. Their explicit damage/movement payloads remain exact.
  Crucible remains empty when damage >= maximum. Adding native super here would
  introduce unintended damage/speed, and its shared damage UUID would prevent
  HashMultimap.put from replacing the inherited amount. Calling Crucible's super
  stack bridge also revives its inherited ability payload when spent.
- Mirror, RBMK, ToolingWeapon and GunB92 retain the genuine Item-only superclass
  contribution and virtual dispatch. Native supplied slot and original stack
  are forwarded, not a blindly chosen worn slot.
- WeaponSpecial genuinely inherits Sword. It keeps native Sword attack speed,
  but restores original effective damage using exactly float `4.0F + material`
  before widening to double. Native Sword's real `getAttackDamage()` accessor
  returns raw material attack, so `super.getAttackDamage()` supplies that value
  without introducing a new material field or dispatching a subclass override.
  Remove-then-put replaces only the native damage UUID in MAINHAND. The original
  modifier name/UUID/operation are preserved; native speed and unrelated entries
  survive. Adding 1 to an already rounded native value is deliberately not used.
- Explicit NTM payloads still apply at the original held-plus-four-armor
  positions. The new OFFHAND position does not gain those effects. Native
  inherited Sword entries remain naturally MAINHAND-only. This distinction
  does not imply ordinary tools normally equip as armor.
- The gauntlet queries the already-selected main-hand ItemStack using MAINHAND,
  retaining native stack-sensitive dispatch and authoritative AttributeModifiers
  NBT, rather than querying Item directly. No surrounding condition or effect
  changes. Ordinary NBT Slot filtering remains the native engine's behavior.

Native cooldown is intentionally not old-engine combat parity. Replacement maps
have no attack-speed tuple, leaving native default speed 4. WeaponSpecial retains
native Sword's -2.4000000953674316 tuple (effective speed approximately 1.6).
No invented speed modifier is used to imitate old combat. WeaponSpecial damage,
all explicit NTM amounts, movement penalties, UUIDs, operations, identity gates,
Crucible metadata predicate and float evaluation are preserved.

## Evidence and replay

From `ntm-port-1.12.2`, using the cached Java 8/native classpath:

```sh
python3 porting/tests/native-tool-attributes/check_source.py
python3 porting/tests/native-tool-attributes/engine/run.py
python3 porting/tests/native-tool-attributes/run.py --mutations
python3 porting/tests/native-armor-attributes/tool_composition.py
python3 porting/tests/native-armor-attributes/check_source.py
python3 porting/tests/native-armor-slots/check_source.py
```

- Nine strict complete-file CP91 inverses, 27 drift controls, seven unchanged
  ancestor/descendant pins and a global attribute API census.
- All 4,071 production inputs parsed and attributed, no mod bytecode emitted:
  10,583 → 10,555 diagnostics, exactly 28 removed, zero added. Eight native
  overrides, fifteen real Item UUID references, native ItemTool constructor,
  genuine ItemSword raw-material accessor and ItemStack held query are typed.
  `attribute.py` reproduces this isolated analysis; saved TSV/logs and diagnostic
  delta preserve the receipt. `registration-census.json` records 75 actual
  construction/registration source lines without changing their arguments.
- 329,839 production-projection assertions: eight complete attribute methods,
  three complete constructor bodies (SwordAbility, ToolAbility, WeaponSpecial),
  one exact nullable held-expression projection, actual native Item/Sword/Tool/
  ItemStack and NBT. All six native query slots, representative explicit payloads,
  five native materials plus nine custom/rounding probes, identity predicates,
  active/spent metadata, virtual dispatch, exact tuple identity and genuine
  native speed are checked. The six original maps/seven tuples are compared
  directly from `engine/original-payloads.json` through native stack dispatch.
- Ten runtime mutants killed: blind super replacement, mainhand-only custom
  payloads, new offhand effects, missing WeaponSpecial removal, +1 after rounding,
  double rather than float evaluation, cleared native speed, resurrected spent
  Crucible damage, wrong UUID and wrong held-slot query.
- 191 separate actual-engine assertions, including old/native baseline formulas,
  raw accessor, exact UUID equality/removal, unrelated entry preservation,
  two-/four-argument native ItemTool equivalence, metadata/NBT and default/native
  attack speeds. Complete supplied original SwordAbility, GunB92 and Crucible
  classfiles execute after symbol-only remapping, with unrelated constructors
  bypassed. Their actual attribute bodies produce six maps/seven tuples.
- The CP91 armor census composes only the pinned exact tool-family inverse.
  Forty-five original/composed/drift controls pass. Existing census, source pins,
  CP90 slot/material inverse scripts and all prior protection checks remain
  unchanged. This is strict composition, never repinning or skipping.

## Honest limits

Generated classes are explicit method/constructor projections, not full owner
classes or a game build. ToolAbility's enum supplies only local type/blocks
scaffolding; original block membership, harvesting, renderer, charge machinery,
abilities, world effects and full combat are not runtime claims here. Their
unchanged bytes are covered by the full-file inverse. The gauntlet expression is
fed the existing nullable held result; no pretend EntityPlayer is used.

Original ToolAbility's real class initializer cannot execute on the bare legacy
client because it requires Forge Block.captureDrops(boolean). That exact expected
failure is asserted, and its complete original bytecode plus the genuine Forge
patch are retained instead of a stub. Mirror/RBMK/ToolingWeapon old super bridges
need the patched old Item API; WeaponSpecial's identity branches need unrelated
ModItems initialization. Their original contracts are static evidence, not
misrepresented runtime executions. See `engine/README.md` for precise boundaries.

No full original/native game, mod lifecycle, complete combat cooldown equivalence,
full common build, dependency changes or network operations are claimed.
