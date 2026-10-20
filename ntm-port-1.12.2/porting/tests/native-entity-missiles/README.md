# Missile inheritance native metadata, bounded post-ticket slice

Baseline `5e6ba284ef32b46ad5f81cd1e1570c08b53cb24f` (CP65). Ten production files, 35 original watcher invocations, six new native keys. Independent of drone patch. Mixed CRLF/LF bytes retained. No chunk-ticket, flight, damage, fuel, item registry, particle, payload, or balance changes.

## Lifecycle boundary: explicitly not full entity acceptance

Production EntityMissileBaseNT.entityInit still executes, in exactly this order:

1. super.entityInit(), including existing ThrowableNT STUCK_FACE registration.
2. init(ForgeChunkManager.requestTicket(MainRegistry.instance, this.world, Type.ENTITY)).
3. Base direction registration (now native DataManager).

The complete actual ticket call, init(Ticket) body, World constructor and their relative registration order are byte-proved in ticket-boundary.json and by whole-file inverse audit. Nothing moves the ticket call or swallows its exceptions. Actual ticket allocation/binding/forceChunk and full constructor/chunk lifecycle remain UNVERIFIED.

The executable probe deliberately replaces ONLY that ticket expression in an extracted test-only initialization model with AssumedTicketBoundary.recordReturn(this). This marker asserts that seven genuine vanilla/ThrowableNT entries exist before missile registration and counts the explicitly assumed returns. It is not a Forge/mod registry stub, not an executed ticket call, and not a production change. No claim that a no-op parent or null-world construction proves full real lifecycle. The test's null world is permissible only for this declared post-ticket metadata model. Actual World constructor bodies and relevant health/position fields are extracted to preserve Java initialization timing, but this does not make the whole constructor path verified.

## Ownership and source closure

- EntityMissileBaseNT owns protected DIRECTION, BYTE, legacy3, default(byte)5. Existing inherited ThrowableNT STUCK_FACE remains a separate native key. setDirection(byte)/getDirection() are newly introduced typed accessors for launchpad and render consumers.
- EntityMissileCustom owns five VARINT keys: HEALTH_SNAPSHOT legacy8 with original Integer.valueOf(this.health), and WARHEAD9/FUSELAGE10/FINS11/THRUSTER12 each Integer0. Keep the health expression, not literal50: virtual entityInit runs during native Entity construction before BaseNT's health=50 initializer. Model proves metadata0 and ordinary field50 afterward. Original health snapshot is not subsequently updated/saved; no synchronization fix added.
- EntityMissileTier4 reads inherited direction for existing contrail rotation. No shadow key.
- RenderMissileGeneric/Huge/Nuclear/Strong/Taint retain their original instanceof EntityMissileBaseNT guard and use typed direction accessor. RenderMissileCustom's four ID reads cast to its sole registered EntityMissileCustom domain. LaunchPadBase's statically typed missile keeps exact `(byte)MathHelper.clamp(this.getBlockMetadata()-10,2,5)` expression, including original int overflow.
- 35 named owner/descendant classes across ten entity files are enumerated, including nested tier variants. Existing EntityRideableRocket sibling's three direct keys plus twelve RocketMetadata keys still allocate by its own owner, not numeric assumptions. Its source is unchanged. Probe compiles its exact static key declarations and complete production RocketMetadata helper, verifying all fifteen keys allocate above the inherited base direction without collision. This is static key-allocation proof only, not Rideable constructor/ItemStack alias or full metadata lifecycle acceptance.

All calls/receivers and source offsets are manifest-pinned. Exact inverse reconstructs all ten complete baseline files byte-for-byte, preserving all constructors, formulas, branch guards and payloads. Original-pristine invocation parity has one explicit already-existing normalization: launchpad MathHelper.clamp_int -> MathHelper.clamp. No other original-call difference is accepted. Consumer closure records source-wide family references and remaining raw calls for review; all family entity files and changed consumers contain no legacy watcher calls. Renderer/domain and launchpad static receiver assertions are independent of the transformation manifest.

## Executed tests

118 focused assertions on real native EntityDataManager, SPacketEntityMetadata/PacketBuffer bytes and binary CompressedStreamTools NBT. General byte/VARINT protocol edge evidence is reused from preceding probes.

- Genuine inherited-key registration after explicit assumed ticket return; exact default counts, independent Custom/Tier4 client/server instances, full and delta packets, equal scalar dirty behavior, constructor-time health0, and independent native sibling allocation.
- Actual added accessors; source-isolated Tier4 direction read and guarded renderer expressions; actual four custom renderer statements; launchpad setter expression tested with a scalar block-metadata argument replacing only the tile receiver read.
- Complete scalar BaseNT NBT tail AFTER excluded ThrowableNT super NBT: motion, position, acceleration, target/start coordinates and velocity. Complete Custom NBT tail AFTER excluded parent call: fuel, consumption and four item IDs. These are named tail helpers, invoked separately. Full inherited NBT hooks throw rather than pretend to serialize unsupported parent state. No full combined NBT chain claim. Health/direction remain unsaved, missing custom scalar tags reset IDs/floats, arbitrary signed item IDs remain unchanged without attempting a registry lookup.
- Full missile updates, impacts, part registry interpretation, template constructor, gameplay, all real chunk operations, actual GL rendering and full ancestor NBT are not exercised or claimed.

## Reproduce

From project directory:

    python3 porting/tests/native-entity-missiles/audit.py /path/to/ntm-original-pristine
    python3 porting/tests/native-entity-missiles/probe.py
    git -c core.whitespace=blank-at-eol,blank-at-eof,space-before-tab,cr-at-eol diff --check

JAVA_HOME and NAGAPORT_NATIVE_CP_FILE can override authorized existing defaults. No dependencies installed; no Gradle/full-mod compile run by worker. Combined compile and real lifecycle acceptance remain coordinator work.
