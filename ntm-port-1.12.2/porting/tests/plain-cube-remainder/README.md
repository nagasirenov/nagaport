# Remaining audited plain cubes (15 sites, CP81)

Baseline: `242867906a53eb7da03c879f476ced61c1170e14`. Separate sparse worktree. Existing plain/hazard/depth migrations are untouched.

## Bounded selection and closure

Exactly 15 compiler-attributed `setBlockTextureName` errors in ModBlocks: three BlockNuclearWaste, three BlockHazardFalling, two BlockSpeedy, two BlockNoDrop, and one each BlockNetherCoal, BlockRadResistant, FragileBrick, BlockMeteoriteTreasure and exact vanilla BlockFalling (`concrete_super_broken`). Their complete original/current NTM parent closure has no icon/tint/state/shape/render-pass/custom-renderer override. Actual original 1.7.10 BlockFalling bytecode confirms its Block-inherited opaque full cube, all-face icon and white color. Actual native BlockFalling is used, never replaced by a fixture or new subclass.

Ten use vanilla ItemBlock, four ItemBlockBlastInfo and one ItemBlockBase. The original vanilla ItemBlock icon fallback and white tint are executed against original bytecode. Original ItemBlockBase's explicit face/color delegates and ItemBlockBlastInfo's unchanged hierarchy are source-guarded. Native item constructors and placement-metadata slices are compiled with actual ItemBlock. None of these owners implements IBlockMulti.

Two original conveyor renderers consumed concrete_smooth's inherited icon. Both were already migrated before CP81: current RenderConveyorLift/Chute obtain ConveyorMaterials.concrete and unchanged ConveyorBlockModelHandler explicitly registers and loads `hbm:blocks/concrete`. These consumers are pinned unchanged, not ignored or substituted. No current direct legacy visual call remains for the selected fields.

Exclude all fourteen vanilla BlockFalling constructions still blocked by setStepSound; no sound-root workaround or change was attempted. BlockGeneric/Beaconable are already owned by LegacyBaseModelHandler and are not remigrated. BlockPorous inherits BlockStone's non-plain visual behavior and is excluded. Pillars, structures, machines, custom geometry, unknown subclasses and tile-entity owners are excluded.

## Production change

Only ModBlocks (15 expressions) and PlainCubeTextures (nine exact class-name admissions) change. Each complete original receiver and texture argument is evaluated once, in original order. The exact block identity, String reference and original resource name are retained. No inheritance, gameplay method, sound setter, item class, registration timing, resource or native model implementation changes.

PlainCubeModelHandler remains the owner of late real Block/ItemBlock registration, native singleton state mapping and constant all-raw-damage item routing. Its unchanged loader wraps native cube_all geometry with the existing original-face UV correction and native perspective transforms. ModelLoaderRegistry and the native atlas own resource reload and animation.

## Results

- Full-source Java8 *attribution*, not Gradle compilation: 4,065 units, zero parse errors, 12,471 -> 12,456 source diagnostics. ModBlocks diagnostics 517 -> 502. Exactly the 15 intended texture errors disappear, with none added and no selected-line errors remaining. Complete mod compilation is still blocked elsewhere.
- `check-source.py`: byte-exact complete CP81 ModBlocks inverse (including CRLF and every unselected expression), exact helper allowlist delta, nine owner classes plus full NTM parent closure, unchanged gameplay/sound/items/models/registry, original item delegates, compiler-resolved receiver and argument conservation, two already-migrated conveyor consumers, pristine resource bytes and frozen evidence hashes.
- `PlainCubeNativeTest`: 65,309 assertions against real Minecraft 1.12.2 / Forge 2768 with Mesa EGL/GL, 15 exact construction chains, expected hardness/resistance/sound, no premature item binding, fail-fast missing ItemBlock, actual Forge event/state/mesh/model ownership and all raw-damage edge values.
- All 15 original PNGs stitch and survive native GL upload/readback; native frames retain their exact original pixels. Nether coal's original metadata is exactly `{ "animation": { "frametime": 4 } }`: two entire sequential animation cycles are checked tick-by-tick through actual native sprite updates and GPU readback, on both resource reloads. No static replacement or animated approximation.
- All six faces are compared with original RenderBlocks bytecode for XYZ, UV, winding and material. Existing 512 native flat/smooth AO, asymmetric-light and neighbor-culling cases retain native geometry/color/lighting. Native perspective matrices and wrapper identity remain exact.
- Unchanged previous depth-family native suite passes again: 51,935 assertions against this helper expansion.

## Honest proof boundary

Headless native model/atlas/GL proof, not a launched client or server. BlockNoDrop is compiled whole and vanilla BlockFalling is the actual complete dependency class. Other NTM owners use exact production field/constructor/used-builder slices with their real parent chain; unrelated unported gameplay methods are not compiled or claimed fixed. ItemBlockBase/BlastInfo use exact constructor/placement slices; unrelated tooltip/name/rarity bodies are not claimed compiled. The unrelated creative-tab bootstrap is a null fixture. No Minecraft or Forge class is stubbed.

No falling-physics callback migration, live falling-entity render, original-versus-native item-camera/lighting pixel parity, save compatibility, external model redefinition or whole-mod success is certified. No network, download, install, push, access transformer, new reflection workaround or sound migration occurred. Existing test reflection only inspects native state or runs the historical renderer oracle.

## Reproduce from cached inputs

From the module root:

    python3 porting/tests/plain-cube-remainder/run-attribution.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /tmp/nagaport-ae2-native-classpath.txt
    python3 porting/tests/plain-cube-remainder/check-source.py --original /workspace/shared/nagaport/ntm-original-pristine --resources /workspace/shared/nagaport/ntm-port-1.12.2/src/main/resources
    python3 porting/tests/plain-cube-remainder/run-native.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /tmp/nagaport-ae2-native-classpath.txt --resources /workspace/shared/nagaport/ntm-port-1.12.2/src/main/resources --legacy-jar /tmp/nagaport-legacy-client.jar

The parent owns combined integration and full Gradle checkpoint.
