# Recovered native train entity-data tranche

## Recovery and current validation status

The original execution workspace disappeared after the first implementation. The original evidence files and logs were not recovered. This directory is newly reconstructed evidence, not a copy of the originals.

The three-file production patch is proven byte-identical to the originally frozen patch by SHA256: `67337912237ebefe7477644873b6300f620c40e1a519a9c51fbb2a864dfd6a2e`. It contains eighteen watcher operations and six owner-local keys. The recovered CP76 production baseline tree is `b51f8971c483858304306df2f0cbbafade512a77`. Each actual source's before/after bytes are pinned separately in source-manifest.json; validation does not require the lost original CP76 commit. When the recovered tree object is available, audit additionally compares its exact bytes; otherwise the independently recorded baseline byte hashes remain authoritative.

Fresh current source audit: PASS. After authorized restoration of the genuine native classpath, the unchanged reconstructed probe freshly passed on 2026-10-20: 196 assertions in train-first and 196 in dummies-first, separate JVMs, exit0. No production or test corrections were needed. See validation.log for the current actual output. Earlier attempts were blocked by missing dependencies, and the original pre-reset logs remain lost; neither is substituted for this fresh verification. No dependency download was performed by this tranche.

## Owners and native registration order

- Base.BoundingBoxDummyEntity independently extends native Entity. Original slot3 Integer0 → TRAIN_ID/VARINT; slot4 Float1F → WIDTH/FLOAT; slot5 Float1F → HEIGHT/FLOAT.
- Ridable.SeatDummyEntity independently extends native Entity. Original slot3 Integer0 → TRAIN_ID/VARINT; slot4 Integer0 → SEAT_INDEX/VARINT.
- Electric extends Ridable → Cargo → Base → Entity. Original slot3 Integer0 → POWER/VARINT. Existing unchanged Cargo already registers OCCUPIED_SLOTS, and Electric retains its super.entityInit call before registration.

The native API's six Entity keys occupy 0–5. The dummy keys therefore allocate independently at 6/7/8 and 6/7, while Cargo's key6 precedes Electric's key7. The former literal slots 3/4/5 must not be reused as native IDs. Static keys belong to the declaring entity class, not an enclosing class, runtime subclass, shared helper or fake watcher.

Native Entity creates the manager, registers its six own keys, and calls virtual entityInit before subclass field initialization and constructor bodies. Bounds' actual constructor invokes setSize only after this chain returns, so WIDTH/HEIGHT must already be registered. The reconstructed test checks this with real Entity constructors and a sentinel observing Cargo/Ridable fields before allocation. No null-manager guard, duplicate registration, key-ID constant or production compatibility shim is introduced.

## Preserved behavior

Only imports, static typed declarations and the eighteen watcher operations change. The inverse-source audit reconstructs every byte of the three pre-change files. It also compares the exact eighteen original watcher invocation expressions to pristine 1.7.10 source. Constructors, defaults, wrappers, branches, movement/interpolation formulas, rendering positions, charge arithmetic, ordinary train references and NBT methods are unchanged.

Bounds dimensions still update on the original client onUpdate boundary; a received metadata packet alone must not change dimensions early. setSize retains the same native-size-first, width-write, height-write order. The train reference remains a separate ordinary field. Seat lookup and indexing retain their original timing and raw values, with no new clamp or fallback. The legacy rider callback itself is not natively ported by this change.

Electric's original unconditional acceleration and commented-out fuel subtraction are intentionally retained, not repaired. It never persisted power, and this change does not add a power NBT tag. Fresh loads keep zero; loading into an existing entity leaves live power unchanged. Existing Cargo Items persistence stays unchanged. Dummy optional writes return false, entity writes are empty, and entity reads kill the transient dummy.

## Reconstructed test coverage and explicit boundaries

The source audit covers eighteen original calls, exact full-file reversal, six key owners, unchanged Cargo/Tram/Trailer source hashes, all five outer descendants and sixteen named consumer domains. Informational line excerpts in consumer-closure.json are freshly regenerated from recovered source. Unrelated event-getter changes may move or alter these excerpts; the audit checks named domains and rejects numeric watcher access through typed train aliases rather than freezing unrelated event implementation.

The runnable native probe covers actual EntityDataManager/DataSerializers and SPacketEntityMetadata/PacketBuffer bytes, full/dirty/empty deltas, equal setters, int extremes, float extremes/NaN/signed zero, native constructor registration, real descendant constructor sizes and arrays, original dummy client/server ticks, optional persistence refusal, binary NBT, and Cargo's actual diamond ItemStack count/custom-tag roundtrip. It runs train-first and dummies-first in separate JVMs.

The complete Cargo and Electric files compile against bounded ancestor fixtures. Base, Ridable and concrete train classes use exact constructor/init/selected-method slices, not complete production classes. Dummy legacy interaction/interpolation overrides and rider callbacks are excluded rather than faked. Probe-only accessors execute the exact typed reads from these callbacks without claiming full callback execution. Electric charging/onUpdate is not executed. A single test-only ModItems.battery_creative field permits complete Electric compilation; the real unchanged IBatteryItem interface is used. The native World subclass runs the native World constructor and side flag; unsupported chunk operations throw. No native Minecraft/Forge/data-manager/network/NBT class is replaced.

These checks do not establish full train/mod compilation, live network transport, actual physical riding, track simulation, rendering, game startup or save/restart acceptance. Native 1.12 protocol only; no 1.7 wire compatibility claim.

## Reproduction

    python3 ntm-port-1.12.2/porting/tests/native-entity-train/audit.py /path/to/ntm-original-pristine
    JAVA_HOME=/path/to/java8 NAGAPORT_NATIVE_CP_FILE=/path/to/native-classpath.txt python3 ntm-port-1.12.2/porting/tests/native-entity-train/probe.py
    git diff --check

The native runner fails explicitly before javac if the classpath file is missing. It never fabricates a successful test result or downloads dependencies.
