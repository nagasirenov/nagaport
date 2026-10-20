# Hazard-family native plain cubes (26 sites, CP78)

Baseline: `89b6bfc71a8b532c2fe7cdbd43bb66b7991ee365`. Work was performed in a separate sparse worktree. The original 36 sites and their evidence files are untouched.

## Finite scope and visual-owner audit

The CP78 full-source compiler reported 12,775 errors, including 558 ModBlocks diagnostics. Ranking remaining texture construction owners selected the largest single exact owner, BlockHazard (24 sites), plus its two exact BlockHotHazard construction sites. These are the 26 sites in `sites.json`, not a global texture-name substitution.

Both pristine original and current source were audited. BlockHazard extends the actual Block; it implements ITooltipProvider only. BlockHotHazard extends BlockHazard and has no declared constructor. Neither declares an icon, texture registration, color, render type/pass, opacity, bounds, tile entity, state, or model override. BlockHazard's display-effect branches produce particles; BlockHotHazard adds steam particles. Those bodies and every gameplay callback remain byte-identical to CP78. They are not replaced by a simplified renderer and are not claimed ported.

All 26 items retain their exact original registration: 21 standard ItemBlock and five ItemBlockBase (schraranium, schrabidium, schrabidate, solinium, schrabidium_fuel). ItemBlockBase's original getIconFromDamage explicitly delegates to the selected block's getIcon(1, meta), and its original getColorFromItemStack delegates to getRenderColor(stack.getItemDamage()). These are the inherited metadata-independent Block icon and white color for these exact owners, proven by original bytecode tests; the audit guards both exact delegate bodies. Current ItemBlockBase has no visual override. Its other branches concern names (including an unrelated original BlockMetalFence branch), tooltip/rarity and the IBlockMulti placement marker. Neither chosen owner implements IBlockMulti or is BlockMetalFence. Neither item version adds a custom model or glint path. Native placement metadata remains zero, while raw ItemStack metadata remains arbitrary and is handled by the existing constant ItemMeshDefinition. No selected-field inherited visual-method consumer was found in either source tree.

Excluded: all other owners, including BlockHazardFalling, BlockNuclearWaste, contextual colors, custom faces, tile renderers, pillars and unknown subclasses. BlockFalling's 15 sites and the depth family were not opportunistically included. Existing sound setter failures outside the selected family remain parked. No access transformer, fake old Forge setter, class hierarchy replacement, source exclusion or resource fallback was added.

## Production change

Exactly 26 complete ModBlocks expressions now pass the unchanged complete receiver and explicit original texture expression to PlainCubeTextures.bind. Java evaluates the receiver first, texture second, exactly once; bind returns the identical Block and stores the exact String reference. Later explicit bindings replace earlier values. No texture name is inferred from a block name.

The only other production change expands PlainCubeTextures' exact class-name allowlist by BlockHazard and BlockHotHazard. Unknown subclasses remain rejected. PlainCubeModelHandler and PlainCubeModel are unchanged: native Forge registration runs after real ItemBlocks exist, routes the one inherited block state, adds an inventory bake dependency, maps every raw damage value with the native mesh definition, and uses actual Forge ModelLoaderRegistry plus cube_all. The existing adapter retains original six-face UV direction/endpoints and perspective-wrapper identity. Resource reload owns model and sprite refresh; no new caches are introduced.

## Evidence

- `before.tsv` / `after.tsv`: independent complete-source Java8 attribution of all 4,065 production units. Before is exactly 12,775 errors and after 12,749, a reduction of 26. Every selected original receiver and native first argument is genuine net.minecraft.block.Block; every native call resolves to PlainCubeTextures.bind(Block,String). Complete receiver/texture source spans are conserved. No selected-line error remains. The mod as a whole still does not compile.
- `check-source.py`: byte-exact full ModBlocks inversion to CP78, exact helper allowlist delta, unchanged gameplay, model, item, sound and registry sources, original/current owner hierarchy checks, 52 wrong-receiver/texture negative controls, original PNG hashes and frozen evidence hashes.
- `PlainCubeNativeTest`: 66,353 assertions against real Minecraft 1.12.2 / Forge 2768, including 26 exact constructor-chain slices, original builder state and return identity, explicit texture replacement, Java argument evaluation order, null storage, unknown-owner/subclass rejection, late ItemBlock failure/success, real world/item model routing and baking, raw metadata edges, two resource reloads, Mesa EGL atlas/mipmap/upload/readback, every texel of all 26 original PNGs, six original face XYZ/UV/winding/material comparisons, native perspective preservation and 512 vanilla/Forge flat/smooth AO/light/culling scenarios.
- `OriginalCubeFaces`: executes unmodified 1.7.10 Block and RenderBlocks bytecode, checking original setter identity/string reference, white colors, normal cube methods, 156 side/meta icon identities and original face coordinates. The legacy client SHA256 is `a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce`.
- `resource-audit.json`: all 26 PNGs are byte-identical to pristine original resources. None has an animation metadata companion. Nothing was renamed, replaced or deleted.
- Prior 27-site and nine-site native suites are rerun against the expanded production helper, with outputs saved separately here. Prior source/evidence files are not rebased or overwritten.

## Honest runtime boundary

This is a headless native model/GL test, not a launched game. Complete BlockHazard and BlockHotHazard still contain unported gameplay/particle callbacks, so the harness extracts exact production fields, constructors and used builders into clearly labeled NTM-only construction fixtures. BlockHotHazard's fixture retains the original implicit constructor and actual parent. ItemBlockBase uses exact production constructor/getMetadata slices. No Minecraft or Forge class is stubbed. Complete real ModSoundType, ModSoundTypes and ModSoundEvents are compiled; unrelated MainRegistry initialization is isolated by a deterministic random field and null creative-tab fixture.

The test does not certify original-versus-native GUI/held/dropped camera transforms or lighting pixel parity; it preserves and verifies the existing native transforms/AO and original face texture mapping. It does not certify particles, beacon callbacks, radiation, full gameplay, audio samples, a launched client/server, save compatibility, external model redefinition, or complete-mod build success. These remain open boundaries, not silently flattened behavior.

## Reproduce using cached inputs only

From the module root:

    python3 porting/tests/plain-cube-hazards/run-attribution.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /tmp/nagaport-ae2-native-classpath.txt
    python3 porting/tests/plain-cube-hazards/check-source.py --original /workspace/shared/nagaport/ntm-original-pristine --resources /workspace/shared/nagaport/ntm-port-1.12.2/src/main/resources
    python3 porting/tests/plain-cube-hazards/run-native.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /tmp/nagaport-ae2-native-classpath.txt --resources /workspace/shared/nagaport/ntm-port-1.12.2/src/main/resources --legacy-jar /tmp/nagaport-legacy-client.jar

The coordinator owns combined integration/full compilation. No download, install, push or whole-mod Gradle invocation was performed for this family.
