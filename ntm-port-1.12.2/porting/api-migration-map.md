# Checked API migration map

This is a concise record of verified mappings, not a blanket search/replace recipe. Keep pure formulas, NBT keys, balance, RNG consumption and call order from the supplied pristine tree. Distinguish complete production-class tests from extracted method fixtures and live gameplay.

| Legacy contract | Target contract | Semantics / trap | Evidence |
|---|---|---|---|
| World coordinate block/tile access | BlockPos + IBlockState | Preserve metadata via actual block state mappings and update flags. Other NTM block mappings remain incomplete. | worldgen/component probes |
| StructureComponent integer facing | set/getCoordBaseMode(EnumFacing) | Legacy0/1/2/3 = S/W/N/E; -1=null. NTM X/Z rotation differs from vanilla mirror; do not rotate already-oriented metadata twice. | production LegacyStructureOrientation + native NBT fixture |
| BlockSelector fields and old getters | protected blockstate / getBlockState() | Keep selector branch/RNG order, including accumulating slab metadata. | selector source audit/native roll probes |
| ICrafting / crafters / addCraftingToCrafters / sendProgressBarUpdate | IContainerListener / listeners / addListener / sendWindowProperty | Preserve property IDs, initial/changed sync and removal lifecycle. This does not migrate every container's ByteBuf path or16bit wire limits. | container-listener probe |
| ChatComponentText/Translation, IChatComponent, ChatStyle, EnumChatFormatting | TextComponentString/Translation, ITextComponent, Style, TextFormatting | Keep styles/siblings/translation arguments/click events and strings; player delivery is sendMessage. | full ChatBuilder + extracted packet codec native tests |
| ChunkCoordIntPair | ChunkPos | Fields x/z, Chunk.getPos(), asLong(), getXStart/getZStart(). Legacy and target station hash/packing proven equal against official1.7.10 binary. Never assume hash equality without evidence. | full SubChunkKey/native57308checks |
| IEntitySelector.isEntityApplicable | Guava Predicate<Entity>.apply | World.getEntitiesWithinAABB; old symmetric AABB expand becomes grow, not directional target expand. Original resetTask anomaly preserved. | full production custom AI/nativeWorld+Chunk query |
| TileEntity void writeToNBT(compound) | NBTTagCompound writeToNBT(compound) | Mutate and return exact supplied object on every path. Do not include unrelated same-name serializers; do not add missing super as an unnoticed behavioral fix. |297method parity, native NBT fixtures |
| TileEntity.updateEntity() / canUpdate() | ITickable.update() / absence of interface for static tiles | Only first-declaring tick roots receive interface. Preserve super/early-return order; do not enable ticking on broad shared bases. Native World supplies scheduler guards. |336declaration audit, nativeWorld scheduler fixture |

## Remaining contracts, not completed by this map

- Native inventory ItemStack.EMPTY must be coordinated across actual owners/consumers; a global null substitution is unsafe.
- Native entity/block/tile fields, position calls, registry/resource lifecycle and networking still need substantial work.
- Old achievements, NEI, item/block render interfaces and particles are unresolved. Do not remove behavior or introduce no-op API stand-ins to lower diagnostics.
- OBJ/HMF: raw Forge OBJModel is not a universal replacement. It rejects out-of-range UVs and loads MTL references that legacy HFR ignored. Existing mod-owned parser/native BufferBuilder migration must preserve part order/selection, raw UV wrapping, normals, HMF timing, external textures/tint and GPU reload lifecycle. A first limited consumer is an implementation step, not permission to simplify remaining rendering.

Detailed commands and exact checkpoint chain: continuation-2026-10-19/RESUME.md. Historical whole-file migration audits are snapshot-specific; reusable method/native tests have separately documented scope.

## First native OBJ slice

Plain Forge Wavefront ModelWormHead → mod-owned ObjMeshParser/ObjMeshRenderer with native BufferBuilder. Preserve flat normals and0.0005F inset, not HFR smooth mode. Only single fully attributed part migrated; drawParts is per-part batching, not universal renderAll state equivalence. Six resource/native-byte fixtures PASS; no GL proof. Details: tests/obj-mesh-migration-notes.md.

## Native construction and live HMF boundary

- Render(RenderManager) requires the factory-supplied manager. NTM PreInit runs before the global Minecraft manager exists. 52 side-effect-free constructor roots moved to factories; evaluate original arguments eagerly and keep distinct instances. Resource-loading classes preload at their old registration position rather than silently deferring IO/class initialization.
- EntityEMPBlast DataWatcher slot16 becomes native class-owned VARINT DataParameter. Numeric wire ID is target-allocated, not an old protocol promise. Preserve synchronized default0 vs unused public100, no-super tick, death/scale order, and signed-short NBT without maxAge.
- Live Tom HMF path now uses native mesh rendering. Each flame draw samples cached Clock.get_ms() as float before double modulo/division; do not replace it with fresh wall time or fix precision loss. Separate rotation still uses its original wall-clock long expression. Four obsolete loader classes are retired because the complete live chain moved, not hidden from compilation.
- Real GL/client startup and VBO/resource-pack acceptance remain outstanding. See checkpoint7 notes for exact isolated/fixture boundaries.

## Shared native model root (checkpoint8)

- Legacy IModelCustom/IModelCustomNamed types → actual mod-owned MeshModel with immutable mesh and only consumed draw verbs. Plain eager loader → ObjMeshModel; HFR remains a real lifecycle owner and asVBO retains GL15 streams, not a shim.
- Immediate renderAll is one raw source-corner batch; selected parts have separate batches. Preserve UV/normal carry after initialization, noSmooth capture, final-group primitive mode and selector multiplicity. Undefined prefix attributes now fail before mutation/draw under explicitly approved narrow policy; do not silently zero or drop them.
- VBO position/UVW/normal streams remain float3, not packed native immediate normals. Manual append/latest snapshot and failed upload lifecycle limits are documented.
- Target ResourceLocation lowercases namespace AND path. Eleven referenced asset filenames normalized byte-identically; native fixtures use exact target paths. Other resource names need separate audit.
- ObjUtil/RenderMirror geometry access moved to immutable mesh corners. Their legacy block/render/Tessellator calls remain unresolved, not emulated.
- See tests/hfr-cpu-migration-notes.md and related consumer/VBO/direct-reader/resource-case notes. Clean production compilation still fails1489; no in-game visual/GPU proof.

## Inherited tile World field and canonical resource reads (checkpoint9)

- Only owner-audited `this.worldObj` reads in native TileEntity subclasses become `this.world`. Actual protected field, getWorld/setWorld/hasWorld share identity. Do not rename custom fields, entity receivers, locals, unqualified names or anonymous receivers blindly.
- Source replay and reusable tick-body checks are separate from native controlled World fixture/production-expression checks. Neither compiles the complete production machines.
- ResourceLocation lowercasing also affects texture/shader PNGs and structure classloader paths derived from normalized names.32 additional verified native lookup failures fixed by byte-identical case-only renames; raw-reader audit required before expansion.
- Particle is not Entity. See native-particle-assessment.md before any particle migration; engine queue/capacity/collision/first-frame changes are not silently accepted as parity.

## Coordinate consumers (checkpoint10)

- Mutable spawn triples → per-owner BlockPos.MutableBlockPos, explicit original integer floor/NBT getX/setPos semantics.
- ICommandSender.getPlayerCoordinates → getPosition, preserving player Y+0.5 floor; do not reuse supplied completion targetPos or unrelated truncating casts.
- Solar HashSet positions → immutable domain-specific SolarMirrorPosition using native BlockPos storage plus original shifted hash/equality. Plain BlockPos hash changes beam-limit selection; no Comparable/sorting/packed-long or native-subclass hash override.
- Keep persistent primary/secondary sets and clear/addAll/clear timing; source int distance arithmetic unchanged. Identity-based collision tree order was not universally deterministic originally.
- Full source reversal is not complete consumer class compilation. See tests/native-coordinate-notes.md.

## Maskman approach AI (checkpoint11)

PathEntity→Path; continueExecuting→shouldContinueExecuting; clearPathEntity→clearPath; PathPoint xCoord/yCoord/zCoord→x/y/z; owner.worldObj→owner.getEntityWorld() while preserving custom World field; boundingBox→getEntityBoundingBox; home distance→isWithinHomeDistanceFromPosition with explicit original floored BlockPos. Native getPathToXYZ is final and floors through BlockPos before getPathToPos. Do not change timer/penalty/RNG order while renaming APIs. Full production class native compile verified; controlled fixtures do not validate engine navigation.

## Rocket metadata and biome consumers (checkpoint12)

- EntityDataManager typed keys allocated by declaring entity; RocketMetadata owns only supplied key group, no global numeric-ID shim. EMPTY↔null boundary required before old drive consumers. Preserve signed packing and partial fixed-capacity writes.
- Biome raw temperature→getDefaultTemperature, rainfall→getRainfall, rootHeight→getBaseHeight, heightVariation→getHeightVariation; position-aware old getFloatTemperature→getTemperature(BlockPos). canSpawnLightningBolt→canRain preserves snow/rain exclusion.
- Old provider lookup→getBiomeProvider().getBiome(BlockPos), old World lookup→world.getBiome(BlockPos); never interchange around mutated chunk biomes. Dictionary.isBiomeOfType→hasType; exact vanilla identity lists stay identity lists. No ID/registration policy changes.

## Inventory/energy/cache boundaries (checkpoint13)

- Native IInventory returns EMPTY, but original domain slots remain nullable. Preserve nonempty object aliases and original dirty ownership. Native getName includes custom name; rename subclass translation-key hook to getDefaultName. Retain only wrappers with real old GUI/container callers.
- Battery stackTagCompound → getTagCompound/setTagCompound; missing compound versus missing charge key are different states. Native tooltip World/ITooltipFlag and creative NonNullList callback requires dependent ItemAnchorRemote override too. Library transfer arithmetic/order untouched.
- WorldProvider.dimensionId→getDimension for cache key. Nonloading lookup uses isBlockLoaded(BlockPos,false) before native World.getTileEntity; default true is unsafe on WorldClient, getLoadedChunk changes server unloadQueued. Do not replace world lookup with direct chunk access and lose pending-tile semantics.
