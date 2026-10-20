# Native client-only height queries

Exact isolated base f0e992aa (full hash in manifest.json): CP59 plus the preceding 123-block-read increment. Production changes ONLY three height calls in EntityNukeTorex and GUIScreenSatInterface, plus the narrow NTM-owned WorldHeight boundary. No metadata calls, server consumers, engine overrides, queue writes/restoration, debug-world promises, or direct chunk-map reads in production.

## Accepted client scope

Two Torex calls occur inside the true branch of `if(this.world.isRemote)` in onUpdate. Full-source destination AST attribution validates that ancestry, not merely a string match elsewhere in the file. A true/negated/unguarded fixture proves the scope checker distinguishes branches.

The remaining call is in private GUIScreenSatInterface.drawMap, invoked by drawScreen. The original full-source constructor audit finds exactly one owner: ItemSatInterface.provideGUI, annotated SideOnly(CLIENT); its onItemRightClick opens this GUI only when world.isRemote. The exact original source hashes and constructor inventory are recorded in client-source-audit.json. The entire two modified classes invert byte-for-byte, and the unchanged native GUI provider is separately hash-guarded. Arbitrary external direct construction with a server player/world is not certified.

WorldHeight.read checks null AFTER caller coordinates evaluate, retains X/Z [-30000000,30000000) and original outside sentinel64, and delegates in-range to genuine native World.getHeight(int,int). The native world returns seaLevel+1 outside this range, so a bare rename would not preserve original behavior. The accepted client provider has no server unload queue; genuine original/native client absent chunks both use an EmptyChunk with height zero.

## Rejected server scope, not activation permission

The initial scanner found59 exact-World/int candidates. The other56 remain byte-identical and are inventoried in excluded-server-scope.json. Actual original ahb.f(int,int) checks ahb.c -> provider existence, then ahb.e -> ms.d; loaded ms.d returns its chunk without removing a pending unload entry. Native World.getHeight -> getChunk -> ChunkProviderServer.provideChunk/loadChunk/getLoadedChunk sets Chunk.unloadQueued=false for a loaded chunk. The engine-boundaries.txt disassembly records relevant original/native implementations.

A reachable original-owned example is SatelliteLaser.onClick(World,int,int), whose arbitrary target column is not proved to have no pending unload. The test deliberately queues the loaded original/native chunk and observes the differing state after equal height results. Server runs characterize this rejected family; they do not grant server use of the helper. The public native getLoadedChunk route itself changes the flag; no alternate non-mutating public provider route was established. Direct map access and queue-state suppression/restoration are not implemented. Existing earlier WorldBlockState lookup lifecycle differences are not used to approve this new scope.

## Evidence and reproduction

From module root:

    python3 porting/tests/native-world-height/check_source.py
    python3 porting/tests/native-world-height/attribute.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)"
    python3 porting/tests/native-world-height/probe.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)" --original-client /tmp/nagaport-legacy-client.jar --mutants

17,769 assertions execute actual original/native WorldClient, WorldServer, real client/server providers, Chunk and EmptyChunk. Tests cover all horizontal boundary classes, negative coordinates/local mask indexing, changed sea levels including overflow, absent/empty/filled column maps, no absent-chunk creation, client queue-field invariance, and the explicit rejected server lifecycle counterexample. Height maps are initialized to distinguish X/Z indexing; this does not claim height-map generation parity. Unsafe bypasses unrelated World constructors, while real providers and chunks are constructed and original signed classes are unchanged. The test's original/server provider maps are populated as fixtures, not used by production code. No mock World/provider implementation or platform stub is used.

Actual original direct invocation tests prove receiver/coordinate/null/argument-exception order. Two accepted/eight excluded attribution fixtures, true/negated/unguarded branch fixtures, and six boundary/sea-level/null/coordinate/height mutants pass. Full-source before/after javac attribution proves all three original exact-World/int callsites and native helper overloads, with exact receiver and coordinate AST spans. Two complete affected source files invert byte-exactly, preserving unrelated edits. Helper and GUI-provider hashes are fixed.

The full-source attribution diagnostics (17,385 before;17,382 after) are unfinished-port evidence, NOT a clean build. No complete affected class, renderer, height-map generation, world-save or game-launch test is claimed. Existing CP37 gates remain unresolved.

For integration use merge.py --target MODULE [--apply] to preflight all exact invocation fragments; copy the new helper exactly. Preserve historical manifest baselines. Do not widen production use to any of the56 rejected candidates.
