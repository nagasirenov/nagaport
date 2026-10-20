#!/usr/bin/env python3
import json,pathlib,sys
H=pathlib.Path(__file__).resolve().parent
m=json.loads((H/'manifest.json').read_text());doors=[];ints=[];i=0
for path,rec in sorted(m['files'].items()):
 for e in rec['edits']:
  args=e['before'][len('AxisAlignedBB.getBoundingBox'):]
  assert args.startswith('(') and args.endswith(')')
  line='geometry(azt.a'+args+',new AxisAlignedBB'+args+','+str(i)+'); // '+path
  (doors if 'forCollision' in args else ints).append(line);i+=1
assert len(doors)==7 and len(ints)==17
methods='void doors(boolean forCollision){\n'+'\n'.join(doors)+'\n}\n'
methods+='void ints(int x,int y,int z){this.pos=new BlockPos(x,y,z);\n'+'\n'.join(ints)+'\n}\n'
s=(H/'ProbeBase.java.in').read_text().replace('// INSERT_GENERATED',methods)
(pathlib.Path(sys.argv[1])/'OrderedAabbProbe.java').write_text(s)
