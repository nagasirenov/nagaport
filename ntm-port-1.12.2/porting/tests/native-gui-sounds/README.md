# Native resource-backed GUI one-shots

Exact isolated base: CP63 `0bbba4f47800cea8dd60370f697450597f27c8a6`. Local/withheld like CP37 onward. No change to any pending lighting, HUD, sound-entity coordinate or category policy.

## Production

157 executable `PositionedSoundRecord.func_147674_a` calls across 83 files now select `com.hbm.sound.GuiSound.create`. 142 are vanilla button clicks; 15 uses cover eight actual HBM names. The 158th textual occurrence is a disabled comment in GUIAnvil, left byte-identical. The compilation log has 156 `(ResourceLocation,float)` diagnostics plus one `(ResourceLocation,int)` call.

The small final NTM native PositionedSoundRecord subclass preserves the genuine original factory's volume **0.25**, supplied pitch, no repeat, delay zero, NONE attenuation and three zero coordinates. It queries the existing unchanged SoundCategoryResolver at playback, including after reload. Before native accessor selection its volume/pitch getters remain usable by Forge playback observers; afterward native sample multipliers apply once, as the original SoundManager did. Arbitrary unregistered resource names are not rejected or replaced with a fallback. Missing resources follow native SoundHandler missing-sound behavior. The factory retains null rather than introducing an early failure. The one vanilla rename is minecraft:gui.button.press to minecraft:ui.button.click, verified against actual old/native GuiButton bytecode and native SoundEvents identity; other namespaces/paths are not remapped.

No SoundEvent registration or new dependency, networking, gameplay balance, resource assets, original method bodies or outer playback expressions are changed. Existing imports are intentionally retained: only the attributed method-select spans change, avoiding overlapping GUI changes. This is NTM semantics on real native client audio, not a legacy platform facade.

## Executed proof

- Actual cached original `bso` / `bsk` factory bytecode compared with the production native class for 256 raw pitch patterns including negative/signed-zero, subnormal, infinity and NaN, plus null and arbitrary identity behavior.
- 2,154 assertions total, including actual native SoundHandler parsing and accessor/sample selection for all eight HBM definitions and eleven original OGGs; original category metadata, quarter volume, pitch, missing sound and unregistered resource lookup; category reload/replacement; real Forge SoundHandler -> SoundManager -> ForgeHooksClient PlaySoundEvent cancellation and replacement. Sound hardware is deliberately stopped by the final ordinary event observer, not mocked platform classes.
- Three complete unmodified production methods apart from the factory selection are extracted by retained AST method spans and compiled together with real native GUI/Minecraft/audio types: GUIScreenBobble.initGui, GUIScreenRecipeSelector.click and ItemCrucible.playSwing. They execute through the native event path. This is not whole-GUI/weapon-owner compilation.
- Independent JavacTask parses/analyzes all 4,062 original genuine production sources and 4,063 after. All 157 original class receivers, both arguments, all outer SoundHandler receivers and complete enclosing methods are retained. All 157 new factory bindings and outer native handler methods resolve correctly. Exact diagnostic multiset: 15,952 before -> 15,795 after, precisely 157 old missing-method diagnostics removed, zero new/other changes. This is **not** a successful full-mod build.
- Full byte inverses cover all 83 files, preserving every non-selector byte and disabled comment. Existing category resolver is hash-pinned and unchanged. Original/native selected sample bytes match exactly; existing explicit HBM namespace qualification and native lowercase file normalization are documented in resource-provenance.json.

Native sound-name normalization, native Forge event types and resource packs using native vanilla event names remain engine boundaries. No universal legacy resource-pack/third-party mod compatibility, audible playback, launched full client/server, complete GUI interaction, full weapon behavior or whole-mod success is claimed. Previously existing category-resolver append/replace validation behavior is reused, not reimplemented.

## Reproduce

From repository root, using retained cached dependencies:

    python3 ntm-port-1.12.2/porting/tests/native-gui-sounds/check-source.py --project "$PWD/ntm-port-1.12.2" --attribution ntm-port-1.12.2/porting/tests/native-gui-sounds/proof
    python3 ntm-port-1.12.2/porting/tests/native-gui-sounds/run.py --project "$PWD/ntm-port-1.12.2" --resources "$PWD/ntm-port-1.12.2/src/main/resources" --out "$(mktemp -d /tmp/gui-sound-runtime.XXXXXX)"
    python3 ntm-port-1.12.2/porting/tests/native-gui-sounds/run-attribution.py --repo "$PWD" --out "$(mktemp -d /tmp/gui-sound-attribution.XXXXXX)"

Both runners default to /workspace/shared/nagaport-toolchain/java8, /tmp/nagaport-ae2-native-classpath.txt and the retained /tmp/nagaport-legacy-client.jar. Provide their arguments for alternate locations. No downloads, asset copies or installs.

`check-source.py` is the **isolated whole-file guard**, not a claim that later independently edited GUI files still match CP63. To compose after such edits, `merge.py --project <target-module> --receipt <new-json>` first verifies the exact complete original invocation multiset in active Java code, excludes comments/literals, and proves a full inverse equal to each integrated input. It is a dry run unless `--apply` is provided. This preserves unrelated GUI edits and records their before/after hashes rather than regenerating the historical assertion. All 83 baseline sources remain in sources.zip, and the original/after attribution records remain immutable.
