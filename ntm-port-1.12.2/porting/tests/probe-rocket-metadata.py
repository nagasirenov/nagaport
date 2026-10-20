#!/usr/bin/env python3
"""Whole production RocketMetadata with native packets; explicitly extracted caller fixtures.
No RocketStruct/EntityRideableRocket whole-class or runnable-mod claim.
"""
import argparse
import os
import importlib.util
from pathlib import Path
import re
import subprocess
import tempfile

p = argparse.ArgumentParser()
p.add_argument('--classpath')
p.add_argument('--java-home', type=Path)
p.add_argument('--source-only', action='store_true')
a = p.parse_args()
root = Path(__file__).resolve().parents[2]
pristine = root.parent / 'ntm-original-pristine'
src = Path('src/main/java/com/hbm')
audit_spec = importlib.util.spec_from_file_location('rocket_source_audit', root / 'porting/tests/check-rocket-metadata-source.py')
audit = importlib.util.module_from_spec(audit_spec)
audit_spec.loader.exec_module(audit)
audit.main()

def compact(s):
    s = re.sub(r'/\*.*?\*/', '', s, flags=re.S)
    s = re.sub(r'//[^\n]*', '', s)
    return re.sub(r'\s+', '', s)

def block(s, signature):
    start = s.index(signature)
    opening = s.index('{', start)
    i, depth = opening + 1, 1
    while depth:
        depth += (s[i] == '{') - (s[i] == '}')
        i += 1
    return s[start:i]

def source(relative, old=False):
    return ((pristine if old else root) / src / relative).read_text(encoding='utf-8')

def replace(s, new, old, count=1):
    new, old = compact(new), compact(old)
    assert s.count(new) == count, 'Unexpected migration shape: ' + new
    return s.replace(new, old)

def class_body(s):
    return compact(s[s.index('public class '):])

rocket_old = source('handler/RocketStruct.java', True)
rocket = source('handler/RocketStruct.java')
normalized = class_body(rocket)
normalized = replace(normalized,
    block(rocket, 'public static void setupDataManager('),
    block(rocket_old, 'public static void setupDataWatcher('))
for new, old in [
    ('writeToDataManager(EntityDataManager manager, RocketMetadata keys)', 'writeToDataWatcher(DataWatcher watcher, int start)'),
    ('readFromDataManager(EntityDataManager manager, RocketMetadata keys)', 'readFromDataWatcher(DataWatcher watcher, int start)'),
    ('keys.setCapsule(manager, MissilePart.getId(capsule))', 'watcher.updateObject(start, MissilePart.getId(capsule))'),
    ('keys.setStageCount(manager, stages.size())', 'watcher.updateObject(start + 1, stages.size())'),
    ('keys.setFirst(manager, i, watchable.key)', 'watcher.updateObject(start + i * 2 + 2, watchable.key)'),
    ('keys.setSecond(manager, i, watchable.value)', 'watcher.updateObject(start + i * 2 + 3, watchable.value)'),
    ('keys.getCapsule(manager)', 'watcher.getWatchableObjectInt(start)'),
    ('keys.getStageCount(manager)', 'watcher.getWatchableObjectInt(start + 1)'),
    ('keys.getFirst(manager, i)', 'watcher.getWatchableObjectInt(start + i * 2 + 2)'),
    ('keys.getSecond(manager, i)', 'watcher.getWatchableObjectInt(start + i * 2 + 3)')]:
    normalized = replace(normalized, new, old)
assert normalized == class_body(rocket_old).replace('EnumChatFormatting', 'TextFormatting'), 'RocketStruct non-metadata behavior changed'
assert compact(block(rocket, 'public static void setupDataManager(')) == 'publicstaticvoidsetupDataManager(EntityDataManagermanager,RocketMetadatakeys){keys.register(manager);}'

entity_old = source('entity/missile/EntityRideableRocket.java', True)
entity = source('entity/missile/EntityRideableRocket.java')
key_start = entity.index('private static final DataParameter<Integer> STATE')
key_end = entity.index('private double rocketVelocity', key_start)
key_block = entity[key_start:key_end]
integer_key = 'EntityDataManager.createKey(EntityRideableRocket.class, DataSerializers.VARINT)'
stack_key = 'EntityDataManager.createKey(EntityRideableRocket.class, DataSerializers.ITEM_STACK)'
expected_keys = ('private static final DataParameter<Integer> STATE = ' + integer_key + ';'
    'private static final DataParameter<ItemStack> DRIVE = ' + stack_key + ';'
    'private static final DataParameter<Integer> TIMER = ' + integer_key + ';'
    'private static final RocketMetadata ROCKET = new RocketMetadata(' + integer_key + ',' + integer_key + ', Arrays.asList('
    + ','.join([integer_key] * 5) + '), Arrays.asList(' + ','.join([integer_key] * 5) + '));')
assert compact(key_block) == compact(expected_keys), 'Entity-owned key declarations/capacity/serializers changed'
old_key_block = entity_old[entity_old.index('private static final int WATCHABLE_STATE'):entity_old.index('private double rocketVelocity')]
normalized = replace(class_body(entity), key_block, old_key_block)
for new, old, count in [
    ('RocketStruct.readFromDataManager(dataManager, ROCKET)', 'RocketStruct.readFromDataWatcher(dataWatcher, WATCHABLE_ROCKET)', 1),
    ('rocket.writeToDataManager(dataManager, ROCKET)', 'rocket.writeToDataWatcher(dataWatcher, WATCHABLE_ROCKET)', 1),
    ('dataManager.get(STATE)', 'dataWatcher.getWatchableObjectInt(WATCHABLE_STATE)', 1),
    ('dataManager.set(STATE, state.ordinal())', 'dataWatcher.updateObject(WATCHABLE_STATE, state.ordinal())', 1),
    ('dataManager.set(TIMER, 0)', 'dataWatcher.updateObject(WATCHABLE_TIMER, 0)', 1),
    ('ItemStack drive = dataManager.get(DRIVE); if(drive.isEmpty()) drive = null;', 'ItemStack drive = dataWatcher.getWatchableObjectItemStack(WATCHABLE_DRIVE);', 2),
    ('dataManager.set(DRIVE, drive == null ? ItemStack.EMPTY : drive)', 'dataWatcher.updateObject(WATCHABLE_DRIVE, drive)', 1),
    ('dataManager.get(TIMER)', 'dataWatcher.getWatchableObjectInt(WATCHABLE_TIMER)', 1),
    ('dataManager.set(TIMER, timer)', 'dataWatcher.updateObject(WATCHABLE_TIMER, timer)', 1),
    ('dataManager.register(STATE, RocketState.AWAITING.ordinal())', 'dataWatcher.addObject(WATCHABLE_STATE, RocketState.AWAITING.ordinal())', 1),
    ('dataManager.register(DRIVE, ItemStack.EMPTY)', 'dataWatcher.addObjectByDataType(WATCHABLE_DRIVE, 5)', 1),
    ('dataManager.register(TIMER, 0)', 'dataWatcher.addObject(WATCHABLE_TIMER, 0)', 1),
    ('RocketStruct.setupDataManager(dataManager, ROCKET)', 'RocketStruct.setupDataWatcher(dataWatcher, WATCHABLE_ROCKET)', 1),
    ('private static final DataParameter<Integer> PARENT_ID = EntityDataManager.createKey(EntityRideableRocketDummy.class, DataSerializers.VARINT);', 'private static final int WATCHABLE_PARENT_ID = 3;', 1),
    ('dataManager.set(PARENT_ID, parent.getEntityId())', 'dataWatcher.updateObject(WATCHABLE_PARENT_ID, parent.getEntityId())', 1),
    ('dataManager.register(PARENT_ID, 0)', 'dataWatcher.addObject(WATCHABLE_PARENT_ID, 0)', 1),
    ('dataManager.get(PARENT_ID)', 'dataWatcher.getWatchableObjectInt(WATCHABLE_PARENT_ID)', 1)]:
    normalized = replace(normalized, new, old, count)
expected = class_body(entity_old)
for old, new in [('EnumChatFormatting', 'TextFormatting'), ('MovingObjectPosition', 'RayTraceResult'),
                 ('Vec3', 'Vec3d'), ('motion.xCoord', 'motion.x'), ('motion.yCoord', 'motion.y'), ('motion.zCoord', 'motion.z'),
                 ('target.xCoord', 'target.x'), ('target.yCoord', 'target.y'), ('target.zCoord', 'target.z')]:
    expected = expected.replace(old, new)
assert normalized == expected, 'Non-metadata rocket/dummy behavior changed (including NBT, timers, navigation and update order)'
assert 'dataWatcher' not in entity and 'DataWatcher' not in rocket
print('PASS: complete caller/struct body source parity against pristine + enumerated historical type renames; native key allocation, null boundary, packing/defaults/order and existing NBT anomalies unchanged.', flush=True)

assert 'public static final int MAX_STAGES = 5;' in rocket
assert 'public static final int STAGE_CAPACITY = 5;' in source('handler/RocketMetadata.java')
if a.source_only:
    raise SystemExit(0)
if not a.java_home or not a.classpath:
    p.error('--java-home and --classpath are required unless --source-only')

# The following is explicitly an extracted fixture, NOT the complete rocket entity.
# The only initialization adaptation replaces the source-audited one-line RocketStruct
# setup delegate with the actual complete production RocketMetadata.register call.
imports = '''import java.util.Arrays;
import com.hbm.handler.RocketMetadata;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.world.World;
'''
fixture = imports + '''class NativeMetadataParent extends Entity {
    static final DataParameter<Byte> EXTRA = EntityDataManager.createKey(NativeMetadataParent.class, DataSerializers.BYTE);
    NativeMetadataParent(World world) { super(world); }
    protected void entityInit() { dataManager.register(EXTRA, (byte) 73); }
    protected void readEntityFromNBT(NBTTagCompound tag) { }
    protected void writeEntityToNBT(NBTTagCompound tag) { }
}
class ExtractedRocketEntity extends NativeMetadataParent {
    ExtractedRocketEntity(World world) { super(world); }
    int stateTimer;
'''
fixture += key_block.replace('EntityRideableRocket.class', 'ExtractedRocketEntity.class').replace('private static final', 'static final')
fixture += block(entity, 'public enum RocketState') + '\n'
for signature in ['public RocketState getState(', 'public void setState(', 'public void setDrive(',
                  'public int getStateTimer(', 'public void setStateTimer(', 'protected void entityInit(']:
    fixture += block(entity, signature).replace('RocketStruct.setupDataManager(dataManager, ROCKET)', 'ROCKET.register(dataManager)') + '\n'
fixture += '}\n'
dummy = entity[entity.index('public static class EntityRideableRocketDummy'):]
parent_key = re.search(r'private static final DataParameter<Integer> PARENT_ID[^;]+;', dummy).group()
fixture += 'class ExtractedDummyEntity extends Entity {\n'
fixture += parent_key.replace('private static final', 'static final').replace('EntityRideableRocketDummy.class', 'ExtractedDummyEntity.class') + '\n'
fixture += '''ExtractedDummyEntity(World world) { super(world); }
    protected void readEntityFromNBT(NBTTagCompound tag) { }
    protected void writeEntityToNBT(NBTTagCompound tag) { }
'''
fixture += block(dummy, 'protected void entityInit(') + '\n}\n'

# Extract only arithmetic expressions; integer inputs replace domain lookups. This
# does NOT test MissilePart ID lookup, fallback capsule, NBT or complete RocketStage.
pack = block(rocket, 'public Pair<Integer, Integer> zipWatchable(')
first = re.search(r'int first = (.*);', pack).group(1)
second = re.search(r'int second = (.*);', pack).group(1)
for part in ['fuselage', 'fins', 'thruster']:
    first = first.replace('MissilePart.getId(' + part + ')', part)
    second = second.replace('MissilePart.getId(' + part + ')', part)
unpack = block(rocket, 'public static RocketStage unzipWatchable(')
expressions = []
for field in ['fuselage', 'fins', 'thruster', 'fuselageCount', 'thrusterCount']:
    expression = re.search(r'stage\.' + field + r' = (.*);', unpack).group(1)
    if expression.startswith('MissilePart.getPart('):
        expression = expression[len('MissilePart.getPart('):-1]
    expressions.append(expression.replace('pair.key', 'first').replace('pair.value', 'second'))
fixture += '''class ExtractedStageExpressions {
    static int[] pack(int fuselage, int fins, int thruster, int fuselageCount, int thrusterCount) {
        return new int[]{''' + first + ',' + second + '''};
    }
    static int[] unpack(int first, int second) { return new int[]{''' + ','.join(expressions) + '''}; }
}
'''
with tempfile.TemporaryDirectory(prefix='rocket-native-metadata-') as tmp:
    generated = Path(tmp) / 'ExtractedRocketEntity.java'
    generated.write_text(fixture, encoding='utf-8')
    exe = '.exe' if os.name == 'nt' else ''
    subprocess.run([str(a.java_home / ('bin/javac' + exe)), '-source', '8', '-target', '8', '-encoding', 'UTF-8',
                    '-cp', a.classpath, '-d', tmp,
                    str(root / src / 'handler/RocketMetadata.java'), str(generated),
                    str(root / 'porting/tests/RocketMetadataMinecraftTest.java')], check=True)
    subprocess.run([str(a.java_home / ('bin/java' + exe)), '-cp', tmp + os.pathsep + a.classpath,
                    'RocketMetadataMinecraftTest'], cwd=tmp, check=True)
