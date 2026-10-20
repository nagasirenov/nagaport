#!/usr/bin/env python3
"""Full production capability lifecycle + player properties; extracted living properties except
setDigamma (unported achievements). Real MC/Forge/Netty and Forge event transformer.
Controlled NTM collaborators are listed below; this is NOT loaded NTM player/radiation acceptance.
"""
import argparse, hashlib, json, os, re, subprocess, tempfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2];SRC=ROOT/'src/main/java';TEST=ROOT/'porting/tests'
p=argparse.ArgumentParser(description=__doc__);p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);a=p.parse_args();exe='.exe' if os.name=='nt' else ''
if not (a.java_home/'bin'/('javac'+exe)).is_file() and (a.java_home.parent/'bin'/('javac'+exe)).is_file():a.java_home=a.java_home.parent
def method(text,signature):
 start=text.index(signature);end=text.index('{',start)+1;depth=1
 while depth: depth+=(text[end]=='{')-(text[end]=='}');end+=1
 return text[start:end]
baseline=json.loads((TEST/'entity-properties-source-baseline.json').read_text())
for name in ['HbmLivingProps','HbmPlayerProps']:
 current=(SRC/('com/hbm/extprop/'+name+'.java')).read_text();old=baseline[name]
 assert current[current.index('public static final String key'):current.index('public '+name+'(')]==old[old.index('public static final String key'):old.index('public '+name+'(')],(name,'field defaults changed')
 for signature in ['public void serialize(', 'public void deserialize(', 'public void saveNBTData(', 'public void loadNBTData(']:
  assert method(current,signature)==method(old,signature),(name,signature,'payload changed')
 if name=='HbmLivingProps':
  assert current[current.index('public static class ContaminationEffect'):]==old[old.index('public static class ContaminationEffect'):], 'original contamination codec quirks must stay unchanged'
  before=method(old,'public static void setDigamma(').replace('.worldObj','.world').replace('SharedMonsterAttributes.maxHealth','SharedMonsterAttributes.MAX_HEALTH').replace('Blocks.soul_sand','Blocks.SOUL_SAND')
  assert method(current,'public static void setDigamma(')==before,'achievement grants/formula must NOT be removed or replaced'
 for signature in re.findall(r'public (?:static )?(?:float|int|boolean|void|List<ContaminationEffect>|CBT_Atmosphere) (\w+)\(',old):
  if signature in ['init','setDigamma','plink','getEffectiveMaxShield','serialize','deserialize','saveNBTData','loadNBTData','save']:continue
  prefix=re.search(r'public (?:static )?(?:float|int|boolean|void|List<ContaminationEffect>|CBT_Atmosphere) '+signature+r'\(',old).group()
  expected=method(old,prefix).replace('.worldObj','.world').replace('MathHelper.clamp_int','MathHelper.clamp').replace('player.ridingEntity','player.getRidingEntity()')
  assert method(current,prefix)==expected,(name,prefix,'non-lifecycle body changed')
 assert 'IExtendedEntityProperties' not in current and 'getExtendedProperties' not in current
common=(SRC/'com/hbm/main/ModEventHandler.java').read_text();main=(SRC/'com/hbm/main/MainRegistry.java').read_text()
assert method(common,'public void onEntityDeath(')==baseline['onEntityDeath'], 'death handler changed'
assert 'void onPlayerClone(' not in common and 'HbmEntityProps.register();' in main
construct=method(common,'public void onEntityConstructing(')
assert 'getData(' not in construct and 'BlockAshes.ashes = 0;' in construct
# No actual direct or known radiation helper call was found before attachment in
# entity constructors/vanilla construction callbacks. This is a source guard,
# not a claim about arbitrary third-party calls to the public accessor.
for path in (SRC/'com/hbm/entity').rglob('*.java'):
 text=path.read_text();name=path.stem
 for match in re.finditer(r'\b(?:'+re.escape(name)+r'|entityInit|applyEntityAttributes)\s*\([^;{}]*\)\s*\{',text):
  end=match.end();depth=1
  while depth and end<len(text):depth+=(text[end]=='{')-(text[end]=='}');end+=1
  assert not re.search(r'HbmLivingProps|HbmPlayerProps|ContaminationUtil|EntityEffectHandler|HazardSystem',text[match.start():end]),('review early property access',str(path))
for path in (SRC/'com/hbm').rglob('*.java'):
 if path.name in ['HbmLivingProps.java','HbmPlayerProps.java']:continue
 text=path.read_text()
 assert not re.search(r'Hbm(?:Living|Player)Props\.registerData\(',text),('review external registerData caller',str(path))
 if 'EntityEvent.EntityConstructing' in text:assert path.name=='ModEventHandler.java',('review extra constructing subscriber',str(path))
print('PASS exact original living/player NBT + wire + contamination quirks, property formulas/toggles, unchanged death method and unresolved achievement calls; native registration/source guard',flush=True)
# No net.minecraft / net.minecraftforge replacement sources. Each NTM fixture is an explicit
# boundary; excluded digamma setter throws rather than pretending to award achievements.
collaborators={
'com/hbm/config/RadiationConfig.java':'''package com.hbm.config; public class RadiationConfig { public static boolean enableContamination=true,disableNeutron,disableAsbestos,disableCoal; }''',
'com/hbm/config/ServerConfig.java':'''package com.hbm.config; public class ServerConfig { public static final Flag ENABLE_MKU=new Flag(); public static class Flag {public boolean value=true; public boolean get(){return value;}} }''',
'com/hbm/dim/trait/CBT_Atmosphere.java':'package com.hbm.dim.trait; public class CBT_Atmosphere {}',
'com/hbm/entity/mob/EntityDuck.java':'package com.hbm.entity.mob; public class EntityDuck extends net.minecraft.entity.passive.EntityChicken {public EntityDuck(net.minecraft.world.World w){super(w);}}',
'com/hbm/entity/train/EntityRailCarBase.java':'''package com.hbm.entity.train; public abstract class EntityRailCarBase extends net.minecraft.entity.Entity {public EntityRailCarBase(net.minecraft.world.World w){super(w);}}''',
'com/hbm/handler/ArmorModHandler.java':'''package com.hbm.handler; public class ArmorModHandler {public static int kevlar=0; public static net.minecraft.item.ItemStack[] mods=new net.minecraft.item.ItemStack[1]; public static net.minecraft.item.ItemStack[] pryMods(net.minecraft.item.ItemStack s){return mods;}}''',
'com/hbm/items/armor/ItemModShield.java':'''package com.hbm.items.armor; public class ItemModShield extends net.minecraft.item.Item {public float shield;}''',
'com/hbm/lib/ModDamageSource.java':'''package com.hbm.lib; public class ModDamageSource {public static net.minecraft.util.DamageSource digamma=new net.minecraft.util.DamageSource("digamma"),asbestos=new net.minecraft.util.DamageSource("asbestos"),blacklung=new net.minecraft.util.DamageSource("blacklung"),oxyprime=new net.minecraft.util.DamageSource("oxyprime");}''',
'com/hbm/main/MainRegistry.java':'''package com.hbm.main; public class MainRegistry {public static final Object instance=new Object(); public static final Proxy proxy=new Proxy(); public static class Proxy {public int ID_GAS_HAZARD=1,ID_JETPACK=2,ID_MAGNET=3,ID_HUD=4; public void displayTooltip(String s,int i){} }}''',
'com/hbm/handler/threading/PacketThreading.java':'''package com.hbm.handler.threading; public class PacketThreading {public static void createAllAroundThreadedPacket(Object p,net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint t){throw new UnsupportedOperationException("packet integration outside scope");}}''',
'com/hbm/packet/PacketDispatcher.java':'''package com.hbm.packet; public class PacketDispatcher {public static final Wrapper wrapper=new Wrapper();public static class Wrapper {public void sendTo(Object p,net.minecraft.entity.player.EntityPlayerMP e){throw new UnsupportedOperationException("packet integration outside scope");}}}''',
'com/hbm/packet/toclient/AuxParticlePacketNT.java':'''package com.hbm.packet.toclient; public class AuxParticlePacketNT {public AuxParticlePacketNT(net.minecraft.nbt.NBTTagCompound t,double x,double y,double z){}}''',
'com/hbm/packet/toclient/PlayerInformPacket.java':'''package com.hbm.packet.toclient; public class PlayerInformPacket {public PlayerInformPacket(Object t,int id,int life){}}''',
'com/hbm/util/ChatBuilder.java':'''package com.hbm.util; public class ChatBuilder {public static ChatBuilder start(String s){return new ChatBuilder();} public ChatBuilder nextTranslation(String s){return this;} public ChatBuilder color(net.minecraft.util.text.TextFormatting f){return this;} public Object flush(){return this;}}'''
}
keys=(SRC/'com/hbm/handler/HbmKeybinds.java').read_text();enum=method(keys,'public static enum EnumKeybind')
collaborators['com/hbm/handler/HbmKeybinds.java']='package com.hbm.handler; public class HbmKeybinds { '+enum+' }'
living=(SRC/'com/hbm/extprop/HbmLivingProps.java').read_text()
collaborators['com/hbm/extprop/HbmLivingProps.java']=living.replace(method(living,'public static void setDigamma('),'public static void setDigamma(EntityLivingBase entity, float digamma) { throw new UnsupportedOperationException("Excluded unported achievement/digamma integration"); }')
production=['com/hbm/extprop/HbmEntityProps.java','com/hbm/extprop/HbmPlayerProps.java','com/hbm/tileentity/IGUIProvider.java']
with tempfile.TemporaryDirectory(prefix='hbm-entity-properties-') as directory:
 out=Path(directory);files=[]
 for relative,source in collaborators.items():
  path=out/'fixture-src'/relative;path.parent.mkdir(parents=True,exist_ok=True);path.write_text(source);files.append(str(path))
 for relative in production:
  path=SRC/relative;files.append(str(path));print('FULL PRODUCTION',relative,hashlib.sha256(path.read_bytes()).hexdigest(),flush=True)
 print('EXTRACTED living properties: exact production class except setDigamma excluded; no whole-class achievement/gameplay acceptance',flush=True)
 files += [str(TEST/'EntityPropertiesNativeTest.java'),str(TEST/'ForgeEventNativeLauncher.java')]
 subprocess.run([str(a.java_home/'bin'/('javac'+exe)),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',directory]+files,check=True)
 cp=directory+os.pathsep+a.classpath
 subprocess.run([str(a.java_home/'bin'/('java'+exe)),'-cp',cp,'ForgeEventNativeLauncher','EntityPropertiesNativeTest'],check=True,cwd=directory)
