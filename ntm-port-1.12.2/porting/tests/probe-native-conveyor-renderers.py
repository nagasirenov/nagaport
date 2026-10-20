#!/usr/bin/env python3
"""Compile whole conveyor consumers and unchanged native frame pipeline; no game mocks."""
import argparse, hashlib, importlib.util, json, os, pathlib, subprocess, tempfile, zipfile
p = argparse.ArgumentParser()
p.add_argument('--classpath', required=True)
p.add_argument('--java-home', type=pathlib.Path, required=True)
a = p.parse_args()
root = pathlib.Path(__file__).resolve().parents[2]
conveyors = json.loads((root/'porting/tests/native-conveyor-renderer-scope.json').read_text())
for group in ['production', 'unchanged_collaborators']:
    for path, digest in conveyors[group].items():
        assert hashlib.sha256((root/path).read_bytes()).hexdigest() == digest, (group, path)
for path, reference in conveyors['original_sources'].items():
    assert hashlib.sha256(reference['source'].encode()).hexdigest() == reference['sha256'], ('pristine reference changed', path)
# Read genuine 1.12 assets, not fixture declarations, to audit old-flat versus
# native block/builtin inventory categories independently of wrapper gui3d flags.
remaining = dict(conveyors['native_item_parents'])
for jar in a.classpath.split(os.pathsep):
    if not pathlib.Path(jar).is_file() or not zipfile.is_zipfile(jar):
        continue
    with zipfile.ZipFile(jar) as archive:
        for name, parent in list(remaining.items()):
            path = 'assets/minecraft/models/item/'+name+'.json'
            if path in archive.namelist():
                assert json.loads(archive.read(path))['parent'] == parent, ('native item category changed', path)
                del remaining[name]
assert not remaining, ('native item assets unavailable', list(remaining))
print('PASS frozen whole conveyor sources, unchanged entity/helper collaborators, pristine references and '+str(len(conveyors['native_item_parents']))+' genuine native inventory model categories.', flush=True)
scope = json.loads((root/'porting/tests/native-machine-item-scope.json').read_text())
for path, digest in scope['production'].items():
    assert hashlib.sha256((root/path).read_bytes()).hexdigest() == digest, ('unchanged native machine slice', path)
files = [root/p for p in [
    'src/main/java/com/hbm/render/util/NativeItemBaseModel.java',
    'src/main/java/com/hbm/render/util/RenderDecoItem.java',
    'src/main/java/com/hbm/render/util/RenderItemStack.java',
    'src/main/java/com/hbm/render/util/NativeMachineItemRenderer.java',
    'src/main/java/com/hbm/render/util/NativeIconExtrusion.java',
    'src/main/java/com/hbm/render/util/NativeItemQuadRenderer.java',
    'src/main/java/com/hbm/render/util/NativeGuiItemDraw.java',
    'src/main/java/com/hbm/render/item/nativeitem/MaterialTextureAtlasSprite.java',
    'src/main/java/com/hbm/render/entity/item/RenderMovingItem.java',
    'src/main/java/com/hbm/render/entity/item/RenderMovingPackage.java',
    'porting/tests/NativeMachineItemTest.java',
    'porting/tests/NativeMachineDrawTest.java',
    'porting/tests/NativeMachineConsumerTest.java',
    'porting/tests/NativeBaseModelOwnershipTest.java',
    'porting/tests/MaterialTextureAtlasSpriteNativeTest.java',
    'porting/tests/NativeConveyorRendererTest.java']]
with tempfile.TemporaryDirectory(prefix='native-conveyor-renderers-') as temp:
    tmp = pathlib.Path(temp)
    spec = importlib.util.spec_from_file_location('fixtures', root/'porting/tests/machine-renderer-fixtures.py')
    fixtures = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(fixtures)
    consumers = fixtures.add(root, tmp, files)
    blocks = tmp/'fixtures/com/hbm/blocks/ModBlocks.java'
    blocks.write_text(blocks.read_text().replace('public class ModBlocks {', 'public class ModBlocks { public static net.minecraft.block.Block crate=net.minecraft.init.Blocks.STONE;'))
    entity = tmp/'fixtures/com/hbm/entity/item/EntityMovingItem.java'
    entity.parent.mkdir(parents=True, exist_ok=True)
    entity.write_text('''package com.hbm.entity.item;
/** Controlled NTM storage fixture; native Entity, EntityItem and ItemStack are real.
 * Conveyor movement/network/transfer mechanics are explicitly not tested here. */
public class EntityMovingItem extends net.minecraft.entity.Entity {
 public net.minecraft.item.ItemStack stack;
 public net.minecraft.entity.item.EntityItem cacheForRender;
 public EntityMovingItem(net.minecraft.item.ItemStack stack) { super(null); this.stack=stack; cacheForRender=new net.minecraft.entity.item.EntityItem(null,0,0,0,stack); }
 public net.minecraft.item.ItemStack getItemStack() { return stack; }
 protected void entityInit() { }
 protected void readEntityFromNBT(net.minecraft.nbt.NBTTagCompound nbt) { }
 protected void writeEntityToNBT(net.minecraft.nbt.NBTTagCompound nbt) { }
}
''')
    files.append(entity)
    for jar in a.classpath.split(os.pathsep):
        if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
            with zipfile.ZipFile(jar) as archive:
                for name in archive.namelist():
                    if name.endswith('.so') and '/' not in name:
                        (tmp/name).write_bytes(archive.read(name))
    assert (tmp/'liblwjgl64.so').exists(), 'Existing native Linux LWJGL JAR required; no downloads or installs.'
    subprocess.run([str(a.java_home/'bin/javac'), '-encoding', 'UTF-8', '-source', '8', '-target', '8', '-cp', a.classpath, '-d', temp] + list(map(str, files)), check=True)
    print('PASS both complete conveyor renderers, real native helper/model pipeline, and all '+str(len(consumers))+' existing machine consumers compile against genuine native APIs.', flush=True)
    for test in ['NativeMachineItemTest', 'NativeConveyorRendererTest']:
        subprocess.run([str(a.java_home/'bin/java'), '-Djava.awt.headless=true', '-Djava.library.path='+temp, '-cp', temp+os.pathsep+a.classpath, test], cwd=temp, env=dict(os.environ, MESA_SHADER_CACHE_DISABLE='true'), check=True)
