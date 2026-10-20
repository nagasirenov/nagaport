#!/usr/bin/env python3
import argparse,hashlib,importlib.util,json,os,pathlib,re,subprocess,tempfile,zipfile
p=argparse.ArgumentParser();p.add_argument('--java-home',type=pathlib.Path,required=True);p.add_argument('--classpath-file',type=pathlib.Path,required=True);p.add_argument('--legacy-client',type=pathlib.Path,required=True);p.add_argument('--legacy-srg',type=pathlib.Path,required=True);p.add_argument('--verify-attribution',action='store_true');a=p.parse_args();h=pathlib.Path(__file__).resolve().parent;root=h.parents[2]
assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
srg=zipfile.ZipFile(a.legacy_srg).read('joined.srg').decode();assert 'FD: sa/C net/minecraft/entity/Entity/field_70121_D' in srg and 'CL: azt net/minecraft/util/AxisAlignedBB' in srg
cp=a.classpath_file.read_text().strip();java=str(a.java_home/'bin/java');javac=str(a.java_home/'bin/javac');tools=str(a.java_home/'lib/tools.jar')
spec=importlib.util.spec_from_file_location('guard',h/'check_source.py');guard=importlib.util.module_from_spec(spec);spec.loader.exec_module(guard);guard.main()
with tempfile.TemporaryDirectory(prefix='entity-bounds-proof-') as td:
 d=pathlib.Path(td);subprocess.run([javac,'-cp',tools,'-d',str(d),str(h/'EntityBoundsScanner.java')],check=True)
 def scan(paths):
  inp=d/'inputs';inp.write_text('\n'.join(map(str,paths))+'\n');return [r.split('\t') for r in subprocess.check_output([java,'-Xmx3g','-cp',str(d)+os.pathsep+tools,'EntityBoundsScanner',str(inp),str(a.classpath_file)],text=True).splitlines()]
 prefix='import net.minecraft.entity.Entity; import net.minecraft.entity.item.EntityItem; import net.minecraft.world.World; import net.minecraft.util.math.AxisAlignedBB; '
 cases=[
 ('class T {Object f(Entity e){return e.boundingBox;}}',1),
 ('class T extends EntityItem {T(){super((World)null);} Object f(){return boundingBox;}}',1),
 ('class T extends EntityItem {T(){super((World)null);} Object f(){return this.boundingBox;}}',1),
 ('class T extends EntityItem {T(){super((World)null);} Object f(){return super.boundingBox;}}',1),
 ('class T {Object boundingBox; Object f(T t){return t.boundingBox;}}',0),
 ('class T extends EntityItem {Object boundingBox; T(){super((World)null);} Object f(){return this.boundingBox;}}',0),
 ('class T extends EntityItem {T(){super((World)null);} Object f(Object boundingBox){return boundingBox;}}',0),
 ('class T {Object f(Object e){return e.boundingBox;}}',0),
 ('class T {Object f(Missing e){return e.boundingBox;}}',0),
 ('class T {Object f(Entity e){return e.getEntityBoundingBox();} String s="boundingBox"; /* e.boundingBox */}',0),
 ('class T {Object f(Entity e){return ((Entity)e).boundingBox;}}',1),
 ('class T {Object f(){return Entity.boundingBox;}}',0),
 ('class T extends EntityItem {T(){super((World)null);} static Object f(){return boundingBox;}}',0),
 ('class T extends EntityItem {T(){super((World)null);} class Inner {Object f(){return boundingBox;}}}',0),
 ('class T extends EntityItem implements Shadow {T(){super((World)null);} Object f(){return this.boundingBox;}} interface Shadow {Object boundingBox=null;}',0),
 ('class T {Object f(Entity e){String s="😀";return e.boundingBox;}}',1),
 ('class T {Object f(Entity e){return e.\\u0062oundingBox;}}',0),
 ('class T extends EntityItem {T(){super((World)null);} Object inner=new Object(){Object f(){return boundingBox;}};}',0),
 ('class T extends EntityItem {T(){super((World)null);} class Inner {Object f(){return T.this.boundingBox;}}}',1),
 ('class T {Object f(){return ((Entity)null).boundingBox;}}',1),
 ('class T<E extends Entity> {Object f(E e){return e.boundingBox;}}',0),
 ('class T extends EntityItem {T(){super((World)null);} Object f(){Object boundingBox=null;return boundingBox;}}',0),
 ('class T {void f(Entity e,AxisAlignedBB a){e.boundingBox=a;}}',0),
 ('class T {Object f(Entity e,AxisAlignedBB a){return e.boundingBox=a;}}',0),
 ('class T {void f(Entity e){e.boundingBox+=null;}}',0),
 ]
 paths=[]
 for i,(src,n) in enumerate(cases):
  src=re.sub(r'\bT\b','T'+str(i),src);f=d/('T'+str(i)+'.java');f.write_text(prefix+src);paths.append(f)
 rows=scan(paths)
 for i,(_,n) in enumerate(cases):assert len([r for r in rows if r[0]==str(paths[i])])==n,(i,n,[r for r in rows if r[0]==str(paths[i])])
 override=d/'OverrideCase.java';override.write_text(prefix+'''class OverrideCase extends EntityItem {OverrideCase(){super((World)null);} public AxisAlignedBB getEntityBoundingBox(){return null;} Object direct(){return this.boundingBox;} Object base(Entity e){return e.boundingBox;}}''');assert scan([override])==[], 'reachable subclass override must exclude base-typed receivers too'
 print('PASS',len(cases)+1,'actual javac ownership/read-only/shadow/Unicode and reachable-subclass override fixtures')
 native=d/'NativeAccess.java';native.write_text(prefix+'''class NativeAccess { static String trace=""; static Entity recv(Entity e){trace+="R";return e;} static Entity explode(){trace+="X";throw new IllegalStateException();} static AxisAlignedBB read(Entity e){return recv(e).boundingBox;} static AxisAlignedBB failure(){return explode().boundingBox;} }''')
 rows=scan([native]);assert len(rows)==2
 raw=native.read_bytes().decode().encode('utf-16-le')
 for r in sorted(rows,key=lambda r:int(r[1]),reverse=True):raw=raw[:int(r[1])*2]+r[3].encode('utf-16-le')+raw[int(r[2])*2:]
 native.write_bytes(raw.decode('utf-16-le').encode())
 source=root/'src/main/java/com/hbm/entity/item/EntityBoatRubber.java';s=source.read_text();methods=[]
 for name in ['getCollisionBox','getBoundingBox']:
  method=re.search(r'public AxisAlignedBB '+name+r'\([^)]*\) \{[^{}]*\}',s).group();methods.append(method)
 assert methods==['public AxisAlignedBB getCollisionBox(Entity entity) {\n\t\treturn entity.getEntityBoundingBox();\n\t}', 'public AxisAlignedBB getBoundingBox() {\n\t\treturn this.getEntityBoundingBox();\n\t}']
 prod=d/'GeneratedProduction.java';prod.write_text(prefix+'class GeneratedProduction extends EntityItem {GeneratedProduction(){super((World)null);} '+''.join(methods)+'}')
 subprocess.run([javac,'-cp',cp,'-d',str(d),str(native),str(prod),str(h/'NativeBoundsTest.java')],check=True)
 subprocess.run([java,'-cp',str(d)+os.pathsep+cp,'NativeBoundsTest',str(a.legacy_client),str(a.classpath_file)],check=True,cwd=d)
 # Original binary is isolated in a signature-free test container; class bytes unchanged.
 unsigned=d/'original-test.jar'
 with zipfile.ZipFile(a.legacy_client) as oldzip,zipfile.ZipFile(unsigned,'w') as outzip:
  for info in oldzip.infolist():
   if info.filename.upper().startswith('META-INF/') and info.filename.upper().endswith(('.SF','.RSA','.DSA')):continue
   outzip.writestr(info,oldzip.read(info.filename))
 legacy=d/'LegacyBoundsTest.java';legacy.write_text('''import java.lang.reflect.*;import sun.misc.Unsafe;
 public class LegacyBoundsTest { static String trace=""; static sa recv(sa e){trace+="R";return e;} static sa explode(){trace+="X";throw new IllegalStateException();} static azt read(sa e){return recv(e).C;} static azt failure(){return explode().C;} static azt getCollisionBox(sa entity){return entity.C;}
 public static void main(String[] a)throws Exception{Field uf=Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);Unsafe u=(Unsafe)uf.get(null);sa e=(sa)u.allocateInstance(xk.class);azt box=azt.a(-3,2,7,9,13,17);Field bf=sa.class.getField("C");bf.setAccessible(true);bf.set(e,box);if(read(e)!=box||!trace.equals("R")||getCollisionBox(e)!=box)throw new AssertionError();sa alias=e;bf.set(e,null);if(read(alias)!=null)throw new AssertionError();trace="";try{read(null);throw new AssertionError();}catch(NullPointerException ex){if(!trace.equals("R"))throw new AssertionError();}trace="";try{failure();throw new AssertionError();}catch(IllegalStateException ex){if(!trace.equals("X"))throw new AssertionError();}System.out.println("PASS 5 actual original field read, callback body, alias/null and receiver-exception oracle controls");}}
 ''')
 subprocess.run([javac,'-cp',str(a.legacy_client),'-d',str(d),str(legacy)],check=True)
 subprocess.run([java,'-cp',str(d)+os.pathsep+str(unsigned)+os.pathsep+cp,'LegacyBoundsTest'],check=True,cwd=d)
 if a.verify_attribution:
  paths=[];tmp=d/'sources'
  for source in sorted((root/'src/main/java').rglob('*.java')):
   rel=source.relative_to(root).as_posix();raw=source.read_bytes()
   if rel in guard.data['files']:raw=guard.reverse(raw,guard.data['files'][rel])
   target=tmp/rel;target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(raw);paths.append(target)
  rows=scan(paths);actual={(pathlib.Path(r[0]).relative_to(tmp).as_posix(),int(r[1]),int(r[2]),r[3],r[4]) for r in rows};expected={(rel,e['start'],e['end'],e['new'],e['owner']) for rel,v in guard.data['files'].items() for e in v['edits']};assert actual==expected,(len(actual),len(expected),actual^expected)
  print('PASS full-source re-attribution reproduces all 184 approved read edits; no writes or domain boxes selected')
print('Scope: field read identity only, not AABB mutation/arithmetic migration, complete entity compilation or gameplay; unknown external accessor overrides are not certified.')
