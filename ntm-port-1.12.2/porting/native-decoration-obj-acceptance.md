# Native decoration OBJ family acceptance

## Finite closure

This batch replaces three legacy renderer classes with native baked-quad producers and completes seven concrete owners:

- `RenderSteelBeam`: `steel_beam` now uses focused `BlockSteelBeam`.
- `RenderConserve`: both `crate_can` and the previously shared `crate_supply` renderer consumer.
- `RenderPribris`: ordinary, burning, radiating and digamma debris.

`RenderBlockDecoModel` was audited but deferred: its computer owner inherits legacy `BlockEnumMulti`, while `BlockDecoContainer` also inherits the shared state/placement/bounds base and overrides rendering to its separate TESR route. It is not silently included in this family. The original `DecoBlock` serves wall/corner AO and other TESR owners and remains byte-identical. Its old beam render-ID declaration/branch is now unreachable from the registered beam owner, but the rest of that legacy class remains an explicit porting gate. No source is excluded or replaced by a legacy API shim.

The exact production freeze is `tests/decoration-obj-production-scope.json`: 25 current production paths, five retired case-only audio names, and parent-owned integration paths listed separately. The scope does not include unrelated concurrent changes.

## Geometry, registration and resources

The existing `AtlasObjGeometry`, `AtlasObjBakedModel`, `BlockPositionLightQuad` and transform helpers are unchanged. Three whole-OBJ producers retain original group/face order, float casts, UV mapping, normal packing, original whole-model shade formula, per-position world light and separate inventory shading. There is no new AO approximation. All world quads remain unculled, AO-disabled and diffuse-disabled; item geometry retains its normals and the existing native block camera transforms.

Original inventory transforms translate Y by -0.5, with no scale or rotation. The native model's block-centered coordinates therefore match the original world origin at (0.5, 0, 0.5). The three original assets contain 120 triangles: beam 12, conserve crate 60 and debris 48. All three meshes and six textures remain byte-identical to pristine. Resource-replacement triangle/quad behavior uses the unchanged, separately tested CP29 whole-geometry stream contract.

`DecorationObjBlockModelHandler` is an automatic client-only ModelRegistryEvent subscriber. It registers its native custom loader before model loading and obtains ItemBlock identities only after registry flush. Seven blockstate JSONs expose all 16 metadata values through actual native loader routes. Items also register all 16 metadata routes. Every owner has its original texture and particle material; debris decay metadata intentionally does not change mesh/material. Unknown owner or malformed route is not silently replaced with another geometry. The loader clears mesh caches on resource reload. Existing static/callback/custom item families do not claim these seven owners.

## Complete-owner behavior

All seven complete concrete block classes compile against actual cached Minecraft/Forge 1.12.2. No native facade classes are used. The focused beam keeps original placement metadata, drops, bounds, no-tile behavior, screwdriver rejection and NBT structure rotation without changing any other DecoBlock owner.

Both complete crate classes and the complete nested supply tile are compiled and exercised. Can selection/order/count and RNG consumption are preserved, with the exact original food enum. Supply contents, sparse slot amount/index encoding, removal/drop guards, placement restore and tile NBT remain intact. Native ItemStack.EMPTY replaces absent loaded items; the original nullable public list's player-removal path continues accepting null holes. The approved nested shouldRefresh override preserves the same tile and inventory through metadata changes but invalidates on actual block replacement. Both native activation hands are supported, without changing original main-hand behavior. The crates retain WOOD sounds.

The four complete debris classes retain pristine gameplay bodies modulo enumerated native API substitutions. Scheduled timing, state transitions, six-face order, random calls, fire sound envelope, particle NBT/coordinates/target, radiation resistance ray and inverse-square calculation, damage thresholds and main-hand marshmallow mutation are covered. No replacement radiation engine or invented drop behavior was added.

## Sound dependency

The native `BLOCK_CRATE_BREAK` event retains original `hbm:block.crateBreak` lookup under native lowercase canonicalization. Exactly five original OGG files have byte-identical lowercase case-only renames, listed in `tests/decoration-crate-sound-renames.json`. The move used temporary names, not Windows-incompatible aliases. Only the five sample strings in that original JSON entry gain `hbm:` prefixes; ordering, category, weights, gain and pitch are unchanged. No other source references those raw sample names. Actual SoundHandler validation and directory/JAR lookup retain all five samples. The packaging helper must include these five case-only renames; Windows execution itself is not claimed.

## Terminal evidence

- `native-decoration-obj-model-test.log`: **385,021 assertions**, **86,016 complete native vertices**. All seven owners ×16 world/item routes in directory and JAR resource packs, actual native ModelLoaderRegistry/ItemModelMesher, original ordered geometry/UV/color/normal/material output, Forge plus vanilla block-position lighting, and three changed/missing/restored mesh reloads.
- `tests/crate-blocks-native-proof.log`: **10,573 checks**, complete can/supply crates, nested tile, beam and unchanged tool/structure interfaces. Real World/Chunk metadata and replacement lifecycle, inventory/NBT/drop/activation and placement/bounds checks.
- `rbmk-debris-native-probe.log`: **74,997 checks**, including **6,720 seeded tick scenarios**, all four complete owners, six-direction propagation and exact radiation/main-hand cases. Full source-body substitution proof pins pristine gameplay bodies.
- `native-decoration-sound-test.log`: **9,466 native assertions**, plus **12 cached original-runtime sound checks**. The existing sound regression suite is extended only for the eighth event and five required samples.

Reproduce with each probe's `--java-home` and `--classpath` arguments. Local cached values are `/workspace/shared/nagaport-toolchain/java8` and the contents of `/workspace/shared/nagaport-tick-preparation/classpath`; these are environment-specific, not packaged dependency paths. The model probe also accepts `--original-root`. The sound runtime oracle optionally takes `--legacy-jar`; it never downloads software.

## Boundaries

Headless native proof is not a launched Minecraft client, full-project compile, audio playback or live graphical acceptance. Model tests compile the complete model/loader/parser/helper sources but use controlled owner registration collaborators; separate owner probes compile all actual owner source classes. External NTM registries, food effects, tool-recipe matching, packet transport/effect dispatch and contamination sink remain controlled collaborators with call values/order checked, not accepted full-engine implementations. The parent independently owns the integrated frozen full compile, source guards and package helper proof. No staging, commits or remote push were performed by this family implementation.
