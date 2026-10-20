#!/usr/bin/env python3
"""Bounded Tom HMF migration: actual native classes/bytes plus exact source-delta audit.

The complete TomPronter/ResourceManager/ClientProxy classes are NOT compiled here.
Their remaining dependencies still belong to the failed whole-mod compile.
No test or production replacement for ResourceManager, Minecraft or Forge is made.
"""
import argparse
import hashlib
import importlib.util
import json
import os
from pathlib import Path
import re
import subprocess
import tempfile

p = argparse.ArgumentParser()
p.add_argument('--classpath')
p.add_argument('--java-home', type=Path)
p.add_argument('--audit-only', action='store_true')
a = p.parse_args()
root = Path(__file__).resolve().parents[2]
src = root / 'src/main/java/com/hbm'

def require(value, label):
    if not value:
        raise AssertionError(label)

def replace_once(text, old, new):
    require(text.count(old) == 1, 'Expected one exact source boundary: ' + old)
    return text.replace(old, new, 1)

def digest(text):
    return hashlib.sha256(text.encode('utf-8')).hexdigest()

tom = (src / 'render/util/TomPronter.java').read_text()
old_tom = replace_once(tom,
    'import com.hbm.render.mesh.ObjMeshRenderer;\n'
    'import com.hbm.render.mesh.ObjMeshRenderer.Shading;\n'
    'import com.hbm.util.Clock;\n', '')
old_tom = replace_once(old_tom,
    'ObjMeshRenderer.drawParts(ResourceManager.tom_main.getMesh().parts, Shading.LEGACY_FLAT, 0);',
    'ResourceManager.tom_main.renderAll();')
old_tom = replace_once(old_tom,
    'float currentTime = Clock.get_ms();\n'
    '\t\t\tObjMeshRenderer.drawParts(ResourceManager.tom_flame.getMesh().parts, Shading.HMF,\n'
    '\t\t\t\t\tObjMeshRenderer.hmfOffset(currentTime, HmfController.modoloMod, HmfController.quotientMod));',
    'ResourceManager.tom_flame.renderAll();')
require(digest(old_tom) == '6f8be8e6d7b6ace057ea37193cd5e2bbcbf38adbe8ccfa123d2f45550beb225e',
        'TomPronter changed outside its three imports and two drawing call sites')

resources = (src / 'main/ResourceManager.java').read_text()
composed_consumer_boundary = 'import com.hbm.render.mesh.MeshModel;' in resources
if composed_consumer_boundary:
    # Validate the current complete consumer sources independently before using
    # their exact reverse delta. Never relax the historical HMF whole-file hash.
    consumer_script = root / 'porting/tests/check-mesh-model-consumers.py'
    spec = importlib.util.spec_from_file_location('mesh_model_consumer_audit', consumer_script)
    require(spec is not None and spec.loader is not None, 'Native consumer source audit is unavailable')
    consumer_audit = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(consumer_audit)
    consumer_audit.main([])
    consumer_manifest = json.loads((root / 'porting/tests/mesh-model-consumer-manifest.json').read_text())
    resource_entries = [entry for entry in consumer_manifest['files']
                        if entry['path'] == 'src/main/java/com/hbm/main/ResourceManager.java']
    require(len(resource_entries) == 1, 'Expected one exact native-consumer ResourceManager boundary')
    resources = consumer_audit.reverse_consumer(resources, resource_entries[0])
old_resources = replace_once(resources,
    'import com.hbm.render.mesh.ReloadableObjMesh;\nimport net.minecraft.client.Minecraft;\n', '')
for field, extension in [('tom_main', 'obj'), ('tom_flame', 'hmf')]:
    old_resources = replace_once(old_resources,
        'public static final ReloadableObjMesh ' + field + ' = new ReloadableObjMesh(Minecraft.getMinecraft().getResourceManager(), new ResourceLocation(RefStrings.MODID, "models/weapons/' + field + '.' + extension + '"), false);',
        'public static final IModelCustom ' + field + ' = AdvancedModelLoader.loadModel(new ResourceLocation(RefStrings.MODID, "models/weapons/' + field + '.' + extension + '"));')
require(digest(old_resources) == 'b92105e2fa23ea4a6226d705fa6a905e4e242fdb3d968512b80b81d02c966d40',
        'ResourceManager changed outside the two imports/two eager field declarations')
if composed_consumer_boundary:
    print('PASS composed ResourceManager source audit: verified current MeshModel consumer delta -> exact checkpoint 7 -> historical HMF delta -> exact checkpoint 6. This does not compile the complete consumers.', flush=True)

for relative, expected in [
        ('render/loader/HmfController.java', 'b89da7f5c672c4e6d5839989fa56395caf965322d64ca695b314216234eb018e'),
        ('util/Clock.java', '0480a35b0591b1bf65812fb86e3b2be8449498f819f8bb0ac348e8c70bbf4d47')]:
    require(hashlib.sha256((src / relative).read_bytes()).hexdigest() == expected, relative + ' changed')

retired = ['HmfModelLoader', 'HbmModelObject', 'HbmFace', 'HbmGroupObject']
for name in retired:
    require(not (src / 'render/loader' / (name + '.java')).exists(), name + ' was not retired')
tokens = re.compile(r'\b(?:' + '|'.join(retired) + r')\b')
for file in (root / 'src/main/java').rglob('*.java'):
    require(not tokens.search(file.read_text()), 'Retired HMF reference remains: ' + str(file))

for asset, expected in [
        ('tom_main.obj', '13f91982a16bbabd39fa94c8f9da7578c9b39b805d351c5be3f1af2aa3b9775f'),
        ('tom_flame.hmf', 'ac7ce0b81645307777c4ede34bb72c8dc33535235b89e6a2b8940c2f6fbbffdd')]:
    require(hashlib.sha256((root / 'src/main/resources/assets/hbm/models/weapons' / asset).read_bytes()).hexdigest() == expected,
            asset + ' geometry changed')
print('PASS historical HMF source audit: after any separately validated native-consumer reversal, only two Tom draws and two eager resource fields changed; cached clock/controller, GL/matrix/RNG order and assets retained; four obsolete HMF classes have no remaining production references.', flush=True)
if a.audit_only:
    raise SystemExit(0)
require(a.classpath and a.java_home, '--classpath and --java-home are required for native tests')

files = sorted((src / 'render/mesh').glob('*.java')) + [
    src / 'render/loader/HmfController.java', src / 'util/Clock.java',
    root / 'porting/tests/ObjMeshMinecraftTest.java', root / 'porting/tests/HmfMeshMinecraftTest.java']
with tempfile.TemporaryDirectory(prefix='hmf-native-probe-') as tmp:
    subprocess.run([str(a.java_home / 'bin/javac'), '-source', '8', '-target', '8', '-encoding', 'UTF-8',
                    '-cp', a.classpath, '-d', tmp] + list(map(str, files)), check=True)
    subprocess.run([str(a.java_home / 'bin/java'), '-cp', tmp + os.pathsep + a.classpath,
                    'HmfMeshMinecraftTest', str(root / 'src/main/resources/assets/hbm/models')], cwd=tmp, check=True)
