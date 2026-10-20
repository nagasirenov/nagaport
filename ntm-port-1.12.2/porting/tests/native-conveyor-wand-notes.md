# Native conveyor-wand rendering, bounded closure

Production is frozen to the seven files in `native-conveyor-wand-scope.json`. ClientProxy and conveyor block/model/asset integration belong to separate manifests. No model/player/Sedna changes are included here.

## Public integration

- `new ItemRenderBlock(Block...)`: same public constructor; same original array order and damage index; selected block metadata is always zero.
- `new BlockItemRegistration(Item, ItemRenderBlock)`: subscribe to the native event bus. If constructed during `ModelRegistryEvent`, explicitly call `register(event)` once for that in-flight event. The subscriber handles native model bake/reload and item-frame correction.
- `BlockItemRegistration.droppedRenderers()`: immutable `Map<Item, SingleMeshEntityItemRenderer.BlockItemDraw>`.
- `new StaticItemEntityOwnership(oneMeshItems, droppedRenderers, warning)`: optional map added to the existing first-RenderTick exact-native ownership acquisition. All old public and package supplier constructors remain unchanged in signature and default behavior. The wand must not also be a one-mesh item.
- The existing `NativeMachineItemRenderer` recognizes the narrow `FrameItemModel` contract. Only `BlockItemModel` implements it. The runner proves every previous machine branch byte-identical after removing the interface and its dispatch block.

## Original behavior and matrix derivation

The pinned original `fixtures/ItemRenderBlock-legacy.java.txt` returns true for every old render helper. It selects four conveyors by item damage, calls `renderBlockAsItem(block, 0, 1F)`, and adds (+.5,+.5,+.5) only in the two held contexts. The original public mesh API was not a plain item-renderer callback.

Audited cached Forge 1.7.10 build 1614 `ForgeHooksClient.renderInventoryItem`, `renderEquippedItem`, `renderEntityItem`, the actual old RenderItem patch/client bytecode, and RenderPlayer/RenderItemFrame bytecode. This is a native adaptation of that bounded caller, not a general IItemRenderer compatibility implementation.

- GUI: `T(x-2,y+3,z-3) S10 T(1,.5,1) S(1,1,-1) Rx210 Ry45 Ry-90`, before centered inventory geometry. Native GUI supplies its normal outer matrix; `BlockItemPerspectives` supplies the exact inverse-adjusted helper matrix. The actual RenderItem GUI entrypoint is exercised, including its +50 effect depth.
- First-person: original .4 scale, with the old helper -.5 and renderer +.5 cancelling. Native's -45 degree suffix is compensated by the model's +45.
- Third-person: original block-3D held path after the posed arm is `T(-.0625,.4375,.0625) T(0,.1875,-.3125) Rx20 Ry45 S(-.375,-.375,.375)`. Native arm suffix is inverted. Existing immutable, arm-aware native sneaking compensation is reused; the previous family test owns its broad equation proof.
- Dropped: the wand is not an ItemBlock, so old all-true BLOCK_3D chooses renderType 1 and scale .5, not .25. Counts are 1/2/3/4/5 at >1/>5/>20/>40. Seed 187 gives extra-copy xyz offsets `((nextFloat*2-1)*.2)/.5`. Old bob and rotation use age/partial/hover exactly. Native RenderEntityItem instead uses >16/>32/>48 and another seed/spread, requiring the bounded identity delegate before those operations.
- Ordinary item frames: actual native private item-render phase, genuine Forge frame event, and FIXED RenderItem path are tested against the four original frame rotations and offsets. Intermediate eight-angle native rotations extend the established original pivot.
- Machine/decorative frames: existing explicit frame context is retained. Both use the old `S1.25 T(0,.05,0) Ry(-90)` helper before scale .5; decoration has no bob and exactly one copy, while entity frames retain bob and original copy thresholds.
- HEAD/NONE have no old context to preserve. Per approved native-context policy, they show the centered selected conveyor mesh, not the generic wand icon. Native offhand uses Forge's standard flip-X extension. These are presentation adaptations, not legacy-context parity claims.

The production conveyor inventory model supplies six actual unculled quads and the +.25 Y coordinate compensation. Native final `T(-.5,-.5,-.5)` therefore gives the original conveyor inventory `T(-.5,-.25,-.5)` relative to its 0..1 by 0..0.25 slab. Its own block camera transforms must not be applied again. `ConveyorBlockModelHandler.inventoryModelLocation(selectedBlock, 0)` resolves the current runtime native model manager, never a cached RenderItem/model/ItemBlock from preInit.

No count, damage, enchantment or NBT changes/copies occur in rendering. Original custom-helper draws have no generic enchantment pass; the genuine native built-in model/TEISR route preserves that. Conveyor quads are untinted and the original white block color is retained instead of arbitrary wand ItemColors tint. Original baked item geometry remains available through immutable `NativeItemBaseModel` ownership for existing explicit GUI bypass paths.

## Genuine native proof

Run `probe-native-conveyor-wand.py --classpath <cached native classpath> --java-home <Java 8>`.

Final evidence: `../continuation-2026-10-19/native-conveyor-wand-FINAL.log`.

- Whole seven-file root, all nine complete conveyor owner classes/loader/models, fourteen machine consumers and real helper/context classes compile against the actual target jar. Only explicitly declared NTM gameplay/data/mesh collaborators are controlled fixtures; no Minecraft/Forge/platform types are replaced.
- Genuine Forge LaunchClassLoader plus EventSubscriptionTransformer; actual native registries, ModelRegistryEvent, ModelBakeEvent, RenderTickEvent, production custom loader and native ItemModelMesher. ModelLoader initializes the real registry and resource manager. No offline event transformation or fake registry.
- 1,872 checks: actual native GUI/held/HEAD/NONE draws; all four selected baked models; full native item-frame phase; complete dropped/machine-frame copies/matrices at every divergent count threshold; original stack identity/NBT preservation; real framebuffer output; no custom glint; actual optional ownership installation and immutable map snapshot; failed bake cannot use stale live models or partially replace owners; real cache clear/rebake and runtime renderer replacement.
- Unrelated item draws are compared directly with actual native RenderEntityItem; old one-mesh item draws with the unchanged-constructor route. Both copy matrices and framebuffer bytes agree, and neither enters the wand callback.
- Existing regressions: 26 bake/reload/base-ownership, 111 complete native press/mass-storage consumer, 72 native machine/GUI draw, 41 native frame/generated-model, and 2,893 dropped count/ownership/lifecycle checks pass.
- The preexisting structural test now permits only the old protected copy-count override plus the new typed doRender dispatch (excluding compiler bridges). All old runtime tests remain. NativeMachineDrawTest's resource lookup now accepts genuine LaunchClassLoader `jar:` code-source URLs as well as ordinary `file:` URLs; no old draw assertion was removed.

## Original base icon asset closure

The original registration passes `conveyor_wand.getUnlocalizedName()`, retaining `item.` in the native registry path. Therefore the native generated base is `models/item/item.conveyor_wand.json`, binding `hbm:items/wand_s` to the unchanged original `textures/items/wand_s.png`. The genuine native ModelBakery.getItemLocation route, ModelLoaderRegistry, PNG-size/frame decoder, generated extrusion, immutable captured particle/base and actual no-effect fallback draw are verified for both directory and JAR resource packs. Directory/JAR framebuffer bytes agree; no custom or enchantment pass is introduced. The seven Java production hashes are unchanged.

## Limits

The EGL test atlas is a controlled solid texture, not a visual screenshot acceptance of the complete game. Actual conveyor materials/UVs/atlas/resource reload are independently proved by the conveyor model suite. This test renders genuine complete quads and verifies matrices, counts and effects, not subjective visual polish.

Unknown/non-exact EntityItem owners are preserved by the preexisting ownership guard and warned once. Native item models remain active, but dropped parity with another mod's renderer is unsupported/unverified. No second owner or legacy renderer shim is installed. Invalid damage retains the original bounded index contract; no invented clamping/default variant is introduced.

This closes the rendering consumer only. The full ItemConveyorWand gameplay class and complete mod/client runtime acceptance are separate work. These selected compile/draw proofs do not imply the whole mod compiles or the wand is playable end to end.
