# Native projectile and utility metadata, CP82 continuation

Baseline: `df18fc077648dd18e6d0c1055d82c11dc2ebdb78`. Seven production files, five owned native data keys, sixteen replaced watcher invocations. All other production bytes reverse exactly to the baseline. This is a primitive metadata closure, not a physics, explosion, rendering, or whole-mod acceptance claim.

## Ownership and original semantics

- `EntityChemical`: legacy integer slot 10 becomes private `FLUID_ID` / VARINT. The fluent setter preserves the fluid's numeric ID and null failure before mutation; the getter retains `Fluids.fromID` fallback behavior. Its original `entityInit` does not call its parent. The already-native `EntityThrowableNT.STUCK_FACE` key is allocated but remains unregistered on Chemical, exactly as before. No new fluid persistence, ancestor initialization, throwing behavior, or cache is introduced.
- `EntityFallingNuke`: legacy byte slot 20 becomes private `BLOCK_METADATA` / BYTE. This is the originating block's metadata, not an unused flag: `NukeCustom` writes it, and `RenderFallingNuke` reads it through an erased `Entity`. Both now use typed accessors. The existing byte cast, native world-state read, metadata conversion, write-before-position order, signed-byte interpretation and renderer switch branches are unchanged. Registration still omits the parent call. All seven payload floats and native inherited NBT remain unchanged; orientation is still not persisted.
- `EntityDepress`: legacy byte slot 10 becomes private `DIRECTION` / BYTE. Constructor delegation, init-before-fields, TTL assignment before direction update, signed byte conversion, invalid-direction UNKNOWN fallback, particle parameters, and integer-only `ttl` NBT are retained. Direction is deliberately not saved. No force, AABB, timing, or lifecycle change is made.
- `EntityNukeTorex`: legacy float slot 10 and integer slot 11 become private `SCALE` / FLOAT and `COLOR_TYPE` / VARINT. The inner Cloudlet uses the actual outer owner's key. `setScale` still synchronizes only on the server and rescales local simulation dimensions on both sides. The raw `NukeBoy` write must NOT call that setter: new `setSynchronizedScale` only writes metadata. Packet receipt likewise does not resize receiver fields. Arbitrary signed type IDs, special floats, omitted parent init, atmospheric construction, empty writer, disabled optional save, and read-kills behavior remain unchanged.

Observed native allocation in both tested loading orders: Entity keys 0–5; existing ThrowableNT stuck-face 6; Chemical fluid 7 (without registered 6); FallingNuke block metadata 6; Depress direction 6; Torex scale/type 6/7. These are observations, not hard-coded production IDs or cross-version wire promises.

## Frozen source and compiler evidence

`source-manifest.json` records full pristine/baseline/result SHA-256 values, each original and replacement invocation, exact offsets, method, owner, legacy ID, serializer, default, and inserted declarations/accessors/imports. `audit.py` reverses all seven full files byte-for-byte and independently applies `source-inverse.patch` and `production.patch` in a temporary tree.

The sixteen pristine watcher invocations match the baseline exactly except one explicitly recorded earlier prerequisite: NukeCustom's nested original `world.getBlockMetadata(x, y, z)` argument was already migrated to the native `WorldBlockState` read/metadata expression. This patch preserves that baseline expression verbatim; the audit only permits that one exact prerequisite normalization. It does not silently normalize arbitrary arguments.

`consumer-closure.json` pins thirty source-wide owner/renderer reference files, their original watcher calls, four named owner classes with no named descendants, typed aliases and erased renderer bindings. It found and covers both external writes and the numeric renderer read. Other entities' unrelated calls in those reference files are not claimed as migrated.

`compiler-attribution.json` pins the existing CP82 full compiler log and sixteen exact affected line diagnostics. Fifty-five unrelated same-file diagnostics remain, including the renderer's unrelated constructor API and Chemical's throwing/physics APIs. Attribution is against the recorded baseline log, not a new full-mod build or a claim that all seven complete production files compile. The sole space-before-tab warning in forward/reverse application is retained original FallingNuke indentation.

## Executed native proof and exact boundaries

`probe.py` runs Java 8 against the installed genuine Minecraft/Forge 1.12.2 classpath. Two fresh JVMs reverse all four owner loading orders; each passes 3,646 assertions, 7,292 total. Sixty-five independently frozen source inputs/fragments are checked against `slices.json` before compilation. No Minecraft/Forge Entity, DataWatcher, EntityDataManager, serializer, network, NBT, or world implementation is replaced.

- The complete actual `EntityFallingNuke` source compiles with complete production `WorldBlockState`. Both complete constructors, native EntityThrowable inheritance, registration, accessors, payload fields, and complete NBT methods execute. A test-only NukeCustom explosion fixture throws if invoked; no update/collision/explosion is executed.
- Chemical's exact complete World constructor and unchanged ancestor World constructor execute, with native Entity virtual initialization. Its init, setter/getter, style selection, age, drag and gravity source methods compile unchanged. The throwing constructor and inherited NBT are excluded: their existing legacy owner-name/throwing APIs are not repaired here. The bounded parent throws on NBT invocation rather than pretending to implement it. Fluid fixture contents and trait classes are explicitly test-only; original selected FluidType ID/trait/color methods and exact Fluids.fromID method execute. This is not full fluid registration or payload acceptance.
- Depress's two complete constructors, exact fields, init, accessors and full NBT methods execute. Its exact particle-parameter slice verifies native synchronized direction use and unchanged random consumption. No particles are emitted, force loop executed, or tick/TTL simulation claimed.
- Torex's complete constructor and original fields execute with explicitly test-only atmosphere/orbit inputs, including null and pressure-threshold cases. Its complete metadata/scalar/NBT methods and complete original Cloudlet constructors/color method execute. Cloud movement, spawning, audio, GL, actual atmosphere integration and general tick behavior are excluded.
- Actual native data managers and `SPacketEntityMetadata` serialize, decode and apply full, dirty, empty and forced snapshots to separate receiving entities. Tests cover independent owners/keys, defaults, all 256 signed values for both bytes, integer edge values, immutable snapshots, equal-value coalescing, raw IEEE float payloads (including signed zero, infinity and NaN equality), and a combined two-key delta. This proves packet serialization/application, not transport, entity spawning or cross-version compatibility.
- Exact external NukeCustom/NukeBoy application statements and FallingNuke's renderer lookup expression execute separately. The former follows native block state to unchanged byte cast to typed getter; the latter tests all signed values. The actual rendering model/GL is not executed. Complete source reversal preserves its switch branches and all enclosing gameplay order.
- Native binary NBT exercises FallingNuke payloads and Depress TTL, including missing tags and intentionally absent orientation/direction state. Torex's existing non-persistence and death-on-read are exercised, not changed. Chemical NBT is source-preserved only, not runtime-certified.

No chunk tickets are requested by these owners; live chunks are explicitly rejected in the native World test subclass. No toolchain download, whole-mod build, game launch, or network access is needed.

## Reproduce offline

From the repository root:

    python3 ntm-port-1.12.2/porting/tests/native-projectile-utility-data/audit.py /path/to/ntm-original-pristine
    python3 ntm-port-1.12.2/porting/tests/native-projectile-utility-data/probe.py

Java defaults to `/workspace/shared/nagaport-toolchain/java8`, and native classpath to `/tmp/nagaport-ae2-native-classpath.txt`; override `JAVA_HOME` or `NAGAPORT_NATIVE_CP_FILE` if needed. `validation.log` records the final run. `production.sha256` pins the seven resulting production files.
