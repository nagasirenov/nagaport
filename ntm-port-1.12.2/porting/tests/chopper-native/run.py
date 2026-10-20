#!/usr/bin/env python3
import pathlib,argparse,tempfile,subprocess,os,hashlib
p=argparse.ArgumentParser();p.add_argument('--java-home',type=pathlib.Path,required=True);p.add_argument('--classpath',required=True);a=p.parse_args();here=pathlib.Path(__file__).resolve().parent;root=here.parents[2]
with tempfile.TemporaryDirectory(prefix='chopper-native-') as t:
 out=pathlib.Path(t);files=[root/'src/main/java/com/hbm/items/special/ItemChopper.java',root/'src/main/java/com/hbm/blocks/generic/BlockMetalFence.java',root/'src/main/java/com/hbm/blocks/IBlockMulti.java',here/'ChopperNativeTest.java']; print('WHOLE INPUT ItemChopper SHA256',hashlib.sha256(files[0].read_bytes()).hexdigest(),flush=True)
 sources={'com/hbm/blocks/ModBlocks.java':'package com.hbm.blocks; public class ModBlocks {public static net.minecraft.block.Block fence_gate=net.minecraft.init.Blocks.OAK_FENCE_GATE;}', 'com/hbm/items/ModItems.java':'package com.hbm.items; public class ModItems {public static net.minecraft.item.Item spawn_chopper,spawn_worm,spawn_ufo,spawn_angel,spawn_duck;}'}
 for name in ['EntityBFAngel','EntityDuck','EntityHunterChopper','EntityUFO','botprime/EntityBOTPrimeHead']:
  package='com.hbm.entity.mob'+('.botprime' if '/' in name else '');simple=name.split('/')[-1]
  sources['com/hbm/entity/mob/'+name+'.java']='package '+package+'; public class '+simple+' extends net.minecraft.entity.EntityLiving { public int scanCooldown,initialCalls;public '+simple+'(net.minecraft.world.World w){super(w);} @Override public net.minecraft.entity.IEntityLivingData onInitialSpawn(net.minecraft.world.DifficultyInstance d,net.minecraft.entity.IEntityLivingData data){initialCalls++;return data;} }'
 for n,s in sources.items():f=out/n;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(s);files.append(f)
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-cp',a.classpath,'-d',t]+list(map(str,files)),check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-cp',t+os.pathsep+a.classpath,'ChopperNativeTest'],cwd=t,check=True)
