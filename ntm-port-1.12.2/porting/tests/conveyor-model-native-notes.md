# Native conveyor model family proof

## Result

The focused native probe passes on the complete current production conveyor owners, renderers, baked model, cuboid encoder, material selectors, extended-state helper, and custom model handler. No production source is substituted or method-extracted for execution. The final proof log records each compiled production file's SHA-256, including the final Base and Classic `getBlockFaceShape = UNDEFINED` changes.

- 30,320 independent pristine-descriptor layouts and 813,600 ordered original faces.
- Metadata 0 through 15 for all six registered owners and an explicitly configured, test-only Classic instance.
- Exhaustive relevant lift/chute snapshots at y=0, y=64, and y=255: both vertical neighbors independently ordinary/belt/enterable/both, and all four horizontal belt combinations.
- Separate native snapshot classification of all 4^6 combinations across six neighbors, at all three heights. Horizontal enterable-only neighbors correctly do not alter geometry.
- Exact pristine cuboid order, bounds, material overrides, icon selectors, per-face rotation descriptors, and inventory position-only offset. The belt item's legacy translation becomes a +0.25 native local Y offset without changing its UV sampling. Chute inventory uses metadata 2 and its original three grates.
- Native belt world culling: five culled buckets and one uncullable recessed top. Inventory faces occur exactly once. Lift/chute faces are wholly general and do not disappear against neighbors.
- Actual `ForgeHooksClient.getDamageModel` for 768 lift/chute metadata/height/neighbor combinations, verifying owner extended-state acquisition, face counts, replacement texture, and unchanged cuboid positions.

## Native registry and asset closure

Both directory and constructed resource-JAR runs use actual Minecraft/Forge classes:

- All six production registry identities and their real `ItemBlock` mappings.
- Six blockstate JSON files cover all sixteen metadata states without extra model or UV rotations.
- Native custom model loader, `ItemModelMesher`, and `BlockModelShapes` resolve every registered item metadata and listed block state.
- The optional Classic fixture uses its explicit `ResourceLocation` material and handler-provided custom state mapper. No production Classic content is invented or registered by this proof.
- Lift inventory resolves its real JSON parent `minecraft:item/generated`, with original conveyor pixels and native generated-item alpha-edge geometry. It remains a thin, non-GUI-3D icon, rather than invented lift cuboids.
- All sixteen used PNG sprites are decoded in each pack: thirteen animated conveyor materials, concrete, grate, and vanilla iron. Original PNGs and `.mcmeta` files, including animated `conveyor_side.png.mcmeta`, are byte-pinned against pristine sources. The vanilla generated-item JSON and iron PNG are also hash-pinned.
- Invalid metadata, malformed/unknown loader routes, unrelated loader prefixes, missing neighbor snapshots, unconfigured Classic material, and an unspecified registered conveyor owner reject rather than silently substitute another conveyor or missing model.
- Native resource-cache reload and fresh bakes use fresh atlas sprite identities; even a retained unbaked descriptor accepts replacement atlas sprites.

## Oracle independence and exact boundaries

`conveyor-model-native-probe.py` extracts **only pristine historical oracle methods** into a test-only pure-Java recorder. The original world/inventory renderer bodies, `standardBundle`, and Base/Bendable/Classic icon callbacks retain their control flow and numeric constants. Only type names are changed to explicitly labeled oracle types. The recorder captures per-face bounds, icon overrides, legacy rotation fields, and GL translation descriptors; it is not a Minecraft/Forge facade and is never used by production.

The independent descriptor is then encoded with the separately verified production `ConveyorCuboidGeometry` helper and compared to actual production renderer output. This proves layout, selection, material, rotation, and offset composition. It deliberately does **not** claim another independent low-level UV implementation. See `conveyor-cuboid-native-notes.md` and its separate original face-method/vanilla-AO probe for that boundary.

All nine complete conveyor owners are compiled. The only controlled NTM collaborators are the explicit registry/item/tool-tip/entity helper fixtures already declared by the separate complete-owner probe. Actual Minecraft/Forge blocks, states, resource packs, resource manager, metadata parser, models, custom loader, registry delegates, mesher, item-layer generator, and breaking-overlay path are used.

`TextureAtlasSprite.loadSprite(PngSizeInfo, boolean)` and native animation metadata are used. Native `loadSpriteFrames` cannot run headlessly because it initializes `TextureUtil`'s GL texture singleton. An explicitly labeled **test-only CPU frame splitter** therefore decodes the actual PNG pixels, populates the native sprite via `setFramesTextureData`, and installs the parsed native animation metadata. Native item generation receives the real original pixels and every frame. The virtual `minecraft:missingno` fallback advertised by the generated parent is supplied as a controlled atlas sprite, but every emitted lift face and its particle are separately required to use the real conveyor sprite. No Minecraft class or production method is patched to avoid GL.

Controlled atlas rectangles and an Unsafe-allocated headless Minecraft resource-manager holder avoid graphics startup. This proof does not claim atlas upload, live animation playback, live chunk rendering, an actual GUI screenshot, full-mod compilation, or graphical acceptance.

## Reproduce

Run from the repository root with cached native dependencies:

```sh
python3 porting/tests/conveyor-model-native-probe.py \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath "$(cat /workspace/shared/nagaport-tick-preparation/classpath)"
```

Optional `--pristine` and `--vanilla-assets` parameters accept equivalent local source/asset locations, validated against the pinned manifest. The probe uses only temporary compilation output and generated test resource JARs; it changes no production source, performs no staging or commits, and requires no network.
