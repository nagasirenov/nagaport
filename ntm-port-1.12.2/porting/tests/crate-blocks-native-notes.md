# Native crate owners and steel beam probe

## Production scope

- `BlockCanCrate`: native Forge 1.12.2 state/hand/drop/sound APIs; original random contents methods remain byte-for-byte identical to pristine.
- `BlockSupplyCrate`: the complete class and complete nested `TileEntitySupplyCrate`, not extracted method snippets. Original sparse `slotN` plus `amount` item payload and ordered `items` tile list remain distinct formats. Legacy null and native empty holes are both skipped during player-removal packing without reducing `amount`; placement restores holes as native empty stacks. Placement still appends, tile reads still clear before restoring, and tile writes do not add broad null normalization.
- Both crate constructors bind wood sounds. All 16 metadata values have native MODEL/CUTOUT_MIPPED state dispatch, nonopaque/nonfull faces, UNDEFINED face shape and original full collision/selection.
- The approved supply tile lifecycle override compares block identity, preventing native Forge metadata-state updates from erasing stored inventory while still refreshing on block replacement.
- `BlockSteelBeam`, `IToolable` and `INBTBlockTransformable` are compiled unchanged from the parent-owned production sources. The beam assertions cover all metadata, narrow original bounds, no tile, placement yaw, unchanged structure transforms, rejected tools, unconditional face rendering and native item drops.

## Run

From repository root:

```sh
set -o pipefail
python3 porting/tests/crate-blocks-native-probe.py \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath "$(cat /workspace/shared/nagaport-tick-preparation/classpath)" \
  2>&1 | tee porting/tests/crate-blocks-native-proof.log
```

Terminal result: 10,573 assertions pass, including 1,000 independent seeded can-content runs and every original can-pool index.

## Evidence and boundaries

The runner hashes and compiles the whole actual production owner sources against existing real Minecraft/Forge dependencies, plus actual `ModSoundEvents` and `RefStrings`. No native classes are mocked, no production source is rewritten into the probe, and no native-looking compatibility facades are generated. The harness subclasses real `World` with real `Chunk` storage and native tile lifecycle, using in-memory chunk access and observing spawned entities and sound calls. Native `ItemStack`, NBT, block drops, gamerules, player inventory and Forge registries execute directly.

Controlled NTM collaborators are explicitly limited to item registry fields, food effects outside the exact pristine food enum, and the unused tool-recipe `ComparableStack` collaborator needed to compile unchanged `IToolable`. This is bounded owner/lifecycle proof, not a whole-mod build, client rendering screenshot, audio playback test, or end-to-end gameplay claim. The parent owns client dispatch/model and sound-resource integration evidence.

The runtime suite covers survival/creative, server/client and `willHarvest` removal combinations; exact centered packed-item spawn; ordinary-versus-crowbar destruction; both activation hands; opposite-hand rejection; no crowbar consumption/damage; contents order/count/tags; native empty omission; no duplicated crate drop; game-rule suppression; tile save/load order and empty holes; sparse item serialization with both legacy null and native EMPTY; placement append semantics; metadata tile retention and block-identity invalidation; original integer sound location, BLOCKS category, event object, volume and pitch. Native hand assignment emits its own equip sound, so activation measurements start after fixture equipment setup.

The standalone Forge registry warns about fixture `crate_probe` and actual `hbm` namespace registration without an active mod container. These warnings are expected in this bounded process; compilation and all assertions complete successfully.
