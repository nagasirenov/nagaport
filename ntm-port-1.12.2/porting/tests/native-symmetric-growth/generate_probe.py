#!/usr/bin/env python3
import json,pathlib,sys
import source
H=source.H;R=source.R;m=json.loads((H/'manifest.json').read_text());methods=[];calls=[];centers=[];i=0
for rel,rec in m['files'].items():
 source.inverse((R/rel).read_bytes(),rec)
 for e in rec['edits']:
  args=', '.join(e['arguments']);methods.append('static void site%d(AxisAlignedBB b){azt original=old(neo(b));geometry(original.b(%s),b.grow(%s),%d);}'%(i,args,args,i));calls.append('site%d(b);'%i)
  if e['receiver'].startswith('new AxisAlignedBB('):
   recv=e['receiver'];old=e['original']['receiver'].replace('AxisAlignedBB.getBoundingBox','azt.a',1);centers.append('geometry(%s.b(%s),%s.grow(%s),%d);'%(old,args,recv,args,i))
  i+=1
assert i==32 and len(centers)==2
methods+=['static void sites(AxisAlignedBB b){'+''.join(calls)+'}','static void centers(int x,int y,int z){'+''.join(centers)+'}']
(pathlib.Path(sys.argv[1])/'GrowthProbe.java').write_text((H/'ProbeBase.java.in').read_text().replace('// INSERT_GENERATED','\n'.join(methods)))
print('Generated 32 exact production argument slices plus 2 complete original/native constructor-growth expressions')
