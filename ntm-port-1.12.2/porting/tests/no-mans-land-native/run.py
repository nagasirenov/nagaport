#!/usr/bin/env python3
import pathlib,argparse,tempfile,subprocess,os,hashlib
p=argparse.ArgumentParser();p.add_argument('--java-home',type=pathlib.Path,required=True);p.add_argument('--classpath',required=True);a=p.parse_args();here=pathlib.Path(__file__).resolve().parent;root=here.parents[2]
with tempfile.TemporaryDirectory(prefix='no-mans-land-native-') as t:
 out=pathlib.Path(t);files=[root/'src/main/java/com/hbm'/n for n in ['world/biome/BiomeGenNoMansLand.java','world/biome/BiomeDecoratorNoMansLand.java','world/feature/WorldGenSurfaceSpot.java']]+[here/'NoMansLandNativeTest.java',root/'porting/tests/ForgeEventNativeLauncher.java']
 for f in files[:3]:print('WHOLE INPUT',f.name,hashlib.sha256(f.read_bytes()).hexdigest(),flush=True)
 fixtures={'com/hbm/blocks/ModBlocks.java':'package com.hbm.blocks;public class ModBlocks {public static net.minecraft.block.Block sellafield_slaked=net.minecraft.init.Blocks.COBBLESTONE;}', 'com/hbm/entity/mob/EntityUndeadSoldier.java':'package com.hbm.entity.mob;public class EntityUndeadSoldier extends net.minecraft.entity.monster.EntityMob {public EntityUndeadSoldier(net.minecraft.world.World w){super(w);}}'}
 for n,s in fixtures.items():f=out/n;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(s);files.append(f)
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-cp',a.classpath,'-d',t]+list(map(str,files)),check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-cp',t+os.pathsep+a.classpath,'ForgeEventNativeLauncher','NoMansLandNativeTest'],cwd=t,check=True)
