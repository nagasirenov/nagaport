#!/usr/bin/env python3
"""Four exact production event methods only, not whole ModEventHandler compilation."""
import argparse,hashlib,json,os,subprocess,tempfile
from pathlib import Path
from mapping import NAMES,method,migrate
p=argparse.ArgumentParser();p.add_argument('--module',type=Path,required=True);p.add_argument('--original',type=Path,required=True);p.add_argument('--source-root',type=Path);p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);a=p.parse_args();test=Path(__file__).resolve().parent;s=a.source_root or a.module
rel='src/main/java/com/hbm/main/ModEventHandler.java';text=(s/rel).read_text();original=(a.original/rel).read_text();methods=[]
for name in NAMES:
 v=method(text,name);assert v==migrate(method(original,name)),name;methods.append('@SubscribeEvent\n'+v)
fixtures={
'com/hbm/config/GeneralConfig.java':'package com.hbm.config;public class GeneralConfig {public static boolean enable528ExplosiveEnergistics,enableExtendedLogging,enableDebugMode;}',
'com/hbm/main/MainRegistry.java':'package com.hbm.main;public class MainRegistry {public static final Logs logger=new Logs();public static class Logs {public final java.util.List<String> messages=new java.util.ArrayList<>();public void log(org.apache.logging.log4j.Level l,String m){messages.add(l+":"+m);}public void debug(String m){messages.add(m);}}}',
'com/hbm/items/ModItems.java':'package com.hbm.items;public class ModItems {public static net.minecraft.item.Item bobmazon_hidden=net.minecraft.init.Items.DIAMOND;}',
'com/hbm/dim/WorldProviderCelestial.java':'package com.hbm.dim;public class WorldProviderCelestial extends net.minecraft.world.WorldProviderSurface {public static boolean attemptingSleep;}',
'com/hbm/dim/orbit/WorldProviderOrbit.java':'package com.hbm.dim.orbit;public class WorldProviderOrbit extends net.minecraft.world.WorldProviderSurface {}',
'com/hbm/lib/ModDamageSource.java':'package com.hbm.lib;public class ModDamageSource {public static final net.minecraft.util.DamageSource euthanizedSelf=new net.minecraft.util.DamageSource("probeA"),euthanizedSelf2=new net.minecraft.util.DamageSource("probeB");}',
'com/hbm/explosion/vanillant/ExplosionVNT.java':'''package com.hbm.explosion.vanillant;public class ExplosionVNT {public static final java.util.List<String> calls=new java.util.ArrayList<>();public static net.minecraft.world.World world;public ExplosionVNT(net.minecraft.world.World w,double x,double y,double z,float size,Object source){world=w;calls.add("create:"+x+":"+y+":"+z+":"+size+":"+source);}public void setEntityProcessor(Object o){calls.add("entity");}public void setPlayerProcessor(Object o){calls.add("player");}public void setSFX(Object o){calls.add("sfx");}public void explode(){calls.add("explode");}}''',
'com/hbm/explosion/vanillant/standard/EntityProcessorCrossSmooth.java':'''package com.hbm.explosion.vanillant.standard;public class EntityProcessorCrossSmooth {public EntityProcessorCrossSmooth(float a,float b){com.hbm.explosion.vanillant.ExplosionVNT.calls.add("processor:"+a+":"+b);}public EntityProcessorCrossSmooth setupPiercing(float a,float b){com.hbm.explosion.vanillant.ExplosionVNT.calls.add("pierce:"+a+":"+b);return this;}}''',
'com/hbm/explosion/vanillant/standard/PlayerProcessorStandard.java':'package com.hbm.explosion.vanillant.standard;public class PlayerProcessorStandard {}',
'com/hbm/explosion/vanillant/standard/ExplosionEffectWeapon.java':'''package com.hbm.explosion.vanillant.standard;public class ExplosionEffectWeapon {public ExplosionEffectWeapon(int a,float b,float c){com.hbm.explosion.vanillant.ExplosionVNT.calls.add("effect:"+a+":"+b+":"+c);}}'''
}
imports='''import java.util.Random;import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;import net.minecraftforge.event.entity.player.PlayerInteractEvent;import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;import net.minecraft.entity.player.EntityPlayer;import net.minecraft.entity.item.EntityItem;import net.minecraft.block.*;import net.minecraft.item.*;import net.minecraft.tileentity.TileEntitySign;import net.minecraft.util.math.BlockPos;import net.minecraft.world.World;import net.minecraft.init.Blocks;import org.apache.logging.log4j.Level;import com.hbm.config.GeneralConfig;import com.hbm.dim.WorldProviderCelestial;import com.hbm.dim.orbit.WorldProviderOrbit;import com.hbm.explosion.vanillant.ExplosionVNT;import com.hbm.explosion.vanillant.standard.*;import com.hbm.lib.ModDamageSource;import com.hbm.util.ShadyUtil;import com.hbm.items.ModItems;'''
with tempfile.TemporaryDirectory(prefix='native-player-events-') as tmp:
 d=Path(tmp);files=[]
 for path,body in fixtures.items():
  f=d/path;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(body);files.append(f)
 f=d/'com/hbm/main/ModEventHandler.java';f.write_text('package com.hbm.main;'+imports+'public class ModEventHandler {private static Random rand=new Random();'+ '\n'.join(methods)+'}');files.append(f)
 files += [a.module/'src/main/java/com/hbm/util/ShadyUtil.java',test/'PlayerEventsNativeTest.java',a.module/'porting/tests/ForgeEventNativeLauncher.java']
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',tmp]+list(map(str,files)),check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-cp',tmp+os.pathsep+a.classpath,'ForgeEventNativeLauncher','PlayerEventsNativeTest'],cwd=tmp,check=True)
print('PASS four exact original-derived event methods and full production ShadyUtil; explicit NTM explosion/config/provider/log/item/damage collaborators, no whole handler/game claim.')
