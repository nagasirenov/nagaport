# Bomb/launcher nullable inventory NBT loading

Base: `ea933e28fe3a1a9a60e9d438411a02182b2ab22a` (sealed CP70).

Production scope: thirteen `slots[b0]` load assignments in the bomb package, plus
one NTM-owned `NullableInventoryStackNbt` utility. No platform classes, registry
replacement, source-set exclusions, numeric world importer, or unconditional
empty-to-null converter. This does not finish bomb inventories or the mod.

## Why this boundary exists

Every selected class initializes its own `ItemStack[]`, loads bounded signed-byte
`slot` indexes, tests entries against null, clears consumed entries to null, and
writes only non-null entries. The pristine and CP70 files retain those boundaries;
`verify.py` checks them individually and exactly reverses every caller edit to its
CP70 byte hash. Only the loader expression and utility import change. Unselected
native inventory boundaries and prior ItemStackUtil work are not touched.

The actual 1.7.10 loader returns null only when its item registry lookup is null.
It does not return null for a registered zero/negative byte count. Its negative
short damage clamps to zero and its tag aliases the input compound. Actual mapped
Forge 1.12.2 constructs a non-null empty stack for invalid/missing IDs. Simply
changing the call to a constructor would therefore populate an owned nullable
slot with an invalid object; simply converting every `isEmpty()` result to null
would instead discard registered nonpositive-count records that the old loader
retained. The utility validates the native string registry ID first, then lets the
real native constructor handle count, damage, tag, item update hooks and Forge
capability initialization/deserialization. AIR is not an inventory item and maps
to vacancy, corresponding to original numeric ID zero's missing item.

Native 1.12.2 public getters/serialization still apply native empty-stack behavior
for nonpositive counts. The probe explicitly checks raw signed count storage and
native emptiness rather than claiming that legacy public getters or serialization
of malformed zero/negative stacks are reproduced. No platform internals are
modified by production. Numeric 1.7 world IDs are deliberately not imported; the
project targets fresh 1.12.2 saves and saves written by this port. String numeric
IDs retain native Item.getByNameOrId behavior. Invalid resource syntax follows the
native lookup's exceptions; no exception-swallowing fallback is added.

## Reproduce

Set `JAVA_HOME` to Java 8, `MINECRAFT_CLASSPATH` to the actual mapped Forge 1.12.2
classpath, and `LEGACY_CLIENT_JAR` to the supplied 1.7.10 client. Run `./run.sh`.
No installation or downloads. The disposable old-client copy removes signature
metadata only, with every remaining class/resource byte verified unchanged.
`runtime-inputs.json` identifies inputs without environment-specific paths.

Observed: 191 assertions using the compiled, unextracted production utility and
real mapped ItemStack/NBT/Forge; 123 assertions using actual obfuscated original
loader; thirteen exact source reversals and original nullable consumer checks.
Native coverage includes absent/unknown/AIR/wrong-type ID; signed counts; damage;
NBT alias; populated roundtrip; custom item update hook; Forge capability input,
provider deserialize/serialize/reload. The custom test Item and capability provider
are real API extensions, not fake Minecraft classes. Forge prints an expected
prefix warning for the test-only registry item outside mod-loading context.

The caller classes still have unrelated compile errors and are not instantiated
in this probe. It proves the helper at runtime plus exact finite source wiring,
not tile entity integration, graphical/client/dedicated-server startup, complete
inventory native-empty migration, or an end-to-end build. The old oracle uses the
supplied vanilla client, not an old Forge capability implementation. New Forge
capability behavior is verified independently on the actual native runtime.
