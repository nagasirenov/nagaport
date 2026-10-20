# Bounded native RBMK debris owner closure

## Scope

Only these production owners changed:

- `src/main/java/com/hbm/blocks/machine/rbmk/RBMKDebris.java`
- `src/main/java/com/hbm/blocks/machine/rbmk/RBMKDebrisBurning.java`
- `src/main/java/com/hbm/blocks/machine/rbmk/RBMKDebrisRadiating.java`
- `src/main/java/com/hbm/blocks/machine/rbmk/RBMKDebrisDigamma.java`

No ClientProxy, ModBlocks, sourceguard, existing accepted test, packet implementation, contamination engine, staging, commit, or network changes were made by this closure.

The base now has a native `METADATA` property for all 0..15 values, default 0, `MODEL` rendering, `CUTOUT_MIPPED`, nonopaque/nonfull states, undefined face shape and unchanged full-block selection/collision. The obsolete renderer ID is removed.

The complete gameplay subclasses use real 1.12.2 callback/state/world APIs. Fire uses `SoundEvents.BLOCK_FIRE_AMBIENT` (the original `fire.fire`), `SoundCategory.BLOCKS`, null excluded player and the unchanged volume/pitch draws. Scheduled update delays, server/client guards, metadata transitions, gas emission, extinguishing/decay probabilities, directional order, packet NBT/coordinates/target, proxy dispatch and random-call order are retained. Radiation keeps symmetric range expansion through `grow`, the original null-entity explosion resistance sum, inverse-square formula, strict damage/range thresholds, and main-hand-only same-stack marshmallow damage mutation.

## Evidence

`porting/tests/rbmk-debris-native-probe.py` compiles all four complete production classes and the actual NTM ForgeDirection enum against cached real Minecraft/Forge 1.12.2 dependencies. There are no Minecraft or Forge facade classes. It pins SHA-256 digests of all four pristine files and requires each entire gameplay subclass to equal its pristine source after an explicit, bounded native-API substitution list.

`porting/tests/rbmk-debris-native-test.java.in` exercises real native block states, chunks, World callback dispatch, entities, ItemStack, NBT and Forge TargetPoint. Test worlds record scheduling/sounds and provide entity-query fixtures. Controlled NTM collaborators are limited to registry handles/sand enum, particle-packet construction/transport, client effect dispatch and contamination sink.

Terminal result: exit 0, **74,997 checks**. This includes:

- All 16 metadata states for all four classes, model/layer/material/face/collision contracts and real native chunk state changes.
- Added callbacks and seeded initial packet/schedule behavior; explicit client callback invocation because native client chunks do not dispatch server-only added callbacks.
- 6,720 seeded burning/radiating update scenarios, seven neighbor configurations, four decay states and both logical sides; exact packet, sound, write/schedule trace and random-stream parity against an independent behavioral oracle.
- All six digamma directions, eight neighbor block identities, both sides, metadata reset, schedule-on-conversion, no repeat source scheduling and original six-face traversal order.
- Exact radiation query bounds, inverse-square output, resistance clamp/sum, zero-distance behavior, negative-coordinate diagonal floor ray, strict five-/ten-block thresholds and player RNG eligibility.
- Empty/non-marshmallow/already-roasted/unroasted main hands, offhand exclusion and mutation of the same stack without changing counts.

Full command:

```sh
python3 porting/tests/rbmk-debris-native-probe.py \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath "$(cat /workspace/shared/nagaport-tick-preparation/classpath)"
```

Final evidence: `porting/rbmk-debris-native-probe.log`. Initial complete-owner compile evidence: `porting/rbmk-debris-native-initial-compile.log`. `git diff --check` for these four owners and Python probe syntax validation also pass.

## Boundary

This establishes the four-owner native API/gameplay contract, not a full-project compilation or launched-game rendering proof. External packet wire serialization/transport, particle renderer, contamination engine effects, renderer registration/baking and complete ModBlocks registry are outside this compile cohort. Their production call sites, payload values and ordering are verified; their full implementations are not replaced or claimed to be integrated by this fixture.

Production freeze hashes:

- RBMKDebris: `ad90e085eff050a1db1975049db8cfcd2734e278583f049c90575223f00b10cb`
- RBMKDebrisBurning: `af5b78989213b64a992188ebec85fe7b2e394efdd9fb3779f1c62ef44e269ded`
- RBMKDebrisRadiating: `a53fdd9f1506f2585c423c742496f7709c9f97aff7dde9cbb27f1eb933eeb9d3`
- RBMKDebrisDigamma: `d058e1d1a0b927c6e91f13506957127b3c7e44e6b861ccb9fe85ae22013ced28`
