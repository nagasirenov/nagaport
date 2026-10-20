# Native packet/type declaration boundary (checkpoint 45)

Exact base CP44 7cd6c88acefd38915a8495320cbd200b1f2bff17. Nine existing Java files change by explicit counted mappings; no new production classes or old-API emulation. This checkpoint removes declaration blockers, not complete projectile/GUI/turret/world correctness.

- EntityBullet, EntitySchrab, EntityRocket: native SPacketChangeGameState and player connection sender. Original event6/value0 and every enclosing hit condition remain byte-identical after inverse mapping.
- XFactoryDrill: SPacketEffect with native BlockPos and connection sender. Original effect2001, block-ID plus metadata-shift12 payload, target coordinates and false global flag retained.
- GUIMachineRadarNTSlots: CPacketCloseWindow and native client player connection. Does not replace the operation with closeScreen, so the original separate server-container close intention is retained. Complete GUI/client cursor behavior is not yet runnable or certified.
- EntityMinecartModBase/EntityMinecartDeobf: two exact class-package moves for command/spawner minecarts. Constructor arguments and factory branches unchanged; complete custom minecart physics remains outside this batch.
- ExplosionNukeGeneric: native GameType import and interactionManager member. The exact CREATIVE comparison is unchanged, with no added spectator or other balancing exemption.
- TileEntityTurretBaseNT: EntityDragonPart becomes MultiPartEntityPart. Pinned original class wy constructor accepted generic IEntityMultiPart wx already; this is not a new blanket exemption invented for non-dragon entities.

Run from module:

    python3 porting/tests/native-declarations/run.py --source-root . --java-home /path/to/java8 --classpath "native-classpath" --legacy-client /path/to/original-1.7.10.jar

371 assertions pass. The runner hash-checks original official client SHA-256 a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce, loads its actual gv/iy/ha packet classes in an isolated URLClassLoader, and executes original serialization/decode alongside native classes. Game-state and close-window packet bytes match. Native block-effect packed-position wire intentionally differs from the old x/int,y/byte,z/int representation; decoded semantics agree across negative/positive X/Z, legal world heights0/64/255 and metadata0/7/15. Native handler dispatch executes through recording interface proxies. No connected server or actual screen/audio/terrain effects are claimed.

Actual native multipart construction/damage delegation and relocated minecart kinds/coordinates execute. The test compiles native sender/member chains for server/client packets and creative-mode lookup. Exact source forward/reverse checks preserve the complete nine source files outside counted edits. This is NOT compilation or behavioral certification of the entire nine owners; their remaining bodies still need migration after shared declaration roots are resolved. Source transformation checks alone are not runtime proof.

Original SRG signature audit: gv=S2BPacketChangeGameState,ha=S28PacketEffect,iy=C0DPacketCloseWindow,wy=EntityDragonPart,wx=IEntityMultiPart,xn=EntityMinecartCommandBlock,xt=EntityMinecartMobSpawner,ahk=WorldSettings$GameType. Original case6 client handler uses random.successful_hit with volume0.18/pitch0.45; native handler uses ENTITY_ARROW_HIT_PLAYER with same values. This audit does not claim byte-identical game sound assets or network protocol interoperability between versions.

Full offline clean build FAIL135 diagnostics versus146,56seconds,exit1. All nine edited sources match the frozen staged tree, independent snapshot and corresponding generated compiler inputs among 4007. Full historical source guards pass9existing/0new/27drift mutations. No source exclusions, production stubs or new dependencies. CP37–45 remain local-only/withheld pending CP37 lighting policy. CP44 damage-light discrepancy remains open and is not waived by this checkpoint.
