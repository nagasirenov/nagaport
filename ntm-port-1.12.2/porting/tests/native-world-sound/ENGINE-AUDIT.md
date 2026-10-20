# Coordinate world-sound engine audit

Scope: cached, unmodified Minecraft 1.7.10 client bytecode plus Forge 10.13.4.1614 patches, compared with the actual native Forge 1.12.2-14.23.5.2768 classes on the supplied compilation classpath. No production edits were made by this audit worker.

## Conclusions

- Legacy World.playSoundEffect(DDD,String,FF) loops over the live World-declared listener list in index order. It supplies raw coordinates, volume, and pitch to each IWorldAccess callback. Neither WorldClient nor WorldServer overrides this coordinate method.
- Legacy RenderGlobal.playSound(String,DDD,FF) is a no-op. Calling legacy playSoundEffect on a normal client therefore does not locally play the sound. The separately named boolean WorldClient.playSound method is the actual client playback route used by the received legacy packet. Native RenderGlobal's corresponding IWorldEventListener callback is also a no-op.
- Legacy Forge adds PlaySoundAtEntityEvent to entity-sound methods, not to this coordinate method. Native World.playSound(EntityPlayer,DDD,SoundEvent,category,FF) does add the Forge entity event, including when player is null. Thus that convenience API is not an exact replacement.
- Native WorldClient's EntityPlayer overload only plays if its supplied player equals Minecraft.player. Passing null can accidentally pass that comparison before the local player exists. The listener bridge avoids this edge case.
- The exact World-declared native field is eventListeners, runtime SRG field_73021_x. Reading that field preserves empty/custom worlds without a WorldServer cast, and allows ordinary native listeners to receive the callback. No replacement World or engine API stub is needed.
- Both native and legacy built-in server listeners broadcast to players in the sound world's dimension, excluding only the supplied player reference. Coordinate legacy calls exclude nobody. The radius is volume > 1F ? (double)(16F * volume) : 16D. The multiplication occurs as float before widening; changing it to 16D * volume is not exact.
- Both actual player-list implementations use squared Euclidean distance < radius squared, not <=. Wrong-dimension and explicitly excluded players do not receive the packet. Radius filtering uses raw source coordinates, not packet-quantized coordinates.
- Legacy packet coordinates are (int)(coordinate * 8D), decoded through float division by 8F then widened to double. Native registered and custom sound packets match, including truncation toward zero for negative fractional positions and Java saturation for extreme values.
- Legacy network pitch is max(0,min(255,(int)(pitch*63F)))/63F. The native packets transport float pitch unchanged, so the exact built-in server callback must receive explicitly quantized pitch. Raw pitch must remain visible to other listeners. Volume remains a float without extra clamping.
- The native registered packet resolves the registered SoundEvent ID. Its client handler runs WorldClient, PositionedSoundRecord, SoundHandler, SoundManager, and the real Forge PlaySoundEvent hook. It produces a non-repeating immediate LINEAR sound with float coordinates. A narrowly allowlisted category wrapper can therefore replace only the category using receiving-client resource metadata without replacing samples or introducing a custom transport protocol.

## Explicit compatibility boundaries

- Native callbacks accept SoundEvent and SoundCategory rather than the removed string-only callback. Registration and the selected literal allowlist are intentional native requirements. Null/unknown literals are rejected, even on empty worlds; legacy empty listener lists did not validate those strings.
- Exact ServerWorldEventHandler instances receive legacy-quantized packet pitch. Custom subclass overrides retain raw pitch. A subclass that merely inherits the native built-in method consequently sends native unquantized pitch. This is deliberately tested and is not claimed to be legacy wire parity for arbitrary third-party subclasses.
- The evidence verifies events through the actual native playback stack, with a final event listener cancelling before audio hardware. It does not claim audible hardware output or a launched game world.
- Unsafe is confined to test fixture allocation, bypassing unrelated world/chunk/server/bootstrap/socket construction. Real native callback, registry, packet serialization, recipient selection, resource reload and Forge event methods execute unchanged.

## Tests and evidence

NativeWorldSoundCallbackProbe.java compiles the full actual production WorldSoundEffects, WorldSoundCategories, SoundCategoryResolver, ModSoundEvents, and RefStrings sources before execution. It checks normal/remote/empty worlds, duplicate callbacks, live-list mutation and protected-field list replacement during callbacks, native RenderGlobal silence, raw custom/subclass values, the absence of a new entity hook, null/invalid inputs, exact Java receiver/argument evaluation and failure timing, native server radius/dimension/packet data, real PlayerList recipient boundaries, and actual client packet reception with a receiving-client WEATHER resource override over the packet's BLOCKS category.

PacketParityProbe.java loads the unmodified legacy S29PacketSoundEffect class in a separate loader and compares actual legacy/custom/registered native encode-decode results across 13,332 special-value and deterministic randomized cases. Comparisons use raw floating-point bit representations. Registered event identity survives serialization; legacy/custom names survive their string transport. It is a native encoding audit, not a model-only expected-value test.

The concise/ directory contains exact method excerpts rather than the multi-megabyte full javap output. Inputs, mappings, production sources, and probe sources are hashed. Full raw javap output remains outside that directory for local verification.

Reproduce with run-probes.sh, passing the production project root, the native classpath text file, the Java 8 home, and the cached legacy client jar. Logs are native-world-sound-callback-probe.log, packet-parity-probe.log, and full-production-probe-compile.log.

Final independent runtime result: 3,428 production/native callback checks and 13,332 legacy/native packet differential cases passed.
