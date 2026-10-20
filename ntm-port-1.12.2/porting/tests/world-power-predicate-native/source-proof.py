#!/usr/bin/env python3
"""Receiver-aware parse/scope proof, exact byte reversals and hash-pinned prior engine audit."""
import argparse,collections,hashlib,json,subprocess,tempfile,zipfile
from pathlib import Path
H=Path(__file__).resolve().parent;M=H.parents[2];S=M/'src/main/java'
def sha(b):return hashlib.sha256(b).hexdigest()
def inventory(source,java_home):
 with tempfile.TemporaryDirectory(prefix='power-source-') as td:
  t=Path(td);tools=java_home/'lib/tools.jar'
  subprocess.run([str(java_home/'bin/javac'),'-cp',str(tools),'-d',str(t),str(H/'SourceInventory.java')],check=True,capture_output=True)
  return json.loads(subprocess.check_output([str(java_home/'bin/java'),'-Xmx400m','-cp',str(t)+':'+str(tools),'SourceInventory',str(source)],text=True))
def resolve(call,classes):
 r=call['receiver']
 if r=='this.world':
  owner=call['owner'];chain=[]
  while owner in classes:
   a=classes[owner]
   if any(f['name']=='world' for f in a['fields']):return None
   if owner in chain:raise AssertionError('cyclic hierarchy')
   chain.append(owner);owner=a['parent']
  if owner!='net.minecraft.tileentity.TileEntity':return None
  return {'kind':'inherited_native_field','chain':chain,'terminal':owner,'field':'protected net.minecraft.world.World world'}
 v=call['locals'].get(r)
 if not v or v['type']!='net.minecraft.world.World':return None
 return {'kind':'lexical_binding','name':r,'declaration':v}
def controls(java_home):
 with tempfile.TemporaryDirectory(prefix='power-controls-') as td:
  t=Path(td);(t/'Cases.java').write_text('''package control;import net.minecraft.world.World;import net.minecraft.tileentity.TileEntity;
class Other { boolean isBlockIndirectlyGettingPowered(int x,int y,int z){return false;} }
class Cases extends TileEntity {
 boolean parameter(World world,int x,int y,int z){return world.isBlockIndirectlyGettingPowered(x,y,z);}
 boolean custom(Other world){return world.isBlockIndirectlyGettingPowered(1,2,3);}
 boolean inherited(World world){return this.world.isBlockIndirectlyGettingPowered(1,2,3);}
 Object lambda=()->{World world=null;return world.isBlockIndirectlyGettingPowered(1,2,3);};
 // this.world.isBlockIndirectlyGettingPowered(1,2,3)
 String comment="world.isBlockIndirectlyGettingPowered(1,2,3)";
 class Shadow extends TileEntity {Other world;boolean query(){return this.world.isBlockIndirectlyGettingPowered(1,2,3);}}
 Object anonymous=new TileEntity(){boolean query(){return this.world.isBlockIndirectlyGettingPowered(1,2,3);}};
}''');d=inventory(t,java_home);classes={x['owner']:x for x in d['classes']};assert len(d['calls'])==6;resolved=[resolve(c,classes) for c in d['calls']];assert [bool(v) for v in resolved]==[True,False,True,True,False,True];assert len(d['declarations'])==1
 print('PASS parser controls: typed parameter/local lambda, custom receiver rejection, inherited field, shadow field rejection, anonymous owner, comments/strings and custom declaration detection')
def audit(java_home=None):
 d=json.loads((H/'selection.json').read_text());assert d['baseline']=='ea933e28fe3a1a9a60e9d438411a02182b2ab22a';assert sha((H/'baseline.zip').read_bytes())==d['baseline_zip_sha256'];by=collections.defaultdict(list)
 for op in d['operations']:by[op['path']].append(op)
 assert len(d['operations'])==58 and len(by)==47
 with zipfile.ZipFile(H/'baseline.zip') as z:
  for path in z.namelist():
   before=z.read(path);assert sha(before)==d['baseline_files_sha256'][path];expected=before.decode()
   for op in sorted(by[path],key=lambda x:x['start'],reverse=True):
    assert expected[op['start']:op['end']]==op['old'];r=op['receiver'];args=op['old'].split('isBlockIndirectlyGettingPowered(',1)[1][:-1];assert op['new']==r+'.isBlockPowered(new net.minecraft.util.math.BlockPos('+args+'))';assert len(op['arguments'])==3;expected=expected[:op['start']]+op['new']+expected[op['end']:]
   actual=(S/path).read_bytes();assert expected.encode()==actual,('unselected change',path)
   inverse=actual.decode();shift=0
   for op in sorted(by[path],key=lambda x:x['start']):
    start=op['start']+shift;assert inverse[start:start+len(op['new'])]==op['new'];inverse=inverse[:start]+op['old']+inverse[start+len(op['new']):]
   assert inverse.encode()==before,('inverse',path)
 for path,expected in d['reused_proof_hashes'].items():assert sha((M/path).read_bytes())==expected,('prior proof changed',path)
 report={'calls':58,'files':47,'baseline':d['baseline'],'receiver_kinds':dict(collections.Counter(o['resolution']['kind'] for o in d['operations'])),'excluded_comments':1,'custom_declarations':0}
 if java_home:
  inv=inventory(S,java_home);classes={x['owner']:x for x in inv['classes']};assert not inv['declarations'];assert not any('.isBlockIndirectlyGettingPowered(' in c['source'] for c in inv['calls']);assert not any(c['owner'].endswith('.World') for c in inv['classes'])
  rows=[]
  for path,ops in by.items():
   actualcalls=[c for c in inv['calls'] if c['path']==path];cursor=0
   for op in ops:
    found=[(i,c) for i,c in enumerate(actualcalls) if i>=cursor and c['source']==op['new']];assert found,(path,op['new']);i,c=found[0];cursor=i+1;r=resolve(c,classes);assert r and r['kind']==op['resolution']['kind'];assert c['owner']==op['owner']
    if r['kind']=='lexical_binding':assert r['name']==op['resolution']['name'] and r['declaration']['source']==op['resolution']['declaration']['source'] and r['declaration']['type']==op['resolution']['declaration']['type']
    else:assert r==op['resolution']
    rows.append({'path':path,'line':c['line'],'owner':c['owner'],'new':c['source'],'resolution':r,'locals':c['locals']})
  report['post_migration_attribution']=rows;controls(java_home)
 print('PASS 58 exact call-expression replacements in 47 files; 53 exact whole-file inverses/closure files; prior native/original dispatcher proof hashes verified')
 return report
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path);p.add_argument('--report',type=Path);a=p.parse_args();r=audit(a.java_home)
 if a.report:a.report.write_text(json.dumps(r,indent=2)+'\n')
