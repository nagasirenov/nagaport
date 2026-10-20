# Native core_angel item renderer

Bounded production slice on parent baseline 5f720e0f82340aa29f2613ff71fc706689d7c3aa, 2026-10-19. The old ItemRenderAngelCore inheritance and one MinecraftForgeClient.registerItemRenderer call are replaced with native model/event/TEISR registration. No other legacy renderer families are converted, no source exclusion, Forge shim or substituted Minecraft class is introduced.

## Ownership

ClientProxy registers AngelCoreModelHandler before client model events. ModelRegistryEvent maps metadata 0; ModelBakeEvent installs a new complete AngelCoreItemModel and attaches ItemRenderAngelCore to the real Item. A real bfa_body particle sprite is registered at texture stitch. Re-baking replaces the model without changing its ownership key. ResourceManager still owns bfangel/HFR reload lifecycle and bfangel_tex; drawing uses the real `body` group with smooth shading and original UVs/normals.

The generic StaticMeshItemModel owns defensive copies of matrices and dispatches complete geometry through TEISR. Empty baked quads are intentional native built-in rendering, not missing geometry. StaticMeshItemRenderer draws the supplied real mesh/parts and texture, no duplicate scale, tile entity surrogate, icons, glint or overlays. ItemRenderAngelCore is now a native subclass supplying the original body selector. Inventory charge/count/durability overlays remain native outer Item rendering; core originally did not draw mesh glint, including forced enchanted tags.

## Geometry

AngelCorePerspectives.derived implements the exact matrices in porting/native-item-render-assessment.md for GUI, ground, ordinary first-person right hand and standing arm-local third-person right hand. It includes original GUI depth -103, native ground lift -.25, old non-block equipped helper and the final +.5 origin compensation. No extra center/corner conversion is applied by the caller.

Frame correction was independently derived from old bnx and actual native RenderItemFrame bytecode:
- Event occurs after frame root F and Rz(45r), before native S(.5), lighting and FIXED draw.
- Apply T(-.02 sin(45r), -.02 cos(45r), .015625) in that event. Do not cancel or pop away the change; native caller owns the scope.
- P_FIXED = S(2) T(0,-.06,0) S(.75) Ry(270) T(.5,.5,.5).
- For old k=0..3 mapped to native r=2k, this equals old F T(0,-.18,.015625) Ry(180) Rz(-90k) T(o_k) T(0,.1,0) S(.75) Ry(90), with o=(0,0),(-.16,-.16),(0,-.32),(.16,-.16).
- Native hangingPosition is original support block plus outward wall direction. The wall-local depth difference is .015625.

Native LayerHeldItem adds T(0,.2,0) before posed arm A, unlike the original. ItemOverrideList snapshots actual native ModelBiped arm angles into a new immutable model and compensates with N^-1 A^-1 T(0,-.2,0) A N before the ordinary perspective. ModelRenderer uses Z,Y,X, and its float degree conversion is retained. Arm pivot translations cancel in this conjugation. Left-hand correction accounts for Forge's subsequent flip-X conjugation. No entity, stack or last-perspective singleton/global is stored.

This preserves core geometry relative to the native posed arm, not the entire old avatar. Old/native player-root crouch translations, slim-arm geometry, child scaling and non-biped/custom renderers retain native avatar behavior and require visual coverage. An unknown/non-biped holder does not crash or disappear: it keeps the ordinary real-mesh perspective, without guessing its private skeleton.

## Explicitly approved new 1.12 policies

Approval was forwarded by parent from the user's 2026-10-19 reply to its specific proposal. complete() is separate from derived():
- Left/offhand uses Forge flip-X mirrored ordinary-item transforms.
- Odd frame rotations continuously extend the same off-center pivot.
- HEAD/NONE display the real centered mesh at ordinary ground-item scale/orientation, without ground bobbing/lift correction.
These are intentional new-context presentation choices, not claims of 1.7 parity. Direct FIXED outside a frame uses its normal fixed mesh transform without the entity-frame correction.

## GL and lighting

The GUI model installs original GUI-standard lights before the returned geometry transform. At this boundary native has already applied S(16,-16,16); its inverse is applied in a temporary matrix scope while setting directional lights, then popped. This uses native GlStateManager/RenderHelper consistently. It does not retain mutable context for TEISR.

The mesh renderer uses a matrix and raw enable/lighting attribute scope for cull/alpha/smooth shading and restores texture binding using GlStateManager, keeping the texture binding cache synchronized. Texture and matrix cleanup runs in finally. It does not read the float TEISR overload as a frame clock: actual Forge 2768 RenderItem invokes the one-argument method, whose superclass would forward 1.0F. The core is static.

These contracts compile; no real GL/client execution or screenshot comparison has yet verified lighting, culling, depth, adjacent-item state leakage, resource-pack reload or avatar presentation. Do not claim complete visual parity from algebra.

## Verification

`testPortAngelCoreNative` via porting/angel-core-native-probe.init.gradle compiles every complete production class in the slice plus actual MeshModel/ObjMesh/ObjMeshParser against the resolved Forge 2768/native dependencies. It does not compile a stub ResourceManager or excerpt renderer. Parent owns whole-project build/source guards.

Latest direct equivalent probe: PASS 21,024 assertions, /tmp/angel-core-native-proof.log. Independent double-precision matrices compare old caller and original GL operation sequences against production float matrices at transformed basis/corner points: 201 first-person swing/equip, ground, posed-arm third-person and right sneak samples; four original frame placements; 201 left-native sneak compensation samples. The model uses defensive matrix copies. Actual native ModelBakeEvent/RegistrySimple and Item TEISR binding execute, including re-bake identity. Actual balefire_angel asset's body has 128 faces/384 corners, each with UV and normal. No substitute Minecraft/Forge types.

## Next static-family inventory (not yet migrated)

Read-only strict lexical audit identifies 162 anonymous ItemRenderBase bodies across 82 files: 82 in ItemRenderLibrary/Doors and 80 tile providers. Each has only literal GL inventory transforms and direct static ResourceManager mesh/material/GL common operations; no additional callbacks, time/entity/stack reads, recursive item rendering or custom glint. Full exact bodies and locations: /tmp/nagaport-static-item-candidates-strict.json (temporary audit artifact).

Before batch migration, verify original item-caller branch for each owner (plain item vs full-3D/block branch) and helpers; static callback shape alone does not prove the AngelCore ordinary-item third-person caller is correct for every ItemBlock. The next pattern needs generic context matrices taking captured inventory transforms and a real ordered multi-material draw callback; no old ItemRenderType/helper emulation. Preserve operation ordering and provider registration overrides. Parent owns batching, manifests and source guards.

Additional batch gate: most machine ItemBlocks are stackable. Original non-block custom ENTITY dispatch renders one object; native RenderEntityItem chooses count-based multiple copies. The core max-stack-one exemption does not generalize. Do not fake isGui3d, mutate ItemStack count or accept repeated meshes silently. A separate native entity-render ownership path that draws one original custom object, while preserving stack physics/count and delegating unrelated items, needs a representative and tests before the stackable static family can activate.
