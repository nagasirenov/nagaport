# Native scalar synchronization: finite CP60 increment

Baseline da80e3f7e6bba2800456f3d860c1c73a8af1ae2c. Three production classes, 15 operations, five real native keys. No previous batch overlap.

- EntityFireLingering, directly extends Entity: original slot 10 Integer 0 becomes FIRE_TYPE/VARINT; slots 11 and 12 Float positive zero become AREA_WIDTH and AREA_HEIGHT/FLOAT. Each key is private static final and allocated against EntityFireLingering.class. The existing fluent setArea and setType/getType methods are unchanged except manager calls; onEntityUpdate reads native values in the original locations. No size clamp, NaN filtering, duration cap or changed damage/tick formula. maxAge remains an ordinary unsynchronized int, initialized to 150 and changed directly by setDuration.
- EntityWaypoint, directly extends Entity: original slot 10 Integer 0 becomes WAYPOINT_TYPE/VARINT. The commented-out slot 11 declaration remains a comment, never a new registration. Accessors and type NBT preserve arbitrary signed integers.
- EntitySiegeLaser, extends vanilla EntityThrowable: original slot 12 Integer 0xffffff becomes COLOR/VARINT. No clamping to a 24-bit color. Vanilla EntityThrowable.entityInit is empty (included javap evidence), so retaining the original non-super-calling override does not skip parent custom keys. Entity's six base keys register before entityInit; all three owners allocate after them. Production native layout is fire 6/7/8, waypoint 6, laser 6. Reusing numeric indices across distinct siblings is correct; key objects are not shared. Two fresh JVM runs with opposite class initialization order verify stable layouts.

## Source conservation and ownership evidence

source-manifest.json identifies exact original/pristine hashes, CP60 hashes and resulting hashes, declaring class/superclass, receiver type (this), slot, key type/serializer/default, and every full before/after statement with line and enclosing source method. audit.py independently finds after statements and owner declarations, checks unique full-line ranges and reconstructs CP60 byte-for-byte after inverse replacements/removal of new imports and keys. Pristine synchronization statements match CP60 exactly, including Waypoint's untouched comment. No unrelated transforms or hidden production exclusions.

All references to these classes were inspected for external numeric watcher consumers. Existing glyphid/siege watcher calls in referencing files belong to those receiver classes, not the selected waypoint/laser entities. The selected classes expose existing typed methods; no external numeric call changes were needed.

## Explicit save semantics

Fire still writes no entity NBT, suppresses optional saving, and dies on read. Its pre-existing legacy void writeToNBT override is NOT changed here and remains a separate compiler blocker. Native data manager registration does not introduce persistence.

Waypoint writes/reads the raw task integer under type; absent type is zero. Laser retains actual vanilla EntityThrowable super NBT plus damage, explosive, breakChance, incendiary and raw color. The original missing-color read yields zero, not its white registration default. No new save tags or fallback values added.

## Genuine native packet/dirty proof

probe.py extracts exact production declarations and selected methods into minimal test-only shells derived from the real Entity or EntityThrowable. Laser's unused abstract impact hook throws explicitly if exercised; no gameplay test or stubbed platform is implied. Native metadata manager, PacketBuffer and SPacketEntityMetadata serialize/write/read/apply real entries. Native inherited EntityThrowable NBT is exercised, not replaced. Unrelated world/gameplay methods are not included in this bounded harness, but are retained in the production build.

Each of two fresh JVMs passes 301 assertions (602 total): default/full/delta packets, signed integer limits, Float positive/negative zero, subnormal values, finite extrema, infinities, raw NaN payload bits, equal-value dirtiness, dirty reset, immutable snapshot independence, multiple area keys, separate class ownership and exact setter/NBT behavior. Native FLOAT serialization preserves raw IEEE bits. Float.equals treats different NaN payloads as equal, leaving the first value clean, while signed zero changes mark dirty. This matches the original boxed-Float/ObjectUtils.notEqual policy shown in the previous batch's legacy bytecode evidence. No ItemStack or mutable serializer claims. Forced dirty is only characterized in the test, never added to production. Old client packet compatibility is not claimed.

## Run / limits

python3 porting/tests/native-entity-data-scalars/audit.py /workspace/shared/nagaport/ntm-original-pristine
JAVA_HOME=/workspace/shared/nagaport-toolchain/java8 python3 porting/tests/native-entity-data-scalars/probe.py

NAGAPORT_NATIVE_CP_FILE overrides /tmp/nagaport-ae2-native-classpath.txt. No downloads, dependencies or Gradle invocation. This is bounded production-method proof, not complete entity/mod compilation or game runtime acceptance. Existing fire yOffset/void-writeToNBT/AABB calls, waypoint AABB/spawn, and laser old block APIs remain outside scope. No boss changes and no spawn edits. MinerRocket was excluded because its direct SatDock numeric consumers need a larger migration.
