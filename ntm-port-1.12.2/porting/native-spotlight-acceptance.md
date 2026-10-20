# Native spotlight family

## Finite production scope

The six registered lamp definitions (incandescent, fluorescent, halogen, each on/off) and their invisible spotlight beam use native block callbacks and model registration. `RenderLight` is replaced in place by the native geometry selector, not excluded from compilation. `SpotlightBlockModelHandler` owns the six ItemBlock routes only after `ModelRegistryEvent`, together with six real blockstate JSON resources.

The nine Java files are `Spotlight`, `SpotlightModular`, `SpotlightBeam`, `BlockBeamBase`, `SpotlightModelState`, `RenderLight`, `SpotlightGeometry`, `SpotlightBakedModel`, and `SpotlightBlockModelHandler`. Existing model parser, block-position light quad and native item transform helpers are reused without changes. No model, texture or audio asset bytes are changed.

## Preserved rendering contract

- Exact original three OBJ assets and six atlas textures. Named groups use the original case-sensitive last matching group. Missing named groups emit no geometry, as before; triangles duplicate their third vertex and quads retain all four corners.
- All 16 metadata values survive native state round trips. Direction remains `metadata >> 1`, including the original UNKNOWN direction for metadata 12–15.
- Fluorescent adjacency remains exact block identity, not common lamp type or matching orientation. Original six-direction priority, ignored mounting axis, opposite-neighbor second connection and ad-hoc roll formulas are retained. Render state is an immutable six-bit neighbor snapshot rather than mutable global rendering state.
- Vertex transforms retain the original 1.7 float sine/cosine lookup table arithmetic and source float cast before translation. Roll affects vertices but not the original normal path. Inventory independently preserves the -90-degree Y rotation, 1.5 scale and native half-block recentering.
- Original world rendering explicitly disabled normal shading. Native world quads remain white, unculled, AO-disabled and diffuse-disabled, with block-position packed lighting via the already-validated native flat-pipeline quad contract. Inventory retains the independently rotated geometric normal and native diffuse lighting. The six source PNGs are opaque; the default solid block layer is retained.
- Native resource reload reparses the supplied OBJ and rebakes new sprite bindings. It does not silently substitute bundled geometry for replacement resources. Existing parser syntax limits are unchanged.

## Preserved gameplay contract

The owner migration retains placement orientation, float-precision selection bounds, no collision, no supporting face, on/off inverse-redstone scheduling, original four-tick delay, broken-state bits, recursive repair and celestial trait calls, beam length/decrement order, intersection direction bits, recursive propagation/unpropagation/back-propagation, native block replacement callbacks, original drops/pick result and structure-transform behavior. `TileEntityData` is compiled unchanged, preserving its `meta` NBT key.

`BlockBeamBase` also serves `FloodlightBeam`. The latter overrides no migrated base callback (only tile creation), so its inherited invisible/air/leaves/no-collision behavior remains reachable. Its separate tile update engine and `Floodlight` owner are not migrated or claimed complete here.

## Terminal native verification

- `porting/tests/native-spotlight-owners-FINAL.log`: **11,600 passing checks**, compiling all four complete owners plus the actual model-state helper, `TileEntityData`, and native direction/structure interfaces. Genuine native `World`/`Chunk` placement, break and neighbor dispatch, native redstone, slab variants, direction-bit intersections, cutoff/repropagation, repair and NBT are exercised. The four-tick scheduling request is recorded and its actual callback invoked; this does not claim a running server scheduler. Four external NTM collaborator classes are bounded recorders: `ModBlocks`, `CelestialBody`, `CelestialBodyTrait`, and `CBT_Lights`; generic/varargs signatures match the production trait API.
- `porting/tests/spotlight-model-native-proof.log`: **3,498,230 passing assertions, 2,208 exhaustive layouts and 837,256 ordered native vertices**, with all six owners, all 16 metadata values, and all 64 modular-neighbor masks. Pinned pristine renderer bodies supply selectors; original cached 1.7 `Vec3` bytecode independently verifies 8,232 exact double rotation results. All 20 complete production source classes/interfaces compile together, including all four actual owners, actual `TileEntityData`, native geometry/state/model/handler and their helpers. Controlled triangle/quad, exact-case duplicate-group, missing-group and missing-UV cases are tested separately from the three shipped assets.
- Both actual native flat-light pipelines verify the unshaded white world color and own-block lighting. Real `ModelRegistryEvent`, `ModelLoaderRegistry`, `ItemModelMesher`, and `BlockModelShapes` routes are tested against both a directory pack and a packaged JAR. All six PNGs decode, all three OBJ resources mutate/reload/restore/fail when missing, new sprite bindings are used, and retained baked geometry stays immutable. The final model harness uses the actual complete lamp owners, not owner substitutes, with only the same four external registry/celestial collaborators. The separate owner suite adds gameplay coverage.
- `porting/tests/spotlight-production-scope.json` freezes the exact 15 production paths and SHA-256 values. Parent-owned initializer/legacy-registration integration is recorded separately. No source in the previously delivered conveyor/workflow family changes.

## Shared tile boundary

`TileEntityData` remains unchanged. Owned beam propagation preserves an existing beam block's metadata/color and changes only its direction-bit field; actual native tests retain the tile across those operations. A deliberate external same-block metadata change still recreates this non-vanilla tile under its pre-existing native `shouldRefresh` default. That external-mutation limitation is tested and disclosed, not silently presented as old-engine parity or repaired by broadening this family into a shared tile lifecycle migration.

## Explicit boundaries

This checkpoint is not complete mod compilation, a running graphical Minecraft session, a lighting screenshot comparison, or full celestial-dimension integration. External NTM collaborators in selected native tests are explicitly identified by those tests. No Minecraft/Forge API facade, source exclusion or fake legacy renderer is introduced. Crane logistics remains a separate future family, and the other consumers of the old rotated-cube renderer remain unchanged.
