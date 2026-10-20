#!/usr/bin/env python3
"""Native RBMK models plus exact extracted owner render hooks, not complete reactor compilation."""
import argparse,hashlib,json,os,re,subprocess,tempfile,zipfile
from pathlib import Path
import build_oracle
p=argparse.ArgumentParser();p.add_argument('--module',type=Path,required=True);p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);p.add_argument('--legacy-client',type=Path,required=True);p.add_argument('--original-root',type=Path);a=p.parse_args();a.module=a.module.resolve();here=Path(__file__).resolve().parent;src=a.module/'src/main/java';res=a.module/'src/main/resources';exe='.exe' if os.name=='nt' else ''
production=['com/hbm/blocks/machine/rbmk/RBMKRenderState.java','com/hbm/main/RBMKModelHandler.java','com/hbm/lib/RefStrings.java','com/hbm/render/mesh/ObjMesh.java','com/hbm/render/mesh/ObjMeshParser.java']
production+=['com/hbm/render/block/RenderRBMK'+x+'.java' for x in ['Rod','Control','Reflector']]
production+=['com/hbm/render/block/nativeblock/'+x+'.java' for x in ['RBMKMaterials','RBMKGeometry','RBMKContextQuad','RBMKBakedModel','ConnectionQuadFactory','BlockPositionLightQuad','ConveyorCuboidGeometry','ThermalModelTransforms']]
base=(src/'com/hbm/blocks/machine/rbmk/RBMKBase.java').read_text()
def extract(name):
 m=re.search(r'(?:public|protected) (?:static )?[\w<>]+ '+name+r'\([^)]*\)\s*\{',base);assert m,name
 end=m.end();depth=1
 while depth:depth+=(base[end]=='{')-(base[end]=='}');end+=1
 return base[m.start():end]
methods='\n'.join(extract(n) for n in ['createBlockState','getRenderType','getRenderLayer','getExtendedState','metaToLid'])
fixture='''package com.hbm.blocks.machine.rbmk;
import net.minecraft.block.Block; import net.minecraft.block.material.Material;
import net.minecraft.block.state.*; import net.minecraft.block.properties.*; import net.minecraft.util.*;
import net.minecraft.world.IBlockAccess; import net.minecraftforge.common.property.*;
public class RBMKOwnerFixture extends Block {
 public static final PropertyInteger METADATA=PropertyInteger.create("metadata",0,15);
 public static final int LID_NONE=0,LID_STANDARD=1,LID_GLASS=2;
 public static final EnumFacing DIR_NORMAL_LID=EnumFacing.EAST,DIR_GLASS_LID=EnumFacing.SOUTH;
 public Integer core; public int coreCalls;
 public RBMKOwnerFixture(){super(Material.IRON);setDefaultState(blockState.getBaseState().withProperty(METADATA,0));}
 public boolean hasOwnLid(){RBMKRenderState.Profile p=RBMKRenderState.profile(this);return p!=null&&p.ownLid;}
 public int[] findCore(IBlockAccess w,int x,int y,int z){coreCalls++;return core==null?null:new int[]{x,y-8,z};}
 @Override public IBlockState getStateFromMeta(int m){return getDefaultState().withProperty(METADATA,m&15);}
 @Override public int getMetaFromState(IBlockState s){return s.getValue(METADATA);}
'''+methods+'\n}\n'
manifest=json.loads((here/'source-manifest.json').read_text())
for path,want in manifest['files'].items():
 if '/resources/' in path:assert hashlib.sha256((a.module/path).read_bytes()).hexdigest()==want,'Original RBMK model changed: '+path
with tempfile.TemporaryDirectory(prefix='rbmk-native-') as d:
 out=Path(d);files=[]
 def add(rel,content):
  f=out/rel;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(content);files.append(str(f))
 add('com/hbm/blocks/machine/rbmk/RBMKOwnerFixture.java',fixture)
 classes=['RBMKRod','RBMKRodReaSim','RBMKControl','RBMKControlAuto','RBMKBoiler','RBMKHeater','RBMKBlank','RBMKReflector','RBMKAbsorber','RBMKModerator','RBMKOutgasser','RBMKStorage','RBMKCooler']
 for name in classes:add('com/hbm/blocks/machine/rbmk/'+name+'.java','package com.hbm.blocks.machine.rbmk;public class '+name+' extends RBMKOwnerFixture {}\n')
 for path in production:
  f=src/path;print('COMPLETE PRODUCTION',path,hashlib.sha256(f.read_bytes()).hexdigest(),flush=True);files.append(str(f))
 print('EXTRACTED OWNER HOOKS',hashlib.sha256(methods.encode()).hexdigest(),flush=True)
 add('OriginalRBMKOracle.java',build_oracle.generate(a.legacy_client,a.original_root));files.append(str(here/'RBMKNativeTest.java'))
 subprocess.run([str(a.java_home/'bin'/('javac'+exe)),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',d,'-cp',a.classpath,'-d',d]+files,check=True)
 bytecode=subprocess.run([str(a.java_home/'bin'/('javap'+exe)),'-c','-p','-classpath',d]+[x[:-5].replace('/','.') for x in production]+['com.hbm.blocks.machine.rbmk.RBMKOwnerFixture'],capture_output=True,text=True,check=True).stdout
 for forbidden in ['ISimpleBlockRenderingHandler','net/minecraft/client/renderer/RenderBlocks','getNextAvailableRenderId','getBlockMetadata:']:
  assert forbidden not in bytecode,'Legacy production rendering reference '+forbidden
 print('PASS complete native model and extracted owner-render bytecode contains no removed renderer APIs',flush=True)
 jar=out/'rbmk-resources.jar'
 with zipfile.ZipFile(jar,'w') as z:
  for folder in ['assets/hbm/models/rbmk','assets/hbm/textures/blocks/rbmk']:
   for f in (res/folder).glob('*'):
    if f.is_file():z.write(f,f.relative_to(res).as_posix())
 subprocess.run([str(a.java_home/'bin'/('java'+exe)),'-Xmx1G','-Djava.awt.headless=true','-cp',os.pathsep.join([d,a.classpath]),'RBMKNativeTest',str(res),str(jar),str(a.legacy_client)],cwd=d,check=True)
