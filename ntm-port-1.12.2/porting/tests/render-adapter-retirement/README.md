# Retire unused legacy RenderBlocks implementations

Baseline: CP49 `caf22cf027f33f9d0d0be366454c2f54fdba7915`.

## Production change

Only these four implementations are deleted:

- `src/main/java/com/hbm/render/block/RenderBlocksFixed.java`
- `src/main/java/com/hbm/render/util/RenderBlocksNT.java`
- `src/main/java/com/hbm/render/util/RenderBlocksWrapper.java`
- `src/main/java/com/hbm/render/util/IconUtil.java`

There are no replacement Java tombstones, placeholders, compatibility APIs, Gradle exclusions, runtime registration changes, resource changes, or changes to gameplay owners. The two remaining mentions of `RenderBlocksNT` in production are explanatory comments in `RenderSteelCorner` and `UniversalCuboidGeometry`.

The original four source files remain byte-identical in the existing `ntm-original-pristine` tree. The existing `mods1710/HBM-NTM-.1.0.27_X5645_H261.jar` remains byte-identical and still contains all four original classes. The manifest pins both preservation boundaries, the exact CP49 pre-deletion hashes, all eight original consumers, and 27 unchanged native successor sources. Original source-oracle inputs and immutable earlier source hashes are not edited.

## Exact consumer-to-native audit

The complete pristine Java census has zero non-self consumers of `RenderBlocksFixed`, `RenderBlocksWrapper` and `IconUtil`. `RenderBlocksNT` has exactly eight consumer files:

1. `blocks/generic/BlockBarrier.java`: its original inventory/world callbacks are now `getRenderBoxes(IBlockState, boolean)` plus native metadata, adjacency, collision and placement callbacks. `UniversalBlockModelHandler` selects `UniversalModelFactory`, `RenderISBRHUniversal.getQuads` and `UniversalCuboidGeometry`.
2. `blocks/generic/BlockSandbags.java`: the same native universal route retains original inventory/world bounds and boundary-face culling, not an empty replacement.
3. `blocks/generic/BlockWoodStructure.java`: the same native route retains all three variants, neighbor-driven boxes and original duplicate missing-brace draws.
4. `blocks/generic/BlockRebar.java`: the universal route retains twelve inventory rods and detailed/simple world rods. Its separate render-last concrete path now calls `UniversalCuboidGeometry.bake` through `concreteModel` and the actual native block model renderer; the selection path uses native buffer vertices. The server owner/tile/network is not removed.
5. `blocks/machine/rbmk/RBMKMiniPanelBase.java`: the universal route retains panel cuboids, native state/placement, three existing concrete panel owners and same-owner tile retention.
6. `render/block/RenderPneumoTube.java`: complete native `bake` emits real `BakedQuad`s using `PneumaticCuboidGeometry`; `PneumaticBakedModel` and `PneumaticModelHandler` bind the unchanged exposed-tube identity and its complete native owner. Tube center/arms/ports, rotations and world NORTH/EAST UV correction are retained.
7. `render/block/RenderBoxDuct.java`: complete native `bake` retains original branch order, box bounds, material rules, UV rotations and world tint. `BoxDuctBakedModel`, `BoxDuctMaterials`, `BoxDuctModelHandler`, three native duct/cable owners and `BoxDuctBreakingRenderer` bind world/item/damage routes. The original world NORTH/EAST correction is supplied by `PneumaticCuboidGeometry`.
8. `render/block/RenderSteelCorner.java`: complete native `bake` uses `PneumaticCuboidGeometry` for original world NORTH/EAST UVs and the existing inventory face helper for the original two omitted inventory end faces. `SteelDecorationBakedModel`, `SteelDecorationModelHandler` and `BlockSteelDecoration` retain the owner/model route.

Paths above are relative to `src/main/java/com/hbm/`. The universal path preserves the original world-only NORTH/EAST global mirror correction rather than incorrectly mirroring within a partial box. The five universal owners are not removed or redirected to vanilla cubes.

## Existing native replacement evidence

These tests already exercise complete native producers/owners or state their precise controlled collaborator boundary. This deletion does not repeat their large geometry suites or expand their acceptance claims:

- Universal owners: `../native-universal-blocks-notes.md`, `../NativeUniversalBlocksTest.java`, `../universal-original-geometry.json`, and `../../continuation-2026-10-19/universal-native-FINAL.log`. Original callback recordings and actual cached original renderer face bytecode plus both pristine NT face overrides cover all five owners, metadata, adjacency, world/item UVs and item transforms.
- Rebar's separate concrete/selection and owner paths: `../native-rebar-notes.md`, `../native-rebar-proof.log`, `../native-rebar-source-parity.py`. The source comparison remains runnable without compiling the full mod.
- Exposed tube: `../pneumatic-native-notes.md`, `../pneumatic-proof.log`, `../probe-native-pneumatic.py`, and its pristine source manifest. The independently recorded original renderer/ports and analytic NT NORTH/EAST checks remain intact.
- Box duct: `../box-duct-native-notes.md`, `../box-duct-native-geometry/probe.py`, `../../native-box-duct-geometry-test.log`, and `../../box-duct-model-native.log`. These compare actual original callback/icon/NT-face bodies and native registry/model/damage routes. The known original/native Forge diffuse rounding difference remains explicitly unaccepted; adapter retirement does not resolve or conceal it.
- Steel corner: `../steel-native/README.md`, `../steel-native/proof.log`, and the retained `pristine-steel-sources.zip`. Both original NT overrides remain in the independent oracle. Native owner, geometry, world/item routes, placement and damage behavior remain covered within that proof's stated scope.

## Reflection, resources and configuration

The audit scans every current `src/main` file, preserving Java string/character literals while ignoring only Java comments. It rejects the retired simple names in live identifiers, class-name strings, binary-name strings and shipped resources, including service/config files and UTF-16 text. Three root build/config files are also scanned. There is no source exclusion or runtime service/config reference to any retired class.

The source's live reflective class-loading sites were also reviewed. Fixed names target Fisk's heroes, Thermos and EndlessIDs. `ShadyUtil.test` decodes its debug-only signature to `mcheli.weapon.MCH_EntityBaseBullet`, not a renderer. `DamageResistanceHandler.deserialize` accepts configured entity-resistance class names; no shipped resource/config names these renderers and none of the four is an entity. There are no production `loadClass(...)` or `ServiceLoader` references. This is a closed shipped-source/resource audit, not a claim that arbitrary external mods or user-written reflection can never name a removed implementation.

## Reproduction and historical guards

From the module root, using only Python and included original evidence:

    python3 porting/tests/render-adapter-retirement/audit.py
    python3 porting/tests/check-thermal-family-source.py

Optional explicit locations: `audit.py --module /path/to/ntm-port-1.12.2 --pristine /path/to/ntm-original-pristine`. The original binary archive remains at the documented repository-relative path. No network, Gradle, downloaded dependencies or Git object is needed for either audit.

`proof.log` records the retirement audit, including 37 negative reference/tombstone fixtures. `historical-source-proof.log` records a successful complete supported historical chain on the worker’s original three-helper deletion-only tree; the coordinator records the final four-helper combined guard in the checkpoint continuation. The normal bulk audit compares its owned/excluded discovery and counters, not its ancient strict untouched-file census. The removed files do not affect that discovery. Therefore no historical guard rewrite or weakening is needed. The coordinator's next checkpoint boundary should record the four exact deletions normally in `deleted_files`; CP49 bytes/hashes are recorded here and remain available in the baseline commit.

Ancient standalone strict snapshot modes still describe their own historical complete file sets; changing their immutable snapshots to fit the current tree would be incorrect. No such change is part of this patch.

This is a source reachability and retirement audit. The coordinator owns the next whole-mod compile. Full client/server startup, original/native lighting equivalence, gameplay, world-save and external-mod compatibility are not newly certified here.

Coordinator extension: IconUtil is also self-only in the entire pristine/current source census and absent from resource/config/reflection-name references. Its two public texture lookup helpers were never called by scoped original content. Retiring it avoids inventing a generic native texture fallback or preserving a dead RenderBlocks dependency. This is not a guarantee for unknown external binaries invoking obsolete1.7 APIs.
