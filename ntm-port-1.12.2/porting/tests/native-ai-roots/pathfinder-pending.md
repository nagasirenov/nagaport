# Historical CP48 PathFinder audit

This is the original CP48 audit, retained as evidence. The later bounded helper migration and still-pending Glyphid movement lifecycle are documented in [original-pathfinder/README.md](../original-pathfinder/README.md).

At CP48, no production edit is made to `com/hbm/entity/pathfinder/PathFinderUtils.java`. Its `PathEntity` import and two return declarations remain real blockers; replacing only their type would conceal a non-equivalent engine migration.

## Actual callers and flag scope

Repository search at CP47 finds exactly five active calls:

- `EntityFBI.updateAITasks`: entity-target helper, `16F, true, false, false, true`.
- `EntityGlyphid`: one entity-target helper call with `16F, true, false, true, true`; three coordinate helper calls with `maxDist, true, false, true, true`.

No other active helper callers were found. Commented duplicate engine calls within the helper are not additional consumers. The wrapper's parameter names are misleading and must not be mapped by English name alone:

- First flag (`allowDoors`, legacy engine `e`) allows traversal consideration of wooden doors. False rejects the wooden-door block before the later passability test.
- Second flag (`allowBlocked`, engine `f`) permits the wooden-door collision bypass. It is false for all current callers.
- Third flag (`allowWater`, engine `g`) is actually the water-avoidance flag: true returns `-1` for water cells. Thus the Glyphid and FBI deliberately pass different water behavior.
- Fourth flag (`canDrown`, engine `h`) enables the start-in-water surface search. During that search the engine temporarily disables water avoidance and restores it afterward. It is not a permission to drown.

These statements come from original constructor stores and `ayg.a(Entity, x, y, z, size, boolean, boolean, boolean)` instructions; `ayg.a(Entity,...)` passes flags to the static collision routine as `g,f,e`.

## Material algorithm differences

Original `ayg` owns its block access and four flags. Native PathFinder instead receives a NodeProcessor and takes IBlockAccess plus EntityLiving at search time; the wrapper currently accepts generic Entity.

Original search heuristic and accumulated step cost use `aye.b`, the **squared Euclidean** float distance. Native PathFinder uses Manhattan distances plus per-node cost malus, and imposes a loop limit at `++iterations >= 200`. The original loop runs until its heap is empty or the exact target is reached, without that cap.

Original neighbor consideration order is +Z, -X, +X, -Z, with step/fall handling in its safe-point routine. Native WalkNodeProcessor has additional diagonal candidates, node categories and entity-specific path priorities. Original max-distance admission compares each candidate's Euclidean distance to the target; native additionally tracks distance from origin and rejects candidates at the search-length bound. These are not fixed by `setCanEnterDoors`, `setCanOpenDoors` or `setCanSwim`.

The wrapper also has behavior that a future original-algorithm port must retain:

- Normalize the source-to-target vector then scale to maxDist even when the requested target is nearer.
- Use start floor(posY + 1) and cache bounds maxDist + 16.
- Search descending from candidate Y through Y-9, then from Y+10 through Y+1 for air/nonblocking above a normal cube; preserve order and unchanged Y on failure.
- Pass integer target coordinates through the original half-block target conversion, and preserve entity-width/start bounding-box rounding, water surface handling and partial closest-path result.
- Preserve current profiler ordering and original collision/block-state semantics; do not silently adopt a different hazard/door/water policy.

A separate coherent original-algorithm implementation and native controlled-world path oracle are needed before claiming this root migrated. The current AI patch does not make FBI or Glyphid whole entities runnable and does not authorize a pathfinding or combat redesign.
