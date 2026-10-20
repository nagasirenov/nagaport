# Native Sedna declaration and first-person root

Root production is 58 files in `native-sedna-root-scope.json`: superclass and 51 consumers, factory, bounded handler/GUI consumers, native hand listener and lexical projection scope. No ClientProxy or gameplay item/projectile changes. The separately tested player/armor companion now gates activation; its manifest explicitly supersedes this root's intermediate ModEventHandlerRenderer hash. See native-sedna-player-notes.md.

## Native ownership and integration

`GunFactoryClient.createItemRenderers()` returns the exact original 67 item/renderer pairs in an identity map. The projectile portion of `init()` is byte-for-byte unchanged. Parent integration creates a separate `ContextItemRegistration` for these renderers during native ModelRegistryEvent. `SednaHandHandler` is registered only by the successful companion transaction. Rejection of contextual ownership leaves original TEISRs unchanged. Hand/FOV/weapon-table lookup uses only an actually activated `ItemRenderWeaponBase` TEISR, never a parallel map that would acquire side effects on rejection.

The existing native context model retains immutable actual holder/stack/TransformType arguments. The new superclass executes real context-dependent draw/setup bodies inside native rendering, not matrix-only capture. The 51 consumer files are source-checked against CP29: only native type/name/holder substitutions and new imports, no numerical/body/animation/material/part changes. All 67 constructors/material arguments are source-checked. This is not a claim that all 51 complete consumers compile yet: the selected full native compile covers the superclass, Coilgun, listener, projection scope and existing context dependencies. Some other bodies retain unrelated legacy APIs.

## Hand timing and projection

Native 1.12 EntityRenderer calls overlays after a canceled RenderHandEvent; old Forge 1614 canceled the entire outer method, requiring the old manual overlay. Keeping that manual overlay would draw twice. The new listener cancels only the native hand body, invokes actual ItemRenderer.renderItemInFirstPerson for its cached main/offhand stack selection, and handles each actual RenderSpecificHandEvent. It leaves exactly one native overlay call under the original custom perspective. Main custom hand retains projection precedence when both are custom. The actual native private EntityRenderer.renderHand method is invoked by the test, not a surrogate dispatch.

Actual cached stacks survive slot switching. Main/offhand and left-primary routing retain independent stack identities and mirror the native physical side. Mixed ordinary/custom hands retain native ordinary-hand dispatch. Hidden GUI suppression retains the native overlay path. Per-hand drawing uses lexical projection/state scopes; no last holder/stack, Object[] or thread-local hand bridge exists. The original static interpolation clock remains an animation clock only, with nested hand calls restoring its previous value.

`NativeProjectionScope` restores projection/modelview depths and matrices, active texture, texture bindings and enable caches, packed lightmap coordinate state, lighting/color-material caches and ordinary GL attributes after normal/nested/exception draws. Native EntityRenderer's final successful overlay projection remains intentional caller-owned state. Whole native renderer exception cleanup beyond these owned scopes is not claimed.

## Evidence

`sedna-root-native-FINAL.log`: PASS 171 native checks plus source contract. The independent original Coilgun matrix transcription checks all native contexts with aim/recoil/reload samples. Native RenderItem/model calls execute the actual Coilgun body. Actual stack count/metadata/NBT is unchanged. Actual smoke and laser BufferBuilder bytes prove color, normal, UV and light channels. The source contract proves all 44 shared effect vertices and their order/UV/alpha expressions unchanged.

A real native buffer test caught BufferBuilder.lightmap's reverse short-component storage: legacy packed brightness 240 requires `.lightmap(0, 240)`, producing native shorts `[240, 0]`. All 36 flash vertices use that corrected order. No light level or balance change was intended or made.

The bounded production `ModEventHandlerClient.setupFOV` method is extracted verbatim into the probe because the surrounding legacy class is not yet fully native. It uses the real native FOVUpdateEvent, preserves the original current-held-stack formula, and does nothing for empty/unactivated items. GUIWeaponTable's lookup alone changes; its original preview setup/draw body is untouched and whole GUI compilation is not claimed.

The test runs cached actual Forge/Minecraft/LWJGL and EGL with genuine Forge event transformation. Native player/world/controller subclasses provide controlled data or ordinary-hand/overlay draw observation boundaries. NTM resource meshes and animation/config collaborators are controlled. It does not fake platform classes. Native overlay invocation count and projection are proven, not underwater/fire overlay pixels or a launched game. All consumers' live assets, gameplay animation scheduling, slim/child/armor/manly integration, shaders and external-mod compatibility remain separate acceptance boundaries.

Run without installs/network:

```
python3 porting/tests/check-native-sedna-source.py
python3 porting/tests/probe-native-sedna-items.py --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)" --java-home /workspace/shared/nagaport-toolchain/java8
```

The delivered source contract uses the pinned CP31 reversible source boundary, not local unpublished Git objects. All 53 original inputs used by that contract were verified identical between CP29 and CP30; it also passed in the standalone frozen snapshot with no Git directory. See sedna-portable-source-contract.log.
