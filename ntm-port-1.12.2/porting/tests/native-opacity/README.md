# Native constant-false opacity callbacks

Exact base: CP67 `d860257bb1f4bc6043d5cb80bd752ede1ebc53bd`.

## Production scope

95 existing Block-owner methods now accept the genuine native `net.minecraft.block.state.IBlockState state` parameter. Their literal `return false;` bodies, annotations, whitespace/CRLF, class hierarchy, constructors, registry/resource names, render callbacks, full-cube callbacks, collision callbacks and every other byte remain unchanged. No helper, legacy Block facade, new field, texture/model route, normalization or policy choice is introduced.

Inventory found 103 old no-argument Block methods: 102 false and one true. Eight methods in the existing BlockDummyable native-to-legacy virtual bridge closure are deliberately unchanged, including RBMKBase's true result and RBMKRod's false specialization. All 95 selected false results are independently present in the original pristine 1.7.10 owner sources. This is not a blanket opacity rewrite or a claim that any selected block is an ordinary full cube.

## Hierarchy and dispatch

Full-source Java8 attribution covers all 4,063 units and all 159 opacity declarations. All 95 migrated methods genuinely override native Block.isOpaqueCube(IBlockState). The complete selected source descendant closure contains 114 named owners; none of the 19 additional descendants adds an overlooked old/native opacity override. Its ancestor paths resolve fully through native Block, BlockContainer or BlockLeaves. All 552 anonymous classes were checked; none derives from this closure. Every existing opacity invocation has byte-identical before/after source and attribution. Unresolved arbitrary Block/IBlockAccess legacy call sites remain honestly unresolved, not converted to guessed default-state queries.

The only custom state factories in the selected ancestor/descendant closure are BlockChargeBase and BlockNTMFlower. Both return genuine BlockStateContainer instances, and their exact property declarations and factory bodies are tested. No selected method reads state, metadata, fields, globals or world, calls another method, throws, or causes a side effect.

Pinned actual engine bytecode and runtime checks explain why this restriction matters. Original Block constructor invokes its virtual opacity callback twice, initializing full-block and light-opacity fields; native Block invokes its default state's callback once and caches fullBlock/lightOpacity. Genuine StateImplementation.isOpaqueCube subsequently dispatches virtually each time, rather than caching a per-state opaque result. A stateful callback counterexample produces two old calls versus one native call. Constant false is safe through both initialization orders because it reads no uninitialized owner state and performs no effects. Native BlockLeaves is separately dynamic in its parent implementation; the explicit original RubberLeaves false override remains false for either graphics setting, while native leaves' explicit light opacity of one is retained.

## Results

- Full-source diagnostics: 15,191 before, 15,102 after. Exactly 89 obsolete override diagnostics disappear; no other diagnostic message changes and none is added. Six selected old callbacks lacked an override annotation diagnostic but also required native dispatch restoration.
- 31,009 finite original/native assertions exercise all 95 exact callback bodies and inherited dispatch against every combination of a genuine 16-value integer property and Boolean property; constructor caches, null/foreign state arguments and full-cube/collision independence are checked.
- Six actual constructor/callback slices compile against native Block, BlockContainer and BlockLeaves: WaterPlant, Plasma, Vacuum, WasteLeaves, TurretSentry and RubberLeaves. Both custom state factory/property slices also compile and execute.
- Three compiled runtime mutants fail: retaining the old signature, returning true and adding state dependence. A compilation failure does not count as a mutation kill.
- All 95 full files invert exactly to baseline. Forward/reapply/reverse fragment replay and independent-edit composition controls pass. All 4,063 source hashes bind the isolated audit, along with exact original-source provenance and genuine engine class hashes.

Fixtures are explicitly bounded, not full production owner compilations. The generic callback fixtures use native Block solely to execute each exact body through real state dispatch. Representative constructor fixtures omit unrelated gameplay callbacks. Required unexercised tile/shearing/wood-type methods throw AssertionError rather than fabricate success. Original signed Minecraft classes are unchanged; only generated legacy fixture class names are relocated to a separate package to avoid signed default-package collisions. No fixture is shipped in production.

No full game, complete lighting or pixel parity, complete collision/rendering callback migration, external-mod subclass equivalence, radiation, saves or full-mod compilation is claimed. In particular, existing legacy render/full-cube/collision APIs remain untouched and may still be independently unported. This restores the original false opacity dispatch; it does not certify all other rendering behavior.

## Reproduction and composition

From the module, using existing cached inputs only:

    python3 porting/tests/native-opacity/run-attribution.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /tmp/nagaport-ae2-native-classpath.txt
    python3 porting/tests/native-opacity/check-source.py
    python3 porting/tests/native-opacity/check-replay.py
    python3 porting/tests/native-opacity/check-engine.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /tmp/nagaport-ae2-native-classpath.txt --legacy-jar /tmp/nagaport-legacy-client.jar
    python3 porting/tests/native-opacity/run-native.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /tmp/nagaport-ae2-native-classpath.txt --legacy-jar /tmp/nagaport-legacy-client.jar
    python3 porting/tests/native-opacity/check-mutants.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /tmp/nagaport-ae2-native-classpath.txt --legacy-jar /tmp/nagaport-legacy-client.jar

The isolated full-source guard is not silently rebased after another batch. For composed integration, dry-run `apply-fragments.py --root TARGET_MODULE`, then add `--apply --receipt RECEIPT_JSON`. It prevalidates every exact method before writing and preserves unrelated activation/drop edits. `--reverse` removes only this batch, and replay is idempotent. The coordinator owns integration and the combined native build. Main was never edited.
