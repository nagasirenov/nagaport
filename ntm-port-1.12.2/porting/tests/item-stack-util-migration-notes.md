# ItemStackUtil: complete native utility root

Production ownership: only `src/main/java/com/hbm/util/ItemStackUtil.java`. The standalone runner compiles the **entire unchanged-on-input production class**, not extracted methods, with real Minecraft/Forge 1.12.2 classes and `ItemStackUtilMinecraftTest`. No substitute Minecraft/Forge classes or Unsafe are used. No Gradle task was run during this change.

## Reviewed migration

- `getCount`/`setCount`/`shrink`, native compound access, `new ItemStack(NBTTagCompound)`, `JsonToNBT.getTagFromJson`, BlockPos tile/comparator calls and native EntityItem getter/world spawn.
- Registry lookup uses **Item.REGISTRY.getNameForObject**, not just assigned `getRegistryName`. A native test proves a named-but-unregistered item still returns null, while vanilla membership returns `minecraft`.
- Array helpers retain nullable elements, inclusive range semantics, full versus truncated shape, original exceptions and odd cases. Positive oversized counts remain supported for display labels/copies. `carefulCopyArray(null)` still throws; the explicit-range null overload still returns null.
- Nested container reader retains `count == 0 -> list.tagCount()`, signed-byte slot keys, iteration over **requested count**, record ordering and last-valid-duplicate semantics. It does not infer capacity from maximum slot. A requested count smaller than list length still processes only that many records. Native-empty loaded records are skipped, so absent/default records cannot erase a valid slot 0. Writers omit native EMPTY while retaining null omission.
- Shared native EMPTY never receives new count/NBT in the mutation helpers. This is intentional native-boundary handling, **not universal parity for legacy malformed/zero-count stacks**. In particular, old nonnull zero-count spill slots consumed three offset floats; native empty slots consume none. Native empty identities cannot reproduce old zero-count item resurrection. Ordinary null-input behavior remains: malformed JSON with a null target is ignored, while a valid compound with a null target throws.
- Spill snapshots item, metadata and tag reference before shrinking to zero, then deep-copies the tag onto the emitted native EntityItem. It retains supplied-Random offsets, nextInt(21)+10, decrement, EntityItem construction, Gaussian motion and spawn order. Spawn rejection still consumes/decrements the source as in original; the method is not a transactional retry API. The original noninventory tile cast still throws, and comparator notification occurs whenever an inventory tile exists, even when empty.

## Evidence

`porting/continuation-2026-10-19/item-stack-util-native-probe.log`: **532 native assertions passed**, standalone Java 8 compilation/run. Tests cover copy isolation, lore replacement/prefixes, oversized labels, JSON replacement/invalid input, registry membership, native OreDictionary, sparse/default/invalid/duplicate/signed-byte NBT records, EMPTY safety, and native World/TileEntityChest/EntityItem spill fixtures.

Spill oracle uses the original literal integer-count/RNG arithmetic rather than calling production to derive expected output. Four seeds × six positive counts (1, 9, 10, 21, 64, 130), plus a multiple-slot case, assert emitted item/meta/count/NBT, last depleted chunk, per-slot offsets, exact float Gaussian motion, source count at spawn, same live source alias, NBT independence, comparator/coordinates, and subsequent RNG tail. Fixture world captures entities and returns false from spawn intentionally to establish original mutation behavior even on rejection. This is native headless integration, not world/chunk gameplay.

Manifest pins the reviewed native full-file digest and records pre-migration/pristine hashes. The source check is a review-scope guard, not a semantic test or proof that original Minecraft 1.7 was executed. Runtime fixtures provide behavioral checks; no local Git objects or pristine checkout are required to run them. Change the digest only after reviewing a further source change.

## Reproduction

Portable Python runner (Java 8 and resolved target classpath required):

    python porting/tests/probe-item-stack-util.py --java-home <jdk8> --classpath "<native compile classpath>"

On Windows use the platform classpath separator `;`; the runner uses `.exe`, `os.pathsep`, UTF-8 compilation and normalized-newline source hashing. No local absolute path is hardcoded. Source-only scope check:

    python porting/tests/probe-item-stack-util.py --source-only

Optional Gradle integration for the parent/common runner:

    gradlew -I porting/item-stack-util-probe.init.gradle testPortItemStackUtil

Use `-PportPython=python` if Python is installed under that name. These tasks do not modify main sourceSets/compileJava. They were prepared but not run by this worker.

Native JSON accepts compound syntax via the target parser and rejects noncompound/trailing-invalid inputs. Complete legacy parser grammar equivalence is not established; configurable recipe NBT remains a separate integration gate.

## Still open

This utility now supports the container-NBT migration, but HazardSystem, transformers, player tick/protection, item-container implementations and HbmLivingProps remain separately unported/unverified. No offhand rule, shielding formula, activation formula or radiation gameplay behavior was changed. Every live gate in `porting/inventory-radiation-acceptance.md` remains unchecked.
