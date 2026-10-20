# Constant-false native full-cube callbacks

Exact base: `91da806c9ad17b279ef5142d83d4fe60365d563f` (CP69). This is an isolated, signature-only batch, not a change to CP70. Main was never edited.

## Production boundary

85 `renderAsNormalBlock()` declarations become `isFullCube(net.minecraft.block.state.IBlockState state)`. Every body remains exactly `return false;`, including original whitespace and line endings. No import, annotation, constructor, collision, opacity, face-shape, registry, name, resource, rendering implementation or other byte is changed. All 85 original/pristine declarations have the same constant-false body. The original callback maps to `aji.d()` / `func_149686_d`; the native callback is genuine `Block.isFullCube(IBlockState)`, not an old API shim.

The census contains 93 legacy declarations. All eight BlockDummyable/RBMK bridge-family declarations are excluded: BlockDummyable, MachineCyclotron, MachineGasDock, MachineIGenerator, MachineRocketAssembly, RBMKBase, RBMKConsole, RBMKRod. RBMKBase's true return and the existing native-to-NTM virtual bridge remain intact. Existing independent native full-cube overrides are untouched.

Whole-source javac attribution parses all 4,064 Java units, with no parse errors. The 85 selected declarations and their 19 named descendants form a fully resolved 104-owner closure. Their source ancestry reaches only genuine Block or BlockContainer; no custom native ancestor override or descendant callback override is bypassed. All 552 anonymous classes are checked; none extends this closure. All 19 full-cube/render-normal call records remain identical, including unresolved unrelated legacy calls. No default-state arguments were invented for those remaining calls.

Diagnostics: 14,742 before, 14,662 after. Exactly 80 override errors disappear, no new diagnostics or other message deltas. Five old methods lacked an override diagnostic but still needed native dispatch. Native overriding declarations rise 57 to 142 among 150 total relevant methods. This is full-source attribution, NOT a successful full compile or runnable mod.

## Semantic boundary and engine proof

Original render-normal and native full-cube are live virtual calls. Neither constructor uses them to populate the fullBlock/lightOpacity caches; those depend on opacity. All selected callbacks are constant, field-free, side-effect-free, state-independent and cannot throw. Both original/native normal-cube consumers therefore retain false, including through native state dispatch. Native top-solid, suffocation and silk eligibility also consume this property, but their general algorithms are NOT asserted identical to every original/Forge consumer.

Opacity and full-cube are distinct. Collision boxes and face shape are also distinct. This batch changes no collision or face-shape policy and does not interpret a false result as an empty collision box. Native vanilla fence/pane connections use face shape and additional hooks, not a universal full-cube test. Consequently, this callback migration does NOT establish original foreign fence/pane connectivity. That separate migration gap remains explicit; adding a guessed `UNDEFINED` face shape would affect other support/attachment contracts and is not done here. In particular, native default face shape remains SOLID in the bounded default-method controls even when full-cube is false.

The existing complete `BlockMetalFence` is compiled unchanged and exercised as a real consumer: its original same-owner/custom-gate/oak-gate/opaque-full-non-gourd domain predicate sees the corrected false result. It is NOT replaced by vanilla BlockFence behavior. Original signed vanilla fence bytecode and runtime confirm the ordinary false branch. Previous full fence collision/connection-mask proof is retained in `../fence-palette-native`; this batch does not rerun or relabel those wider checks.

`engine-audit.json` and `check-engine.py` pin native/original class hashes, mappings, selected bytecode, and original Forge patches. Original vanilla World top-solid has special cases, while Forge replaces it with side-solid dispatch. No universal support, attachment, culling, AO, pixel, or gameplay equivalence is claimed.

## Executed native/original fixtures

`run-native.py` compiles all 85 exact selected callback bodies and their unchanged exact opacity methods against genuine Forge/Minecraft. Generic state fixtures provide metadata0..15 and a Boolean property, plus inherited anonymous controls. These are bounded callback fixtures, not complete 85 production classes. All valid states, null/foreign-state arguments, live dispatch, opacity/cache separation and default collision/face-shape independence are checked.

Five exact actual constructor slices cover BlockWaterPlant, BlockPlasma, BlockVacuum, WasteLeaves and TurretSentry. TurretSentry's mandatory unrelated tile factory throws if reached. Both custom state factories/properties in the 111-source ancestor/descendant closure (BlockChargeBase and BlockNTMFlower) are extracted unchanged. Their unrelated methods are not compiled or certified; notably legacy collision overrides still require separate migration.

The complete current BlockMetalFence and IBlockMulti compile as-is. A bounded ModBlocks fixture supplies only the existing custom-gate identity field, not production registration or the custom gate implementation. A strict real IBlockAccess implementation supplies state; unexercised queries throw. Native vanilla fence/pane and side-culling controls confirm that changing only full-cube does not silently change their distinct shape/opacity paths.

An original Block fixture compiles against the signed 1.7 client and exercises real original render-normal, normal-cube, silk and fence callers. Only test class names are relocated to avoid signed default-package collisions; original classes are unmodified. The runtime proof is 66,110 assertions. Three independently compiled runtime mutants (old callback signature, true body, state-dependent body) are rejected. None is counted as killed merely for a compilation error. There is no platform substitute, downloaded dependency, or game launch.

## Reproduce and compose

From the module directory, with the already cached inputs or equivalent pinned inputs:

    python3 porting/tests/native-fullcube/check-source.py
    python3 porting/tests/native-fullcube/check-replay.py
    python3 porting/tests/native-fullcube/run-attribution.py --java-home JAVA8 --classpath-file NATIVE_CLASSPATH
    python3 porting/tests/native-fullcube/run-native.py --java-home JAVA8 --classpath-file NATIVE_CLASSPATH --legacy-jar ORIGINAL_CLIENT
    python3 porting/tests/native-fullcube/check-mutants.py --java-home JAVA8 --classpath-file NATIVE_CLASSPATH --legacy-jar ORIGINAL_CLIENT

Engine proof additionally takes `--legacy-srg ORIGINAL_PACKAGED_SRG --legacy-userdev ORIGINAL_FORGE_USERDEV --native-mcp-srg NATIVE_MCP_SRG`. See `engine-report.md` for exact cached paths and its standalone command. No original jar is distributed.

Source and attribution scripts require the exact base Git object and deliberately reject later composed full-file changes. They must not silently rebase the proof. `apply-fragments.py --root TARGET_MODULE` dry-runs the 85 exact methods without Git. Add `--apply --receipt RECEIPT.json` to write, or `--reverse` for the exact inverse. The helper prevalidates every fragment before writing, is idempotent, and preserves independent callback edits. `check-replay.py` verifies forward/reapply/inverse and independent-edit composition. Do not use a whole-file replacement to integrate overlapping callbacks.
