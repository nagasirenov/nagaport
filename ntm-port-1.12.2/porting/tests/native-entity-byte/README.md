# Five-owner projectile BYTE synchronization

Exact baseline: 36387bc1c10fa9c36fbb27f5e9d44b45d474eb18 (CP62). Independent of the preceding Vortex patch. Six changed production files, 39 legacy operations, seven private owner-allocated DataParameter<Byte> keys using native DataSerializers.BYTE.

## Ownership and semantics

- EntityBullet extends Entity: original slots16/17/18, all Byte0, become CRITICAL_FLAGS / TAU_FLAGS / CHOPPER_FLAGS, all created against EntityBullet.class. Each setter preserves all seven upper bits and changes only bit0 with the original OR1 / AND-2 expressions. All boolean getters test only bit0. These must remain BYTE, not BOOLEAN.
- EntityRocket, EntitySchrab and EntityExplosiveBeam each directly extend Entity and independently own CRITICAL_FLAGS, original slot16 Byte0. Same bit0 behavior; key objects are not shared between classes even though all allocate native index6.
- EntityShrapnel extends vanilla EntityThrowable: original slot16 Byte0 becomes SHRAPNEL_TYPE. Unlike the four flag owners, its four boolean setters replace the entire byte with mode1/2/3/4 or zero. setWatz writes3, setRadVolcano writes4 exactly as before. New getShrapnelType is a raw byte accessor. Particle/impact comparisons and formulas are unchanged.
- All seven register via existing entityInit on both sides; no new world-side condition. Vanilla Entity has six keys; Bullet uses native6/7/8, other owners native6. EntityThrowable.entityInit is the existing no-op characterized in the previous scalar evidence; preserving Shrapnel's non-super-calling override loses no parent metadata.
- A full-tree inheritance search finds no descendants of these five classes. No custom parent fixture, mutable synchronized type, manager adapter or fabricated platform class.

## External consumer closure

RenderShrapnel had the only external numeric consumer of these entries. It is shared by EntityShrapnel AND EntityFireworks. The original `rocket instanceof EntityShrapnel` guard stays in place, with only its nested getter changed to `((EntityShrapnel) rocket).getShrapnelType()`. The signed `>=2` comparison is preserved. The guarded condition is tested for every signed byte and for a non-Shrapnel native entity, so the shared Fireworks route cannot trigger this cast.

The full source was searched for byte16/17/18 accessors and DataWatcher aliases. Remaining slot16 reads belong to SiegeCraft/UFO/BFAngel/HunterChopper/Bomber/RenderBomber. Remaining literal slot17 read belongs to RenderGlyphidNuclear; slot18 has none. Nonnumeric byte getters belong to Glyphid/UndeadSoldier named flags, not this family. No declared DataWatcher type aliases exist. Unrelated mob flags remain untouched; this is not boss work.

## Explicit old NBT behavior

The four arrow-like owners keep their exact original xTile/yTile/zTile/life/inTile/inData/shake/inGround/pickup/damage methods and supporting field defaults. None writes critical/tau/chopper metadata. Their fresh loaded entities therefore retain registration defaults0; reading NBT into an already flagged instance does not reset those unsaved flags. No new persistence has been introduced. Native CompressedStreamTools binary NBT is exercised with the actual production methods. Test block fields use their original null default, not a fabricated registry or world; full block identity/world save gameplay is not claimed.

Shrapnel still returns false from writeToNBTOptional and performs real native EntityThrowable.readEntityFromNBT then setDead. Its mode is not newly persisted. Other existing native Throwable state behavior is not modified.

## Evidence

source-manifest.json records owner/superclass, receiver type, slot/type/default and original/baseline/result SHA256 plus exact full before/after statements, line positions and enclosing source methods. audit.py independently checks these ranges and declarations and reconstructs all six CP62 source files byte-for-byte after removing only the new key/import/accessor declarations and reversing calls. It also verifies descendant closure, guarded renderer context and remaining unrelated literal slot consumers.

Original/pristine watcher statements match the baseline, except an explicitly recognized prior port change in one Shrapnel particle condition: `worldObj.isRemote` had already become `this.world.isRemote`. The audit permits only that receiver-name normalization for the original comparison; its exact CP62 inverse guard permits no unrelated current edits. An initial strict pristine comparison exposed this pre-existing difference and was corrected with that narrow documented normalization, not relaxed broadly.

The native probe extracts exact synchronization accessors, entityInit, save methods and supporting field declarations into test-only shells deriving directly from real Entity/EntityThrowable. Unrelated heavy gameplay is not compiled in this bounded probe, but is retained in production. Shrapnel's unused abstract impact hook explicitly throws if called. The exact guarded renderer condition is tested without GL.

PASS: 8,821 assertions. One exhaustive native SPacketEntityMetadata roundtrip sweep covers all256 signed BYTE values rather than repeating full protocol suites for every owner. Per-owner/default/independence checks and one edge packet per channel establish correct allocations and binding. Exact production bit setters/getters are checked exhaustively across all256 bytes on all six flag channels, including upper-bit/sign retention, changed-value dirty behavior and idempotence. Mode setters, raw signed renderer conditions, binary NBT omissions and death-on-load are also tested. Immutable Byte values require no ItemStack alias workaround. Old-client wire compatibility is not claimed.

No full entity, renderer, mod, client or server compilation/gameplay acceptance is claimed. Legacy projectile movement/block/brightness/render APIs remain. No spawn calls, damage formulas, caps or balance changes.

Run from project root:
python3 porting/tests/native-entity-byte/audit.py /workspace/shared/nagaport/ntm-original-pristine
JAVA_HOME=/workspace/shared/nagaport-toolchain/java8 python3 porting/tests/native-entity-byte/probe.py

Classpath defaults to /tmp/nagaport-ae2-native-classpath.txt; override NAGAPORT_NATIVE_CP_FILE. No Gradle invocation, download or dependency installation.
