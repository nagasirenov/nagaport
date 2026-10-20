# Weighted chest loot migration

Implemented in `com.hbm.lib.LegacyWeightedLoot` and the Minecraft adapter
`com.hbm.lib.WeightedRandomChestContent`. All 31 imports of the removed vanilla
class were eliminated: custom loot consumers use the mod-local adapter and the
two vanilla loot integrations use 1.12 loot tables. `ItemPool.getStack` and the red-room pedestal
use the same local weighted selection rather than the removed array overload of
`WeightedRandom.getRandomItem`.

The contract was checked against the read-only pristine `ItemPool`,
`HbmChestContents`, pool definitions and generation consumers, and the Forge
1.7.10 implementation:

- https://github.com/MinecraftForge/MinecraftForge/blob/1.7.10/patches/minecraft/net/minecraft/util/WeightedRandomChestContent.java.patch
- https://github.com/MinecraftForge/MinecraftForge/blob/1.7.10/src/main/java/net/minecraftforge/common/ChestGenHooks.java

Counts include both bounds. Weight zero is unselectable. A chest roll draws an
entry, draws its count, then writes copies to random slots. Collisions replace
contents, including contents from earlier rolls. Counts exceeding the item's
stack limit split into individual items, each with a separate random slot draw.
This deliberately retains the legacy loot distribution and random draw order.
The adapter copies the original `ItemStack` before setting count, retaining its
metadata and NBT; a generated zero count becomes `ItemStack.EMPTY`. The template's
own count is ignored, including a zero-count template retaining a raw item.
Availability and item stack limit are evaluated on a separate copy normalized to
count 1; output copies receive the drawn count before checking emptiness. This
retains metadata/NBT and slot RNG consumption for zero-count templates without
mutating the template or the `ItemStack.EMPTY` singleton.

Malformed negative weights, empty/zero-total pools, overflowing total weights and
invalid count intervals raise explicit exceptions instead of silently producing
different loot. Existing fallback lookup in `ItemPool.getPool` is unchanged.

Run from the project directory:

```sh
JAVA_HOME=/path/to/jdk8 bash porting/tests/run-weighted-loot-test.sh
```

The test compiles and executes the **production generic algorithm**, without
Minecraft/Forge stand-ins. It covers exact weighted bucket boundaries, inclusive
and fixed counts, zero counts, absent source items, oversized/unstackable loot,
slot overwrites and malformed input. For 1,000 seeds (20,000 rolls) it compares
every insertion and resulting inventory against an independent legacy oracle.
Evidence: `porting/weighted-loot-semantic-test-2026-10-19.log` (JDK 8u504, exit 0).

The actual target Forge 14.23.5.2768/stable_39 API was inspected with `javap`:
`porting/weighted-loot-target-api-2026-10-19.log`. The three production helpers
and `WeightedLootMinecraftTest.java` were then compiled against the real mapped
jar and executed against real Minecraft classes, without substitutes. Native
test PASS covers `ItemStack` count, metadata, NBT isolation, canonical empty,
`InventoryBasic` slot replacement/splitting, event insertion and reload,
weight/count preservation, retained vanilla entries and roll settings, and an
item queued before its registry name is assigned. Zero-count template regressions
also check raw item/metadata/NBT recovery, three RNG draws for a one-item roll,
five draws for three unstackable items, and two draws without insertion for actual
`ItemStack.EMPTY`. Evidence:
`porting/weighted-loot-minecraft-test-2026-10-19.log`. The latter log includes the
exact classpath; its Minecraft dependency versions must take precedence over
older dependencies of Gradle plugins.

`VanillaLootHandler` replaces the old `ChestGenHooks` registration in
`MainRegistry`. It queues `(Item, weight)` during preInit, resolving registry
names on `LootTableLoadEvent` after deferred item registration. It adds all 13
original entries to the corresponding vanilla table's `main` pool, except bonus
chest which uses the resource pool `pool3`. Every original
entry has min=max=1, metadata 0 and no NBT, matching an item entry without
functions. Existing 1.12 pool rolls, bonus rolls, entries and other pools are
retained. In the actual target bonus chest, `main` guarantees an axe, `pool1`
guarantees a pickaxe and `pool2` supplies food. NTM `no9` competes only with
sticks/planks/logs in `pool3`, preserving the tool and food guarantees. A target
table missing the expected selected pool fails explicitly rather
than silently omitting the NTM additions.

The native regression reads the actual target
`assets/minecraft/loot_tables/chests/spawn_bonus_chest.json` and parses it through
`ForgeHooks.loadLootTable` with `LootTableManager`'s real Gson configuration. Since
that API freezes tables after the event and raw target jars lack FML's event class
transformations, the test creates mutable pool wrappers around those real parsed
entries and rolls, then invokes the native event callback directly. It compares
the complete serialized axe/pickaxe/food pools and all original resource entries,
counts and rolls before/after addition of a real `Item` fixture representing
`no9` with weight 7. It does not claim to test normal FML event dispatch.

`LibraryDungeon` now fills all eight chests with `CHESTS_SIMPLE_DUNGEON` via
`WorldServer.getLootTableManager()` and `LootContext.Builder(server).build()`.
The vanilla baseline is now Minecraft **1.12.2**, including its loot distribution,
rolls and inventory placement. Exact vanilla 1.7.10 distribution is not preserved
by this integration migration. Custom NTM `ItemPool` generation keeps the legacy
algorithm described above. Mineshaft additions use the first (`main`) rare-item
pool in 1.12, an explicit adaptation from the single 1.7.10 category; exact vanilla
relative probabilities across the target's separate pools differ. Vanilla table names and constructors were checked
against the real target jar and the official Forge 1.12.x `LootTableLoadEvent`,
`LootEntryItem`/`LootPool`/`LootTable` patches and `ForgeHooks.readPoolName` sources.

- https://github.com/MinecraftForge/MinecraftForge/blob/1.12.x/src/main/java/net/minecraftforge/event/LootTableLoadEvent.java
- https://github.com/MinecraftForge/MinecraftForge/blob/1.12.x/patches/minecraft/net/minecraft/world/storage/loot/LootEntryItem.java.patch
- https://github.com/MinecraftForge/MinecraftForge/blob/1.12.x/patches/minecraft/net/minecraft/world/storage/loot/LootPool.java.patch
- https://github.com/MinecraftForge/MinecraftForge/blob/1.12.x/patches/minecraft/net/minecraft/world/storage/loot/LootTable.java.patch
- https://github.com/MinecraftForge/MinecraftForge/blob/1.12.x/src/main/java/net/minecraftforge/common/ForgeHooks.java

Run the pristine comparison with:

```sh
python3 porting/tests/verify-vanilla-loot-migration.py
```

It verifies every original item/table/weight/count registration and all eight
library chest consumers. Evidence:
`porting/weighted-loot-source-parity-2026-10-19.log`.

These are diagnostic compile and native API test results, not a full mod build or
a client/dedicated-server launch. Use a seeded test world to inspect structure
chests, vaults, satellite cargo and pedestal drops after the full mod builds;
save/reload representative NBT-bearing stacks. LibraryDungeon's unrelated old
world/block/generator APIs remain part of the broader port.
