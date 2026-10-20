#!/usr/bin/env python3
"""Full radio tile sources + native base classes against real Minecraft/Forge.
Only external NTM atmosphere, compatibility, ore-name and explosion-domain boundaries
are controlled collaborators. Packet envelope is extracted unchanged; dispatch is captured.
No Minecraft/Forge API shims and no substitutions in the radio production source.
"""
from pathlib import Path
import argparse, hashlib, json, os, re, subprocess, tempfile
p=argparse.ArgumentParser()
p.add_argument('--module',type=Path,default=Path(__file__).resolve().parents[3])
p.add_argument('--java-home',type=Path,required=True)
p.add_argument('--classpath',required=True)
p.add_argument('--pristine',type=Path,required=True)
a=p.parse_args();src=a.module/'src/main/java';tests=Path(__file__).resolve().parent
pins=json.loads((tests/'tile-original-manifest.json').read_text())
for relative,wanted in pins.items():
 actual=hashlib.sha256((a.pristine/relative).read_bytes()).hexdigest()
 assert actual==wanted, 'Pristine radio evidence changed: '+relative
# Preserve the original queue, including summation, timestamp, clear order and song.
old=(a.pristine/'src/main/java/com/hbm/tileentity/network/RTTYSystem.java').read_text()
new=(src/'com/hbm/tileentity/network/RTTYSystem.java').read_text()
expected=old.replace('import net.minecraft.server.MinecraftServer;','import net.minecraftforge.fml.common.FMLCommonHandler;').replace('MinecraftServer.getServer().worldServers','FMLCommonHandler.instance().getMinecraftServerInstance().worlds')
assert expected==new, 'RTTYSystem changed beyond its native server accessor'
def method(text,name):
 match=re.search(r'public (?:void|NBTTagCompound) '+name+r'\([^)]*\)\s*\{',text);assert match,name
 i=match.end();depth=1
 while depth:
  depth+=(text[i]=='{')-(text[i]=='}');i+=1
 return re.sub(r'\s+',' ',text[match.start():i].replace('public NBTTagCompound writeToNBT','public void writeToNBT').replace('return nbt;','')).strip()
for owner in ['Base','Logic','Reader','Controller','Counter']:
 relative='com/hbm/tileentity/network/TileEntityRadioTorch'+owner+'.java'
 old=(a.pristine/'src/main/java'/relative).read_text();new=(src/relative).read_text()
 for name in ['readFromNBT','writeToNBT','serialize','deserialize']:
  assert method(old,name)==method(new,name),owner+' '+name+' differs from original persistence/protocol'
print('PASS pinned original radio sources, unchanged queue body and all original persisted keys/byte-buffer protocols.',flush=True)
script=(a.module/'porting/tests/probe-machine-core.py').read_text()
context={'src':src}
exec(script[script.index('collaborators={'):script.index('def method(')],context)
collaborators=context['collaborators']; production=context['production']
collaborators.update({
'com/hbm/handler/CompatHandler.java':'package com.hbm.handler; public class CompatHandler {public interface OCComponent {}}',
'com/hbm/util/Compat.java':'''package com.hbm.util; public class Compat {public static net.minecraft.tileentity.TileEntity getTileStandard(net.minecraft.world.World world,int x,int y,int z){net.minecraft.util.math.BlockPos pos=new net.minecraft.util.math.BlockPos(x,y,z);if(!world.isBlockLoaded(pos,false))return null;return world.getTileEntity(pos);}}''',
'com/hbm/items/special/ItemBedrockOreNew.java':'package com.hbm.items.special; public class ItemBedrockOreNew extends net.minecraft.item.Item { public int getGrade(int m){return m;}}',
'com/hbm/util/ItemStackUtil.java':'package com.hbm.util; public class ItemStackUtil {public static java.util.List<String> getOreDictNames(net.minecraft.item.ItemStack s){return java.util.Collections.emptyList();}}',

'com/hbm/explosion/vanillant/ExplosionVNT.java':'package com.hbm.explosion.vanillant; public class ExplosionVNT {public static int exploded;public ExplosionVNT(net.minecraft.world.World w,double x,double y,double z,float r,Object e){}public void setEntityProcessor(Object p){}public void setPlayerProcessor(Object p){}public void setSFX(Object p){}public void explode(){exploded++;}}',
'com/hbm/explosion/vanillant/standard/EntityProcessorCrossSmooth.java':'package com.hbm.explosion.vanillant.standard; public class EntityProcessorCrossSmooth {public EntityProcessorCrossSmooth(float a,float b){}public Object setupPiercing(float a,float b){return this;}}',
'com/hbm/explosion/vanillant/standard/PlayerProcessorStandard.java':'package com.hbm.explosion.vanillant.standard; public class PlayerProcessorStandard {}',
'com/hbm/explosion/vanillant/standard/ExplosionEffectWeapon.java':'package com.hbm.explosion.vanillant.standard; public class ExplosionEffectWeapon {public ExplosionEffectWeapon(int a,float b,float c){}}'
})
production+=['com/hbm/tileentity/network/'+n+'.java' for n in ['RTTYSystem','TileEntityRadioTorchBase','TileEntityRadioTorchSender','TileEntityRadioTorchReceiver','TileEntityRadioTorchLogic','TileEntityRadioTorchCounter','TileEntityRadioTorchReader','TileEntityRadioTorchController']]
production+=['com/hbm/util/'+n+'.java' for n in ['BufferUtil','Tuple','NoteBuilder']]
production+=['com/hbm/interfaces/'+n+'.java' for n in ['IControlReceiver','Spaghetti','NotableComments']]
production+=['api/hbm/redstoneoverradio/'+n+'.java' for n in ['IRORInteractive','IRORValueProvider','IRORInfo','RORFunctionException']]
production+=['com/hbm/module/ModulePatternMatcher.java','com/hbm/tileentity/IControlReceiverFilter.java','com/hbm/interfaces/ICopiable.java','com/hbm/util/Either.java']
# Compile the real owner predicates used by native custom-breaking tile callbacks.
owner_script=(a.module/'porting/tests/rtty-native/probe-owner-model.py').read_text()
owner_context={}
exec(owner_script[owner_script.index("production=["):owner_script.index("# Use the tile proof")],owner_context)
production=list(dict.fromkeys(production+owner_context['production']))
for relative,body in owner_context['fixtures'].items():collaborators.setdefault(relative,body)
for relative in production:collaborators.pop(relative,None)
with tempfile.TemporaryDirectory(prefix='radio-tile-native-') as tmp:
 out=Path(tmp);files=[]
 for name,body in collaborators.items():
  f=out/name;f.parent.mkdir(exist_ok=True,parents=True);f.write_text(body);files.append(str(f))
 files += [str(src/f) for f in production]
 test=out/'RadioTileNativeTest.java';test.write_text((tests/'tile-native-test.java.in').read_text());files.append(str(test))
 j=a.java_home/'bin';exe='.exe' if os.name=='nt' else ''
 subprocess.run([str(j/('javac'+exe)),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',str(out),'-cp',a.classpath,'-d',str(out)]+files,check=True)
 print('PASS all seven production radio tiles, RTTYSystem, actual filter interface, both complete native base classes, BufferUtil and ModulePatternMatcher compile against real Minecraft/Forge APIs.',flush=True)
 subprocess.run([str(j/('java'+exe)),'-Xmx1000m','-Djava.awt.headless=true','-cp',str(out)+os.pathsep+a.classpath,'RadioTileNativeTest'],cwd=out,check=True)
