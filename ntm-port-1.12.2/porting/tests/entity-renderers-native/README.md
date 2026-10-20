# Native entity renderer manager family

## Production scope

This finite family repairs all **19 direct `Render` constructor diagnostics** in
CP84 `502f0b9d`, the required `RenderQuasar -> RenderBlackHole` constructor closure,
and all three `RenderChicken` subclasses. The chicken closure also repairs the
two generic erasure clashes in `RenderDuck.getEntityTexture` and
`RenderQuacc.preRenderCallback`. The typed attribution is **24 diagnostics**, not
23 classes or 24 registrations treated as an error count. No full-build delta is
claimed by this isolated proof.

There are 24 original live registrations, with exact entity/renderer pairs and
original order retained. `RenderBalls` and `RenderQuackos` had no live registration
and are not newly registered. All native `Render` subclasses use `Render<Entity>`
and retain their existing exact Entity signatures; their narrower overloads and
casts are unchanged. The chicken texture and pre-render overrides use native
`EntityChicken` signatures so compiler-generated bridges reach the intended body.

The native `RenderChicken(RenderManager)` constructor creates a fixed default
model and 0.3 shadow. Each subclass explicitly restores the caller's original
model identity and shadow radius. Thus the registered Quacc retains the original
`ModelChicken`, 7.5 shadow, 25x pre-render transform and the existing non-darkening
boss-display capture. This patch does not add a HUD, change boss behavior, or
replace any custom renderer with a generic visual.

## Native lifecycle

The pinned Forge 14.23.5.2768 `RenderManager` constructor calls the typed
`RenderingRegistry.loadEntityRenderers(RenderManager, Map)` factory overload.
Actual Minecraft bytecode assigns `renderItem` first, constructs RenderManager,
and only then publishes its global `renderManager`. Every factory therefore uses
its callback argument. No global-manager shim or early owner lookup is used.

The test registers before a Minecraft singleton exists. It then constructs a real
Minecraft settings fixture, resource manager, texture manager, native RenderItem,
and native RenderManager. All factory constructors execute while the global
manager is still null. A second real manager executes the same factories while
the global slot still points to the first, proving fresh callback ownership.
The item-model registry is an empty native registry because this test draws no
items; this is not a full model reload or client startup proof.

## Exact preservation and constructor closure

`run.py --audit-only` reverses only the enumerated API migration edits and verifies
SHA-256 equality against each complete CP84 renderer source. This pins all draw
bodies, texture expressions, model construction, scale, transforms, state changes,
and existing comments. The 24 selected registration statements are checked
exactly and in order, leaving unrelated ClientProxy changes independent.
A whole-main-source call/subclass scan catches missed constructor callers and
subclasses; Quasar is the only dependent subclass.

The manifest also pins the unchanged complete mesh/model dependency sources and
29 existing literal-referenced resource files. Three pre-existing unresolved
literal paths are recorded, not invented or fixed in this work:

- `RenderBeam5`: `textures/models/PlasmaBeam.png`. Its actual draw is untextured
  and never binds that getter's return value.
- `RenderShrapnel`: `textures/models/shrapnel.png` in its getter. Its actual draw
  explicitly binds the existing `textures/entity/shrapnel.png` instead.
- `RenderBombletTheta`: `textures/models/bombletThetaTexture.png` is the original
  fallback field. Its live EntityBombletZeta branch uses the existing Zeta texture.
  No fallback-render acceptance is claimed.

## Case-sensitive resource correction

The original live chopper-mine draw failed actual native texture loading because
`ResourceLocation` lowercases `chopperBomb.png`, while the stored filename did not.
Only this asset is renamed:

`assets/hbm/textures/models/chopperBomb.png`
→ `assets/hbm/textures/models/chopperbomb.png`

Old and new content SHA-256 are both
`96aa291efb807bb5cc3e89aa3e20449c74705e395632890164e33c32580c4c4e`.
Git records a 100%-identical rename. Both original source references are retained
and enumerated; all main Java/JSON/metadata/OBJ/MTL references are audited. The test
builds two temporary resource ZIPs from these same bytes and uses the real native
`FileResourcePack`: old mixed-case lookup fails; native lowercase lookup succeeds
and decodes the PNG. This makes the case check meaningful on Windows as well as
Linux. The successful actual chopper draw then binds the original texture.

On a case-insensitive Windows working copy, check the recorded Git path spelling,
not merely whether opening either spelling works. If an application method leaves
the old spelling, perform a two-step rename through a verified-unused temporary
filename, then to `chopperbomb.png`, and inspect `git diff --summary`. Never
force-overwrite an existing differently owned destination or temporary file. The
packaged ZIP/JAR must contain exactly the lowercase entry.

## Evidence boundary

**Seven complete production renderer classes are compiled unchanged and executed:**
`RenderBeam5`, `RenderChopperMine`, `RenderCoin`, `RenderWormBody`, `RenderDuck`,
`RenderQuacc`, and `RenderQuackos`. The first four run their actual complete
`doRender` bodies on Mesa surfaceless EGL. GL primitive queries verify real
geometry submission, original texture bindings are checked where applicable,
and matrix stack balance and GL errors are checked at partial ticks 0, 0.375, 1.

For the three chickens, the real native `prepareScale`, erased texture and flap
bridges, and `renderModel` phases run with real ModelChicken geometry. Tests
validate matrix/capture/model/shadow ownership and actual GPU primitives. These
are **render-phase tests**, not complete `doRender` or in-game acceptance for those
three classes.

**Sixteen other renderer classes are source-derived constructor+field
projections.** The generator keeps original complete constructors and field
initializers verbatim, including real model construction and real mesh/VBO
loading. It removes ordinary methods and adds a throwing test-only texture
method, so no projection can be mistaken for drawable production code. All
Forge/Minecraft/LWJGL APIs and model/mesh classes are genuine native classes;
none are copied stubs. Application entity class tokens and the boss display name
are controlled fixture inputs. This is not proof of full entity simulation, full
ClientProxy compilation, every projected renderer's render closure, a mod boot,
or visual parity. No production placeholder or source exclusion is introduced.

Result: **232 native assertions passed**. Three independent generated-input
negative controls also fail at the intended runtime checks after source auditing:
using the global manager fails ownership; removing Quacc's 25x scaling fails its
native phase matrix; changing Duck's texture fails native erased bridge texture
selection. These mutations exist only in temporary test inputs.

## Reproduce

No download or whole Gradle build occurs. From the project directory:

```sh
python3 porting/tests/entity-renderers-native/run.py --audit-only \
  --diagnostics porting/checkpoint84/full-compile.log

python3 porting/tests/entity-renderers-native/run.py \
  --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)" \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --vanilla /workspace/shared/nagaport-toolchain/archives/minecraft-1.12.2-client.jar \
  --diagnostics porting/checkpoint84/full-compile.log \
  --attribution /tmp/entity-renderers-attribution.json
```

The three optional negative controls use the same native command plus
`--mutant global-manager`, `--mutant quacc-scale`, or `--mutant duck-texture`, and
are expected to return nonzero with the corresponding assertion. The proof uses
portable source hashes and does not need access to Git history. No proprietary
Minecraft, Forge, or toolchain binaries are included in the evidence.
