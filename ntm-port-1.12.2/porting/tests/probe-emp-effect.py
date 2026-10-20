#!/usr/bin/env python3
"""Compile entire production EMP entity, EMP/plastic bag renderers, and mesh core against the actual pinned target.
Source parity audit is against the supplied pristine original, never an invented API shim.
The extracted ClientProxy registration is compiled only, not a claim of client startup.
"""
import argparse
import os
from pathlib import Path
import re
import subprocess
import tempfile

p = argparse.ArgumentParser()
p.add_argument('--classpath', required=True)
p.add_argument('--java-home', type=Path, required=True)
a = p.parse_args()
root = Path(__file__).resolve().parents[2]
pristine = root.parent / 'ntm-original-pristine'
src = Path('src/main/java/com/hbm')

def compact(s):
    s = re.sub(r'/\*.*?\*/', '', s, flags=re.S)
    s = re.sub(r'//[^\n]*', '', s)
    return re.sub(r'\s+', '', s)

def body(s, signature):
    start = s.index('{', s.index(signature)) + 1
    i, depth = start, 1
    while depth:
        depth += (s[i] == '{') - (s[i] == '}')
        i += 1
    return compact(s[start:i - 1])

def source(relative, old=False):
    return ((pristine if old else root) / src / relative).read_text()

old = source('entity/effect/EntityEMPBlast.java', True)
new = source('entity/effect/EntityEMPBlast.java')
for signature in ['EntityEMPBlast(World p_i1582_1_)', 'EntityEMPBlast(World p_i1582_1_, int maxAge)',
                  'void onUpdate()', 'void readEntityFromNBT(', 'void writeEntityToNBT(']:
    assert body(new, signature) == body(old, signature), 'Original body changed: ' + signature
for field in ['public int maxAge = 100;', 'public int age;', 'public float scale = 0;']:
    assert compact(field) in compact(new), 'Original field changed: ' + field
assert body(new, 'void entityInit()') == 'this.dataManager.register(MAX_AGE,Integer.valueOf(0));'
assert body(new, 'void setMaxAge(') == 'this.dataManager.set(MAX_AGE,Integer.valueOf(i));'
assert body(new, 'int getMaxAge(') == 'returnthis.dataManager.get(MAX_AGE);'
assert 'EntityDataManager.createKey(EntityEMPBlast.class,DataSerializers.VARINT)' in compact(new)
for method in ['getBrightnessForRender', 'getBrightness']:
    assert body(new, method + '()') == body(old, method + '(float '), method
assert 'dataWatcher' not in new
old = source('render/entity/effect/RenderEMPBlast.java', True)
new = source('render/entity/effect/RenderEMPBlast.java')
render_body = body(new, 'void renderRing(').replace(
    'ObjMeshRenderer.drawParts(ringModel.getMesh().parts,ObjMeshRenderer.Shading.LEGACY_FLAT,0);',
    'ringModel.renderAll();')
assert render_body == body(old, 'void renderRing('), 'Original transform, texture, GL state, or draw order changed'
assert body(new, 'void doRender(') == body(old, 'void doRender(').replace('(EntityEMPBlast)', '')
assert body(new, 'ResourceLocation getEntityTexture(') == body(old, 'ResourceLocation getEntityTexture(')
assert body(new, 'RenderEMPBlast(RenderManager renderManager)') == 'this(renderManager,loadRingModel());'
assert body(new, 'static ReloadableObjMesh loadRingModel()') == (
    'returnnewReloadableObjMesh(Minecraft.getMinecraft().getResourceManager(),ringModelRL,false);')
assert body(new, 'RenderEMPBlast(RenderManager renderManager, ReloadableObjMesh ringModel)') == (
    'super(renderManager);this.ringModel=ringModel;ringTexture=newResourceLocation(RefStrings.MODID,"textures/models/EMPBlast.png");')
assert '"models/Ring.obj"' in new and 'Render<EntityEMPBlast>' in new
assert 'AdvancedModelLoader' not in new and 'IModelCustom' not in new
old = source('render/entity/mob/RenderPlasticBag.java', True)
new = source('render/entity/mob/RenderPlasticBag.java')
assert body(new, 'void doRender(').replace(
    'ObjMeshRenderer.drawParts(model.getMesh().parts,ObjMeshRenderer.Shading.LEGACY_FLAT,0);',
    'model.renderAll();') == body(old, 'void doRender('), 'Original plastic bag transform/texture/GL order changed'
assert body(new, 'ResourceLocation getEntityTexture(') == body(old, 'ResourceLocation getEntityTexture(')
assert body(new, 'RenderPlasticBag(RenderManager renderManager)').replace('super(renderManager);', '') == body(old, 'RenderPlasticBag()')
assert body(new, 'static ReloadableObjMesh getModel()') == 'returnmodel;'
assert ('privatestaticfinalReloadableObjMeshmodel=newReloadableObjMesh(Minecraft.getMinecraft().getResourceManager(),'
        'newResourceLocation(RefStrings.MODID,"models/mobs/plasticbag.obj"),false);') in compact(new)
assert 'Render<Entity>' in new and '"textures/entity/plasticbag.png"' in new
proxy = source('main/ClientProxy.java')
registration = re.search(r'final com\.hbm\.render\.mesh\.ReloadableObjMesh empRing\s*=\s*RenderEMPBlast\.loadRingModel\(\);\s*'
                         r'RenderingRegistry\.registerEntityRenderingHandler\(EntityEMPBlast\.class,\s*manager\s*->\s*new RenderEMPBlast\(manager,\s*empRing\)\);', proxy)
assert registration, 'Expected eager resource sampling + native typed factory registration missing'
assert proxy.count('RenderingRegistry.registerEntityRenderingHandler(EntityEMPBlast.class,') == 1
print('PASS: pristine EMP constructors/update/NBT, EMP/plastic bag transform/GL/texture order; native VARINT, eager per-instance/static mesh lifetime, and factory source audits.', flush=True)

files = sorted((root / src / 'render/mesh').glob('*.java')) + [
    root / src / 'entity/effect/EntityEMPBlast.java',
    root / src / 'render/entity/effect/RenderEMPBlast.java',
    root / src / 'render/entity/mob/RenderPlasticBag.java',
    root / src / 'lib/RefStrings.java',
    root / 'porting/tests/EmpEffectMinecraftTest.java',
    root / 'porting/tests/EmpEffectMeshMinecraftTest.java']
with tempfile.TemporaryDirectory(prefix='emp-effect-native-probe-') as tmp:
    fixture = Path(tmp) / 'EmpRegistrationSignature.java'
    fixture.write_text('import com.hbm.entity.effect.EntityEMPBlast;\n'
        'import com.hbm.render.entity.effect.RenderEMPBlast;\n'
        'import net.minecraftforge.fml.client.registry.RenderingRegistry;\n'
        'import net.minecraftforge.fml.client.registry.IRenderFactory;\n'
        'import net.minecraft.entity.Entity;\n'
        'import com.hbm.render.entity.mob.RenderPlasticBag;\n'
        'final class EmpRegistrationSignature {\n'
        '  static IRenderFactory<Entity> compileOnlyPlasticFactory() { return RenderPlasticBag::new; }\n'
        '  static void compileOnlyPlasticRegistration(Class<? extends Entity> type) { RenderingRegistry.registerEntityRenderingHandler(type, RenderPlasticBag::new); }\n'
        '  static void compileOnlyRegistration() {\n' + registration.group() + '\n  }\n}\n')
    subprocess.run([str(a.java_home / 'bin/javac'), '-source', '8', '-target', '8', '-encoding', 'UTF-8',
                    '-cp', a.classpath, '-d', tmp] + list(map(str, files)) + [str(fixture)], check=True)
    subprocess.run([str(a.java_home / 'bin/java'), '-cp', tmp + os.pathsep + a.classpath,
                    'EmpEffectMinecraftTest'], cwd=tmp, check=True)
    subprocess.run([str(a.java_home / 'bin/java'), '-cp', tmp + os.pathsep + a.classpath,
                    'EmpEffectMeshMinecraftTest', str(root / 'src/main/resources/assets')], cwd=tmp, check=True)
