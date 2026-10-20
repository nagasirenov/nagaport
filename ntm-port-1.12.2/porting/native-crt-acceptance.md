# Native CRT decorative block and model root

Baseline: `641ba083c276c6ac75afa6a8f996cdd86f8b16d4`. Work was isolated in `nagaport-crt-next`; no main-worktree edits, full Gradle build, downloads, installs, commits or pushes were performed.

## Frozen integration scope

`tests/crt-native/production-scope.json` identifies the six whole production files and their exact hashes. It separately records the only two shared-file byte edits:

1. `ModBlocks.java`: change only the CRT constructor expression to native `Material.IRON`, remove obsolete `setBlockTextureName`, retain its original name/tab/hardness/resistance. Existing `register(deco_crt)` is unchanged.
2. `ClientProxy.java`: remove exactly `RenderingRegistry.registerBlockHandler(new RenderCRT());`. Native `CRTModelHandler` is automatically registered by its client-only `@Mod.EventBusSubscriber`; no second registration is added.

The shared file's existing CRLF or LF bytes are retained. Apply its exact replacements rather than overwriting either shared file with this worktree copy. A Git whitespace check can flag the preserved CR at the changed ModBlocks line; no unrelated line ending was normalized.

The six whole production files are:

- `src/main/java/com/hbm/blocks/generic/BlockDecoCRT.java`
- `src/main/java/com/hbm/render/block/RenderCRT.java`
- `src/main/java/com/hbm/render/block/nativeblock/CRTBakedModel.java`
- `src/main/java/com/hbm/render/block/nativeblock/CRTPackedLightQuad.java`
- `src/main/java/com/hbm/main/CRTModelHandler.java`
- `src/main/resources/assets/hbm/blockstates/tile.deco_crt.json`

No existing shared geometry/frame helper, native superclass, original OBJ, PNG or animation file changes. The complete owner still extends native BlockMulti/BlockBase. The root directly reuses AtlasObjGeometry, BlockPositionLightQuad, ObjMesh/ObjMeshParser, ThermalModelTransforms, ItemTransformMatrix and AngelCoreFrameCorrection. It neither substitutes a Minecraft/Forge class nor reintroduces an ISBRH/RenderBlocks facade.

## Preserved bounded behavior

- Four variants and all sixteen persisted metadata values; metadata is exactly `4 * variant + orientation`. Creative variants, drop/pick item damage, placement yaw/flag 2, structure rotation and native noncube behavior are preserved. The collision/selection extent remains the original full block, independently of the smaller visible mesh.
- Strict `hbm:native_crt/tile.deco_crt/{meta}/{world|item}` routes: sixteen world routes, four item routes, only exact original owner identity and class. The native JSON stores the original `meta` property with no extra rotation.
- Original world Monitor then Screen groups, exact geometry and uncontracted atlas UVs, original part shading, and all four original materials. Inventory retains the original whole-object continuous GL_QUADS stream, original yaw `-pi/2`, and native centered coordinates that become the original `(0,-.5,0)` inventory translation.
- In the normal Forge flat world pipeline Monitor and inactive Screen retain the owner block's packed environmental light. Only active Screen at metadata 8–15 replaces final lightmap bytes with decimal **240**. It is explicitly not `0xF000F0`: sky remains zero. No global lighting setting, color/shader simplification or surrounding-quad state change is used.
- The CRT-only quad adapter temporarily wraps the actual native Forge light consumer's output, restores its exact parent in finally, and does not change source vertices. Forge's unobfuscated protected `parent` field is read because no getter exists; the native setter performs the adaptation. This narrow reflection seam is exercised against the real cached target runtime.
- Ordinary vanilla item frames retain the original four-angle pivot/orientation and quarter-scale ItemBlock helper via a narrow FIXED perspective plus existing frame-translation helper. The handler selects only the registered CRT ItemBlock. Machine/decorative frames use the existing ordinary native block branch, unchanged, preserving original bob/count/spread and decoration suppression. Other GUI/held/ground transforms use the existing native block defaults, not a claim of exact legacy caller parity.

## Evidence

`tests/crt-native/run.py` compiles all 19 complete production classes without any Minecraft/Forge or NTM owner/collaborator substitutes. Its independent oracle is generated from pinned pristine CRT methods and ObjUtil shaders, not migrated code. It runs with either the pristine checkout or its byte-identical bundled source/archive assets.

- 70,472 assertions and 6,852 complete exact native vertices.
- 17,284 actual native World/Chunk placement operations.
- 7,776 exact double comparisons with the hash-pinned official original client's Vec3 rotation bytecode.
- Folder and JAR resources; genuine ModelLoaderRegistry, BlockModelShapes, ItemModelMesher, sixteen world/four item routes and all original textures/animation metadata.
- Actual native Forge flat buffer output, forced smooth adapter isolation, direct consumer output, subsequent Monitor isolation, source immutability and downstream-error parent restoration.
- Original/modified/restored/missing OBJ lifecycle; immutable already-baked lists; exact-case/duplicate/missing group and controlled quad/tail behaviors; invalid routes and wrong-owner/replacement/subclass negatives.

`tests/crt-native/frame-run.py` additionally compiles the exact shared CRT registration expression and executes actual native EGL/OpenGL, Forge event transformation/dispatch, RenderItemFrame, RenderItem, mesher, textures, framebuffer, and unchanged NativeMachineItemRenderer.

- 2,706 checks: all four original CRT texture first frames emit visible pixels; all four original item-frame rotations; ordinary machine copy thresholds at 1/2/5/6/20/21/40/41 in both display modes; source stack/NBT unchanged; matrix restoration; no machine generic glint.
- An unrelated item using identical geometry has identical frame matrices and pixels with the CRT listener present or absent.
- The unchanged machine harness's existing NTM gameplay/TESR collaborators are explicitly declared fixtures. CRT production is never substituted. The controlled atlas/framebuffer proof does not certify every complete-game render state.

Logs: `tests/crt-native/proof.log`, `portable-proof.log`, and `frame-proof.log`. The source archive includes original project licenses. The complete copied path list and hashes are in `scope.json`.

## Explicit open visual gates

1. Vanilla's optional non-Forge block-rendering fallback does not call quad `pipe`; it replaces lit Screen light with environmental light. The test confirms all eight affected Screen vertices per lit model.
2. Native Forge damage retexturing recreates baked quads and discards the custom light wrapper. Geometry/native crack UV substitution is verified, but the eight lit Screen vertices lose original brightness 240. This is an observed breaking-overlay lighting gap, not a passing crack-parity claim.
3. Full game/client acceptance, complete live animation timing, GUI/held/dropped legacy caller parity and arbitrary third-party model/render replacements are not established here. The separate GL proof covers ordinary vanilla and machine/decorative frames only. Forced SmoothAo preserves the native helper baseline; CRT itself is deliberately non-AO as the original shaded renderer was.

No global light override is proposed to conceal these gates. This bounded native root can be integrated, but it does not mean the whole mod compiles or all CRT visuals have full original-game parity.

## Reproduce

From the module:

```sh
python3 porting/tests/crt-native/run.py --module . \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)" \
  --legacy-client /tmp/nagaport-legacy-client.jar
python3 porting/tests/crt-native/frame-run.py \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)"
```

All external dependency inputs must already be cached. Neither command downloads or installs anything. Add `--original ../ntm-original-pristine` to the first command to verify the checkout rather than the bundled exact original archive.
