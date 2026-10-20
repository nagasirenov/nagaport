# Bounded native entity synchronization, CP59

Baseline: e72ec3d2ae8db6653ebaad529a574ee32c99260b.
Production scope: seven classes, 21 exact DataWatcher operations, five native Integer keys.

## Ownership and preserved values

- EntityDebrisBase declares protected static final DEBRIS_TYPE via createKey(EntityDebrisBase.class, VARINT), registers default Integer 0 in entityInit, replacing original slot 20. EntityRBMKDebris and EntityZirnoxDebris inherit this exact base-owned key. No subclass allocation or shadow key. The two subtype enum orderings remain distinct and unchanged. Both use the original abs(raw) % values.length expression, including its original MIN_VALUE overflow behavior, without new normalization or cap.
- Each of EntityCloudFleija, EntityCloudFleijaRainbow, EntityCloudTom and EntityCloudSolinium independently declares private MAX_AGE with its own class owner, registers Integer 0, and preserves setMaxAge/getMaxAge. Original slot 16 default was 0, NOT the unrelated public maxAge field initialized to 100. No change to that field or any tick formulas.
- These owners extend vanilla Entity, directly or through DebrisBase. Native EntityDataManager.createKey starts after vanilla Entity keys. Inherited registration happens once through the virtual entityInit called from Entity construction. Cloud siblings can legally have the same numeric native index, but not the same DataParameter object. No registration depends on world.isRemote. Server and client execute the same initialization.
- Exact original synchronization lines were compared to ntm-original-pristine, not a derivative fork. source-manifest.json records original/CP59/after hashes, owner, receiver, superclass, slot, type, default and exact before/after statements and source line/method attribution. audit.py proves all other production bytes unchanged from CP59.

## NBT is explicit, not automatic

DebrisBase persists the raw Integer under debtype. RBMK keeps its original redundant override; Zirnox inherits the base methods. Neither enum-normalizes saves. Missing key remains zero. Cloud NBT persists age as short and (except Tom) scale as short. MAX_AGE was never saved and is still not saved, including its original zero default after fresh load. No “fix” or new persistence introduced.

## Native networking semantics

Integer is immutable; no ItemStack mutation/copy issues enter this scope. All changed setters use manager.set, not forced dirties. Original DataWatcher.updateObject and target manager.set both use ObjectUtils.notEqual before notifying the entity and marking dirty (see supplied native/legacy javap evidence; legacy class te is mapped in Forge1614 conf/packaged.srg). Target dirty extraction consumes dirty status; later delta extraction is empty. Full metadata snapshots include defaults. Actual SPacketEntityMetadata write/read, read buffer exhaustion and receiving manager application are tested. Signed integer edge values round-trip through VARINT. Wire IDs/encoding need not match 1.7.10, and no old-client compatibility is claimed. Explicit force-dirty testing is characterization only; production does not add forced dirty calls. Native packet entry copies use immutable Integers here; no unsupported serializer placeholders.

## Reproduce and honest limits

JAVA_HOME=/workspace/shared/nagaport-toolchain/java8 python3 porting/tests/native-entity-data/probe.py
python3 porting/tests/native-entity-data/audit.py /workspace/shared/nagaport/ntm-original-pristine

CP defaults to /tmp/nagaport-ae2-native-classpath.txt; override NAGAPORT_NATIVE_CP_FILE. No downloads, Gradle or new dependencies. Probe dynamically extracts exact production key declarations, entityInit/getters/setters/NBT and enums into small test-only Entity subclasses against the actual Forge/Minecraft dependency. Debris rot/lastRot and cloud age/scale supporting fields are included; unrelated gameplay is deliberately not compiled in this bounded test. No fake platform classes and no production build exclusions.

PASS: 414 assertions across seven owner fixtures. The native manager and metadata packet are real. This is NOT complete production-class compilation, a client renderer check, a dedicated server run or a world-save gameplay acceptance. Debris still has substantial old movement/collision/interaction APIs. Cloud brightness signatures and lightning constructors/spawn remain unrelated compile blockers. No such methods were edited. Other entity synchronization families and external numeric consumers remain for later work; searched references to these seven types use their typed accessors, not direct watcher slots.
