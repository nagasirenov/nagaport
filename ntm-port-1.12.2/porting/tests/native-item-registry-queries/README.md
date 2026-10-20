# Native item registry output-name queries

Baseline CP74: `5a102874e0cd18dfd6b2984a994330b9de0ece75`.
Twenty exact expressions in ten production files: nineteen name queries and one
GUI iteration field access. No new registration, fake registry, adapter, resource
rename or changes to saved keys are introduced.

`Item.REGISTRY.getNameForObject` returns a native ResourceLocation, whereas the
original `Item.itemRegistry.getNameForObject` returned String. Each selected
name query explicitly uses `java.util.Objects.toString(key, null)`. The second
argument is essential: null/unregistered items still serialize as JSON null,
not the string "null", and string consumers retain their old null behavior.
The original item expression is evaluated exactly once, in its original order
relative to the writer/other enclosing receiver. No item identity is replaced.
The GUI iterates actual native items, without introducing an ordering promise.

## Proof

Run from any directory, with the existing offline toolchain:

```
python3 porting/tests/native-item-registry-queries/run.py \
 /workspace/shared/nagaport-toolchain/java8 \
 /tmp/nagaport-ae2-native-classpath.txt /tmp/nagaport-legacy-client.jar \
 /tmp/nagaport1710-srg.zip \
 /workspace/shared/nagaport-toolchain/gradle-user-home/caches/minecraft/de/oceanlabs/mcp/mcp_stable/39/fields.csv
```

2,372 actual native/original assertions pass. The original signed client archive
is hash-verified, and only its signature metadata is omitted in the temporary
execution copy. Actual original registry `adb.e` and native Forge Item.REGISTRY
are used. Tests cover every bootstrapped native item, genuine identity,
registered/null/unregistered names, common original saved keys, JSON and NBT
roundtrips, writer/argument evaluation order, and registry membership.
Two verbatim production serialization statements (AnnihilatorSavedData and
ItemPoolConfigJSON) are compiled and executed for every native registered item.
These are explicitly bounded statement extractions, not complete owner classes.
No platform or registry stubs are used. Original and native mappings are checked.

Javac attributes the original qualifier to net.minecraft.item.Item in each
complete selected owner; unresolved unrelated dependencies are not claimed to
compile. Negative controls exclude a shadow Item class, a value receiver, and
string contents. Every selected expression reverses to the exact baseline
bytes; original line endings, arguments, enclosing statements and surrounding
source remain intact. Twenty independent single-edit reversal mutations are
rejected by source hashes. A token-only native field rename fails genuine
javac String assignment, proving explicit result conversion is required.

## Deliberate exclusions

The baseline census contains 45 accesses; 20 are selected. All 21 input lookups
remain unchanged. Actual Forge missing-item lookup returns null, not AIR. Tests
verify this against original null behavior, but ResourceLocation lowercases
namespace/path and has different colon parsing: `MINECRAFT:APPLE` is absent in
the original registry but resolves to the native apple. Therefore blindly
wrapping arbitrary saved/user/external names in ResourceLocation is not certified.
The two Mekanism external IDs, Compat and structure palette are excluded too.

Three StatHelper iterators feed numeric-ID statistics arrays, and HazardTypeGlitch
uses registry size as a raw numeric-ID random bound. They are not migrated as
mere name changes. No whole-owner compilation, save-reader migration, cross-mod
registration/name normalization, numeric-ID parity, full game launch or gameplay
parity is claimed. Main's coordinator owns full compile diagnostic accounting.
