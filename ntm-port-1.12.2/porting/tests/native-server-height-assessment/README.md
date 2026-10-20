# Evidence only: exact-owned server height feasibility

NO PRODUCTION ACTIVATION. This increment contains only porting/tests evidence. The accepted three-client WorldHeight increment is unchanged. The baseline is isolated commit 2ba3efd5 (full hash in source-inputs.json).

## Finding

A real, non-mutating native route exists for normally absent or present-nonnull columns owned by exact WorldServer/WorldServerMulti and exact ChunkProviderServer:

1. Preserve original null and horizontal bounds; outside returns64 without touching provider state.
2. Obtain the exact native server provider and confirm provider.world is the same world.
3. Test public final loadedChunks.containsKey(key). A normally absent key returns0, without loading or creating anything.
4. Read loadedChunks.get(key), then invoke that actual Chunk's virtual getHeightValue(x&15,z&15).

Neither queue representation is written, restored or suppressed. The engine's getLoadedChunk/provideChunk functions are deliberately NOT called for a loaded column because they clear unloadQueued. The actual state-owner Chunk method is retained, including a custom Chunk subclass's virtual method dispatch.

OwnedHeightCandidate.assess returns OptionalInt solely to report experimental capability. Empty means this proof does not establish a result; it is NOT a proposed production failure/fallback policy. No general World method emulation or policy replacement is added.

## Original call graph and load behavior

Actual original ahb.f(int,int), mapped to World.getHeightValue, bounds-checks then calls ahb.c -> provider.chunkExists. False returns0. True invokes ahb.e -> ms.d/provideChunk -> actual Chunk.b/getHeightValue. Normally present nonnull chunks return without modifying the pending-unload set. Therefore a normal missing column is NOT a load-if-missing operation.

There is a real exceptional path: an existing map key with a null value passes chunkExists, but original provideChunk then enters loadChunk (depending on its load-on-request settings). In the tested real original provider with the default enabled setting and null generator, it installs a genuine EmptyChunk and removes that key from the unload set. The candidate explicitly declines this present-null state rather than misclassifying it as absent. A race between containsKey/get is likewise not certified. The candidate guards the owner classes and matched provider identity; it does not purport to prove arbitrary concurrent or externally corrupted map invariants.

Original mappings and bytecode are retained. The authentic original Forge1614 userdev FML and Forge provider patches are included. FML adds a world-generation callback; Forge changes loadChunk, queue/spawn and unload/dormant behavior. The normal existing-value provideChunk/exists path used here is not rewritten by those patches. Runtime original-provider assertions execute the actual signed original vanilla client classes; they are NOT represented as execution of a fully transformed Forge1614 server. Exceptional load and forced/dormant lifecycle paths are not accepted by this experiment.

## Ownership/exclusion findings

- Exact native WorldServer and WorldServerMulti pass; WorldServerMulti adds no height/provider-query overrides in the inspected actual class.
- A custom WorldServer subclass is declined before its method is called. A custom ChunkProviderServer subclass or mismatched provider.world is declined.
- Client worlds are declined by this server experiment. The prior three client callsites remain separate.
- The candidate checks containsKey before get, so an altered map default return value cannot fabricate an absent column.
- A custom Chunk instance still receives its virtual height query once, preserving owner dispatch instead of reading a private height-map field.
- No actual production callsite has yet been assigned this exact runtime ownership or threading contract. All56 server consumers stay unchanged. Their activation/fallback decisions remain with the parent.

## Tests

The probe performs12,195 experiment assertions plus1,521 reused actual-original server assertions. It tests two exact native world types, four sea levels, all horizontal boundaries, absent/empty/filled chunks, no-create behavior, and unchanged actual droppedChunks/unloadQueued state. Both original and native queue APIs are called to establish an actual queued example; candidate reads preserve it, while native getHeight demonstrates the prior flag-clearing counterexample. Test setup writes maps/queue fields as fixtures; the candidate contains only reads.

Six mutations must fail: use getLoadedChunk, remove World ownership gate, remove provider ownership gate, ignore containsKey, treat present-null as zero, and transpose coordinates. Supporting source hashes are checked. No stubs, excluded production sources, new dependencies, full-game claims or production compilation claims.

Reproduce from module root:

    python3 porting/tests/native-server-height-assessment/run.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)" --original-client /tmp/nagaport-legacy-client.jar --mutants

## Decision boundary

This establishes a feasible read-only primitive for the exact native owner and normal map states above. It does not approve extension of WorldHeight, silent fallback for foreign/custom owners, present-null/race handling, or activation of existing server consumers. The client patch remains frozen and unchanged.
