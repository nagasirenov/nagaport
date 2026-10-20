# Native teleporter engine boundary

Complete migrated TileEntityMachineTeleporter plus 35 unchanged real production dependencies compile. The actual CP41 tank/loaders/registry/endpoints, loaded tile base, energy receiver/handler, TrackerUtil and BufferUtil are used, not replacements. Twenty-four explicit external NTM collaborators are enumerated in run.py. Server/world/chunk storage and packet delivery IO are captured in native subclasses allocated without running a server. Native entities, registry factories, ItemStacks, NBT, packets, connection setPlayerLocation, tracker broadcasts and Forge event bus run.

Run from the module:

    python3 porting/tests/teleporter-native/check-mapping.py --source-root .
    python3 porting/tests/teleporter-native/run.py --module . --source-root . --java-home /path/java8 --classpath "NATIVE_DEPENDENCY_CLASSPATH"
    python3 porting/tests/teleporter-native/audit-copy-data.py --java-home /path/java8 --classpath "NATIVE_DEPENDENCY_CLASSPATH" --legacy-client /path/original-client.jar --legacy-srg /path/original-srg.zip

The bytecode audit pins original client SHA256 a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce and MCP/SRG archive d49df5c445dcf00ada532ab3b0e4283bf4b05ac13f210647658dfd3cec8b34c2. It performs no downloads and redistributes neither. Whole-source mappings pin CP41 9b83e93f2948b58875c6b841d6bf32c2f0908173 input and expected complete output. Genuine Forge EventSubscriptionTransformer launcher is reused from the existing repository.

54 assertions cover energy threshold/cost, same-dimension no-fluid requirement, cross-dimension 100mB gate, failed target original expenditure, exact coordinates/offsets and target-box selection, one-funded-entity iteration, native tracker broadcast with original negative-angle floor and untouched updateCounter, player native respawn/location/effect packet order, genuine dimension event, player world/interaction-manager synchronization order and non-alive/missing-target quirks. Whole tile/base/tank NBT and42-byte synchronization, actual base duplicate-packet suppression/periodic refresh, unload hook and client-only particle gates also run.

Real native entity factory and NBT cloning preserve UUID/name/custom payload and original source Dimension value, cooldown and direction. This is deliberate original behavior, not a fix: the native private copyDataFromOld helper removes Dimension and copies extra native portal context, unlike original func_82141_a. Native World/WorldServer spawn code was also inspected; complete storage/trackers/chunk spawn lifecycle remains unverified. Original unsuccessful factory still removes source and returns true; no invented rollback/resource refund. Native protocol carries doubles and onGround, not1.7 fixedpoint wire. Global static server lookup is replaced by the entity/machine world's owning server; it is not a cross-server transfer feature.

Actual EntityItem clone test preserves stack count/name/custom NBT and pickup delay. This is payload preservation only, NOT proof of inventory or dropped-item irradiation. Original bread exception runs actual native EntitySlime initial spawn and leaves bread count unchanged. Portal sound maps to native ENDERMEN_TELEPORT/BLOCKS; original client particle gate is retained even for same-dimension target.

No launched server/client, end-to-end dimension save/reload, actual chunk IO, player reconnect, full energy/fluid topology, complete teleporter block/target-setting GUI or multiplayer acceptance. MachineTeleporter block and target-setting callers remain separate legacy work. No fake working-machine claim and no visual/core gameplay simplification.
