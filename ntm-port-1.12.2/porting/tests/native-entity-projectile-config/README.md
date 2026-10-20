# Projectile ammunition/configuration native entity data

Pinned production baseline: `89b6bfc71a8b532c2fe7cdbd43bb66b7991ee365` (CP78). Five changed production files: four metadata owners and the sole external raw-ID reader. Seventeen original watcher invocations become genuine native `EntityDataManager` calls/accessor use. No compatibility watcher, fixed native slot IDs, payload omission, balance change, or altered flight/damage/save mechanism is introduced.

## Ownership and preserved behavior

- `EntityBulletBaseMK4`: legacy slot 3, `Integer.valueOf(0)` -> private `BULLET_CONFIG` / native VARINT, declared with `EntityBulletBaseMK4.class`. Keeps `super.entityInit()` before registration. `EntityBulletBaseMK4CL` inherits exactly this key, then runs its original ticket request; it declares no shadow key.
- `EntityBulletBeamBase`: independently extends native Entity. Legacy slot 3, `Integer.valueOf(0)` -> its own private `BULLET_CONFIG` / VARINT, declared with the beam class. It is not a throwable key or shared helper allocation.
- `EntityArtilleryRocket`: legacy slot 10, `new Integer(0)` -> private `AMMO_TYPE` / VARINT. Its original entityInit omits `super.entityInit()`. That omission is intentionally preserved: the inherited `STUCK_FACE` key is statically allocated, but not registered in rocket instances. Ticket call stays before ammo registration.
- `EntityArtilleryShell`: legacy slot 10, `new Integer(0)` -> its own private `AMMO_TYPE` / VARINT. Original sequence remains super registration, ticket request, ammo registration.
- `RenderArtilleryRocket` uses `rocket.getTypeId()` for its raw texture-array index. Replacing this with `getType()` would silently add fallback behavior, so the new accessor returns the unmodified raw synchronized integer. Existing renderer invalid-index/null-entry exceptions remain.

The native Entity six keys occupy 0–5. In the tested inheritance model, the existing ThrowableNT key is 6, its three ammo/config children independently allocate 7, and the unrelated direct-Entity beam allocates 6. These are observed native allocation results, never production constants or replacements for original numeric slots.

Sedna's config field is assigned before metadata is written; null input still leaves the field null then throws before the metadata changes. The original `id > configs.size()` guard remains, including the `id == size` exception. Existing client lazy resolution happens only while the ordinary config cache is null; metadata receipt does not refresh a populated cache. Ammo lookup preserves catch-to-index-zero behavior, null entries, arbitrary signed raw IDs and fluent setters. Rocket saving still initializes a null target from its current position. Shell scalar tags and every cargo/flight/payload byte outside the metadata substitutions remain untouched in this patch.

## Frozen source proof

`source-manifest.json` pins full before/after byte hashes, the exact pristine-source hashes, all 17 before/after invocation offsets, methods, typed receiver owners, defaults and insertions. `production.patch` is the exact five-file forward artifact; `source-inverse.patch` is the exact reverse artifact. `audit.py` applies the reverse in a temporary tree and checks all five whole files against the baseline bytes. It independently checks the original ticket/super ordering, key declaration owners, five named family classes including the CL descendant, and source-wide closure across 52 baseline consumer files. `consumer-closure.json` includes the baseline source references and any unrelated raw watcher calls, which are not claimed as migrated.

The pristine 1.7.10 invocation sequence matches the CP78 sequence exactly for all scoped calls. Whole-file reversal proves all unrelated constructors, NBT, branch guards, formulas, spawn data and payload bytes remain as in CP78. It does not prove those pre-existing APIs already work in 1.12.2.

## Executed native proof: exact boundary, not whole-owner compilation

Fresh Java 8 probe: 658 assertions in `artillery-first`, then 658 in `sedna-first`, each in its own JVM. All native Minecraft/Forge Entity, data-manager, serializer, network, PacketBuffer, ByteBuf, Vec3d, ResourceLocation and binary NBT classes come from the installed genuine 1.12.2 classpath. No native class is stubbed/replaced. No whole production owner is compiled or certified by this harness.

The runner compiles 54 exact selected source fragments into explicitly bounded test classes. It checks their independently frozen bytes in `slices.json` before compilation. Coverage includes:

- Four true-owner key declarations, existing ThrowableNT static key and registration, exact init super order, defaults and registration counts; actual native Entity construction and virtual entityInit dispatch, two load orders, no key collision or CL shadow key.
- Full/default/dirty/empty/forced metadata packets serialized and decoded through real `SPacketEntityMetadata` and `PacketBuffer`; exact entry counts, signed int extremes, dirty resets, equal values and snapshot isolation.
- Complete source `setBulletConfig`/`getBulletConfig`, the exact single lazy-resolution statement (not onUpdate), minimal exact source registry list/id/constructor, valid/invalid/off-by-one/null/cache behavior.
- Complete source ammo setters/getters and raw-ID accessor, against explicit two-entry NTM ammo fixtures. No real ammunition registry or payload implementation is simulated or claimed.
- The complete texture-selection method, made static only in its test wrapper, against the same explicit NTM fixture. No GL rendering or complete renderer is executed.
- Complete Rocket NBT tail after its excluded parent NBT call; Shell scalar prefixes after parent NBT and before cargo handling. Binary native NBT roundtrips, missing tags, type dirty propagation and original target mutation. Full inherited NBT hooks throw in the test model rather than pretending to pass. Shell cargo is excluded, not substituted.
- Beam's complete transient NBT methods and complete extra spawn write/read methods; MK4's complete extra spawn writer tested for its null-thrower sentinel only. No claim for non-null thrower resolution or complete spawn transport.

Constructor boundaries are explicit. ThrowableNT and ThrowableInterp World constructors are source-exact; Shell's World constructor and CL's World constructor are source-exact atop bounded parents. MK4, Beam and Rocket use synthetic `super(world)` test constructors, so their full real constructor bodies are not executable proof. Their preservation is proven only by the source inverse.

Every ticket expression in extracted init methods is replaced ONLY in the test model by an `AssumedTicketBoundary.recordReturn` marker. It asserts the exact 6/7/8 pre/post-registration count and constructor-time null config, but is an explicit assumed return, not a Forge stub or successful chunk-ticket operation. Production requestTicket/init/bind/force/unforce/release code and ordering are unchanged. Actual tickets, worlds, chunks, flight, impact, damage, ammunition registration/content, gameplay, GL, full owner/mod compilation, startup and save/restart remain unverified. The tests establish native 1.12 metadata semantics, not 1.7 wire compatibility or live client/server transport.

## Fragment-safe integration

The shell cargo loader is deliberately outside the executed NBT prefix. A separate cargo-loader change can apply to that existing statement without touching this patch's metadata hunk. The full-source `audit.py` validates the frozen metadata-only checkpoint and should remain strict; do not rewrite its baseline hashes to conceal another tranche. `probe.py` separately validates the 54 executed slices, so it can be rerun on a combined tree with an independently audited cargo-loader hunk without invalidating metadata proof. A combined tree must separately retain/reconcile both whole-source patches.

## Reproduce without downloads

From the repository root:

    python3 ntm-port-1.12.2/porting/tests/native-entity-projectile-config/audit.py /path/to/ntm-original-pristine
    JAVA_HOME=/path/to/java8 NAGAPORT_NATIVE_CP_FILE=/path/to/native-classpath.txt python3 ntm-port-1.12.2/porting/tests/native-entity-projectile-config/probe.py
    git -c core.whitespace=blank-at-eol,blank-at-eof,space-before-tab,cr-at-eol diff --check

Defaults use the authorized installed Java 8 and `/tmp/nagaport-ae2-native-classpath.txt`. The runner fails on a missing classpath and never downloads dependencies. Full-mod compilation/integration belongs to the coordinator; no full compile was claimed by this worker.
