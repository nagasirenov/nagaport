# Native client loop-sound family

Baseline: checkpoint35, local revision `39837aa8a5fad4d7fde8657e18d1956881fc2bcc`. No commit or push is made by this work. Original/pristine is unchanged.

## Scope and preservation

The entire ten-class `com.hbm.sound` family is compiled, not extracted methods. Five existing files require migration: AudioDynamic, AudioWrapperClient, MovingSoundPlayerLoop, SoundLoopMachine and SoundLoopSiren. AudioDynamicEntity, AudioWrapper and the three chopper subclasses are unchanged and compile against the migrated roots. Two small native implementation helpers are new:

- ResourceLoopSound uses the genuine PositionedSound(ResourceLocation, SoundCategory) API. MovingSound has only a SoundEvent constructor in this native version, so the original roots now implement genuine ITickableSound and retain the original done-playing field/reader themselves. No unregistered SoundEvent, sound registry placeholder, or fake Minecraft/Forge API is introduced. Native SoundHandler still creates the accessor, validates samples and owns playback.
- SoundCategoryResolver retains the old sounds.json category metadata, which native SoundHandler ignores. It reads active resource packs in their actual resource-manager order. The first entry supplies category; appending samples does not replace it; `replace:true` does. Missing category defaults MASTER. It validates definitions with the actual native SoundListSerializer, rejects an invalid metadata/definition file without partially publishing it, closes resources and publishes a complete immutable volatile snapshot. Every existing loop sound consults the current snapshot, so reload/removal changes its category too.

ClientProxy adds exactly one reload listener after its QMAW/HFR listeners. Its three existing getLoopedSound bodies remain byte-identical. In particular, their previously omitted pitch update is not silently fixed. This parent-owned integration is checked as bounded original bodies plus the exact listener insertion, not a changing whole-file hash.

Both original attenuation formulas, including negative values, NaN/infinity and zero ranges, are retained. Keepalive stops only when the pre-increment counter is strictly greater than the threshold. Dynamic stop delegates to SoundHandler without changing repeat/done. Moving loops retain duplicate-list removal, done/repeat flags and parent clearing. Chopper dying/dead distinctions are unchanged. Machine sounds retain construction-time coordinates and invalidation done latch. Siren list retention, LOOP/PASS/SOUND selection, repeat delay, no-player intended volume and wrong-tile stopping are unchanged. The original local-player +10 Y stereo workaround, entity getter delegation/fallback, and null-player/null-parent exception are explicitly tested and unchanged. AudioWrapper's pre-existing dedicated-server no-op implementation remains unchanged; it is not a newly added stub.

Native PositionedSound multiplies volume/pitch by its selected sample, unlike the original raw getters. ResourceLoopSound retains safe pre-accessor getters; after accessor creation native sample multiplication occurs exactly once. AudioWrapperClient uses explicit unscaled accessors to retain its original raw caller-facing getter contract. Its inherited getRange still returns zero, as before.

## Resources

The selective caller audit covers 69 original event keys and 68 distinct original samples. It includes direct loop calls, all three moving-chopper events, the rocket selector, original door loop methods, all 22 siren tracks and broadcaster's bounded 1..3 selector. All selected entries exist in the original. Only their sample-name namespace is qualified with `hbm:`. Original order, volume/pitch/weight, streaming flags and category metadata are retained. Exactly 43 corresponding filenames are case-only renamed to native lowercase; every OGG remains byte-identical to pristine. No unrelated event or sample is swept into this migration. Manifests record every source, selector, event, before/after resource path and byte hash. Case changes used an intermediate name and leave no stored-name duplicate.

## Evidence and exact boundaries

- `check-native-loop-sound-source.py`: complete ten-source byte reversal, two exact new helper pins, bounded ClientProxy integration, original JSON semantics, all unchanged OGG hashes/stored spellings and caller selectors. Its drift mutations reject source/resource changes and reintroduced old-case names without touching production files.
- `probe-native-loop-sounds.py`: compiles all twelve production classes against the actual installed Forge14.23.5.2768/MCPstable39 JDK8 classpath. No downloaded dependencies or compilation exclusions are used.
- `LoopSoundNativeTest.java`: 5,512 checks. Genuine native SoundHandler parses, validates and resolves all 69 unregistered client resource events in both FolderResourcePack and FileResourcePack. Real SimpleReloadableResourceManager registration and reload dispatch exercise omitted/append/replace/removed categories, existing sound instances, invalid category, invalid sound weight and malformed JSON. Actual native SoundManager delayed scheduling and done-tick dispatch run with narrowly overridden admission/stop hardware boundaries.
- `LegacyLoopSoundOracle.java`: 417 checks executing the cached unmodified official 1.7.10 client bytecode. It verifies all 408 original categories, omitted MASTER, invalid category rejection, actual original SoundHandler append/replace behavior and original raw getters. The optional jar is hash-checked (`a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce`) and is not distributed or a production dependency. The original bss/bsp/btp/bto/bsk mappings come from the existing cached joined.srg; no reconstructed API oracle is used.

Explicit NTM-only collaborator fixtures: EntityHunterChopper exposes getIsDying on a native Entity; TileEntityMachineSiren is a native TileEntity subclass for owner identity/invalidation; ItemCassette supplies its unchanged LOOP/PASS/SOUND enum. Actual Minecraft/EntityPlayerSP instances are allocated without starting a client and their relevant native fields set for controlled tests. Recording SoundHandler and SoundManager subclasses intercept hardware admission/stop only where stated. They do not replace parser/accessor/resource lookup or native scheduling logic.

This is not full-mod compilation, a launched client, audible/OpenAL hardware playback, a live chopper/tile/world, multiplayer synchronization, or a game-save claim. Complete callers such as ClientProxy/ModEventHandlerClient/TESirenPacket are not compiled by this focused test. Full build status belongs to the parent's frozen compile. Native warnings from intentionally malformed packs are expected rejection evidence.

## Reproduce

```sh
python3 porting/tests/check-native-loop-sound-source.py
python3 porting/tests/probe-native-loop-sounds.py \
  --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)" \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --legacy-jar /tmp/nagaport-legacy-client.jar
```

Use the environment's genuine resolved compile classpath/JDK8; the temporary paths above are local conveniences, not required recipient paths. Omit --legacy-jar when that optional official oracle is unavailable. The portable source guard and native tests still run. Final logs and exact deliverable hashes are in native-loop-sound-evidence-scope.json; production scope includes all 12 complete classes, sounds.json and both sides of all 43 case renames.
