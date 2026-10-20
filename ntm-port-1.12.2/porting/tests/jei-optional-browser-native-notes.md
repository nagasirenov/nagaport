# Native recipe browser optional-dependency correction

Baseline provenance: checkpoint21, `290af844793ced6c20380adfaa83aab764ae6197`.

The tests do **not** require that commit or any Git repository. The test-only
`jei-optional-browser-baseline.json` retains 23 complete original sources: the
20 affected handlers, NEIConfig, NTMJeiPlugin, and GuiInfoContainer. It was
packaged once from the verified baseline Git objects, retaining their original
UTF-8 bytes (including original line endings), byte lengths, and per-file SHA-256.
Both runners pin and verify the complete JSON fixture SHA-256 before reading it:
`9d44102bb5e663aaebc71bc2e13c404b79e23b4a1727dd920f1f7cfd01d19584`.
The retained source is never included in production. Native oracle compilation
uses the retained whole NEIConfig and changes only its class name; source checks
normalize CRLF for comparison with Python's text-mode source reads.

## Bounded production changes

- `NTMJeiPlugin` no longer resolves `NEIConfig`, which implements optional NEI's `IConfigureNEI`. Both browser entry points instead call `RecipeBrowserVisibility`, a plain class containing the unchanged visibility policy (one whitespace-only blank line is cleaned).
- `RecipeStackMatcher` is a small recipe-focus helper using native `ItemStack` and Forge's wildcard constant. It preserves the actual pinned **NEI 1.12.2-2.4.3.245** predicates, including their asymmetry between subtype checks and damageability, native NBT behavior, null handling, count insensitivity, and lack of an extra EMPTY rejection.
- Exactly 20 recipe handler files lose their incidental NEI utility import: 17 callers use the new helper; the three unused imports in CrucibleAlloying, Radiolysis and Refinery are removed. No recipe data, quantities, matching call order, layout or handler control flow changes.
- The optional native `NEIConfig` adapter remains. `GuiInfoContainer` and its Forge optional GUI/ghost integration remain unchanged. CodeChickenLib rendering utilities are not part of this correction and remain where previously used.
- No build configuration, production source exclusion, fake platform class, CE dependency or balance changes are introduced.

## Native oracle and no-NEI proof

Run:

```sh
python3 porting/tests/jei-optional-browser-native-test.py \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath "$(cat porting/continuation-2026-10-19/native-probe-classpath.txt)"
python3 porting/tests/check-jei-optional-browser-source.py
```

The native runner performs these distinct checks:

1. Compiles the **complete production** plugin, visibility policy, matcher, three JEI core classes and catalyst adapter with the NEI jar physically removed from the pinned native classpath. No source slicing is used for these production files.
2. Compiles the current optional NEI adapter and the complete retained baseline `NEIConfig`, with only its class name changed to `OriginalNEIConfig` so both can coexist. This baseline is the visibility oracle, not a copied rewrite of the policy.
3. Executes **117,379 checks** against the actual pinned `NEIServerUtils` binary: 242 stack variants, all 58,564 ordered pairs through each of the two predicates, input nonmutation, and explicit edge-case assertions. Variants cover null, EMPTY, negative/zero/positive counts, wildcard metadata on either side, normal metadata, absent/empty/equal/different NBT, subtype/non-subtype items, ordinary damageable items and a native `Item` subclass explicitly reporting both subtypes and damageability.
4. Executes **311 checks** for visibility and complete plugin lifecycle methods with the native oracle available. Visibility covers both secret settings and both polaroid branches, ordered item/metadata/count/NBT equivalence, battery charge variants and exclusion of BASE ore grade. Plugin checks cover category reset/registration, custom-category branch, native lookup, real catalyst adapter, blacklist delegation, GUI-registration callback, the anvil transfer registration branch and runtime navigation.
5. Executes **99 checks** for the same scoped plugin/new-helper graph in a fresh `URLClassLoader` that has no NEI jar and rejects every `codechicken.nei.*` load. The actual optional `NEIConfig` is a negative control: its `IConfigureNEI` interface fails resolution as expected. After that control, all plugin lifecycle methods and both new helpers execute with **zero attempted NEI resolutions**.
6. The separate source guard provides **30 checks** that the exact 20 handler changes are only the expected import/call substitutions or unused-import removal, the visibility body is unchanged, the existing optional GUI integration is unchanged, and the whole plugin delta is limited to shared-policy delegation plus two unused imports.

Actual Minecraft, Forge, JEI and NEI implementations are used, with observation-only proxies for JEI registration interfaces. Native Forge's `Loader.setupTestHarness` and `DummyModContainer` provide the owner for the real event-bus registration call; this is not mod discovery or client startup.

The temporary **NTM-only fixtures** are explicit in the runner: item/block registries, settings and enum cardinalities, battery/ore/gun content, main-registry state, category registry/configuration, anvil/custom category subclasses, and the anvil GUI transfer collaborator. Their narrow behavior only isolates this graph from the still-unported mod. They are not production replacements or evidence that those NTM implementations compile. The visibility oracle shares these fixtures, while source comparison separately establishes that the full original policy body was preserved.

## Portable packaging verification

The two bounded runners were also executed from a fresh temporary copy containing
only their required production/test/fixture files, outside any Git checkout.
The real `git rev-parse --show-toplevel` returned 128 there before the tests ran;
no fake Git executable was used. The source check and native differential,
visibility/plugin oracle, and rejecting no-NEI classloader checks all passed.
The saved dependency classpath and Java 8 toolchain were reused; no production
classes or platform APIs were replaced to make this packaging check work.

## Existing native regressions without NEI

The existing complete-class runners were updated only to include the new production matcher and to stop claiming that NEI is still an active dependency. All were rerun with the `NotEnoughItems` jar removed:

- JEI core: 58 assertions.
- Selected JEI handlers: 272 assertions.
- Complete custom machine handler: 78 executable assertions plus 13 source-only annotation contracts.
- Complete satellite handler: 407 assertions.
- Complete anvil recipe/overlay handlers: 159 assertions plus the existing source-only GUI lifecycle guards.
- Satellite source/overflow parity: 540 source/mathematical assertions, not a native rendering test.

The existing custom and satellite tests still use their previously documented exact extracted tooltip collaborator; that pre-existing limitation is not promoted to whole-helper coverage. The new optional-browser runner does not slice any production class.

Recorded runs:

- `porting/continuation-2026-10-19/jei-optional-browser-native.log`
- `porting/continuation-2026-10-19/jei-without-nei-regression.log`

## Explicit limits

This establishes the complete scoped production plugin/new-helper graph with isolated NTM collaborators, **not** a full-mod build or a working game client without NEI. Actual NTM item/static initialization, real anvil GUI integration, rendered output, and Forge's optional-interface stripping/client lifecycle still require the wider port and game-level validation. No claim is made that all remaining mod errors or optional GUI concerns are resolved. Checkpoint21's historical source manifest is not rehashed by this correction; a newer checkpoint must record the subsequent source state separately.
