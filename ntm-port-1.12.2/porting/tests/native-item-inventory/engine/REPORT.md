# Item-inventory engine oracle

Run `python3 porting/tests/native-item-inventory/engine/run.py`.

This evidence is deliberately separate from native production inventory behavior.
It invokes genuine cached Minecraft/Forge engine classes and original NTM
classfiles. It does not replace Minecraft, Forge, ItemStack, NBT, player, or world
methods with stubs. Compilation and runtime output live in a temporary directory.
Only source, text evidence and receipts are retained here.

## Original operations

The original mod's complete `ItemInventory`, `ItemStackUtil`, and three nested
`InventoryLeadBox`, `InventoryToolBox`, and `InventoryRebar` classfiles are
remapped by official SRG-to-notch names and loaded with the actual Minecraft
1.7.10 client. No original method instructions are added, removed or replaced.
A generated test-owned subclass supplies only its constructor and the original
abstract inventory size/name/custom-name hooks. Its inventory capacity is three.
All setters, decrement/removal, markDirty and small-NBT handling are inherited
from the original classfile, including its real serialization/compression calls.

Checks cover full signed integer counts, upper-only setter clamping, exactly-zero
cleanup, negative retention, split versus whole-stack reference identity, null
absence, Java signed overflow, and NBT signed-byte count storage. Native count
tests use the same signed values and valid split preconditions.

The complete original nested constructors also run directly with a null player,
not through the generated base fixture. All 27 combinations of owner counts
`Integer.MIN_VALUE`, -1 and 0, stored signed-byte counts -128, -1 and 0, and the
three constructors preserve the exact existing owner compound and items-list
references and their contents. They retain the raw owner count and load the
nonpositive stored diamond stack and its tag. Actual capacities 20, 24 and 1 and
nullable unused slots are checked. No outer gameplay class is remapped, modified
or called by the test; no extra linkage workaround was required. The current
native constructor behavior is tested separately, not claimed by this oracle.

Old ItemStack runs against the genuine vanilla class. The cached Forge1614
ItemStack patch was inspected: its hooks do not replace count storage, split,
copy, NBT count serialization/loading, or the compressor tested here. Custom
Forge item-damage hooks and a complete old Forge launch are not claimed.

The real original `checkNBT` returns the identical input at compressed sizes
5999 and 6000. At 6001 it reaches the player warning/ejection branch. That
branch is detected with an intentional null-player exception, not a fake
player/world. Actual spawned-world ejection is outside this oracle.

## Native operations

The actual Forge2768 ItemStack is tested for raw counts, public getCount masking,
patched copy preserving raw item/count/damage and deeply copying tags, split
copy identity and signed overflow, cached-empty repaired stacks, and read-only
quantity fallback. The fallback follows the prior scoped DiFurnace convention:
use a nonzero native getCount, otherwise read the genuine private integer using
Forge ReflectionHelper with both official MCP and SRG field names.

The proposed null-to-EMPTY boundary expression is tested for null, sentinel,
zero/negative/nonempty objects and exact identity. This is explicitly an adapter
contract test, not a claim to invoke a native production inventory implementation.

A last positive stack is depleted through real shrink. Tests establish that
getItem becomes AIR and hasTagCompound becomes false afterward, then prove that
pre-shrink item/damage/tag capture constructs a correct final drop stack with
independent NBT. No spawned EntityItem or world is fabricated.

## Mapping and compression

`mapping-evidence.txt` records official MCP-to-SRG and notch-to-SRG quantity
field mappings. The existing test-only `RemapQuantityField` transforms the
genuine native class by renaming only its quantity field and references to the
official SRG name. A separate JVM runs the identical native oracle with that
class first in the classpath. Class hashes are recorded in `receipt.json`.
This proves the exact production field-name fallback path, not an entire
production-obfuscated game launch.

The old compressor and native writeCompressed produce byte-identical output
for 401 common-tag compounds, with deterministic random byte payloads.
The dataset includes exact compressed lengths 5999, 6000 and 6001. Native reads
round-trip every compound. The same bytes are obtained with the SRG-field
runtime layout. Hashes and threshold input sizes are in `receipt.json`.

Scope excludes native production inventory class closure, container click
behavior, shared utility rewrites, NTM registration/bootstrap, and gameplay.
