# Native selective item family

## Scope and ownership

All six complete original declarations now compile against genuine 1.12.2 Forge/vanilla/LWJGL APIs. No removed IItemRenderer compatibility interface or fake platform classes. `native-selective-item-scope.json` records the exact 11 production files, test files, and original dff345a asset hashes.

ClientProxy registers the original 17 Transformer items, crucible_template, detonator_laser and steel_roof through the existing deferred ContextItemRegistration lifecycle. PoleTop and SatelliteReceiver had no original registrations and remain unregistered; their declarations are converted and invoked in the native test, not newly activated in gameplay. The parent owns the ClientProxy integration.

NativeSelectiveItemRenderer is opt-in. Declined contexts return the complete original base model's own override/perspective result. BakedItemModel's distinct GUI model is retained, not replaced with a particle quad. Native base camera scale remains visible before dropped-item perspective, preserving the ordinary entity lift. FrameBaseModel allows the existing machine renderer to choose the resolved base before its ordinary cube/icon/count/glint decision. Non-selective contextual items keep their prior model and unmodified native machine fallback.

Only renderers explicitly implementing FrameItemModel receive the private frame-model subclass. The three Java-model deco renderers retain their original full ModelBase geometry and `.0625` scale. GUI stays ordinary. The original non-3D ItemBlock branch uses ordinary equipped caller compensation. Dedicated dropped/machine draws retain one mesh, no bob, no rotation and no spread, including oversized stacks; fixed-frame composition removes the original bob that these renderers never requested. Parent merges the steel_roof dropped draw only after successful context ownership activation, and excludes all selective renderers from the single-mesh default entity override. Existing compatibility/rollback gates remain in place.

Transformer retains the original unused tfp parameter, original rotation/scale order, first-layer pixel extrusion, HBM-specific `hbm:textures/misc/glint.png`, doubled no-effect GUI draws and original custom glint passes. Full-3D third-person caller compensation is independently checked against the pinned RenderPlayer bytecode; it is not the ordinary plain-item caller. Entity/FIXED/HEAD/NONE retain the complete native base. Unknown non-generated model geometry is rendered natively rather than flattened to a particle sprite. Sword use/gameplay and original-vs-native first-person blocking behavior are not certified by this rendering probe.

Template derives its preview from the current stack rather than a mutable last callback. Only left-Shift GUI owns the callback; normal GUI and every world/hand context preserve the actual base model. Missing recipes and self-recursive previews safely retain the ordinary icon instead of a stale prior recipe. Nested previews use the installed native renderer and retain the original doubled RenderItemStack draw.

Detonator preserves original Main/Lights VBO groups, material, all original local transforms, red fullbright lights, 32-segment yellow waveform and three changing six-digit red font lines. Matrix/attribute and lightmap state is restored on successful draws and controlled model/font failures. The original HFR VBO and actual native font are invoked, not a mesh stub.

Three original uppercase Java-model textures are renamed lowercase without changing one byte, because native ResourceLocation normalizes the requested paths. Both directory and ZIP resource packs load and render the exact assets. The OBJ, detonator material and HBM glint are also byte-identical to dff345a.

## Reproduction and evidence

Use Java 8 and the cached complete native classpath:

    python3 porting/tests/probe-native-selective-items.py --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)" --java-home /workspace/shared/nagaport-toolchain/java8

The runner compiles all six selected declarations plus actual production support classes. Its only NTM data collaborators are the ResourceManager field holder, ModItems crucible identity and CrucibleRecipes data map. Actual MeshModel/HFR parser/VBO, all three actual Java models, textures, vanilla/Forge model/render/GL/BufferBuilder classes and native font are unchanged production/native code. EGL is Mesa surfaceless compatibility GL. The two resource runs are recorded in `continuation-2026-10-19/selective-items-native-FINAL.log`.

Additional terminal regressions:
- Context owner/cache/rollback/reload/typed dispatch: 163 checks.
- Sedna hand/context/projection/overlay: 171 checks.
- Sedna player ownership/rollback/render phase: 145 checks.
- Native machine: all 14 complete TESR consumer declarations compile, 26 base bake/reload checks, 111 actual consumer checks, 192 GUI/frame/framebuffer checks and 41 generated-model/frame-matrix checks. The 192 include 40 selective ordinary frame combinations, each checking selection/glint, framebuffer equality and matrix balance, across two enchant states, two graphics modes, five stack counts and two displays (40 combinations total).

This is selected native declaration and real invoked headless render evidence, not a full-mod compile, whole-game launch, screenshot parity, multiplayer acceptance or gameplay acceptance. Original-vs-native visual review of hand motion, frame placement, blocking, resource reload in a running world and surrounding item lighting remains required.
