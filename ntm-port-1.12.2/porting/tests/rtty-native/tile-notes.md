# Native radio tile boundary

The tile probe compiles complete production sources for all seven radio tiles,
RTTYSystem, TileEntityLoadedBase, TileEntityMachineBase, IControlReceiverFilter,
ICopiable, BufferUtil and ModulePatternMatcher against real 1.12.2 Minecraft,
Forge, Netty and OpenComputers API classes. It does not replace any Minecraft or
Forge class. The original radio source hashes are pinned independently.

## Production delta

- Base and separate logic tile expose `volatile lastState` for the native
  concurrent render snapshot. Initial update tags retain native tile ID and
  coordinates and add the same byte `l` as the existing update packet.
  Initial tags and packets share state application and native render invalidation.
  Minimal render tags deliberately do not call the full persistent NBT reader,
  which would erase separately synchronized channels/mappings from absent keys.
- Sender reads real native redstone power at the original support block and
  direction, then lets native comparator output override it as before. State is
  assigned before native notification because a listener may request the packet
  immediately. Original dirty-before-state order remains unchanged.
- Receiver and logic use native update and six-neighbor notifications; observer
  propagation is false because the original API had no observer propagation.
  Receiver integer clamp is the native equivalent.
- Controller uses native no-drop destruction; explosion configuration and branch
  order are unchanged. Counter uses native stack count; its existing EMPTY-to-null
  inventory boundary is retained. Reader tick logic remains unchanged; only its exact custom-breaking predicate was added.
- RTTYSystem changes only the native FML server/world accessor. Its entire remaining
  original source, including numeric aggregation, timestamp assignment, queue order,
  per-world channel keys and test signal body, is asserted unchanged.
- IControlReceiverFilter changes only its two dirty calls to
  `tile.getWorld().markChunkDirty(tile.getPos(), tile)`. There is no added block
  notification and no inventory/filter rewrite.
- Base, logic, reader, controller and counter have exact native custom-breaking
  predicates; sender/receiver inherit the base predicate. These only route damage
  rendering and do not alter ticking or packets. Their real owner predicate is
  compiled alongside the tiles by both probes.
- NetworkPack ranges 50 (ordinary family) and 15 (counter), polling, receiver stale
  thresholds, condition precedence, original NBT keys, byte-buffer field order,
  and server tick registration remain unchanged.

## Tests

Run from any directory:

    python3 tile-probe.py --module /path/to/ntm-port-1.12.2 --java-home /path/to/jdk8 --classpath "$NATIVE_CLASSPATH" --pristine /path/to/ntm-original-pristine

Result: 491 assertions, including all 16 initial/packet states; identity-bearing
initial tags; change-only render invalidation; volatile field reflection; all six
sender sides through real World/IBlockState/Block weak-power and vanilla redstone
callbacks; comparator override; receiver parse/clamp/remap and timestamp edges;
all ten logic operators and precedence; reader/controller/counter gates; exact
filter dirty-call owner/position and counts; NBT/byte-buffer round trips; actual
FMLCommonHandler server access; two real WorldServer identities and timestamps;
queue delayed visibility, sum, overwrite, noninteger fallback and retention.
Source checks independently preserve all five persistence/buffer implementations
against the pinned pristine original.

## Deliberately bounded collaborators

The test reuses the explicit external-domain collaborators from
`probe-machine-core.py`: atmosphere/dimension routing, DirPos, packet dispatch,
and the production packet envelope extracted without its unrelated client handler.
Additional collaborators are CompatHandler's marker interface, exact isolated
Compat.getTileStandard body, ore-name lookup, bedrock-ore grading, and explosion
processors/effects. Explosion configuration/physics and production network
threading are not accepted by this probe. The real filter interface and actual
pattern matcher are compiled, not stubbed.

World subclasses capture notifications without running a Minecraft server.
The queue test installs a temporary IFMLSidedHandler proxy and allocates real
native server/world instances without starting them, restoring the prior delegate.
This is an API and behavioral parity probe, not whole-mod startup, network
transport, live client rendering, optional-mod integration, or server acceptance.
