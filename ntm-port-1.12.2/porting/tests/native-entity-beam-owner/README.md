# Native beam-owner string metadata

Baseline: `77b595c32b46e2f9309bcf9924d654ddc29a3c95` (CP79). Three production files, four original watcher calls, one protected owner-local `EntityBeamBase.PLAYER_NAME` / native STRING key. Its empty-string default, original registration timing and inherited Vortex ownership remain unchanged. RenderVortexBeam now reads through `getOwnerDisplayName()`; the descendant reads the same inherited key. No fixed native slot20 or imitation watcher is used.

The constructor's original Forge `player.getDisplayName()` returns a cached String. Native `getDisplayNameString()` is the corresponding API, not `getName()`, a decorated text component, or a component-to-string conversion. `api-audit.py` verifies the pinned Forge1614 source patch and genuine native bytecode; `api-evidence.json` pins both. The live native-player probe separately gives profile name, cached Forge display name and decorated component different values. The cached string is synchronized before final position calculation, as originally.

Original World lookup is still by exact username. If Forge's display alias differs from the profile name, both gameplay and renderer fail to find the player just as before. No UUID substitution, alias repair, case folding, damage change or visual policy is introduced.

## Source and executed proof

- `source-manifest.json`, `production.patch` and `source-inverse.patch` pin and exactly reverse the three entire files. Audit checks all four pristine watcher invocations, the two-class named hierarchy and five source-wide consumer domains. Unrelated constructor formulas, lifetime gates, effects, damage, rendering and NBT bytes are unchanged from CP79.
- 224 native assertions; 11 exact source slices pinned in `slices.json`. No whole production owner class is compiled: Base's unrelated legacy brightness overrides and Vortex's effect/damage body still have independent 1.12 issues.
- Both complete Base constructors, both complete Vortex constructors, actual metadata init and empty NBT methods are compiled verbatim. The original Vortex onUpdate prefix through owner lookup is executed verbatim inside a named test wrapper; only observation is appended. Tick/death ordering is checked at 0/1/2/10/11 on both World sides, with no invented super tick. The renderer's exact lookup statement executes separately, not GL or the full renderer.
- All Entity/DataManager/DataSerializers/metadata packet/PacketBuffer/World/EntityPlayer/Vec3d/NBT classes are genuine native dependencies. The native World subclass declares unsupported chunk operations; the player subclass supplies deterministic look/eye values and a distinct decorated component. Its cached private Forge display field is seeded by reflection, so the actual native cached getter executes. External Forge display-name listeners are verified by API bytecode, not executed.
- Real STRING full/dirty/empty/forced packets, independent source/receiving managers, equal-content strings, immutable snapshots, empty/Unicode/color/NUL/maximum 32767-byte ASCII payloads and binary empty-NBT behavior pass. These are native packet serialization/application checks, not live transport or cross-version wire compatibility.

No world startup, loaded chunks, effects, ray tracing, damage, GL, whole mod compilation or gameplay acceptance is claimed. No other remaining metadata owners are included.

Run from repository root, using installed dependencies only:

    python3 ntm-port-1.12.2/porting/tests/native-entity-beam-owner/audit.py /path/to/ntm-original-pristine
    python3 ntm-port-1.12.2/porting/tests/native-entity-beam-owner/api-audit.py
    python3 ntm-port-1.12.2/porting/tests/native-entity-beam-owner/probe.py

JAVA_HOME and NAGAPORT_NATIVE_CP_FILE override the existing Java8/native classpath defaults. API evidence additionally uses pinned `/tmp/nagaport-forge1614-userdev.jar`. No dependencies are fetched. See `validation.log` and `production.sha256`.
