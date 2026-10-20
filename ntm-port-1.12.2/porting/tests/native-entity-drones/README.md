# Native drone family scalar metadata

Exact baseline `27dceb0ef9e5815de3dc5f0494ca43aef9997370` (CP64). Independent of the preceding scalar-pairs patch. Five production files, 14 original watcher operations, two BYTE keys. Delivery's mixed CRLF/LF source bytes are preserved, not normalized.

## Actual owner/lifecycle closure

EntityDroneBase directly extends native Entity. It owns protected static APPEARANCE (legacy slot10, Byte0), inherited by both RequestDrone and DeliveryDrone. RequestDrone has no own entityInit and creates no key. Its existing duplicate app NBT save/load statements now use the inherited key, not a shadow owner key. setAppearance still narrows int to byte and getAppearance still sign-extends byte to int.

DeliveryDrone owns private static LOAD (legacy slot11, Byte0), registers after its unchanged super.entityInit call, and exposes byte getLoad/setLoad for two external consumers. ItemDrone's existing cast/express branch now calls setLoad((byte)1). RenderDeliveryDrone is registered for exactly DeliveryDrone and RequestDrone; its original request-instance branch remains first, and only its exclusive else-if casts to DeliveryDrone for getLoad. This matters: RequestDrone must never read Delivery's key. Appearance rendering already uses the base typed accessor. Source-wide named references and lack of further descendants are audited.

LOAD==1 alone selects speed0.375*3 and express texture/drop variant; other byte values retain speed0.375. No clamping, boolean conversion, target movement, timer, inventory, payload, registry lookup or chunk-loading behavior changed. Original mixed-newline inventory/fluid/request-program NBT bodies and ticket calls remain byte-exact except the identified scalar manager calls. In particular, the original request-program serialization anomalies are not fixed.

Delivery entityInit has no ticket side effects. Actual ticket request remains in setChunkLoading, called separately by ItemDrone and conditionally after NBT payload load. No ticket request has been removed, moved, bypassed or represented as successfully tested.

## Evidence and executed limits

107 focused assertions use genuine EntityDataManager, SPacketEntityMetadata/PacketBuffer and CompressedStreamTools, compiled with existing Java8/Minecraft/Forge classpath. General BYTE dirty/default/wire edge evidence is reused from previous native-entity-byte work, rather than another exhaustive byte matrix.

Probe extracts the exact complete World constructor for base and both children, base/Delivery complete entityInit, real target initializers, Request heldItem/program/nextActionTimer initializers and Delivery slots initialization using its actual getSizeInventory()=18. Native Entity constructor runs the actual virtual initialization chain. Constructor size0.75, target=-1, empty program and eighteen null inventory slots are asserted. No fake net.minecraft classes or custom-parent no-op entityInit exists. Unused fluid/ticket fields are not mocked or asserted; they have no initialization expression.

Base target/app NBT methods execute in full, including missing-tY preservation. Child helpers deliberately execute only noncontiguous exact super-NBT and scalar app/load statements. Request held-item/program, Delivery inventory/fluid, nextActionTimer reset and chunk-ticket NBT paths are outside executable scope. Full child NBT hooks throw instead of masquerading as complete serializers. Named bounded helper tests cover inherited key persistence, duplicate app call, signed byte narrowing, missing app/load defaults, inherited target offsets and binary NBT. They do not establish full child inventory/ticket save acceptance.

Actual getters/setters and getSpeed formula run. ItemDrone's new setter statement is extracted. Shared renderer's original request guard and new delivery-only comparison are verified and exercised as a boolean predicate; no GL/texture rendering claim. Fresh server/client instances allocate matching inherited keys; full/default and dirty packets are genuine native serialization. Equal-value set stays clean. No new metadata callback is added; existing accessors consume manager state directly.

source-manifest.json pins raw before/after/original byte hashes, exact invocation offsets, enclosing methods, static receiver and declaring-key owner. audit.py independently attributes after-source statements, checks renderer registration/branch exclusivity, scans descendants and reconstructs every baseline byte by inverse transformation. consumer-closure.json records all named references. Original-pristine invocation parity is exact. No full class or whole-mod build claim.

## Reproduce

From project directory:

    python3 porting/tests/native-entity-drones/audit.py /path/to/ntm-original-pristine
    python3 porting/tests/native-entity-drones/probe.py

Override JAVA_HOME or NAGAPORT_NATIVE_CP_FILE if needed; defaults use the authorized Java8 toolchain and `/tmp/nagaport-ae2-native-classpath.txt`. No new dependencies or Gradle run.
