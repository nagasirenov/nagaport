# Native tile lookup/cache migration

Baseline: `15b0a104`. Production scope is exactly `api/hbm/tile/ILoadedTile.java` and the `Compat.getTileStandard` method. Frozen after focused tests on 2026-10-19. No packet, LoadedBase, inventory, energy formula, or network subscription implementation changed.

## Changes and native semantics

- Cache dimension key reads `WorldProvider.getDimension()`.
- Lookup constructs native `BlockPos`, checks `world.isBlockLoaded(pos, false)`, then calls native `world.getTileEntity(pos)`.
- Native 2768 bytecode: World delegates the loaded check using arithmetic X/Z shifts; WorldServer uses its provider's `chunkExists`. WorldClient with `allowEmpty=true` returns true even for missing chunks, so the one-argument overload is unsuitable. Explicit false tests the provider's empty fallback. Actual normal Chunk.isEmpty returns false.
- Avoid `IChunkProvider.getLoadedChunk` as a replacement guard: server implementation clears `unloadQueued`. The chosen guard does not. Subsequent native getTileEntity still uses provideChunk and can clear that flag, preserving normal tile lookup behavior.
- Keep native pending-tile handling and IMMEDIATE tile creation by calling World.getTileEntity rather than directly consulting a chunk map.
- World access and cache calls remain owning-world-thread operations; neither old cache nor this migration establishes thread safety.

## Evidence and reproduction

Pinned native artifact: Forge 1.12.2-14.23.5.2768 / stable_39, project `.gradle/minecraft/forgeBin-1.12.2-14.23.5.2768-PROJECT(ntm-port-1.12.2).jar`. Dependency provenance is recorded in `resolved-toolchain-2026-10-19.json`.

Run without Gradle:

```
python3 porting/tests/probe-tile-access-cache.py --parity-only
python3 porting/tests/probe-tile-access-cache.py --java-home "$JAVA_HOME" --classpath "$NATIVE_CLASSPATH"
```

The Gradle init adapter `tile-access-cache-probe.init.gradle` exposes `testPortTileAccessCache` and supports `-PportPython=python`; it was not executed in this batch. It does not change production source sets. The probe uses platform-specific Java executable suffixes, classpath separators and explicit UTF-8.

The probe accepts a caller-provided native classpath and Java 8 home, uses a temporary output directory, and reverses exactly the two substitutions and compares normalized whole-file SHA-256 hashes recorded from baseline 15b0a104. Git and the baseline commit object are not required. It compiles whole production ILoadedTile, Tuple, and Spaghetti **linked to a test class containing only the exact extracted production Compat method**. Full Compat has unrelated unresolved dependencies and is NOT compiled by this probe. No fake Minecraft APIs are supplied.

`continuation-2026-10-19/tile-access-cache-native.log`: PASS, 31 checks. Native WorldServer, WorldClient, ChunkProviderServer, ChunkProviderClient, and Chunk are exercised. Unsafe allocation bypasses unrelated game/server/client bootstrap constructors and reflection installs test world storage and chunk entries. Native lookup methods themselves are not replaced. A separate native World subclass controls clock/tile identity for cache-specific checks. This is a focused API/semantics test, not a running game, whole Compat build, or network/furnace acceptance.

Checks include server/client absent chunks, no creation, explicit client allowEmpty regression, negative chunk boundaries, normal loaded client chunk, server tile resolution, pending tile precedence, out-of-height positions, unload queue guard/downstream semantics, client removed chunk, 20/60 TTL endpoints, invalid/unloaded expiry, scratch key cloning, dimension separation, replacement delay, backwards clock, long overflow and configurable zero TTL.

## Deliberately preserved anomalies

- Cache identity is xyz+dimension, not world identity or logical side. Same-dimension world collision is characterized in the tests, not repaired.
- Public static HashMap/shared mutable scratch Quartet, cloned insertion keys, check order, TTL arithmetic and public mutable TTL constants are unchanged.
- Backwards world time and long expiry overflow behavior remain unchanged.
- No periodic purge, world-unload clearing, or non-ILoadedTile chunk-lifecycle check is added.
- TileEntityLoadedBase's one-way `isLoaded=false` onChunkUnload behavior is outside this batch. No onLoad/validate restoration added.
- Network subscriptions remain wall-clock-expiring through NodeNet/PowerNetMK2, and furnace's 40-tick refresh is untouched.

Full production compilation and real client/dedicated-server acceptance remain prerequisites for any end-to-end claim.
