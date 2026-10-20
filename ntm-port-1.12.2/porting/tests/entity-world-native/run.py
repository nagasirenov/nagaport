#!/usr/bin/env python3
import argparse,hashlib,importlib.util,json,os,pathlib,re,subprocess,tempfile
p=argparse.ArgumentParser();p.add_argument('--java-home',type=pathlib.Path,required=True);p.add_argument('--classpath-file',type=pathlib.Path,required=True);p.add_argument('--legacy-client',type=pathlib.Path,required=True);p.add_argument('--legacy-srg',type=pathlib.Path,required=True);p.add_argument('--verify-attribution',action='store_true');a=p.parse_args();h=pathlib.Path(__file__).resolve().parent;root=h.parents[2]
assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
import zipfile
assert 'FD: sa/o net/minecraft/entity/Entity/field_70170_p' in zipfile.ZipFile(a.legacy_srg).read('joined.srg').decode()
cp=a.classpath_file.read_text().strip();java=str(a.java_home/'bin/java');javac=str(a.java_home/'bin/javac');tools=str(a.java_home/'lib/tools.jar')
spec=importlib.util.spec_from_file_location('guard',h/'check_source.py');guard=importlib.util.module_from_spec(spec);spec.loader.exec_module(guard);guard.main()
with tempfile.TemporaryDirectory(prefix='entity-world-proof-') as td:
 d=pathlib.Path(td);subprocess.run([javac,'-cp',tools,'-d',str(d),str(h/'EntityWorldScanner.java')],check=True)
 def scan(paths):
  inp=d/'inputs';inp.write_text('\n'.join(map(str,paths))+'\n');return subprocess.check_output([java,'-Xmx3g','-cp',str(d)+os.pathsep+tools,'EntityWorldScanner',str(inp),str(a.classpath_file)],text=True).splitlines()
 prefix='import net.minecraft.entity.Entity; import net.minecraft.entity.item.EntityItem; import net.minecraft.world.World; import net.minecraft.tileentity.TileEntity; '
 cases=[
 ('class T {Object f(Entity e){return e.worldObj;}}',1),
 ('class T {void f(Entity e,World w){e.worldObj=w;}}',1),
 ('class T extends EntityItem {T(){super((World)null);} Object f(){return worldObj;}}',1),
 ('class T extends EntityItem {T(){super((World)null);} Object f(World world){return worldObj;}}',1),
 ('class T extends EntityItem {T(){super((World)null);} Object f(){return this.worldObj;}}',1),
 ('class T extends EntityItem {T(){super((World)null);} Object f(){return super.worldObj;}}',1),
 ('class T {Object f(TileEntity t){return t.worldObj;}}',0),
 ('class T {Object worldObj; Object f(T t){return t.worldObj;}}',0),
 ('class T extends EntityItem {Object worldObj; T(){super((World)null);} Object f(){return this.worldObj;}}',0),
 ('class T extends EntityItem {Object world; T(){super((World)null);} Object f(){return this.worldObj;}}',0),
 ('class T extends EntityItem {T(){super((World)null);} Object f(Object worldObj){return worldObj;}}',0),
 ('class T {Object f(Object e){return e.worldObj;}}',0),
 ('class T {Object f(Missing e){return e.worldObj;}}',0),
 ('class T {Object f(Entity e){return e.world;} String s="worldObj"; /* e.worldObj */}',0),
 ('class T {Object f(Entity e){return ((Entity)e).worldObj;}}',1),
 ('class T {Object f(){return Entity.worldObj;}}',0),
 ('class T extends EntityItem {T(){super((World)null);} static Object f(){return worldObj;}}',0),
 ('class T extends EntityItem {T(){super((World)null);} class Inner {Object f(){return worldObj;}}}',0),
 ('class T extends EntityItem implements Shadow {T(){super((World)null);} Object f(){return this.worldObj;}} interface Shadow {Object worldObj=null;}',0),
 ('class T extends EntityItem implements Shadow2 {T(){super((World)null);} Object f(){return this.worldObj;}} interface Shadow2 {Object world=null;}',0),
 ('class T {Object f(Entity e){String s="😀";return e.worldObj;}}',1),
 ('class T {Object f(Entity e){return e.\\u0077orldObj;}}',0),
 ('class T extends TileEntity {Object f(){return worldObj;}}',0),
 ('class T extends EntityItem {T(){super((World)null);} Object inner=new Object(){Object f(){return worldObj;}};}',0),
 ('class T extends EntityItem {T(){super((World)null);} class Inner {Object f(){return T.this.worldObj;}}}',1),
 ('class T {Object f(){return ((Entity)null).worldObj;}}',1),
 ('class T<E extends Entity> {Object f(E e){return e.worldObj;}}',0),
 ('class T extends EntityItem {T(){super((World)null);} Object f(){Object worldObj=null;return worldObj;}}',0),
 ]
 paths=[]
 for i,(src,n) in enumerate(cases):
  src=re.sub(r'\bT\b','T'+str(i),src);f=d/('T'+str(i)+'.java');f.write_text(prefix+src);paths.append(f)
 rows=[r.split('\t') for r in scan(paths)]
 for i,(src,n) in enumerate(cases):
  selected=[r for r in rows if r[0]==str(paths[i])];assert len(selected)==n,(i,n,selected)
  for r in selected:assert paths[i].read_text().encode('utf-16-le')[int(r[1])*2:int(r[2])*2].decode('utf-16-le')=='worldObj'
 print('PASS',len(cases),'actual javac attribution receiver, shadow, interface, TileEntity, static, Unicode and negative fixtures')
 native=d/'NativeAccess.java';native.write_text(prefix+'''class NativeAccess {
 static String trace=""; static Entity recv(Entity e){trace+="R";return e;} static World rhs(World w){trace+="W";return w;}
 static World explode(){trace+="X";throw new IllegalStateException();}
 static World assign(Entity e,World w){return recv(e).worldObj=rhs(w);}
 static World read(Entity e){return recv(e).worldObj;}
 static void throwing(Entity e){recv(e).worldObj=explode();}
}''')
 # Default-package fixture cannot share a loader with signed default-package engine classes.
 # Only signature metadata is omitted in the temporary test container; every class byte is identical.
 unsigned=d/'legacy-test-container.jar'
 with zipfile.ZipFile(a.legacy_client) as oldzip,zipfile.ZipFile(unsigned,'w') as outzip:
  for info in oldzip.infolist():
   if info.filename.upper().startswith('META-INF/') and info.filename.upper().endswith(('.SF','.RSA','.DSA')):continue
   outzip.writestr(info,oldzip.read(info.filename))
 legacy=d/'LegacyAccess.java'; legacy.write_text(native.read_text()[len(prefix):].replace('NativeAccess','LegacyAccess').replace('Entity','sa').replace('World','ahb').replace('worldObj','o'))
 subprocess.run([javac,'-cp',str(a.legacy_client),'-d',str(d),str(legacy)],check=True)
 legacytest=d/'LegacyFieldTest.java';legacytest.write_text('''import java.lang.reflect.*; import sun.misc.Unsafe;
 public class LegacyFieldTest { public static void main(String[] args)throws Exception {
 Field f=Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);Unsafe u=(Unsafe)f.get(null);
 sa e=(sa)u.allocateInstance(xk.class);ahb w=(ahb)u.allocateInstance(mt.class);
 if(LegacyAccess.assign(e,w)!=w||e.o!=w||!LegacyAccess.trace.equals("RW"))throw new AssertionError();
 LegacyAccess.trace="";if(LegacyAccess.read(e)!=w||!LegacyAccess.trace.equals("R"))throw new AssertionError();
 sa alias=e;if(LegacyAccess.assign(alias,null)!=null||e.o!=null)throw new AssertionError();
 LegacyAccess.trace="";try{LegacyAccess.assign(null,w);throw new AssertionError();}catch(NullPointerException expected){if(!LegacyAccess.trace.equals("RW"))throw new AssertionError();}
 LegacyAccess.trace="";try{LegacyAccess.read(null);throw new AssertionError();}catch(NullPointerException expected){if(!LegacyAccess.trace.equals("R"))throw new AssertionError();}
 LegacyAccess.trace="";try{LegacyAccess.throwing(null);throw new AssertionError();}catch(IllegalStateException expected){if(!LegacyAccess.trace.equals("RX"))throw new AssertionError();}
 System.out.println("PASS 6 actual original Entity field evaluation order, alias, null and RHS-failure controls");
 }}''')
 subprocess.run([javac,'-cp',str(d)+os.pathsep+str(a.legacy_client),'-d',str(d),str(legacytest)],check=True)
 subprocess.run([java,'-cp',str(d)+os.pathsep+str(unsigned)+os.pathsep+cp,'LegacyFieldTest'],check=True,cwd=d)
 rows=[r.split('\t') for r in scan([native])];assert len(rows)==3
 raw=native.read_bytes().decode().encode('utf-16-le')
 for r in sorted(rows,key=lambda r:int(r[1]),reverse=True):raw=raw[:int(r[1])*2]+r[3].encode('utf-16-le')+raw[int(r[2])*2:]
 native.write_bytes(raw.decode('utf-16-le').encode())
 source=root/'src/main/java/com/hbm/entity/mob/botprime/EntityWormBaseNT.java'
 method=re.search(r'public Entity getHead\(\) \{[^{}]*\}',source.read_text()).group()
 assert method=='public Entity getHead() {\n\t\treturn this.world.getEntityByID(this.headID);\n\t}'
 prod=d/'GeneratedProduction.java';prod.write_text(prefix+'class GeneratedProduction extends EntityItem {int headID; GeneratedProduction(){super((World)null);} '+method+'}')
 subprocess.run([javac,'-cp',cp,'-d',str(d),str(native),str(prod),str(h/'NativeWorldTest.java')],check=True)
 subprocess.run([java,'-cp',str(d)+os.pathsep+cp,'NativeWorldTest',str(a.legacy_client)],check=True,cwd=d)
 bytecode=subprocess.check_output([str(a.java_home/'bin/javap'),'-c','-private','-classpath',str(d)+os.pathsep+cp,'NativeAccess','GeneratedProduction'],text=True)
 assert bytecode.count('Field net/minecraft/entity/Entity.world:Lnet/minecraft/world/World;')==3
 assert 'getfield' in bytecode and 'putfield' in bytecode
 print('PASS compiled native bytecode uses direct Entity.world getfield/putfield; no accessor/helper substitution')
 if a.verify_attribution:
  paths=[];tmp=d/'sources'
  for source in sorted((root/'src/main/java').rglob('*.java')):
   rel=source.relative_to(root).as_posix();raw=source.read_bytes()
   if rel in guard.data['files']:raw=guard.reverse(raw,guard.data['files'][rel])
   dest=tmp/rel;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(raw);paths.append(dest)
  rows=scan(paths);normalized=['\t'.join([pathlib.Path(r.split('\t')[0]).relative_to(tmp).as_posix()]+r.split('\t')[1:]) for r in rows]
  assert sorted(normalized)==sorted((h/'attributed-accesses.tsv').read_text().splitlines())
  print('PASS complete reconstructed CP52 source attribution selects exactly',len(rows),'manifest edits')
