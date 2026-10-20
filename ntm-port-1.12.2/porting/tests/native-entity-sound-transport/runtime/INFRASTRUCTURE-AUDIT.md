# Dormant entity-sound downstream infrastructure audit

Tested exact production LegacyEntitySoundPacket and EntitySoundTransport from the isolated CP59 worktree, plus unchanged SoundCategoryResolver from main, against native Forge 1.12.2-14.23.5.2768. No production source was edited by this audit. World/entity call activation is deliberately out of scope and is not proven safe by these tests.

## Results

NativeEntityTransportProbe: 110,264 checks passed, including 11,009 actual 1.7.10 S29PacketSoundEffect byte-for-byte differential cases. Coverage includes all packet fields, random raw float/double values, NaN/infinities, negative/fractional/extreme coordinates, raw arbitrary/foreign/mixed-case/Unicode names, empty names, 256/257-character and write-byte bounds, null construction, truncated and malformed reads. Client coordinate float rounding is compared with the old packet's float division semantics.

The production transport executes against real ServerWorldEventHandler, MinecraftServer and PlayerList classes. Exact built-in instances use the scoped packet and actual listener-owned dimension/server. Custom observers and subclass overrides receive exact SoundEvent/null/category and raw floats without sound-name dereference or encoder use. Inherited subclasses retain their own native SPacketSoundEffect path. Actual native PlayerList filtering through production dispatch checks strict radius boundaries, wrong dimension, nonfinite positions, origin player included, and the identical encoded packet object passed to recipients.

Genuine SimpleNetworkWrapper registration and FMLEmbeddedChannel encoding/decoding execute with actual production handler. A network thread enters actual Minecraft.addScheduledTask; no sound event fires until the main thread runs the native FutureTask. Unregistered foreign replacement resource metadata resolves WEATHER on the client before creating an ordinary PositionedSoundRecord. Actual SoundHandler, SoundManager and ForgeHooksClient PlaySoundEvent run, with audio stopped by a final event observer. Main-thread direct scheduling executes immediately as native Minecraft specifies. No fake Minecraft/Forge or fake production classes are used in this first probe.

NativeCustomCodecProbe compiles the exact production NetworkHandler after its approved native PacketBuffer constructor prerequisite, the unchanged actual ThreadedPacket, and the exact production packet/handler. The only project boundary fixture is PacketThreading, exposing the real ReentrantLock type and unchanged threadPrefix constant. Its unrelated executor, configuration, logging, task management and full lifecycle are NOT tested. There are no engine API stubs or altered codec source.

The actual custom nested PrecompilingNetworkCodec passes functional checks for discriminator 33, exact message bytes, channel identity, one owned outbound payload reference, native outbound-target restoration, lock blocking and balanced unlock, native custom decode, actual client scheduling, foreign-name resource category and normal Forge playback event. Full PacketDispatcher initialization/registration of all existing project packets is not executed; registration of the exact new handler at discriminator 33 is executed directly through actual NetworkHandler.registerMessage.

## Blocking ownership observation before scoped cleanup

The initial actual custom codec run leaves FMLProxyPacket.payload refCnt=1 after successful decode. The original standard native SimpleNetworkWrapper codec releases it to zero. Native FMLProxyPacket.processPacket does not release it afterward; it only dispatches into the embedded channel and clears its message queue. Thus the custom codec has a pre-existing successful-decode retention, not a double-release caused by the new native PacketBuffer wrapper.

No production fix was made by this audit. The parent subsequently authorized and implemented a release limited to this final new packet class; its accepted result is recorded below. Releasing inside packet.fromBytes would violate the caller's ownership and double-release standard native codec use; a scoped custom-codec finally is the proposed boundary. Unrelated existing packet ownership must not be changed by this test.

custom-codec-before-ownership-fix.log preserves the initial 15-check functional result and explicit refCnt1 finding. The final reusable runner requires refCnt0 and does not accept the historical retained state.

## Fixture limitations

Unsafe is used only to avoid unrelated server/world/chunk/client/audio-hardware/socket bootstrap. Native methods under test are unmodified. Real client queue tasks are run explicitly on the designated Minecraft main thread, not by launching an actual game loop. Socket delivery is captured at NetHandlerPlayServer.sendPacket; native recipient selection executes unchanged. Event observers cancel before hardware audio. No audible output, playable world, complete mod build, PacketThreading lifecycle, all-packet registry linkage or activated entity hook/coordinate semantics is claimed.

## Reproduction

Run run-probes.sh with overlay project root, baseline project root, native classpath file, Java 8 home, cached legacy client jar, and a sixth mandatory fresh empty work directory outside this evidence directory. All generated classes, logs, hashes, and native runtime log output go only in that supplied work directory; sealed evidence and historical logs are not overwritten. It compiles exact source paths and writes tested-inputs.sha256 plus compiler/runtime logs. The runner uses only the explicit PacketThreading boundary for the custom codec probe. The independent engine audit, player-state coordinate blocker, and scoped packet architecture decision remain separate evidence.

## Final scoped ownership proof

The parent implemented NetworkHandler.java lines 88-102: a finally releases inboundBuf only when originalMsgClass equals the final LegacyEntitySoundPacket class. Packet.fromBytes stays unchanged and never releases caller-owned buffers. Existing packet classes follow their previous ownership.

Fresh end-to-end replay passed all three probes: 110,264 packet/transport/native-codec checks, 16 actual project custom-codec functional checks, and 150 scoped ownership checks. Final fresh proof directory: /workspace/shared/entity-sound-transport-replay.IlUKNn. Its tested-inputs.sha256 pins exact production and fixture/probe sources. No further production change is required by this audit within the dormant approved scope.

ScopedCodecOwnershipProbe executes actual FMLProxyPacket.processPacket on a network thread for the successful route, not merely direct handler invocation. It verifies one fresh inbound payload reference, explicit non-ReferenceCounted FML message release is a no-op, successful custom decode releases to zero, and copied packet fields remain usable by the subsequently scheduled client task. Every truncation after known discriminator33, invalid/overlong string bounds, immediate handler exception, and delayed playback exception retain refCnt0 without double-release.

The exact existing production ExplosionKnockbackPacket and its actual handler serve as a control: decoded motion values are applied correctly, and its previous payload reference count remains1. Unknown-discriminator and zero-length envelopes also retain their previous behavior, since they cannot be identified as the new class. This is intentional scope preservation, not a claim that the unrelated pre-existing codec issues were globally fixed.

Ownership evidence is structural and runtime: LegacyEntitySoundPacket has only String/primitive fields; its decoder copies rather than retaining the supplied slice. Native FMLProxyPacket implements Packet, not ReferenceCounted, and its processPacket has no payload release. Netty MessageToMessageDecoder's automatic ReferenceCountUtil.release targets that non-reference-counted FML message, not the contained buffer. The new exact-class codec finally is the unique consumer release for this message. Native SimpleNetworkWrapper's own codec still releases its own input once, which is why fromBytes must remain non-owning.

Source evidence: native-FMLProxyPacket-process.txt and native-MessageToMessageDecoder.javap; native-FMLIndexedMessageToMessageCodec.javap is the standard-codec comparison. All engine classes remain unchanged. Deliberate malformed/exception tests generate expected native error logs; the final PASS line and exit status establish the expected assertions.

## Final reusable source set

- NativeEntityTransportProbe.java
- NativeCustomCodecProbe.java
- ScopedCodecOwnershipProbe.java
- ForgeEventNativeLauncher.java
- fixtures/com/hbm/handler/threading/PacketThreading.java
- run-probes.sh
- INFRASTRUCTURE-AUDIT.md

The runner additionally compiles the unchanged actual ThreadedPacket and ExplosionKnockbackPacket sources. The PacketThreading fixture limitation remains explicit; full project initialization and all-packet registry linkage are not claimed. Actual native event/category/transport handling is proven, not gameplay activation or the unresolved cross-version player coordinate mapping.
