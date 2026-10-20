# Spotlight gameplay-owner native proof

## Finite production ownership

Only these existing classes were changed by the gameplay-owner task:

- `com/hbm/blocks/machine/Spotlight.java`
- `com/hbm/blocks/machine/SpotlightModular.java`
- `com/hbm/blocks/machine/SpotlightBeam.java`
- `com/hbm/blocks/machine/BlockBeamBase.java`

All final production hashes are printed in `native-spotlight-owners-FINAL.log`; the parent-owned combined scope manifest freezes their integration with the native render implementation.

Callbacks now use native 1.12.2 states/positions/facings. Spotlight and beam retain every metadata value 0–15. Spotlight state delegates to actual `SpotlightModelState`. Resource accessors retain the original OBJ and texture assets without loading legacy client HFR classes. Constructor sound remains inherited STONE. Bounds retain the original float calculations, with explicit non-full/undefined attachment faces and no collision.

## Reproduction

Run from the port root:

```sh
python3 porting/tests/probe-native-spotlight-owners.py \
  --classpath "$(cat /workspace/shared/nagaport-tick-preparation/classpath)" \
  --java-home /workspace/shared/nagaport-toolchain/java8
```

Terminal evidence: `native-spotlight-owners-FINAL.log`, 11,600 passing checks. The script compiles every complete owned production class, the real `TileEntityData`, actual model-state helper, and production enums/direction/transform interfaces. It never extracts or substitutes production owner methods, and supplies no Minecraft or Forge replacements.

## Proven within the bounded native fixture

- Real Forge registries and native World/Chunk state storage and block/tile callback dispatch.
- All 16 metadata values; exact float bounds, world-space selection, no collision, passability, map color, side-attachment predicates, normal/pick/silk on-variant drops, original light/hardness/sound, and structure rotation/broken transformations.
- Native placement superclass rejection, six facing values, all stone/wood slab metadata and double slabs, and modular same-instance-only connections.
- Actual native-created `TileEntityData`, all 64 incoming-direction bit combinations, independent block color metadata, NBT round trip, unchanged-state tile retention and missing-tile cancellation.
- Six-direction recursive beam length-minus-one extent, colors, crossing direction unions, first-color retention, independent ray removal, glass obstruction and failed placement cancellation.
- Native on/off replacement and source destruction, real redstone-block power, original four-tick scheduling arguments, power disappearing before callback, original broken-bit guards, support-loss drops, beam obstruction break callbacks, and source back-propagation after obstruction removal.
- Original client guards and client-side repair behavior; recursive repair only for the exact original block identity, intact activation rejection, no consumable use, and the external celestial lookup/add/modify calls and order.

## Explicit boundaries and shared-owner audit

The test uses a bounded chunk provider with native Chunk objects, bypasses light-engine recomputation, captures entity output, and records schedule requests before invoking the actual update callback. It does not claim a running WorldServer scheduler, game/server boot, chunk persistence/reload, networking, or GL rendering.

Four external NTM collaborators are declared inline in the probe: ModBlocks registry references, CelestialBody, CelestialBodyTrait, and CBT_Lights. CelestialBody's generic trait lookup and varargs mutation signatures preserve the production API. These record repair notifications; production planetary trait persistence/intensity is not proven or changed.

`TileEntityData` was not edited. Its existing default Forge `shouldRefresh` recreates the tile when an external caller changes the beam block's metadata. The native test explicitly exposes this boundary rather than masking it. Spotlight's owned algorithms do not change an existing beam's color metadata; crossings preserve first color, and `setDirection` changes only the tile's field. No unrelated shared-tile behavior fix is claimed.

`FloodlightBeam` is the only other `BlockBeamBase` subclass. Its outer class overrides only `createNewTileEntity(World,int)`, whose signature remains native and unchanged. It has no overriding legacy block callback lost by the base migration. Its inner tile's legacy world/chunk operations remain unresolved and unchanged; no complete Floodlight owner closure is claimed. `SpotlightModular` is the sole Spotlight subclass and its connection helper remains callable with coordinate arguments.

## Owner-task artifacts

- `NativeSpotlightOwnersTest.java`
- `probe-native-spotlight-owners.py`
- `native-spotlight-owners-FINAL.log`
- `spotlight-owner-proof.md`
