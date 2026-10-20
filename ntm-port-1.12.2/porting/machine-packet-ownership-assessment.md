# Deferred machine synchronization ownership assessment

Read-only assessment, 2026-10-19. No packet implementation is included in the tile cache migration. Scope examined: TileEntityLoadedBase, IBufPacketReceiver, BufPacket, ThreadedPacket, PrecompiledPacket, NetworkHandler, PacketThreading, PacketDispatcher, ModEventHandler, energy/cache/node interfaces. See electric-furnace-acceptance.md for the required vertical slice.

## Existing wire and send-order contract

BufPacket writes three int coordinates, then receiver serialization. ElectricFurnace serializes super's muffled boolean, power long, maxProgress int, progress int in exactly that order. No window-property truncation or invented dimension field is warranted.

TileEntityLoadedBase constructs and compiles before dispatch. Its duplicate suppression is bypassed whenever total world time modulo20 is zero so unchanged tiles reappear correctly after client chunk reload. PacketThreading prepares PrecompiledPacket before worker submission; preserve that server-thread snapshot instead of serializing mutable tile state from a worker. NetworkHandler codec writes/consumes the compiled readable bytes. Server tick END waits for packet tasks then flushes channels.

## Native scheduling evidence

Pinned Forge `userdev/sources.zip`, 1.12.2-14.23.5.2768:
- NetworkDispatcher routes registered custom payloads through the channel pipeline.
- FMLProxyPacket processes the embedded channel synchronously.
- SimpleChannelHandlerWrapper.channelRead0 calls messageHandler.onMessage directly and has no main-thread scheduling.

Pinned native Minecraft bytecode:
- Minecraft.addScheduledTask(Callable) executes immediately if called on its Minecraft thread; otherwise creates a ListenableFutureTask and synchronizes enqueue to scheduledTasks.
- World.getTileEntity accesses chunk/pending tile state, so direct Netty-thread receiver mutation is unsafe. Scheduling on the client thread is an explicit native safety correction, not a payload-format change and not something byte parity requires preserving.

Registration runs common-side. Introduce any client scheduling/access through the sided proxy/client implementation, avoiding eager client-only API dependencies in common handler classes. A dedicated-server classloading check is required in the coherent sync batch.

## Payload ownership distinction

Our NetworkHandler custom decoder passes `inboundBuf.slice()` to fromBytes. BufPacket stores that borrowed slice. Its existing handler releases it only when a matching receiver exists, within its deserialize finally; no-world/no-receiver drops do not release it.

Pinned native FMLIndexedMessageToMessageCodec.decode duplicates the payload, passes a slice to decodeInto, then explicitly calls payload.release(). **Our custom decoder does not do that release.** FMLProxyPacket implements Packet, not ReferenceCounted. Netty MessageToMessageCodec's automatic release of the wrapper therefore does not prove release of its backing payload.

Consequences:
- Simply scheduling the borrowed slice is not an adequate ownership contract.
- Simply copying the bytes then deleting the old BufPacket release leaks the original backing payload under this custom codec.
- Adding native-style codec release changes the lifetime available to every packet type, including EntityBufPacket and other handlers retaining decoded data. Audit all such consumers before codec-wide changes.
- A scoped BufPacket ownership transfer can be designed, but must spell out who owns/releases the original payload and new scheduled copy, including decode failure. Do not present a broad codec rewrite as a tiny API substitution.

The coherent implementation must test success, malformed payload, no world, missing/unloaded chunk, removed/invalid/nonreceiver tile, scheduling rejection/failure, and disconnect/world replacement before queued application. Own and release scheduled bytes exactly once on every path. Preserve FIFO arrival application. World/connection epoch checks should be explicit decisions because wire coordinates alone cannot identify another world or replacement tile generation.

## Existing anomalies requiring explicit separate decisions

- LoadedBase duplicate-return path leaves the compiled packet buffer allocated.
- Replacing lastPackedBuf abandons the previous copy; no unload cleanup.
- PacketThreading queue clearing discards tasks without releasing their packet buffers.
- Send exception bypasses post-send buffer release.
- ThreadedPacket getter/codec consume reader indexes; repeated dispatch/recompile needs ownership scrutiny.
- Pool size validation appears inverted; do not silently fix it.
- PacketThreading.init was found called by the command reset path, not ordinary MainRegistry startup in the inspected source.
- LoadedBase initializes isLoaded true and only sets false on chunk unload; onLoad/validate reset is absent. This is not fixed by cache modernization.

## Next coherent boundary, not implemented here

LoadedBase native coordinates/world/dimension + BufPacket owned decode/apply + sided client scheduling + ThreadedPacket/NetworkHandler/PacketThreading verified ownership and flush ordering. Keep IBufPacketReceiver format, packet discriminator ordering, resend timing and furnace wire layout. Use native Netty codec ownership tests, scheduler/drop-path tests and dedicated-server loading evidence before claiming sync closure. Full production and client/server machine acceptance remain pending.
