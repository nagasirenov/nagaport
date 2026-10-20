# Native steel grate root

Base: e03449f (checkpoint 45). Work is isolated in `nagaport-grate-next`; main worktree and shared registration owners were not edited. No commit, push, whole-mod build, new dependency or downloaded JAR is part of this handoff.

## Scope and integration

Seven production paths are frozen in `manifest.sha256`: complete `BlockGrate`, complete replacement `RenderGrate`, three native model/loader classes, and two 16-variant blockstate resources. The unchanged `ConveyorCuboidGeometry` is reused after comparison against actual original 1.7 RenderBlocks face bytecode. Original `grate_top`, `grate_wide_top`, and `grate_side` PNG bytes are checked against pinned SHA256 values.

Apply only the exact shared integration fragments in `registration-fragments.txt`. They replace two block constructors, switch narrow grate registration to the existing deferred helper with plain ItemBlock, keep wide grate's existing ItemBlockBase registration, and remove the obsolete ISBRH registration. The client-only native subscriber handles models. Do not create an ItemBlock during block construction. No other ModBlocks/ClientProxy changes are requested.

## Focused proof

From the module root:

```
python3 porting/tests/grate-native/run.py --module . \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)" \
  --legacy-client /tmp/nagaport-legacy-client.jar
```

The dependency paths are arguments, not imports of old staging output. The runner uses a temporary directory, compiles eleven complete production Java sources against actual native dependencies, and runs separate mechanics and client-model JVMs. It checks complete changed-owner bytecode for removed Minecraft/Forge APIs. It needs the externally supplied original Minecraft client JAR solely as a hash-verified face oracle; that dependency is not redistributed or downloaded. Original NTM owner/render sources are bundled byte-identically in the small hash-checked `pristine-grate-sources.zip` with project licenses. No pristine checkout is required.

`proof.log`: 35,079 mechanics assertions plus 37,514 rendering assertions = 72,593. These are assertion counts, not distinct bug counts or whole-port acceptance.

- Native actual World/Chunk state storage, all 32 owner states, exact float selection/collision bounds, no added tile, drops and side support.
- Verbatim pinned original owner methods execute in an explicitly separate recording oracle. Every side/eighth-height placement boundary, both native hands, sneaking air/null/collision cases and strict .05 thresholds match.
- Below-placement intentionally invokes the below block's `isAir` at the ABOVE position. Tests distinguish both wrong position and wrong block identity. No silent original bug fix.
- Native neighbor callback reads current world state, preserves server-only gate, drops metadata 0/fortune 0 before removing the block, and uses original strict collision thresholds for metadata 8/9 only. Final native drop entrypoint reaches a recording `dropBlockAsItemWithChance` override; this verifies callback/arguments/order rather than claiming actual item spawn physics.
- Actual EntityItem, EntityXPOrb and player instances execute native collision callbacks. Wide grates omit collision boxes for eligible entities regardless of the query; below the strict threshold they set (0,-.25,0) motion and lower position by .125. The threshold remains `meta*.125+.375`, not `getY(meta)`, including metadata 9. Real entity position/AABB updates are tested.
- Tooltip dispatch executes production ITooltipProvider/I18nUtil/ITranslate/I18nServer. Sole controlled NTM collaborator is the MainRegistry translation-service holder. Its replaceable test translator records exact `.desc` key forwarding; full application initialization is outside this scope.
- Pristine RenderGrate/getY callback plus actual 1.7 RenderBlocks face bytecode: all 16 metadata, both original top materials, six faces, exact XYZ/UV/winding up to canonical cyclic rotation, and original flat-item decision.
- All 2,048 owner/metadata/opaque-neighbor masks. Original inherited `Block.shouldSideBeRendered` uses the owner's unnormalized collision bounds, including wide epsilon, while neighbor coordinates are based on shifted renderer origin. Neither detail is silently normalized away. Inherited original bytecode evidence is recorded in `bytecode-evidence.txt`.
- Real ModelRegistryEvent/custom loader, sixteen listed blockstates per owner, exact deferred ItemBlock ownership, actual ItemModelMesher/BlockModelShapes routes, real PNG decoding, directory/JAR resource reload and foreign-owner route rejection. ItemLayerModel uses only the original top icon and remains flat.

## Shifted lighting and explicit open gates

For metadata 9, original RenderGrate moves its render origin down one and uses local Y=.875. For metadata 8 and 10–15, it moves the origin up one and subtracts one from local Y. Merely placing native vertices outside the owner block samples the wrong lighting neighborhood.

`GrateShiftedLightQuad` adapts only these owned shifted faces. It runs the existing native Forge Flat/SmoothAo lighter on normalized coordinates at the shifted origin, adds the origin delta to outgoing positions, then restores the previous native consumer/world/state/position in `finally`. No native class is replaced and no global pipeline is patched. Success and intentional output-sink failures both verify restoration of the exact previous parent/world/state/position for the native Flat and SmoothAo classes. Arbitrary mod-provided VertexLighterFlat subclasses are accepted by the implementation but have NOT been tested or certified. Two stable unobfuscated protected Forge fields, `QuadGatheringTransformer.parent` and `VertexLighterFlat.blockInfo`, are read reflectively because Forge exposes context setters but no current-parent getter. Native light algorithms remain unchanged.

128 actual native renderer scenarios (two owners, sixteen metadata, uniform/asymmetric worlds, Flat/SmoothAo) match a separately positioned normalized native reference byte-for-byte across position, RGB, UV and packed light. This proves shifted sampling and output placement within the native algorithms; it is NOT an assertion of complete numeric equality between 1.7 AO and 1.12 AO or a launched-client visual certification.

Unapproved, reproducible visual gates remain:

1. Optional vanilla lighting fallback (`forgeLightPipelineEnabled=false`) bypasses `BakedQuad.pipe`. Geometry/UV/culling remain original, but shifted metadata lighting differs from the normalized vanilla reference at 540 compared vertices in this probe. No fallback simplification is approved.
2. Native ForgeHooksClient damage model uses BakedQuadRetextured and drops the shifted-light wrapper. Retextured geometry/UV/material are checked, but native model-stage light/color differ at 560 compared vertices. This is the same structural wrapper-loss category as the existing shared damage-light gate, not a fix for it.
3. Full breaking draw and original/native color suppression, GPU state, live client/server, save/reload, external renderer modifications and complete mod initialization remain unverified. Native RenderGlobal invokes `BufferBuilder.noColor`; the focused damage-model comparison alone does not certify that complete draw path.

Do not describe these gaps as approved visual changes or complete grate rendering parity. They do not silently alter grate gameplay, introduce tiles, change textures, widen owner registration, or require a global renderer redesign in this bounded root.
