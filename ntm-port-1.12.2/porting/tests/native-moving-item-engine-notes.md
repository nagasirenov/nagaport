# Native moving-item engine acceptance

Status: bounded native proof passes. The complete production `EntityMovingConveyorObject` and `EntityMovingItem` owners compile and execute against the cached Forge 1.12.2 APIs, with the complete production faux `BlockPos` coordinate helper. The test also compiles and exercises all nine frozen native conveyor owners together with the real moving item, not a surrogate moving entity.

## Production change boundary

Only these three files belong to this migration:

- `src/main/java/com/hbm/entity/item/EntityMovingConveyorObject.java`
- `src/main/java/com/hbm/entity/item/EntityMovingItem.java`
- `src/main/java/com/hbm/util/fauxpointtwelve/BlockPos.java`: only the tile-coordinate constructor and double-coordinate floor expression are mapped to native APIs. The helper remains mutable, with its existing equality, clone, add, rotation and offset semantics.

The engine retains the mod-owned `enterBlock(IEnterableBlock, com.hbm.util.fauxpointtwelve.BlockPos, ForgeDirection)` signature. The sole sibling subclass, `EntityMovingPackage`, therefore does not require a signature rewrite. Its separate legacy world, interaction, NBT and item helper issues remain unresolved and explicitly out of this acceptance. `RenderMovingItem`, entity registration, crane callers and shared `Library` remain outside the production closure. Constructors, `getItemStack`, `setItemStack`, and `cacheForRender` remain compatible with their immediate callers. No direct repository callers of the removed legacy interaction or interpolation callback names were found for these owners.

`Library` was used by the base owner only for six aliases. Its original definitions are `POS_X=EAST`, `NEG_X=WEST`, `POS_Y=UP`, `NEG_Y=DOWN`, `POS_Z=SOUTH`, `NEG_Z=NORTH`; substituting those exact `ForgeDirection` values removes that broad dependency without changing receiving-face direction.

## Native mappings and retained control flow

- `world`, native block-state lookup/material lookup, `move(MoverType.SELF, ...)`, and `spawnEntity` replace removed APIs. The move remains no-clip; no vanilla `super.onUpdate()` call or extra dead/server guard was introduced.
- `canBeAttackedWithItem`, `processInitialInteract(EntityPlayer, EnumHand)` and `setPositionAndRotationDirect(..., int, boolean)` are actual native overrides. Interpolation still uses packet step count plus two, ignores incoming rotation/teleport, restores cached velocity, and performs no travel/drop logic on clients.
- A class-allocated native `DataParameter<ItemStack>` using `DataSerializers.ITEM_STACK` replaces fixed watcher slot 10. A second native BOOLEAN parameter tracks missing/null separately from present-empty. Registering `ItemStack.EMPTY` and normalizing null is required because the native serializer dereferences `stack.copy()`. The getter uses presence, never emptiness, to decide the stone fallback. The setter still forcibly marks the item dirty even when the same stack instance is set after mutation.
- The item getter, cache, native inventory insertion, NBT and damage callbacks use genuine 1.12 APIs. Raw synchronized data, not the getter's fallback, decides whether loaded invalid NBT kills the entity. `DamageSource.getTrueSource()` forwards the responsible entity, including indirect attacks.
- Entry is `can` then `on` then death. Hit/leave are death then spawn; rejected or throwing spawn never revives the source. Belt conversion by frozen owners instead attempts spawn before source death, including killing after a false spawn result but not after an exception. Those distinct original orders are covered.
- Pickup still inserts a copy and tests the native inventory method's boolean before detect-and-send/death. A full insertion kills after detection. A partial insertion that returns false leaves the moving entity and original full stack alive even though the inventory received part of the copy. This existing control flow is deliberately not corrected here.

## Null versus consumed-empty accounting

Pristine `getItemStack()` returns fresh stone only when its watched value is null. A non-null live stack can be legitimately consumed down to zero by transfer callers, and must not become stone on a later get, hit, leave or pickup. The final migration stores `HAS_ITEM` separately from `ITEM`: absent/explicit-null is false and retains the fresh-stone fallback; every non-null setter input, including explicit EMPTY, is true and returns the raw synchronized stack. Direct server-side mutations retain the original stack alias. Native metadata collapses zero-count stacks to EMPTY, but the synchronized presence flag retains the accounting distinction on clients. Tests exercise actual nonempty-to-zero mutation, repeated getters, hit/leave/pickup and rejected/throwing spawn, existing-cache refresh, and both full initial and incremental metadata packets.

A preliminary empty-based getter failed this reachable transfer edge and was replaced before acceptance. It is not frozen or accepted. Native `getItem()` reports AIR for empty stacks and serialized empty stacks cannot retain legacy invalid-item identity, but present-empty no longer manufactures a nonempty fallback. Missing/unknown/wrong-type/nonpositive NBT still kills based on the raw tracked value.

The native NBT proof preserves the `Item` compound, valid item identity, count, damage and custom tags through native compressed-NBT save/load. It does not prove arbitrary 1.7 numeric item-ID import: native `ItemStack(NBTTagCompound)` expects the native string `id` representation. Legacy numeric-ID remapping, registry migration and data-fixer registration are outside this slice; no guessed old-ID conversion was added.

## Tick accounting caveat

The original explicit server-side `ticksExisted++` and `<=5` gate are retained. Six direct `onUpdate()` calls reach first travel/leave. Native `World.updateEntityWithOptionalForce` increments the counter before dispatch, so in a native loaded world the counter advances 2, 4, 6 and the gate is crossed on the third world update. A real World-dispatch test covers that distinction. This proof does not mislabel the direct-callback gate as five live-world ticks of grace.

## Reproducible native proof

From the repository root:

    python3 porting/tests/probe-native-moving-item-engine.py \
      --java-home /workspace/shared/nagaport-toolchain/java8 \
      --classpath "$(cat /workspace/shared/nagaport-tick-preparation/classpath)"

The frozen result is in `native-moving-item-engine-proof.log`: 20,859 assertions and 3,854 actual direct engine callbacks, plus six actual World-dispatched updates. The probe compiles 22 complete production classes/interfaces. It never extracts or rewrites their methods.

Coverage includes:

- Complete real faux coordinate helper, native TileEntity coordinates, negative floor, clone, offset, mutation and zero-add identity.
- Real Entity constructor/no-clip/size, native DataManager registration, allocated key, serializer, valid-stack aliasing, same-instance forced dirty, deep dirty snapshots and native PacketBuffer metadata encoding/decoding.
- Initial missing/null fallback, explicit EMPTY and consumed-to-zero preservation, native metadata arrival refreshing an existing client cache, cache identity preservation for equal stacks, no independent cache ticks, server cache absence.
- Tagged item count/damage/compound roundtrip through native compressed NBT streams, unknown/missing/wrong-type Item and nonpositive counts, raw-data validation and fallback save behavior.
- Native interpolation dispatch through Entity, negative/zero/positive delays, both teleport booleans, exact convergence, velocity restoration and unchanged yaw/pitch.
- Server counter gate and native World dispatcher; all six receiving faces, diagonal UNKNOWN, positive/negative block coordinates, direct-entry priority, nonsolid falling to lower UP face, solid/same-cell fall rejection, can-stay rejection, leave-continuation behavior, deliberate original dead-state edge behavior.
- Server/client/alive/dead hit guards, direct/indirect damage, death-before-spawn with success/false/exception, leave projection and velocities, entry accept/deny/callback exception, both interaction hands, empty/full/partial native inventory capacities and copy behavior.
- All nine frozen conveyor owners, all 16 metadata values, positive and negative coordinates: actual loose item conversion into the real EntityMovingItem, actual snapped positions, up to 40 direct engine ticks per case, travel/leave, and conversion failure ordering.

## Fixtures and limits

The only substituted NTM dependency owners are `ModBlocks` (three referenced block registry fields), `ModItems` (the conveyor-wand field), `RecipesCommon.ComparableStack` (tooltip signature only), and `I18nUtil` (tooltip key array). These are printed as BOUNDED_NTM_COLLABORATOR entries in the log. Synthetic Belt and Enterable test blocks provide observable routing choices, alongside a separate integration matrix using every complete frozen production conveyor owner. Native World subclasses supply loaded real Chunks and a minimal IChunkProvider and record outbound spawn attempts/acceptance; they do not replace entity/data-manager/stack/movement code. No `net.minecraft` or `net.minecraftforge` class is stubbed.

Expected callback sequences and thresholds were audited directly against pristine `EntityMovingConveyorObject`/`EntityMovingItem`; this is a native contract test, not execution of a pristine full-game binary. Original source hashes and changed production hashes are recorded in the scope manifest. Frozen conveyor artifacts are not edited. No whole-mod compilation, renderer/GL visuals, EntityMovingPackage closure, all-crane gameplay, live client/server tracking or full save/server acceptance is claimed.
