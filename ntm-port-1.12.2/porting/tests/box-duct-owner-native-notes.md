# Native boxed duct and cable owners

## Production scope

The complete production classes `FluidDuctBox`, `PowerCableBox`, and `FluidDuctBoxExhaust` now use real Minecraft/Forge 1.12.2 blockstate, metadata, creative-list, render-layer, collision, selection, and world-position APIs. The fluid owners continue to inherit the existing native `FluidDuctBase`; its activation, fluid identification, recursive routing, and network-debug methods are not replaced.

Each owner persists all sixteen metadata values using `METADATA`. Creative fluid variants remain 0–14, cable variants 0–4, and exhaust variants 0, 3, 6, 9, 12. Drop metadata remains `% 15` for both fluid owners and `% 5` for cable. The material subcounts remain 3, 1, 1; neither the state nor drops silently normalize invalid creative variants. Full-size and undersize geometry for all sixteen persisted nibbles remains as in the original.

Extended state supplies `ConnectionState.CONNECTIONS` in the original bit layout and `FluidConnectionState.FLUID_COLOR` for fluid owners. The final tint is white except when metadata `% 3 == 2` and the source tile is a `TileEntityPipeBaseNT`; that case retains `ColorUtil.lightenColor(..., 0.25D)`, including its packed alpha bits. Exhaust normally has no `TileEntityPipeBaseNT` and stays white. The renderer/model successor owns texture selection; legacy `IIcon` fields, numeric render ID, and mutable global tint are removed rather than emulated. These owners use `CUTOUT_MIPPED` to retain original pass-zero alpha testing and mipmapped binary-alpha textures, non-full/non-opaque, with every face rendered.

Original fluid `Library.canConnectFluid` and cable `Library.canConnect` predicates are retained. Each owner snapshot preserves the original owner-side nX, pX, nY, pY, nZ, pZ call order; the old renderer's redundant independent connectivity/icon queries are no longer a shared mutable source of state. Exhaust preserves its tile-independent `SMOKE || SMOKE_LEADED || SMOKE_POISON` short-circuit order. Missing or wrong source tiles leave normal fluid/cable at zero connections with the original centered collision/selection shape; exhaust continues probing all smoke routes independently of its source tile.

Compound collision order is unchanged: core, +X, -X, +Y, -Y, +Z, -Z, with special full-axis boxes for one connection or an opposite pair. Fluid/exhaust retain wider junction cores except for two-way elbows. Merged bounds and native world offsets match the original. Pipe sounds are bound in the fluid constructor through native protected `setSoundType`; ordinary cable sound behavior is unchanged. The companion damage integration supplies `hasCustomBreakingProgress` through exact registry/class matching in `BoxDuctRenderState`; the owner probe checks registered ownership and rejects unregistered/foreign-subclass ownership.

## Reproduce

From the repository root:

```
python3 porting/tests/box-duct-owner-native-probe.py \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)" \
  --original-root /workspace/shared/nagaport/ntm-original-pristine
```

The run recorded in `porting/native-box-duct-owner-test.log` passes 1,230,245 assertions. All three pristine owner files are SHA-256 pinned; original collision and selection method bodies generate an independent executable oracle with only native AABB construction substituted. All owner routing bodies are compared against pristine after whitespace and the local direction-variable rename only.

The test compiles all three complete production owners plus the complete production `FluidDuctBase`, actual native state helpers including the exact-class/registry `BoxDuctRenderState`, `IBlockMulti`, `IBlockFluidDuct`, `IAnalyzable`, `IItemFluidIdentifier`, `ForgeDirection`, energy/fluid connector interfaces, `ModSoundType`, `ModSoundTypes`, `ModSoundEvents`, and `RefStrings`. There is no extraction or replacement of production owner methods and no Minecraft/Forge facade.

Checks cover:

- All 64 connection masks × 16 persisted metadata states for four standard fluid types, cable, and three exhaust smoke types.
- Exact ordered pristine compound boxes, merged bounds, three deterministic clipped queries per case, and world-space offset exactly once.
- Final tint, dynamic-state cleanup, metadata/drop/placement/subblocks, native model/cutout-mipped flags, non-full/opaque behavior, all rendered faces, and custom pipe sound.
- Missing/wrong source tiles; fluid mismatch; exhaust non-smoke rejection and three-smoke short-circuit sequence; target block/tile opposite-facing routing; block-first short circuit; y-boundary protection.
- Original fluid and unconditional exhaust overlay labels and colors.
- Real `World`/`Chunk` creation of each owner tile, all sixteen native persisted metadata values, six-way neighboring connectivity, neighbor removal, stable source tile, and exact resulting compound collisions.

## Honest dependency boundary

Genuine Minecraft/Forge 1.12.2 classes and the cached runtime dependencies are used directly. The complete listed NTM production classes above also compile directly. The harness controls unrelated NTM fluid definitions, network tiles, registry/item globals, player key state, identifier writes, network objects, translation and overlay capture, and MainRegistry RNG ownership. Tile fixtures implement the production connector interfaces; they do not prove the complete tile implementations or their ticking/NBT behavior.

`Library.canConnect`, `Library.canConnectFluid`, and `ColorUtil.lightenColor` are extracted unchanged from current production into narrowly isolated NTM collaborators. Their entire legacy owner utility classes are not claimed to compile. The pure color body uses real Java `java.awt.Color`; its fixture is explicitly not a production replacement. `FluidDuctBase` is compiled in full against controlled activation/network collaborators, but this owner suite does not independently re-prove all inherited activation permutations (the existing `duct-native-probe.py` suite does).

This is bounded CPU-headless native compile and behavior evidence. It is not a whole-repository Gradle build, a graphical lighting/texture/destroy-stage acceptance run, or proof that unrelated NTM collaborators have already been ported. Renderer/model/lifecycle and shared-registration integration are separate companion work.
