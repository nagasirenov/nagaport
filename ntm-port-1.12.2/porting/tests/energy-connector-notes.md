# Native energy connector boundary

The four production files `IEnergyHandlerMK2`, `IEnergyProviderMK2`,
`IEnergyReceiverMK2`, and `IEnergyConductorMK2` only replace removed Minecraft
coordinate fields with native `getPos()` component access, world-provider
`dimensionId` with `getDimension()`, and the unsubscribe lookup with native
`World.getTileEntity(BlockPos)`.

Do not replace the legacy domain BlockPos/DirPos used by Nodespace. In particular,
constructor neighbors retain Java int addition/subtraction and overflow, and the
debug position's Y expression retains int addition before conversion to double.
The provider/receiver debug branches retain every RNG call, face sign, magnitude,
and ordering; only dimension access changes. `particleDebug` remains false.
All transfer arithmetic (including negative values and overflow), source debit
following receiver credit and leftover subtraction, dual conductor/receiver
branches, node lookup and registration calls, direct-provision opt-out and
self-exclusion remain byte-for-byte identical after reversing API substitutions.

## Evidence

`energy-connector-manifest.json` stores normalized complete pre-migration file
SHA-256 hashes from baseline 60264ff and counted reversible substitutions. Run:

    python3 porting/tests/probe-energy-connectors.py --parity-only

No Git objects, pristine tree, network, or developer-specific paths are required
for that audit. The native probe accepts `--java-home` and `--classpath`; its
launcher selects `.exe` on Windows, uses `os.pathsep`, and writes UTF-8 sources.
`porting/energy-connector-probe.init.gradle` exposes `testPortEnergyConnectors`.
The init script does not exclude any production files or weaken the normal build.

Native `javap` inspection verified TileEntity.getPos/setPos,
World.getTileEntity(BlockPos), isBlockLoaded overloads, and
WorldProvider.getDimension before fixture execution. Pristine conductor source
was identical before the bounded substitutions.

The fixture compiles the **whole four interfaces**, actual IEnergyConnectorMK2,
ILoadedTile (including its actual cache), ForgeDirection, Tuple and annotations
against real Minecraft/Forge classes. It uses real TileEntity and World subclasses
with controlled tile identity/loading/time. Compat.getTileStandard, legacy int
BlockPos/DirPos constructors/getters, six Library direction constants, and two EC
keys are extracted exactly from current production source. Unrelated unported
legacy positional overloads and broader Compat/Library implementation are not
compiled, and no Minecraft/Forge API is invented or shadowed.

NTM Nodespace/PowerNode/net collaborators are explicit observation controls, not
real topology. They record actual interface lookup and registration calls.
Packet collaborators throw if reached; debug is disabled in production, so debug
branch RNG/TargetPoint parity is a source audit, **not a runtime packet claim**.

88 assertions pass covering default speeds, sink capacity/overshoot, full and zero
transfers, negative and overflow behavior, source debit/sink credit order,
receiver-returned leftovers, provider/receiver rate limits, self-exclusion,
connection denial, direct opt-out, registration, null nodes/nets, hybrid branches,
cache reuse and unloaded invalidation, loading guard behavior, direct unsubscribe
lookup without cache or added loading guard, constructor coordinate/direction order
and coordinate overflow, and debug position integer Y overflow. The World override
records whether lookup/loading guards were requested; it does not claim server
chunk generation behavior. Existing TileAccessCacheNativeTest separately exercises
native client/server lookup behavior. There is **no full PowerNet/NodeNet/
Nodespace/UniNodespace, machine, packet, chunk-loading topology, or gameplay proof**.

No battery, Library, packet, network, domain coordinate, or cache production files
were edited in this step. Full project compile is owned by the parent batch.
