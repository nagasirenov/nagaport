# Direct native player-chunk update scheduling (CP89)

Base: `a65d3acbf05634c3cedbce5eff43ad0bd15cefdb` (CP88).

## Finite production scope

Seven existing server-only statements change directly from
`WorldServer.getPlayerManager().markBlockForUpdate(x, y, z)` to
`WorldServer.getPlayerChunkMap().markBlockForUpdate(new net.minecraft.util.math.BlockPos(x, y, z))`.

- `PneumoTubePaintableBlock`: the screwdriver direction-change notification (one).
- `TileEntityPipeBaseNT`: `setType` (one).
- `TileEntityPylonBase`: `setColor`, `addConnection`, and the peer in `disconnectAll` (three).
- `TileEntityPipelineBase`: `addConnection` and the peer in `disconnectAll` (two).

The native position class is fully qualified because two owners also use the unrelated NTM network-position class. The old coordinate expressions are retained individually, including all three separate `getPos()` calls on a peer. No coordinate is cached early, reordered, collapsed, clamped, or replaced by a mutable tile position reference. The receiver/getter is evaluated before coordinates, coordinates are evaluated left to right once, and native map virtual dispatch occurs after construction. Getter failure and a null world still prevent coordinate evaluation; a null map still fails after the coordinate expressions. Native immutable position allocation and class initialization are ordinary native API adaptation; this proof does not assert equivalence under allocation failure or first-use class-initialization failure.

The enclosing `instanceof WorldServer` guard, dirty calls, tile fields, connected-list edits, node construction/destruction, network-provider choices, fluid/color logic, insertion/ejection direction mechanics, returns, and all other source bytes are unchanged. `baseline.zip`, SHA-256 pins, exact edit positions and complete-file regeneration/inverse checks enforce this for all four files, including mixed line endings. No compatibility facade, replacement player map, early custom packet, network mechanic deletion, or vanilla workaround was added.

This is **not** a `World.markBlockForUpdate` migration. It adds no state lookup or listener invocation and needs no loaded-state/collision-box closure. The 38 remaining old World notification calls, `WorldUtil`'s separate legacy PlayerManager usage, and commented radiation code are intentionally untouched.

## Actual original and native behavior

The actual 1.7.10 `WorldServer` getter (`mt.t`) returns the existing PlayerManager. Actual `mq.a(III)` computes the chunk coordinates and calls its private entry lookup with `create=false`. The actual native getter returns the existing PlayerChunkMap, and actual `markBlockForUpdate(BlockPos)` calls `getEntry` without creating an entry. Both pass the same masked local x/z and original y to the entry's dirty queue, coalescing duplicate packed positions and marking the same section bit. Neither scheduling path reads a block state, tile, chunk provider or World listener, including for unwatched/unloaded coordinates. The test covers integer extremes and out-of-domain heights without introducing a fabricated domain filter.

Deferred native `entry.update()` is also executed with real native Chunk storage: it constructs a genuine `SPacketBlockChange` and tile update packet using the current stored state and later tile data, rather than taking a snapshot at scheduling time. The existing CP88 server fixtures are reused, not replaced by a pretend engine.

Native platform differences remain explicit:

- Native `sentToPlayers=false` entries ignore changes; the original queue did not have that gate. The test requires the native gate and does not remove/emulate it.
- The retained authentic Forge1614 patch grows the original update array, changes clumping thresholds and changes metadata-sensitive tile packet behavior. The authentic original runtime oracle covers only the small duplicate/single-position queue, whose selected behavior is unchanged by those patches. It does not launch a patched Forge1614 server or claim complete batch/wire equivalence.
- Native packet types, state representation, runtime transport and chunk-watcher lifecycle remain native.

The previous proof's `dependency-hashes.json` pins the same original client, Forge1614 userdev and native forgeBin jars. This proof verifies those pins, the original SRG getter/map ownership mappings and byte-identical complete Forge player-manager patch. `boundary-bytecode.txt` records and is checked against the actual original/native getter, existing-entry lookup, queue, native update and integer-constructor bytecode. No original game instructions are modified. Only the original test class is package-relocated to coexist with the signed original default package.

## Reproduce and exact evidence

From the module directory:

    python3 porting/tests/player-chunk-update-native/prove.py --attribute --mutants
    python3 porting/tests/world-update-notification-native/prove.py --attribute --mutants

The supplied Java 8 toolchain, classpath and jars are defaults. These commands do not download dependencies, alter dependency caches, run Gradle or emit classes for the whole mod.

- 12,560 native assertions execute all seven exact production expressions in mechanically generated bounded projections, actual native map/entry queue methods, existing-only scheduling with a null chunk provider/listener list/entry chunk, genuine deferred block/tile packet construction, and receiver/getter/coordinate/null/exception evaluation order. The packet tests observe tile data changed after scheduling.
- 1,491 authentic original assertions execute the actual original direct getter/map/entry chain, duplicate queue and unwatched behavior, and matching evaluation cases.
- Full-source javac **attribution without code emission** identifies exactly the seven old exact-WorldServer/int-coordinate positions and matches them to the CP88 missing-getter diagnostics. Because that legacy method is genuinely absent from 1.12.2, this is receiver/argument attribution, not a claim of successful resolution of the old missing method. Original getter/map ownership is independently established from actual mapping and bytecode. All seven new calls resolve to actual WorldServer getter, PlayerChunkMap method and native BlockPos(int,int,int) constructor owners.
- Two positive and eight foreign/subtype/boxed/wrong-arity/unresolved controls pass in each attribution mode.
- Twelve runtime mutations reject wrong coordinates, wrong peer evaluation, duplicate scheduling, added state reads and deletion of each extracted call. Seven distinct production call-deletion mutations are separately rejected by the complete-file guard.
- CP88's complete-file guard permits only the independently pinned CP89 inverse for the later Pneumo statement, then enforces its original CP87/88 full-file and closure checks unchanged. Its original notification runtime proof still runs separately. This is not a relaxed hash, ignored file, or broad exclusion.

The extracted-expression fixtures do **not** compile or execute complete enclosing network owners or NTM interaction/tick paths. Startup is bypassed with test-only Unsafe. Evaluation probes override getters/coordinate methods only to observe evaluation; queue and packet tests execute actual native engine bodies. Packet delivery stops at the existing recording `PlayerChunkMapEntry.sendPacket` seam after genuine packet construction. Null uninitialized world internals deliberately cause any accidental state/listener/chunk work to fail. No production Minecraft/Forge class is stubbed.

Not certified here: whole-mod compilation or playable JAR, complete owner construction, real player watch/unwatch sessions, launched original Forge runtime, full network transport, chunk batching, save/reload, full fluid/energy/NTM behavior, or migration of other legacy World calls. Parent integration owns the next full offline build and its diagnostic delta.
