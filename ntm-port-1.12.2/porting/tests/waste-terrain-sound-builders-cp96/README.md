# CP96 candidate: existing waste-terrain sound builders

Isolated source baseline: sealed CP95 `58f7f0c93cb4f5814d552e003ec9c7e2179a115f`. This is a bounded production migration, not full-owner, full-build or complete-game acceptance. Work was performed in a detached, non-cone sparse worktree containing only four production files and the unchanged native test launcher. No resources, dependency tree or broad source archive was copied. There was no main-worktree source edit, network, dependency regeneration, Gradle run, access transformation, commit or push.

## Concrete scope

Only four production files change:

- `WasteEarth`, `WasteLog` and `WasteLeaves`: one six-line owner-local `withSoundType(SoundType)` builder each.
- `ModBlocks`: seven exact call-position substitutions. `waste_earth`, `waste_mycelium`, `waste_leaves` and `burning_earth` retain PLANT; `waste_log` retains WOOD; `frozen_grass` and `frozen_log` retain GLASS.

Each existing NTM owner still directly extends native `Block`. Its builder calls genuine protected `super.setSoundType(sound)` and returns the same owner. Each cast is over its proven fresh named constructor followed by native `setTranslationKey`, at the exact original sound-call position. Native setter bytecode and the absence of owner overrides establish receiver identity; no general safety claim is made for arbitrary Block references or subclasses.

All constructor arguments, material identities, names, random-tick flags, sounds, following light/tab/hardness/resistance setters, texture calls, gameplay, rendering and registry bodies remain unchanged. In particular, frozen grass keeps `tick=false`, waste mycelium keeps its post-sound full light setter, and WasteLeaves keeps the false opaque/full-cube overrides used during native construction. All four following legacy texture calls remain in production, unresolved. Mixed ModBlocks line endings are byte-preserved.

The seven original CP95 `setStepSound(SoundType)` diagnostics are retained in `cp95-selected-diagnostics.txt`; `compiler-scope.json` pins that original complete compiler log. This is frozen selection evidence, not a new compile. No fresh full-source attribution or full-mod build was run. The coordinator owns composition and the next common compile. The later texture diagnostics may become visible; they are not suppressed here.

## Fresh bounded proof

- `check-source.py`: exact whole-file inverse against CP95 for all four complete family files, exact tested hashes, seven closed receiver/cast formulas, original sound identity and chain position, exact three builder bodies, absence of overridden native setters, unchanged constructors, texture/gameplay bytes, and unchanged line endings. This is explicitly four-file-family evidence, not acceptance of unrelated composed files.
- `run-native.py`: **386 finite assertions** against genuine cached Minecraft/Forge classes. It executes all seven current registry construction slices: the three complete chains (`waste_log`, `frozen_grass`, `frozen_log`) and four chains ending immediately before their unchanged unsupported texture call. Tests cover exact native owner/material/name/ticking/light/hardness/resistance/tab/sound properties; all five event identities and volume/pitch; custom/null arguments; same-instance return; unchanged state/ticking; unrelated-block isolation; all native fluent setters; and four constructor/name/sound/next-setter evaluation-order shapes plus failure ordering.
- Native construction includes the exact unchanged WasteLeaves opaque/full-cube overrides, not invented replacements. The test checks their native state results and the constructor's cached light opacity. These are construction dependencies, not new gameplay-port scope.
- Nine runtime mutations (drop assignment, wrong argument, null return for each owner) compile successfully and fail at actual native execution. Compile rejection is not counted as a runtime kill.
- Twelve independent source mutations fail the whole-file guard, including moved sound, foreign cast, changed constructor flags, changed construction-dispatched opacity, deleted texture calls and unrelated renderer/registry changes.
- Two native compiler negative controls establish that `Block.setSoundType` remains protected and native Block has no `withSoundType`. `check-access.py` pins the native jar/class to the existing sealed CP94 proof and checks all six native fluent setters' same-receiver bytecode; the sound setter is precisely the original five instructions. No native/cache files are changed.
- `check-replay.py`: dry run, exact forward replay, duplicate-apply rejection, reverse dry run and byte-exact reversal of all four files. `production.patch.gz` is deterministic and production-only.

## Honest fixture boundary

These are NTM-only construction projections, not compiled complete production classes. The actual current constructor and builder bodies are extracted into same-named test-only NTM owners retaining their genuine native Block superclass. The exact unchanged WasteLeaves `isOpaqueCube` and `isFullCube` bodies are also extracted because native construction/state initialization can dispatch into them. All Minecraft/Forge classes are genuine, not stubs. A test-only MainRegistry supplies only `blockTab`; its value is a genuine native creative tab. The unchanged Forge event launcher is hash-pinned.

Unrelated icons, updates, drops, radiation, burning, slowing, leaf falling and effects remain protected by the whole-file inverse but are not runtime-accepted. There is no claim of complete owner compilation, registry loading, renderer/texture acceptance, world gameplay, audible output or launched client/server success.

## Reproduce offline

From the module, using the existing cached toolchain:

```sh
python3 porting/tests/waste-terrain-sound-builders-cp96/check-source.py
python3 porting/tests/waste-terrain-sound-builders-cp96/check-source-mutants.py
python3 porting/tests/waste-terrain-sound-builders-cp96/check-replay.py
python3 porting/tests/waste-terrain-sound-builders-cp96/check-access.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /workspace/shared/nagaport-toolchain/native-classpath.txt
python3 porting/tests/waste-terrain-sound-builders-cp96/run-native.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /workspace/shared/nagaport-toolchain/native-classpath.txt
python3 porting/tests/waste-terrain-sound-builders-cp96/check-mutants.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /workspace/shared/nagaport-toolchain/native-classpath.txt
```

The optional `--family-only` flags on the source/native runners are explicit aliases: all modes require exact whole-file restoration for these four files and do not project away additional composed changes in them. Additional integration changes inside ModBlocks or another family file require separately justified composition evidence, not a silently weakened source guard.
