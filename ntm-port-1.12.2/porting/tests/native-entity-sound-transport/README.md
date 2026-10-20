# Dormant entity sound transport, CP59 baseline

Exact baseline: `e72ec3d2ae8db6653ebaad529a574ee32c99260b` (CP59, not CP58). This is incremental infrastructure for a later entity-coordinate port. **Zero existing sound calls are migrated or activated.** The earlier coordinate helper, its registrations/categories and the separate loop-runner reproduction correction are untouched.

## Production changes

- `LegacyEntitySoundPacket`: mod-owned one-shot payload retaining arbitrary resource-name strings, original int-coordinate truncation at scale 8, float volume and clamped unsigned-byte pitch. Decode uses genuine native PacketBuffer's 256-character bound. It does not create or register foreign SoundEvents.
- Client handler schedules on Minecraft's main thread, resolves category from the receiving client's existing reload-aware SoundCategoryResolver, constructs an ordinary PositionedSoundRecord and enters genuine SoundHandler. Existing native playback-event cancellation/replacement handlers still run. No global foreign-name wrapper.
- `EntitySoundTransport.dispatch`: downstream listener transport only, taking already-proven explicit coordinates and an explicit encoder dependency. Only exact ServerWorldEventHandler is specialized; reads only its declared server/world fields with verified MCP/SRG names, uses the listener's own dimension/server and native PlayerList radius filtering, excludes nobody. Other listeners and subclasses retain their own native callback with the exact supplied sound/category/raw values. No world list traversal, entity event posting, entity-position conversion or call-site selection is hidden here.
- Existing channel appends client discriminator 33 after unchanged IDs 0..32. NetworkHandler exposes locked native FMLEmbeddedChannel.generatePacketFrom and corrects its pre-existing FMLProxyPacket constructor argument from ByteBuf to a native PacketBuffer wrapping the same allocated/copied buffer. Only this new final packet releases its custom-codec inbound payload after copying values, in a finally block. Existing packets retain their prior ownership behavior; no broad codec cleanup is included.

A future caller must separately prove Java receiver/argument ordering, real entity-event ownership/cancellation, event sound snapshot, live World listener iteration, per-callback position reads, world/client behavior and positions. Null entity timing is not this transport's responsibility. Null sound is forwarded to custom observers; exact built-in transport rejects it when constructing its packet, not as cancellation. Custom ServerWorldEventHandler subclasses retain native packet/category behavior, including full-float pitch, and are not claimed equivalent to legacy callbacks.

## Why candidate calls remain parked

Independent full JavacTask parse+analyze identified 36 eligible literal-HBM player-typed calls across 21 tool/machine files, 11 names and 18 actual bundled samples. Complete invocation/receiver/argument AST spans and types are retained. The four-file infrastructure overlay preserves all 41 prefiltered candidate AST records exactly (36 eligible plus five exclusions). New native destination bindings are attributed; this does not establish a working full mod build.

Vanilla awake server players use legacy yOffset 0, but sleeping and local-player smoothing defeat a blanket player-coordinate proof. At bedY 64, old sleeping soundY is 64.73749999701977 while native posY is 64.6875. Packet quantization happens to agree at normal bed heights, yet native radius filtering can select different recipients. Event/listener movement also prevents reconstructing the old value from sleep state alone. Native getYOffset is a riding adjustment, not the removed field. No positional approximation, sleeping-state rejection or fail-fast compatibility gate has been introduced.

## Scope of proof

`engine-audit/` retains original Forge hook/source and actual old/native bytecode evidence, native event/custom-packet/player-coordinate probes. `before-attribution/` records exact CP59 candidate eligibility. `infrastructure-attribution/` records full genuine-source overlay attribution and native bindings. `runtime/` contains actual new production packet/transport and native codec probes; fixture limitations are identified in its notes/runner. The packet/transport probe passed 110,264 checks (including 11,009 exact old-packet byte comparisons); the actual custom-codec functional probe passed 16 checks and the scoped ownership probe passed 150 checks. Its only project boundary fixture exposes PacketThreading's lock/prefix; executor/logging behavior and full PacketDispatcher initialization are not claimed runtime-tested.

Run from repository root:

    python3 ntm-port-1.12.2/porting/tests/native-entity-sound-transport/check-source.py

Runtime runner and attribution commands are included alongside their evidence. Java 8 and the supplied genuine native classpath are required. No full Gradle invocation, game launch, audible-audio claim or global unknown-mod override is part of this checkpoint. Native resource-name normalization and native callback SoundEvent types remain engine/API boundaries, not claimed exact legacy raw-String observer parity.

Replay fresh runtime and engine evidence (all generated output goes outside this sealed directory):

    bash ntm-port-1.12.2/porting/tests/native-entity-sound-transport/runtime/run-probes.sh "$PWD/ntm-port-1.12.2" "$PWD/ntm-port-1.12.2" /tmp/nagaport-ae2-native-classpath.txt /workspace/shared/nagaport-toolchain/java8 /tmp/nagaport-legacy-client.jar "$(mktemp -d /tmp/entity-sound-runtime.XXXXXX)"
    python3 ntm-port-1.12.2/porting/tests/native-entity-sound-transport/run-engine-audit.py
    python3 ntm-port-1.12.2/porting/tests/native-entity-sound-transport/run-attribution.py

The scripts retained under engine-audit/ and before-attribution/ are historical provenance commands; use the root replay runners above instead of writing into sealed historical directories.
