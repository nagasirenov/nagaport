# Native foundry block/model boundary

Baseline: checkpoint 32, `e8970b85e0c8fe46f17348330b3853bb32ffaa0b`.

## Scope and route

Seven complete pre-existing block classes (casting base, basin, mold, channel, tank, outlet, slagtap) now extend native BlockContainer through FoundryBlock. Native metadata is a lossless 0..15 property. Native callbacks preserve the original hand action ordering, mold copies/counts/NBT, scrap amounts, shovel behavior, tool return values, placement quadrants, collision components, drop ordering, and per-face solidity. Material transfer still delegates to the original tile methods. There is no RenderBlocks or IIcon platform substitute.

Five old ISBRH classes are retired. FoundryGeometry preserves their explicit face order, bounds, offsets, texture assignments, neighbor conditions, fill formulas, Color.brighter behavior and byte-truncated shade. The independent test oracle extracts the immutable original five renderer bodies, runs them against a pure face recorder, and compares actual native packed quads. Its recorder is not used by production and does not replace Minecraft/Forge.

Housing is a native unculled IBakedModel with CUTOUT_MIPPED sampling, original shade and block-position light. The shared existing BlockPositionLightQuad and ConveyorCuboidGeometry are unchanged. A local foundry UV rule retains old RenderBlocks' whole-side V reset when an overfilled tank's bounds extend above the unit block; the amount and surface height are not clamped.

An immutable unlisted snapshot captures current tile primitives and neighbors each extended-state request. No tile/world/ItemStack is retained by a baked model. The original tank wall openings distinguish outlets from slagtaps. Amount changes do not cache molten geometry in a chunk. RenderFoundryFluid obtains a fresh snapshot each TESR invocation and emits the original channel/tank molten faces using packed light 240 (block 15, sky 0), not the differently lit housing. Basin/mold RenderFoundry retains separate inventory/output and original two molten passes, including packed full sky/block light, original color, alpha, UV rotation and depth behavior.

Native model registration is a client ModelRegistryEvent subscriber after block/item creation. Six exact owner identities are checked, 96 native item metadata routes are installed, and six blockstate JSON files route every metadata state. Loader accepts actual native block/ and models/block/ prefixes. Atlas sprites are recreated on model rebake; dynamic fluid resolves the current native atlas.

## Native evidence

Run the bounded probe using the already cached classpath and Java 8:

    python porting/tests/probe-native-foundry-blocks.py --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)" --java-home /workspace/shared/nagaport-toolchain/java8

The selected compile includes all seven complete block owners, both complete TESRs, native state/model/loader helpers, all eight actual foundry tiles, real LoadedBase/material/CrucibleUtil and full node dispatcher/network foundations, plus existing native preview/projection utilities. No transformed production method body is used. Platform types are genuine cached Minecraft/Forge/LWJGL classes.

Final terminal results:
- 1,956,401 owner/model/state/light/asset assertions, including 53,176 complete native faces matched to immutable original callbacks.
- 4,841 real surfaceless EGL/OpenGL native TESR/custom GUI assertions.
- Actual native World/Chunk replacement and retention, both-hand activation, original sound event/category, copied mold stack/NBT, tool and break dispatch, collision and support, initial/incremental tile state consumption.
- Actual Forge VertexLighterFlat and vanilla flat fallback both preserve block-position light and exact original shade with deliberately different neighbor brightness.
- Actual ModelRegistryEvent registration, real native loader/bake, all JSON metadata routes and original PNG assets.
- Actual native RenderItem.renderItemIntoGUI -> item TEISR invocation. Custom x=1/z=-3 matrix matches the original Forge inventory caller, without the effect entry's extra +50 z. The exact slot stack reaches the renderer unchanged. Normal and intentional exception paths restore owned matrix depth, zLevel, GL enables, lightmap and native atlas filtering caches. A local four-field atlas filter snapshot is necessary because vanilla GUI code does not restore these on renderer exceptions.
- Real TextureMap PNG load/stitch/upload and actual channel/tank/basin molten draw calls under cached Mesa EGL, with current amount changes and no GL errors.

The native client fixture uses the genuine Minecraft constructor and explicitly initializes controlled native settings/resource manager/texture manager/model manager services. Only a RenderGlobal holder is Unsafe-allocated for its actual damage pre/post methods, rather than creating an entire world renderer; native TileEntityRendererDispatcher and real tile/world constructors execute normally. It is not a launched client or game loop. The GL test's item model/renderer subclass selects a controlled native built-in model; actual native GUI matrices and TEISR dispatch execute. Recipe registries, ItemMold/ItemScraps outer implementations, localization/overlay destinations, particle transport and unrelated slag owner remain explicit NTM collaborators in the runner. The tile worker's separate native proof covers transfer/network/update formulas; this combined probe uses its actual eight tiles and replaces its FoundryChannel shell with the actual production owner.

## Limits requiring honesty

- No full-game screenshot, multiplayer play, external-mod compatibility, shader compatibility or whole-project compilation claim.
- Actor preview WorldInAJar remains a compiled explicit collaborator, not an executed full tutorial engine.
- Old invalid outlet metadata could inherit mutable global block bounds from a prior block. Native selection uses deterministic full-block fallback for invalid metadata; its collision component list remains empty, as before. Normal placement only creates 2..5.
- Channel/Tank alone implement both native breaking gates: block hasCustomBreakingProgress and tile canRenderBreaking. RenderGlobal's native chunk path skips these owners; the native damage TESR emits housing plus molten geometry exactly once, including empty containers. Normal TESR emits only fluid. Native BakedQuadRetextured maps the original partial UV interval onto the crack sprite. The original RenderGlobal/Tessellator bytecode disables vertex color during breaking, so a dedicated native no-color vertex format retains neutral half-alpha instead of applying material/shade colors to cracks. Packed housing and molten light remain distinct. Tests invoke actual RenderGlobal pre/post and TileEntityRendererDispatcher damage methods and assert both native routing gates. They do not launch the entire RenderGlobal.renderEntities scene.
- User explicitly approved an actual-output-item fallback for native models with no upward surface. Normal cubes and unculled upward surfaces retain the original flat top rectangle and exact sprite. No-top output uses the existing actual item draw at the same output height, with a warning once per block, no particle-icon substitution, and no changes to recipes/counts/NBT/cross-mod support. The regression executes the actual native custom item draw with the original output stack, verifies its matrix/height, repeated warning suppression, and both supported top-surface paths. This is an approved compatibility fallback, not an assertion that an arbitrary external model matches the original flat icon.

## Artifacts and integration

`native-foundry-blocks-scope.json` records all 15 Java and six JSON owned production hashes, five retired source tombstones, original CP32 input hashes, and the two coordinated tile-only canRenderBreaking additions. The original renderer bodies at CP31 and CP32 are byte-identical. Final native log: `porting/continuation-2026-10-19/native-foundry-blocks-FINAL.log`. `check-native-foundry-blocks.py` checks source freeze, original inputs, deletion boundaries and the exact four ClientProxy bindings. Parent owns those proxy/registry/audio edits and whole-project compilation. Tile transfer/network proof is separately frozen at 260 native assertions in its own manifest and final log.
