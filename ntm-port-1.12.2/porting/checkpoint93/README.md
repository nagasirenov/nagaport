# Checkpoint 93: native state queries and closed normal-bearing render streams

Based on CP92 0675e599e5ee31a60776b560af792372df191790. Fresh offline full compilation **FAILS with 10,164 diagnostics**, down **202** from 10,366. All **4,071 generated Java inputs** byte-match frozen production: **49 existing Java files**, no new production files. Line-insensitive comparison finds 202 removed reports and zero newly introduced reports. This is not a successful mod build, playable JAR or launched client/server.

## Closed render streams with normals

Six owners contain thirteen closed sites and 88 vertex expressions: Excavator, Chemical gas/fire/antimatter, RadarScreen, Celestial and Thatmo smoke/glow, and Aberrator first-person rendering. Actual required POSITION_TEX_NORMAL / POSITION_TEX_COLOR_NORMAL formats and companion POSITION_COLOR batches replace the legacy streams. No normals, UVs or lightmap attributes are invented. Escaping/borrowed streams and FurnaceSteel's UV1-only issue remain outside this family.

180 paired complete-region executions compare 477 batches and 2,688 vertex records through 954 nonempty actual original/native Mesa submissions. Signed normal bytes, XYZ/UV/color, tracked current normal/color, arrays, texture/matrix state and RNG counts match. Six complete-file inverses and 145 source mutations pass. These bounded extracted-region proofs do not claim complete renderer linkage or in-game pixels. The exact 141 CP92 legacy-stream diagnostic sites are retained in the evidence.

## Native normal-cube state dispatch

Forty-seven queries use actual native state dispatch: nineteen inline reads, six reused previously captured air states, and twenty-two calls from twenty new captures. Existing Block reads and pre-mutation/explosion snapshots remain at their original positions; no guessed default state or extra read is inserted. PowerDetector's constant-true capability callback also acquires its genuine native state signature so downstream normal-cube decisions do not accidentally inherit false.

Thirty complete-file inverses, ninety mutation controls and all 47 exact receiver/native-method attributions pass. 11,137 bounded assertions cover 5,485 actual registry states, complete actual BlockNTMGlass and BlockBeamBase, AEBaseBlock's distinct one-argument versus contextual dispatch, genuine Forge extended states, the original predicate matrix and explicitly isolated PowerDetector projections. BlockMutatorBulkie's Block-parameter case, Autosaw's contextual overload, mismatched OilWell replaceability and unrelated placement/hardness work remain excluded. Exact owner closure and projection limitations are documented in the family evidence.

## World air-query coordinates

Fourteen calls in thirteen files pass native BlockPos(int,int,int) to the actual World.isAirBlock API. Receiver, integer expressions/overflow and all surrounding source bytes are unchanged. Whole-source attribution proves the native World method and integer constructor, rather than relying on textual name matching.

82,283 checks cover actual native World dispatch over 5,485 registry states, coordinate extremes, state-sensitive hooks, null/throwing argument order and short-circuiting. The fixture uses genuine World but skips its unrelated constructor and supplies state access; it does not prove real chunks or world lifecycle. The platform boundary is explicit: original Forge read one Block before its hook; native World reads two states, selecting the first Block and passing the second state. This is preserved native behavior, not emulated old-engine equivalence for custom worlds that mutate during reads. Thirteen full-file inverses and fourteen omitted-wrapper controls pass.

## Evidence composition and remaining work

Prior tool/armor evidence composes the exact hash-pinned World-air inverse for ItemToolAbility before its unchanged historical attribute checks. No historical source hashes or modifier census is repinned. The CP92 air guard composes six exact CP93 files and retains its historical manifest/checker byte-for-byte; eighteen new mutation controls and all 42 historical controls pass. The press-cache audit composes exactly six affected CP93 inputs before prior layers, preserving all 518 occurrences and historical hashes, with eighteen new mutation controls. Genuine dependency archives remain byte-pinned; only the two inert dummy ZIP containers are pinned by their sole dummyThing entry and payload rather than regenerated ZIP timestamps. No class content is ignored.

CP92 is durably preserved in Library version20. Published GitHub main still remains CP75 eefb4c1f6f344e4fcb15efb7e6cb6a370419a570; no new push is authorized. Cumulative packages target that exact clean baseline and replace earlier recovery archives. Stop on overlap, conflicts or CRLF refusal. Never force/reset, automatically normalize or overwrite dirty user files. Windows execution remains untested.

The shared item-backed inventory migration is separate ongoing work, not included or claimed complete here. Full compilation, startup, machinery/energy/radiation, saves and network integration remain unfinished. Prior explicit compatibility gates, isolated potion policy and unresolved dependency regeneration remain in force. No proprietary Minecraft binaries are redistributed.

## Reproduce

From ntm-port-1.12.2, using the verified existing offline cache:

```sh
/workspace/shared/nagaport-toolchain/run-gradle.sh --offline -I /workspace/shared/nagaport-toolchain/restore-official-repositories.init.gradle -I porting/compile-diagnostics-expanded.init.gradle clean compileJava --console=plain
python3 porting/checkpoint93/verify-build.py
python3 porting/checkpoint93/compare-diagnostics.py
python3 porting/tests/check-thermal-family-source.py
python3 porting/tests/native-world-air-query/check_source.py
python3 porting/tests/native-world-air-query/run.py
python3 porting/tests/native-tool-attributes/check_source.py
python3 porting/tests/native-armor-attributes/check_source.py
python3 porting/tests/remaining-normal-batches/check_source.py
python3 porting/tests/native-normal-cube-queries/check_source.py
python3 porting/tests/native-block-air-captures/check_cp93_composition.py
python3 porting/tests/legacy-press-promotion/cache-audit/scan.py --compose-colored --compose-replaceability --compose-cp92 --compose-cp93
```

See family READMEs for full bounded native commands and exact prerequisites. The combined build completed normally in 46 seconds; no denied poll or retry occurred for CP93.
