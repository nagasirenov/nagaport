# Native universal block family

## Exact scope and behavior

The former five-owner `ISBRHUniversal` family now uses native `IBlockState`, an unlisted immutable adjacency mask, `IBakedModel` and native block/placement/collision callbacks. `ISBRHUniversal` is a common-side shape contract, not an adapter exposing removed RenderBlocks APIs. `RenderISBRHUniversal` is its native baked-model consumer. No removed Minecraft/Forge class is reintroduced and no source is excluded.

The production manifest covers twelve existing and four new Java files. That includes all five original geometry owners, two small concrete RBMK callback migrations, three narrowly scoped RBMK tile retention hooks, the native geometry helper and registry-time model handler. `RBMKGauge` compiles unchanged with the other concrete owners; it is not an edited production file.

- Sandbags preserve neighbor connection widths, full-height collision/selection, the wider inventory geometry and original boundary-face culling. A same-owner neighbor connects even though it is not opaque.
- Barriers preserve horizontal placement side versus yaw fallback, metadata direction, independent four-slab collisions, neighbor-driven four-wall plus ceiling geometry, and the original inventory lower plank height 0.4725 (world height remains 0.4375).
- Wood structures preserve all three metadata variants, exact raw-metadata adjacency, roof/ceiling thickness, scaffold narrow collision versus broad selection, top support and the player ladder property. The original five missing-brace statements rendered even when their conditions were false; the native code intentionally preserves those duplicate draws instead of silently fixing them.
- Rebar preserves twelve detailed rods versus three configurable world rods, separate twelve-rod inventory geometry, original -0.001/1.001 world endpoints, fluid distribution/order and separate render-last concrete and selection paths. See `native-rebar-notes.md` for the complete owner proof and external services.
- The three RBMK panel blocks preserve native tile dispatch, placement direction, bounds and original screwdriver/GUI/keypad branches. The three existing tile classes gain only a same-block metadata retention callback. Native Forge's default non-vanilla tile policy otherwise removes a tile when facing changes. The actual native World/Chunk placement test retains the same tile identity and stored tile data, and changing block owner removes it. Full reactor tile implementations, GUI implementation and reactor controls are outside this bounded compile claim.

The complete production geometry is compared with exact original callback recordings for five owners, all 16 metadata values, all 64 adjacency masks, inventory/world and both rebar settings. The 112 distinct emitted cuboid bounds are then compared with the actual cached 1.7.10 client face bytecode and the two complete pristine RenderBlocksNT face override methods. This retains the world-only NORTH/EAST UV mirror fix, inventory's vanilla partial-box UVs, full-interval out-of-range UV resets, original +90-degree inventory rotation, per-variant vertical offsets and finite normals. Original texture bytes are unchanged.

## Registration and common callbacks

`UniversalBlockModelHandler` is a client event subscriber. It registers the exact seven existing HBM names, all 16 native state/item metadata routes per owner, and required original atlas materials. The rebar model also declares the concrete-liquid atlas resource used by its separate render-last renderer. Actual native ItemBlocks are resolved only at ModelRegistryEvent. An unowned or foreign block implementing the shape contract is left untouched; it is not rejected or rerouted. Reload rebakes use fresh atlas sprites.

There is no blockstate JSON duplication: native state mappers directly point all sixteen listed states to the loader. Adjacency is unlisted and recomputed for rendering rather than expanding persistent metadata. The ordinary legacy cube bridge already excludes this explicitly modeled family. Wood's original icon-stitch callback remains functional with its exact `hbm:wood_barrier` texture name.

Parent-owned integration removes only the obsolete universal ISBRH registration, migrates seven construction chains to native material/sound/naming methods and updates the existing rebar render-last call to native World/event access. The central queued registry preserves original `hbm:tile.*` keys. Shared files are separately guarded by the checkpoint's source boundary; this worker does not claim full ModBlocks/ClientProxy initialization.

## Native evidence and reproduction

    python3 porting/tests/probe-native-universal-blocks.py --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)" --java-home /workspace/shared/nagaport-toolchain/java8 --legacy-client /tmp/nagaport-legacy-client.jar
    python3 porting/tests/native-rebar-probe.py --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)" --java-home /workspace/shared/nagaport-toolchain/java8
    python3 porting/tests/native-rebar-source-parity.py
    python3 porting/tests/check-native-universal-source.py

The classpath/JDK/cache paths above are examples from this workspace, not required hardcoded project locations. No new download is needed. The original face client is pinned by SHA256 and is not redistributed. Without `--legacy-client`, the owner/original-shape/native-light/registry tests still run, but the cached old-client face-bytecode comparison is explicitly skipped.

The combined runner compiles 41 complete actual production classes with 23 explicit external NTM collaborator types and three exact production retention methods inserted into the external reactor-tile fixtures. No Minecraft/Forge classes are replaced. The source oracle is generated from the included `universal-original-geometry.json` with pinned source hashes, not an unpublished Git object. Rebar's separate source and distribution oracles use the supplied pristine source tree.

The combined final native test covers 1,128,135 assertions, not that many distinct behaviors. It includes real metadata, collision, placement and keypad callbacks; actual World/Chunk tile lifecycle; exact old shape/UV/material bindings; actual registry/state mapper/loader routing and reload; native vanilla and Forge flat/smooth buffer consumers; original face-culling policy; and actual Forge damage-model wrapping with unchanged geometry, light, shade and crack-material replacement. Rebar adds 15,441 assertions including 1,600 original transfer comparisons and actual offscreen EGL concrete/selection draws. The renderer test's original client allocation uses Unsafe only to access existing old renderer face methods without creating a second game; native owner/model/registry constructors are real.

These tests establish bounded native behavior and geometry, not full game startup, screenshot/pixel lighting parity, full reactor/fluid-network transport, old-world save migration or dedicated-server acceptance. The whole mod still has unrelated legacy compilation failures, and no runnable mod JAR is claimed. External `ItemRebarPlacer`, receiver service and packet handler still have their separately documented migration work. No gameplay, balance, energy or radiation simplification was made.
