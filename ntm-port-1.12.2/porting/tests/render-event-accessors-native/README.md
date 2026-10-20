> Historical recovery notice below is superseded by the successful CP77 rerun recorded in porting/recovery-validation-2026-10-20/README.md and the actual *-current evidence in this directory. The old lost logs remain unavailable; the later rerun is separate fresh evidence. CP85 adds a distinct four-rule extension under native-render-event-extension.

# Recovered native render-event accessors

## Recovery status, 2026-10-20

The previous execution workspace disappeared. The production patch has been
recovered exactly; the original native proof logs and evidence files were lost.
This directory contains reconstructed evidence SOURCE, not a recreated historical
proof log. Native tests and fresh compiler attribution have NOT RUN in the
replacement environment. The authentic native classpath is being restored by the
parent task; this worker performs no downloads and does not bypass denied routes.

Current source-only verification:

- Recover from source-only commit d25dcb9f0908d76b1e9eca96b75e58f6397de355,
  which reproduces the previously frozen CP76 production source.
- Recover all 65 exact historical UTF-16 token positions in 10 files.
- The complete generated 23,592-byte production patch has SHA256
  6fdceab3cef00dddfbccbc7d7d8af309f425ee54613ca067b1cb20d559e64337,
  matching the retained original patch SHA256 byte-for-byte.
- Ten exact whole-file source reversals and 65 rejected single-token regressions
  pass in this environment. These are static checks, not native execution.

Historical report only, NOT current evidence: the former workspace reported
3,771 native assertions (including six complete-callback assertions), 19 scope
fixtures, 65 reversed-field negative compile controls, one native getter mutant,
65 source mutants, and source analysis 13,021 to 12,956 diagnostics. Those logs
are gone. None of those historical test counts certify these reconstructed tests.
No fabricated proof.log, bytecode dump, or dependency hash inventory is included.
Only a successful actual run of run.py will generate *-current evidence files.

## Production scope and boundaries

Exactly 65 qualified read tokens in 10 production files change to actual Forge
getters. Receiver expressions and their position/evaluation count remain intact.
No wrapper, event copy, caching, cancellation write, registration, priority,
GL call, layout/timing formula, renderer signature, or fog-color mutation changes.

- RenderGameOverlayEvent: 23 resolution and 16 type reads.
- RenderWorldLastEvent: six partialTicks reads.
- RenderLivingEvent: five entity and one each renderer/x/y/z.
- DrawBlockHighlightEvent: four player, two partialTicks, one target, one context.
- EntityViewRenderEvent: two entity and one renderPartialTicks.

Four extra signatures in rules.tsv have zero production selections: overlay
partialTicks, world-last context, highlight subID, view renderer. Old references
inside comments remain unchanged. Unrelated entity/type/coordinate fields are
never textual migration targets. ItemToolAbility's existing CRLF is preserved;
git diff --check flags that retained line ending, not a new whitespace change.

The reconstructed scanner resolves receiver ancestry and native field/getter
ownership. It rejects unknown/type-name receivers, assignments, compound writes,
increment/decrement, class/interface field shadows, and reachable source getter
overrides. Exact private-final fields and public zero-argument getter signatures
are required. Native source/dependency closure must be checked anew.

Unknown external subclasses may override virtual getters unlike old field reads.
The test explicitly demonstrates this boundary and native parent-constructor
virtual dispatch. No equality with arbitrary third-party overrides is promised.
No reflective production access or synthetic Minecraft/Forge classes are added.

The planned native proof executes genuine event constructors, all overlay native
ElementType Pre/Post variants, reference/null identity, raw float/double bits,
receiver/null/exception timing, flags, bytecode, and dependency override closure.
It compiles/executes all 65 selected accessor expressions independently. The
complete pristine preRenderEventFirst method is extracted unchanged except its
getter token; only the NTM vanish lookup and cancellability metadata are fixtures.
This is not full-handler compilation, event-bus transformation or gameplay proof.

Test-only Unsafe supplies genuine payload identities without initializing a game
world. Real Mesa EGL allows living-renderer static initialization; the context
helper is reconstructed from existing block-registry-native evidence. No render
scene, full ScaledResolution-dimension behavior, or game loop is certified.

## Reproduce once authentic dependencies are restored

From module root:

    python3 porting/tests/render-event-accessors-native/check_source.py

Then, only when all real dependencies are available:

    python3 porting/tests/render-event-accessors-native/run.py \
      --java-home /workspace/shared/nagaport-toolchain/java8 \
      --classpath-file /tmp/nagaport-ae2-native-classpath.txt \
      --forge1614 /tmp/nagaport-forge1614-userdev.jar \
      --original-root /workspace/shared/nagaport/ntm-original-pristine \
      --verify-attribution

The paths are configurable inputs, not proof those files currently exist. The
runner rejects missing JARs, extracts cached LWJGL libraries into a temporary
directory, uses software Mesa EGL and genuine JNA/LWJGL, and downloads nothing.
Fresh pre-attribution uses complete token-reversed input files; post-attribution
checks exact getter spans. Any diagnostic counts printed belong to that run, not
to the lost historical environment. Full builds still belong to the parent task.

Integrate the finite production patch rather than copying complete changed files.
For overlap, freshly scan the destination in pre mode and feed its TSV to migrate.py;
it enforces the same receiver/path/field/getter inventory and refreshes file hashes
while retaining unrelated changes. Rerun the native checks after composition.
