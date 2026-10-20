# Original-semantic path search on native 1.12.2 APIs

Base: CP48 `4117777482629b159d6cfa5dcc20c1f4ec5aa311`. Local, bounded migration. No whole-mod build or playable-mob claim.

## Production scope

- Complete `PathFinderUtils`: native `Path`, world/profiler/block-state/cache APIs; original target extension, ground search and profiler sequencing retained.
- New complete `NTMPathFinder`: the actual original four-neighbour search expressed with native `Entity`, `IBlockAccess`, `Path`, `PathPoint`, `PathHeap` and `IntHashMap`. It is not a fake Minecraft class, old-API emulator, or wrapper around the changed native search.
- Exactly five call arguments change `worldObj` to `world`: one FBI call and four Glyphid calls. No attack, speed, target, lifecycle, path sink, callback placement or flag changes.
- Existing native `BlockMetalFence` and `IBlockMulti` are unchanged and compiled in the focused test, because the search distinguishes NTM's original custom-render fence from vanilla fences.

The three existing files have byte-exact CP48 inverse/forward mappings. The new helper is hash-pinned. `check-mapping.py` rejects 12 drift mutations. This is source-identity proof, not gameplay proof.

## Semantics retained

- Original squared-Euclidean step cost and heuristic, original heap tie ordering, strict candidate distance-to-target admission, closest partial endpoint, packed-point-key aliasing, and no new 200-iteration bound.
- Four cardinal neighbours, in evaluation/insertion order +Z, -X, +X, -Z. Original one-block step decision, fall callback order/count, water/lava/fence/door/trapdoor decisions, and no native diagonal/hazard-malus policy.
- Original float half-block target conversion, bounding-box start rounding, width/height rounding, and start-in-water surface scan with temporary water-avoidance suppression/restoration.
- The initial surface scan uses the provided block cache. Collision checks intentionally use the entity's live world, as the original static routine does.
- The wrapper extends even a nearby target to `maxDist`; cache bounds use `floor(posY + 1)` and `(int)(maxDist + 16)`; ground search is Y through Y-9, then Y+10 down through Y+1, unchanged on failure. Its original exception/profiler behavior is retained, without an added `finally`.
- Caller flags remain unchanged. The third flag is water *avoidance*, despite the old `allowWater` name. The fourth enables the start-in-water surface search.

Native block APIs alone are also not perfectly equivalent. A complete audit of the 1,160 representable canonical block states shared with original vanilla exposed three extra differences, retained locally in this search rather than changing global block collision:

1. Original snow layers are path-passable at every thickness. Native `BlockSnow.isPassable` rejects layers five through eight.
2. Original cauldrons inherit solid-material path rejection. Native cauldrons are passable.
3. The original partial-target ground scan accepts end-portal frames as normal cubes. Native state `isNormalCube` does not.

The old engine inferred rails/fences/walls from render IDs. Native type checks recover the corresponding original content: NTM RailGeneric/RailHighspeed inherit original rail type 9; vanilla fence/wall type 11/32 map to native classes. NTM BlockMetalFence is explicitly excluded from the type-11 condition: its original `getRenderType()` returns a custom allocated ID. Open oak fence gates remain governed by their passability callback before the original closed-gate check. Only original oak-door/trapdoor identities receive the original identity-specific flags; new wood species are not silently reclassified as original oak doors.

Unknown third-party blocks whose old render ID and native type disagree are not certified. Impossible old metadata combinations that native block states cannot represent, such as upper-door metadata 12, are not made into synthetic native states. Canonical states and valid paired doors are tested; corrupt-save conversion is a separate concern.

## Actual original differential oracle

`run.py` verifies the supplied original client, original Forge1614 patch ZIP, and pristine NTM wrapper SHA256. No PathFinder/PathPoint/Path/PathEntity Forge patch exists. Reviewed Forge block/entity patches do not alter the old path decisions exercised here. Forge BlockRailBase changes its render getter to a mutable instance renderType initialized to 9; pristine NTM does not call the introduced setRenderType setter. A foreign mod changing a rail render ID is outside the certified original-content scope.

The original `ayg`/`aye`/`ayd`/`ayf` class bytes execute unchanged alongside actual native Minecraft classes. The original NTM wrapper source is compiled with only explicit obfuscated API spelling substitutions; its control flow and original mutable Vec3 arithmetic execute against the original game classes. It is not a newly written reference algorithm or a rehost of the algorithm on native block behavior.

For the test only, original class bytes are copied unchanged into a temporary unsigned archive, allowing default-package fixture subclasses to coexist without signed-package conflicts. The supplied original client is never modified; the temporary archive is deleted and not distributed. No original game JAR is a production dependency.

Controlled boundary subclasses provide voxel world storage, chunk storage, profiler observations and entity water/lava/fall callbacks. They extend the actual original/native World, Chunk, Entity and Profiler classes. There are no fake `net.minecraft`/Forge classes. `Unsafe` allocation skips unrelated world startup only. The original NTM metal-fence render-ID boundary is represented by an actual original BlockFence subclass overriding only its render ID; the complete native NTM BlockMetalFence is compiled/exercised. A one-field `ModBlocks.fence_gate` test fixture is explicit and unrelated to path decisions.

Frozen focused result: **887,015 assertions pass**. Assertions are test checks, not independent bugs or a completion percentage.

Coverage includes:

- 10,000 original/native point hash and floating-distance comparisons; 32 heaps with 1,300 points each, resizing, equal priorities, upward/downward distance updates and dequeue ordering.
- All eight collision flag combinations over 1,160 canonical original vanilla states and all 16 search flag combinations across representative material/door/rail/trapdoor/fence/hazard cases, metadata and four entity widths.
- 640 deterministic randomized voxel worlds; costs, heuristics, heap priorities, visited state, every returned path point, native/old entity-offset path vectors, and ordered entity callbacks.
- A 241-point corridor beyond the native 200-iteration limit, closest partial route, strict max-distance threshold, coordinate/float boundaries, fall/Y boundaries, rail-source checks, and reused-finder water-flag restoration.
- 480 wrapper calls with original cache footprints/Y bounds, ground-search choices, source/target rounding and profiler order; nearby-target extension; separate cache/live-world ownership; ordered live-world reads for non-metadata callback cases; propagated failures retaining original profiler order.
- Exact extracted FBI statement compiled/executed through a native navigator and all four exact Glyphid helper expressions compiled/executed on native entities. These tests deliberately do not replace or pretend to compile Glyphid's removed path sink.

## Explicit remaining owner work

FBI and Glyphid whole classes are **not** compiled or certified by this test. FBI still has unrelated old owner APIs around the verified statement.

Glyphid's four helper results still feed removed `EntityCreature.setPathToEntity`, three inside removed `updateEntityActionState` and one in `digToWaypoint`. Its old `hasPath`, `entityToAttack`, `fleeingTick`, `setTarget`, `findPlayerToAttack`, `updateWanderPath`, and the inherited path-follow/turn/jump/movement state machine remain a separate original-semantic migration. Blindly replacing these with native `PathNavigate` would change callback timing and movement. Its blindness/task transitions also clear the old target/path and depend on the old fleeing state. A future bounded slice must trace the original EntityCreature action-state lifecycle and inherited EntityMob/EntityLiving dispatch, preserve original target/attack callbacks, path consumption, RNG, turn/jump/flee decisions and validate actual original/native movement tick traces. This helper migration does not authorize that redesign or claim that it is finished.

No live client/server/world/save, collision physics, mob combat, radiation/energy end-to-end, unknown-mod interoperability or Windows runtime acceptance is claimed. No source exclusions, downloads, installs, full Gradle run or push were used.

## Reproduce

From the module, with Java 8 and the already provisioned native classpath:

```sh
python3 porting/tests/original-pathfinder/run.py \
  --java-home /path/to/java8 \
  --classpath "$NATIVE_CLASSPATH" \
  --legacy-client /path/to/original-1.7.10-client.jar \
  --forge-patches /path/to/forge1614-forgepatches.zip
```

The original source defaults to the sibling `ntm-original-pristine`; `--original-root` accepts another verified copy. The test consumes only existing dependencies. `proof.log` records the frozen run; `source-manifest.json` records every changed production file/hash and exact inverse. Coordinator performs the whole-mod compile and combines this finite patch with other verified work.
