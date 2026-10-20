# Native AE2 storage proof (rv6-stable-7)

## Reproduction

Requires the user-supplied full AE2 release JAR, Java 8, Python 3, and the project's existing ForgeGradle toolchain. No dependency is generated or replaced by a test stub.

1. `python3 porting/install-ae2-dependency.py /path/to/appliedenergistics2-rv6-stable-7.jar`
2. Run Gradle with `--offline -I porting/tests/ae2-native-classpath.init.gradle writeAE2NativeClasspath`. On Windows use the existing Gradle launcher and `python` if that is the Python 3 command. The classpath is written as UTF-8 to `build/porting-tests/ae2-classpath.txt`; `-Pae2ClasspathOutput=...` overrides that path.
3. `python3 porting/tests/ae2-storage-native-test.py --java-home /path/to/java8 --classpath-file build/porting-tests/ae2-classpath.txt`

All paths use platform-native separators. The runner supports Java's Windows `.exe` tools. It does not need Git or the original checkout at runtime. The artifact installer leaves its source untouched and refuses a conflicting pre-existing JAR or POM. The local repository is narrowly ignored; do not include the third-party JAR in the patch or deliverable.

Pinned source artifact: 4,062,630 bytes; SHA-256 `e7f7fbf6caaf6206dbd958fb8f9185f874a405289e62ae3e97209b455a89fb1a`. ZIP CRC and internal mod/version/MC metadata verified. This is not an independent signature/authenticity assertion. Requested official file: <https://www.curseforge.com/minecraft/mc-mods/applied-energistics-2/files/2747063>.

## What actually runs

- All six production `com.hbm.handler.ae2` source files compile against real Forge/Minecraft and the actual ForgeGradle-remapped AE2 full JAR. The raw SRG dependency is not also on the test classpath.
- The runner extracts eight exact production helper bodies: MSU total/increase/decrease/change/type; AFL distribute/collect/available. Only surrounding NTM state, recipe lookup, proxy resolution, and the electrode item are controlled fixtures. Native ItemStack, NBT, TileEntity, Forge capability/events, AE2 API, channel, item-list, IMEAdaptor and MEMonitorIInventory are real classes, not source replacements.
- Genuine Forge `ModCandidate` discovery/ASM metadata and `ModAPITransformer` remove unavailable optional AE2 interfaces in memory as the actual loader does. No dependency JAR is rewritten. The test runs in Forge's real server-side thread group, initializes the real AE2 configuration/API, and exercises its actual storage implementation.
- Real `appeng.capabilities.Capabilities.register()` creates the capabilities. The headless test obtains the registered capability from Forge's registry and assigns AE2's public capability field explicitly; it does not claim to test Forge's entire annotation injection lifecycle.
- Presence/attachment tests invoke the real handler with genuine `AttachCapabilitiesEvent<TileEntity>` objects directly. A separate process/classloader removes the entire AE2 JAR and rejects all `appeng.*` requests, executes the genuine Forge mod-presence guard, and asserts zero such requests. Loading an AE2 adapter is the negative control and fails as expected. NTM adds no globally scanned capability-injection annotation referring to optional AE2.

## Results

80 native assertions pass, plus the independent no-AE2 classloading proof. The suite also passes from a 13-file temporary copy outside Git (actual `git rev-parse` exits 128), without the pristine checkout. See `../continuation-2026-10-19/ae2-native-portable.log`.

The installer safety test is reproducible with `python3 porting/tests/test-ae2-dependency-installer.py --jar /path/to/appliedenergistics2-rv6-stable-7.jar`; it passes valid/idempotent install, conflicting JAR/POM refusal, wrong input refusal, and unchanged source hash. Independent read-only review compared the frozen production diff with pristine code and real bytecode and found no semantic defect.

MSU: reservoir/input/output scan order; exact capacity including both buffers; native max-stack sizes 1/16/64; wrong item/damage/NBT refusal; matching NBT retained in new buffers; incompatible buffers neither counted nor overwritten; native EMPTY filter/buffers; partial and complete extraction; AE null protocol; simulation leaves state, dirtiness and caller unchanged; ordinary lock/output behavior is not imposed on the original custom ME path. Original int-cast insertion and long-minus-int extraction anomalies are deliberately pinned at the direct adapter boundary, not silently corrected. These anomaly tests do not claim that every full live storage-bus/monitor conversion path exposes the same value.

AFL: three single electrode slots; electrode/input exclusion; solid output-ratio and liquid caps; machine input cap; partial merge and incompatible-slot skip; slots 5–24 only for recipe insertion; no visibility of 3/4/25+; partial extraction order; closed-lid custom access; simulation and EMPTY behavior.

Capability/monitor: exact item-only channel, all six faces and null, separate monitors per source/accessor request, native selected-type priority, immediate execute notifications and source, real monitor tick notices external stockpile change, simulate produces no notification, valid stable core identity, unresolved/unrelated/MSU proxy exclusion, AFL proxy access independent of ordinary proxy inventory flag, invalid core/direct tile removal, changed-core identity replacement, detached-core removal, correct attachment target scope.

## Deliberate limitations

This is not a whole-machine compile or an in-game test. The project retains unrelated legacy errors, and these tests do not bootstrap the full NTM registry/world/client.

The headless AE2 bootstrap logs native `NoSuchMethodException` event registration warnings for unrelated AE2 configuration/world handlers because Forge's full event-subscription launch transformation is not installed in this isolated loader. Those warnings are retained in the log rather than hidden. Direct attach invocation proves handler scope and provider behavior, not complete event-bus dispatch. Full Forge launch/attachment, save/load, client visuals and a real ME network require later integrated smoke tests.

`TileEntity.invalidate()` and a missing/replaced proxy core are tested. They are not mislabeled as a chunk-unload test: native Forge `onChunkUnload()` is distinct, and NTM's real loaded-base/proxy resolver is outside the fixture. Real chunk unload/reload and world neighbor invalidation remain integration tests. Existing storage-bus polling is source/API supported and its monitor is exercised, but an entire live PartStorageBus graph is not instantiated.

No Inventory Tweaks replacement, dependency mirror, generic sided-inventory downgrade, new lock/permission rule, balancing change, or external API stub was added.
