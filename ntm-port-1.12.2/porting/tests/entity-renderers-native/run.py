#!/usr/bin/env python3
"""Native manager/constructor/registration proof with seven complete renderer classes.
Sixteen source-derived constructor+field projections are explicitly NOT render proofs.
No downloads, Gradle, generated production sources, or native API stand-ins.
"""
import argparse, hashlib, json, os, pathlib, re, subprocess, tempfile, zipfile
p=argparse.ArgumentParser(); p.add_argument('--classpath');p.add_argument('--java-home',type=pathlib.Path);p.add_argument('--vanilla',type=pathlib.Path);p.add_argument('--diagnostics',type=pathlib.Path);p.add_argument('--attribution',type=pathlib.Path)
p.add_argument('--audit-only',action='store_true');p.add_argument('--mutant',choices=['global-manager','quacc-scale','duck-texture']);
a=p.parse_args(); root=pathlib.Path(__file__).resolve().parents[3]; here=pathlib.Path(__file__).resolve().parent
manifest=json.loads((here/'source-manifest.json').read_text()); rows=manifest['classes']; names={r['class'] for r in rows}
def sha(s):return hashlib.sha256(s.encode() if isinstance(s,str) else s).hexdigest()
def inverse(row,s):
 n=row['class'];s=s.replace('import net.minecraft.client.renderer.entity.RenderManager;\n','')
 if row['family']=='direct':
  s=s.replace('extends Render<Entity> {','extends Render {')
  if row['had_constructor']:s=s.replace('public '+n+'(RenderManager renderManager) {\n\t\tsuper(renderManager);','public '+n+'() {')
  else:s=s.replace('\n\n\tpublic '+n+'(RenderManager renderManager) {\n\t\tsuper(renderManager);\n\t}\n','',1)
 elif n=='RenderDuck':
  s=s.replace('import com.hbm.lib.RefStrings;','import com.hbm.entity.mob.EntityDuck;\nimport com.hbm.lib.RefStrings;').replace('import net.minecraft.entity.passive.EntityChicken;','import net.minecraft.entity.Entity;')
  s=s.replace('public RenderDuck(RenderManager renderManager, ModelBase model, float shadowSize) {\n\t\tsuper(renderManager);\n\t\tthis.mainModel = model;\n\t\tthis.shadowSize = shadowSize;','public RenderDuck(ModelBase p_i1252_1_, float p_i1252_2_) {\n\t\tsuper(p_i1252_1_, p_i1252_2_);')
  s=s.replace('\t@Override\n\tprotected ResourceLocation getEntityTexture(EntityChicken entity) {','\tprotected ResourceLocation getEntityTexture(Entity entity) {\n\t\treturn this.getEntityTexture((EntityDuck) entity);\n\t}\n\n\tprotected ResourceLocation getEntityTexture(EntityDuck entity) {')
 elif n=='RenderQuacc':
  s=s.replace('import com.hbm.render.util.ClientBossDisplay;','import net.minecraft.entity.EntityLivingBase;\nimport com.hbm.render.util.ClientBossDisplay;')
  s=s.replace('public RenderQuacc(RenderManager renderManager, ModelBase model, float shadowSize) {\n\t\tsuper(renderManager);\n\t\tthis.mainModel = model;\n\t\tthis.shadowSize = shadowSize;','public RenderQuacc(ModelBase model, float f0) {\n\t\tsuper(model, f0);')
  s=s.replace('    @Override\n    protected ResourceLocation getEntityTexture(EntityChicken DUCC)','    protected ResourceLocation getEntityTexture(EntityChicken DUCC)').replace('    @Override\n    protected void preRenderCallback(EntityChicken DUCC, float f0)','    protected void preRenderCallback(EntityLivingBase DUCC, float f0)')
 else:
  s=s.replace('public RenderQuackos(RenderManager renderManager, ModelBase model, float shadowSize) {\n\t\tsuper(renderManager);\n\t\tthis.mainModel = model;\n\t\tthis.shadowSize = shadowSize;','public RenderQuackos(ModelBase p_i1252_1_, float p_i1252_2_) {\n\t\tsuper(p_i1252_1_, p_i1252_2_);')
  s=s.replace('    @Override\n    protected ResourceLocation getEntityTexture(EntityChicken p_110775_1_)','    protected ResourceLocation getEntityTexture(EntityChicken p_110775_1_)')
 return s
sources={r['class']:(root/r['path']).read_text() for r in rows}
for row in rows:
 assert sha(inverse(row,sources[row['class']]))==row['base_sha256'],row['class']+' exact source inverse differs from CP84'
 assert 'getRenderManager()' not in sources[row['class']],row['class']+' global manager lookup'
for path,expected in manifest['assets'].items():assert sha((root/path).read_bytes())==expected,path
for rename in manifest['asset_renames']:
 old,new=root/rename['from'],root/rename['to']
 assert new.name in {p.name for p in new.parent.iterdir()} and old.name not in {p.name for p in old.parent.iterdir()},'exact native lowercase filename must be the sole stored spelling'
 assert sha(new.read_bytes())==rename['sha256'],'renamed original PNG bytes differ'
references=[]
for path in (root/'src/main').rglob('*'):
 if path.is_file() and path.suffix in ('.java','.json','.mcmeta','.obj','.mtl'):
  for line in path.read_text(errors='replace').splitlines():
   if 'chopperbomb.png' in line.lower():references.append({'path':str(path.relative_to(root)),'text':line.strip()})
assert references==manifest['renamed_asset_references'],'texture reference ownership changed'
for item in manifest['model_mesh_closure']:assert sha((root/item['path']).read_bytes())==item['sha256'],item['path']
proxy=(root/'src/main/java/com/hbm/main/ClientProxy.java').read_text();lines=[]
for reg in manifest['registrations']:
 e,n,args=reg['entity'],reg['renderer'],reg['original_arguments'];line='RenderingRegistry.registerEntityRenderingHandler('+e+'.class, renderManager -> new '+n+'(renderManager'+(', '+args if args else '')+'));'
 assert proxy.count(line)==1,line
 assert len(re.findall(r'registerEntityRenderingHandler\('+e+r'\.class,',proxy))==1,e
 lines.append(line)
assert [proxy.index(x) for x in lines]==sorted(proxy.index(x) for x in lines),'registration order'
# Audit every constructor call and subclass for this closed family, not just the known proxy lines.
subclasses=[];callers=[]
for path in (root/'src/main/java').rglob('*.java'):
 s=path.read_text();
 for match in re.finditer(r'\bextends ('+'|'.join(names)+r')\b',s):subclasses.append((path.stem,match.group(1)))
 for match in re.finditer(r'new ('+'|'.join(names)+r')\s*\(',s):
  assert path.name=='ClientProxy.java',(path,match.group())
  callers.append(match.group(1))
assert subclasses==[('RenderQuasar','RenderBlackHole')],subclasses
assert len(callers)==24
assert not re.search(r'new (RenderBalls|RenderQuackos)\(',proxy),'unregistered classes must not get invented ownership'
assert 'extends EntityChicken' in (root/'src/main/java/com/hbm/entity/mob/EntityDuck.java').read_text()
assert 'extends EntityDuck implements INtmBossDisplay' in (root/'src/main/java/com/hbm/entity/mob/EntityQuackos.java').read_text()
print('PASS exact CP84 source inverses: 23 complete renderer files; all original draw bodies and constructor assignments retained; 24 exact ordered factories, derived Quasar closure, 29 existing direct resource hashes, unchanged model/mesh sources.',flush=True)
selected=[]
if a.diagnostics:
 for block in re.split(r'(?=^/[^\n]+:\d+: error: )',a.diagnostics.read_text(),flags=re.M):
  m=re.match(r'^/[^\n]+/(Render\w+)\.java:(\d+): error: (.*)',block)
  if not m or m[1] not in names:continue
  kind='render-constructor' if m[3].startswith('constructor Render in class') else 'chicken-constructor' if m[3].startswith('constructor RenderChicken in class') else 'generic-override' if m[3].startswith('name clash:') else None
  if kind:selected.append({'class':m[1],'line':int(m[2]),'kind':kind,'header':block.splitlines()[0]})
 assert {k:sum(r['kind']==k for r in selected) for k in set(r['kind'] for r in selected)}=={'render-constructor':19,'chicken-constructor':3,'generic-override':2}
 print('PASS CP84 typed attribution: 19 direct Render constructor + 3 RenderChicken constructor + 2 typed-override diagnostics. Quasar is required dependent closure, not an extra claimed error.',flush=True)
 if a.attribution:a.attribution.write_text(json.dumps(selected,indent=2)+'\n')
if a.audit_only:raise SystemExit(0)
if not all([a.classpath,a.java_home,a.vanilla]):p.error('native execution needs --classpath, --java-home and --vanilla')
# Keep fields and constructors verbatim. Strip every ordinary method (and its
# annotations) from sixteen projection classes, retaining actual mesh/model init.
# A throwing texture method prevents any projection being mistaken for drawable.
def masked(s):
 return re.sub(r'"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|//[^\n]*|/\*[\s\S]*?\*/',lambda m:' '*len(m.group()),s)
def project(n,s):
 mask=masked(s);start=re.search(r'public class '+n+r' extends [^{]+\{',mask).end();end=mask.rfind('}');members=[];pos=start;i=start
 while i<end:
  if mask[i]==';':members.append(s[pos:i+1]);pos=i+1
  elif mask[i]=='{':
   signature=mask[pos:i];depth=1;j=i+1
   while depth:
    if mask[j]=='{':depth+=1
    if mask[j]=='}':depth-=1
    j+=1
   if re.search(r'\b'+n+r'\s*\(',signature):members.append(s[pos:j])
   i=j-1;pos=j
  i+=1
 imports=[]
 for line in s[:start].splitlines():
  if line.startswith('import ') and (not line.startswith('import com.hbm.') or re.search(r'com\.hbm\.(lib\.RefStrings|render\.(model|mesh|loader))',line)):imports.append(line)
 package=re.search(r'package [^;]+;',s).group();header=re.search(r'public class '+n+r' extends [^{]+\{',s).group()
 result=package+'\n'+'\n'.join(imports)+'\n'+header+'\n'+'\n'.join(members)
 if n!='RenderQuasar':result+='\n@Override protected ResourceLocation getEntityTexture(Entity ignored) { throw new AssertionError("Constructor-only projection must not render"); }\n'
 return result+'\n}\n'
with tempfile.TemporaryDirectory(prefix='entity-renderers-native-') as tmp:
 tmp=pathlib.Path(tmp);files=[]
 rename=manifest['asset_renames'][0];before=tmp/'case-before.zip';after=tmp/'case-after.zip'
 for target,key in [(before,'from'),(after,'to')]:
  with zipfile.ZipFile(target,'w') as z:z.writestr(rename[key].removeprefix('src/main/resources/'),(root/rename['to']).read_bytes())
 for row in rows:
  if row['full_class_test']:
   mutation={'global-manager':('RenderBeam5','super(renderManager);','super(net.minecraft.client.Minecraft.getMinecraft().getRenderManager());'),'quacc-scale':('RenderQuacc','GL11.glScaled(25, 25, 25);','GL11.glScaled(1, 1, 1);'),'duck-texture':('RenderDuck','textures/entity/duck.png','textures/entity/chicken.png')}.get(a.mutant)
   if mutation and mutation[0]==row['class']:
    out=tmp/row['class']/(row['class']+'.java');out.parent.mkdir();assert sources[row['class']].count(mutation[1])==1;out.write_text(sources[row['class']].replace(mutation[1],mutation[2]));files.append(out)
   else:files.append(root/row['path'])
  else:
   out=tmp/row['class']/(row['class']+'.java');out.parent.mkdir();out.write_text(project(row['class'],sources[row['class']]));files.append(out)
 entities=[]
 for reg in manifest['registrations']:
  e=reg['entity']
  if e=='EntityDuck':body='static class EntityDuck extends net.minecraft.entity.passive.EntityChicken { EntityDuck(){super(null);} }'
  elif e=='EntityQuackos':body='static class EntityQuackos extends EntityDuck implements com.hbm.entity.boss.INtmBossDisplay { public net.minecraft.util.text.ITextComponent getDisplayName(){return new net.minecraft.util.text.TextComponentString("Factory duck");} }'
  else:body='static class '+e+' extends net.minecraft.entity.Entity { '+e+'(){super(null);} protected void entityInit(){} protected void readEntityFromNBT(net.minecraft.nbt.NBTTagCompound n){} protected void writeEntityToNBT(net.minecraft.nbt.NBTTagCompound n){} }'
  entities.append(body)
 fixture=tmp/'RendererRegistrationFixture.java'
 fixture.write_text('import net.minecraft.client.model.ModelChicken; import net.minecraftforge.fml.client.registry.RenderingRegistry;\n'+''.join('import '+re.search(r'package ([^;]+);',sources[r['class']])[1]+'.'+r['class']+';\n' for r in rows)+'''final class RendererRegistrationFixture {\n'''+ '\n'.join(entities)+'\nstatic void register(){\n'+'\n'.join(lines)+'\n}\nstatic Class<?>[] entities(){return new Class<?>[]{'+','.join(r['entity']+'.class' for r in manifest['registrations'])+'};}\nstatic Class<?>[] renderers(){return new Class<?>[]{'+','.join(r['renderer']+'.class' for r in manifest['registrations'])+'};}\n}\n')
 files += [fixture,here/'EntityRenderersNativeTest.java',root/'porting/tests/MaterialTextureAtlasSpriteNativeTest.java']
 files += [root/x['path'] for x in manifest['model_mesh_closure']]
 files += [root/'src/main/java/com/hbm'/x for x in ['lib/RefStrings.java','entity/boss/INtmBossDisplay.java','render/util/ClientBossDisplay.java','render/item/nativeitem/MaterialTextureAtlasSprite.java']]
 for jar in a.classpath.split(os.pathsep):
  if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
   with zipfile.ZipFile(jar) as z:
    for name in z.namelist():
     if name.endswith('.so') and '/' not in name:(tmp/name).write_bytes(z.read(name))
 subprocess.run([str(a.java_home/'bin/javac'),'-source','8','-target','8','-encoding','UTF-8','-Xlint:unchecked','-cp',a.classpath,'-d',str(tmp)]+list(map(str,files)),check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-Djava.library.path='+str(tmp),'-cp',str(tmp)+os.pathsep+a.classpath,'EntityRenderersNativeTest',str(root/'src/main/resources'),str(a.vanilla),str(before),str(after)],cwd=tmp,env=dict(os.environ,MESA_SHADER_CACHE_DISABLE='true'),check=True)
