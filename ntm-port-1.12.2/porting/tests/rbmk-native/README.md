# Native RBMK column renderer boundary

Pinned source base: CP46, 641ba083c276c6ac75afa6a8f996cdd86f8b16d4. This is a local, finite renderer slice, not a complete reactor port or a runnable-mod claim. No core reactor method, neutron mechanic, inventory lifecycle, lid installation/removal, dummy-tile creation, resource asset or balance value is intentionally changed.

## Production scope

The three removed ISBRH owners are replaced by native baked geometry for the exact original 18 registry/class pairs (4 rods, 7 passive, 7 piped). `RBMKRenderState` supplies an immutable neighborhood/lid snapshot; `RBMKModelHandler` registers native world and item model routes and reloads the three original meshes. `RBMKBase` adds the actual native extended-state/layer hooks; four descendants use the owned texture lookup. `ModBlocks` changes only 18 constructors and their 18 deferred registrations. `ClientProxy` retires only the three old block-handler registrations.

The retained integer family constants in RBMKBase are NTM descendant tags, not engine renderer IDs; the actual native IBlockState render callback returns MODEL. Original IIcon callbacks remain for existing icon consumers and share the same exact texture identities. No old Forge/Minecraft facade is introduced.

Native CUTOUT_MIPPED retains pass-0 binary-alpha material holes, including glass lids and pipes. It does not invent blended glass. Standard cuboid geometry reuses the existing independently tested native helper; OBJ parsing uses the existing real mod-owned parser. OBJ world vertices retain original block-position packed light and ObjUtil shading under both native Forge flat and smooth-AO consumers. Raised cube geometry is evaluated with the native lighter at the original y+1 render origin, then moved back into the owning chunk frame.

## Reproduction

From the module root:

    python3 porting/tests/rbmk-native/run.py --module . --java-home JAVA8 --classpath CLASSPATH --legacy-client ORIGINAL_1_7_10_CLIENT
    python3 porting/tests/rbmk-native/verify_scope.py --module .

Use the resolved project native dependency classpath, Java 8 and the original client whose SHA-256 is pinned in source-manifest.json. No dependencies or original Minecraft binary are distributed. The optional --original-root reads a pristine checkout instead of the bundled pinned source excerpt archive. Both routes require the same hashes.

Final proof: 390,511 assertions and 1,053,360 original vertex comparisons (151,504 standard-call vertices; 901,856 other vertices). The independent extracted-source oracle uses actual original Minecraft RenderBlocks face bytecode in an isolated class loader, not the native implementation. It covers all 18 owners, all 16 metadata, absent/no/standard/glass core outcomes, ten cull masks, original inventory geometry/material/color batches and callable original damage selectors. ReaSim control DOWN-before-pipe texture precedence, original no-world-fuel geometry, triangle duplication, exact named group choice, shift and cuboid UV/winding are covered.

All 16 complete production helper/model/loader/dependency classes compile against real Minecraft/Forge. Five render-only methods extracted verbatim from actual RBMKBase also compile and execute against native states. The test has explicit NTM owner fixtures: 13 classes carrying the exact original registry-class names, inheriting a native Block recording shell. Its core locator and `hasOwnLid` fixture isolate unported reactor collaborators. This is NOT complete production RBMKBase/descendant compilation or reactor mechanics execution. No Minecraft or Forge substitute classes are supplied.

Real native tests exercise immutable extended states, actual TextureAtlasSprite packing, BufferBuilder light/color output, Forge flat/smooth context restoration on success and intentional sink failure, exact-owned ModelRegistryEvent routes, 288 listed states and 288 item metadata routes, ModelLoaderRegistry, ItemModelMesher, BlockModelShapes, directory/JAR resource reads and resource-reload mesh replacement. The resource-reload test was corrected to call the actual resource manager reload, rather than wrongly expecting clearModelCache alone to notify registered resource listeners.

## Explicit open acceptance gates

1. **Default native damage dispatch is not equivalent.** Forge's ordinary SimpleBakedModel damage builder indiscriminately retextures every quad and strips the custom lighting context. Original rod Inner remains its own texture while Cap/outer/lid faces crack. On the pinned asymmetric test scene, every rod has 32 wrongly retextured Inner quads; see proof.log for exact light/color differences for every owner and both native light pipelines. Raised control pipes and passive/boiler/heater lids lose their y+1 lighting context too. These are unresolved, not approved simplifications.
2. `RBMKBakedModel.getBreakingQuads` preserves and tests the original mixed-material selector and the scoped native light adapters. It is a clearly callable preparation routine only: **it is not registered with in-game damage dispatch**. The full original RenderGlobal no-color damage upload/GL blend/depth path is not certified. Tileless RBMK dummies prevent reusing the existing tile-only breaking renderer without altering mechanics. No dummy tile, global dispatcher interception or speculative workaround was added.
3. Optional vanilla non-Forge rendering bypasses custom quad pipe adapters. Its fixed OBJ light/raised-cube behavior is not certified here. Unknown mod VertexLighterFlat subclasses are not certified. Native Forge standard-cube light solver use is verified, but full old-vs-native AO/light-solver visual equality is not claimed.
4. Original inventory startDrawingQuads clears hasColor. The rod Inner/Cap/shell and ordinary cuboid batches rely on current GL color, while explicit fuel batches supply green. GL current-color carry after a color-array draw is not guaranteed by the API. Native meshes use neutral white for non-color batches; explicit original fuel/control-lid colors are tested. This is an explicit original-state graphical acceptance limitation, not a claim to reproduce undefined GL carry.
5. No actual GL/framebuffer test or launched game/client/server/save/network/radiation/energy proof was run in this slice. Original cuboid-face bytecode and actual native CPU buffers are not GL screenshots or gameplay acceptance.

The previously pending CP37 lighting decision is not approval for these gaps. No new policy decision is inferred. Parent coordinates the single full production compile and any checkpoint artifact; this worker performed no Gradle build or push.
