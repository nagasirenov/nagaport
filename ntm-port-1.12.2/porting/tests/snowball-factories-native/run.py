#!/usr/bin/env python3
"""Actual Forge factories + native textured snowballs. No downloads or full mod build.
The extracted production registrations are unchanged. Application entity class tokens
and item slots are fixtures: no native API is replaced and no projectile behavior is claimed.
"""
import argparse, hashlib, json, os, pathlib, re, subprocess, tempfile, zipfile
p=argparse.ArgumentParser()
p.add_argument('--classpath',required=True)
p.add_argument('--java-home',type=pathlib.Path,required=True)
p.add_argument('--vanilla',type=pathlib.Path,required=True)
p.add_argument('--legacy-vanilla',type=pathlib.Path,required=True)
p.add_argument('--legacy-mappings',type=pathlib.Path,required=True)
p.add_argument('--diagnostics',type=pathlib.Path)
p.add_argument('--manifest',type=pathlib.Path)
a=p.parse_args()
root=pathlib.Path(__file__).resolve().parents[3]
original=root.parent/'ntm-original-pristine'
proxy_path=pathlib.Path('src/main/java/com/hbm/main/ClientProxy.java')
old=(original/proxy_path).read_text(); new=(root/proxy_path).read_text()
legacy=re.compile(r'RenderingRegistry\.registerEntityRenderingHandler\((\w+)\.class, new RenderSnowball\(([^)]+)\)\);')
pairs=legacy.findall(old)
assert len(pairs)==42 and len(set(pairs))==42
lines=[]; manifest=[]
items=(root/'src/main/java/com/hbm/items/ModItems.java').read_text()
old_items=(original/'src/main/java/com/hbm/items/ModItems.java').read_text()
for entity,item in pairs:
 native_item=item.replace('Items.slime_ball','Items.SLIME_BALL')
 line='RenderingRegistry.registerEntityRenderingHandler(%s.class, renderManager -> new RenderSnowball<>(renderManager, %s, Minecraft.getMinecraft().getRenderItem()));'%(entity,native_item)
 assert new.count(line)==1,(entity,'missing or duplicate exact lazy native factory')
 assert len(re.findall(r'RenderingRegistry\.registerEntityRenderingHandler\('+entity+r'\.class,',new))==1
 lines.append(line)
 row={'entity':entity,'original_item':item,'native_item':native_item,'metadata':0,'count':1}
 if item.startswith('ModItems.'):
  name=item.split('.')[1]
  decl=re.search(r'^\s*'+name+r' = (.*);',items,re.M).group(1)
  old_decl=re.search(r'^\s*'+name+r' = (.*);',old_items,re.M).group(1)
  texture=re.search(r'RefStrings.MODID \+ ":([^"\n]+)"',decl).group(1)
  assert '.setTextureName(RefStrings.MODID + ":'+texture+'")' in old_decl
  assert decl.startswith('NativeItemTextureRegistry.texture(')
  row.update(texture='hbm:items/'+texture,item_class=re.search(r'new (\w+)\(',decl).group(1))
  path=pathlib.Path('src/main/resources/assets/hbm/textures/items')/(texture+'.png')
  actual=(root/path).read_bytes();before=(original/path).read_bytes()
  assert actual==before,(name,'original texture changed')
  row.update(asset=str(path),sha256=hashlib.sha256(actual).hexdigest())
 else: row.update(texture='minecraft:items/slimeball',item_class='Item')
 manifest.append(row)
assert new.count('new RenderSnowball')==42 and not legacy.search(new)
assert [new.index(line) for line in lines] == sorted(new.index(line) for line in lines), 'Original registration order changed'
main=(root/'src/main/java/com/hbm/main/MainRegistry.java').read_text()
preload=main[main.index('void PreLoad(FMLPreInitializationEvent'):main.index('proxy.registerRenderInfo();')+len('proxy.registerRenderInfo();')]
assert preload.index('ModItems.mainRegistry();') < preload.index('proxy.registerRenderInfo();')
assert new.index('registerItemRenderer();') < new.index('registerEntityRenderer();')
assert set(row['item_class'] for row in manifest)=={'Item','ItemCustomLore','ItemGrenade'}
# These original item classes have no icon/model override; their native generated
# foundation owns the original texture. Specialized grenade renderers are not replaced.
for name in ['weapon/ItemGrenade','ItemCustomLore']:
 text=(root/'src/main/java/com/hbm/items'/(name+'.java')).read_text()
 assert not re.search(r'\b(registerIcons|getIcon|getIconFromDamage|getIconFromDamageForRenderPass|requiresMultipleRenderPasses|getRenderPasses|getColorFromItemStack|getRenderer|getItemsForRenderer)\s*\(',text),name
for entity,renderer in [('EntityGrenadeFlare','RenderFlare'),('EntityGrenadeMk2','RenderGrenade'),('EntityGrenadeASchrab','RenderGrenade'),('EntityGrenadeBouncyGeneric','RenderGenericGrenade'),('EntityGrenadeImpactGeneric','RenderGenericGrenade'),('EntityDisperserCanister','RenderGenericGrenade')]:
 assert re.search(r'registerEntityRenderingHandler\('+entity+r'\.class,.*new '+renderer+r'\(',new)
for path in (root/'src/main/java/com/hbm/render/item').rglob('*.java'):
 text=path.read_text()
 for row in manifest:
  if row['native_item'].startswith('ModItems.'):
   assert not re.search(r'\b(?:put|registerItemRenderer)\('+re.escape(row['native_item'])+r'\s*,',text),(path,row['native_item'],'unexpected specialized owner')
ownership=(root/'src/main/java/com/hbm/render/item/nativeitem/context/NativeItemRendererOwnership.java').read_text()
assert 'kind == RenderSnowball.class || kind == RenderPotion.class' in ownership
# Derive 1.7 class name from actual mappings. Assert actual original zero metadata,
# 0.5 billboard scale, and 0.25 local baseline, not an invented oracle.
with zipfile.ZipFile(a.legacy_mappings) as z:
 mapping=z.read('joined.srg').decode()
legacy_class=re.search(r'^CL: (\S+) net/minecraft/client/renderer/entity/RenderSnowball$',mapping,re.M).group(1)
dump=subprocess.check_output([str(a.java_home/'bin/javap'),'-c','-p','-cp',str(a.legacy_vanilla),legacy_class],text=True)
assert 'iconst_0' in dump.split('public '+legacy_class+'(')[2].split('return')[0]
assert 'float 0.5f' in dump and 'float 0.25f' in dump and 'GL11.glScalef:(FFF)V' in dump
with zipfile.ZipFile(a.vanilla) as z:
 ground=json.loads(z.read('assets/minecraft/models/item/generated.json'))['display']['ground']
 assert ground=={'rotation':[0,0,0],'translation':[0,2,0],'scale':[.5,.5,.5]}
 assert json.loads(z.read('assets/minecraft/models/item/slime_ball.json'))['textures']['layer0']=='items/slimeball'
print('PASS original-source audit: 42 entity/item mappings, metadata 0, 41 exact original PNGs, specialized grenade ownership preserved; native generated GROUND retains 0.5 size and original vertical extent.',flush=True)
if a.diagnostics:
 blocks=re.split(r'(?=^/[^\n]+:\d+: error: )',a.diagnostics.read_text(),flags=re.M)
 selected=[]
 for b in blocks:
  if re.match(r'^/[^\n]+ClientProxy.java:\d+: error:',b) and 'new RenderSnowball(' in '\n'.join(b.splitlines()[:3]):
   entity=re.search(r'registerEntityRenderingHandler\((\w+)\.class',b).group(1)
   assert entity in dict(pairs)
   kind='constructor-arity' if 'constructor RenderSnowball' in b.splitlines()[0] else 'vanilla-item-symbol'
   selected.append({'entity':entity,'kind':kind,'header':b.splitlines()[0]})
 assert len(selected)==42 and sum(r['kind']=='constructor-arity' for r in selected)==41
 print('PASS CP83 typed attribution: 41 RenderSnowball constructor-arity diagnostics + 1 Items.slime_ball symbol diagnostic; no measured full-build delta claimed.',flush=True)
else:selected=[]
if a.manifest:
 a.manifest.write_text(json.dumps({'base_checkpoint':'bdb4c897','production_source':str(proxy_path),'production_sha256':hashlib.sha256((root/proxy_path).read_bytes()).hexdigest(),'original_source_sha256':hashlib.sha256((original/proxy_path).read_bytes()).hexdigest(),'registrations':manifest,'cp83_diagnostics':selected,'legacy_class':legacy_class,'legacy_jar_sha256':hashlib.sha256(a.legacy_vanilla.read_bytes()).hexdigest(),'native_jar_sha256':hashlib.sha256(a.vanilla.read_bytes()).hexdigest(),'limits':'Application tokens and item slots are controlled fixture inputs. Actual Forge/Minecraft classes, production registration statements, texture/model provider and GPU draw path are used. Not a full ClientProxy compile, full mod initialization or in-game acceptance.'},indent=2)+'\n')
with tempfile.TemporaryDirectory(prefix='snowball-native-') as tmp:
 tmp=pathlib.Path(tmp)
 entities='\n'.join('static class %s extends Entity { %s(){super(null);} protected void entityInit(){} protected void readEntityFromNBT(NBTTagCompound n){} protected void writeEntityToNBT(NBTTagCompound n){} }'%(e,e) for e,i in pairs)
 fields=''.join('static Item %s;\n'%r['native_item'].split('.')[1] for r in manifest if r['native_item'].startswith('ModItems.'))
 init='\n'.join('%s=SnowballFactoriesNativeTest.registerItem("%s", "%s");'%(r['native_item'],r['native_item'].split('.')[1],r['texture']) for r in manifest if r['native_item'].startswith('ModItems.'))
 fixture=tmp/'SnowballRegistrationFixture.java'
 fixture.write_text('''import net.minecraft.client.Minecraft; import net.minecraft.client.renderer.entity.RenderSnowball; import net.minecraft.entity.Entity; import net.minecraft.item.Item; import net.minecraft.init.Items; import net.minecraft.nbt.NBTTagCompound; import net.minecraftforge.fml.client.registry.RenderingRegistry;
final class SnowballRegistrationFixture {
'''+entities+'\nstatic class ModItems {\n'+fields+'}\nstatic void register(){\n'+'\n'.join(lines)+'\n}\nstatic void initializeItems(){\n'+init+'\n}\nstatic Class<?>[] entities(){return new Class<?>[]{'+','.join(e+'.class' for e,i in pairs)+'};}\nstatic Item[] items(){return new Item[]{'+','.join(r['native_item'] for r in manifest)+'};}\n}\n')
 for jar in a.classpath.split(os.pathsep):
  if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
   with zipfile.ZipFile(jar) as z:
    for name in z.namelist():
     if name.endswith('.so') and '/' not in name:(tmp/name).write_bytes(z.read(name))
 files=[fixture,root/'porting/tests/snowball-factories-native/SnowballFactoriesNativeTest.java',root/'porting/tests/MaterialTextureAtlasSpriteNativeTest.java']+[root/'src/main/java/com/hbm/render/item'/n for n in ['NativeItemTextureRegistry.java','NativeItemTextureModelHandler.java','nativeitem/MaterialTextureAtlasSprite.java']]
 # Compile the existing ownership transaction's real production closure, not a
 # stub: these exact native snowball caches must remain eligible at load complete.
 names=['render/item/nativeitem/staticfamily/SingleMeshEntityItemRenderer.java','render/util/NativeMachineItemRenderer.java','render/util/NativeGuiItemDraw.java','render/util/NativeItemQuadRenderer.java','render/util/NativeIconExtrusion.java','render/util/NativeItemBaseModel.java','render/item/nativeitem/direct/PlainItemPerspectives.java','render/item/nativeitem/ItemTransformMatrix.java','render/item/nativeitem/StaticMeshItemModel.java','render/item/nativeitem/StaticMeshItemRegistration.java','render/item/nativeitem/AngelCoreFrameCorrection.java','render/item/nativeitem/staticfamily/OrdinaryMeshItemModel.java','render/mesh/MeshModel.java','render/mesh/ObjMesh.java']
 files += [root/'src/main/java/com/hbm'/name for name in names]
 files += sorted(f for f in (root/'src/main/java/com/hbm/render/item/nativeitem/context').glob('*.java') if f.name!='SednaPlayerOwnership.java')
 subprocess.run([str(a.java_home/'bin/javac'),'-source','8','-target','8','-encoding','UTF-8','-cp',a.classpath,'-d',str(tmp)]+list(map(str,files)),check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-Djava.library.path='+str(tmp),'-cp',str(tmp)+os.pathsep+a.classpath,'SnowballFactoriesNativeTest',str(root/'src/main/resources'),str(a.vanilla)],cwd=tmp,env=dict(os.environ,MESA_SHADER_CACHE_DISABLE='true'),check=True)
