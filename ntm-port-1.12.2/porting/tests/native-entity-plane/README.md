# Native plane metadata: CP81 bounded continuation

Baseline: `242867906a53eb7da03c879f476ced61c1170e14`. Four production files; exactly 19 original watcher invocations and 19 attributed CP81 compiler diagnostics. No other family or gameplay changes.

## Ownership and preserved behavior

- `EntityPlaneBase` owns protected `PLANE_HEALTH` / native FLOAT, originally watcher 17 with boxed Float 50F. `EntityBomber` owns private `BOMBER_STYLE` / native BYTE, originally watcher 16 with Byte 0. Native keys allocate against their declaring classes, not legacy numeric slots. The native layout is inherited Entity keys 0–5, plane health 6, bomber style 7. C130 inherits only health. Registration remains parent before child even though original slot numbers were 17 then 16.
- Every direct owner, inherited Bomber/C130 and erased-Entity renderer consumer is migrated. The renderer uses a typed `getBomberStyle()` accessor; all style values remain signed bytes, without clamping. Its existing binding to EntityBomber is verified through ClientProxy. There are no additional named descendants in the source closure.
- The health field and synchronized health deliberately remain distinct. Init still registers hard-coded 50F before ordinary fields initialize. `health = getMaxHealth()` and `timer = getLifetime()` still dispatch virtually afterward. Server update writes the local health field; client update reads synchronized health. NBT load restores only metadata, not the local health field. Consequently the first server tick still overwrites loaded synchronized health with the current local health. This old behavior is tested, not repaired.
- All factory timing, random draws, style overrides, positions, speed formulas and local bomb fields are unchanged. Byte style NBT and Float health NBT retain their names, types and missing-tag defaults. C130's original EnumUtil payload normalization remains unchanged. No timer or additional metadata persistence was introduced.

## Source and compiler evidence

`source-manifest.json` pins full pristine, baseline and result hashes, precise before/after offsets, methods, owner/type/default/legacy IDs, and the finite transformations. `audit.py` requires exact pristine watcher-call parity and reverses all four complete files byte-for-byte. Both `production.patch` and `source-inverse.patch` are independently applied and checked. Two space-before-tab warnings are original indentation retained in CV/jet factory assignments, not unrelated formatting edits. Existing mixed CRLF bytes in PlaneBase are preserved; whitespace validation recognizes CR-at-EOL and the inherited indentation.

`consumer-closure.json` pins 11 source-wide reference files, three named entity classes and the renderer's binding. `compiler-attribution.json` pins the committed CP81 log and the 19 exact affected source-line diagnostics. It also records six unrelated same-file diagnostics left untouched. This is attribution against the existing compiler result, not a new full-mod build or a claim that the four complete production classes compile.

## Executed native proof and limits

`probe.py` compiles 51 exact production source slices into bounded classes retaining their actual packages and inheritance. Two fresh JVMs with opposite sibling loading order each pass 2,382 assertions, 4,764 total:

- Complete constructors, field initializers, complete entityInit methods and original native Entity-constructor virtual dispatch. Actual ForgeChunkManager requestTicket and complete original init method execute in a deliberately quota-zero native Forge fixture. The real request returns null before registration, exactly as a denied quota request does. Genuine Loader/LoadController fields are seeded reflectively with a test mod mapping; no Minecraft/Forge class is replaced. Successful tickets, chunk forcing/loading, mod startup and a live world are NOT tested. Native World subclasses explicitly reject chunk operations.
- Genuine EntityDataManager, DataParameter, FLOAT/BYTE serializers, PacketBuffer and SPacketEntityMetadata full/delta/empty/forced packets, independent receiving managers, dirty reset, immutable snapshots, raw IEEE float edge cases and all 256 signed-byte values. Different NaN payloads remain equal under Float.equals; signed zeros differ. This is native packet serialization/application, not network transport or cross-version wire compatibility.
- Exact health-update prefix and complete Bomber/C130 audio-consumer blocks, including their native health/style reads. AudioWrapper/MainRegistry proxy are explicitly test-only observation sinks; no audio engine or renderer GL is exercised. The renderer's exact style lookup executes separately.
- Complete factory methods, base rotation and no-ticket chunk-helper paths with deterministic real World random input. Every factory's local settings, random draw order, positions, speed and style are checked. This does not execute payload spawning, flight updates, collision, damage, explosions or chunk operations.
- Complete original-derived inherited NBT methods with genuine native NBT binary round trips, omitted tags, signed style, arbitrary local integers and C130 payload enum normalization. No synthetic DataWatcher or metadata-manager implementation is used.

The small mod fixtures supply only GeneralConfig input, mod-instance identity, and audio observation. `IChunkLoader` and `EnumUtil` are compiled verbatim from pinned baseline source. The production build retains all excluded unrelated code unchanged. No whole-mod build, game launch, renderer acceptance, flight behavior or successful chunk-ticket claim is made.

## Run without downloads

From repository root:

    python3 ntm-port-1.12.2/porting/tests/native-entity-plane/audit.py /path/to/ntm-original-pristine
    python3 ntm-port-1.12.2/porting/tests/native-entity-plane/probe.py

Java8 defaults to `/workspace/shared/nagaport-toolchain/java8`; native classpath defaults to `/tmp/nagaport-ae2-native-classpath.txt`. Override `JAVA_HOME` / `NAGAPORT_NATIVE_CP_FILE` if necessary. See `validation.log` and `production.sha256`.
