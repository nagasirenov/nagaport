# Entity world-sound audit

Scope: `World.playSoundAtEntity(Entity,String,float,float)` at sealed CP59 `e72ec3d2ae8db6653ebaad529a574ee32c99260b`, cached exact Minecraft 1.7.10 client bytecode plus Forge 10.13.4.1614 source patches, compared with actual Forge 1.12.2-14.23.5.2768 classes on the supplied native compilation classpath. Read-only production audit; no production edits.

## Decision

Architecture feasibility is pending parent review and explicit acceptance of player-state coordinate boundaries. Neither native `Entity.playSound` nor `World.playSound` is a semantic replacement. The bridge needs its own one-time native Forge entity hook followed by the existing live World-declared listener traversal pattern, with the entity coordinates fetched separately for each callback. Strict universal cross-version parity is not established: arbitrary legacy event name strings and legacy player/custom entity position/ySize state have no automatic registered-native equivalent. The parent is reviewing a scoped NTM-owned packet proposal; no implementation has been approved by this audit.

## Exact legacy contract

1. Invocation receiver and every argument expression execute according to Java invocation order; a null receiver fails after argument evaluation.
2. Forge constructs `PlaySoundAtEntityEvent` with the supplied entity, string name, volume and pitch, and posts once even on a remote/empty World. Entity may be null. The event's entity, volume and pitch fields are final; only name is mutable. Cancellation returns before accessing listeners or entity coordinates.
3. World reads `event.name` once after the post. It does not read event volume/pitch or check a null name. A retained event mutated later by a callback does not alter the already-read name.
4. World indexes the current World-declared listener list. Each iteration rereads list size and the list field for get(i), preserving replacement, insertion/removal, order and duplicates.
5. Each callback receives the current entity `posX`, `posY - (double)yOffset`, `posZ` and the original volume/pitch. Coordinates are read only after the callback target has been selected and anew on each iteration. Event handlers and earlier listeners may move the entity. A null entity can be canceled; an uncanceled null entity on an empty list does not fail.
6. There is no entity silence test, no equality check between supplied world and entity.world, and no player exclusion. A player passed to playSoundAtEntity is still included. The separate playerSoundToNearExcept overload has a distinct excluding callback and is outside this port.
7. Neither WorldClient nor WorldServer overrides this entity/string signature. A regular client uses the same listener traversal. Legacy RenderGlobal's string and player-excluding sound callbacks are no-ops. This is not the local playback API.
8. Built-in server listener broadcasts dimension/radius-limited packets excluding nobody. The existing coordinate audit proves radius, packet coordinates, pitch quantization, client playback, and custom-listener boundaries; these downstream facts apply unchanged here.

## Native API differences

- Native Forge entity event exposes immutable default volume/pitch but separate mutable new volume/pitch, mutable SoundEvent, and mutable category. Native World consumes all mutable getters. For the old ownership contract, the bridge must preserve original volume/pitch and not grant the new event category setter ownership over the legacy receiving-client resource category.
- Native World's entity argument is an EntityPlayer used for exclusion, not an arbitrary source entity. Passing null avoids exclusion but posts an event with null source, losing original entity identity. Calling it after posting the correct event posts a second event.
- Native WorldClient overrides this API: it only locally plays when the supplied player equals Minecraft.player, bypassing World's event/listener route. Before a local player exists, null equals null and can enter playback. A direct listener bridge avoids both changes.
- Native Entity.playSound adds `isSilent()`, uses entity.world, and for a nonplayer passes null as World's event source. Native EntityPlayer.playSound passes itself for exclusion; EntityPlayerSP has its own event then local playback. None substitutes for an arbitrary supplied World's original method.
- Native event `setSound(null)` does not itself cancel. Native World nevertheless returns early for null. The legacy method does not. A bridge can preserve null behavior by forwarding null to observers/no-op RenderGlobal; the exact built-in native packet constructor rejects null sound just as legacy S29PacketSoundEffect rejects null name. Do not silently translate null into cancellation.

## Y coordinate conclusion

Native `Entity.setPosition` sets bounding-box minY directly to posY, and `resetPositionToBB` sets posY to minY. Therefore ordinary native feet `entity.posY` is the intended replacement for legacy `posY - yOffset`, not eye height, getEyeHeight, getMountedYOffset, or `posY - getYOffset()`.

Native getYOffset is a riding adjustment: Entity returns 0, EntityPlayer returns -0.35. Legacy Entity.getYOffset returned yOffset and EntityPlayer returned yOffset - 0.5; matching method names is not a field replacement.

Do not claim exact arithmetic identity for every custom entity. Legacy setPosition computes minY as posY - (double)yOffset + (double)ySize, and movement reconstructs posY as minY + (double)yOffset - (double)ySize. Native has removed that offset/smoothing state. Floating-point roundoff can also make a numeric normalize-then-subtract comparison differ. A bounded call-site/entity migration can use native feet semantics; arbitrary unported custom yOffset/ySize state cannot be recovered by this sound helper. NTM explicitly sets yOffset in several original custom entities, including EntityTNTPrimedBase and EntitySiegeTunneler.

## Event replacement/category boundary

A legacy handler could replace name with any string, not merely a source literal in an allowlist. Native server sound-effect packets use registered SoundEvent identities. Restricting post-hook replacements to the original-literal allowlist or accepting an unregistered SoundEvent would not preserve arbitrary legacy handlers. The selected original NTM sources contain no PlaySoundAtEntityEvent handler and no generic Event/EntityEvent handler for this event; EntityEvent.EntityConstructing handlers do not receive it. Third-party compatibility is a distinct boundary, not proven by absence of an NTM handler.

Receiving-client sound category remains determined by replacement name and resource metadata. A wrapper restricted to source-literal sound IDs does not automatically cover renamed events outside that list. Do not derive legacy category from the source entity's category, blindly honor new mutable category, or claim arbitrary replacement categories are preserved without tests.

## NTM handlers inspected

Original `ModEventHandlerClient.onPlayMusic(PlaySoundEvent17)` suppresses overlapping music and replaces music outside Earth. `onPlaySound(PlaySoundEvent17)` suppresses all sounds in vacuum, inspects chopper/crashing/mine marker names and coordinates, and starts/restarts managed loop sounds. These are downstream actual-playback events, not entity-source events. Existing port uses native PlaySoundEvent and retains these hooks. ItemPill's PlaySoundEvent import is unused. Exact handler excerpts are included.

## Proposed bridge shape and required tests

- Validate only the World receiver before method-body work, preserving ordinary argument evaluation timing. Resolve the bounded source literal to the registered native event/category using explicit approved metadata.
- Post exactly one real native entity event with the actual entity argument and original floats, including remote/empty worlds. Return only on cancellation.
- Snapshot replacement SoundEvent once. Do not reread it during callbacks. Retain original floats. Define registered replacement/category boundaries explicitly; preserve null without treating it as cancellation.
- Traverse the exact mutable World eventListeners field in indexed order. Fetch entity posX/native-feet-posY/posZ afresh for each callback. Pass null excluded player even when entity is a player. Do not invoke native World or Entity convenience sound methods.
- Preserve raw values for ordinary/custom listeners. Apply CP58's legacy byte-pitch conversion only to exact native built-in server handler instances and keep the documented inherited-subclass boundary.

Tests should cover actual event source identity and single count, cancellation before dereference, null entity on empty/canceled worlds, event-time coordinate movement, callback-time coordinate movement, retained-event mutation after post, field-list replacement, player included in broadcast, remote RenderGlobal silence, event-name replacement/null, mutable native floats ignored, unchanged packets/category/resource hooks, and receiver/argument evaluation order. No production bridge is tested by this audit's small event probe.

## Runtime evidence

`NativeEntitySoundEventProbe.java` executes unchanged native Forge event creation/posting and native World callback dispatch, with the genuine Forge event-subscription transformer from the previous audit launcher. Unsafe only skips unrelated engine startup for fixture entities/worlds. It passed 18 checks: exact entity identity, null source, one post per API invocation, sound/category setters, mutable floats vs immutable defaults, null sound not cancellation at event level but early return in World, cancellation, and native player exclusion.

The probe is an engine-contract audit, not a production-helper test, running game world, or audible-audio claim. Logs and compiler output are included. Full legacy/native Entity javap files are retained locally alongside concise method excerpts. Input hashes are in inputs.sha256.

## Extended audit: native custom packet and scoped proposal

The genuine native SPacketCustomSound route transports the supplied non-null string unchanged, without SoundEvent registration, including mixed-case and unregistered names. Its decoder's 256-character limit is the same as the old sound packet. The client constructs ResourceLocation and PositionedSoundRecord, then invokes the ordinary SoundHandler/SoundManager/Forge PlaySoundEvent stack. Existing native ServerWorldEventHandler does not use this route; it always constructs SPacketSoundEffect.

The custom route therefore solves registration-free wire delivery, but not arbitrary replacement category ownership in a bounded helper. The receiving client can have a replacement name whose resource category is WEATHER, while the packet carries MASTER; the existing original-name allowlist leaves it MASTER. Broadly wrapping unrelated native sound packets would exceed this bounded change. The parent is reviewing a scoped NTM-owned packet carrying the old name/encoded coordinates/volume/byte pitch, resolving category client-side before ordinary PositionedSoundRecord and SoundHandler playback. This is a proposal only, not approved implementation here. There must be no newly invented fail-fast gate for arbitrary or null post-hook replacement.

Custom native listeners accept SoundEvent/null, not a raw string. They can receive the exact post-hook object/null; generic raw-string listener equivalence is not available from the native interface. Native ResourceLocation lowercases namespace and path, whereas the old single-string ResourceLocation lowercases namespace but retains path case. Native string packet transport itself retains raw case; its client parsing does not. A scoped native packet still reaches the native resource namespace model and should not claim unchanged case semantics for arbitrary old resource names.

Exact built-in listener field mappings: server / field_72783_a, world / field_72782_b. Both are private final on ServerWorldEventHandler. Dispatch using these exact fields would preserve a listener attached to a different supplied caller World; using the caller's server/provider instead would not. A proposed replacement must continue using the exact native PlayerList recipient method and raw coordinates/radius, not encoded coordinates.

NativeCustomSoundProbe passed 28 checks through actual native packet serialization/client handling/playback event, without registered replacement SoundEvent and with actual receiving-client category reload. It demonstrates the allowlist category gap, mixed-case transport/native parsing distinction, and actual null-constructor rejection. Source and log are included. No production edit or audible playback is claimed.

## Extended player-state result and limits

The baseline label is CP59, not CP58. The earlier conclusion is now explicitly pending architecture/player-state acceptance.

The ordinary old EntityPlayerMP constructor sets yOffset=0. Its resetHeight override also sets 0. EntityPlayer wake invokes that virtual reset before setting y to (double)((float)spawnY + yOffset + .1F). With MP yOffset=0, the result is exactly the native wake expression (double)((float)spawnY + .1F), assuming the same wake branch and spawn position. This is not a universal lifetime invariant.

Old EntityPlayer sleep sets yOffset=.2F and posY=(double)((float)bedY+.9375F). EntityPlayerMP's sleep override calls this parent method and does not restore yOffset. Native sleep sets posY=(double)((float)bedY+.6875F). At bedY=64, the original callback Y is 64.73749999701977 and native feet Y is 64.6875. Sleeping therefore disproves an unconditional exact player mapping, including real multiplayer players.

The packet nuance is favorable but insufficient: PlayerSoundCoordinateProbe checks every vanilla bedY 0..255 and additionally -256..-1. All have the same (int)(soundY*8D) in both versions. Every normal-height raw origin still differs. At the deliberately out-of-range bedY=16777216, even packet integers differ.

A concrete actual native recipient-filter run proves the remaining difference is consequential: at bedY64, radius16, recipientY80.7 and matching dimension, PlayerList.sendToAllNearExcept includes the recipient using the original raw origin and excludes it using native feet Y. Both variants can carry the same encoded packet position. Thus preserving packet positions alone does not preserve broadcast recipient sets or custom callback values.

PlayerSoundCoordinateProbe passed 771 checks. Its sleep arithmetic is directly transcribed from exact bytecode, not a claim that full sleep methods were executed; the native PlayerList filtering is executed unchanged. Event-time/listener-time movement of a sleeping entity means a correction computed solely from the bed location is not a general fix. Legacy local-player sneak ySize smoothing provides an additional removed-state boundary. No blanket player state equivalence is claimed.
