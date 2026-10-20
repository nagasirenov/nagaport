# Bounded native stack loading: restored inventory ingress

Base: CP52 e0d4369. Three factory calls are migrated, not the complete 70-call root:

* BlockStorageCrate.onBlockPlacedBy, restored slot loop.
* BlockMassStorage.onBlockPlacedBy, restored slot loop.
* EntityMinecartCrate restoration constructor, restored slot loop.

Each passes directly into an existing setter that already normalizes null or isEmpty to the legacy nullable slot representation. Crates and mass storage share TileEntityCrateBase's setter; the cart uses EntityMinecartContainerBase's setter. Existing clamping and dirty notification are unchanged. The nested tag accessor is also made native at these three expressions so that the exact loop can compile and execute. No general nullable loader, fake platform class, new compatibility layer, count coercion, import mapping, or balance change is introduced.

## Evidence and boundaries

67 assertions execute the actual original client ItemStack factory via an isolated classloader and actual native Forge2768 ItemStack constructor, plus verbatim complete production restoration loops and inventory setter methods in explicitly bounded native fixtures. Native bootstrap registers real canonical Items. Cases: valid identity/count/damage, missing and unknown IDs, empty compound, missing slot, Count0 and -1, stack-limit127→64, alias identity/mutation, deep-copy independence, compressed binary save/reload, original dirty notification count. An additional full-production MachineBase + LoadedBase existing regression passes130 checks using its already documented controlled NTM collaborators. No platform stand-ins are used in the new test.

The three whole block/cart classes, placement event dispatch, actual minecart/world construction, lock metadata, stockpile, break/drop behavior, and a running mod are not validated by this bounded test. The binary write loop in the new fixture is test setup, not a claim that a whole production block/drop path compiles. The full production MachineBase regression is supplemental evidence for the established inventory boundary, not compilation proof for these three owners.

Original vanilla bytecode has factory-null only for absent item, not zero/negative count. Actual Forge1614 userdev FML/Forge patches are retained: FML changes read item assignment to delegate-aware func_150996_a but still reads numeric short id, and Forge does not replace loader/serializer. This is patch inspection, not execution of a fully launched Forge1614 server. Native saves use string registry names. A direct native constructor does not load a raw numeric ID compound; legacy world import and registry remapping are separate unresolved work, not a blanket assertion that old worlds can never interoperate.

Important observed native Forge distinction: getItem() masks nonpositive-count stacks as AIR, while this Forge2768 copy() and writeToNBT() retain the raw item identity. The test checks all three facts. Therefore the remaining67 nullable/domain loader calls are not mechanically replaced: armor mods, recipe descriptors, render payloads, direct nullable slot assignments and zero-count identity consumers need owner-specific decisions. Treating every native empty result as null would delete legacy domain descriptors; merely using a constructor would turn old null returns into nonnull empties. This patch changes only the already-normalizing ingress paths.

## Reproduce

From module root:

    python3 porting/tests/native-stack-loading/run.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)" --legacy-client /tmp/nagaport-legacy-client.jar
    python3 porting/tests/probe-machine-core.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)"

The new test extracts current exact loops and setters at runtime; changed call expressions are rejected and changed normalization/side effects are exercised. It does not mutate production sources or alter Gradle source sets. No full Gradle build is claimed.
