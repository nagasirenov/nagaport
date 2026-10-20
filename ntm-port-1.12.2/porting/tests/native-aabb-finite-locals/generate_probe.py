#!/usr/bin/env python3
"""Generate runtime expressions and local initializations verbatim from the exact source inverse."""
import json,pathlib,re,sys
import source
H=source.HERE;R=source.ROOT
m=json.loads((H/'manifest.json').read_text());assert len(m['files'])==1
direction='src/main/java/com/hbm/lib/ForgeDirection.java'
assert source.sha((R/direction).read_bytes())==m['before_all_java_sha256'][direction], 'actual direction source pin'
path,rec=next(iter(m['files'].items()));before=source.verify((R/path).read_bytes(),rec)
methods=[]
for i,e in enumerate(rec['edits']):
 args=e['before'][len('AxisAlignedBB.getBoundingBox'):]
 init=before[:e['start']];start=init.rfind('double x = ');init=init[start:init.index(';',init.rfind('double z = '))+1]
 lines=init.splitlines();assert len(lines)==3
 for axis,line in zip('xyz',lines):assert line.strip()=='double '+axis+' = this.pos.get'+axis.upper()+'() + inputSide.offset'+axis.upper()+' * reach;'
 assert any('[1D, 0.5D, 0.33D]' in r for r in e['flow'])
 methods.append('void site'+str(i)+'(ForgeDirection inputSide,double reach){\n'+init+'\ngeometry(azt.a'+args+',new AxisAlignedBB'+args+','+str(i)+');\n}')
methods.append('void ints(int x,int y,int z){this.pos=new BlockPos(x,y,z);for(ForgeDirection inputSide:ForgeDirection.values())for(double reach:new double[]{1D,0.5D,0.33D}){site0(inputSide,reach);site1(inputSide,reach);}}')
s=(H/'ProbeBase.java.in').read_text().replace('// INSERT_GENERATED','\n'.join(methods))
(pathlib.Path(sys.argv[1])/'FiniteAabbProbe.java').write_text(s)
