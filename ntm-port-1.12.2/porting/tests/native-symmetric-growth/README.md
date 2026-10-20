# Native symmetric AABB growth: bounded silent-semantics repair

## Application boundary

Baseline is CP64 `27dceb0ef9e5815de3dc5f0494ca43aef9997370`, followed by the already supplied 20-constructor production patch SHA256 `cb9ea2fa3984e38839ab73c04780cd86128e64ab03a0a9e5cd69fd17110af7f3`. That exact prerequisite layer has Git tree `23630f8019bbf0ae17d92b1a430e97c7afd3f243`. Apply this growth patch AFTER that prerequisite. It contains only 32 `expand` → `grow` method-name tokens in 26 existing Java files. It does not repeat any constructor migration. Every receiver, argument, surrounding statement and source newline is unchanged. Main was never edited by this worker.

## Actual semantic finding

Original 1.7.10 expand is `azt.b(DDD)Lazt;`, SRG `func_72314_b`: subtract from each minimum and add to each maximum, then store the six doubles directly. Native stable39 maps that same SRG method to `grow`; native `expand` is instead SRG `func_72321_a`, corresponding to original directional `addCoord`. Actual old/native class bytecode, pinned hashes, SRG records and native MCP rows are retained.

For `[0,0,0 .. 1,1,1]` and `(2,3,4)`, original expand and native grow both return `[-2,-3,-4 .. 3,4,5]`; the old spelling left in native source returns `[0,0,0 .. 3,4,5]`. This is a real collision/search-area semantic defect, not a compile error.

Native grow still sorts the arithmetic result through the native constructor. Therefore a universal rename remains unjustified. Over-shrink can cross bounds; negative zero can reorder zero signs; infinite growth can introduce a one-sided NaN; arbitrary original reversed/unequal-NaN endpoints are not a native box invariant. Unknown overriding AABB subclasses can also change virtual dispatch.

## Exact accepted contract

All 32 calls have genuine compiler-resolved native `AxisAlignedBB.expand(double,double,double)` receivers, three finite nonnegative numeric literals, and an exact original `.expand` provenance match in the same source class/method. Negative zero is explicitly rejected. Thirty calls use existing native entity box reads; two use the previously proven int-local point constructors in MachineFluidTank and MachineRefinery. No constructor proof is broadened.

For boxes constructed by the genuine native AABB implementation, each coordinate pair is ordered, or both endpoints carry the same NaN. Finite nonnegative growth preserves ordering, including finite overflow to infinity; same NaN operands preserve the observed raw payloads on the tested Java8 engine. Zero signs are tested explicitly. Source and 67 resolved dependency jars contain no AABB expand/grow overrides (48,245 dependency classes, only the base AABB hierarchy member). A deliberate overriding subclass is a negative control, not a universal external-mod compatibility promise. No reflection, final-field mutation or new production helper is introduced.

Provenance matching permits only the already-audited receiver spellings: original `.boundingBox` to native `.getEntityBoundingBox()`, the two approved factory-to-constructor expressions, explicit `this` qualification, and ModEventHandler's original `event.entity` accessor migration. All original file bytes match the pristine tree at CP64. The original parser is syntax/provenance evidence; actual native call attribution and actual original engine runtime are separate evidence, not falsely described as a complete old-mod compile.

## Preserved exclusions and history

The before audit records 110 expand calls: 60 native-resolved, with 32 selected and 28 retained. The retained native group includes negative/dynamic deltas and the intentionally directional ParticleSpentCasing movement call. Fifty other expand calls have unresolved receiver chains (38 native getBoundingBox errors, 10 addCoord errors and 2 unresolved factory owners); all remain unchanged. All 28 existing grow calls, including domain methods and the already-native BlockPedestal corrections, remain unchanged.

ParticleSpentCasing's original `boundingBox.addCoord(motionX,motionY,motionZ)` was deliberately ported to native directional expand in commit `66a7b7b0`; its exact history diff is retained. BlockPedestal's prior native grow work was introduced in `b2289c3d` and is also retained. Original addCoord versus actual native expand is separately runtime-checked. No blind spelling rewrite is used.

## Reproduce and limits

From the module:

```
bash porting/tests/native-symmetric-growth/run.sh
python3 porting/tests/native-symmetric-growth/source.py --strict-all
python3 porting/tests/native-symmetric-growth/verify_attribution.py
python3 porting/tests/native-symmetric-growth/attribute.py
python3 porting/tests/native-symmetric-growth/verify_baseline.py
```

Defaults use retained Java8, native classpath manifest and actual original client; JAVA8_HOME, NATIVE_CP_FILE and LEGACY_CLIENT_JAR override them. No downloads. Original jar SHA256 is `a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce`. Only unchanged original azt/azw/azu bytes are temporarily extracted to avoid signed default-package conflicts. Native class hash is verified. No fake engine classes or production stubs.

Results:
- 3,910,874 actual old/native checks: all 32 exact production argument slices, two complete point-constructor/growth expressions, normalized-box raw IEEE boundaries, random bit patterns, intersections and axis offsets, real EntityItem getter bounds, receiver/argument order and null/exception timing. Seven semantic negative controls preserve unsafe differences.
- Four positive and 15 negative compiler controls plus two reachable source-subclass override controls.
- 26 exact whole-file inverses, 52 source mutation rejections, and all 4,062 production input identities/bytes verified against the prerequisite layer.
- Full-source native attribution before and after: 15,858 errors both times, with zero source AABB overrides. All 32 destination bindings are actual native `(DDD)AxisAlignedBB` grow methods. These are unfinished-source attribution runs, not successful Gradle builds.

`source.py --strict-all` is deliberately an isolated layered-baseline guard. Later unrelated overlapping main changes need an exact combined inverse receipt, not ignored hashes. Default diff --check reports the pre-existing CRLF on ItemTurretControl's changed line; `git -c core.whitespace=cr-at-eol diff --check` passes. No newline normalization was performed.

These are native arithmetic/API, provenance and extracted-expression tests, not complete 26-class gameplay execution, world-query acceptance, full collision/lifecycle/renderer execution, client/server startup or world-save proof. No full build success or runnable mod is claimed. Existing withholding/lighting/HUD/attribute decisions are unchanged. All unsafe cases remain excluded.
