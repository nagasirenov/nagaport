# Complete production item-inventory base gate

Run `python3 porting/tests/native-item-inventory/production/run.py`.

The gate compiles the complete current production `ItemInventory`,
`NullableInventoryStackNbt`, `WorldSoundEffects`, `ModSoundEvents`, and
`RefStrings` sources. There are no extracted production methods, replacement
platform classes, or substituted production collaborators. Receipt hashes
pin every compiled production source and both genuine native quantity-field
layouts. Source hashes are checked again after execution.

The test-owned inventory subclass supplies only the original abstract size,
name and custom-name contracts, its initial fields, and public access to
protected production helpers. It does not override inventory algorithms.

## Coverage

- Actual setters/decrements/removals across fourteen signed integer counts
  and eight decrement amounts, including overflow and exact reference identity.
- Raw count reader, cached-empty repaired stack, null failure, exactly-zero
  cleanup, upper-only clamp, sentinel input, and native null-to-EMPTY outputs.
- Real native `Slot` delegates, positive/negative/zero emptiness, and nullable
  domain state remaining untouched by the native emptiness query.
- Complete production domain-local NBT reader/writer, signed-byte counts,
  zero/negative records, sparse slots, duplicate indexes, invalid registry
  entries, AIR, signed slot indexes, capacity and tag-count distinctions.
- Clear preserving array identity, persisting empty contents and retaining
  unrelated owner tags; fieldless native contract; custom/translated display.
- Exact compressed sizes 5999/6000/6001 against production `checkNBT`.
- Actual sound bridge and native listener interface for open/close callbacks.
- Complete production overflow ejection using real constructed native World,
  EntityPlayer and EntityItem objects. The test World subclass observes spawn
  calls and deliberately returns false; the algorithm must still consume/drop
  the same quantities. This is not a real server/chunk spawn claim.
- Positive item totals, final-drop identity/metadata, independent tags, position
  range, preserved zero-count slot aliases immediately after ejection, negative
  retention and later exact-zero cleanup.

The World subclass supplies the required abstract chunk hooks without loading
chunks. The EntityPlayer subclass supplies required creative/spectator hooks and
records warning messages. These are test-owned subclasses of actual engine
classes, not platform stubs. No Unsafe or reflective writes are used.

The same complete production gate runs against the original native ItemStack
class and that genuine class with only quantity field/references renamed to the
official SRG field name by the established test-only remapper.

## Malformed damage: raw payload preserved; native semantics disclosed

`damage-facts.csv` records actual old and native constructors, public item/count/
damage access, copies, serialized signed-short damage and NBT roundtrips.
`DamageFacts` uses reflection only to choose the two actual engine APIs; it does
not write fields or replace methods.

Both constructors clamp negative damage arguments to zero. Damage 0, 32767,
32768, 65534 and 65535 retains registered diamond identity and count in both
engines. At damage 65536 and Integer.MAX_VALUE, old stacks remain ordinary
diamond stacks while native stacks retain raw count/damage but are considered
empty: public item is AIR and count is zero. Copies retain this native condition.
A native constructor supplied the original diamond/count/damage still creates
an engine-empty object.

Both engines serialize damage as a signed short, so the tested damage values
32768 and above round-trip to zero. Consequently a detached NBT snapshot's
`Damage` short cannot recover the complete transient signed-int damage.
Its item registry id can identify the raw item, and that item's native
`getDamage(stack)` reads the raw int, but unchanged out-of-range damage still
has native-empty semantics. No normalization, rejection or clamp is implemented.
The original raw payload is preserved, while native engine-empty interpretation
remains explicit.

Production ejection now has an edge-only public-snapshot fallback when the
captured item is AIR. The complete production test verifies final emitted
EntityItem stacks for damage 65536 and Integer.MAX_VALUE retain the original
registered item id, raw count seven, full integer damage, and independent NBT.
Raw item identity is verified from serialized registry id and full damage by
calling the recovered item's getDamage on the actual emitted stack, not by
reading NBT's truncated Damage short. Native EntityItem constructors and getters
retain the supplied raw ItemStack reference even when that stack isEmpty.

The repaired cached-empty case is also exercised: construct damage 65536 then
publicly set damage to nine. Dynamic isEmpty is false while cached getItem/count
remain masked. The fallback catches the captured AIR, preserving a valid
diamond/count-seven/damage-nine emitted stack and copied tag.

The raw out-of-range stacks still report native isEmpty and may be rejected or
removed by later native update/pickup logic. Neither these semantics nor custom
capability serialization callbacks are normalized or suppressed. The edge
snapshot invokes native writeToNBT, so capability serialization may run; the
ordinary capture path is unchanged.

One mutant removes only the production fallback block from a complete temporary
copy of ItemInventory. The same runtime gate rejects it at the malformed-drop
raw registry identity assertion.

## Limits

Four nested production item-inventory subclasses are outside this gate.
Gameplay/bootstrap, real packet delivery, real world spawning and full builds
are not claimed. Static sound registration emits expected Forge warnings about
the hbm prefix outside a loaded mod context; the complete source is unchanged.
