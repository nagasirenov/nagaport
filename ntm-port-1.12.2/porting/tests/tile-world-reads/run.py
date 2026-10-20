#!/usr/bin/env python3
import argparse,pathlib,tempfile,subprocess,hashlib,json,importlib.util,os
p=argparse.ArgumentParser();p.add_argument('--java-home',required=True,type=pathlib.Path);p.add_argument('--classpath-file',required=True,type=pathlib.Path);p.add_argument('--legacy-client',required=True,type=pathlib.Path);p.add_argument('--verify-attribution',action='store_true');a=p.parse_args();h=pathlib.Path(__file__).resolve().parent;root=h.parents[2];j=a.java_home/'bin';cp=a.classpath_file.read_text().strip();tools=str(a.java_home/'lib/tools.jar')
spec=importlib.util.spec_from_file_location('guard',h/'check_source.py');g=importlib.util.module_from_spec(spec);spec.loader.exec_module(g);g.main();m=g.manifest()
assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
def extract(path,signature):
 s=(root/path).read_bytes().decode();start=s.index(signature);i=s.index('{',start)+1;depth=1
 while depth:
  if s[i]=='{':depth+=1
  if s[i]=='}':depth-=1
  i+=1
 out=s[start:i];print('EXACT PRODUCTION METHOD',path,signature,hashlib.sha256(out.encode()).hexdigest(),flush=True);return out
with tempfile.TemporaryDirectory(prefix='tile-world-proof-') as td:
 d=pathlib.Path(td);subprocess.run([str(j/'javac'),'-cp',tools,'-d',str(d),str(h/'TileWorldScanner.java')],check=True)
 def scan(paths,post=False,fail=False):
  inp=d/'inputs';inp.write_text('\n'.join(map(str,paths))+'\n');r=subprocess.run([str(j/'java'),'-Xmx3g','-cp',str(d)+os.pathsep+tools,'TileWorldScanner',str(inp),str(a.classpath_file)]+(['post'] if post else []),capture_output=True,text=True)
  if fail:assert r.returncode!=0 and 'Unapproved source override' in r.stderr,r.stderr;return []
  assert r.returncode==0,r.stderr;return r.stdout.splitlines()
 prefix='import net.minecraft.tileentity.TileEntity; import net.minecraft.world.World; '
 cases=[('class T {World f(TileEntity t){return t.getWorldObj();}}',1),('class T extends TileEntity {World f(){return getWorldObj();}}',1),('class T extends TileEntity {World f(){return this.getWorldObj();}}',1),('class T extends TileEntity {World f(){return super.getWorldObj();}}',1),('class T {World f(Object t){return t.getWorldObj();}}',0),('class T {World f(Missing t){return t.getWorldObj();}}',0),('class T<U extends TileEntity> {World f(U t){return t.getWorldObj();}}',0),('class T {World f(TileEntity t){return t.getWorldObj(1);}}',0),('class T {World f(){return TileEntity.getWorldObj();}}',0),('class T extends TileEntity {static World f(){return getWorldObj();}}',0),('class T extends TileEntity {class I {World f(){return getWorldObj();}}}',0),('class T extends TileEntity {class I {World f(){return T.this.getWorldObj();}}}',1),('class T {String s="t.getWorldObj()"; /* getWorldObj() */}',0),('class T {World f(TileEntity t){String s="😀"; return t.getWorldObj();}}',1),('class T {World f(TileEntity t){return t.getWor\\u006cdObj();}}',0),('class T extends TileEntity implements I {World f(){return getWorldObj();}} interface I {World getWorldObj();}',0),('class T {World getWorldObj(){return null;} World f(){return getWorldObj();}}',0),('class T extends TileEntity {World getWorldObj(int... a){return null;} World f(){return getWorldObj();}}',0)]
 for i,(src,n) in enumerate(cases):
  f=d/'T.java';f.write_text(prefix+src);rows=scan([f]);assert len(rows)==n,(i,rows,n)
 for name in ['getWorld','getWorldObj']:
  f=d/'T.java';f.write_text(prefix+'class T extends TileEntity {public World '+name+'(){return null;}}');scan([f],fail=True)
 print('PASS',len(cases)+2,'compiler ownership/arity/static/shadow/override/unicode adversaries',flush=True)
 if a.verify_attribution:
  before=d/'before';paths=[]
  for src in (root/'src/main/java').rglob('*.java'):
   rel=src.relative_to(root).as_posix();dest=before/rel;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(g.reverse_source(rel,src.read_bytes().decode(),m).encode());paths.append(dest)
  rows=scan(paths);actual=set()
  for row in rows:
   path,lo,hi,new,receiver,old=row.split('\t');actual.add((str(pathlib.Path(path).relative_to(before)),int(lo),int(hi),new,receiver,old))
  expected={(r,e['start'],e['end'],e['new'],e['receiver'],e['old']) for r,f in m['files'].items() for e in f['edits']};assert actual==expected,(len(actual),len(expected));print('PASS full baseline attribution',len(actual),flush=True)
  rows=scan(list((root/'src/main/java').rglob('*.java')),post=True);actual=set()
  for row in rows:
   path,lo,hi,new,receiver,old=row.split('\t');actual.add((str(pathlib.Path(path).relative_to(root)),int(lo),int(hi),new,receiver,old))
  expected={(r,e['after_start'],e['after_start']+len(e['new']),e['new'],e['receiver'],e['new']) for r,f in m['files'].items() for e in f['edits']};assert expected<=actual,(len(expected-actual),list(expected-actual)[:3]);print('PASS full native destination method attribution',len(expected),flush=True)
 text=(h/'NativeWorldTest.java.in').read_text().replace('@PUMP@',extract('src/main/java/com/hbm/tileentity/machine/TileEntityAirPump.java','public World getWorld()')).replace('@RELEASE@',extract('src/main/java/com/hbm/inventory/fluid/FluidType.java','public void onFluidRelease(TileEntity te, FluidTank tank, int overflowAmount)'));f=d/'NativeWorldTest.java';f.write_text(text)
 subprocess.run([str(j/'javac'),'-source','8','-target','8','-cp',cp,'-d',str(d),str(f)],check=True)
 subprocess.run([str(j/'java'),'-cp',str(d)+os.pathsep+cp,'NativeWorldTest',str(a.legacy_client)],cwd=str(d),check=True)
