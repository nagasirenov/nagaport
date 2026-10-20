# Native conveyor wand gameplay workflow proof

## Result

PASS: **244,865 assertions and 49,176 pristine routing scenarios** in one clean native compile/run. The retained output is `conveyor-wand-workflow-proof.log`; source and test fingerprints are in `conveyor-wand-workflow-source-manifest.json`.

The probe compiles the **entire actual ItemConveyorWand**, WorldInAJar, ILookOverlay, all nine frozen conveyor block owners, ConveyorModelState, actual EntityMovingItem and EntityMovingConveyorObject, actual faux BlockPos/Rotation, and their listed API/helper dependencies. It does not source-extract production methods or substitute Minecraft/Forge classes. Final EntityMovingItem includes the `HAS_ITEM` accounting fix and SHA256 `978c436c8d6beba7fdd3c4fbc08d71bf05272b53478621e8e338f85c07d81d68`.

## Independent oracle and finite coverage

The original 519-line ItemConveyorWand is read directly from the pristine checkout, SHA256 `ab6d137b50190a1f33443a9a592f044ab73ce17dfcc64b541adf7e4f687a6c3f`. The oracle extracts its nine entire routing/helper methods, not any migrated production logic. Adaptations are confined to access visibility, the old coordinate World facade, independent preview storage, and the native MathHelper spelling. The original conditionals, iterations, distances, direction choices, block choices, and metadata arithmetic remain intact.

- **48,384 full Cartesian cases:** seven endpoint deltas `(0,0,0)`, `(3,0,0)`, `(0,0,3)`, `(-2,0,-3)`, `(3,2,1)`, `(-1,-2,2)`, `(0,3,0)`; all six start and target faces; four conveyor types; no obstruction, blocked starting cell, and midpoint obstruction; limits 0/1/5/16; yaw -45.01/45; ordinary and crane-type targets. Each case compares dry-run return values with zero world writes, exact preview block/raw-short-metadata contents with no route-world mutation, and actual native placement results, complete stored states, write order, and flags.
- **288 additional placement cases:** lift/chute target identities, all face pairs and types, covering their special turn-to-target rule.
- **504 additional placement cases:** 21 yaw values around quadrant boundaries, all types and both vertical faces, at a negative-coordinate anchor and near y=0/y=255. These preserve the original single-block max-zero bypass and exact native metadata.
- **384 immediate-sneak cases:** server/client, survival/creative, all types/faces, straight/bent source metadata, open/blocked destinations. Source conversion before obstruction testing, native placement yaw, consumption even in creative and on the client, and the absence of a new canPlayerEdit gate are asserted.

The total routing count includes the two focused placement-only sets; those 792 cases are not claimed as additional dry-run/preview comparisons.

## Actual gameplay callback checks

Native Item/ItemStack callbacks cover subtype identity/count/names, legacy invalid-subtype behavior, exact selection NBT, capture-time available count, inventory-order/subtype-only consumption, survival synchronization, creative non-consumption for completed routes, no partial placement on a failed dry run, stale captured-count behavior, offhand stack selection with the original main-inventory count domain, and actual ItemStack NBT serialization. Start and end conveyor auto-snapping are exercised.

Client onUpdate executes with a real native Minecraft object, RayTraceResult, player, item, WorldInAJar, and production wand. Tests compare the exact preview handoff contents/origin/bounds/success, no world mutation, unchanged-hit caching, the 15-degree yaw threshold, missing-hit reset, held/subtype selection cancellation, and client completion cleanup. **RenderOverhead is an explicitly bounded recording sink; no on-screen preview or OpenGL drawing is claimed.**

WorldInAJar checks include signed-short coordinate metadata, native state metadata, all six out-of-range boundaries, ignored outside writes, tile identity, clearing all three arrays on nuke, raw lightlevel values, inert strong power, plains biome, DEFAULT world type, existing height/extended-level helpers, null-face fallback, and metadata-sensitive slab side solidity.

## Genuine Forge break path

The existing native event launcher applies Forge's actual EventSubscriptionTransformer. A real Forge event subscriber observes/cancels actual `BlockEvent.BreakEvent` instances dispatched by actual `ForgeHooks.onBlockBreakEvent`. Tests cover positive experience results, cancellation (-1), spectator permission cancellation, block survival past a canceled position, corrective SPacketBlockChange state, native block destruction, packet handoff, creative/sneak/MP/client guards, and recursion depths 0/1/2/3/32. The original bidirectional recursion can revisit the clicked block; the expected 61-event straight-line traversal and depth boundaries are preserved rather than redesigned.

## Fixture boundaries and exclusions

- Actual native World subclasses delegate storage writes to native World/Chunk and only record outcomes. They use actual WorldProviderHell to avoid unrelated skylight work during repeated finite matrix resets; lighting is not under test.
- Normal players are constructed native EntityPlayer subclasses. EntityPlayerMP is allocated without launching a server and populated with inherited state from a constructed player plus a real PlayerInteractionManager. A native NetHandlerPlayServer subclass records outgoing packets instead of opening a network connection. Thus this is not a live-server acceptance test.
- A real Minecraft instance is allocated without graphics startup for the client ray-trace callback. Keyboard tooltip drawing, overlay rendering, resource registration, and an on-screen preview remain outside acceptance.
- Explicit mod-owned bounded collaborators: ModBlocks/ModItems registry holders, localization, MainRegistry proxy, BlockCraneBase type identity, RenderOverhead preview sink, and the unchanged unused constructor-only RecipesCommon.ComparableStack boundary required by the actual IToolable API. Recipe/tool registration is not invoked.
- All conveyor owners and the moving entity engine are whole actual sources. No owner methods or entity surrogate are used. Spawn/dead behavior is not accepted by this wand proof; it has its separate engine proof.
- No network, installation, staging, or commits were performed. No prior frozen artifact was edited by this workflow test task. This is not a successful full-mod compile or a graphical RenderOverhead/ParticleDebris migration.

## Reproduce

From the repository root:

```
python3 porting/tests/conveyor-wand-workflow-probe.py \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath "$(cat /workspace/shared/nagaport-tick-preparation/classpath)"
```

The default performs the full clean temporary-directory run. `--section routes` and `--section callbacks` are development subsets; their output identifies the selected section and does not replace the retained full proof.
