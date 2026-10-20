# Scoped native vanilla coordinate sounds

Base: `5e6ba284ef32b46ad5f81cd1e1570c08b53cb24f` (CP65). Separate production and evidence patches. No main-checkout edits or commits.

## Result

- 94 exact literal coordinate calls in 76 consumers now bind to a native World helper. Full genuine-source javac attribution changes 15,608 diagnostics to 15,514: exactly 94 missing playSoundEffect diagnostics removed, zero other diagnostic changes.
- 15 old vanilla resource names receive their exact original category/sample definitions under assets/minecraft/sounds.json. All 15 keys are absent from official native definitions. Both official version/index/resource chains and all 22 identical indexed sample hashes/sizes are pinned in provenance/. No OGG payload was downloaded or added.
- 2,210 actual old/native runtime checks pass, plus 14 provenance positive/tamper-negative checks. One complete production onSuck method compiles and executes using native TileEntity/World coordinates, preserving sound-before-super order.

## Production boundary

VanillaWorldSoundEffects is called only by the 94 audited literal coordinate expressions. It preserves complete receivers, expression order, Java numeric widening, live indexed World listener iteration, duplicate listeners, in-callback additions/removals/list replacement, null-receiver timing, client RenderGlobal silence and absence of the native entity-sound hook.

Other listeners and ServerWorldEventHandler subclasses receive the genuine registered native SoundEvents object, original bundled category and raw numeric values. They retain ownership of their implementation. No vanilla registry entry is created, replaced or intercepted. The exact built-in server listener uses a new explicit-resource-name overload of EntitySoundTransport and the existing LegacyEntitySoundPacket, discriminator 33, custom locked NetworkHandler codec, main-thread handler and buffer ownership contract. Its packet body is byte-identical to actual original hc/S29PacketSoundEffect for the original raw name, coordinates, gain and pitch. Recipient radius remains float-first and the listener's own dimension is used. The existing arbitrary/entity dispatch overload is byte-for-byte unchanged; no entity or sleeping-sound call is activated.

The client packet keeps the original resource alias, rather than asking the modern event to choose a different category or sample pool. The original sample names/properties are copied, not references to modern events. Native sample resources are supplied by Minecraft itself. This retains original alias-name and sample-path pack overrides; a pack changing only a modern event name does not automatically rewrite a legacy alias. No universal legacy-resource-pack conversion is attempted.

Existing resolver semantics remain: first category on append, replacement category on replace, MASTER if replacement omits category, atomic malformed-pack exclusion, and fresh metadata on reload. Client playback uses the receiving resource snapshot. The extra resolver logic is diagnostic only, transactional and limited to the 15 names. It logs both source pack names and append/replace at most once per alias per reload. It never logs on playback, changes ownership or guesses whether an override is intentional. A native missing sample emits the engine's warning and missing selection, not a substitute sound or exception.

## Compatibility gates and untouched calls

A different mod defining the same formerly absent alias is not taken over: ordinary resource order/append/replace remains in effect, with the bounded runtime duplicate warning. The warning cannot distinguish intentional resource packs from unknown-mod collisions. Such combinations require pack/mod integration testing. No claim of arbitrary collision parity is made.

Two additional literal coordinate calls remain unchanged because full attribution is not proven: TileEntityRBMKKeyPad:119 has unresolved worldObj; ExplosionNT:187 has unresolved explosionX/Y/Z arguments. Dynamic names, HBM custom names/registrations, existing category wrappers, entity paths, sleeping positions and unrelated gameplay are untouched. The helper's unsupported-name guard is private to this explicit bounded call family, not a global unknown-sound policy.

## Native runtime evidence and explicit boundaries

runtime/run-probes.sh compiles the exact production helper, transport, packet, resolver, NetworkHandler and ThreadedPacket against the pinned genuine Java8/native dependency classpath. The PacketDispatcher fixture supplies only the existing wrapper field/bootstrap boundary; unrelated 33 packet registrations are not compiled here. Full genuine-source attribution binds the helper to the actual dispatcher, and check-source.py verifies the original packet's real registration index. PacketThreading's lock/prefix/executor is the same disclosed fixture as prior transport evidence.

The probe executes real old/new sound serializers, old SoundHandler category merges, native registry identity, native listeners, project custom codec/channel, Minecraft task queue, native SoundHandler/SoundManager and Forge playback event. Audio output is deliberately stopped by the final event sink. Native accessor tests use empty sample streams strictly as an existence/metadata boundary, not audio bytes; official indexes independently prove original/native asset hash equality. Missing samples exercise the real engine warning. Exact native sample selection and gain/pitch scaling, append weight and replace pool isolation are checked.

Prior native-entity-sound-transport evidence remains applicable to the unchanged packet/codec: malformed names/truncation, native PlayerList recipient filtering, locking and abnormal buffer ownership. This batch tests the new dispatch route with 75 original packet-body comparisons, plus live client events after category overrides. It does not repeat the unchanged full infrastructure matrix.

No complete mod build, game/client launch, audible output, OGG decoding, resource-pack/mod collision integration, or release readiness is claimed. Other compile blockers remain; checkpoint publication stays subject to the parent's existing gate.

## Replay

```sh
python3 porting/tests/native-vanilla-sound-aliases/check-source.py --project "$MODULE" --proof "$EVIDENCE/proof"
python3 porting/tests/native-vanilla-sound-aliases/provenance/verify_provenance.py
python3 porting/tests/native-vanilla-sound-aliases/provenance/test_verify_provenance.py
"$EVIDENCE/runtime/run-probes.sh" "$MODULE" /tmp/nagaport-ae2-native-classpath.txt /workspace/shared/nagaport-toolchain/java8 /tmp/nagaport-legacy-client.jar /tmp/fresh-vanilla-alias-proof
```

All script path arguments should be absolute for runtime replay. The runtime work directory must be fresh and outside sealed evidence. sources.zip contains original edited Java files, not platform stubs. manifest.json records full AST invocations, arguments/types, enclosing methods and line-context fragments. Exact whole-file reversals and diagnostic multisets are checked. merge.py provides an optional exact-fragment composition over independent CP66/CP67 edits, with a complete inverse preserving every unrelated input byte; by default it only dry-runs. It rejects new-path collisions and changed transport/resolver contracts rather than overwriting them. The archived dry run succeeded against CP67, and no main files were written.

The whitespace diagnostic is retained in proof/whitespace-check.log: mixed indentation and CRLF on six edited production lines are inherited verbatim, proven by the complete source inverse. The seventh diagnostic is a raw native reload log's trailing space. They are not cleaned up as unrelated edits or rewritten test output.
