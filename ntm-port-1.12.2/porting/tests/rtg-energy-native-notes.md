# RTG native energy/inventory owner proof

Baseline: original source is pinned to `dff345a:ntm-original-pristine`, not an external RTG implementation. The 20-method source oracle normalizes only native world/position and item-tag API spellings; it preserves complete update/provision ordering, heat scaling, decay, lifespan conversion and buffer payload bodies.

## Production scope

- `TileEntityMachineRTG`: native `IInventory`/`ISidedInventory`, native world lookup, NBT item construction, display-name contract, empty-stack API boundary; private nullable inventory remains the original domain model. Added metadata-only refresh retention.
- `TileEntityMachineMiniRTG`: metadata-only refresh retention, no generation, persistence, networking or accounting rewrite.
- `ItemRTGPellet`: native item tag/count/translation access, native right-click/action-result and tooltip callbacks, native vanilla experience-orb sound event. Preserves original decay/scaling equations and item descriptions. Pellet right-click sound and rendered tooltip are compiled, not live-play verified.
- Actual `RTGUtil`, energy provider/receiver/connector/conductor interfaces, loaded tile base and cache compile unchanged. No shared energy API or base-class edits.
- Parent-owned actual `MachineRTG` and `MachineMiniRTG` compile in this same harness, not stand-ins. The seeded break regression identified and now verifies preservation of tag data when native shrinking empties the final source stack.

## Executable evidence

`probe-rtg-energy.py --java-home <Java 8> --classpath <native Forge 1.12.2 classpath>` compiles whole production classes and executes `RTGEnergyNativeTest` with real Minecraft World/Chunk/TileEntity/NBT/ItemStack and actual Forge registries/GUI dispatcher. Current result: 218 assertions.

Coverage:
- Actual block-created tile, native World tick registration and dispatch, all 16 metadata state changes preserving tile and item identity, native default versus explicit retention distinction.
- 48-case source/sink accounting matrix, empty/full/speed caps, receiver remainder, provider speed, six-face original priority order, direct provision and connection refusal, overflow, RTG provision-before-generation and Mini generation-before-provision.
- Original 200/600 heat caps, heat-to-power factor five, 100000 RTG and 1400 Mini power caps, existing 2500/50000 power-RTG branch values without migrating that separate block/render owner.
- Actual utility and pellet decay sequence 40/30/20/10/0, lifespan decrements, depleted item and metadata, disabled decay/scaling, native client authority.
- Native EMPTY inventory boundary, original 64 inventory cap, split/full extraction conservation, all-face insert/no-extract rules, compressed real NBT roundtrip including pellet tag and invalid-item absence, custom name and base muffled field.
- Exact original nine-byte tile buffer (muffled boolean plus power long, no invented heat sync), actual BufPacket serializer coordinate prefix, original radius 50 and sync target.
- Grey/non-grey/client/sneaking block activation, actual Forge GUI dispatcher call to a controlled null-returning GUI handler, Mini's original no-GUI behavior, usability radius, render/solidity flags.
- Seeded original drop partition, item/damage/count, independent copied NBT including final emptied source, positions, velocities, native tile removal and item conservation.

## Explicit boundaries / non-claims

The runner lists every collaborator. Controlled NTM boundaries are: configuration toggles; ModBlocks/ModItems registry references; GUI/container presentation; depleted-material enum; localization and display math; Library direction constants extracted from production; Compat native tile lookup; Nodespace topology; debug particle transport; threaded packet dispatch observation. `BufPacket` retains the original serializer/deserializer body but excludes its still-legacy client transport Handler. The GUI handler deliberately returns null before screen creation or network transmission; this proves dispatch arguments, not a rendered GUI or connected client.

The provider/receiver interfaces and TileAccessCache are actual complete production code. Nodespace is not: this gate proves direct provision and accounting, not cable/network discovery or complete power-network acceptance. Native world uses real loaded chunks but controlled lighting/entity-drop collection. No live Minecraft server/client, disk world reload, GL/rendered screenshot, full repository build or full network/game acceptance claim.

## Re-run

- `python3 porting/tests/check-rtg-energy-source.py`
- `python3 porting/tests/probe-rtg-energy.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)"`
