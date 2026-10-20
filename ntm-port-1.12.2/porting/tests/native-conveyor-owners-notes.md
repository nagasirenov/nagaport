# Native conveyor owner closure

## Scope

The complete existing owners migrated are `BlockConveyorBase`, `BlockConveyorBendable`, `BlockConveyor`, `BlockConveyorDouble`, `BlockConveyorTriple`, `BlockConveyorExpress`, `BlockConveyorLift`, `BlockConveyorChute`, and `BlockConveyorClassic`.

No production conveyor API, `EntityMovingItem`, registry, client proxy, or unrelated owner is changed by this slice. Rendering/material geometry and the `ConveyorModelState` helper are the separate coordinated native renderer slice.

## Native contract

- A single listed `metadata` integer property retains all values 0–15. The default is 0 and decoding masks to the saved nibble. Classic shares the same property identity without changing its independent superclass or adding tooltip behavior.
- Every owner returns native `MODEL`, `CUTOUT_MIPPED`, nonopaque and nonfull. Ambient occlusion is a model-owned contract; this owner slice does not disable it.
- Base and Classic explicitly return native `BlockFaceShape.UNDEFINED` on every face. The other seven owners inherit it, including full-height lift/chute collision cases. This retains the original non-normal block support semantics without inventing slab-top solidity. Native fence and torch attachment paths do not infer support from collision height.
- All nine constructors retain the original default stone sound type, volume 1 and pitch 1; no sound mutation is introduced.
- Base and Classic delegate `createBlockState` and `getExtendedState` to the actual shared `ConveyorModelState`. Its neighbor snapshot is unlisted and never used for movement or collision predicates.
- Base/Classic selection and collision use local `(0,0,0)–(1,.25,1)` boxes. Lift uses half-height only when a belt is below, no belt is above, and no enterable block is above; otherwise full height. Chute selection and collision retain the original full cube. Native engine methods translate these boxes to world coordinates.
- Placement keeps yaw arithmetic and metadata order 2/5/3/4, writing once with flag 2. Screwdriver paths keep rotation, sneaking path cycles, replacement target order (standard → lift → chute → standard), and flag 3. Other tools still return false without mutation.
- Standard, lift and chute drop wand metadata 0; express/double/triple retain 1/2/3. Classic retains its self-item drop.
- `IConveyorBelt`, `IToolable`, and mod-owned tooltip signatures remain unchanged. Minecraft callbacks are native state/position APIs.
- Entity collision retains server-only, age `>10`, live-item guards, then construct, copied stack, snap, zero rotation, spawn attempt, original death. A rejected spawn still kills the original, matching legacy behavior; a thrown spawn prevents the subsequent death statement. No opportunistic correction was made.
- Existing immutable `Vec3d` repairs are retained: base/classic movement uses clamped horizontal origins after snapping; lift/chute preserve the original origin on vertical paths and clamp only their horizontal path. Express x3, chute x5/x3, double lane offsets and triple thresholds are unchanged.
- Legacy render IDs/icon bodies are removed only after coordinating their material ownership with the native loader. Classic has no invented registration or default material: `setModelTexture(ResourceLocation)` accepts an explicit, nonnull full native atlas location, and `getModelTexture()` throws a clear exception until set.

## Evidence

Run from the repository root:

```sh
python porting/tests/probe-native-conveyor-owners.py \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath "$(cat /workspace/shared/nagaport-tick-preparation/classpath)"
```

The probe compiles all nine complete production owners, the complete production `ConveyorModelState`, `ITooltipProvider`, `ForgeDirection`, and all relevant production conveyor/tool interfaces against the cached actual Minecraft/Forge 1.12.2 classpath. It explicitly disables incidental source discovery with an isolated source path. There are no shadow `net.minecraft` or `net.minecraftforge` types.

`ConveyorOwnersNativeTest.java` uses real native `World`, `Chunk`, `BlockState`, `EntityItem`, `EntityPlayer`, `ItemStack`, NBT, and Forge registries. Bounded world subclasses intercept external entity spawning and observe state flags while forwarding native block writes to real chunks.

The recorded final run in `native-conveyor-owners-proof.log` passes **469,591 assertions**, including **101,880 movement-vector comparisons**. It covers all sixteen metadata values, curves and invalid direction nibbles, exact lane thresholds, speed zero, four vertical neighbor categories on both sides, negative coordinates, y0/y255, selection/collision engine dispatch, every owner/metadata/face support shape, real fence and torch rejection with positive stone controls, default stone sound identity/volume/pitch, drops, yaw boundaries, all screwdriver/sneak/tool combinations, conversion guards/copy/order, and all 64 belt-neighbor snapshots plus enterable distinctions and clean-state identity.

Movement expectations are generated from verbatim pristine 1.7.10 method bodies, not reconstructed from the migrated implementation. Only oracle type names, a clearly labeled mutable vector, native math symbol spellings, and an old-coordinate read adapter over the real world are supplied. The log fingerprints every pristine owner and every compiled production dependency. The preexisting baseline immutable-vector corrections were also reviewed directly against the final diff.

Controlled NTM boundaries are explicitly limited to registry identity fields, `RecipesCommon.ComparableStack` (unused tool-registration storage dependency), I18n lookup (not exercised), and an `EntityMovingItem` shell extending the real native `Entity` for observing conversion handoff. These are not claims of production registry, tool lookup, tooltip UI, actual moving-item tick, server/save, renderer/GL, or full-mod acceptance.


## Final support-callback review

Pristine Base and Classic both explicitly return `renderAsNormalBlock() == false`; the family has no slab/top-support specialization. Inspection of the cached native `Block` bytecode established that `getBlockFaceShape` defaults unconditionally to `SOLID`, independently of `isFullCube`. Native `BlockFence.canConnectTo`, `BlockTorch` side-placement, and `Block.canPlaceTorchOnTop` consume that face shape. Therefore preserving only nonopaque/nonfull was insufficient.

The new face-support test was run before the fix and failed immediately at Base metadata 0, DOWN. The narrow Base/Classic overrides then passed the complete suite. Existing native `isSideSolid`/`isTopSolid` behavior is also asserted false, so no additional production support override or collision change was necessary.
