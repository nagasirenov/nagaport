#!/usr/bin/env python3
"""Exact-method native probe, NOT a compilation of ModEventHandlerClient as a whole."""
import argparse, hashlib, json, os, subprocess, tempfile, zipfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=Path,required=True);a=p.parse_args()
r=Path(__file__).resolve().parents[2];src=r/'src/main/java';tests=r/'porting/tests';original=r.parent/'ntm-original-pristine'
rel='com/hbm/main/ModEventHandlerClient.java'
def method(s,name):
 start=s.index('public void '+name+'(');i=s.index('{',start);depth=1;j=i+1
 while depth:
  if s[j]=='{':depth+=1
  elif s[j]=='}':depth-=1
  j+=1
 return s[start:j]
def migrate(s):
 for old,new in [('PlaySoundEvent17','PlaySoundEvent'),('minecraft:music.game.creative','minecraft:music.creative'),('event.sound.getPositionedSoundLocation()','event.getSound().getSoundLocation()'),('.theWorld','.world'),('event.result = null;','event.setResultSound(null);'),('provider.dimensionId','provider.getDimension()'),('event.result = currentSong = PositionedSoundRecord.func_147673_a(MUSIC_LOCATION);','currentSong = new PositionedSoundRecord(MUSIC_LOCATION, SoundCategory.MUSIC, 1.0F, 1.0F, false, 0, ISound.AttenuationType.NONE, 0.0F, 0.0F, 0.0F) { @Override public SoundCategory getCategory() { return SoundCategoryResolver.get(MUSIC_LOCATION); } };\n event.setResultSound(currentSong);'),('MathHelper.floor_double','MathHelper.floor'),('mc.world.getBlock(i, j, k)','mc.world.getBlockState(new BlockPos(i, j, k)).getBlock()'),('e.result = null;','e.setResultSound(null);'),('e.sound.getPositionedSoundLocation()','e.getSound().getSoundLocation()'),('e.sound.','e.getSound().'),('.thePlayer','.player'),('player.ridingEntity','player.getRidingEntity()'),('EntityClientPlayerMP','EntityPlayerSP'),('client.sendQueue.addToSendQueue','client.connection.sendPacket'),('C03PacketPlayer.C05PacketPlayerLook','CPacketPlayer.Rotation'),('C0CPacketInput','CPacketInput')]:s=s.replace(old,new)
 for k in ['nullChopper','nullCrashing','nullMine']:s=s.replace('r.toString().equals("hbm:misc.'+k+'")','r.equals(new ResourceLocation("hbm:misc.'+k+'"))')
 return s
assert hashlib.sha256(Path('/tmp/nagaport-legacy-client.jar').read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
current=(src/rel).read_text();legacy=(original/'src/main/java'/rel).read_text();methods=[]
for n in ['onPlayMusic','onPlaySound','worldTick']:
 c=method(current,n);o=method(legacy,n);assert ''.join(c.split())==''.join(migrate(o).split()),'unmapped original delta '+n
 methods.append('@SubscribeEvent\n'+c);print('EXACT METHOD',n,hashlib.sha256(c.encode()).hexdigest(),flush=True)
# Old event Result remains observer-visible; both hooks return the replaceable sound rather than Event.Result.
with zipfile.ZipFile('/tmp/nagaport-forge1614-userdev.jar') as z:
 hook=z.read('src/main/java/net/minecraftforge/client/ForgeHooksClient.java').decode();assert 'return e.result;' in hook
 event=z.read('src/main/java/cpw/mods/fml/common/eventhandler/Event.java').decode();assert 'result = value;' in event
# Source boundaries against CP36: only nine namespace qualifiers and one helper visibility change.
baseline='e146f1df78ba6ce7ebf3198217a67d132e1e7dff'
def cp36(path):return subprocess.check_output(['git','show',baseline+':ntm-port-1.12.2/'+path],cwd=r).decode()
path='src/main/resources/assets/hbm/sounds.json';expected=cp36(path)
for k in ['misc.nullChopper','misc.nullCrashing','misc.nullMine']:
 start=expected.index('"'+k+'"');end=expected.index('\n',start);expected=expected[:start]+expected[start:end].replace('"name": "misc/null"','"name": "hbm:misc/null"')+expected[end:]
for suffix in ['Break','Trill','Beat','Transcend','Calm','Koyaanis']:expected=expected.replace('"name": "music/space'+suffix+'"','"name": "hbm:music/space'+suffix+'"')
assert (r/path).read_text()==expected,'sounds.json outside narrow namespace scope'
path='src/main/java/com/hbm/sound/SoundCategoryResolver.java'
assert (r/path).read_text()==cp36(path).replace('static SoundCategory get(ResourceLocation sound)','public static SoundCategory get(ResourceLocation sound)'), 'helper change beyond public getter'
print('PASS CP36 exact resource/helper source boundaries',flush=True)
imports='''import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;import net.minecraftforge.fml.common.eventhandler.Event.Result;import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;import net.minecraftforge.client.event.sound.PlaySoundEvent;import net.minecraft.client.Minecraft;import net.minecraft.client.audio.*;import net.minecraft.client.entity.EntityPlayerSP;import net.minecraft.client.multiplayer.WorldClient;import net.minecraft.entity.player.EntityPlayer;import net.minecraft.util.*;import net.minecraft.util.math.*;import net.minecraft.world.WorldProvider;import net.minecraft.block.Block;import net.minecraft.network.play.client.*;import com.hbm.dim.WorldProviderCelestial;import com.hbm.dim.orbit.WorldProviderOrbit;import com.hbm.blocks.ModBlocks;import com.hbm.util.Library;import com.hbm.entity.mob.EntityHunterChopper;import com.hbm.entity.projectile.EntityChopperMine;import com.hbm.entity.train.EntityRailCarRidable;import com.hbm.sound.*;import com.hbm.sound.MovingSoundPlayerLoop.EnumHbmSound;import com.hbm.items.ItemCustomLore;'''
entity='extends net.minecraft.entity.Entity {public %s(){super(null);}protected void entityInit(){}protected void readEntityFromNBT(net.minecraft.nbt.NBTTagCompound n){}protected void writeEntityToNBT(net.minecraft.nbt.NBTTagCompound n){} %s}'
fixtures={
'com/hbm/main/MainRegistry.java':'package com.hbm.main; public class MainRegistry {public static Proxy proxy=new Proxy();public static class Proxy {public net.minecraft.entity.player.EntityPlayer me(){return net.minecraft.client.Minecraft.getMinecraft().player;}}}',
'com/hbm/blocks/ModBlocks.java':'package com.hbm.blocks;public class ModBlocks {public static net.minecraft.block.Block vacuum=net.minecraft.init.Blocks.BARRIER;}',
'com/hbm/items/ItemCustomLore.java':'package com.hbm.items;public class ItemCustomLore {public static int calls;public static void updateSystem(){calls++;}}',
'com/hbm/dim/WorldProviderCelestial.java':'package com.hbm.dim;public class WorldProviderCelestial extends net.minecraft.world.WorldProviderSurface {}',
'com/hbm/dim/orbit/WorldProviderOrbit.java':'package com.hbm.dim.orbit;public class WorldProviderOrbit extends net.minecraft.world.WorldProviderSurface {}',
'com/hbm/entity/mob/EntityHunterChopper.java':'package com.hbm.entity.mob;public class EntityHunterChopper '+entity%('EntityHunterChopper','public boolean getIsDying(){return false;}'),
'com/hbm/entity/projectile/EntityChopperMine.java':'package com.hbm.entity.projectile;public class EntityChopperMine '+entity%('EntityChopperMine',''),
'com/hbm/entity/train/EntityRailCarRidable.java':'package com.hbm.entity.train;public class EntityRailCarRidable '+entity%('EntityRailCarRidable','public boolean sit;public boolean shouldRiderSit(){return sit;}'),
'com/hbm/util/Library.java':'''package com.hbm.util;public class Library {public static com.hbm.entity.mob.EntityHunterChopper chopper;public static com.hbm.entity.projectile.EntityChopperMine mine;public static int calls; public static float x,y,z;public static int range;public static com.hbm.entity.mob.EntityHunterChopper getClosestChopperForSound(net.minecraft.world.World w,float a,float b,float c,int d){calls++;x=a;y=b;z=c;range=d;return chopper;}public static com.hbm.entity.projectile.EntityChopperMine getClosestMineForSound(net.minecraft.world.World w,float a,float b,float c,int d){calls++;x=a;y=b;z=c;range=d;return mine;}}'''}
production=['ResourceLoopSound','SoundCategoryResolver','MovingSoundPlayerLoop','MovingSoundChopper','MovingSoundCrashing','MovingSoundChopperMine']
with tempfile.TemporaryDirectory(prefix='client-sound-train-native-') as tmp:
 d=Path(tmp);files=[]
 for path,body in fixtures.items():
  f=d/'fixtures'/path;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(body);files.append(f);print('NTM FIXTURE',path,flush=True)
 f=d/'extracted/com/hbm/main/ModEventHandlerClient.java';f.parent.mkdir(parents=True);f.write_text('package com.hbm.main;'+imports+'public class ModEventHandlerClient {private static final ResourceLocation MUSIC_LOCATION=new ResourceLocation("hbm:music.game.space");private ISound currentSong;'+ '\n'.join(methods)+'}');files.append(f)
 for n in production:files.append(src/('com/hbm/sound/'+n+'.java'))
 files += [tests/'ClientSoundTrainNativeTest.java',tests/'ForgeEventNativeLauncher.java']
 jar=d/'resources.jar'
 with zipfile.ZipFile(jar,'w') as z:
  base=r/'src/main/resources'
  z.write(base/'assets/hbm/sounds.json','assets/hbm/sounds.json')
  defs=json.loads((base/'assets/hbm/sounds.json').read_text())
  for event in ['misc.nullChopper','misc.nullCrashing','misc.nullMine','entity.chopperFlyingLoop','entity.chopperCrashingLoop','entity.chopperMineLoop','music.game.space']:
   for sample in defs[event]['sounds']:
    name=sample['name'] if isinstance(sample,dict) else sample
    path='assets/hbm/sounds/'+name.removeprefix('hbm:').lower()+'.ogg'
    if path not in z.namelist():z.write(base/path,path)
 for item in json.loads((tests/'client-sound-train-resources.json').read_text()):
  assert not (r/item['retired']).exists()
  assert hashlib.sha256((r/item['current']).read_bytes()).hexdigest()==item['sha256']==hashlib.sha256((original/item['retired']).read_bytes()).hexdigest()

 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',str(d/'fixtures'),'-cp',a.classpath,'-d',tmp]+list(map(str,files)),check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-cp',tmp+os.pathsep+a.classpath,'ForgeEventNativeLauncher','ClientSoundTrainNativeTest',str(r/'src/main/resources'),str(jar),'/tmp/nagaport-legacy-client.jar'],cwd=tmp,check=True)
print('PASS original-method mapping, original Forge result semantics, exact extracted native methods; NOT whole event class or launched gameplay')
