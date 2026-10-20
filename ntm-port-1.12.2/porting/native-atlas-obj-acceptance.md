# Native atlas-OBJ block family

## Bounded production scope

Five obsolete block renderer implementations are replaced in place by real native geometry producers: `RenderPipe`, `RenderCapacitor`, `RenderBarrel`, `RenderBattery`, and `RenderAntennaTop`. The family includes all 45 original owners: 24 decorative pipes, six capacitor tiers, six fluid barrels, seven explosive/radioactive/oil barrels, HEV battery and antenna top. No renderer class is deleted, excluded from compilation, replaced by an empty callback, or backed by recreated legacy Forge APIs.

`porting/tests/atlas-obj-production-scope.json` enumerates the finite 24 Java files, 45 new blockstates, and one sound-definition resource edit, with frozen SHA256 values. Parent-owned ClientProxy handler removals and ModBlocks initializer conversions are separate integration files. Existing asset bytes remain unchanged.

The shared renderer implements the original two distinct `ObjUtil` contracts. Whole-model and named-part shading are not conflated. Float-table Z-then-Y rotation, source-order materials, last exact-case named-group selection, uncontracted UVs, float-versus-double UV multiplication, color saturation, and byte-quantized item normals are retained. Inventory translations/scales remain separate from world transforms. World quads are unculled, non-AO, pre-shaded, and sample the block's own light through the established native quad pipeline. This is not an AO cuboid solution; diode, pneumatic and structural AO renderers are not silently flattened.

The native loader reads original OBJ assets through the resource manager and invalidates its mesh cache on reload. Real ModelRegistryEvent registration resolves ItemBlock identities only after registry construction. Every owner and persisted metadata value has an actual blockstate/model route; unsupported owner identifiers fail explicitly rather than being silently assigned another block's geometry. Existing CP22/static item and callback model owners do not overlap this family.

Original supported resource geometry remains supported: part quads emit four vertices; whole-model quads retain the historical third-vertex duplicate, continuous five-vertex-per-face stream across groups, and incomplete final primitive discard. Original HFR parsing already restricts faces to triangles/quads. Missing named groups emit nothing as before; missing asset files report the resource failure. The shipped eight meshes contain only triangles, but valid quad replacements are tested independently.

## Gameplay and lifecycle

Actual block state/callback implementations retain original collision bounds, placement metadata, pipe low-bit/axis rules, capacitor bus and energy algorithms, persistent drop/restore behavior, barrel inventory spill distributions, fluid identifier activation, comparator values, HEV armor charging, and explosive barrel branch/scheduling parameters. No shared tile base lifecycle policy is changed.

Exactly two scoped native `shouldRefresh` overrides retain the existing tile on metadata-only changes: nested capacitor tile and `TileEntityBarrel`. Different block identities still invalidate the tile, including iron/steel-to-corroded barrel replacement. Capacitor wire/NBT and existing barrel tile implementation bodies are unchanged beyond explicitly documented native API substitutions and these lifecycle insertions.

The BlockDetonatable/BlockFlammable chain is migrated only as needed for the complete barrel owners. Four unrelated descendants still have legacy neighbor callback overrides: `BlockPlasticExplosive`, `BlockTNTBase`, `DetCord`, and `ExplosiveCharge`. Their compile/runtime migration remains an explicit unresolved gate; inherited native callbacks must not be treated as proof of their intended behavior. No compatibility adapter or source exclusion hides these diagnostics.

## Sound scope

The HEV activation event `hbm:item.battery` is a real registered SoundEvent. Only its existing sample name gains the required `hbm:` namespace; the OGG bytes, sample order, streaming flag, gain and pitch are unchanged. Actual native SoundHandler load/validate confirms directory and jar lookup without a sound backend.

The fluid barrel constructor accepts an explicit native SoundType. Plastic keeps STONE. Other five fluid variants use `ModSoundTypes.legacyMetal`, preserving original stone samples at volume 1 and pitch 1.5 rather than substituting native 1.12 METAL samples. Hit/fall remain step selection. Decorative pipe construction retains the existing custom grate sound and RNG envelope ownership is unchanged.

## Evidence and limits

- `porting/native-atlas-obj-model-test.log`: 3,266,948 assertions and 722,136 complete native vertices, all 45 × 16 state/item routes in directory and jar, actual ItemModelMesher, Forge/vanilla flat lighting, all eight mesh reload/missing/restored cases, and controlled original quad-stream replacements. An isolated cached-original runtime adds 7,776 exact Vec3 rotation comparisons. See `porting/tests/atlas-obj-model-native-notes.md`.
- `porting/native-atlas-sound-test.log`: 9,359 native assertions plus 12 cached-original sound-oracle assertions. This includes the existing sound regression suite, the battery event/resource and original metal selection/pitch.
- The whole-block proof passes 2,648 checks, compiling eight concrete block classes, three native bases and the complete nested capacitor. Its exact controlled-collaborator boundaries are recorded in `porting/tests/atlas-block-native-notes.md` and `porting/native-atlas-block-test.log`.

The actual complete nested capacitor class is compiled and tested. The full TileEntityBarrel storage/fluid engine is not: only its exact approved lifecycle override is transplanted into the controlled storage collaborator for real World/Chunk lifecycle tests. Existing ItemBlockBase tooltip plumbing, GUI screens, external energy endpoints, armor eligibility and other unported services remain separate gates. These are CPU/headless native proofs, not whole-mod compilation, full game launch, audible playback, or live graphical acceptance.
