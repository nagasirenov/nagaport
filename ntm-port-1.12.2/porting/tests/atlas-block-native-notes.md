# Native atlas block mechanics probe

## Result and reproduction

The final successful run exited 0 with **2,648 checks**. Its complete output, including production-source SHA-256 hashes, is saved in `porting/native-atlas-block-test.log`.

```sh
python3 porting/tests/atlas-block-machines-native-probe.py \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath "$(cat /workspace/shared/nagaport-tick-preparation/classpath)"
```

## Actual compilation and exercised behavior

The probe compiles complete production `MachineCapacitor` (including its complete nested tile), `BlockFluidBarrel`, `BlockPipe`, `HEVBattery`, `DecoPoleTop`, `BrownBarrel`, `YellowBarrel`, and `RedBarrel`, plus complete `BlockDetonatable`, `BlockFlammable`, and `BlockBase`. Actual sound definitions, persistent helpers, interfaces and `TileEntityLoadedBase` are included. Minecraft, Forge and OpenComputers are real dependency classes, not test facades.

Native World/Chunk dispatch tests all 16 metadata states, placement, collision/support shape, tile retention/replacement, drops, harvesting, inventory spill RNG/NBT, comparator updates, hand-aware interaction, battery charging/sound/removal, and pipe orientation. Capacitor tests cover six-direction bus traversal, subscription/provision ordering, transfer limits and preserved quirks, packet layout, persistent/world NBT, ROR and OC callbacks. The full nested capacitor is checked against pristine source after explicitly enumerated native API substitutions and the approved lifecycle addition.

Additional barrel cases exercise native `onBlockExploded` → `onExplosionDestroy` dispatch, server/client and identity branches, fuse RNG, flammability, neighboring fire and projectile callbacks, radiation/particles, oil scheduling, LOX freezing, and independently seeded taint/radon/toxic-generation decisions. Explicit metal-sounding fluid barrels retain original stone samples at volume 1 and pitch 1.5, with STEP for hit/fall; the default constructor remains STONE.

## Explicit proof boundaries

Registry identities, energy endpoint transport, packet delivery, GUI screens, fluid storage/engine, armor eligibility, blast effects, radiation, oil generation and primed-TNT entity behavior are controlled NTM collaborators. The tests prove owner decisions and arguments, not those collaborators' implementations. Native ItemBlock instances provide registration/drop fixtures; the unrelated legacy `ItemBlockBase` tooltip dispatch is not certified.

Complete production `TileEntityBarrel` is not compiled. Its exact approved `shouldRefresh` override is copied unchanged into the storage fixture; removal of that hunk must restore the pinned pre-existing whole-file SHA-256. All existing barrel-tile inventory, fluid, NBT and wire bodies remain unchanged and outside this proof.

Real native Chunk transitions verify capacitor/barrel metadata changes keep the same tile, while block-identity changes (including corrosion replacement) refresh it. This probe is not a full-mod build, live client/world smoke test, or visual geometry/material/AO certification; the separate atlas model probes cover rendering data.
