# Solar mirror native owner closure

## Production boundary

Only these eight files are part of this change:

- `src/main/java/com/hbm/blocks/machine/SolarMirror.java`
- `src/main/java/com/hbm/blocks/machine/SolarMirrorModelState.java`
- `src/main/java/com/hbm/tileentity/machine/TileEntitySolarMirror.java`
- `src/main/java/com/hbm/render/block/RenderMirror.java`
- `src/main/java/com/hbm/render/block/nativeblock/SolarMirrorGeometry.java`
- `src/main/java/com/hbm/render/block/nativeblock/SolarMirrorBakedModel.java`
- `src/main/java/com/hbm/main/SolarMirrorModelHandler.java`
- `src/main/resources/assets/hbm/blockstates/tile.solar_mirror.json`

Existing `AtlasObjGeometry`, `BlockPositionLightQuad`, `ObjMesh`, `ObjMeshParser`, and `SolarMirrorPosition` are reused unchanged. The mirror OBJ and atlas texture are the unmodified pristine originals.

Integration must remove `RenderingRegistry.registerBlockHandler(new RenderMirror())` from `ClientProxy` and migrate the `ModBlocks.solar_mirror` construction to native `Material.IRON` / `setTranslationKey` without `setBlockTextureName`. The event-subscribed handler owns only the exact `SolarMirror.class` at `hbm:tile.solar_mirror`; it leaves addon subclasses, foreign names, and replacement owners alone.

## Preserved behavior

- Missing or wrong tile produces no world geometry, including no base.
- Base and idle Mirror use the original last matching named-part selection and named-part shadow formula.
- Target-directed Mirror rotates X then Y about y=1, retaining the original float trigonometric lookup, integer distance arithmetic, float position casts, float UV multiplication, triangle duplication, and unrotated face-normal shade.
- Original integer square overflow can produce a NaN pitch and point-degenerate faces. The solar-only point-quad adapter retains those exact zero-area primitives instead of crashing or changing the target; nondegenerate geometry uses the unchanged shared adapter.
- Every world quad remains unculled, pre-shaded, and samples the owning block position's packed light through the shared native quad adapter.
- The inventory route is the native flat-texture `ItemLayerModel`, not the world OBJ. Original `renderInventoryBlock` was empty and `shouldRender3DInInventory` was false.
- Extended model data holds immutable primitive snapshots, never live tile/world references. Dynamic targets do not accumulate in a geometry cache.
- Reload invalidates parsed world mesh state; new baked models use the new atlas sprite.
- Tile migration changes only world/position/sky-light/render-update and AABB API calls. Heat delivery, target comparison, active-state logic, packet bytes/range/cadence, NBT keys, cached render bounds, client boiler registration, and target mutation are retained.

## Proof scope

The portable runner is `solar-mirror-native/run.py`, with `--module`, `--java-home`, `--classpath`, and `--original` arguments. It compiles the actual production owner and complete tile, with explicit fixtures only for outside-owner ticking-base/boiler collaborators. The original renderer and named-part helper method bodies are compiled into a reference-only package and executed for geometry comparison.

This is a targeted native compile and deterministic model/owner test boundary, not a full-mod build or a live-game visual/gameplay claim. Original boiler/network implementation is outside this change.

## Verified result

The finalized runner passed again independently on 2026-10-19 with JDK 8 and the Forge 1.12.2 classpath: 13 actual production sources, exactly two outside-owner collaborator fixtures, 1,058 geometry cases, 43,132 bit-exact native vertices, and 415,098 assertions across the main and two negative-registry JVMs. Original rendered method bodies, not rewritten expected-geometry formulas, provide the reference stream. Point-degenerate negative-square-root overflow and absolute-Y/relative-Y overflow branches are included.

Reproduce from any working directory:

```sh
python3 porting/tests/solar-mirror-native/run.py \
  --module /path/to/module \
  --java-home /path/to/jdk8 \
  --classpath "$(cat /path/to/native-classpath.txt)" \
  --original /path/to/pristine-original
```

The proof bundle includes original/production hashes and a documented fixture boundary. There are no owner stubs, CE inputs, main-tree edits, commits, pushes, or live-game claims in this patch.
