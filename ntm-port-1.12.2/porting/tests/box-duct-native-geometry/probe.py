#!/usr/bin/env python3
"""Pinned pristine boxed-duct callback/icon recorder + actual original client faces versus native production geometry."""
import argparse,hashlib,json,os,re,subprocess,tempfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);p.add_argument('--legacy-client',type=Path,required=True);p.add_argument('--pristine',type=Path,default=Path('/workspace/shared/nagaport/ntm-original-pristine'));a=p.parse_args()
t=Path(__file__).resolve().parent;r=t.parents[2];s=r/'src/main/java'
legacy_sha='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce';assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()==legacy_sha

def method(text,name):
 m=re.search(r'public\s+(?:static\s+)?[\w<>]+\s+'+name+r'\([^)]*\)\s*\{',text);assert m,name
 e=m.end();depth=1
 while depth:depth+=(text[e]=='{')-(text[e]=='}');e+=1
 return text[m.start():e]

sources={};manifest={'legacy_client_sha256':legacy_sha,'sources':{},'assets':{}}
for name,folder in [('RenderBoxDuct','render/block'),('RenderBlocksNT','render/util'),('FluidDuctBox','blocks/network'),('PowerCableBox','blocks/network'),('FluidDuctBoxExhaust','blocks/network'),('ColorUtil','util')]:
 rel='src/main/java/com/hbm/'+folder+'/'+name+'.java';data=(a.pristine/rel).read_bytes();manifest['sources'][rel]=hashlib.sha256(data).hexdigest();sources[name]=data.decode()
for old in sorted((a.pristine/'src/main/resources/assets/hbm/textures/blocks').glob('boxduct_*')):
 rel=old.relative_to(a.pristine);data=old.read_bytes();assert (r/rel).read_bytes()==data,'Changed original texture '+str(rel);manifest['assets'][str(rel)]=hashlib.sha256(data).hexdigest()
mp=t/'source-manifest.json'
if mp.exists():assert json.loads(mp.read_text())==manifest,'Pinned pristine source/asset mismatch'
else:mp.write_text(json.dumps(manifest,indent=2,sort_keys=True)+'\n')
oracle=(t/'OriginalBoxDuctOracle.java.in').read_text()
replace={'RENDER':'\n'.join(method(sources['RenderBoxDuct'],n) for n in ['renderInventoryBlock','renderWorldBlock']), 'FLUID_ICONS':method(sources['FluidDuctBox'],'getIcon'),'CABLE_ICONS':method(sources['PowerCableBox'],'getIcon'),'FLUID_REGISTER':method(sources['FluidDuctBox'],'registerBlockIcons'),'CABLE_REGISTER':method(sources['PowerCableBox'],'registerBlockIcons'),'EXHAUST_REGISTER':method(sources['FluidDuctBoxExhaust'],'registerBlockIcons'),'NT_FACES':'\n'.join(method(sources['RenderBlocksNT'],n) for n in ['renderFaceZNeg','renderFaceXPos']),'LIGHTEN':re.sub(r'\bColor\b','java.awt.Color',method(sources['ColorUtil'],'lightenColor'))}
# Extracted methods retain their source bodies. No native production formula feeds the oracle.
for marker,body in replace.items():oracle=oracle.replace('/* '+marker+' */',body)
production=['com/hbm/render/block/RenderBoxDuct.java','com/hbm/render/block/nativeblock/BoxDuctMaterials.java','com/hbm/render/block/nativeblock/BoxDuctBakedModel.java','com/hbm/render/block/nativeblock/PneumaticCuboidGeometry.java','com/hbm/render/block/nativeblock/ThermalModelTransforms.java','com/hbm/render/item/nativeitem/ItemTransformMatrix.java','com/hbm/blocks/network/FluidConnectionState.java','com/hbm/blocks/network/ConnectionState.java','com/hbm/lib/ForgeDirection.java']
# Only the unrelated NTM Library owner is controlled; complete production ConnectionState compiles unchanged.
library='package com.hbm.lib;public final class Library {public static final ForgeDirection POS_X=ForgeDirection.EAST,NEG_X=ForgeDirection.WEST,POS_Y=ForgeDirection.UP,NEG_Y=ForgeDirection.DOWN,POS_Z=ForgeDirection.SOUTH,NEG_Z=ForgeDirection.NORTH;public static boolean canConnect(net.minecraft.world.IBlockAccess w,int x,int y,int z,ForgeDirection d){throw new AssertionError("Geometry scope must not route live networks");}}'
with tempfile.TemporaryDirectory(prefix='box-duct-geometry-') as tmp:
 out=Path(tmp);files=[]
 for name,body in [('OriginalBoxDuctOracle.java',oracle),('BoxDuctGeometryNativeTest.java',(t/'BoxDuctGeometryNativeTest.java.in').read_text()),('com/hbm/lib/Library.java',library)]:
  f=out/name;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(body);files.append(str(f))
 for rel in production:
  f=s/rel;print('PRODUCTION',rel,hashlib.sha256(f.read_bytes()).hexdigest(),flush=True);files.append(str(f))
 subprocess.run([str(a.java_home/'bin/javac'),'-source','8','-target','8','-encoding','UTF-8','-cp',a.classpath,'-d',tmp]+files,check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Xmx1g','-Djava.awt.headless=true','-cp',tmp+os.pathsep+a.classpath,'BoxDuctGeometryNativeTest',str(a.legacy_client)],cwd=tmp,check=True)
