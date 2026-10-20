import pathlib,subprocess,tempfile,os,json,zipfile,hashlib,sys
import check_source as guard
H=pathlib.Path(__file__).resolve().parent;R=H.parents[2];J=pathlib.Path('/workspace/shared/nagaport-toolchain/java8');CPF=pathlib.Path('/workspace/shared/nagaport-toolchain/native-classpath.txt');CP=CPF.read_text().strip();OLD=pathlib.Path('/tmp/nagaport-legacy-client.jar')
assert hashlib.sha256(OLD.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
srg=zipfile.ZipFile('/tmp/nagaport1710-srg.zip').read('joined.srg').decode()
for row in ['FD: sa/E net/minecraft/entity/Entity/field_70123_F','FD: sa/F net/minecraft/entity/Entity/field_70124_G','FD: sa/G net/minecraft/entity/Entity/field_70132_H','FD: ahb/C net/minecraft/world/World/field_72984_F']: assert row in srg
M=json.loads((H/'manifest.json').read_text());guard.main()
def run(cmd):subprocess.run(list(map(str,cmd)),check=True)
with tempfile.TemporaryDirectory(prefix='cp89-fields-') as td:
 d=pathlib.Path(td);tools=J/'lib/tools.jar'
 run([J/'bin/javac','-cp',tools,'-d',d,H/'NativeFieldScanner.java'])
 cases=[
 ('class T {boolean f(Entity e){return e.isCollided;}}',1),
 ('class T {void f(Entity e){e.isCollidedHorizontally=true;}}',1),
 ('class T extends EntityItem {T(){super((World)null);} boolean f(boolean collided){return isCollided;}}',1),
 ('class T extends EntityItem {boolean collided;T(){super((World)null);} boolean f(){return isCollided;}}',0),
 ('class T extends EntityItem {boolean isCollided;T(){super((World)null);} boolean f(){return isCollided;}}',0),
 ('class T {boolean f(Object e){return e.isCollided;}}',0),
 ('class T {boolean f(Missing e){return e.isCollided;}}',0),
 ('class T {boolean f(){return Entity.isCollided;}}',0),
 ('class T extends EntityItem {T(){super((World)null);} static boolean f(){return isCollided;}}',0),
 ('class T extends EntityItem {T(){super((World)null);} class Inner {boolean f(){return isCollided;}}}',0),
 ('class T extends EntityItem {T(){super((World)null);} class Inner {boolean f(){return T.this.isCollided;}}}',1),
 ('class T {Object f(World w){return w.theProfiler;}}',1),
 ('class T {Object theProfiler;Object f(T t){return t.theProfiler;}}',0),
 ('class T {boolean f(Entity e){String s="😀";return e.isCollidedVertically;}}',1),
 ('class T<E extends Entity> {boolean f(E e){return e.isCollided;}}',0),
 ('class T extends EntityItem implements S {T(){super((World)null);} boolean f(){return isCollided;}} interface S {boolean collided=false;}',0),
 ]
 paths=[]
 import re
 for i,(src,n) in enumerate(cases):
  p=d/('T%d.java'%i);p.write_text('import net.minecraft.entity.Entity;import net.minecraft.entity.item.EntityItem;import net.minecraft.world.World;'+re.sub(r'\bT\b','T%d'%i,src));paths.append(p)
 inp=d/'inputs';inp.write_text('\n'.join(map(str,paths)))
 rows=subprocess.check_output([str(J/'bin/java'),'-cp',str(d)+os.pathsep+str(tools),'NativeFieldScanner',str(inp),str(CPF)],text=True).splitlines()
 for i,(_,n) in enumerate(cases):assert sum(x.startswith(str(paths[i])+'\t')for x in rows)==n,(i,n,rows)
 print('PASS',len(cases),'typed receiver/shadow/static/nested/Unicode controls')
 if '--attribute' in sys.argv:
  tree=d/'before';tree.mkdir(); paths=[]
  with zipfile.ZipFile(H/'baseline.zip') as z:
   for p in sorted((R/'src/main/java').rglob('*.java')):
    rel=str(p.relative_to(R));q=tree/rel;q.parent.mkdir(parents=True,exist_ok=True);q.write_bytes(z.read(rel) if rel in M['files'] else p.read_bytes());paths.append(q)
  inp.write_text('\n'.join(map(str,paths)))
  rows=subprocess.check_output([str(J/'bin/java'),'-Xmx3G','-cp',str(d)+os.pathsep+str(tools),'NativeFieldScanner',str(inp),str(CPF)],text=True).splitlines()
  expected=sorted((p,e['start'],e['end'],e['new'],e['receiver'],e['old'],e['owner'])for p,f in M['files'].items()for e in f['edits'])
  actual=[]
  for line in rows:
   p,a,b,new,t,old,owner=line.split('\t');actual.append((str(pathlib.Path(p).relative_to(tree)),int(a),int(b),new,t,old,owner))
  assert sorted(actual)==expected;print('PASS full-source typed selection',len(actual))
 # Explicit bounded production projection: only the four exact collision assignments.
 p='src/main/java/com/hbm/entity/projectile/EntityDebrisBase.java'
 s=(R/p).read_text();start=s.index('\t\tthis.collidedHorizontally =');end=s.index('\n\t\tthis.updateFallState',start);body=s[start:end]
 oldbody=zipfile.ZipFile(H/'baseline.zip').read(p).decode();a=oldbody.index('\t\tthis.isCollidedHorizontally =');b=oldbody.index('\n\t\tthis.updateFallState',a);oldbody=oldbody[a:b]
 native='import net.minecraft.entity.item.EntityItem;import net.minecraft.world.World; class NativeProjection extends EntityItem {NativeProjection(){super((World)null);}void update(double initMoX,double initMoY,double initMoZ,double moX,double moY,double moZ){'+body+'}}'
 legacy='class LegacyProjection extends xk {LegacyProjection(){super((ahb)null);}void update(double initMoX,double initMoY,double initMoZ,double moX,double moY,double moZ){'+oldbody.replace('this.isCollidedHorizontally','this.E').replace('this.isCollidedVertically','this.F').replace('this.isCollided','this.G').replace('this.onGround','this.D')+'}}'
 (d/'NativeProjection.java').write_text(native);(d/'LegacyProjection.java').write_text(legacy)
 # Strip only signature metadata in temporary class-identical original container for default-package projections.
 unsigned=d/'legacy.jar'
 with zipfile.ZipFile(OLD) as z,zipfile.ZipFile(unsigned,'w') as out:
  for i in z.infolist():
   if i.filename.upper().startswith('META-INF/')and i.filename.upper().endswith(('.SF','.RSA','.DSA')):continue
   out.writestr(i,z.read(i.filename))
 run([J/'bin/javac','-cp',CP+os.pathsep+str(unsigned),'-d',d,d/'NativeProjection.java',d/'LegacyProjection.java',H/'NativeFieldTest.java'])
 run([J/'bin/java','-cp',str(d)+os.pathsep+CP+os.pathsep+str(unsigned),'NativeFieldTest'])

 if '--mutants' in sys.argv:
  mutants=[body.replace('initMoX != moX || initMoZ != moZ','initMoX != moX && initMoZ != moZ'),body.replace('initMoY != moY;','initMoY == moY;'),body.replace('this.collidedHorizontally || this.collidedVertically','this.collidedHorizontally && this.collidedVertically')]
  for i,m in enumerate(mutants):
   assert m!=body
   (d/'NativeProjection.java').write_text(native.replace(body,m))
   run([J/'bin/javac','-cp',CP+os.pathsep+str(unsigned),'-d',d,d/'NativeProjection.java'])
   result=subprocess.run([str(J/'bin/java'),'-cp',str(d)+os.pathsep+CP+os.pathsep+str(unsigned),'NativeFieldTest'],text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
   assert result.returncode and 'exact four production assignments original/native' in result.stdout,(i,result.stdout)
  print('PASS three executable production-formula mutants rejected')
