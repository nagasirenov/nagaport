#!/usr/bin/env python3
"""Compile complete native cable blocks/models on real Minecraft/Forge.
The original classic tessellator methods run in a test-only recorder, without
legacy Minecraft/Forge facades. Only NTM registry/tile collaborators are controlled;
the production Library.canConnect method is extracted unchanged for isolation.
"""
import argparse, hashlib, os, re, subprocess, tempfile, zipfile
from pathlib import Path
p=argparse.ArgumentParser()
p.add_argument('--java-home',type=Path,required=True)
p.add_argument('--classpath',required=True)
p.add_argument('--original-root',type=Path,help='Pinned original source checkout; defaults to sibling ntm-original-pristine')
p.add_argument('--module',type=Path,required=True);p.add_argument('--source-root',type=Path,required=True);a=p.parse_args();root=a.module;src=a.module/'src/main/java';tests=Path(__file__).resolve().parent;stage=a.source_root/'src/main/java'
if a.original_root is None:a.original_root=root.parent/'ntm-original-pristine'
exe='.exe' if os.name=='nt' else ''
if not (a.java_home/'bin'/('javac'+exe)).is_file(): a.java_home=a.java_home.parent
pinned={
 'src/main/java/com/hbm/render/block/RenderCable.java':'8b18c13cf596de519848679a4d1b2b37677e2683964817b4ae4e2cfb407a3bb6',
 'src/main/java/com/hbm/render/block/RenderCableClassic.java':'9370713b903286f975ef385e2d616f390c4e248f628b8697f5c7e39aa567e6df',
 'src/main/java/com/hbm/blocks/network/BlockCable.java':'5e66960027a213d7fa9fe953050da7ad3d835567bf0b09cd3bcdf30049e443fd',
 'src/main/resources/assets/hbm/models/blocks/cable_neo.obj':'51f16e698e3d0dcae5e165e185c0eb50be87c5b7fb78be5fc0997503ead37e4e',
}
pinned['src/main/java/com/hbm/render/block/RenderDetCord.java']='e5f450fd603331b59b4d67f523ca8dc86b5620a605c72ed3514e5ff95b2b0e6d'
pinned['src/main/java/com/hbm/blocks/bomb/DetCord.java']='c9ae64074254be02f8ccc77bb0a16bfd43404303c27298dee563e05f60bccd90'
pinned['src/main/java/com/hbm/blocks/bomb/IDetConnectible.java']='7b9a0f95231d069e5114d9af0a895d8f94cd3d4d06c3145c45c482d64a4c35b2'
pinned['src/main/resources/assets/hbm/textures/blocks/det_cord.png']='d344309924da191e1a65b86d8f672d7e25f6ad716ed8cee6e42c249919f8a32d'
for rel,want in pinned.items():
 path=a.original_root/rel;got=hashlib.sha256(path.read_bytes()).hexdigest()
 assert got==want, f'Original source changed: {rel}: {got} != {want}'
 print('PINNED ORIGINAL',rel,got,flush=True)
assert (a.original_root/'src/main/resources/assets/hbm/models/blocks/cable_neo.obj').read_bytes()==(root/'src/main/resources/assets/hbm/models/blocks/cable_neo.obj').read_bytes(), 'Native cable must retain original OBJ bytes'
def method(text,name):
 m=re.search(r'public (?:static )?(?:void|boolean) '+name+r'\([^)]*\)\s*\{',text);assert m,name
 end=m.end();depth=1
 while depth:depth+=(text[end]=='{')-(text[end]=='}');end+=1
 return text[m.end():end-1]
classic=(a.original_root/'src/main/java/com/hbm/render/block/RenderCableClassic.java').read_text()
neo=(a.original_root/'src/main/java/com/hbm/render/block/RenderDetCord.java').read_text()
flags='boolean pX=(mask&32)!=0,nX=(mask&16)!=0,pY=(mask&8)!=0,nY=(mask&4)!=0,pZ=(mask&2)!=0,nZ=(mask&1)!=0;'
# Preserve every original branch and vertex emission expression verbatim. Only bind
# the legacy tessellator/icon plumbing to the test recorder and remove GL calls.
inventory=method(classic,'renderInventoryBlock');inventory=inventory[inventory.index('double uv_cL'):]
inventory=re.sub(r'Tessellator tessellator = Tessellator.instance;', 'Recorder tessellator = new Recorder(true, true);',inventory)
inventory=re.sub(r'GL11\.glPopMatrix\(\);','',inventory)
world=method(classic,'renderWorldBlock');world=world[world.index('double uv_cL'):]
world=world.replace('return true;','return tessellator.quads();')
world='int x=0,y=0,z=0;'+flags+'Recorder tessellator=new Recorder(false,true);'+world
selector=method(neo,'renderWorldBlock');selector=selector[selector.index('if(mask =='):selector.index('tessellator.addTranslation(-x')]
selector=re.sub(r'ObjUtil.renderPartWithIcon\(\(HFRWavefrontObject\) ResourceManager.cable_neo, "([^"]+)", iicon, tessellator, 0, true\);',r'result.add("\1");',selector)
itemparts=re.findall(r'renderPartWithIcon\(\(HFRWavefrontObject\) ResourceManager.cable_neo, "([^"]+)"',method(neo,'renderInventoryBlock'))
oracle='''import java.util.*; import net.minecraft.client.renderer.texture.TextureAtlasSprite;
final class OriginalCableOracle {
 static final class Vertex {final float x,y,z,u,v,nx,ny,nz;final int shade;
 Vertex(double a,double b,double c,double d,double e,float f,float g,float h,int s){x=(float)a;y=(float)b;z=(float)c;u=(float)d;v=(float)e;nx=f;ny=g;nz=h;shade=s;}}
 static final class Recorder {
 final List<Vertex[]> out=new ArrayList<>(); final List<Vertex> pending=new ArrayList<>();
 final boolean inventory,classic; float nx,ny,nz; int shade=255;
 Recorder(boolean i,boolean c){inventory=i;classic=c;}
 void setColorOpaque_F(float r,float g,float b){if(r!=g||r!=b)throw new AssertionError("not gray");shade=(int)(r*255F);}
 void setNormal(float x,float y,float z){nx=x;ny=y;nz=z;}
 void startDrawingQuads(){} void draw(){if(!pending.isEmpty())throw new AssertionError("incomplete legacy quad");}
 void addVertexWithUV(double x,double y,double z,double u,double v){
 if(inventory){if(classic){x=(x-.5)*1.25+.5;y=(y-.5)*1.25+.5;z=(z-.5)*1.25+.5;}else{x=-x*1.25+.5;y=y*1.25+.5;z=-z*1.25+.5;}}
 else if(!classic){x+=.5;y+=.5;z+=.5;}
 pending.add(new Vertex(x,y,z,u,v,nx,ny,nz,shade));if(pending.size()==4){out.add(pending.toArray(new Vertex[4]));pending.clear();}}
 List<Vertex[]> quads(){if(!pending.isEmpty())throw new AssertionError("incomplete legacy quad");return out;}}
 static List<Vertex[]> classicItem(TextureAtlasSprite iicon){\n'''+inventory+'''\nreturn tessellator.quads();}
 static List<Vertex[]> classicWorld(TextureAtlasSprite iicon,int mask){\n'''+world+'''\n}
 static List<String> modernGroups(int mask,boolean inventory){List<String> result=new ArrayList<>();
 if(inventory){return Arrays.asList('''+','.join('"'+x+'"' for x in itemparts)+''');}
 '''+flags+selector+'''return result;}
}
'''
collaborators={
'com/hbm/lib/Library.java':'package com.hbm.lib;public final class Library {public static final ForgeDirection POS_X=ForgeDirection.EAST,NEG_X=ForgeDirection.WEST,POS_Y=ForgeDirection.UP,NEG_Y=ForgeDirection.DOWN,POS_Z=ForgeDirection.SOUTH,NEG_Z=ForgeDirection.NORTH;public static boolean canConnect(net.minecraft.world.IBlockAccess w,int x,int y,int z,ForgeDirection d){throw new AssertionError("Energy connectivity outside cord scope");}}',
'com/hbm/entity/item/EntityTNTPrimedBase.java':'package com.hbm.entity.item;public class EntityTNTPrimedBase extends net.minecraft.entity.item.EntityTNTPrimed {public int fuse;public boolean detonateOnCollision;public EntityTNTPrimedBase(net.minecraft.world.World w,double x,double y,double z,net.minecraft.entity.EntityLivingBase e,com.hbm.blocks.bomb.BlockDetonatable b){super(w,x,y,z,e);}}'
}
production=[stage/q for q in ['com/hbm/blocks/bomb/DetCord.java', 'com/hbm/blocks/bomb/IDetConnectible.java', 'com/hbm/render/block/RenderDetCord.java', 'com/hbm/render/block/nativeblock/DetCordBakedModel.java', 'com/hbm/main/DetCordModelHandler.java']]+[src/q for q in ['com/hbm/blocks/BlockBase.java', 'com/hbm/blocks/generic/BlockFlammable.java', 'com/hbm/blocks/bomb/BlockDetonatable.java', 'api/hbm/block/IFuckingExplode.java', 'com/hbm/render/icon/IIcon.java', 'com/hbm/render/icon/IIconRegister.java', 'com/hbm/lib/RefStrings.java', 'com/hbm/lib/ForgeDirection.java', 'com/hbm/render/mesh/ObjMesh.java', 'com/hbm/render/mesh/ObjMeshParser.java', 'com/hbm/blocks/network/ConnectionState.java', 'com/hbm/render/block/RenderCableClassic.java', 'com/hbm/render/block/nativeblock/ConnectionQuadFactory.java', 'com/hbm/render/block/nativeblock/BlockPositionLightQuad.java', 'com/hbm/render/block/nativeblock/ThermalModelTransforms.java']]
with tempfile.TemporaryDirectory(prefix='cable-native-') as tmp:
 out=Path(tmp);files=[]
 for rel,content in collaborators.items():
  assert rel.startswith('com/hbm/'),'Only NTM collaborators may be substituted'
  target=out/rel;target.parent.mkdir(parents=True,exist_ok=True);target.write_text(content);files.append(str(target))
 for source in production:
  print('PRODUCTION',source.name,hashlib.sha256(source.read_bytes()).hexdigest(),flush=True);files.append(str(source))
 for name,content in [('OriginalCableOracle',oracle),('DetCordNativeTest',(tests/'detcord-native-test.java.in').read_text())]:
  f=out/(name+'.java');f.write_text(content);files.append(str(f))
 subprocess.run([str(a.java_home/'bin'/('javac'+exe)),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',tmp]+files,check=True)
 resources=root/'src/main/resources';jar=out/'cable-resources.jar'
 with zipfile.ZipFile(jar,'w') as z:
  for path in (resources/'assets/hbm').rglob('*'):
   if path.is_file() and (path.name in ['cable_neo.obj','det_cord.png']):z.write(path,path.relative_to(resources).as_posix())
  extra=a.source_root/'src/main/resources/assets/hbm/blockstates/tile.det_cord.json';z.write(extra,'assets/hbm/blockstates/tile.det_cord.json')
 # Folder fixture contains original resources plus the staged blockstate.
 folder=out/'resources';import shutil;shutil.copytree(resources,folder);shutil.copy2(extra,folder/'assets/hbm/blockstates/tile.det_cord.json')
 subprocess.run([str(a.java_home/'bin'/('java'+exe)),'-Djava.awt.headless=true','-cp',os.pathsep.join([tmp,a.classpath]),'DetCordNativeTest',str(folder),str(jar),str(a.original_root/'src/main/resources/assets/hbm/models/blocks/cable_neo.obj')],cwd=tmp,check=True)
