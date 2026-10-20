# Original RBMK static renderer audit

## Provenance and scope

Audited the unchanged `ntm-original-pristine` files beside the CP46 worktree, at enclosing repository commit `641ba083c276c6ac75afa6a8f996cdd86f8b16d4`. `source-manifest.json` pins every source, OBJ, PNG and license byte used by this audit/oracle. `pristine-rbmk-sources.zip` is a deterministic, byte-identical source bundle; it includes the original `LICENSE` (GPL v3) and `LICENSE.LESSER` (LGPL v3), without changing the upstream licensing terms. It contains no Minecraft client bytecode. The independently supplied original 1.7.10 client must SHA-256 to `a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce`; the generator checks it. No download or native production source contributes to the oracle.

Key SHA-256 pins:

- `RenderRBMKRod.java`: `9cf1fd6aaf4818eb3df018f159f20e324c8c9db2e7e23c2ddb6541600380c86f`
- `RenderRBMKControl.java`: `92537c6e0c3b359d19c69346b4377c7362d9c463b1ef7befe1b17087e16cfa82`
- `RenderRBMKReflector.java`: `edb385469628527f4a15d103fded855e1e8d2147cd9f2ed96e3d0f0dd26fc000`
- `ObjUtil.java`: `570a88abd771875467643ec6d5e4f6b3f61756f80754b71cc73133b77f2b8608`
- `rbmk_element.obj`: `e84fdd7b1890ebc037a24ae08053794366c6bdbfd0dc873fcaac0aa25c8f0c0c`
- `rbmk_element_rods.obj`: `5702f774da7e88eb1185ba64c32668e9417dd3e5c6838e764faa3d8a4eb8258c`
- `rbmk_rods.obj`: `873a5f93e5a3471d0bc57a2f61082866aca5905507cac2cbb0eecd9913b2e98b`

## Exact owners and materials

`ModBlocks.java:2390–2407,3577–3594` constructs/registers exactly 18 owners for these three handlers. Registered names are `hbm:tile.<field>` because registration passes `getUnlocalizedName()`. The oracle keys below omit `tile.` and namespace.

- Rod handler: `rbmk_rod`, `rbmk_rod_mod`, `rbmk_rod_reasim`, `rbmk_rod_reasim_mod`. Their texture roots are respectively `hbm:rbmk/rbmk_element`, `_mod`, `_reasim`, `_reasim_mod`.
- Control handler: `rbmk_control`, `rbmk_control_mod`, `rbmk_control_auto`, `rbmk_control_reasim`, `rbmk_control_reasim_auto`, `rbmk_boiler`, `rbmk_heater`.
- Passive handler: `rbmk_blank`, `rbmk_reflector`, `rbmk_absorber`, `rbmk_moderator`, `rbmk_outgasser`, `rbmk_storage`, `rbmk_cooler`.
- All non-rod texture roots are `hbm:rbmk/<field>`. Shaft DOWN/UP use `_top`, lateral faces `_side`. Non-control owners additionally register `_cover_top`, `_cover_side`, `_glass_top`, `_glass_side`. Piped owners register `_pipe_top`, `_pipe_side`. Rods register `_inner`, `_fuel`.
- The five control identities have their own non-removable lid appearance; `hasOwnLid()` skips cover/glass icon registration. Only `rbmk_control_reasim` and `rbmk_control_reasim_auto` use `_bottom` on DOWN. That override occurs before `renderPipes`, so their world pipe bottoms also use `_bottom`, not `_pipe_top`.
- `ClientProxy.java:926–928` registers the three handlers. Fuel/control moving TESRs at lines 454–462 are separate owners of visual work, not replacement geometry to remove with these static handlers.

## World geometry, selectors and culling

All metadata retain the shaft. Extra decorations occur only for metadata `6 <= meta < 12`. A successful `findCore` supplies core metadata: 15 means standard lid, 13 glass lid, all other values no lid. Missing core is not a lid.

- Rod: explicitly reset render bounds to `[0,0,0,1,1,1]`; standard-block shell draws only horizontal sides. Then emit OBJ `rbmk_element/Cap` followed by `/Inner`, translated `(x+.5,y,z+.5)`. These OBJ parts are unconditional, not neighbor-face culled. World static handler never emits `/Rods`. A selected lid adds `[0,0,0,1,.25,1]` at `(x,y+1,z)`.
- Passive: full standard block, then the same optional shifted lid. It explicitly resets full bounds before its shaft.
- Piped: shaft uses incoming RenderBlocks bounds. Ordinary engine dispatch supplies owner bounds, normally full cube; unlike the other two handlers, this callback does not reset them itself. Boiler/heater use the shifted lid when present, otherwise four pipe boxes. All five controls always use pipes, irrespective of a fixture's core-lid metadata.
- Pipe boxes at `(x,y+1,z)` have Y `[0,.125]`; X/Z pairs are `[.0625,.4375]` or `[.5625,.9375]`, in low/low, low/high, high/high, high/low order. World calls render all six eligible faces, including pipe bottoms; item calls deliberately omit pipe bottoms.
- `RBMKBase.shouldSideBeRendered`: shell-only suppresses DOWN/UP; active lid forces all lateral faces; otherwise inherited vanilla owner culling applies. Inset pipe render bounds do NOT make their faces unconditionally visible: the culling query uses the full owner bounds. Inherited opaque neighbors can therefore hide pipe faces. Shifted-lid DOWN/UP culling queries neighbors of `(x,y+1,z)`, not neighbors of the original shaft origin.
- Base and piped/passive owners are opaque/full cubes; rods override both to false. Do not change physical collision, light opacity or tile behavior just to simplify the visual model. Existing lid collision is separately derived from the tile's `hasLid()` and adds `.25` to the bounding height.

## Inventory geometry and inherited state

Metadata is ignored by all three inventory callbacks. None removes internal stack faces. Incoming bounds are used for shaft faces; normal engine item dispatch first copies full owner bounds. All callbacks return 3D-in-inventory true and push/pop the GL matrix, but do not push/pop general render state.

- Rod/passive: translate `(0,-.675,0)`, then scale by the actual float `0.35F`, render four one-unit levels, translating up one after every level. Native block-centered mesh adds `(.5,.5,.5)` after this transform. Normal full shaft footprint is approximately `[.325,.675]` X/Z and `[-.175,1.225]` Y.
- Rod each level: `/Inner` 32 quads, `/Cap` 54 faces (24 triangles padded to degenerate quads plus 30 genuine quads), shell WEST/EAST/NORTH/SOUTH, separate `/Rods` 112 padded triangles. Total 808 quads and eight batches. The separate rods asset is used; do not accidentally render the extra `/Rods` group contained in `rbmk_element.obj` too.
- Passive: all six shaft faces on every level, 24 quads/four batches. Order is UP, DOWN, EAST, WEST, SOUTH, NORTH. The caller's NORTH/SOUTH and EAST/WEST icon lookups are crossed for the X/Z faces, although these current side textures are identical.
- Piped: translate `(0,-.75,0)` then scale `0.35F`, four full levels with only three intervening translations, then four five-faced pipe boxes starting at local Y=4. Boiler/heater stop here: 44 quads/five batches. Controls additionally draw `/Lid` at the current translation (Y=3), with the shaft top material: 44 padded triangles, total 88 quads/six batches. `/Lid` asset bounds are `[-.5,.125,-.5,.5,1.25,.5]`, so total control item height extends to approximately native Y=1.2375.

### Inventory color is not a persistent Tessellator tint

The initial `setColorOpaque_F(1,1,1)` precedes `startDrawingQuads`. Actual original `bmh.a(int)` bytecode resets `hasColor`, `hasBrightness` and `hasNormals` (offsets 31–48); it does not make the saved color an active vertex array. Rod shell/Cap/Inner batches therefore contain no color array under normal `ObjUtil.hasColor=false`; fuel batches explicitly contain `0x304825`. Control/passive cuboid batches also contain no color array. Control OBJ Lid has explicit shadow colors.

Original item dispatch sets GL current color from the owner render color before entering the handler. After a colored draw, original Tessellator disables the color array but never issues a compensating GL color reset. The callback source and headless recorder do not establish a portable resulting GL current color for subsequent uncolored batches. Do not claim that upper rod shaft batches are proven green, nor that neutral white proves pixel parity. A neutral native material choice must retain an explicit graphical-acceptance gate for this historical GL-current-color uncertainty. The oracle preserves `hasColor` independently from the stale color integer.

## Override/damage textures

Actual original `RenderBlocks.renderFace*` bytecode (`blm.a` through `blm.f`, offsets 5–16) overrides its passed icon itself. This inherited behavior matters even where the callback never substitutes the local icon variable.

- World and inventory: every standard/cuboid face uses the override, including rod shell, control shaft/pipes, passive shaft, and shifted cover/glass lid.
- Rod OBJ Cap uses override; OBJ Inner and fuel Rods do not.
- Control inventory OBJ Lid uses override. There is no static world control OBJ Lid in this handler.
- An early source-only interpretation that reflector item or direct item face calls ignore override is incorrect; engine bytecode resolves that ambiguity.
- Original render-with-override dispatch (`blm.a(aji,int,int,int,rf)`) sets override, invokes ordinary dispatch, then clears it. It does not set render-all-faces.

## OBJ UV, shade and original light behavior

`HFRWavefrontObject` parses source positions/UVs as floats and flips V with float `1-v`. `S_Face` computes face normals from float position differences, a double cross product, then original `Vec3.normalize`: length is `(double)(float)Math.sqrt(sum)`, and length `< 1.0E-4D` yields zero. `/Inner` and `/Cap` use mixed faces; only triangles repeat vertex 3. `ObjUtil.renderPartWithIcon` uses the face normal rather than vertex normals, exact winding/order, and atlas interpolation of UV×16. It performs no texture contraction. Small original X asymmetries (`-.4374/.4376`, `-.3749/.3751`) are real asset coordinates, not rounding to repair.

`ObjUtil` part shadow is `max(.45F, ny*.3F + .7F - abs(nx)*.1F + abs(nz)*.1F)`, converted to channels by float×255 then truncation. Do not confuse it with the different formula in `renderWithIcon`. Rod world Cap/Inner and control item Lid request shadow=true. Rod item Inner/Cap/Rods request false. Global ObjUtil color, if externally left enabled, multiplies these values; the normal oracle entry state is unset and not a claim about malformed external GL/state sequences.

World standard faces are ordinary RenderBlocks lighting, NOT uniformly fullbright or the caller's initial white. Original `blm.q(aji,int,int,int)` selects full/partial AO when enabled and owner emitted light is zero, otherwise ordinary color-multiplier lighting. None of these owners sets emitted light. Flat branch `blm.d(...FFF)` applies DOWN .5, UP 1, N/S .8, W/E .6 and queries face-neighbor light when at a cube boundary, origin light for inset planes. Therefore shifted lid/pipe faces light relative to `(x,y+1,z)`; pipe inset sides/top sample that origin in flat mode, pipe bottom samples below. AO must retain partial geometry and that shifted origin rather than pretending the entire adornment is at the shaft origin.

Rod OBJ pieces reset packed brightness to the shaft's own `getMixedBrightnessForBlock` after shell rendering, then apply only ObjUtil directional shade. They do not inherit per-corner shell AO, and are not emissive. Standard glass lids remain original pass-0 cutout geometry: all 26 applicable glass PNGs have only alpha 0/255, not semi-transparent pixels. No RBMK callback enables blending or changes cull state.

## Oracle API and limits

`build_oracle.generate(legacy_client, pristine=None)` returns `OriginalRBMKOracle.java`; CLI supports `--legacy-client`, optional `--pristine`, and `--output`. Generation validates the archive or supplied original checkout against the manifest. All three original callback bodies and original icon/lid/culling selectors are inserted verbatim. Original ObjUtil part methods are inserted with only recording labels added. OBJ source text is embedded from pinned assets and independently parsed; six cuboid face methods execute actual original client bytecode in an isolated loader.

`layout(ownerField, metadata, item, coreMetaOrNull, override, visibleMask, legacyFaces, spriteFunction[, initialBounds])` returns:

- Ordered `faces`, each with texture key, OBJ group or `standard`, side, batch, and four `VertexRecord`s (XYZUV, color, color-presence, light, normal/presence).
- `standardCalls` with original bounds, XYZ origin, lid, pipes and shell-only selector values.
- Batch counts/ranges and GL matrix events. `LegacyFaces` is AutoCloseable and takes the independently supplied original client JAR.

The six-bit visible mask is a fixture response from inherited owner culling at each queried neighbor, not a substitute world implementation. `coreMetaOrNull` is an explicit findCore result fixture, not a traversal proof. Faces marked `standard=true` record geometry/material/culling only: their color/light fields are NOT an AO/RenderBlocks-lighting oracle. Brightness fixture values only make OBJ origin/reset behavior observable. The fixture does not prove registration, mechanics, reload, actual lighting-engine execution, GL pixel parity or later dynamic TESRs. Those require separate native production tests and graphical acceptance.

Oracle-only JDK8 compilation and a temporary smoke suite passed 2,049,869 assertions across all 18 owners, all 16 shaft metadata values, absent/no/standard/glass core outcomes, override on/off and empty/single/all visible masks; it also checks item counts, no world fuel rods, ReaSim pipe-bottom material, forced lid sides and suppressed inset pipes. This validates the fixture, not the migrated production.
