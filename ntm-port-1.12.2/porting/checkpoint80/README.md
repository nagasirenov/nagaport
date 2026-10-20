# Checkpoint 80: native attribute keys, cure calls, projectile identity and depth cubes

Baseline: sealed CP79 `77b595c32b46e2f9309bcf9924d654ddc29a3c95`. Remote main remains published CP75 `eefb4c1f6f344e4fcb15efb7e6cb6a370419a570`; no push. Delivery is cumulative from that exact published baseline and replaces previous post-CP75 archives.

Fresh complete offline compile: **FAIL 12,619 diagnostics**, 67 fewer than CP79. All 4,065 generated Java compiler inputs match production bytes exactly. Diagnostics are not distinct bugs or completion percentage.

## Production scope

31 Java files change. No dependencies, assets, recipes, energy formulas or statistics change. The furnace nonpositive-stack policy is still pending and is NOT activated here.

- 32 pure attribute-name expressions migrate 61 identifier tokens across 21 files. Actual original/native strings are identical, and modifier map construction retains UUIDs, amounts, operations and float promotion. 350 original/native checks pass, plus compiler owner controls and exact source inversion. This does not migrate the 87 registration/value consumers: native maximum-health, speed and damage clamps differ, including actual original health above the native cap. No blanket uppercase mapping or silently capped boss/player statistics.
- Six ItemPill/ItemSyringe removal-only arguments use native effect singleton identities. Original/native removal tests pass 300 assertions and ten attribution controls. Applications of strength, weakness, hunger, haste/fatigue and jump remain gated because native formulas/modifiers/visuals differ. Lifecycle callbacks in the test are observation boundaries, not full world/attribute cleanup execution. If later original-effect implementations change effect identities, cure lists must be reviewed together.
- Fifteen exact BlockDepth/BlockDepthOre texture calls use the existing native cube model registry. Original PNGs are unchanged; headless native six-face/atlas/reload/item-route tests pass. Mining/drop callbacks are unchanged, not whole-owner compilation claims. Custom top/side BlockKeyhole is excluded.
- Beam name synchronization and its Vortex renderer consumer use a shared owner-local native STRING key. Original Forge display-name string semantics, empty default, constructor ordering and failed owner lookup behavior are preserved, not silently changed to username or decorated text. Native packet/constructor/lookup tests are bounded as documented by the family README.
- Two generic grenade owners use separate native VARINT keys with complete stored/NBT signed IDs. Genuine original lookup returns null for item ID0 whereas native lookup returns ItemAir; the production getter explicitly preserves the original zero/missing-NBT fallback. Nonzero wrong-type casts still fail and unknown IDs still fall back. Native packet/NBT/registry tests use complete generic owner classes atop explicitly bounded ancestors, not full grenade physics, explosions or launched-world proof.
- Independent DiFurnace positive-fuel correction captures the fuel Item before decrement, preserving the last lava bucket's container callback with the same depleted stack. Only canonical EMPTY callback absence becomes nullable null; other callback aliases remain. Fifty native extracted-block checks plus two rejected regression mutations pass. The harness adapts the still-unported quantity syntax only for positive-entry test cases. All six production quantity errors remain; this is not full machine compilation or approval to normalize arbitrary malformed stacks.

## Verification boundaries and continuation

See `full-compile.log`, `diagnostics.json` and `source-inputs.json` for the fresh complete offline compilation and exact source input hashes. No successful full-mod build, runnable JAR, game launch, client/server, complete machine/energy/radiation or saved-world claim.

The combined source boundary reverses all31 current production files to exact CP79 bytes and passes the complete historical guard chain. The three already-native knockback getter changes are a suffix to the primary attribute-name patch; their evidence reverses that suffix before the primary whole-file proof. No historical manifest is weakened to conceal overlapping edits.

Reproduce from the module:

    /workspace/shared/nagaport-toolchain/run-gradle.sh --offline -I /workspace/shared/nagaport-toolchain/restore-official-repositories.init.gradle -I porting/compile-diagnostics-expanded.init.gradle clean compileJava --console=plain
    python3 porting/tests/check-thermal-family-source.py

Per-family commands and scopes live under `porting/tests/native-attribute-keys`, `native-attribute-key-suffix`, `native-potion-removal`, `plain-cube-depth`, `native-entity-beam-owner`, `native-entity-generic-grenades`, and `difurnace-fuel-receiver`. The separate DiFurnace lifecycle audit documents the unresolved nonpositive item identity/storage/recipe boundary. Do not turn that audit into blanket count replacements or shared-storage changes without resolving the pending scope.

Next independent work: literal native World particle calls under the user's particle-only simplification approval. Keep all gameplay, broader model rendering and machine mechanics outside that approval. Major remaining roots still include native state placement, quantity/slot lifecycle, graphics bodies, attributes/effect application compatibility and entity callbacks. Use current compiler evidence, not historical README counts. Preserve every sealed checkpoint durably in Library; no additional GitHub push without authorization.
