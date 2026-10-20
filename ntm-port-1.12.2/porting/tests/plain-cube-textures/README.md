# Audited plain-cube texture boundary (27 sites, CP59 baseline)

Baseline: `e72ec3d2ae8db6653ebaad529a574ee32c99260b`.

Production changes only 27 construction texture calls in ModBlocks and adds three complete native classes: PlainCubeTextures, PlainCubeModelHandler and PlainCubeModel. BlockOutgas, BlockSmolder and BlockStepEffect inheritance, constructors and gameplay bodies are unchanged. This is NOT a port of their callbacks or a declaration that these blocks are playable.

## Scope and native implementation

The selected calls construct exactly one of those three plain original Block owners, with constant non-null original texture arguments. No icon/color/render/bounds override occurs in their original or current source. Their 24 standard ItemBlock forms plus one each ItemBlockBase, ItemBlockLore and ItemOreBlock have the original white Block color. ItemBlockLore only changes tooltip/rarity; ItemOreBlock only changes rarity. Source scanning found no direct selected-field inherited visual-method consumer. No subclass, custom renderer, CT, contextual icon or multipass owner is admitted by the exact-class-name binding guard. The common binding contains no client-class reference and returns the original Block, retaining receiver-first, texture-second Java evaluation and chain result identity.

Nine other construction chains are deliberately excluded: their existing setStepSound call poisons the complete receiver type during native javac attribution. They remain byte-identical. No partial receiver, helper emulation of Block setters, or inheritance substitution is used to hide these errors.

ModelRegistryEvent runs after real block/item registration. It maps real states and all raw stack-damage values via a native ItemMeshDefinition to an explicit NTM-only route, rejecting a missing late ItemBlock instead of assigning AIR. The custom loader uses actual Forge ModelLoaderRegistry and the native cube_all material model. Native model-cache invalidation and fresh TextureAtlasSprites supply resource reload; no sprite/baked-model cache survives it.

Actual original RenderBlocks bytecode exposed two differences in an unmodified native cube: DOWN V is reversed, and FaceBakery contracts UV endpoints. PlainCubeModel corrects UV0 to the six original face-coordinate conventions. It keeps native geometry, winding, normals, colors, diffuse flags, culling and AO hooks. The perspective wrapper deliberately retains itself: the default BakedModelWrapper would otherwise return the uncorrected model during item rendering. All native display matrices remain unchanged.

## Evidence

- `before.tsv` / `after.tsv`: independent Java8 full-source attribution, whole receiver/first argument and texture expression spans, exact native helper executable. All 27 original receiver types and all 27 native first-argument types are real Block, not ERROR. Before has 4,056 production units; after has 4,059. Unrelated source errors remain; this is not a successful complete-mod compilation.
- `check-source.py`: byte-exact full ModBlocks inversion to immutable CP59, including line endings, unchanged nine excluded chains and three gameplay owners, frozen source hashes, exact attributed receiver/argument conservation, and 54 deliberately wrong-receiver/texture evidence controls. These controls test conservation, not additional compiler runs.
- `PlainCubeNativeTest`: 70,597 assertions against actual Forge 2768/Minecraft 1.12.2 classes. Real native block/item registries, exact constructor-chain slices, late ItemBlock failure/success, world/item routing, real native loader/baking, two resource reloads, Mesa EGL atlas/mipmap/GL upload/readback, every original PNG animation texel, six-face original XYZ/UV/winding/material, native perspective matrices, and 512 vanilla/Forge flat/smooth-AO/asymmetric-light/neighbor-mask render scenarios. The renderer comparisons preserve native XYZ/color/light output while intentionally changing only UV0.
- `OriginalCubeFaces`: unmodified 1.7.10 client Block and RenderBlocks bytecode. Setter returns identical Block and retains the same String reference; 156 side/meta icon identities including raw int edges, 26 white item colors, white world color and normal opaque cube methods are checked. Original client SHA256: `a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce`. The field/method names are from the cached Forge1614 joined mapping, not reimplemented original rendering formulas.
- `resource-audit.json`: 27 PNGs plus animated molten metadata remain byte-identical to pristine original resources. No texture was deleted, replaced, defaulted or renamed.

## Honest fixture and visual boundaries

Native gameplay owners cannot compile yet. The runtime test therefore extracts their exact production constructors/fields/noFortune method into explicit NTM-only constructor fixtures; it does NOT claim complete owner compilation. No Minecraft or Forge class is stubbed. MainRegistry's creative-tab reference is an isolated null fixture; real gameplay/UI tab initialization is not exercised. The runtime fixture uses native standard ItemBlock objects plus exact constructor/getMetadata slices for ItemBlockBase, ItemBlockLore and ItemOreBlock, with complete real IBlockMulti; their unchanged full source establishes the inherited native visual path. ItemBlockBase/ItemBlockLore tooltip and ItemOreBlock rarity callbacks are not compiled or claimed fixed (ItemOreBlock still has an unported rarity constant). Original RenderBlocks is allocated without a world solely for its real six face methods. Atlas reset is explicitly mirrored before native loadTextureAtlas to avoid unrelated whole-client bucket bootstrap; real resources, sprites, mipmaps, stitch and GL upload are still executed.

OPEN VISUAL BOUNDARIES: native 1.12 item-camera transforms and native AO/lighting are retained and checked against the real native cube. Exact original 1.7 per-context GUI/held/dropped-item transform parity and old-versus-new lighting pixel parity are NOT certified. No launched client/server, complete gameplay, save compatibility, generic external renderer/texture-pack model redefinition or complete-mod success is claimed. Existing user approval gates remain unchanged.

## Reproduction

From the module root (all inputs cached; no downloads):

    python3 porting/tests/plain-cube-textures/run-attribution.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /tmp/nagaport-ae2-native-classpath.txt
    python3 porting/tests/plain-cube-textures/check-source.py
    python3 porting/tests/plain-cube-textures/run-native.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /tmp/nagaport-ae2-native-classpath.txt --resources /workspace/shared/nagaport/ntm-port-1.12.2/src/main/resources --legacy-jar /tmp/nagaport-legacy-client.jar

The scripts use their own module's production source. --resources may point at the unchanged main resource tree when the worktree is source-only sparse. No full-mod Gradle run belongs to this family; the coordinator owns combined compilation.

## R2: metadata-independent item lookup

The first delivery's finite metadata 0..15 model table was insufficient. Actual native ItemModelMesher.getMetadata reads ItemStack.getMetadata(), which dispatches Item.getMetadata(ItemStack) and returns raw stack damage. It does NOT use ItemBlock.getMetadata(int), the placement path that returns zero for these owners. The inherited methods are unchanged across the 24 standard ItemBlocks and the three audited NTM item subclasses.

The correction registers one inventory model at metadata zero to retain the native bake dependency, then uses ModelLoader.setCustomMeshDefinition with a constant per-item route. ModelLoader.onRegisterItems transfers the real registry-delegate bindings into genuine Forge ItemModelMesher. No custom mesher or raw-metadata normalization is substituted.

The native test exercises 27 items × 13 damage values × two reloads, covering MIN_VALUE, -32769, -32768, -1, 0, 1, 15, 16, 32767, 32768, 65535, 65536 and MAX_VALUE. Native constructors clamp negative damage to zero and mark >65535 stacks empty/AIR; that behavior is preserved. Separately, directly injected raw damage tests the actual native dispatch without changing cached emptiness. These raw-state fixtures are explicitly abnormal states, not a claim that normal constructors retain every signed int. All valid stacks and retained-owner raw states select the exact owner model, including after cache rebuild. The genuine former 0..15-only Forge mesher is a negative control and returns the missing model outside that interval. Original Block bytecode also retains the same icon/color for the raw edge values.

No camera/AO implementation or scope changed in R2. `native-r2.log` supersedes the historical `native.log` for current production.
