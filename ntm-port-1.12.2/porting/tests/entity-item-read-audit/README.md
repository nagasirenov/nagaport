# EntityItem reads: CP73 audit, no production change

Exact base: a4739afa41966b75e2012bccd7df919e51d5c3a0. All 35 remaining getEntityItem() expressions are inventoried in receiver-inventory.txt, alongside the two mod subclasses. No getter-only subset is recommended for merge. This is an audit outcome, not compilation closure.

## Genuine engine findings

The pinned original client /tmp/nagaport-legacy-client.jar has SHA256 a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce. Original xk.f() (EntityItem getter) reads DataWatcher slot10 and returns its existing ItemStack alias. If that value is null, it constructs a fresh ItemStack from ajn.b. The separately captured original ajn static initializer resolves b from registry key "stone". It does not return null and does not write the fallback back to the watcher. Therefore an EMPTY-to-null compatibility shim would not preserve the old getter contract. This is original bytecode evidence, not a claim of executing the old entity in a world.

Native Forge2768/MCP39 EntityItem.getItem() directly returns EntityDataManager ITEM. Ten executable checks construct the genuine EntityItem(null) with its genuine registered data manager, then invoke the actual setter/getter. No fake entity, fake data manager, Unsafe allocation, or substitute platform class is used. Default value is singleton EMPTY; an explicit null setter remains null. Real stacks retain live object and NBT aliases. A count-zero real stack remains the same stored object but exposes AIR through getItem(); restoring positive count restores the item identity. Explicit EMPTY remains EMPTY. Null world is only a constructor fixture; no tick, pickup, save, chunk or gameplay claim.

## Why the visible read families remain together with their callers

- Fourteen bomb/block/item spill reads mutate the newly constructed entity stack's NBT. Their source slot is decremented before getItem()/metadata are used to construct the drop. Native depletion hides original identity. Renaming only the getter leaves that root broken; converting raw counts without pre-depletion identity capture can destroy the last chunk. Existing ItemStackUtil spill work establishes the right pattern, but it must be applied and tested per full emitter, preserving offsets/RNG/spawn order.
- Two contamination reads (EntityEffectHandler and death drops) attach NBT. Default EMPTY must not become a shared contamination carrier. Original missing watcher state produced an ephemeral fresh stone stack, not shared EMPTY. Preserve exact valid-stack aliasing and decide the exceptional boundary explicitly.
- Pickup advancement reads observe potentially depleted stacks. Original item identity survived zero count; native item identity may be AIR. This belongs with the actual Forge event payload/timing and existing achievement work, not a spelling change.
- Excavator, mining laser, crucible and destroyer reads consume, copy, compare or transfer aliases. Their null-slot/count/identity continuations remain unported. Bread AI similarly retains a live alias while consuming and respawning it.
- HazardSystem and ItemDrop/ItemRag are radiation/drop callbacks; changing fallback or zero-count dispatch in isolation would alter gameplay boundaries. Black-hole and acid checks also rely on item identity, including dead entities. No formula or behavior was changed.

Both current NTM EntityItem subclasses inherit the getter; neither defines getEntityItem/getItem. This source observation is not a guarantee about third-party subclasses. Any future helper must retain virtual dispatch, rather than reading the data manager behind an override.

## Recommended next coherent root

Prefer the remaining full inventory spill-emitter family using the already tested ItemStackUtil boundary, with explicit pre-depletion item/metadata/NBT capture, native spawn/comparator calls, exact RNG/tail and alias tests. Alternatively close the entire pickup event payload/identity boundary with native callback execution. Do not claim that native getter defaults and legacy getter defaults are the same, or normalize all empty stacks to null/stone: count-zero stacks, absent watcher data and explicit null are distinct states.

Reproduce (requires already resolved classpath and JDK8; downloads nothing):

    python porting/tests/entity-item-read-audit/run.py --java-home <jdk8> --classpath-file <native-classpath-file>

Recorded result: PASS, 10 genuine native checks. Earlier javac invocation lacked its output directory and was corrected before the successful recorded run. No whole production class compile, full build, game runtime, dependency change, remote push, or production edit occurred.
