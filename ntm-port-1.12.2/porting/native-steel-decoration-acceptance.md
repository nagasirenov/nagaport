# Native wall, corner and solar mirror closure

## Frozen production and exact integration

`tests/steel-decoration-production-scope.json` pins the complete 15-path production change, unchanged dependencies/assets and two parent-owned integration files. No source exclusion, old-Forge adapter, production stub, compatibility API or global rendering simplification was introduced.

The wall/corner implementation is a focused `BlockSteelDecoration` owner, with a constructor flag identifying which of the two original registered shapes it owns. It does not migrate or alter shared `DecoBlock`: scaffold, roof and the other TESR branches remain unchanged and outside this closure. The already-native beam owner is unchanged. `SolarMirror` retains its complete actual tile source with only bounded native API substitutions; boiler mechanics remain outside this family.

Parent integration, preserving existing registry IDs/order and settings:

- `steel_wall = new BlockSteelDecoration(Material.IRON, false).setTranslationKey("steel_wall").setCreativeTab(MainRegistry.blockTab).setHardness(5.0F).setResistance(15.0F);`
- `steel_corner = new BlockSteelDecoration(Material.IRON, true).setTranslationKey("steel_corner").setCreativeTab(MainRegistry.blockTab).setHardness(15.0F).setResistance(15.0F);`
- `solar_mirror = new SolarMirror(Material.IRON).setTranslationKey("solar_mirror").setHardness(5.0F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);`
- Remove only `RenderingRegistry.registerBlockHandler(new RenderSteelWall())`, `new RenderSteelCorner()` and `new RenderMirror()` registrations from ClientProxy. Both new handlers are client-only automatic ModelRegistryEvent subscribers and require no manual registration call.

The three original block texture assignments are owned by the new model definitions. Removing their legacy texture setter does not change the material: both steel shapes use the byte-identical `steel_wall.png`; mirror uses byte-identical `solar_mirror.png` and `models/machines/solar_mirror.obj`.

## Steel preservation

All four valid orientations retain the original slab placement, collision piece ordering, selection envelope, drop behavior, no-tile behavior, placement yaw arithmetic, screwdriver cycles/notification flags and NBT structure rotation. The original world wall uses standard RenderBlocks UVs; the corner uses RenderBlocksNT's different north/east UV mapping. Those are deliberately not merged into one approximation. Inventory wall retains its unusual Z centering; inventory corner retains its separate three-piece order and two omitted end faces.

The original invalid-orientation wall behavior is stateful: its singleton retains the last bounds assigned by a valid collision/selection/render query, starting with full-block bounds. The native owner preserves this narrow behavior through a last-bounds value and immutable extended model snapshot. It neither normalizes saved metadata nor replaces invalid states with a fixed shape. Subsequent owner queries do not mutate an already captured render snapshot. Invalid corner metadata remains invisible with no collision pieces but a full selection/broad collision envelope.

Native standard cuboid AO/diffuse handling and all-face visibility are retained. Existing native ConveyorCuboidGeometry/PneumaticCuboidGeometry/ThermalModelTransforms are reused unchanged. Model ownership requires exact class, known `hbm:tile.steel_wall`/`tile.steel_corner` identity and matching shape flag. Foreign/unlisted/wrong-class/wrong-kind owners and unknown model routes are unclaimed.

## Mirror preservation

Missing/wrong tile remains entirely invisible. Base and resting Mirror use original named-part selection and shading. A target above the mirror rotates only the Mirror part, around y=1, using the original X-then-Y float-table rotation, UV multiplication, original unrotated face normal/shading and integer distance calculation. The native inventory route remains flat-texture, not OBJ.

The solar-only point-degenerate quad branch preserves the original integer-overflow/NaN rotation result rather than repairing targets or rejecting zero-area faces. It does not change the shared block-light adapter. No unbounded target cache or world/tile references escape into baked model snapshots. Exact native owner registration excludes addon subclasses/replacements. Model reload, tile sunlight/heat/sync bytes and cadence, NBT, render bounds, client beam registration and redraw behavior are covered. See `tests/solar-mirror-native-notes.md` for the precise boundary.

## Evidence and reproduction

- `tests/steel-native/proof.log`: 39,252 main assertions plus two isolated exact-owner negative assertions, 12,480 native vertices. Independently pinned pristine callbacks, actual original Minecraft RenderBlocks bytecode and pristine RenderBlocksNT methods; all 16 metadata × item/world × five prior-bound histories. Actual World/Chunk tool/placement/collision callbacks, 8,642 placement scenarios, positive/negative offsets and strict touching-only collision exclusion. Native Forge/vanilla flat/smooth rendering and damage, actual event/registry/loader/state/item mesher routing, directory/JAR resources and reload.
- `tests/solar-mirror-native-proof.log`: 415,098 assertions over three JVMs, 1,058 independent pristine-renderer geometry cases and 43,132 bit-exact vertices. Complete actual owner/tile/geometry/loader classes; two explicitly bounded outside-owner NTM collaborators.
- Portable runners: `tests/steel-native/run.py` and `tests/solar-mirror-native/run.py`. Both take `--module`, `--java-home`, `--classpath`; steel additionally requires `--legacy-client` and optionally `--original`, while solar requires `--original`. Supplied runtimes and classpaths are never downloaded. Each bundle documents its exact fixture and oracle boundaries.

These are complete targeted native compile and deterministic headless proofs, not full-project compile success, live client/gameplay acceptance, audio playback or live graphics. The parent owns the integrated full compile and package proof. No staging, commits, pushes or main-tree edits were performed by this family.
