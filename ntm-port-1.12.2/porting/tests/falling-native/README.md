# Portable native falling/anvil proof

Run from any directory:

    python3 /path/to/falling-native/run.py --module /path/to/ntm-port-1.12.2 --java-home /path/to/jdk8 --classpath "$NATIVE_CLASSPATH"

Optional `--original /path/to/ntm-original-pristine` verifies every pinned original source and asset. Without it, the immutable extracted oracle is used: OriginalAtlasOracle.java contains the original named-part shader and per-metadata rotation code extracted verbatim into a standalone native helper; skins.txt contains all twelve independently extracted original texture assignments; original-sha256.json pins source provenance and every actual module OBJ/texture used by the test. The runner pins original falling physics constants, constructor asymmetry and byte Time, and tests their behavior. NativeAnvilTest uses an independent ordered OBJ parser rather than production geometry parsing for its expected stream. The actual module OBJ/texture hashes must equal the pristine oracle before tests run.

The bundled original-derived oracle code is covered by the included upstream LICENSE and LICENSE.LESSER. Source provenance: Hbm-s-Nuclear-Tech-GIT / ntm-original-pristine; exact upstream files and SHA-256 are in original-sha256.json. No compiled classes, third-party jars or native libraries are bundled. `manifest.sha256` protects all bundle contents except itself. The supplied module provides all 18 real production classes and resources. The runner compiles to a fresh temporary directory, extracts existing native libraries only from the supplied classpath and deletes outputs afterward. No historical staging directories, cached classes, repository-relative sibling paths or network access are needed.

Exactly four isolated compile fixtures: MainRegistry (instance), ITooltipProvider (signature), ContainerAnvil and GUIAnvil (constructor/tier). Never copy fixtures into production. No Minecraft, Forge, selected owner/model/entity contracts are replaced. Tests establish real block callbacks and exact GUI factory tier arguments, NOT working inventory/crafting/GUI or live GL acceptance.

`--smoke` compiles all production/test source and runs 109 falling assertions plus 41 model-registration assertions, without rerunning the equivalent exhaustive 775,378-assertion anvil geometry test. The full equivalent previously successful run is retained as full-equivalent-test.log; anvil-owner-isolation.log is the later exhaustive rerun adding four unknown-owner compatibility assertions. Default invocation runs everything. relocation-smoke.log records the relocated portable runner check.
