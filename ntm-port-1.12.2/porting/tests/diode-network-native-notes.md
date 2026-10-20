# Native diode and partitioner owner/model checkpoint

## Frozen production result

Seven complete production sources in this module replace the diode and partitioner legacy renderer declarations and native owner boundaries. No old-Forge package shim, renderer facade, production stub, source exclusion, CE code or changed asset is used. The native owners retain their embedded tile classes, interfaces and mechanical formulas. The finite Java 8 compile includes both complete owner files and embedded tiles against real Minecraft/Forge 1.12.2 APIs; explicit NTM-domain collaborators are test doubles, listed in the manifest. This is not a full transport or whole-mod acceptance.

Production paths:

- src/main/java/com/hbm/blocks/network/CableDiode.java
- src/main/java/com/hbm/blocks/network/CranePartitioner.java
- src/main/java/com/hbm/render/block/RenderDiode.java
- src/main/java/com/hbm/render/block/RenderPartitioner.java
- src/main/java/com/hbm/render/block/nativeblock/DiodeBakedModel.java
- src/main/java/com/hbm/main/DiodeNetworkModelHandler.java
- src/main/java/com/hbm/render/tileentity/NetworkBreakingRenderer.java

No shared main-tree file was written by this worker. Existing ConnectionState, AtlasObjGeometry, AtlasObjBakedModel, ConveyorCuboidGeometry, BlockPositionLightQuad, ThermalModelTransforms and OBJ parser classes are reused unchanged.

## Exact coordinator integration

Copy the seven production paths and this module's test/evidence files into the full module. In ClientProxy remove only:

    RenderingRegistry.registerBlockHandler(new RenderDiode());
    RenderingRegistry.registerBlockHandler(new RenderPartitioner());

Keep RenderSplitter registration and its complete original owner unchanged.

In ModBlocks replace only the two owner declarations:

    cable_diode = new CableDiode(Material.IRON).setTranslationKey("cable_diode").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
    crane_partitioner = new CranePartitioner().setTranslationKey("crane_partitioner").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);

Replace the direct cable-diode legacy registration with the existing register helper:

    register(cable_diode);

The helper selects ItemBlockBase and the translation-key-based identity; retain register(crane_partitioner). This is consistent with the present shared registration migration, not a claim the remaining global legacy ModBlocks registration body is already native. Preserve existing TileMappings entries `tileentity_cable_diode` and `tileentity_partitioner` unchanged. The model subscriber selects only exact CableDiode.class + hbm:tile.cable_diode, and exact CranePartitioner.class + hbm:tile.crane_partitioner. Foreign namespace, subclasses, and unlisted owners are left untouched, even without ItemBlocks. It installs all sixteen item metadata routes and sixteen listed world metadata routes. No blockstate JSON is required.

## Preserved render and block behavior

Diode retains its metadata-selected 1/8 plate, 3/4 coil cube, all independent cable arms, original world Z-side group reversal, five-arm inventory, 90-degree cube/item transform, 270-degree arm item transform, and -1/8 inventory vertical offset. Its full-block collision is intentionally not reduced to the visible shapes. Native piston-style six-direction placement retains original metadata. Per-instance mutable icon/bounds state is gone; neighbor connectivity uses the existing immutable ConnectionState value and original Library predicate.

A single native AO flag cannot reproduce the original mix of standard cubes and pre-shaded OBJ arms. The owner therefore exposes exactly two native chunk passes: SOLID contains the original twelve cuboid faces with AO, CUTOUT_MIPPED contains only the original OBJ arms with no AO. The native model uses the current thread-local render layer, never shared active state. Headless real Forge SmoothAo emission and both vanilla/Forge flat emission are invoked; arms retain exact original vertex colors and own-block packed light instead of getting AO or diffuse multiplied a second time. No geometry is duplicated across passes. Inventory remains one complete model. This is a native lighting separation, not simplified geometry. Live GL/atlas upload and rendered screenshots are still unverified. Original diode plate/coil PNGs are fully opaque; cable and partitioner side/back PNGs have binary alpha. The partitioner also uses CUTOUT_MIPPED to retain those holes; its animated-belt metadata remains unchanged.

Partitioner retains all six original OBJ groups/materials, four direction rotations plus all other metadata defaults, original inventory transform, 3/4-height collision, quarter-height item snapping, original entry/travel formulas, package rejection and inherited false activation. Metadata 0..15 round-trips without coercion.

## Exact-owner native breaking route

The original damage behavior is now preserved through the standard native breaking-only TESR extension on both existing tile classes. ModelRegistryEvent binds NetworkBreakingRenderer to the existing CableDiode.TileEntityDiode and CranePartitioner.TileEntityCranePartitioner classes. No tile ID, transport behavior, global renderer, ordinary chunk/item mesh or other owner is changed. No ClientProxy TESR line is needed in addition to the subscriber. The exact block class plus registry identity is checked at each entry; tile subclasses, foreign namespaces and unlisted owner instances are not claimed. Tile checks read the current world block state rather than TileEntity's potentially stale cached block type.

Two actual native RenderGlobal hooks are required together. TileEntity.canRenderBreaking returns true for these exact current owners so drawBlockDamageTexture skips default whole-model cracks. IBlockState.hasCustomBreakingProgress returns true so renderEntities dispatches the native tile renderer with the damage stage. The ordinary stage -1 returns before querying models, world state or OpenGL. Stages outside 0..9 and mismatched owners also do not draw. The native dispatcher overload is actually invoked by the fixture, with only its final protected GPU draw boundary replaced by a buffer capture. The higher camera-relative dispatch wrapper and RenderGlobal branching are audited from actual native bytecode. This is not an execution of the full RenderGlobal frame or a live game.

Pinned original Minecraft 1.7.10 client bytecode proves RenderGlobal disables Tessellator vertex colors before dispatching RenderBlocks.renderBlockUsingTexture. The pinned Forge 1614 RenderBlocks patch proves custom render types reach the original ISBRH. RenderDiode accepts the incoming crack texture for the first plate, explicitly overrides the coil back to its own texture, then clears override before OBJ arms; RenderPartitioner ignores the RenderBlocks override entirely. The fixture executes pristine renderer method bodies with a supplied incoming damage override and compares those materials. The new renderer therefore replaces only the diode's first six faces and keeps all coil, arm and partitioner materials. It uses the original damage blend, alpha threshold, color and polygon offset.

Native cube smooth/flat lightmap calculation is retained according to the native ambient-occlusion setting; OBJ groups retain original own-block lightmap. The final native upload buffer has POSITION, UV0 and UV1 only: no COLOR element, matching the original damage dispatcher's suppressed vertex colors. Representative native dispatcher tests cover both owners, connected topology, AO on/off, stages 0/5/9, invalid stage 10, ordinary stage -1, exact guards, camera-relative geometry, atlas UVs and byte-exact retained lightmap. The default ForgeHooksClient.getDamageModel regression still demonstrates why the custom route is necessary: it would retexture every quad and collapse the mixed lighting into one AO flag. That unsuitable default is bypassed for the two exact owners, not globally changed.

The GPU boundary uses raw GL_ALL_ATTRIB_BITS and GL_CLIENT_VERTEX_ARRAY_BIT push/pop in finally, explicitly selects the default texture unit and disables the color array, then uploads through the actual native WorldVertexBufferUploader. Native uploader/VertexFormat bytecode shows this colorless format does not enter the GlStateManager.resetColor branch; all texture/client-array mutations used are uncached passthroughs. The client attribute mask covers lightmap UV arrays and client-active texture; LWJGL saves/restores its corresponding StateTracker too. This is a bounded source/bytecode state-restoration audit, not a live GL-context test. A live render, texture upload and screenshot remain unverified and are not claimed as passed.

## Mechanical audit and deliberately retained behavior

CableDiode's embedded tile still uses level 1..11 and (long)Math.pow(10, level), five priority values, subscription on every non-output side, per-tick power/pulse reset, a ten-pulse brake and recursion brake. The network branch caps by remaining diode speed and increments the intra-tick counter. The original direct-receiver branch instead caps by the destination's speed and does not increment the diode power counter; this apparent quirk is deliberately preserved and tested, not silently fixed. Original NBT keys level and p and incremental packet payload remain, with native getUpdateTag using that same payload for initial chunk synchronization. Invalid persisted enum ordinals retain the original behavior rather than inventing a repair policy.

Actual closure is Nodespace -> UniNodespace/GenNode/PowerNetProvider/PowerNetMK2/NodeNet; PowerNetMK2 distributes by priority and weighted demand, expiry timestamps and per-network accounting. Real IEnergyReceiverMK2, IEnergyHandlerMK2, ILoadedTile (including its original cache), TileEntityLoadedBase, ForgeDirection and tuple/coordinate domain helpers compile in the probe. Nodespace/network, conductor, audio/packet plumbing, overlay/localization and Compat are explicit collaborators. Actual world network merging, expiry, receiver competition, threading and integration with unrelated energy nodes remain unverified.

Partitioner retains its complete embedded 96-input + 8-output tile, ascending stack-count processing, recipe-derived chunk sizes, invalid-type whole-stack ejection, leftovers, all-side slot exposure, output-only extraction, input validity rules, inventory routing, random inventory breakup and comparator notification. Native ItemStack count/empty conversion preserves the existing nullable internal slot convention, skips native EMPTY inputs, and captures drop identity before depletion. The original markDirty local remains false, and the original normalized travel expression is unchanged, including its zero-distance edge case. TileEntityMachineBase, CrystallizerRecipes, InventoryUtil and EntityMovingItem are explicit test collaborators, not replacement production implementations or proof of the complete conveyor/recipe/persistence closure.

## Splitter boundary: deliberately unchanged, not excluded

CraneSplitter inherits the still-legacy 663-line BlockDummyable machinery for side-sensitive multiblock placement, obstruction/fill logic, core/dummy metadata, orphan removal, core traversal, inventory drops, redirection, persistence/copying and detailed hitboxes. Native listed metadata/model routing cannot be integrated coherently while pretending that inherited owner has compiled. This checkpoint therefore does not modify RenderSplitter, CraneSplitter, BlockDummyable or TileEntityCraneSplitter, and does not install a splitter model loader. Their sources are pinned in the audit manifest. This leaves real future work in the ordinary source tree; it does not exclude them from the build or substitute an approximate owner.

## Portable finite verification

Run from the isolated module or after normal integration:

    python3 porting/tests/probe-native-diode-network.py --module /path/to/production/module --base /path/to/full/ntm-port-1.12.2 --pristine /path/to/ntm-original-pristine --java-home /path/to/jdk8 --classpath "<cached native 1.12.2 dependencies>" --legacy-client /path/to/minecraft-1.7.10-client.jar --legacy-forge-userdev /path/to/forge-1.7.10-10.13.4.1614-userdev.jar

After integration, --module and --base may be the same. The command does not download anything, use precompiled NTM main classes or hidden sourcepaths, invoke Gradle mutation, or read historical staging trees. It explicitly compiles selected complete production sources with an isolated temporary sourcepath and real engine libraries. The bytecode audit additionally pins the original 1.7.10 client and Forge 1614 userdev jars and records bounded original/native dispatch excerpts in diode-damage-bytecode-evidence.txt. The production-only source freeze is diode-network-production.sha256. The manifest pins pristine sources and nine PNG/two OBJ assets plus the original animated-belt .mcmeta and records production hashes plus the explicit collaborator inventory.

The original diode/partitioner world and inventory method bodies are read verbatim into a descriptor recorder at test time; original partitioner material-selection method is included. A separate small OBJ parser and GL-matrix recorder produce the expected vertex/material/UV stream. Geometry checks cover all 16 x 64 diode metadata/connectivity cases, both original inventories and all sixteen partitioner metadata variants. Native tests also invoke owner placement, collision, tools, snapshot/model entry, embedded tile branches, packets, priority/cap bounds, real model registry events, item metadata mappings, state mapper dispatch, resource loading/reloading and native flat/SmoothAo buffer emission.

Final passing result: 2,069,082 assertions; 1,042 pristine renderer cases. Expected headless Unsafe/deprecation/unchecked warnings and missing-active-ModContainer namespace warnings are visible in the log, not suppressed failures. Unsafe allocates real Minecraft/GameSettings/BlockRendererDispatcher holders to avoid GL-dependent constructors; real model shapes, BlockModelRenderer, resources, TileEntityRendererDispatcher and BufferBuilder are used. The final GPU draw is explicitly intercepted for capture. No engine class or production source is replaced.

No live game, integrated server save, full energy network, complete conveyor/recipe behavior, shared multiblock base, full-mod compilation or multiplayer acceptance is claimed.
