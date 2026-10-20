# Native block-entity rendering boundary

Base: CP49 `caf22cf027f33f9d0d0be366454c2f54fdba7915`. Local-only worktree. No production source exclusions, replacement platform classes, new dependencies, asset changes, global lighting/shader changes, boss changes or pushes.

## Closure

Eight existing production files and two new files:

- `RenderTNTPrimedBase`: native typed Render/RenderManager constructor. Original fuse arithmetic, clamping, fourth-power swelling, yaw/pitch transforms, flash cadence, 1.01 expansion, SRC_ALPHA/DST_ALPHA blend, color alpha and postconditions retained. GL state-changing calls now use native GlStateManager so its cache agrees with the actual color/blend state.
- `RenderRubble`: native typed Render/RenderManager constructor, typed synchronized block/metadata readers, original ten-piece ModelRubble, original 180-degree inversion and tick/partial-tick spin, original local Exception catch. Bottom-material resolution has no particle fallback.
- `ModelBlockSpider` and `RenderBlockSpider`: original alternating odd/even legs, rotations, vertical oscillation and carried-block position; native typed entity/model renderer and block draw.
- `EntityTNTPrimedBase`, `EntityRubble`, `EntityBlockSpider`: only exact typed data parameters, their existing readers/writers and small render-facing getters. Their remaining unported mechanics are intentionally not certified by this batch.
- `ClientProxy`: exactly three native factory registrations, preserving all entity class identities.
- `NativeBlockEntityDraw`: bounded native inventory geometry draw, not a general RenderBlocks replacement. No camera/item-entity/enchantment/TEISR pass. Original brightness and flash alpha remain in GL color, not overwritten by an opaque vertex COLOR attribute. Known ordinary cubes use the existing audited ConveyorCuboidGeometry because native FaceBakery reverses the original bottom V direction and contracts UVs. Native custom inventory models retain their authored transforms and normals. LegacyBaseBakedModel's duplicate side/general references are emitted only once.
- `NativeBlockMaterial`: a pure reusable faceSprite(model,state,side) API plus rubble's separate standalone-texture policy. Face choice follows geometric winding, not a getFace hint that some NTM quads deliberately repurpose for light sampling. BlockBase.getIcon(0,metadata) is authoritative. Other native models require one unique geometric DOWN material. Only the exact vanilla yellow/red-flower identities may use their one unique actual cross-model material: original BlockFlower.getIcon bytecode proves side-independent selection. No generic BlockBush, particle-icon or arbitrary foreign-model fallback was added.

ModelRubble, spider OBJ/texture and representative barrel/charge assets are byte-identical to pristine.

## Writer, reader and persistence audit

TNT old slot 12 is a block ID, not a packed block state. The positional constructor and Tile NBT load write it; getBlock and Tile NBT save read it. Original Fuse remains a signed byte in persistence and an ordinary public int in memory. It was not synchronized originally; this batch does not silently add fuse synchronization. Existing BlockTNTBase, BlockChargeBase, BlockDetonatable and DetMiner producers and their fuse/collision assignments remain unchanged.

Rubble old slots 16/17 are separate block ID and raw metadata integers. entityInit defaults both to zero. setMetaBasedOnBlock writes both; impact ParticleBurstPacket and the renderer read both; NBT keys remain lowercase block/meta integer tags. All internal producer call sites use setMetaBasedOnBlock: EntityBuilding, WeaponSpecial, EntityGlyphidDigger, both EntityBlackHole routes, both ExplosionLarge routes, ExplosionChaos and TileEntityTrappedBrick. These producers are unchanged. Arbitrary world blocks can reach this renderer, so its material universe cannot be claimed closed by testing stone alone.

Spider old slots 12/13 default to block ID 1 and metadata zero. makeBlock is their sole non-initialization writer; ModelBlockSpider is the reader. Repository search found no internal caller of makeBlock. The public method remains, including its original health update. There were no spider-specific NBT overrides in pristine, and none were invented. Native inherited mob data parameters remain registered through super.entityInit. Numeric 1.7 watcher slots intentionally do not become hard-coded native data IDs; native EntityDataManager allocates inheritance-safe typed IDs.

This retains data meaning and NBT key/type identities. It does not certify cross-version registry-ID/save conversion or complete spawn/world/network integration.

## Evidence and limits

Run from the module:

    python3 porting/tests/native-block-entities/run.py --source-root . --java-home /workspace/shared/nagaport-toolchain/java8 --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)" --legacy-client /tmp/nagaport-legacy-client.jar

The portable runner checks exact CP49-relative reversible source mappings and pinned pristine sources/assets. Seven exclusively owned existing files are hash-pinned; shared ClientProxy is guarded only by the exact three factory fragments, each with unique occurrence and reversible replacement, so independent coordinator edits remain allowed. The coordinator aggregate source guard owns the rest of that shared file. The runner compiles complete selected render/model/helper classes against cached native Forge 1.12.2 and compiles the exact three factory fragments. It then executes:

- Complete original and native TNT/rubble/spider-model methods with narrowly recorded GL entry points and engine/mesh-upload boundaries. Fuse/partial-tick/brightness cases, original command order, blend factors/alpha, leg order and ten ModelRubble part calls are compared. This is recorded-transform evidence, not offscreen GL, framebuffer or in-game visual acceptance.
- Actual native BufferBuilder vertices for six cube faces against the exact original RenderBlocks face-method bytecode, preserving UVs, winding and normals, with brightness and fractional flash alpha checked separately.
- A complete native barrel inventory draw using the unchanged original OBJ, actual existing RenderBarrel/AtlasObjGeometry and native baked quads, checking all emitted coordinates/UVs/normal quantization and original whole-OBJ vertex-stream behavior. No second ordinary-cube rotation is applied to this custom model.
- Native geometric material uniqueness, absent/conflicting material rejection, original nested texture paths, original BlockFlower.getIcon bytecode, and actual native cross.json geometry. No particle API may be called by the test models.
- Actual original DataWatcher serialization/decode for the old integer slots; actual native SPacketEntityMetadata encode/decode/application for the extracted production synchronization hooks, including inherited mob fields, raw metadata, defaults and NBT round trips.

Explicit fixtures: three entity synchronization-only shells, projectile-base data shell, ResourceManager mesh holder, unrelated ItemEnumMulti collaborator, engine holder/model selection/texture upload captures. The production rendering classes, BlockBase, native BufferBuilder, model geometry, EntityDataManager and packet encoders are not replaced. The inherited RenderLivingBase static initialization requires a live GL context; RenderBlockSpider and its factory compile, while its own model method is executed independently. No native RenderLivingBase constructor or inherited full mob draw is claimed executed.

## Remaining gates

There is no approved generic material policy for arbitrary native blocks without one geometric bottom material, custom models with multiple such materials, missing models or non-MODEL render types. For example, a reed/crop cross or liquid can reach rubble through the shimmer sledge; only the two specifically audited vanilla flower owners were closed here. The renderer retains its original local catch and emits a once-per-state warning for unresolved material rather than inventing a particle texture. This is an explicit compatibility/visual gap, not approved invisibility or full visual parity. No user policy decision is inferred.

Representative cube/barrel carried geometry is proven; arbitrary public makeBlock inputs and every custom inventory model are not exhaustively certified. Existing model/registration limitations remain visible. Full Gradle, actual client/server launch, GPU visuals, world collision/AI/explosion mechanics and save interoperability are coordinator/later acceptance work. CP37 lighting and other pre-existing gates remain separate and unchanged.
