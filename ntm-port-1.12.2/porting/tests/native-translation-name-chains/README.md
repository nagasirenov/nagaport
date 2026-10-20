# Exact fresh ArmorFSB name chains, CP62 increment

Base: 36387bc1c10fa9c36fbb27f5e9d44b45d474eb18.
Production scope: 32 method-identifier replacements in ModItemsArmor.java only.
Every selected receiver is exactly new ArmorFSB(...).cloneStats(...). No subtype,
anonymous class, factory, alias, dynamic receiver, or extra builder is included.
No constructor, armor/radiation logic, texture setter, resource, key argument,
registration, or recipe changes. All other bytes of the entire file invert exactly.

## Proof and boundaries

ChainScanner attributes all 4062 actual production Java sources against the cached
native Forge classpath, before and after. All selected constructors resolve to
ArmorFSB(ArmorMaterial,int,String), all predecessors to ArmorFSB.cloneStats(ArmorFSB),
and every post-change setter to Item.setTranslationKey(String). The exact fresh
class fixes virtual dispatch: ArmorFSB has no legacy setter, and the actual old
and native ItemArmor classes inherit the base Item setter (HierarchyOracle checks
both real class files, not a fake platform fixture). ItemArmor directly extends Item.
The original/native Item setter instruction equivalence and 165 actual runtime
assertions are reused unchanged from native-translation-names, hash-pinned in the
manifest. They are reused evidence, not newly run assertions.

ReturnProof parses the exact production cloneStats method: 13 assignments,
one ignored-result this.setRadResist(original.radResist), then return this,
with no alternate return or branch. setRadResist resolves to ArmorFSB on this
exact fresh instance; its entire source and the complete ArmorFSB owner are pinned
unchanged. Its radiation registration and exceptions therefore remain in their
original position before the name argument and setter. Constructor/argument
exceptions, cast exceptions, null original, shared effects list, field reads and
writes, and left-to-right evaluation are unchanged, not emulated. No cast or
wrapper is inserted. The native base setter stores the same raw key and returns
the same receiver, preserving all following chain evaluations and return typing.

Independent DestinationScanner separately enumerates all 477 receiver AST nodes
and name-argument records for these 32 calls, including resolved field and method
owners, constructor argument types and every predecessor expression. Ordered
records are byte-identical before/after; only the 32 outer native destinations
change. Six controls accept one exact owner and reject dynamic, factory,
anonymous, overriding-subclass and non-String cases. 32 whole-file mutation guards
and the exact CP62 whole-file inverse pass.

Important: ArmorFSB's constructor body still contains unresolved native ItemArmor
super(material,0,int slot) and setTextureName calls. They are unchanged and openly
recorded UNRESOLVED in the closure report. This proof certifies the attributable
fresh constructor expression and the naming transformation, NOT that its whole
owner compiles or that original armor gameplay already runs on native Forge.
The target ItemArmor slot conversion, textures/models, full armor/radiation
runtime integration, client/server and visual acceptance remain separate work.
Four legacy texture-side-effect setter owners (ItemBlowtorch, ItemBoltgun,
ItemModMinecart, ItemCraftingDegradation), all subclasses and other chains remain
excluded. No resource operation has been dropped.

Full-source compiler attribution remains 16,517 diagnostics before and after:
resolving these setters exposes downstream unported chain calls. This is not a
net diagnostic reduction, build success, or a distinct-defect count. Coordinator
owns the integrated offline full build. No build exclusion or API shim is added.

## Reproduction

Run from the module root, with existing Java8 and cached classpath only:

    H=porting/tests/native-translation-name-chains
    J=/workspace/shared/nagaport-toolchain/java8
    mkdir -p /tmp/name-chain-scanner
    "$J/bin/javac" -cp "$J/lib/tools.jar" -d /tmp/name-chain-scanner "$H/ChainScanner.java" "$H/DestinationScanner.java" "$H/ReturnProof.java"
    find "$PWD/src/main/java" -name '*.java' > /tmp/name-chain-sources.txt
    "$J/bin/java" -Xmx1800m -cp "/tmp/name-chain-scanner:$J/lib/tools.jar" ChainScanner /tmp/name-chain-sources.txt /tmp/nagaport-ae2-native-classpath.txt
    "$J/bin/java" -Xmx1800m -cp "/tmp/name-chain-scanner:$J/lib/tools.jar" DestinationScanner /tmp/name-chain-sources.txt /tmp/nagaport-ae2-native-classpath.txt
    "$J/bin/java" -cp "/tmp/name-chain-scanner:$J/lib/tools.jar" ReturnProof src/main/java/com/hbm/items/armor/ArmorFSB.java
    CP=$(cat /tmp/nagaport-ae2-native-classpath.txt)
    "$J/bin/javac" -cp "$CP" -d /tmp/name-chain-scanner "$H/HierarchyOracle.java"
    "$J/bin/java" -cp "/tmp/name-chain-scanner:$CP" HierarchyOracle /tmp/nagaport-legacy-client.jar
    python3 "$H/verify.py"

For before attribution use an isolated CP62 source tree (or substitute only the
CP62 ModItemsArmor.java in the otherwise identical source list). After generating
before.tsv, verify.py --apply applies the validated identifier-only transform and
refuses unknown source content. To reproduce controls append a temporary .java
copy of Controls.java.in to the full-source list and run ChainScanner. No dependency
fetch, full Gradle run or duplicate resource/cache tree is needed.
