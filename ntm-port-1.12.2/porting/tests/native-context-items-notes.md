# Native contextual item seam and Boltgun

## Result and frozen scope

`probe-native-context-items.py` passes 163 checks against the cached, actual Forge 1.12.2 classes with a real Mesa EGL compatibility context. The final run is `porting/continuation-2026-10-19/context-items-native-FINAL.log`, including exact compiled production hashes. Integration source-order guards also pass. This is selected native compilation and invoked-path evidence, not a complete client build, game launch, screenshot comparison, or visual acceptance.

Production owned by this slice:

- `render/item/ItemRenderBoltgun.java`
- `render/item/nativeitem/context/ContextItemModel.java`
- `render/item/nativeitem/context/ContextItemRegistration.java`
- `render/item/nativeitem/context/ContextItemRenderer.java`
- `render/item/nativeitem/context/ContextRenderItem.java`
- `render/item/nativeitem/context/NativeItemContext.java`
- `render/item/nativeitem/context/NativeItemFailureScope.java`
- `render/item/nativeitem/context/NativeItemFields.java`
- `render/item/nativeitem/context/NativeItemRendererOwnership.java`

`render/util/NativeItemBaseModel.java` and adaptations of existing model families are owned by the machine-preview slice. This slice implements that interface, retaining the actual original baked model through every immutable override/perspective copy. Parent-owned integration is in `ClientProxy`, `MainRegistry`, and `ServerProxy`.

## Native seam, not a legacy adapter

The actual native virtual `RenderItem.renderItem(ItemStack, IBakedModel)` receives the model returned by `ForgeHooksClient.handleCameraTransforms`. For owned models only, `ContextRenderItem` calls the renderer with the actual method-argument stack and an immutable `NativeItemContext` containing native `TransformType` and the explicit holder from native item overrides. There is no pending context, global last stack/holder, ThreadLocal relay, old Forge renderer API, legacy helper enum, or `Object[]` dispatch. Abandoned/reordered perspective requests and nested or exceptional draws cannot change another draw's context.

The native public holder, holderless, GUI, first-person-cache, dropped-item-cache, snowball-cache, and potion-cache paths execute in the test. Direct third-party TEISR invocation has no perspective/holder argument and explicitly uses `NONE`/null rather than borrowing a previous draw. Standard native perspective paths use the previously derived ordinary non-full3D caller compensation. This does not generalize to full3D tools or Sedna's custom first-person projection without a separate derivation.

Boltgun retains the exact old local matrices, smooth/cull/material behavior, first-person Barrel-only recoil, part ordering, and original local-player swing suppression. It is a plain `Item`, not a full3D-tool branch. Original source is CP28 `fd079268` and pristine `ItemRenderBoltgun`; no gameplay/item class changed. Existing same-pivot frame correction and approved native left-hand/nonstandard-context policy are retained.

## Exhaustive native ownership and lifecycle

The test ASM-scans every class in the actual cached native jar and asserts exactly these seven non-synthetic RenderItem-typed declarations and their only assignment sites:

1. `Minecraft.renderItem`, assigned in `Minecraft.init`.
2. `GuiIngame.itemRenderer`, assigned in its constructor.
3. `GuiScreen.itemRender`, assigned in `setWorldAndResolution`.
4. `ItemRenderer.itemRenderer`, assigned in its constructor.
5. `RenderEntityItem.itemRenderer`, assigned in its constructor.
6. `RenderItemFrame.itemRenderer`, assigned in its constructor.
7. `RenderSnowball.itemRenderer`, assigned in its constructor and inherited by exact native `RenderPotion`.

Ten synthetic RenderItem anonymous-class captures are also audited: none ever reads its retained RenderItem field. Native jar contains no RenderItem subclass.

Actual bytecode establishes synchronous ModelManager reload and ModelBake before RenderItem construction; native `finishMinecraftLoading` runs mod initialization, loads Forge entity renderers, then invokes Loader.loadingComplete. Minecraft constructs GuiIngameForge and the initial screen only after that call returns. Consequently the transaction is restricted to the load-complete window with no current HUD/screen. The test executes actual native subsequent HUD construction and screen initialization and verifies they capture the replacement.

The installer requires exact native Minecraft/RenderItem/ItemRenderer/RenderManager identities, exact native primary dropped/frame owners, and exact permitted entity cache owners including RenderPotion. It scans unknown entity-renderer class hierarchies for inherited RenderItem caches. Missing, mismatched, late, or unknown owners reject the whole attempt before activation and produce an explicit unsupported-combination warning. No foreign renderer is taken over.

All currently live native cache slots, owned models, and TEISRs commit together. Controlled registry-write failure proves rollback. Rollback attempts all cache/model restorations even when one fails; an unrecoverable foreign registry refusal aborts client initialization instead of continuing under partial ownership or falsely reporting preservation.

The original exact ItemModelMesher and all other native RenderItem fields are retained. The originally registered native reload listener remains valid because it owns that same mesher. Tests invoke that original listener and a subsequent active ModelBake to prove new custom/base models resolve through the replacement. Unrelated models call the unchanged native super implementations, including actual vanilla enchanted-item model/glint passes.

Parent integration defers construction of the existing single-mesh dropped owner until after this transaction. Context items join the shared dropped-owner set only after activation succeeds. Static/direct/callback items remain in that existing set regardless. This preserves both the native preflight window and unchanged fallback behavior when contextual ownership is rejected.

## Exceptions and GL scope

Context draws balance their own matrices and attribute scope. Raw mesh GL restoration explicitly reconciles native GlStateManager caches after nested native calls. The test originally reproduced rescale-normal leakage caused by glPopAttrib restoring GL while Minecraft's cached value stayed false; the production synchronization fixes that observed defect.

Owned native RenderItem model/GUI entry scopes restore their touched state and atlas blur/mipmap bookkeeping on exceptions. Successful native caller state transitions remain native, and the atlas filter's nested single-slot save bookkeeping is restored. The GUI effect wrapper also restores zLevel when the native crash-report wrapper throws. A later draw after nested failure proves no stale holder/stack.

This is not a claim that every unrelated external/native caller has finally cleanup. For example native RenderEntityItem and unknown third-party GUI callers own additional outer matrices; this slice does not replace their entire methods to recover after a propagated fatal draw error. Nor does the bounded Boltgun state scope yet certify arbitrary Sedna projection/lightmap/stencil operations.

## Test boundaries and remaining acceptance

The harness uses actual native Minecraft construction, GameSettings, model mesher, model registry, RenderItem implementations, TextureManager/DynamicTexture, GL, Forge camera-transform dispatch and native entities. Unsafe allocates a RenderManager holder without its unrelated complete renderer graph and controlled native EntityPlayerSP subclasses without a world; their supplied state is explicit. NTM ResourceManager meshes and HbmAnimations are controlled draw/recoil collaborators so the actual production Boltgun body can execute without compiling the unrelated legacy game tree. These are not fake Minecraft/Forge classes.

The baseline local-matrix transcription, part ordering, exact stack identity/count/metadata/NBT, recoil and local-only swing effect are checked. Mesh assets, actual gameplay animation timing, pixels, real worlds, slim/child/non-player poses, live texture appearance, complete shaders/OptiFine/modpack compatibility, and whole-game visual acceptance remain open. Existing unresolved native atlas particles produce an explicit warning rather than being called original icon parity.

The ownership guarantee is the audited native graph at load complete. An external mod retaining a renderer outside that graph, taking ownership later, modifying native classes, or bypassing standard item model dispatch is not silently certified. Unknown initially registered cache owners are explicitly gated and left untouched.

Run without downloads or installs:

```
python3 porting/tests/probe-native-context-items.py \
  --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)" \
  --java-home /workspace/shared/nagaport-toolchain/java8
```
