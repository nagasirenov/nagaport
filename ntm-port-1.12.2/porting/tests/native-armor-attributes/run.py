#!/usr/bin/env python3
"""Complete attribute method projections, exact speed slices, actual native dispatch/maps."""
from pathlib import Path
import re,subprocess,tempfile,json,argparse
import migrate,check_source
H=Path(__file__).resolve().parent;R=H.parents[2]
check_source.main()
J=Path('/workspace/shared/nagaport-toolchain/java8');CP=Path('/workspace/shared/nagaport-toolchain/native-classpath.txt').read_text().strip()
def source(n):return (R/migrate.PREFIX/(n+'.java')).read_text()
def method(s,name):
 m=re.search(r'public (?:static )?\w+ '+name+r'\([^)]*\)\s*\{',s);assert m,name
 end=m.end();depth=1
 while depth:
  if s[end]=='{':depth+=1
  if s[end]=='}':depth-=1
  end+=1
 return s[m.start():end]
imports='''import java.util.*;import com.google.common.collect.*;import net.minecraft.item.*;import net.minecraft.entity.*;import net.minecraft.entity.ai.attributes.*;import net.minecraft.inventory.EntityEquipmentSlot;import com.hbm.util.ArmorSlotOrder;'''
code=[imports,'public class ProductionAttributeProjections {']
original=json.loads((H/'engine/original-payloads.json').read_text());assert len(original)==16
code.append('static final String[][] ORIGINAL = new String[][] {'+','.join('{'+','.join(json.dumps(str(row[k])) for k in ('owner','armor_type','attribute','uuid','name','amount','operation'))+'}' for row in original)+'};')
handler=(R/'src/main/java/com/hbm/handler/ArmorModHandler.java').read_text()
code.append('static class ArmorModHandler {'+'\n'.join(re.findall(r'public static final UUID\[\] (?:fixedUUIDs|UUIDs) = new UUID\[\] \{.*?\};',handler,re.S))+'}')
code.append('static class ModItems { static Item servo_set,servo_set_desh,ncrpa_plate,envsuit_plate,dns_plate; }')
code.append('static class ItemArmorMod extends Item { '+method(source('ItemArmorMod'),'getModifiers')+' }')
for n in migrate.OVERRIDES:
 code.append('public static class '+n+' extends ItemArmor { public '+n+'(ArmorMaterial m,int i){super(m,0,ArmorSlotOrder.fromIndex(i));}\n'+method(source(n),'getItemAttributeModifiers')+'}')
for n in migrate.MANUAL:
 if n.startswith('Item'):
  field={'ItemModHealth':'float health; public ItemModHealth(float f){health=f;}','ItemModInsert':'float speed; public ItemModInsert(float f){speed=f;}'}.get(n,'')
  if n=='ItemModV1':field=re.search(r'private static final UUID speed = [^;]+;',source(n)).group()
  code.append('public static class '+n+' extends ItemArmorMod { '+field+method(source(n),'getModifiers')+'}')
 else:
  s=source(n);gate=re.search(r'if\(this != ModItems\.\w+\)\s*return;',s).group()
  start=s.index('Multimap multimap = super.getAttributeModifiers(')
  end=s.index('if(player.isInWater())',start) if n=='ArmorEnvsuit' else s.index('if(this.hasFSBArmor(player))',start)
  block=s[start:end]
  if n=='ArmorEnvsuit':block+='}' # closes original full-set predicate after exact sprint body
  field=re.search(r'private static final UUID speed = [^;]+;',s).group()
  code.append('public static class '+n+' extends ItemArmor { public '+n+'(){super(ArmorMaterial.DIAMOND,0,EntityEquipmentSlot.CHEST);} '+field+' boolean hasFSBArmor(SpeedContext player){return player.fullSet;} public void speedSlice(SpeedContext player,ItemStack stack){'+gate+block+'}}')
code.append('public static class SpeedContext { final AbstractAttributeMap map; boolean sprint,fullSet; public SpeedContext(AbstractAttributeMap m){map=m;} public AbstractAttributeMap getAttributeMap(){return map;} public boolean isSprinting(){return sprint;} }')
code.append('}')
text='\n'.join(code)
(H/'generated-projections.java.txt').write_text(text)
(H/'runtime-scope.json').write_text(json.dumps({'complete_method_projections':migrate.OVERRIDES+[n for n in migrate.MANUAL if n.startswith('Item')],'speed_slices':[n for n in migrate.MANUAL if n.startswith('Armor')],'complete_production_class':['com.hbm.util.ArmorSlotOrder'],'test_only_context':['ModItems identity holder','ArmorModHandler exact UUID table projection','SpeedContext supplies sprint/full-set booleans and actual native attribute map'],'not_claimed':['full armor constructors/inheritance game boot','complete speed onArmorTick or potion/flight/water mechanics','world/entity equipment tick integration']},indent=2)+'\n')
mutants={
 'custom-own-slot-only':('if(slot == net.minecraft.inventory.EntityEquipmentSlot.OFFHAND)', 'if(slot != this.armorType)'),
 'custom-new-offhand':('if(slot == net.minecraft.inventory.EntityEquipmentSlot.OFFHAND) return multimap;',''),
 'drop-native-protection':('Multimap multimap = super.getItemAttributeModifiers(slot);','Multimap multimap = HashMultimap.create();'),
 'wrong-custom-uuid-index':('ArmorModHandler.fixedUUIDs[com.hbm.util.ArmorSlotOrder.index(this.armorType)]','ArmorModHandler.fixedUUIDs[this.armorType.getIndex()]'),
 'wrong-desh-amount':('"Armor modifier", -0.025D, 1','"Armor modifier", -0.25D, 1'),
 'manual-worn-protection':('super.getAttributeModifiers(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, stack)','super.getAttributeModifiers(net.minecraft.inventory.EntityEquipmentSlot.CHEST, stack)'),
 'manual-bypass-virtual-bridge':('super.getAttributeModifiers(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, armor)','HashMultimap.create()'),
 'insert-null-contract':('if(speed == 1)\n\t\t\treturn null;','if(speed == 1)\n\t\t\treturn HashMultimap.create();'),
 'speed-wrong-sprint-gate':('if(player.isSprinting())','if(!player.isSprinting())'),
 'envsuit-fullset-gate':('if(this.hasFSBArmor(player))','if(true)'),
 'mod-uuid-native-index':('ArmorModHandler.UUIDs[com.hbm.util.ArmorSlotOrder.index(((ItemArmor)armor.getItem()).armorType)]','ArmorModHandler.UUIDs[((ItemArmor)armor.getItem()).armorType.getIndex()]'),
}
p=argparse.ArgumentParser();p.add_argument('--mutations',action='store_true');a=p.parse_args()
with tempfile.TemporaryDirectory(prefix='native-armor-attributes-') as tmp:
 t=Path(tmp);src=t/'ProductionAttributeProjections.java';src.write_text(text)
 files=[src,H/'NativeArmorAttributeTest.java',R/'src/main/java/com/hbm/util/ArmorSlotOrder.java']
 def run():
  c=subprocess.run([str(J/'bin/javac'),'-encoding','UTF-8','-cp',CP,'-d',str(t)]+[str(f) for f in files],capture_output=True,text=True)
  if c.returncode:raise AssertionError(c.stdout+c.stderr)
  return subprocess.run([str(J/'bin/java'),'-cp',str(t)+':'+CP,'NativeArmorAttributeTest'],capture_output=True,text=True)
 res=run();print(res.stdout);assert res.returncode==0,res.stderr
 if a.mutations:
  for name,(old,new) in mutants.items():
   assert old in text,name
   src.write_text(text.replace(old,new))
   res=run();assert res.returncode!=0,'SURVIVED '+name
   print('KILLED',name, next((l.strip() for l in res.stderr.splitlines() if 'AssertionError:' in l),'runtime failure'))
