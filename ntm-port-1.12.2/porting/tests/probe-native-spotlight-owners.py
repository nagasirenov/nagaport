#!/usr/bin/env python3
"""Compile whole native spotlight owners and real TileEntityData against cached 1.12.2.
Only external NTM registry/celestial-trait collaborators are bounded recorders.
No Minecraft/Forge source, beam owner methods, or tile bodies are substituted.
"""
import argparse, hashlib, os, pathlib, subprocess, tempfile
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=pathlib.Path,required=True);a=p.parse_args()
r=pathlib.Path(__file__).resolve().parents[2];s=r/'src/main/java';t=r/'porting/tests'
production=['com/hbm/blocks/machine/'+n+'.java' for n in ['Spotlight','SpotlightModular','SpotlightBeam','BlockBeamBase','SpotlightModelState']]+['com/hbm/blocks/BlockEnums.java','com/hbm/blocks/ISpotlight.java','com/hbm/world/gen/nbt/INBTBlockTransformable.java','com/hbm/lib/ForgeDirection.java','com/hbm/lib/FacingCompat.java','com/hbm/lib/RefStrings.java','com/hbm/tileentity/TileEntityData.java']
collaborators={
'com/hbm/blocks/ModBlocks.java':'''package com.hbm.blocks;public class ModBlocks {public static net.minecraft.block.Block spotlight_incandescent,spotlight_incandescent_off,spotlight_fluoro,spotlight_fluoro_off,spotlight_halogen,spotlight_halogen_off,spotlight_beam;}''',
'com/hbm/dim/CelestialBody.java':'''package com.hbm.dim;
public class CelestialBody {
 public static final java.util.Map<net.minecraft.world.World,CelestialBody> bodies=new java.util.IdentityHashMap<>();
 public com.hbm.dim.trait.CBT_Lights trait; public int modifications,lookups;
 public static CelestialBody getBody(net.minecraft.world.World world){CelestialBody body=bodies.get(world);if(body==null){body=new CelestialBody();bodies.put(world,body);}return body;}
 public <T extends com.hbm.dim.trait.CelestialBodyTrait> T getTrait(Class<? extends T> type){lookups++;return type.cast(trait);}
 public void modifyTraits(com.hbm.dim.trait.CelestialBodyTrait... traits){if(traits.length!=1)throw new AssertionError("repair should submit one trait");modifications++;trait=(com.hbm.dim.trait.CBT_Lights)traits[0];}
}''',
'com/hbm/dim/trait/CelestialBodyTrait.java':'package com.hbm.dim.trait;public abstract class CelestialBodyTrait {}',
'com/hbm/dim/trait/CBT_Lights.java':'''package com.hbm.dim.trait;
public class CBT_Lights extends CelestialBodyTrait {public final java.util.List<net.minecraft.block.Block> blocks=new java.util.ArrayList<>();public final java.util.List<net.minecraft.util.math.BlockPos> positions=new java.util.ArrayList<>();public void addLight(net.minecraft.block.Block block,int x,int y,int z){blocks.add(block);positions.add(new net.minecraft.util.math.BlockPos(x,y,z));}}
'''
}
with tempfile.TemporaryDirectory(prefix='native-spotlight-owners-') as d:
 out=pathlib.Path(d);files=[s/path for path in production]+[t/'NativeSpotlightOwnersTest.java',t/'ForgeEventNativeLauncher.java']
 for path,body in collaborators.items():
  target=out/'fixtures'/path;target.parent.mkdir(parents=True,exist_ok=True);target.write_text(body);files.append(target)
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',str(out/'fixtures'),'-cp',a.classpath,'-d',d]+list(map(str,files)),check=True)
 print('PASS complete four spotlight owners, production model state, real TileEntityData and direction/transform interfaces compile',flush=True)
 subprocess.run([str(a.java_home/'bin/java'),'-cp',d+os.pathsep+a.classpath,'ForgeEventNativeLauncher','NativeSpotlightOwnersTest'],cwd=d,check=True)
 for path in production:print(hashlib.sha256((s/path).read_bytes()).hexdigest(),path)
print('BOUNDARY: real World/Chunk/registry/redstone/ItemStack/NBT; controlled chunk provider, light-engine skip, entity-output sink, recorded scheduling, NTM registry/celestial trait collaborators. Shared TileEntityData external same-block metadata changes refresh its tile; spotlight-owned mutations never change existing beam color metadata. No live server scheduler, persistence, production CelestialBody, GL, full-mod acceptance.',flush=True)
