# CP94 candidate: existing log and pillar sound builders

Isolated source baseline: sealed CP92 `0675e599e5ee31a60776b560af792372df191790`. This package contains a real bounded production migration, not a full-build, complete-owner or complete-game result. The worktree was source-only, non-cone sparse, with only explicitly selected proof dependencies; no broad resources or prior evidence directories were copied. No main-worktree edit, network use, access transformation or dependency regeneration occurred.

## Concrete scope

The frozen CP93 compiler has an unresolved legacy `setStepSound(SoundType)` call at each of ten selected registry sites. `cp93-selected-diagnostics.txt` retains them, and `compiler-scope.json` pins the complete original log hash. CP93's existing full build has 10,164 diagnostics. This is selection evidence only; it is not a new full build of this candidate.

Only three production files change:

- `BlockLogNT`: one six-line owner-local `withSoundType(SoundType)` builder.
- `BlockRotatablePillar`: the same narrow owner-local builder.
- `ModBlocks`: ten exact call-position substitutions. Seven log registrations (`pink_log`, `vinyl_log`, `pvc_log`, `lattice_log`, `bf_log`, `primed_log`, `eu_log`) keep `SoundType.WOOD`. `block_insulator` and `block_fiberglass` keep `SoundType.CLOTH`; `block_tritium` keeps `SoundType.GLASS`.

Each builder calls genuine protected `super.setSoundType(sound)` and returns the identical owner. The preceding receiver expression is cast back to its proven fresh named constructor owner at the original sound-call position. Native `setTranslationKey`, `setHardness` and `setCreativeTab` return the same receiver; neither NTM owner nor its native parent chain overrides these methods. These casts do not claim safety for arbitrary Block values or unknown subclasses.

The pillar chains keep name and creative tab before sound; the pink-log chain keeps name and hardness before sound; the other logs keep name before sound. Every constructor/material/icon-string argument, sound reference, following setter, receiver and argument evaluation order remains unchanged. All constructors, icon/texture code, tooltips, rarity, inherited ownership and other gameplay bytes are unchanged. Existing native BlockLog construction still supplies WOOD sound, hardness 2 and resistance 10 internally; the pink-log chain's later hardness 0.5 does not lower that native resistance. The test checks its resulting explosion-resistance getter remains 2.

The exact inverse restores all 4,071 CP92 production Java files. All 92 remaining legacy registry sound calls and every sound/event definition remain untouched. No generic wrapper, new owner class, native-class substitution, reflection-based production setter, AT, cache edit, resource or dependency change is introduced. Original ModBlocks CRLF is preserved.

## Fresh proof

- All 4,071 production units parse and attribute before/after with JDK8, zero syntax errors and no class generation. All ten builders, closed casts, constructor identities, exact sound objects and native setter chains bind. Diagnostics change **10,366 → 10,359 against CP92**: exactly ten legacy sound errors disappear, exactly three pre-existing pillar texture errors become visible, and there are no other diagnostic-message multiset changes. This is not a CP93 candidate full-build total.
- **387 finite native assertions** run seven complete production log construction chains and three pillar construction chains ending immediately before their still-unported `setBlockTextureName` call. They check owner identity, native material, names, hardness/resistance, tab/null, exact sound object and all five event identities, volume/pitch, same-instance returns, custom/null arguments, untouched unrelated blocks, native constructor defaults, all three chain orders, and receiver/constructor-argument/name/sound exception ordering.
- Six runtime mutations (dropped assignment, wrong argument, null return separately for both owners) each compile successfully and then fail during actual native execution. Compile failure does not count as a runtime kill. In particular, the log dropped-assignment mutant is caught even though the native constructor already happens to use WOOD.
- Ten independent in-memory source mutations fail the exact source guard, covering builder/argument/return errors, foreign cast, relocated sound, constructor fields, unrelated rendering and an unrelated sound definition.
- Two compiler negatives confirm native Block still has no public `withSoundType` and its setter remains protected. `check-access.py` inspects the five native setter instructions and both native parent member tables. The authentic cached Forge1614 archive's original `forge_at.cfg:35` confirms the old Forge external setter was public; no archive is changed or transformed.
- Patch replay passes dry run, exact forward application, duplicate-apply rejection, reverse dry run and whole-file byte-exact reversal. `production.patch.gz` is deterministic and includes production files only.

## Honest native fixture boundary

These are **NTM-only construction projections**, not compiled complete production owner classes. The actual current private constructor string fields, exact constructor bodies and exact builder bodies are extracted into same-named test-only NTM owners with their genuine native `BlockLog` / `BlockRotatedPillar` superclass hierarchy. Unrelated icon/tooltip/rarity methods are excluded; their actual production bodies are protected by the complete-file inverse, not newly accepted at runtime. No native Minecraft or Forge class is stubbed. Test-only MainRegistry supplies only `blockTab`; complete production RefStrings and the existing unchanged genuine Forge event launcher are pinned by hash.

No log/pillar placement, metadata/rotation, texture/icon rendering, tooltip/rarity, drop/break/leaf behavior, audible output, registry loading, launched client/server, complete-owner compilation or full-mod runtime acceptance is claimed. Existing full-source attribution still reports four untouched legacy icon-field errors in BlockLogNT and five untouched icon/texture/override/rarity errors in BlockRotatablePillar. The coordinator owns later composition and integration-build acceptance.

## Reproduce offline

From this module, use the existing cached toolchain only:

```sh
python3 porting/tests/log-pillar-sound-builders-cp94/check-source.py
python3 porting/tests/log-pillar-sound-builders-cp94/check-source-mutants.py
python3 porting/tests/log-pillar-sound-builders-cp94/check-replay.py
python3 porting/tests/log-pillar-sound-builders-cp94/check-access.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /workspace/shared/nagaport-toolchain/native-classpath.txt --forge-userdev-jar /tmp/nagaport-forge1614-userdev.jar
python3 porting/tests/log-pillar-sound-builders-cp94/run-attribution.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /workspace/shared/nagaport-toolchain/native-classpath.txt
python3 porting/tests/log-pillar-sound-builders-cp94/check-attribution.py
python3 porting/tests/log-pillar-sound-builders-cp94/run-native.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /workspace/shared/nagaport-toolchain/native-classpath.txt
python3 porting/tests/log-pillar-sound-builders-cp94/check-mutants.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /workspace/shared/nagaport-toolchain/native-classpath.txt
```

`check-source.py --family-only` and `run-native.py --family-only` are explicit **three-complete-file-only** integration probes. They still require the same exact CP92 whole-file inverse, final source hashes, closed ten-site formulas and pinned probe prerequisites. They do not accept additional changes inside those three files or silently project them away. They intentionally make no assertion about unrelated composed production files. A coordinator's independently pinned aggregate must account for such changes; do not relabel this isolated attribution as fresh composed attribution.
