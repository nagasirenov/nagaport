#!/usr/bin/env python3
import argparse,pathlib,tempfile,subprocess,hashlib,re,importlib.util,json,os
p=argparse.ArgumentParser();p.add_argument('--java-home',required=True,type=pathlib.Path);p.add_argument('--classpath-file',required=True,type=pathlib.Path);p.add_argument('--legacy-client',required=True,type=pathlib.Path);p.add_argument('--verify-attribution',action='store_true');a=p.parse_args();h=pathlib.Path(__file__).resolve().parent;root=h.parents[2];j=a.java_home/'bin';cp=a.classpath_file.read_text().strip();tools=str(a.java_home/'lib/tools.jar')
spec=importlib.util.spec_from_file_location('guard',h/'check_source.py');guard=importlib.util.module_from_spec(spec);spec.loader.exec_module(guard);guard.main();m=guard.manifest()
assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
def extract(signature):
 path=root/'src/main/java/com/hbm/tileentity/machine/fusion/TileEntityFusionKlystron.java';s=path.read_bytes().decode();start=s.index(signature);op=s.index('{',start);depth=1;i=op+1
 while depth:
  if s[i]=='{':depth+=1
  if s[i]=='}':depth-=1
  i+=1
 result=s[start:i];print('EXACT PRODUCTION METHOD',signature,hashlib.sha256(result.encode()).hexdigest(),flush=True);return result
with tempfile.TemporaryDirectory(prefix='tile-coordinate-proof-') as td:
 d=pathlib.Path(td);subprocess.run([str(j/'javac'),'-cp',tools,'-d',str(d),str(h/'TileCoordinateScanner.java')],check=True)
 def scan(paths,fail=False):
  inp=d/'inputs';inp.write_text('\n'.join(map(str,paths))+'\n');r=subprocess.run([str(j/'java'),'-Xmx3g','-cp',str(d)+os.pathsep+tools,'TileCoordinateScanner',str(inp),str(a.classpath_file)],capture_output=True,text=True)
  if fail:assert r.returncode!=0 and 'Source getPos override' in r.stderr;return []
  assert r.returncode==0,r.stderr;return r.stdout.splitlines()
 prefix='import net.minecraft.tileentity.TileEntity; import net.minecraft.util.math.BlockPos; '
 cases=[('class T {int f(TileEntity t){return t.xCoord+t.yCoord+t.zCoord;}}',3),('class T extends TileEntity {int f(){return xCoord+this.yCoord+super.zCoord;}}',3),('class T extends TileEntity {int xCoord;int f(){return xCoord;}}',0),('class T extends TileEntity {int f(int xCoord){return xCoord+this.yCoord;}}',1),('class T extends TileEntity {private BlockPos pos;int f(){return xCoord;}}',1),('class T extends TileEntity {class Inner {int f(){return xCoord;}}}',0),('class T extends TileEntity {class Inner {int f(){return T.this.xCoord;}}}',1),('class T extends TileEntity {static int f(){return xCoord;}}',0),('class T {int f(Object t){return t.xCoord;}}',0),('class T {int f(Missing t){return t.xCoord;}}',0),('class T {void f(TileEntity t){t.xCoord=1;t.yCoord+=2;++t.zCoord;t.xCoord--; (t.yCoord)=3;}}',0),('class T {String s="t.xCoord"; /* t.yCoord */}',0),('class T {int f(){return TileEntity.xCoord;}}',0),('class T extends TileEntity {int f(){int xCoord=2;return xCoord;}}',0),('class T extends B {int f(){return this.xCoord;}} class B extends TileEntity {private int xCoord;}',0),('class T {int f(TileEntity t){String s="😀";return t.xCoord;}}',1),('class T {int f(TileEntity t){return t.\\u0078Coord;}}',0),('class T extends TileEntity implements Shadow {int f(){return this.xCoord;}} interface Shadow {int xCoord=1;}',0),('class T<U extends TileEntity>{int f(U t){return t.xCoord;}}',0)]
 for i,(src,n) in enumerate(cases):
  f=d/'T.java';f.write_text(prefix+src);rows=scan([f]);assert len(rows)==n,(i,rows,n)
 f=d/'T.java';f.write_text(prefix+'class T extends TileEntity {public BlockPos getPos(){return BlockPos.ORIGIN;} int f(){return xCoord;}}');scan([f],True)
 print('PASS',len(cases)+1,'native-owner, write, shadow, nested, override, Unicode and negative attribution fixtures',flush=True)
 if a.verify_attribution:
  before=d/'before';paths=[]
  for src in (root/'src/main/java').rglob('*.java'):
   rel=src.relative_to(root).as_posix();dest=before/rel;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(guard.reverse_source(rel,src.read_bytes().decode(),m).encode());paths.append(dest)
  rows=scan(paths);expected={(r,e['start'],e['end'],e['new'],e['receiver'],e['old']) for r,f in m['files'].items() for e in f['edits']};actual=set()
  for row in rows:
   path,start,end,new,receiver,old=row.split('\t');actual.add((pathlib.Path(path).relative_to(before).as_posix(),int(start),int(end),new,receiver,old))
  assert actual==expected,(len(actual),len(expected),list(actual-expected)[:2],list(expected-actual)[:2]);print('PASS complete original-source re-attribution',len(actual),flush=True)
 text=(h/'NativeTileTest.java.in').read_text().replace('@CONNECTIONS@',extract('public DirPos[] getConPos()')).replace('@PERMISSION@',extract('public boolean hasPermission(EntityPlayer player)'));f=d/'NativeTileTest.java';f.write_text(text)
 inputs=[root/'src/main/java/com/hbm/lib/ForgeDirection.java',root/'src/main/java/com/hbm/interfaces/Spaghetti.java']+[root/('src/main/java/com/hbm/util/fauxpointtwelve/'+x+'.java') for x in ['BlockPos','DirPos','Rotation']]
 subprocess.run([str(j/'javac'),'-source','8','-target','8','-cp',cp,'-d',str(d),str(f)]+list(map(str,inputs)),check=True)
 subprocess.run([str(j/'java'),'-cp',str(d)+os.pathsep+cp,'NativeTileTest',str(a.legacy_client),cp],cwd=str(d),check=True)
