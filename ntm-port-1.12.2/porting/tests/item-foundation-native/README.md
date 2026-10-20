# Native item foundation checkpoint

Baseline: CP51 `077492a6c31c910f34697783f1aa0cfabd8ff88e`.
Target: real Minecraft 1.12.2, Forge 14.23.5.2768, stable_39, JDK 8.

## Implemented boundary

- `NativeItemTextureRegistry.texture(T item, String texture)` is common-side configuration, not an emulation of removed `Item` APIs. It returns the same concrete instance, retains the raw original texture expression result, and offers an immutable identity-map snapshot. Translation and registry names do not replace texture intent. Repeated declarations retain original last-write order. Unregistered overwritten construction identities remain recorded but are never model/atlas owners.
- `ItemCustomLore.setTranslationKey` preserves automatic texture declaration. Rarity, explicit glint, localization keys, P11 lore, undefined-item display/scramble and original display resolution stay unchanged. Native nullable tooltip world supplies the original randomness; absent world uses the existing UNDEFINED fallback. Native `getUnlocalizedNameInefficiently` is deliberately retained: stable_39 still exposes it, and its first translation stage is observable.
- `ItemEnumMulti` uses genuine native naming callbacks and native creative-tab filtering while retaining separate raw `iconString`, enum order, separators, subtype stack construction, and safe metadata lookup. Explicit Item texture declarations never overwrite its separate icon state.
- Exact descendant closure: 18 files, 27 native callbacks, including three special creative callbacks. Empty/full battery pairs, 30-entry circuit order, VOTV processed NBT, specialized names, and conditional superclass tooltip calls are retained. Client tooltip callbacks carry `@SideOnly(CLIENT)` as the native base does. No Minecraft-player singleton is substituted for native world context.
- Native model provider runs registration at LOWEST and fills missing inventory geometry at HIGHEST bake priority. It reads Forge's stable, non-obfuscated custom-route maps before supplying a default route; it never erases or replaces those entries. Plain subtype items keep the original metadata-insensitive icon. Existing nonmissing resource/custom models are untouched. The exact native private final `FancyMissingModel$BakedModel` is recognized in addition to the shared missing-model sentinel.
- Models are genuine Forge-loaded generated/handheld item models, including alpha-edge extrusion and native perspective transforms. Textures are registered during TextureStitchEvent.Pre, and fresh sprites/models are obtained on reload. Full3D declaration selects the native handheld parent.
- Existing enum, dynamic/layered icon, tint, and ItemBlock owners are explicitly not flattened by the new provider. Specialized callback meshes retain final baked/TEISR ownership and receive a real declared-texture base before wrapping it.
- Legacy icon dispatch is retained, not duplicated. Its item callbacks receive `new LegacyIconRegister(map, true)`, which qualifies ordinary item-atlas names under items/. Existing default/block construction and generated-sprite insertion remain unchanged. Explicit items/ or blocks/ paths are not prefixed twice, and namespaces are preserved. No original item callback contains a conflicting literal atlas-directory prefix.

## Reproduction

From the module root:

```sh
python3 porting/tests/item-foundation-native/audit-foundation.py
python3 porting/tests/item-foundation-native/audit-descendant-callbacks.py
python3 porting/tests/item-foundation-native/run.py \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)"
```

Results:

- PASS: 184 runtime assertions on actual Minecraft/Forge classes and genuine Forge event-subscription transformation. Actual ModelRegistryEvent, TextureStitchEvent.Pre, ModelBakeEvent, ItemModelMesher and generated-model loader exercised.
- PASS: native missing sentinel and genuine Forge FancyMissingModel replacement; unknown custom metadata model identity preserved; orphan/dynamic/enum/foreign owners not claimed; actual CallbackItemRegistration wraps the generated base even when subscribed before the foundation; specialized TEISR identity retained.
- PASS: actual ItemEnumMulti, ItemGenericPart, ItemDrillbit and ItemCircuit production classes; representative dot/underscore/explicit original icons, invalid metadata normalization, original enum/circuit creative order, hidden/search visibility, and native virtual tooltip dispatch.
- PASS: resource directory and packaged JAR PNG decoding, newly baked model/sprite identity on reload, and original model snapshots remaining intact. Both passes exercise the native model events and mesher routes.
- PASS: genuine Forge SideTransformer server-side removal of native tooltip signatures in the compiled representatives, while common texture declarations survive. This is not a dedicated-server launch.
- PASS: exact base behavior/source substitution audit, 27 descendant callback source oracles, and 23 byte-identical pristine PNGs. `original-textures.json` records hashes.
- PASS: `git diff --check`.

## Explicit limits and next gates

This is a bounded foundation checkpoint, not full ModItems initialization, whole-mod compilation, client launch, dedicated-server launch, or visual parity acceptance. The production classes compiled by the focused probe are listed and hashed in `native-test.log`; there are no NTM collaborator stubs and no fake Minecraft/Forge classes. The atlas uses real TextureMap/TextureAtlasSprite and original decoded PNG data with controlled headless sprite population, not GPU texture upload.

ItemCustomLore and the remaining large descendant owners still depend on the full mod graph. They receive exact source-level callback checks here, not claimed whole-class runtime proof. Existing unrelated mechanics/ItemStack migrations, special dynamic/layered icon callbacks and generated sprite families remain integration gates. The previous LegacyBaseModelHandler/LegacyBaseBakedModel enum routing is deliberately preserved, not replaced or accepted as complete layered-item parity. LegacyIconRegistrationHandler's exact item-only integration line is source-guarded; the focused runtime probe invokes the real polymorphic icon callbacks with the production adapter rather than booting MainRegistry.

The coordinator owns full compilation and remaining semantics. Existing CP37 lighting, boss-HUD and other broader runtime/visual gates are not waived by this work. No assets, balance data, recipes, or ModItems registry declarations are changed in this patch. The sibling ModItems patch consumes the exact common helper and migrates its three ItemAmmo setter calls together with the newly native override.
