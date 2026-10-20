# Native rotated-cube crane owners

## Result

The complete six production block classes and complete production `TileEntityCraneBase` compile against the cached Minecraft/Forge 1.12.2 classpath. `NativeCraneOwnersTest` passes **15,477 assertions** with real native World, Chunk, Block, ItemStack, inventory, NBT, Container, registry, redstone and tile-packet classes. No Minecraft/Forge classes or production owner/base-tile methods are replaced, extracted, or patched for this proof.

Production ownership is only `BlockCraneBase`, `CraneBoxer`, `CraneExtractor`, `CraneGrabber`, `CraneInserter`, `CraneUnboxer`, and `TileEntityCraneBase`. Interface/state/render-handler/model changes are a separate sibling-owned slice. No shared tile base, concrete crane engine, ModBlocks, ModItems, ClientProxy, spotlight, or conveyor source was edited here.

## Preserved contracts

- All 16 metadata values survive native state and real Chunk storage; the original IRON material, default STONE sound, full opaque cube, zero-damage block drop, and MODEL rendering remain.
- Texture resource names are original sprites under `hbm:blocks/`. The base constructor initializes its own six baseline resources privately without subclass virtual dispatch. Concrete constructors initialize the exact original directional mappings. All 19 active declared resource fields are exposed through an immutable, nonnull catalog.
- Inventory metadata still describes output, while world metadata describes input. The pristine texture selectors and rotations are independently evaluated for every metadata value, six faces, five concrete owners, and all seven output override values. The original extractor/unboxer inverted texture and turn mappings remain.
- Placement uses the original piston integer-corner float proximity tests, strict vertical boundaries, fixed 1.82 height and yaw map `{2,5,3,4}`. With native feet-relative Y, original `posY + 1.82D - yOffset` becomes `posY + 1.82D`, not `getEyeHeight()` and not native center-relative `EnumFacing.getDirectionFromEntityLiving`. The 840-case proof includes sneaking, float rounding and yaw boundaries. Source cross-check: the original repository's inverse `BlockPlasticExplosive.determineOrientation` formula and the 1.7.10 vanilla-source mirror at https://www.javatips.net/api/WildAnimalsPlus-1.7.10-master/build/tmp/recompSrc/net/minecraft/block/BlockPistonBase.java .
- Two-pass insertion retains merge-before-empty ordering, restricted slot order, inventory and existing-stack limits, native side/slot validity predicates, NBT/damage equality, null remainder convention, input mutation/copy differences, dirty calls, and even the original empty-slot inventory-only limit behavior.
- Ingress predicates, redstone gating, local buffering, destroyer behavior, package/input side differences, centered remainder drops and native comparator calculation remain.
- Seeded drops retain exact half-open slot ranges, random-consumption order, count splitting, float placement and Gaussian motion, NBT copies, original item/damage through native depletion, rejected-spawn behavior and comparator-before-removal ordering. Original helper-plus-owner double superclass removal remains.
- The five crane descendants are the only production `TileEntityCraneBase` subclasses. They, `TileEntityMachineBase`, and `TileEntityLoadedBase` define no custom `shouldRefresh`. The approved crane-only override refreshes only when block identity changes. Real native Chunk mutation verifies inventory, output override and tile identity survive every metadata change; replacement with another block removes/invalidates the tile.
- Screwdriver toggles/swaps, flags 3/4, output-before-input settings notifications, base NBT byte format, missing-key retention, client cached redraw behavior and packet serialization/accessor roundtrip remain. Crane-owned `getUpdateTag()` explicitly delegates to `writeToNBT(new NBTTagCompound())`, restoring the legacy full initial tile description rather than native TileEntity's private base-only writer. A regression fails before this fix and passes afterward: actual full and partial `SPacketChunkData` construction and byte roundtrips carry complete crane data, and native client `handleUpdateTag` restores output override/inventory and matching world textures for all five concrete owners. The existing filter-copy index predicate and mode order are deliberately not repaired in this migration. `TileEntityCraneBase` retains CRLF line endings.

## Explicit proof boundaries

The native World fixture supplies loaded genuine Chunks, skips lighting calculation and records entity outputs/notifications. It does not generate terrain, run a server, persist chunks or transport packets. Packet serialization itself uses native `SPacketUpdateTileEntity`, full/partial `SPacketChunkData` and `PacketBuffer` bytes; client tag application uses native `handleUpdateTag`.

The five concrete crane engine tiles and `TileEntityMachineBase` are explicitly bounded NTM inventory collaborators. The entire production crane base tile is compiled and executed, but concrete engine ticking and production shared-base persistence are not claimed. Copy/filter interfaces, unrelated item tools/MainRegistry, tooltip translation and InventoryUtil are explicit external collaborators; InventoryUtil delegates native side slots and its furnace branch, without claiming its production brick-furnace special case. Native activation branches are exercised except actual GUI/network opening. No GL rendering, live dedicated-server launch, or whole-mod compilation is claimed here.

## Reproduction

Run from the project root:

```sh
python porting/tests/probe-native-crane-owners.py \
  --classpath "$(cat /workspace/shared/nagaport-tick-preparation/classpath)" \
  --java-home /workspace/shared/nagaport-toolchain/java8
```

Artifacts: `NativeCraneOwnersTest.java`, `probe-native-crane-owners.py`, `native-crane-owners-proof.log`, this note, and `native-crane-owners-source-manifest.json`. The portable probe defaults to the pinned byte-identical pristine fixture archive (and accepts `--pristine` for an explicit tree). The proof prints SHA-256 hashes of the pristine oracle source, generated oracle and every complete production input. The dedicated manifest freezes only this worker's seven production files.
