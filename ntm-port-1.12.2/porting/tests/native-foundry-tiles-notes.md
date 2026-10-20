# Foundry tiles and shared network dispatcher: bounded native proof

Frozen on 2026-10-19 against checkpoint 32, `e8970b85e0c8fe46f17348330b3853bb32ffaa0b`.

## Production scope

The final Channel/Tank sources also opt into native breaking-pass rendering through `canRenderBreaking() == true`; this is the only coordinated renderer-worker addition, preserving the original damage overlay without changing transfer bodies.

Six edited complete foundry tile classes: `TileEntityFoundryBase`, `Channel`, `Tank`, `Outlet`, `Slagtap`, and `CastingBase`. Complete `TileEntityFoundryBasin` and `Mold` need no further edits and are included in the selected compilation and runtime proof. One approved shared-core change in complete `UniNodespace`: the removed static server accessor becomes the genuine Forge `FMLCommonHandler.instance().getMinecraftServerInstance().worlds`. No server lifecycle rewrite, fallback world array, or replacement networking implementation.

Native block lookups, positions, redstone, block scheduling and dimension access replace removed APIs at the original sites. Initial native chunk sync returns the same complete NBT payload as incremental `SPacketUpdateTileEntity`; packet receipt still calls the original full reader. Same-block property changes preserve a tile; replacing its block refreshes it. Server update notification uses `notifyBlockUpdate(state,state,3)` without adding neighbor mutation; client invalidation uses a native render range. The original `shouldClientReRender() && typeChanged || amountChanged` precedence is deliberately unchanged.

Casting keeps its original nullable internal `ItemStack[]`, not a blanket conversion to `EMPTY`. Native IInventory ingress/output and native item NBT decoding normalize only that API boundary. Slot reset-on-read, signed byte index checks, duplicate-last-wins, `items`/`slot` keys, stack cap 64, output-only sided extraction, no player GUI/insertion, and all existing notify/dirty sites remain. Newly required native inventory field methods expose zero fields; `clear` clears the nullable array. Existing block/TESR owners must treat public IInventory output as native `EMPTY`, while their direct internal array accesses keep original null semantics.

## Preserved behavior, including original oddities

- Tank downward fill has priority, followed by side acceptors, then same-tank randomized swap/integer equalization. The original shuffled direction list and world RNG calls are untouched. Original tank block side-flow rejection is represented in the explicit test owner boundary, so it cannot accidentally bypass the equalization branch.
- Channel acceptor-first flow, overflow identity, `lastFlow`, five-tick timer, one-quantum swap, network-wide material exclusion, node creation/destruction, and unload soul retention are unchanged.
- Standard addition returns null for full acceptance without decrementing its input; overflow mutates and returns the original leftover object. No new authority checks are inserted into these public methods: the original server tick/caller remains the authority boundary.
- Casting starts at cooldown 100, then uses 200; clamps before cooldown, keeps a consumed material for the original one-tick interval, copies the recipe output, and marks dirty only at the original successful-cast site. Timers/cache fields remain unsaved. Client ticks do not clamp, transfer, cool, or craft.
- Outlet filter inversion, redstone XOR, side predicate, four-block pouring ray, copy-tool keys and particle payload are unchanged. Filter remains a short in NBT; type/amount retain original integer keys and null material sentinel -1.
- Slagtap keeps the fifteen-block ray, exact fill/overflow accounting, one-tick scheduling and particle timing. The original no-hit `flow` returns null without mutating its supplied amount; this is tested, not silently repaired. No added dirty calls, recovery defaults, capacity rebalance, or old-world conversion.

## Reproduction and terminal evidence

```
python3 porting/tests/probe-native-foundry-tiles.py \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath "$(cat porting/continuation-2026-10-19/native-probe-classpath.txt)"
```

Terminal log: `porting/continuation-2026-10-19/native-foundry-tiles-FINAL.log`.

- Complete eight tile classes, loaded-tile/NBT/raytrace/material foundations, and complete `UniNodespace`, `GenNode`, `NodeNet`, `FoundryNetwork`, and provider compile against the cached genuine Forge 1.12.2 jar.
- **260 runtime checks PASS**, inside the genuine LaunchClassLoader and Forge EventSubscriptionTransformer.
- Actual native World/Chunk block placement registers the complete production ITickable tile, and actual `World.updateEntities()` executes crafting and channel-node initialization. Actual world tile-removal processing calls unload and removes ticking while preserving the original node soul; invalidation destroys the original node/network.
- Native NBT/ItemStack serialization, native registered TileEntity reconstruction, complete initial update tags, and actual `SPacketUpdateTileEntity.writePacketData/readPacketData` round trips preserve material, amount, muffling and both inventory slots. Invalid native item decoding, signed/out-of-range slots, duplicate entries, full/partial extraction and native empty boundaries are covered.
- Real World ray tracing reaches receivers/slag placement. The test checks exact transfer/overflow, source/receiver identity, particle payload and native one-tick schedule calls.
- Real Forge server-handler lookup obtains an actual native DedicatedServer. The full production dispatcher traverses its actual `worlds` array and joins real nodes only within matching worlds/providers.

## Explicit boundaries and remaining acceptance

There are no fake Minecraft/Forge platform classes. The Java harness subclasses the actual native World with real chunks; its provider supplies only preloaded chunks, no terrain generation or save/network transport. Redstone power is an explicit boolean input, scheduled slag ticks are captured at the native World method boundary, and host blocks are controlled native BlockContainer owners. The separate foundry owner/model/TESR proof compiles the real block owners with these eight actual tiles.

The runner lists every controlled NTM collaborator in its `collaborators` dictionary: ModBlocks/ModItems registry field holders, the material registry maps and MaterialStack value shell, OreDictManager.DictFrame, ItemMold recipe fixture, Compat lookup/mod-presence boundary, Library direction constants, ICopiable signature boundary, exact nested FoundryNode shell, BlockDynamicSlag tile fields, and packet/particle transport capture. Real production NTMMaterial, MaterialShapes, CrucibleUtil, ForgeDirection, node algorithms and loaded-tile superclass are compiled, not replaced. The dispatcher proof allocates native server/world identity objects without booting an independent dedicated server and injects the actual FMLServerHandler server field; this is a startup boundary, not server lifecycle acceptance.

The real ItemMold icon/registry/recipe root, the real slag block's scheduled simulation, broader ICopiable helpers, multiplayer delivery, whole-mod loading and visual/gameplay acceptance remain outside this selected gate. The eight tile proof does not claim the entire foundry or whole mod is playable. No dependencies are downloaded. Fixtures are pristine original source copies, with hashes in the scope manifest; tests need no runtime Git history or network.
