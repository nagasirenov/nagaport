#!/usr/bin/env python3
"""Complete HUD production classes against native Forge; explicit mod-state fixtures, real GUI/EGL draw."""
import argparse,pathlib,tempfile,subprocess,os,zipfile,hashlib
p=argparse.ArgumentParser();p.add_argument('--java-home',type=pathlib.Path,required=True);p.add_argument('--classpath',required=True);p.add_argument('--cache-mutation',action='store_true');a=p.parse_args();r=pathlib.Path(__file__).resolve().parents[2]
names=['render/util/RenderScreenOverlay.java','items/weapon/sedna/hud/HUDComponentAmmoCounter.java','items/weapon/sedna/hud/HUDComponentDurabilityBar.java','items/weapon/sedna/hud/IHUDComponent.java','items/weapon/sedna/Crosshair.java','lib/RefStrings.java','interfaces/Spaghetti.java','interfaces/Untested.java','render/item/nativeitem/MaterialTextureAtlasSprite.java']
fixtures={
'config/ClientConfig':'public static class Offset {public int get(){return 0;}} public static Offset GEIGER_OFFSET_HORIZONTAL=new Offset(),GEIGER_OFFSET_VERTICAL=new Offset();',
'config/GeneralConfig':'public static boolean true528(){return false;}public static boolean trueExp(){return false;}',
'config/MobConfig':'public static boolean trueRam(){return false;}',
'handler/ImpactWorldHandler':'public static long getTimeForClient(net.minecraft.world.World w){return 20000;}',
'util/Clock':'public static long get_ms(){return 10000;}',
'extprop/HbmPlayerProps':'public float shield=20;public static HbmPlayerProps getData(net.minecraft.entity.player.EntityPlayer p){return new HbmPlayerProps();}public int getStamina(){return 30;}public int getDashCount(){return 3;}public float getEffectiveMaxShield(){return 100;}',
'items/weapon/sedna/impl/ItemGunStinger':'public static double lockon=.5;',
'items/weapon/sedna/mags/IMagazine':'public net.minecraft.item.ItemStack getIconForHUD(net.minecraft.item.ItemStack s,net.minecraft.entity.player.EntityPlayer p);public String reportAmmoStateForHUD(net.minecraft.item.ItemStack s,net.minecraft.entity.player.EntityPlayer p);',
'items/weapon/sedna/ItemGunBaseNT':'public static com.hbm.items.weapon.sedna.mags.IMagazine magazine;public static int wear=25,durability=100,lastIndex;public Config getConfig(net.minecraft.item.ItemStack s,int i){lastIndex=i;return new Config();}public int getWear(net.minecraft.item.ItemStack s,int i){lastIndex=i;return wear;}public static class Config {public Receiver[] getReceivers(net.minecraft.item.ItemStack s){return new Receiver[]{new Receiver(),new Receiver()};}public int getDurability(net.minecraft.item.ItemStack s){return durability;}}public static class Receiver {public com.hbm.items.weapon.sedna.mags.IMagazine getMagazine(net.minecraft.item.ItemStack s){return magazine;}}'
}
with tempfile.TemporaryDirectory(prefix='native-hud-') as t:
 t=pathlib.Path(t);files=[r/'src/main/java/com/hbm'/n for n in names]
 if a.cache_mutation:
  original=r/'src/main/java/com/hbm/items/weapon/sedna/hud/HUDComponentDurabilityBar.java';mutant=t/'HUDComponentDurabilityBar.java';mutant.write_text(original.read_text().replace('GlStateManager.color(', 'GL11.glColor4f('));files[files.index(original)]=mutant
 for n,body in fixtures.items():
  f=t/'com/hbm'/ (n+'.java');f.parent.mkdir(parents=True,exist_ok=True);pkg,cls=n.rsplit('/',1);kind='interface' if cls=='IMagazine' else 'class';extend=' extends net.minecraft.item.Item' if cls=='ItemGunBaseNT' else '';f.write_text('package com.hbm.'+pkg.replace('/','.')+';public '+kind+' '+cls+extend+'{'+body+'}');files.append(f)
 for jar in a.classpath.split(os.pathsep):
  if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
   with zipfile.ZipFile(jar) as z:
    for n in z.namelist():
     if n.endswith('.so') and '/' not in n:(t/n).write_bytes(z.read(n))
 files += [r/'porting/tests/NativeHudTest.java',r/'porting/tests/MaterialTextureAtlasSpriteNativeTest.java']
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',str(t)]+list(map(str,files)),check=True)
 result=subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-Djava.library.path='+str(t),'-cp',str(t)+os.pathsep+a.classpath,'NativeHudTest',str(r/'src/main/resources')],cwd=t,env=dict(os.environ,MESA_SHADER_CACHE_DISABLE='true'),capture_output=a.cache_mutation,text=True)
 if a.cache_mutation:
  assert result.returncode!=0 and 'AssertionError: native color cache agrees with GPU' in result.stderr,(result.returncode,result.stdout,result.stderr)
  print('PASS controlled original raw-color mutation rejected by genuine native cache/GPU check; production untouched.')
 else: result.check_returncode()
 for f in files:
  if str(f).startswith(str(r)):print(hashlib.sha256(f.read_bytes()).hexdigest(),f.relative_to(r))
