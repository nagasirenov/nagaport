#!/usr/bin/env python3
"""Sensitivity proof: intentional native defects must fail the independent oracle.
All mutations occur in a temporary directory. Production files are never changed.
"""
import argparse, importlib.util, os, subprocess, tempfile
from pathlib import Path
import sys
sys.dont_write_bytecode = True
HERE=Path(__file__).resolve().parent
p=argparse.ArgumentParser(description=__doc__)
p.add_argument('--original-root',type=Path,required=True)
p.add_argument('--native-root',type=Path,default=HERE.parents[2])
p.add_argument('--java-home',type=Path,required=True)
p.add_argument('--classpath-file',type=Path,required=True)
a=p.parse_args()
spec=importlib.util.spec_from_file_location('rail_oracle_build',HERE/'oracle-build.py');oracle=importlib.util.module_from_spec(spec);spec.loader.exec_module(oracle)
oracle.verify(a.original_root,a.native_root)
cp=a.classpath_file.read_text().strip()
renderer='com/hbm/render/block/RenderRail.java';geometry='com/hbm/render/block/nativeblock/AtlasObjGeometry.java'
production=[renderer,geometry,'com/hbm/render/block/nativeblock/BlockPositionLightQuad.java','com/hbm/render/mesh/ObjMesh.java','com/hbm/render/mesh/ObjMeshParser.java']
mutations=[
 ('narrow-yaw',renderer,'if(metadata == 12) yaw = 90F / 180F','if(metadata == 12) yaw = 180F / 180F','exact original XYZ'),
 ('dummy-visibility',renderer,'if(!inventory && kind != Kind.NARROW_STRAIGHT','if(false && !inventory && kind != Kind.NARROW_STRAIGHT','vertex count'),
 ('inventory-matrix',renderer,'translate(2.5, -.0625, -1.5)','translate(2.6, -.0625, -1.5)','exact original XYZ'),
 ('sign-selection',renderer,'sign == 2 ? "SignTurn" : "SignStraight"','"SignStraight"','exact source UV'),
 ('funnel-material',renderer,'mesh, "Top", sprites[0]','mesh, "Top", sprites[1]','source material/part order'),
 ('whole-shader',geometry,'(ny + .7F) * .9F','(ny + .7F) * .8F','exact whole/part color'),
 ('triangle-duplication',geometry,'if(i % 3 == 2 && (whole || face.corners.size() == 3))','if(false)','vertex count'),
 ('inventory-normal',renderer,'if(source.getFormat().hasNormal())','if(false && source.getFormat().hasNormal())','transformed original packed normal'),
]
with tempfile.TemporaryDirectory(prefix='rail-oracle-mutations-') as tmp:
 root=Path(tmp);base=root/'baseline';base.mkdir();files=[]
 for rel in production:files.append(str(a.native_root/'src/main/java'/rel))
 for name,content in [('OracleRailOriginal',oracle.generate(a.original_root,a.native_root)),('OracleRailNativeTest',(HERE/'oracle-native-test.java.in').read_text())]:
  path=base/(name+'.java');path.write_text(content);files.append(str(path))
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-cp',cp,'-d',str(base)]+files,check=True)
 for name,rel,old,new,expected in mutations:
  source=(a.native_root/'src/main/java'/rel).read_text();assert source.count(old)==1,(name,'mutation target changed')
  out=root/name;path=out/rel;path.parent.mkdir(parents=True);path.write_text(source.replace(old,new))
  subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-cp',os.pathsep.join([str(base),cp]),'-d',str(out),str(path)],check=True)
  run=subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-Xmx768m','-cp',os.pathsep.join([str(out),str(base),cp]),'OracleRailNativeTest',str((a.native_root/'src/main/resources').resolve()),str((a.original_root/'src/main/resources').resolve()),str(HERE/'oracle-original-manifest.json')],cwd=root,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
  assert run.returncode!=0 and 'java.lang.AssertionError' in run.stdout and expected in run.stdout,(name,run.returncode,run.stdout)
  failure=next(line.strip() for line in run.stdout.splitlines() if 'java.lang.AssertionError' in line)
  print('PASS rejected',name+':',failure,flush=True)
print('PASS all eight deliberate native geometry/material/normal/shading mutations were rejected; no production files changed.',flush=True)
