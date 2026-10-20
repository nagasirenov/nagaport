# Bounded full-cube consumer engine audit

Repository examined read-only at CP69 `91da806c9ad17b279ef5142d83d4fe60365d563f`. No repository changes, installs or network activity. The proof is independent of unfinished full-mod compilation. The parent task owns source selection and production/runtime fixture tests.

## Confirmed identity

The original signed 1.7.10 client declares `aji.d():boolean`. The cached original `joined.srg` maps it to `Block.func_149686_d()`, and Forge1614's bundled `conf/methods.csv` calls that `renderAsNormalBlock`. Native 1.12.2 `mcp-srg.srg` maps `Block.isFullCube(IBlockState)` to the same SRG method with the state parameter. The original jar, mapping artifacts, Forge userdev artifact and six actual native classes are SHA256-bound in `engine-audit.json`. Thirty-five complete relevant method bodies and the original Forge patches are included.

## Proven relationships, not a blanket rename

- Original Block construction calls opacity `aji.c()` twice, caching `q` and light opacity `r`; it never calls `aji.d()`.
- Native Block construction queries the default state's `isOpaqueCube` once, caching `fullBlock` and `lightOpacity`; it never calls `isFullCube`.
- Native state `isFullCube()` dispatches directly and virtually to its owning Block with the state on every call. No full-cube value is cached in either state constructor or BlockStateBase. `isFullBlock` remains a different, opacity-derived cached value.
- Original `aji.q()` (`isBlockNormalCube`) is material.blocksMovement AND `d()`. Native `isBlockNormalCube(state)` is state.material.blocksMovement AND state.isFullCube.
- Original `aji.r()` (`isNormalCube`) is material.isOpaque AND `d()` AND NOT canProvidePower. Native `isNormalCube(state)` retains this conjunction with state callbacks. Neither uses block `isOpaqueCube` as the material predicate. The Forge native world overload dispatches to state.isNormalCube.
- Native base `isTopSolid` is material.isOpaque AND isFullCube. The original vanilla World's `ahb.a(IBlockAccess,int,int,int)` (`doesBlockHaveSolidTopSurface`) begins with that conjunction, then has stairs/slab/hopper/snow exceptions. Forge1614 replaces its body with `block.isSideSolid(...,UP)`. Consequently signed-vanilla top-surface bytecode alone is not a Forge top-surface oracle.
- Old entity inside-block/suffocation test `sa.aa()` samples neighboring blocks and calls `aji.r()`. Native Entity calls state.causesSuffocation, whose base Block implementation uses material.blocksMovement AND defaultState.isFullCube, without the power exclusion. Constant-false callbacks make these branches false, but arbitrary callbacks/materials are not generally equivalent across versions.
- Base silk harvesting also consumes the callback. Original vanilla `aji.E()` is `d() && !hasTileEntityFlag`; Forge1614 makes its tile query metadata-aware. Native uses defaultState.isFullCube AND NOT state-sensitive hasTileEntity. Constant false short-circuits both tile queries, but this proves no whole drop pipeline behavior.

## Culling and connections remain separate

- Old base side culling uses bounds and the neighbor's opacity callback, not `d()`.
- Native base side culling uses bounds and neighbor.doesSideBlockRendering, whose default delegates to isOpaqueCube. Native base getBlockFaceShape always returns SOLID, regardless of isFullCube. A false full-cube callback neither repairs the face shape nor changes collision geometry.
- Original vanilla fence `akz.e(IBlockAccess,int,int,int)` accepts the same fence identity, the one vanilla fence gate, or opaque-material/renderAsNormal/non-gourd blocks.
- Native BlockFence's direct predicate uses neighbor face shape, exception identities, material matching and BlockFenceGate type. Its normal neighbor path also ORs neighbor.canBeConnectedTo. None of these direct connection decisions tests isFullCube.
- Original vanilla pane `aoa.a(Block)` accepts the neighbor's constructor-cached opacity getter `aji.j()`, pane/self and certain glass identities. Forge1614 adds an OR of world.isSideSolid for actual pane connections.
- Native pane connections use SOLID/MIDDLE_POLE_THIN face shapes, exception identities and the neighbor hook. They do not directly test isFullCube. Thus old/native pane parity is not implied by the callback migration.

`BlockMetalFence` is already a deliberately repaired domain owner. Its source must retain the custom-gate/same-owner/oak-gate/opaque-material/full-cube/non-gourd predicate and its native bridge methods. Its `getActualState` and collision paths must not be replaced by inherited vanilla connection logic. The prior `fence-palette-native` proof covers this owner and its custom collision height; its result must not be described as behavior of every NTM fence.

## Defensible selected-family limits

This engine audit supports only state-ignoring, side-effect-free constant-false method bodies, with separately established original provenance and attributed Block ownership. A source closure must show which named descendants inherit or override each callback, retain all existing native overloads/bridges, and reject stateful bodies, receiver-call rewrites or unproved hierarchy branches. The callback alone can be compared through real Block/state consumers across valid states and inherited subclasses, while null/foreign-state checks demonstrate only that the selected body ignores its argument.

It does not certify renderer choice, shape/collision migration, model quads, AO/pixels, whole-owner compilation, world lifecycle, dynamic/stated callbacks, arbitrary external mods or general fence/pane parity. Existing face-shape gaps must be exposed as preexisting limits, not hidden by changing unrelated geometry in this batch.

## Reproduce

Run `check-engine.py` with these arguments, omitting `--record` to validate the pinned report:

- `--java-home /workspace/shared/nagaport-toolchain/java8`
- `--classpath-file /tmp/nagaport-ae2-native-classpath.txt`
- `--legacy-jar /tmp/nagaport-legacy-client.jar`
- `--legacy-srg /tmp/nagaport1710-srg.zip`
- `--legacy-userdev /tmp/nagaport-forge1614-userdev.jar`
- `--native-mcp-srg /workspace/shared/nagaport-toolchain/gradle-user-home/caches/minecraft/de/oceanlabs/mcp/mcp_stable/39/1.12.2/srgs/mcp-srg.srg`

The retained `engine.log` is a successful fresh validation against the recorded report. The audit embeds the selected `javap` method bodies; the script independently regenerates all audited methods from the supplied real artifacts.
