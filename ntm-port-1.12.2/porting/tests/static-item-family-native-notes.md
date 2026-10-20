# Native static item family

Checkpoint21 input: 290af844793ced6c20380adfaa83aab764ae6197. This slice migrates complete source-owned static item meshes, not world TESRs or weapon mechanics. No Forge/Minecraft substitute classes, old item-render enum/helper adapter, synthetic tile or icon-only mesh replacement is introduced.

## Effective original ownership

The strict lexical set contains162 anonymous ItemRenderBase bodies in82 files. Source caller audit proves every body uses non-block3D/plain ItemBlock callers: original getRenderType -1 except solar_mirror's custom ISBRH ID whose shouldRender3DInInventory is false. Forge1614's patched RenderBlocks fallback consults RenderingRegistry; the original mod registers no -1 handler. Both plain ItemBlock and ItemBlockBase leave Item.isFull3D false and max stack64; ItemPrototypeBlock only adds tooltip text. All have the same original single-mesh dropped policy.

There are161 effective item identities: library machine_well is shadowed by later RenderDerrick registration. The migration removes the shadowed library body but publishes only RenderDerrick's mesh/transforms. Effective classes are82 ItemBlockBase,78 plain ItemBlock and1 ItemPrototypeBlock. Sentry's two original TESR bindings redundantly register the same normal turret_sentry item; no damaged-owner invention occurs. RenderGeiger's later original TESR binding is the effective one.

`static-item-family-caller-matrix.json` gives all162 classifications with source references. `static-item-family-source-manifest.json` retains every original inventory/common operation sequence, item/resource bootstrap bindings, and reversible pre-migration source text. Original bodies remain the authority, not the earlier informal five-item scratch design: steel/combination/oilburner inventory Y is -1.5; heatex/heating oven Y is -1. All five scale3.25.

## Native design

Nine new classes in nativeitem/staticfamily provide typed static descriptors, explicit real-item/resource bindings, immutable camera matrices/arm-local sneak compensation, complete ordered native TEISR draws, model/frame/particle ownership and the dropped single-copy seam. Existing core_angel matrices and mesh behavior are not reused with its extra common S(4). Generic static matrices retain the original ItemRenderBase S(.25); each original common draw keeps its literal additional scales and interleaved transforms.

Every original renderAll/renderPart/renderOnly call stays in source order, with the same mesh, texture, material changes and shade/cull/color/lighting operations. Heating oven still issues separate Main then Door calls. No universal smooth override is added. The raw GL scope restores enable, lighting/shade and current color state; texture binding is restored through GlStateManager. Native caller inventory overlays remain outside TEISR; the original custom mesh had no vanilla glint, including forced enchanted tags.

ModelRegistryEvent uses an item-keyed custom native mesh definition plus explicit variant registration, preserving all damage values rather than assuming metadata0. ModelBakeEvent validates all complete native models before publishing; reload installs new immutable models while retaining identity keys and stable reload-owned mesh objects. The family only replaces its explicit161 item identities.

Approved new native contexts remain deliberately separate from original parity: Forge flip-X mirrored left/offhand, odd frame angles extending the same legacy pivot, and HEAD/NONE displaying the real centered mesh at ordinary ground scale without ground lift/bobbing. The four original frame placements and ordinary right-hand/standing arm-local geometry retain the independently derived original caller matrices. This is not a promise to reproduce all old player-root/crouch/child/slim/custom skeleton behavior.

## Dropped ownership and compatibility

SingleMeshEntityItemRenderer overrides only native RenderEntityItem.getModelCount. It snapshots identity owners, returns1 for those stacks and calls the actual vanilla superclass for all others. It does not mutate counts, NBT, stack identities, physics, bobbing, rotation or spread policy.

StaticItemEntityOwnership is registered once on MinecraftForge.EVENT_BUS. Actual pinned Forge2768 FMLCommonHandler has eventBus=MinecraftForge.EVENT_BUS and bus() returns that field; onRenderTickStart posts RenderTickEvent.START there. A repeatable javap audit is included in the ownership runner. The native listener is LOWEST priority and waits until the first START, after both factory and deprecated startup renderer maps have loaded. FMLLoadCompleteEvent alone is too early.

It replaces only an exact RenderEntityItem.class live owner, with an immediate identity recheck. Unknown/subclass/null owners are not overwritten; one explicit warning marks that external-renderer combination unsupported, without a new crash. Later ownership loss is not reclaimed. Conventional EntityItem RenderingRegistry registration is intentionally not used: its pending factory map blindly replaces competing factories before a safe live-owner check. Normal native item models remain active in a conflict, but single-copy parity is not claimed for a foreign renderer. No arbitrary cross-mod compatibility promise is made.

## Original particle images, not mesh-material atlas duplication

Independent source ancestry audit finds158 inherited vanilla blockIcon paths and3 explicit registerBlockIcons overrides: test_render, machine_uf6_tank and machine_puf6_tank. All effective item damage values resolve the same original icon. `static-item-family-particles.json` records each source route, actual PNG/hash/dimensions, and special handling.

The161 owners share63 original icons, not152 full mesh-material atlas additions.61 icons use standard native sprites; geiger55x55 and solar_mirror36x36 use the shared mip-aligned custom sprite helper on those same original icon PNGs under dedicated atlas keys. Mesh materials/UVs are unchanged. Five original mixed-case icon files are byte-identical case-only renames to native lowercase names, not case-colliding aliases: lilBoy, theGadget, ivyMike, fatMan, tsarBomba. The portable source guard checks actual directory-entry casing.

## Verification and boundaries

Representative first-five proof passed101,525 assertions before expansion. Latest all161 direct probe:3,796,081 executable assertions, plus1,597 separately labeled source/draw/resource/particle guards. It compiles the complete native family recipe/model/render classes against actual Forge2768 and actual mesh parser, invokes every complete production recipe factory, reconstructs original matrices independently in double precision, checks actual OBJ groups/vertices, executes ModelRegistryEvent registration plus native ModelLoader.onRegisterItems and ItemModelMesherForge routing across damages0/1/3/32767, validates no stack/NBT mutation, and checks bake validation/reload/cache ownership.

A test-only MeshModel data holder contains actual parsed OBJ data and throws if asked to draw. No fake Minecraft/Forge class or pretend GL execution is used. Actual ModBlocks/ResourceManager bootstrap wiring is exact-source guarded here; that one dependency-heavy bootstrap is compiled by the parent whole-project frontier, not silently claimed as isolated executable proof.

Dropped ownership probe passes2,893 actual-native count/identity/ownership/order assertions plus3 separately labeled bytecode bus/dispatch checks. It allocates actual RenderManager via Unsafe to avoid unrelated constructor GL setup, as explicitly isolated fixture state. Direct listener tests are not transformed EventBus execution.

Native GL/client screenshots, whole-mod startup, avatar animation, global atlas coexistence, resource-pack reload, neighboring-item state leakage, lighting/culling/depth and unsupported external renderer combinations remain separate client verification. Matrix/source/asset success alone is not visual parity or full gameplay completion.

Combined Gradle execution of testPortStaticItemFamilyNative and testPortStaticItemEntityOwnership is BUILD SUCCESSFUL (28s); complete log: porting/continuation-2026-10-19/static-item-family-native-final.log. Independent reviewer also checked all161 original G sequences, all83 existing-source hashes, icon ancestry/dimensions and case-only consumers without finding another defect. Production is frozen for parent integration.
