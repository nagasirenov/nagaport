# Bounded native Item/Block translation-name calls

Baseline CP54: `0732d318302d807ce8579878a0cdde6b18bbba86`.
239 method-identifier-only edits in 146 production Java files: 48 Item setters,
191 zero-argument Item/Block getters. No declarations are changed. All literals,
texture calls, registrations, count/NBT/recipe/mechanical expressions, whitespace
(including existing CRLF/trailing whitespace), and resources are unchanged.

## Attribution and dispatch boundary

NameScanner parses/analyzes all 4,049 production Java sources using actual native
Forge/stable_39 dependencies. Before attribution finds 507 candidate invocations;
239 accepted, 268 excluded. This is not a count of all textual occurrences.
Getter receiver must be a compiler-attributed Item/Block subtype and have no
legacy zero-argument method in its complete member hierarchy. Source declaration
inventory independently confirms there are no NTM Item/Block legacy no-argument
overrides. Domain radar/material/fluid/celestial methods are deliberately intact.
ItemBlock's native getter still delegates virtually to the owning Block.
The stack-argument overload is a distinct contract, not renamed by this batch.

Setter receiver must be a direct, non-anonymous constructor expression with a
resolved Item subtype and String/null argument, and no inherited legacy setter.
ItemBlock subclasses are explicitly excluded: their original covariant setter
has additional behavior and cannot be certified by Item's base setter proof.
Post-attribution resolves all 239 selected positions to actual methods declared
by Item, Block, ItemBlock, or ItemEnumMulti. Six ItemEnumMulti calls invoke its
already-migrated real override, preserving its iconString assignment and return.
That complete owner was already compiled/executed by item-foundation-native;
this batch neither changes it nor substitutes a stand-in. Remaining setters
resolve to native Item. No base-method bypass or wrapper is introduced.

The four legacy texture-side-effect setter owners ItemModMinecart,
ItemCraftingDegradation, ItemBlowtorch and ItemBoltgun are untouched, including
their ModItems call sites. Stack-argument naming declarations, ItemStack name
calls, anonymous/dynamic/chained/unresolved constructors, Forge Fluid setters
and domain naming remain deferred. Foreign mods with custom old-name methods
are not certified. Full override closure here means only the selected zero-arg
contract and constructor-exact setters, not the still-unported stack contract.

## Evidence

- 239 exact original/native position matches; resolved method owners recorded.
- 18 focused attribution controls: 7 accepted, 11 rejected, covering unrelated
  domains, legacy overrides, dynamic setters, anonymous class, ItemBlock setter,
  ItemStack receiver, and distinct Item/Block stack-argument overloads.
- Exact CP54 reversal and whole-file expected-result SHA256 on all 146 files;
  239 token mutation controls. No strings or method argument interiors changed.
- 4 actual archived-client/native bytecode method pairs have identical opcode
  streams after CP indices, mapped symbols and ldc width normalization: Item
  setter/getter, Block getter, ItemBlock getter. SRG mappings are retained.
- 165 runtime assertions execute real 1.7 client and native Forge classes:
  raw string/receiver identity, fresh returned strings, null/empty/Unicode keys,
  exact item./tile. prefixes, no automatic .name, virtual override dispatch and
  ItemBlock delegation. Unsafe is used only to allocate old base test instances;
  no platform classes are faked. Native test construction uses actual objects.
- Two exact production methods are extracted into explicitly named native-base
  slice classes: BlockOreBasalt.getUnlocalizedMultiName and
  ItemGear.getUnlocalizedName(ItemStack). Four assertions check suffix behavior.
  These are NOT whole owner compilation or callback migration claims; the
  latter's old overload is called explicitly, not via native display dispatch.
- Ten unchanged language resources are hashed from CP54; actual basalt .name
  keys verify the original underscore/key suffix convention. No key/resource
  rewrite is required because the base name strings remain identical.

Whole-source JavaCompiler attribution diagnostics: 26,162 before, 25,958 after.
These are attribution errors, not a clean Gradle build, distinct defect counts,
or complete-mod/client/server/gameplay acceptance. Coordinator owns full build.

## Reproduction (existing local Java8/dependencies, no downloads)

From module root:

    python3 porting/tests/native-translation-names/migrate.py
    python3 porting/tests/native-translation-names/bytecode.py
    python3 porting/tests/native-translation-names/probe.py
    J=/workspace/shared/nagaport-toolchain/java8
    mkdir -p /tmp/translation-scanner
    $J/bin/javac -cp "$J/lib/tools.jar" -d /tmp/translation-scanner porting/tests/native-translation-names/NameScanner.java
    find "$PWD/src/main/java" -name '*.java' > /tmp/translation-sources.txt
    $J/bin/java -Xmx1800m -cp "/tmp/translation-scanner:$J/lib/tools.jar" NameScanner /tmp/translation-sources.txt /tmp/nagaport-ae2-native-classpath.txt native > /tmp/translation-final-after.tsv 2>porting/tests/native-translation-names/after.log
    python3 porting/tests/native-translation-names/check_attribution.py /tmp/translation-final-after.tsv

For pre-attribution run NameScanner without `native` on an isolated CP54 source
checkout. For controls copy AttributionControls.java.in to a .java file, pass a
one-line source list to NameScanner, and check each printed YES/NO against its
same-line source comment. Baseline offsets are UTF-16 javac positions; all chosen
identifier spans are also independently checked against Python source offsets.
