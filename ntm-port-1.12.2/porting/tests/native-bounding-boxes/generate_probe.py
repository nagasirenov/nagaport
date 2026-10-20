#!/usr/bin/env python3
import json,pathlib,sys
import source
H=source.HERE;R=source.ROOT;m=json.loads((H/'manifest.json').read_text());methods=[];calls={'int':[],'double':[]};i=0
for rel,rec in m['files'].items():
 source.verify((R/rel).read_bytes(),rec)
 for e in rec['edits']:
  assert list(e['variables'])==['x','y','z']
  types=set(e['variables'].values());assert len(types)==1;typ=types.pop();assert typ in calls
  old=e['before'].replace('AxisAlignedBB.getBoundingBox','azt.a',1)
  new=e['after'];fn='geometry' if typ=='int' else 'check'
  methods.append('static void site%d(%s x,%s y,%s z){%s(%s,%s,%d);}'%(i,typ,typ,typ,fn,old,new,i));calls[typ].append('site%d(x,y,z);'%i);i+=1
methods+=['static void ints(int x,int y,int z){'+''.join(calls['int'])+'}','static void doubles(double x,double y,double z){'+''.join(calls['double'])+'}']
rel='src/main/java/com/hbm/blocks/generic/DecoBlock.java';s=(R/rel).read_bytes().decode();start=s.index('switch(meta) {');depth=0
for j in range(s.index('{',start),len(s)):
 if s[j]=='{':depth+=1
 elif s[j]=='}':
  depth-=1
  if depth==0:break
body=s[start:j+1];old=body
for e in m['files'][rel]['edits']:
 assert old.count(e['after'])==1;old=old.replace(e['after'],e['before'].replace('AxisAlignedBB.getBoundingBox','azt.a'),1)
methods+=['static List<azt> decoOld(int meta,int x,int y,int z){List<azt> bbs=new ArrayList<>();'+old+' return bbs;}','static List<AxisAlignedBB> decoNew(int meta,int x,int y,int z){List<AxisAlignedBB> bbs=new ArrayList<>();'+body+' return bbs;}']
(pathlib.Path(sys.argv[1])/'BoundProbe.java').write_text((H/'ProbeBase.java.in').read_text().replace('// INSERT_GENERATED','\n'.join(methods)))
print('Generated exact 20-expression and complete DecoBlock switch oracle from verified production bytes')
