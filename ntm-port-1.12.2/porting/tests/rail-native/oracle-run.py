#!/usr/bin/env python3
"""Run source-extracted original rail geometry vs real native RenderRail.bake."""
import argparse, hashlib, importlib.util, os, subprocess, tempfile
from pathlib import Path
import sys
sys.dont_write_bytecode = True
HERE=Path(__file__).resolve().parent
parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--original-root',type=Path,required=True)
parser.add_argument('--native-root',type=Path,default=HERE.parents[2])
parser.add_argument('--java-home',type=Path,required=True)
parser.add_argument('--classpath-file',type=Path,required=True)
args=parser.parse_args()
spec=importlib.util.spec_from_file_location('rail_oracle_build',HERE/'oracle-build.py');oracle=importlib.util.module_from_spec(spec);spec.loader.exec_module(oracle)
manifest=oracle.verify(args.original_root,args.native_root)
print(f'PASS PINNED ORIGINAL INPUTS {len(manifest["sha256"])}; BYTE-IDENTICAL NATIVE ASSETS {len(manifest["assets"])}',flush=True)
cp=args.classpath_file.read_text().strip();prod=['com/hbm/render/block/RenderRail.java','com/hbm/render/block/nativeblock/AtlasObjGeometry.java','com/hbm/render/block/nativeblock/BlockPositionLightQuad.java','com/hbm/render/mesh/ObjMesh.java','com/hbm/render/mesh/ObjMeshParser.java']
with tempfile.TemporaryDirectory(prefix='rail-original-oracle-') as tmp:
 out=Path(tmp);files=[]
 for rel in prod:
  path=args.native_root/'src/main/java'/rel;files.append(str(path));print('PRODUCTION',rel,hashlib.sha256(path.read_bytes()).hexdigest(),flush=True)
 for name,text in [('OracleRailOriginal',oracle.generate(args.original_root,args.native_root)),('OracleRailNativeTest',(HERE/'oracle-native-test.java.in').read_text())]:
  path=out/(name+'.java');path.write_text(text);files.append(str(path))
 subprocess.run([str(args.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-cp',cp,'-d',tmp]+files,check=True)
 subprocess.run([str(args.java_home/'bin/java'),'-Djava.awt.headless=true','-Xmx768m','-cp',os.pathsep.join([tmp,cp]),'OracleRailNativeTest',str((args.native_root/'src/main/resources').resolve()),str((args.original_root/'src/main/resources').resolve()),str(HERE/'oracle-original-manifest.json')],cwd=tmp,check=True)
