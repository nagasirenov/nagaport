# CP89 flying-boss primitive metadata

Baseline: CP88 `a65d3acbf05634c3cedbce5eff43ad0bd15cefdb`. Exactly three production files, 12 private static final native keys and 33 watcher substitutions. The frozen CP88 compiler log attributes exactly 33 metadata diagnostics to these substitutions; 66 unrelated same-file diagnostics remain recorded, not claimed fixed. This is a metadata boundary, not a boss-HUD or gameplay port.

## Ownership and unchanged behavior

- EntityUFO and EntityBFAngel each independently own BEAM / BYTE / Byte 0 and WAYPOINT_X/Y/Z / VARINT / Integer 0, replacing original slots 16–19. They directly extend EntityFlying, not EntityUFOBase. Their keys must not be shared with one another or with the separate siege ancestry migrated in CP88.
- EntityHunterChopper independently owns ATTACKING / BYTE / Byte 0, LEGACY_UNUSED_21 and LEGACY_UNUSED_22 / FLOAT / Float +0, and DYING / BYTE / Byte 0, replacing16/21/22/23 in the original registration order. Full-source searches find no reads/writes of the two legacy FLOAT channels after their registrations. They are nevertheless retained, not deleted, changed to health fields, exposed by a new API or repurposed.
- All three original entityInit overrides still call the real parent first, then register all four entries unconditionally on both sides. Native Entity/EntityLivingBase/EntityLiving ancestry registers twelve entries in this cached target. Each independent owner therefore receives native indices 12–15. No legacy index is passed to the manager or used as an allocation shortcut.
- BEAM and DYING predicates still mean exactly byte 1; true/false setters replace the entire byte with 1/0. ATTACKING's client getter still means any nonzero byte, and its original server-side AI block still computes `(byte) (attackCounter > 10 ? 1 : 0)` and performs the original explicit inequality check before writing. All original AI outer guards and call order are unchanged.
- Chopper's ordinary `isDying` field is intentionally distinct from its synchronized DYING channel. Neither the setter nor packet application repairs that distinction. Death initialization, damage handling, explosions, sound calls, timers, combat, beam formulas, movement and all attribute constants are unchanged.
- All six custom NBT overrides still do nothing beyond calling the real superclass. There are no new beam, waypoint, attacking, dying, float, timer or ordinary-field tags. A fresh load receives custom registration defaults; loading into an existing object leaves its unsaved custom metadata and ordinary fields alone.

The source-wide typed closure comprises 17 files, including all three renderers, both chopper sound loops, ClientProxy bindings, spawn factories, registration, invasion, sound selection, event and projectile references. There are no production descendants of these owners and no numeric external caller to rewrite. UFO/angel renderer beam predicates and chopper sound-stop predicates already use typed getters. The full remaining-watcher inventory also exposes unrelated numeric readers and rejects undisclosed typed DataWatcher aliases. The boss display interface and ClientBossDisplay are byte-identical to baseline; HUD arbitration and drawing remain outside this patch.

## Executed native proof and honest limits

`probe.py` extracts 56 pinned exact source slices into bounded same-name classes with their exact class declarations and genuine EntityFlying ancestry. These are explicitly method projections, NOT complete production owners. The original constructor bodies, complete metadata getters/setters and entityInit, and complete custom NBT methods are included. All relevant interface files are compiled unchanged, including INtmBossDisplay. The native Minecraft/Forge EntityDataManager, DataParameter, serializers, PacketBuffer, SPacketEntityMetadata, entity parents and NBT implementations are real cached platform classes, not replacements. A small native World subclass supplies no chunks and rejects chunk access.

Six fresh JVMs cover every sibling class-loading permutation, each passing 14,969 assertions, 89,814 total:

- Exact +0/Byte 0/Integer 0 defaults on both world sides, parent-first virtual registration, inherited layout and real inherited health registration, independent class-owned keys, and full/delta/empty/forced native metadata packets.
- Every signed BYTE value for all four byte channels; equality-to1 versus nonzero predicates; whole-byte boolean replacement; dirty/no-op/reset behavior; immutable snapshots; int extremes and independent XYZ axis updates; both retained FLOAT channels including negative zero, NaN equality, infinities, limits and forced packets.
- The exact contiguous inner chopper server metadata block, explicitly named `updateActionMetadataBlockOnly` in the fixture. All 256 raw bytes are tested against six attack-counter boundaries on server and client, including 10/11 and integer limits. The enclosing movement/AI method and its outer `isDying` branch are NOT run or synthesized; complete-file inverse protects them.
- Exact renderer beam expressions and both exact instanceof-guarded sound-stop expressions. The ordinary `isDying` field remains independent after setters and network receipt.
- Full exact custom save/read methods invoking real native parent save/read methods, with binary CompressedStreamTools roundtrips. Fresh/default and existing-state load semantics, missing tags, custom persistence omissions and ordinary-field non-restoration are checked.
- Exact constructor bodies execute. Other production field initializers and attribute overrides are deliberately not included. The fixture therefore has native base max health 20, not the unchanged production 750/20000 values; testing inherited health only establishes no metadata collision, not gameplay/health acceptance.

There is no whole-owner/mod compilation, full AI/death sequence, complete entity construction, live-world or client/server startup, old-client packet compatibility, HUD acceptance, sound playback or renderer GL acceptance claim. No policy-bearing/mutable serializer was added. Native protocol encoding follows the target platform. No full common build, download, install or cache regeneration was used.

## Source and original-platform evidence

`source-manifest.json` records pristine/baseline/result SHA256, all exact before/after calls and offsets, owner/legacy ID/type/default/serializer, and the named parent hierarchy. `audit.py` requires pristine watcher calls to equal baseline calls, correct once-per-declaring-class allocations, exact unconditional default registrations, typed caller closure and byte-for-byte complete-file restoration after reversing only the declared substitutions and insertions. It applies the exact inverse and forward production patches independently in a temporary directory. All source in those three changed files outside the declared substitutions is thereby preserved. The production patch is also required to contain exactly those three files, and the two boss-display interface/implementation files are independently unchanged.

`compiler-attribution.json` freezes CP88 log/source provenance for 33 watcher diagnostics and lists the 66 other diagnostics in those same owners. It is a source-attribution estimate, not a claim that a full post-patch common build passed or that exactly 33 is the final net compiler reduction.

`platform-reference.json` pins the existing CP88 cart/siege platform evidence instead of duplicating 43,082 lines of bytecode. Its recorded original client/JVM mappings establish EntityLivingBase slots 6–9 and EntityLiving 10/11; EntityFlying adds no metadata registration. The selected original 16–23 fields consequently do not collide with those vanilla parent entries. Original DataWatcher bytecode records registration/update equality behavior. The pinned Forge1614 parent patches do not alter those registrations; external mods adding metadata are not modeled. Native bytecode records the actual key allocation and packet implementation. `--platform` regenerates and compares the original/native javap output and original Forge patches from cached inputs without downloads.

## Reproduce

From repository root:

    python3 ntm-port-1.12.2/porting/tests/native-entity-flying-boss/audit.py /workspace/shared/nagaport/ntm-original-pristine --platform
    python3 ntm-port-1.12.2/porting/tests/native-entity-flying-boss/probe.py

Java8 defaults to `/workspace/shared/nagaport-toolchain/java8`; native classpath defaults to `/workspace/shared/nagaport-toolchain/native-classpath.txt`. JAVA_HOME and NAGAPORT_NATIVE_CP_FILE may override them. See `validation.log` and `production.sha256`. The audit is frozen to this baseline family; later independent edits to fully pinned files require explicit reconciliation rather than silently relaxing the inverse.
