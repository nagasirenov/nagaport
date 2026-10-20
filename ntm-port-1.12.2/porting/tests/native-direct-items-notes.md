# Native plain-item direct mesh slice

## Scope and ownership

`ItemRenderMissileGeneric` (all 28 existing item identities) and `ItemRenderFireExt` now extend the native `TileEntityItemStackRenderer` through `DirectMeshItemRenderer`. Their remaining geometry bodies are context-independent. Native `IBakedModel.handlePerspective(TransformType)` supplies pure per-context matrices, including the original time-based GUI rotations, without GL callback capture, legacy helper enums, or `Object[]` dispatch.

`DirectMeshItemModel` creates an immutable per-call pose snapshot. It retains no stack or holder reference, and no mutable last-context/thread-local relay exists. The native renderer receives the actual stack. Existing arm-local sneak compensation is reused with separate left/right snapshot matrices; absent holders create fresh uncorrected snapshots rather than retaining the previous pose. Full3D tools and Boltgun are deliberately not included. Boltgun has a first-person-only animated part and swing side effect and requires a separately proven native draw-context contract.

`DirectMeshItemRegistration` owns an identity map, registers every metadata through the native mesh definition, preserves each freshly baked source model's atlas particle, installs the TEISR, and supplies the existing same-pivot frame correction. If the source particle is missing, it logs an explicit warning rather than claiming icon parity. Parent integration collects exactly 29 direct registrations after the native item registry has been flushed, invokes `register(currentEvent)` explicitly, subscribes for later bake/frame events, and adds this owner's identities to the existing single dropped-item owner. There is no second dropped-item renderer replacement. `ItemRenderMissileGeneric.init()` still precedes use.

## Preserved behavior

- The entire original missile consumer, resource, mesh and scale table is byte-for-byte source guarded against CP26 `f45ef1d63a0d8cd793b90d207d6ea7362793d442`. All 28 entries, the 1.5 strong-missile scale, stealth branch, and existing generic helper methods remain unchanged.
- Missile render selection still uses the actual stack's singular `ComparableStack`; unknown keys still draw nothing.
- FireExt still performs config index zero, receiver zero, magazine lookup, then the original two type checks with the actual stack and null holder, selecting default/foam/sand materials exactly as before. The complete mesh, smooth shade, and culling remain.
- Pure local matrices preserve the exact original context operations, including FireExt's non-axis-aligned `(0,1,1)` rotation and negative GUI Z scale. Caller compensation derives from the ordinary-item, non-block helper branch documented in `porting/native-item-render-assessment.md`. It is not a full3D tool matrix.
- Approved extensions remain mirrored native left hands, the existing extra-angle frame pivot policy, and centered meshes for HEAD/NONE. Existing single-mesh dropped ownership preserves arbitrary stack counts without copying/mutating the stack.
- GUI light installation belongs to the native outer `RenderItem` GUI caller: model perspective intentionally enables lighting, and missiles install GUI-standard directions after undoing the native GUI scale but before applying local geometry. The temporary matrix is balanced. FireExt retains caller light directions. These intended GUI lights are not rolled back by the perspective method. Draw scopes separately restore matrix, texture binding/cache, enable flags, shade/lighting state, color, and alpha-test state even after nested failures.

## Reproduction and evidence

```
python3 porting/tests/probe-native-direct-items.py \
  --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)" \
  --java-home /workspace/shared/nagaport-toolchain/java8
```

Result: **PASS 531 native checks**, plus the exact missile source-table guard. Final log: `porting/continuation-2026-10-19/direct-items-native-FINAL.log`. Production source hashes are recorded there. No download, install, remote action, or substitute platform implementation was used.

The probe compiles the complete two changed renderers, all four new helpers, and their actual native support classes against cached Forge 14.23.5.2768/MC 1.12.2, LWJGL and vecmath. It creates real Mesa/EGL OpenGL, actual Minecraft client settings, native stacks/NBT, ModelRegistryEvent and ModelBakeEvent. Three animation times, all eight missile layouts, and all nine native contexts compare independent original GL calls against production local matrices and old/native caller-compensated matrices. It also covers FireExt's three material choices, actual stack identity/count/metadata/NBT, unchanged draw ordering/state, unknown missile metadata, large-missile scaling, nested/exception/next-call isolation, defensive identity registration, and resource-reload particle/TEISR ownership. Actual GUI light directions, caller matrix balance, nested shade/texture restoration and native texture-cache reuse are checked.

Explicit bounded collaborators: generated test-only `ModItems`, `ResourceManager` resource identifiers/draw spies, `RecipesCommon.ComparableStack`, and FireExt's gun/config/receiver/magazine factory. No Minecraft, Forge, LWJGL, vecmath or Java platform class is stubbed. The ComparableStack fixture covers only non-null ordinary item identity/count/metadata needed here, not its wildcard/null/registry error behavior; production ComparableStack is unchanged. Resource spies inspect real GL state but do not load/draw the actual OBJ assets in this probe. Full gun/config behavior, real holder render-manager selection, actual game scenes, camera/equip timing, resource/model loading for every material, full server startup, full build success, and pixel/visual acceptance are not claimed. Existing source and native model proofs remain separate evidence, not a game acceptance claim.
