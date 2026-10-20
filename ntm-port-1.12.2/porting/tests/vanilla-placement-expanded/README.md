# Native vanilla placement expansion

Pinned baseline: CP52 e0d436953a26ba3e7cb58effa31adc48282eb501. Finite scope: 818 placement statements in 55 production files. The largest owners are GeyserLarge (468) and Geyser (119). One ArcticVault loop-local SNOW_LAYER call is included. No custom metadata owner or public virtual random helper is migrated.

## Semantics

The actual Java 8 compiler attributes every selected receiver to native net.minecraft.world.World, every selected constant to a static final field owned by native net.minecraft.init.Blocks, and coordinate/metadata/flag arguments to primitive int. Parse success is required; unresolved surrounding port errors remain expected, not suppressed as successful compilation. Ten adversarial fixtures verify native/effectful/qualified ownership and reject foreign, shadowed, dynamic, unresolved, used-result and comment/string cases.

Each replacement is a fresh scoped block. Receiver, X, Y, Z, block, metadata and flags are captured exactly once in original Java evaluation order. Null receiver rejection follows all argument evaluations, even at invalid coordinates. Original X/Z [-30000000,30000000) and Y [0,256) checks precede BlockPos allocation and state conversion. No helper or compatibility API is introduced. The actual native call's return remains ignored. Numeric overflow, array/getter exceptions, mutable receiver storage and flag values are preserved.

The original and native actual registries agree on selected block IDs, and every selected metadata value round-trips through the real native owner. Fifteen invalid/non-round-tripping placements remain untouched, including chest 0, torch 0, nether wart 5, diamond block 5/8 and lit lamp 5. Native engine world/debug/lighting/notification behavior remains native, not an assertion of whole-engine identity.

## Proof boundaries

All 818 exact generated native statements compile against real Minecraft/Forge types without platform stubs or collaborator stand-ins. Test extraction groups each file into a single method, including all 468 GeyserLarge statements, so per-method size is not hidden by test splitting. Independent baseline expression evaluation is compared to native recordings over nine ordinary, negative, boundary, invalid-height and overflow contexts. The oracle executes the exact first 23 instructions of the pinned original ahb.d placement bytecode through its pre-chunk bounds decision; it does not replace that decision with a handwritten predicate. It stops at the chunk boundary, so this is not full original World execution.

There are 3,343 ordered ID/metadata/flag/coordinate events and 1,042 checks. Adversarial evaluation probes check every receiver/argument exception point, null world timing at valid and invalid positions, RNG state, ignored true/false returns, and receiver-field mutation during coordinate evaluation. Coordinate variables are fixture inputs; surrounding loops, conditionals, class collaborators, RNG control flow, chunk writes and entire generators are NOT certified. The pre-existing dungeon native probe separately tests real native World/Chunk state writes and notifications.

`selection.json` is the exact fragment manifest, `sources.zip` and `source-hashes.json` pin attribution inputs, and `mapping.tsv` records actual registry/state results. Verification rebuilds edits from pinned bytes and demands exact production output. `merge.py` permits later unrelated edits only when each selected source line is identical; it verifies an exact inverse before writing and emits a receipt. A changed placement interior is rejected for explicit composition, never overwritten silently.

Dungeon exclusions are intentional: 414 getBrick and 34 getShelf calls dispatch through public virtual helpers. Concrete current implementations return bounded vanilla states, but subclasses can override them; this patch does not silently narrow that API. The 2,512 random-concrete calls and 4,324 other custom/dynamic calls still lack owner-state proof. Seven dungeon invalid metadata cases remain. The one valid loop-local snow placement is now migrated.

## Reproduce

From the module, set J to the supplied Java 8 installation, CP to the real native classpath, and LEGACY to the pinned original client JAR. No downloads or full Gradle build are required:

    python3 porting/tests/vanilla-placement-expanded/ownership.py --java-home "$J" --classpath "$CP"
    python3 porting/tests/vanilla-placement-expanded/migrate.py --java-home "$J" --classpath "$CP" --legacy-client "$LEGACY"
    python3 porting/tests/vanilla-placement-expanded/prove.py --java-home "$J" --classpath "$CP" --legacy-client "$LEGACY"

`--apply` is only permitted on exact pinned CP52 source bytes. `merge.py --module PATH` checks later-tree fragment applicability; adding `--apply --receipt PATH` applies only those fragments. Historical full-output manifests need composition with that receipt, not wholesale replacement or weakened assertions.

Original client SHA256: a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce. Only the coordinator runs the full compile. No whole-mod build, gameplay or launched-world claim.
