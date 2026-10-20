# Generic-grenade native item-ID metadata

Baseline: `77b595c32b46e2f9309bcf9924d654ddc29a3c95` (CP79). Two complete production owners, ten original watcher operations, two independently declared private `GRENADE_ID` / native VARINT keys. Each original slot12 literal integer0 default remains0. Impact and Bouncy do not share key objects or hard-code native slots; their existing empty/omitted parent init behavior is preserved. Constructor bodies, fuse/bounce/explosion logic and payloads are otherwise unchanged.

## Necessary native item-zero representation adaptation

Actual pinned vanilla1.7 runtime returns null for item ID0; native1.12 returns ItemAir. An unmodified pre-fallback cast would therefore introduce a default/missing-NBT ClassCastException. Each getter now reads the raw synchronized ID once and treats **only ID0** as null before the original cast and fallback. Nonzero wrong-type items still throw; unknown IDs still fall back; invalid fallback types still throw; a null fallback remains null. No saved or synchronized ID is clamped, reassigned or normalized.

`legacy-audit.py` executes nine assertions against classes loaded from the actual pinned1.7 client JAR, including bootstrapped ID1, null Item ->0, and ID0/unknown ->null. It also pins the Forge1614 FML Item patch and original FML registry/GameData sources: the Forge item registry has null optional default, so absent0 remains null there as well. This is genuine vanilla runtime plus exact Forge source evidence, **not** a claim that a Forge1.7 world was run. `legacy-evidence.json` and `LegacyItemIds.java` preserve the oracle. The native test separately asserts actual ItemAir at0 before testing the compatibility branch.

## Source and executed proof

- Exact whole-file inverse artifacts and pristine call parity for both owners. `source-manifest.json` explicitly records the getter's two-statement adaptation, not just a watcher rename. Audit closes two named classes and six consumer domains; no external numeric grenade watcher consumer remains.
- Both complete, unmodified production `EntityGrenadeImpactGeneric` and `EntityGrenadeBouncyGeneric` files and the actual `IGenericGrenade` interface compile in the harness. This is **whole-owner compilation against bounded ancestors**, not full real inheritance/game compilation.
- Impact's test ancestor compiles all three exact production forwarding constructors on genuine native EntityThrowable, with unsupported impact behavior throwing. Its inherited native NBT chain really executes. Its full production NTM ancestor update/impact logic is excluded, not silently certified.
- Bouncy's test ancestor compiles its exact World constructor, metadata init, fields, timer NBT hooks and thrower getter (11 combined ancestor source slices are pinned). The two other ancestor constructors throw and are not executed; IProjectile/flight/bounce behavior is outside this test model. Owner NBT calls the actual extracted timer parent hooks, not a no-op.
- Explicit test ItemGenericGrenade/ModItems collaborators are native Item subclasses registered using genuine ForgeRegistries.ITEMS, not fake Item registries. Payload/fuse/bounce methods throw if accidentally invoked, preventing an expanded mechanics claim. The real item classes and real mod item catalog are not certified.
- 172 assertions in each of two independent JVM load orders. Real metadata full/dirty/empty/forced packets, exact defaults/counts/owner allocation, fluent item setter, actual Forge item-ID selection, null/zero/unknown/wrong-type behavior, signed raw integer packet/NBT roundtrips, missing tags, fallback failures, native Throwable NBT and exact Bouncy timer ordering pass.

No payload, explosion, flight, physics, complete real ancestor/mod build, startup, loaded world, old-save ID remapping or gameplay acceptance is claimed. Native1.12 metadata bytes are tested, not legacy wire compatibility or live multiplayer. Beam identity is separately proven in the neighboring beam-owner evidence directory; neither subfamily's semantics are conflated with the other.

Run from repository root without downloads:

    python3 ntm-port-1.12.2/porting/tests/native-entity-generic-grenades/audit.py /path/to/ntm-original-pristine
    python3 ntm-port-1.12.2/porting/tests/native-entity-generic-grenades/legacy-audit.py
    python3 ntm-port-1.12.2/porting/tests/native-entity-generic-grenades/probe.py

JAVA_HOME and NAGAPORT_NATIVE_CP_FILE override installed defaults. The legacy oracle additionally uses pinned `/tmp/nagaport-legacy-client.jar`, `/tmp/nagaport-forge1614-userdev.jar` and `/tmp/nagaport1710-srg.zip`. All dependencies are pre-existing. Full combined compilation remains coordinator work. See `validation.log` and `production.sha256`.
