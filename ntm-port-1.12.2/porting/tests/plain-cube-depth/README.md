# Depth-family native plain cubes (15 sites, CP79)

Baseline: `77b595c32b46e2f9309bcf9924d654ddc29a3c95`. Separate sparse worktree; the existing 36 plain cubes and 26 hazard cubes are not changed or re-migrated.

## Audited finite scope

Exactly seven BlockDepth and eight BlockDepthOre construction calls in ModBlocks. Both pristine original and current BlockDepth extend Block and implement only ITooltipProvider. BlockDepthOre extends BlockDepth with its original implicit constructor. Neither owner adds an icon, tint, render pass/type, opacity, bounds, tile entity, block state or model override. Original/current ClientProxy has no selected-owner/field renderer registration. The original inherited Block icon is identical on all six faces and all raw metadata values, with white block/item color.

All 15 registrations use ItemBlockBase. Its original getIconFromDamage explicitly delegates to block.getIcon(1, meta), and its original item-color method delegates to block.getRenderColor(raw stack damage). Both exact delegate bodies are source-guarded and the inherited original Block results are executed against unmodified 1.7.10 bytecode. Current ItemBlockBase has no visual override. Neither depth owner implements IBlockMulti or is BlockMetalFence, so their placement metadata remains zero and their names do not take those special branches.

The depth constructor, mining-tool restriction, ore drops/fortune and tooltip callbacks remain byte-identical to CP79. The constructor still sets unbreakable hardness and resistance 10; depth_dnt retains its explicit later resistance 60,000 override. No gameplay callback is claimed fixed. Adjacent BlockKeyhole explicitly has different top/bottom and side icons and is excluded. Falling blocks, hazardous falling blocks, custom renderer owners and unknown subclasses are excluded; no attempt is made to make them look like plain cubes.

## Native change and preserved behavior

Only two production files change: 15 complete expressions in ModBlocks and two exact class-name admissions in PlainCubeTextures. Each bind receives the complete original receiver first and unchanged explicit texture argument second, evaluates each once, returns the identical Block and stores the exact String reference. Later explicit texture overrides replace earlier bindings. Unknown owners/subclasses remain rejected.

PlainCubeModelHandler and PlainCubeModel are unchanged. The actual Forge ModelRegistryEvent runs after real Block/ItemBlock registration, rejects missing ItemBlocks, maps the inherited singleton block state, registers an inventory bake dependency and uses the native constant ItemMeshDefinition for all raw stack-damage values. The actual Forge model loader/bake path uses native cube_all with the original texture, existing original-face UV correction and unchanged native perspective matrices. Native model cache invalidation and fresh atlas sprites supply resource reload. There is no new sprite/model cache or fake legacy texture setter.

## Evidence

- Complete-source Java8 attribution covers 4,065 units: CP79 before has exactly 12,686 errors, after 12,671. ModBlocks drops from 532 to 517, with exactly the 15 intended texture-call diagnostics removed and none added. All original receivers and native first arguments are genuine net.minecraft.block.Block. The complete mod still does not compile.
- `check-source.py` proves full ModBlocks byte-exact inversion to CP79, the exact two-owner allowlist delta, unchanged gameplay/items/model/sound/registry code, original/current hierarchy, original ItemBlockBase delegate bodies, 30 negative receiver/texture controls, frozen evidence and pristine PNG bytes.
- `PlainCubeNativeTest` passes 51,935 assertions against actual Minecraft 1.12.2 / Forge 2768: 15 exact constructor chains, unbreakable hardness, explicit resistance override, inherited sound identity, argument evaluation order, String/receiver identity, explicit rebinding, null storage, unknown-owner/subclass rejection, late ItemBlock registration, genuine state/mesh routing and native baking, all 15 original PNG texels through Mesa EGL mipmap/GL upload/readback, two reloads and raw damage edge values.
- The six faces of every selected model are compared with unmodified original RenderBlocks bytecode for XYZ, UV, winding and material. Native perspective matrices and wrapper identity are checked. Existing 512 vanilla/Forge flat/smooth AO, asymmetric-light and neighbor-mask scenarios retain native geometry/color/lighting output.
- `resource-audit.json`: all 15 PNGs exactly match pristine original resources; no animation metadata companion exists. No resource is renamed, defaulted, replaced or deleted.
- The previous 26-hazard native suite is rerun unchanged against this expanded helper and passes 66,353 assertions. Its files and all earlier evidence remain untouched.

## Honest proof boundary

This is headless native model/GL proof, not a launched game. BlockDepth's old mining signature and BlockDepthOre's old drop signature still require separate gameplay ports. The test extracts the exact BlockDepth constructor into an explicit NTM-only fixture and preserves BlockDepthOre's implicit constructor and actual parent. ItemBlockBase uses exact production constructor/getMetadata slices; unrelated tooltip/rarity/name callbacks are not claimed compiled. No Minecraft or Forge type is stubbed. Only MainRegistry's unrelated creative-tab bootstrap is represented by a null fixture.

No original-versus-native GUI/held/dropped camera transform or lighting pixel parity is certified. Existing native transform/AO behavior and original face texture coordinates are preserved. No launched client/server, full gameplay, save compatibility, external model redefinition, whole-mod success or permission change is claimed. No download, install, push, sound access transformer or sound reflection workaround was used.

## Reproduce using cached inputs

From the module root:

    python3 porting/tests/plain-cube-depth/run-attribution.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /tmp/nagaport-ae2-native-classpath.txt
    python3 porting/tests/plain-cube-depth/check-source.py --original /workspace/shared/nagaport/ntm-original-pristine --resources /workspace/shared/nagaport/ntm-port-1.12.2/src/main/resources
    python3 porting/tests/plain-cube-depth/run-native.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /tmp/nagaport-ae2-native-classpath.txt --resources /workspace/shared/nagaport/ntm-port-1.12.2/src/main/resources --legacy-jar /tmp/nagaport-legacy-client.jar

The coordinator owns combined integration and the full Gradle checkpoint.
