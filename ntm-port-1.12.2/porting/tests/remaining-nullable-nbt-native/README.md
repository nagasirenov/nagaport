# Remaining nullable NBT reader batch

Base: sealed CP78 `89b6bfc71a8b532c2fe7cdbd43bb66b7991ee365`.
Production commit: `09e909f1`. This is 19 loader invocations in 14 files, plus their helper imports, and nothing else. The existing `NullableInventoryStackNbt` is unchanged. `manifest.json` pins before, after and pristine bytes; reversing precisely those imports/invocations reproduces every CP78 file byte-for-byte, including CRLF. `scope-audit.json` accounts for all 22 remaining loader calls in the CP78 compiler inventory.

## Selected contracts

- BlockLoot: complete nested tile read/write bodies retain the existing list object, append rather than clear, skip missing/unregistered records, retain registered zero-count records, and preserve coordinates. Writer counts holes, skips null quartet/stack entries, and does not remove stale output tags.
- BlockSkeletonHolder: complete nested tile read/write bodies replace the nullable item on read; writer only writes non-null items, preserving stale tags in a reused output compound.
- PistonInserter: complete persistence bodies preserve extend/retract/state and nullable `stack`. This does not change its separate network reader.
- EntityMinecartContainerBase: complete inventory read/write bodies keep uppercase `Items`/`Slot`, unsigned byte slots, array recreation, bounds checks, duplicate last-wins behavior, and null omission. Runtime uses the actual `EntityMinecartCrate.getSizeInventory()` body (54); it is not a test of every subclass or native inventory interface.
- EntityRideableRocket: the complete drive read/write branches retain null on absent or invalid drive records. Other entity state and lifecycle are outside this slice.
- EntityArtilleryShell, ItemAmmoArty, TileEntityTurretArty: cargo read/write tails, actual complete `setCargo` and `groundDespawn`, the full cargo tooltip branch and full turret cargo-assignment branch are exercised. The actual CargoShellCraftingHandler full-stack writer statement provides the valid input. Tagged-but-missing cargo still produces the original tooltip null dereference; non-cargo metadata still skips assignment. No new guard, stack conversion, projectile spawning or metadata behavior is introduced.
- TileEntityCraneConsole: complete NBT read/write bodies retain every scalar field and nullable `held` stack.
- TileEntityPASource: complete NBT read/write bodies plus actual Particle field declarations and complete constructor retain both nullable inputs, constructor aliases, scalars and enum direction behavior. Missing `particle` still leaves the old particle untouched; missing input still becomes null and the existing unguarded writer still throws. No particle simulation or accelerator-world behavior is claimed.
- TileEntityMachineAutocrafter: the actual GUI full-stack writer fragment feeds the actual two loader declarations from control handling. Complete source is reversed and the original inventory/matcher consumer statements are checked. Runtime does not execute packet delivery, slot dispatch, inventory implementation, matcher initialization, or world marking.
- IMCBlastFurnace, IMCCentrifuge, IMCCrystallizer: all seven native-record loader expressions are exercised with their actual keys, including one-based centrifuge outputs. Complete `process` bodies are source-compared to pristine after the loader substitution. Output rejection, item-vs-ore fallback, null-to-ComparableStack behavior, and the existing ore-list behavior are unchanged. Runtime does not execute the complete IMC process methods or fake recipe registries, ore dictionaries, logger or mod initialization. Incoming native named-ID ItemStack records are the selected schema; legacy numeric-ID import is not claimed.

## Three deliberate exclusions

1. PistonInserter `deserialize`: its serializer sends only the item's custom tag, not full ItemStack NBT. Changing a loader cannot establish the intended wire format.
2. TileEntityMachineEPress `deserialize`: the same custom-tag-only serializer mismatch. Its file is unchanged.
3. Compat `scrapeItemFromME`: external AE2 storage record plus a separate integer count override. Native external storage/import schema is unproven; its file is unchanged.

These remain genuine old-loader compiler errors. No silent wire repair, native EMPTY conversion, old numeric-ID world importer, null guard, or unrelated API migration is included.

## Native validation

`run.sh` extracts the selected production bodies directly from pinned source, checks their original counterparts and exact whole-file reversal, compiles them against genuine mapped Forge 1.12.2, and runs 387 checks. Six owners use complete NBT bodies with superclass calls omitted. Rocket, shell, GUI/control, artillery and IMC use explicitly bounded fragments; small host declarations exist only to hold original fields, with no fake Minecraft classes. The production helper, Tuple, ForgeDirection and EnumUtil compile unchanged. Reflection observes native ItemStack raw count only in assertions; it does not alter production behavior.

The same invocation freshly reruns the existing 191-check `bomb-inventory-nbt-native/NativeInventoryNbtTest.java`, pinned by `reused-helper-proof.json`. Those 191 checks are reused helper coverage, not newly authored owner tests. They exercise native item NBT hooks and Forge capability serialization. The genuine ItemStack class hash is verified. The old 1.7 runtime oracle is not rerun in this batch; existing evidence is pinned, not presented as a new run.

Counts, native empty state, native serialization, tag aliases, null-vs-registered-zero records, sparse slots, stale output compounds, particle constructor aliases and original failures are checked. The complete mod does not compile yet. These tests do not establish full owners, a game launch, world saves, network interoperability, radiation/environment behavior, or recipe acceptance.

Run with Java 8 and the already-provisioned genuine classpath:

```
JAVA_HOME=/workspace/shared/nagaport-toolchain/java8 \
MINECRAFT_CLASSPATH="$(cat /tmp/nagaport-ae2-native-classpath.txt)" \
bash ntm-port-1.12.2/porting/tests/remaining-nullable-nbt-native/run.sh
```

No downloads or dependency changes are performed.

## Concurrent Shell ownership

The projectile worker separately ports Shell metadata in this checkpoint. The default verifier remains an isolated whole-source guard and must not be weakened to accept that overlay. `verify_merged_shell_slices.py ROOT` independently verifies byte-exact cargo reader/writer tails, `setCargo`, `groundDespawn`, the import and unchanged helper against a composed tree. It deliberately does not approve the rest of Shell. Compose it with the projectile worker's metadata guard and the parent's integration inverse receipt. It freshly passes against the combined main tree; this does not replace isolated whole-file reversal or constitute full-mod validation.
