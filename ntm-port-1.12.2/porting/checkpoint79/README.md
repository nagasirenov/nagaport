# Checkpoint 79: native projectile data, remaining storage, world reads and hazard cubes

Baseline: sealed CP78 `89b6bfc71a8b532c2fe7cdbd43bb66b7991ee365`. Published remote main remains CP75 `eefb4c1f6f344e4fcb15efb7e6cb6a370419a570`. No push. Cumulative delivery replaces earlier post-CP75 archives, not a patch to stack on them.

## Current result

Fresh clean OFFLINE full compilation: **FAIL 12,686 diagnostics**, versus 12,775 at CP78, 89 fewer. All 4,065 generated Java compiler inputs match current production source bytes exactly. These are diagnostics, not distinct bugs or a progress percentage. There is no runnable JAR, successful whole-mod build, launched client/server or integrated gameplay claim.

29 production Java files change in four bounded families. No source exclusions, dependency changes, balance changes or resource deletions.

- Four projectile owners now use owner-local native VARINT data keys. Seventeen old watcher operations and the artillery renderer raw-ID consumer migrate together. Existing defaults, init ordering, omitted rocket super registration, Sedna off-by-one exception/cache behavior, raw renderer index failure and scalar NBT remain. Actual native metadata/packet/NBT tests pass 658 checks in each of two JVM load orders. These execute 54 pinned source slices, not whole owners: ticket return is explicitly assumed, several constructors are synthetic harness boundaries, full inherited NBT/gameplay is excluded. See `tests/native-entity-projectile-config/README.md`.
- Nineteen nested item-NBT loaders in fourteen files use the existing unchanged nullable storage helper. Native selected-method checks pass 387 checks plus the prior helper's 191. Loot/skeleton/piston persistence/minecart/crane/particle storage and bounded rocket/artillery/IMC records preserve original null/invalid/failure behavior. This does not certify all surrounding classes, IMC recipe registries, machine slots or radiation. Three deliberately excluded loaders remain: piston and press custom-tag wire payloads, plus unproven external AE2 record/count import. See `tests/remaining-nullable-nbt-native/README.md`.
- Thirty-one remaining world reads in nine files migrate according to their actual owners and lexical scopes. No radiation formulas, chunk loading or lifecycle policy changes. Entity/tile/map-generator fields use genuine native fields; ten Chunk reads use the actual trivial native getter after finite source/dependency subclass audit. Unknown future mod getter overrides are not asserted equivalent. Three protected external provider accesses remain deferred. Native/original/evaluation/attribution checks are bounded, not radiation/world-system acceptance. See `tests/remaining-world-native/README.md`.
- Twenty-six exact BlockHazard/BlockHotHazard cube texture registrations use the existing native cube registry, with only those two exact owners admitted. All 26 PNGs match original bytes. Six-face/atlas/reload/raw-metadata native model checks pass, including actual Mesa GL texture upload/readback. The original ItemBlockBase icon/color delegation is explicitly audited. No gameplay/particle callbacks changed; headless models are not launched-game visual acceptance. See `tests/plain-cube-hazards/README.md`.

## Overlap and evidence

The Shell metadata and cargo-loader patches overlap only import context. The cargo import and load invocation were composed as two exact byte fragments; `shell-integration.json` records the reversible merge. Both the 54-slice native projectile probe and strict cargo read/write/setCargo/groundDespawn guard pass on the combined tree. Do not replace isolated historical whole-file hashes to conceal composition: the new combined boundary reconstructs all 29 CP78 files exactly, while each isolated proof retains its declared baseline.

The complete historical source guard passes through the new line-based boundary, including mutation rejection. Whole-file reversal proves preservation of unrelated source, not correctness of already-unported APIs.

## Reproduction and continuation

From `ntm-port-1.12.2`:

    /workspace/shared/nagaport-toolchain/run-gradle.sh --offline -I /workspace/shared/nagaport-toolchain/restore-official-repositories.init.gradle -I porting/compile-diagnostics-expanded.init.gradle clean compileJava --console=plain
    python3 porting/tests/check-thermal-family-source.py

Java 8 and genuine native classpath remain in the restored toolchain; `/tmp/nagaport-ae2-native-classpath.txt` lists them. Family READMEs document exact probe commands and limitations. No proprietary Minecraft binaries are distributed. No ModMaven or synthetic deobf network lookup is authorized.

Next work must use current compiler families rather than historical README counts. Major remaining roots include native block-state placement/callbacks, item quantity and null/EMPTY lifecycle, graphics API bodies and remaining entity synchronization. Sound/position and chunk lifecycle compatibility gates remain explicit; no silent semantics changes to reduce counts. Preserve machine/energy/recipes and inventory/dropped-item radiation acceptance as unfinished integration goals. Seal each cumulative checkpoint durably before relying on local storage; no new push without user authorization.
