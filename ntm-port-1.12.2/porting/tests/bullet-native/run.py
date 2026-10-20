#!/usr/bin/env python3
"""Complete native renderer/helpers, real GL and actual original Tessellator oracle.
The few NTM entity/registry collaborators are explicitly bounded fixtures.
"""
import argparse,hashlib,importlib.util,json,os,pathlib,re,subprocess,tempfile,zipfile
P=pathlib.Path
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=P,required=True);p.add_argument('--legacy-client',type=P,required=True);p.add_argument('--original-root',type=P,required=True);p.add_argument('--write-scope',action='store_true',help='Freeze reviewed evidence after intentional source changes.');a=p.parse_args()
root=P(__file__).resolve().parents[3];here=P(__file__).resolve().parent
assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
# The sole vanilla material used by the meteor branch is byte-identical in the
# actual original/native JARs, not merely the same resource path.
old_obsidian=zipfile.ZipFile(a.legacy_client).read('assets/minecraft/textures/blocks/obsidian.png')
found_obsidian=False
for jar in a.classpath.split(os.pathsep):
 if P(jar).is_file() and zipfile.is_zipfile(jar):
  with zipfile.ZipFile(jar) as z:
   if 'assets/minecraft/textures/blocks/obsidian.png' in z.namelist():
    assert z.read('assets/minecraft/textures/blocks/obsidian.png')==old_obsidian
    found_obsidian=True;break
assert found_obsidian,'Native vanilla obsidian material unavailable'
def method(text,signature):
 start=text.index(signature);brace=text.index('{',start);depth=1;i=brace+1
 while depth:
  if text[i]=='{':depth+=1
  if text[i]=='}':depth-=1
  i+=1
 return text[start:i]
with tempfile.TemporaryDirectory(prefix='bullet-native-') as td:
 tmp=P(td);files=[]
 def fixture(name,body):
  f=tmp/'fixtures'/(name+'.java');f.parent.mkdir(parents=True,exist_ok=True);f.write_text(body);files.append(f);return f
 # Reuse the genuine native item draw test setup and its documented NTM boundaries.
 for n in ['render/util/NativeItemBaseModel','render/util/RenderDecoItem','render/util/RenderItemStack','render/util/NativeMachineItemRenderer','render/util/NativeIconExtrusion','render/util/NativeItemQuadRenderer','render/util/NativeGuiItemDraw','render/item/nativeitem/MaterialTextureAtlasSprite']:
  files.append(root/('src/main/java/com/hbm/'+n+'.java'))
 for n in ['NativeMachineItemTest','NativeMachineDrawTest','NativeMachineConsumerTest','NativeBaseModelOwnershipTest','MaterialTextureAtlasSpriteNativeTest']:
  files.append(root/('porting/tests/'+n+'.java'))
 spec=importlib.util.spec_from_file_location('fixtures',root/'porting/tests/machine-renderer-fixtures.py');fx=importlib.util.module_from_spec(spec);spec.loader.exec_module(fx);fx.add(root,tmp,files)
 sources={name:(root/('src/main/java/com/hbm/'+name+'.java')).read_text() for name in ['render/entity/projectile/RenderBullet','render/util/BeamPronter','render/util/RenderSparks','entity/projectile/EntityBulletBaseNT','entity/projectile/EntityThrowableNT']}
 for n in ['render/entity/projectile/RenderBullet','render/util/BeamPronter','render/util/RenderSparks','render/model/ModelBullet','entity/projectile/IBulletBase','util/Tuple','interfaces/Spaghetti','render/mesh/ObjMeshParser','render/mesh/ObjMeshRenderer','render/mesh/ObjMeshModel','render/mesh/ReloadableObjMesh','render/mesh/ObjMeshVboPayload','render/loader/HFRWavefrontObject','render/loader/HFRWavefrontObjectVBO']:
  files.append(root/('src/main/java/com/hbm/'+n+'.java'))
 # Entity mechanics are not replaced in production. Probe bodies below are the
 # exact typed field/init/getter fragments plus controlled native Entity state.
 entity=sources['entity/projectile/EntityBulletBaseNT'];base=sources['entity/projectile/EntityThrowableNT']
 imports='import net.minecraft.network.datasync.*; import net.minecraft.nbt.NBTTagCompound; import net.minecraft.world.World;'
 fields='\n'.join(re.findall(r'\s*private static final DataParameter[^;]+;',entity))
 basefields='\n'.join(re.findall(r'\s*private static final DataParameter[^;]+;',base))
 fixture('com/hbm/entity/projectile/EntityThrowableNT','package com.hbm.entity.projectile; '+imports+' public abstract class EntityThrowableNT extends net.minecraft.entity.Entity { '+basefields+' public EntityThrowableNT(World w){super(w);} '+''.join(method(base,s) for s in ['protected void entityInit()','public void setStuckIn(int side)','public int getStuckIn()'])+' protected void readEntityFromNBT(NBTTagCompound nbt){} protected void writeEntityToNBT(NBTTagCompound nbt){} }')
 fixture('com/hbm/entity/projectile/EntityThrowableInterp','package com.hbm.entity.projectile; public abstract class EntityThrowableInterp extends EntityThrowableNT { public EntityThrowableInterp(net.minecraft.world.World w){super(w);} }')
 access=''.join(method(entity,s) for s in ['public int getRenderStyle()','public int getRenderTrail()','protected void entityInit()','public boolean writeToNBTOptional(NBTTagCompound nbt)','public void readEntityFromNBT(NBTTagCompound nbt)'])
 setters='\n'.join(re.findall(r'\s*this\.dataManager\.set\([^;]+;',entity)[:3])
 fixture('com/hbm/entity/projectile/EntityBulletBaseNT','''package com.hbm.entity.projectile;
import java.util.*;import com.hbm.util.Tuple.Pair;import net.minecraft.util.math.Vec3d;'''+imports+''' public class EntityBulletBaseNT extends EntityThrowableInterp implements IBulletBase {'''+fields+access+'''
 public EntityBulletBaseNT(){super(null);} public double px,py,pz; public java.util.List<Pair<Vec3d,Double>> trail=new ArrayList<>();
 public double prevX(){return px;} public double prevY(){return py;} public double prevZ(){return pz;} public void prevX(double v){px=v;} public void prevY(double v){py=v;} public void prevZ(double v){pz=v;} public List<Pair<Vec3d,Double>> nodes(){return trail;}
 private com.hbm.handler.BulletConfiguration config; public void setConfig(int config, int style, int trail){this.config=new com.hbm.handler.BulletConfiguration();this.config.style=style;this.config.trail=trail;'''+setters+'''} public int configId(){return dataManager.get(BULLET_CONFIG);} }
''')
 cfg=(root/'src/main/java/com/hbm/handler/BulletConfiguration.java').read_text();constants='\n'.join(re.findall(r'\s*public static final int (?:STYLE|BOLT)_\w+\s*=\s*-?\d+;',cfg))
 fixture('com/hbm/handler/BulletConfiguration','package com.hbm.handler; public class BulletConfiguration { public int style,trail;'+constants+'}')
 items=tmp/'fixtures/com/hbm/items/ModItems.java';items.write_text(items.read_text().replace('public class ModItems {','public class ModItems { public static net.minecraft.item.Item blade_titanium=net.minecraft.init.Items.DIAMOND_SWORD;'))
 resources=tmp/'fixtures/com/hbm/main/ResourceManager.java';rs=resources.read_text();refs=set(re.findall(r'ResourceManager\.(\w+)',sources['render/entity/projectile/RenderBullet']))
 for n in sorted(refs):
  if re.search(r'\b'+n+r'\s*=',rs):continue
  rs=rs.replace('public class ResourceManager {','public class ResourceManager {'+('public static net.minecraft.util.ResourceLocation '+n+'=new net.minecraft.util.ResourceLocation("test","'+n+'");' if n.endswith('_tex') else 'public static Draw '+n+'=new Draw();'))
 rs=rs.replace('public static class Draw implements','public static class Draw implements')
 rs=rs.replace('public final java.util.List<String> calls=', 'public com.hbm.render.mesh.MeshModel delegate; public final java.util.List<String> calls=')
 rs=rs.replace('public void renderAll(){capture("all");}', 'public void renderAll(){capture("all");if(delegate!=null)delegate.renderAll();}').replace('public void renderPart(String n){capture(n);}', 'public void renderPart(String n){capture(n);if(delegate!=null)delegate.renderPart(n);}')
 resources.write_text(rs)
 # The original source retains every formula/branch; only mapped platform/holder
 # endpoints are changed. LegacyTess invokes actual original bmh bytecode.
 for name,path in [('OriginalBullet','render/entity/projectile/RenderBullet'),('OriginalBeam','render/util/BeamPronter'),('OriginalSparks','render/util/RenderSparks')]:
  old=(a.original_root/('src/main/java/com/hbm/'+path+'.java')).read_text()
  old=re.sub(r'^package [^;]+;', 'package bulletoracle;',old,flags=re.M)
  old=old.replace('import net.minecraft.client.renderer.Tessellator;', 'import bulletoracle.LegacyTess;').replace('Tessellator','LegacyTess')
  old=old.replace('import net.minecraft.util.Vec3;', 'import net.minecraft.util.math.Vec3d;').replace('Vec3.createVectorHelper(', 'new Vec3d(');old=re.sub(r'\bVec3\b','Vec3d',old)
  old=old.replace('.xCoord','.x').replace('.yCoord','.y').replace('.zCoord','.z').replace('import net.minecraft.util.MathHelper;', 'import net.minecraft.util.math.MathHelper;').replace('MathHelper.sqrt_double(', 'MathHelper.sqrt(')
  old=old.replace('.lengthVector()', '.length()')
  old=re.sub(r'spinner\.rotateAroundY\(([^;]+)\);',r'spinner = spinner.rotateYaw(\1);',old)
  old=old.replace('pos.x += deltaX * mult;\n\t\t\tpos.y += deltaY * mult;\n\t\t\tpos.z += deltaZ * mult;', 'pair.key = new Vec3d(pos.x + deltaX * mult, pos.y + deltaY * mult, pos.z + deltaZ * mult);')
  old=old.replace('dir.x *= length * rand.nextFloat();\n\t\t\tdir.y *= length * rand.nextFloat();\n\t\t\tdir.z *= length * rand.nextFloat();', 'dir = new Vec3d(dir.x * (length * rand.nextFloat()), dir.y * (length * rand.nextFloat()), dir.z * (length * rand.nextFloat()));')
  old=old.replace('Minecraft.getMinecraft().theWorld','Minecraft.getMinecraft().world').replace('Minecraft.getMinecraft().thePlayer','Minecraft.getMinecraft().player')
  old=old.replace('import com.hbm.render.util.BeamPronter;', 'import bulletoracle.OriginalBeam;').replace('import com.hbm.render.util.BeamPronter.', 'import bulletoracle.OriginalBeam.').replace('import com.hbm.render.util.RenderSparks;', 'import bulletoracle.OriginalSparks;').replace('BeamPronter','OriginalBeam').replace('RenderSparks','OriginalSparks')
  if name=='OriginalBullet':
   old=old.replace('RenderBullet','OriginalBullet').replace('extends Render {','extends Render<EntityBulletBaseNT> {').replace('public OriginalBullet() {','public OriginalBullet(RenderManager manager) { super(manager);').replace('public void doRender(Entity bullet,','public void doRender(EntityBulletBaseNT bullet,').replace('protected ResourceLocation getEntityTexture(Entity p_', 'protected ResourceLocation getEntityTexture(EntityBulletBaseNT p_')
   old=old.replace('bullet.getDataWatcher().getWatchableObjectByte(16)','bullet.getRenderStyle()').replace('bullet.getDataWatcher().getWatchableObjectByte(17)','bullet.getRenderTrail()')
   old=old.replace('import net.minecraft.client.renderer.entity.RenderItem;','')
   blade=method(old,'private void renderBlade()');old=old.replace(blade,'private void renderBlade() { throw new AssertionError("Blade oracle is separately covered by the native frame-helper tests and original transform audit"); }')
  fixture('bulletoracle/'+name,old)
 fixture('bulletoracle/LegacyTess',(here/'LegacyTess.java.in').read_text())
 tex_source=(root/'src/main/java/com/hbm/main/ResourceManager.java').read_text()
 textures=[]
 for name in sorted(refs):
  if name.endswith('_tex'):
   expr=re.search(r'public static final ResourceLocation '+name+r' = ([^;]+);',tex_source).group(1).replace('RefStrings.MODID','\"hbm\"')
   textures.append('ResourceManager.'+name+' = '+expr+';')
 test=fixture('BulletNativeTest',(here/'BulletNativeTest.java.in').read_text().replace('/*TEXTURE_DECLARATIONS*/','\n'.join(textures)))
 for jar in a.classpath.split(os.pathsep):
  if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
   with zipfile.ZipFile(jar) as z:
    for n in z.namelist():
     if n.endswith('.so') and '/' not in n:(tmp/n).write_bytes(z.read(n))
 # Pin every complete compiled production class and the source declarations
 # from which controlled NTM fixture fragments/oracles are generated.
 compiled={str(f.relative_to(root)):hashlib.sha256(f.read_bytes()).hexdigest() for f in files if root in f.parents and '/src/main/java/' in str(f)}
 support={str(f.relative_to(root)):hashlib.sha256(f.read_bytes()).hexdigest() for f in files if root in f.parents and '/porting/' in str(f)}
 for n in ['src/main/java/com/hbm/entity/projectile/EntityBulletBaseNT.java','src/main/java/com/hbm/entity/projectile/EntityThrowableNT.java','src/main/java/com/hbm/handler/BulletConfiguration.java','src/main/java/com/hbm/main/ResourceManager.java','porting/tests/machine-renderer-fixtures.py']:
  support[n]=hashlib.sha256((root/n).read_bytes()).hexdigest()
 original={('src/main/java/com/hbm/'+n+'.java'):hashlib.sha256((a.original_root/('src/main/java/com/hbm/'+n+'.java')).read_bytes()).hexdigest() for n in ['render/entity/projectile/RenderBullet','render/util/BeamPronter','render/util/RenderSparks','entity/projectile/EntityBulletBaseNT','entity/projectile/EntityThrowableNT']}
 assets=['models/projectiles/projectiles.obj','models/projectiles/leadburster.obj','models/sphere_uv.obj','models/weapons/meteor.obj','textures/items/blade_titanium.png','textures/models/bullet.png','textures/models/emplacer.png','textures/models/tau.png','textures/blocks/block_meteor_molten.png']
 for name in sorted(refs):
  if name.endswith('_tex'):
   expr=re.search(r'public static final ResourceLocation '+name+r' = ([^;]+);',tex_source).group(1)
   assets.append(re.findall(r'"([^"\n]+)"',expr)[-1])
 asset_hashes={n:hashlib.sha256((root/'src/main/resources/assets/hbm'/n).read_bytes()).hexdigest() for n in sorted(set(assets))}
 for n,digest in asset_hashes.items():assert hashlib.sha256((a.original_root/'src/main/resources/assets/hbm'/n).read_bytes()).hexdigest()==digest,('asset changed from pristine original',n)
 scope={'baseline':'641ba083c276c6ac75afa6a8f996cdd86f8b16d4','complete_compiled_sources':compiled,'support_sources':support,'original_sources':original,'unchanged_original_assets':asset_hashes,'fixtures':[str(f.relative_to(tmp)) for f in files if tmp in f.parents]}
 if a.write_scope:(here/'scope.json').write_text(json.dumps(scope,indent=2)+'\n')
 else:assert json.loads((here/'scope.json').read_text())==scope,'Frozen source/asset/oracle/fixture scope changed; review before regenerating.'
 assert (root/'src/main/java/com/hbm/main/ClientProxy.java').read_text().count('RenderingRegistry.registerEntityRenderingHandler(EntityBulletBaseNT.class, RenderBullet::new);')==1
 print('PASS frozen '+str(len(compiled))+' complete compiled sources, '+str(len(support))+' supporting sources, '+str(len(original))+' pristine source oracles, '+str(len(asset_hashes))+' unchanged original model/texture assets.',flush=True)
 assert (tmp/'liblwjgl64.so').exists()
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',str(tmp)]+[str(f) for f in files],check=True)
 print('PASS complete RenderBullet, BeamPronter, RenderSparks, real native item/mesh pipeline compile; NTM registry/entity boundaries explicit.',flush=True)
 for test,args in [('NativeMachineItemTest',[]),('BulletNativeTest',[str(a.legacy_client),str(root/'src/main/resources')])]:
  subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-Djava.library.path='+str(tmp),'-cp',str(tmp)+os.pathsep+a.classpath,test]+args,cwd=tmp,env=dict(os.environ,MESA_SHADER_CACHE_DISABLE='true'),check=True)
