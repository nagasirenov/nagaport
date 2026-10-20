# Native scalar projectile pairs, CP64

Exact baseline: `27dceb0ef9e5815de3dc5f0494ca43aef9997370`. Six production files, 43 original watcher invocations, nine owner keys. No production changes outside metadata calls/imports/declarations and three renderer reads. No constructors, formulas, branch guards, spawn calls, timers, registry conversions, parent sources or payloads changed.

## Ownership and defaults

- EntityCog -> EntityThrowableInterp -> EntityThrowableNT -> native Entity. Own legacy10 ORIENTATION and11 META, Integer0 / VARINT. Base already owns native BYTE STUCK_FACE; super.entityInit is retained before child registration. Renderer uses existing orientation and meta accessors.
- EntitySawblade has same ancestry and independent owner keys10 ORIENTATION and11 META, Integer0 / VARINT. META has no setter and is not saved. Renderer uses existing orientation accessor. The siblings may legitimately share native numeric indices, never key objects or declaring owners.
- EntityMist -> native Entity. Legacy10 FLUID_TYPE Integer0 / VARINT;11 AREA_WIDTH and12 AREA_HEIGHT Float positive zero / FLOAT. Constructor still sets noClip; maxAge initializer150 and duration setter remain ordinary unsaved/unsynchronized state. Fluid registry ID conversion remains in existing setType/getType and NBT. Width/height apply in existing onEntityUpdate, not an added notifyDataManagerChange callback.
- EntityDisperserCanister -> EntityGrenadeBase -> native EntityThrowable. GrenadeBase defines no entityInit or NBT override. Canister registration intentionally still omits super.entityInit, exactly as original; native Throwable has no additional custom keys. Legacy12 FLUID_TYPE and13 ITEM_TYPE both Integer0 / VARINT. Complete original NBT calls native Throwable NBT then saves/loads the two integer IDs. Registry accessors and explosion remain unchanged.

The manifest records original/default expressions, exact before/after source offsets, enclosing methods, receiver/declaring-owner evidence and source hashes. The inverse audit reconstructs each complete CP64 file byte-for-byte; original-pristine watcher invocations match exactly. It independently checks current after attribution, parents, lack of descendants and sole Cog/Sawblade renderer registration domains. consumer-closure.json records all named-owner source references, including factories, item users and registration. Dedicated renderers had three generic Entity numeric reads, all closed through their registered owner types; no raw calls remain in the four relevant renderers. No compatibility numeric accessor was added.

## Executed scope, not full-class acceptance

149 actual native assertions. Java8 compilation of dynamically extracted, exact source portions against the existing real target Minecraft/Forge classpath, followed by actual EntityDataManager, SPacketEntityMetadata byte roundtrips and binary CompressedStreamTools NBT. No net.minecraft replacements or fake fluid registry classes.

The complete World-only constructor bodies for all four owners, both ThrowableNT/Interp parents, and GrenadeBase are extracted, with actual native Entity/Throwable super calls. ThrowableNT's complete native entityInit and existing stuck-side accessors are extracted. Interp and GrenadeBase inherit initialization, just as actual sources do. Cog/Sawblade constructor size1 and Mist noClip/maxAge are asserted. Mist's real setPosition override is included because native construction can dispatch to it. Only this World constructor path is executed: living-thrower/position constructors and their physics are unchanged but not compiled by this probe. Unused custom-parent fields/update/physics are not presented as tested.

- Cog/Sawblade: actual metadata getters/setters and renderer expressions; inherited key coexistence; full/default/delta packets; exact +6/-6 callback assignment statements, including int overflow, without claiming execution of enclosing collision/motion guards. Exact child NBT tails after super serialize rot and (Cog only) meta. Parent ThrowableNT full NBT is deliberately not compiled: it still contains unrelated legacy getCommandSenderName. Unsupported parent serializer hooks throw rather than silently passing. No claim of full custom-parent NBT lifecycle.
- Mist: actual setArea/setDuration/getMaxAge; complete constructor/init; exact area NBT statements with native binary persistence, signed zero/infinity/NaN and missing tags. Fluid registry resolving setType/getType and the type portion of NBT are preserved/audited but not executed. Abstract NBT hooks throw; named bounded area helpers cannot masquerade as full registry persistence. No fake FluidType/Fluids stubs.
- Canister: actual scalar setters and complete NBT methods with real native Throwable NBT; arbitrary signed ID values, missing tags, superclass xTile preservation. Registry lookup and explode/onImpact remain outside execution; unsupported impact throws.

Native keys allocate after vanilla keys (and inherited ThrowableNT key where applicable); numeric IDs deliberately need not match 1.7.10. Source/client registration symmetry comes from identical owner class declarations and complete init bodies. No additional metadata callback is introduced. Immutable scalar setters use native equality/dirty behavior; equal ints and boxed NaNs stay clean, signed zeros differ, packets consume dirty sets and client ignores null delta entries using established native handler behavior. Binary NBT canonicalizes NaN, while metadata preserves raw float bits. Existing BYTE/scalar/vehicle probes cover the general protocol edge matrix; this tranche adds only owner-specific constructor/inheritance/NBT/consumer checks.

## Reproduce

Run from any directory:

    python3 porting/tests/native-entity-pairs/audit.py /path/to/ntm-original-pristine
    python3 porting/tests/native-entity-pairs/probe.py

Paths above are project-relative. Native CP defaults to `/tmp/nagaport-ae2-native-classpath.txt`; override NAGAPORT_NATIVE_CP_FILE. JAVA_HOME defaults to the authorized Java8 toolchain. No Gradle or whole-mod build run by this worker. Combined build/runtime acceptance remains coordinator work.
