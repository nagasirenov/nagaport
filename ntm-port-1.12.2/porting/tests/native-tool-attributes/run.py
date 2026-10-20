#!/usr/bin/env python3
"""Exact complete attribute-method projections using real native Item/Sword/Tool/ItemStack."""
from pathlib import Path
import re,subprocess,tempfile,json,argparse
import migrate,check_source
check_source.main()
H=Path(__file__).resolve().parent;R=H.parents[2];J=Path('/workspace/shared/nagaport-toolchain/java8');CP=Path('/workspace/shared/nagaport-toolchain/native-classpath.txt').read_text().strip()
def source(n):return (R/migrate.path(n)).read_text()
def method(s,name):
 m=re.search(r'public (?:static )?(?:[\w<>]+ )?'+name+r'\([^)]*\)\s*\{',s);assert m,name
 end=m.end();depth=1
 while depth:
  if s[end]=='{':depth+=1
  if s[end]=='}':depth-=1
  end+=1
 return s[m.start():end]
code=['''import java.util.*;import com.google.common.collect.*;import net.minecraft.item.*;import net.minecraft.entity.*;import net.minecraft.entity.ai.attributes.*;import net.minecraft.inventory.EntityEquipmentSlot;import net.minecraft.block.Block;
public class ProductionToolProjections {
 static class ModItems { static Item wrench,schrabidium_hammer,shimmer_sledge,shimmer_axe,wrench_flipped; }
''']
original=json.loads((H/'engine/original-payloads.json').read_text());original={k:v for k,v in original.items() if isinstance(v,list)};assert len(original)==6
code.append('static final String[] ORIGINAL_NAMES = {'+','.join(json.dumps(k) for k in original)+'};')
code.append('static final String[][][] ORIGINAL_PAYLOADS = {'+','.join('{'+','.join('{'+','.join(json.dumps(str(row[k])) for k in ('attribute','uuid','name','amount','operation'))+'}' for row in rows)+'}' for rows in original.values())+'};')
for n in migrate.OWNERS:
 stack=n in ('ItemMirrorTool','ItemRBMKTool','ItemToolingWeapon','ItemCrucible');body=method(source(n),'getAttributeModifiers' if stack else 'getItemAttributeModifiers')
 base='ItemSword' if n in ('ItemSwordAbility','WeaponSpecial') else 'ItemTool' if n=='ItemToolAbility' else 'ItemSwordAbility' if n=='ItemCrucible' else 'Item'
 ctor='';fields=''
 if n in ('ItemSwordAbility','ItemToolAbility','ItemCrucible'):
  if n!='ItemCrucible':fields='protected float damage;protected double movement;'
  if n=='ItemToolAbility':
   # Complete real constructor, with only its class-local type/field scaffolding supplied by test projections.
   fields+='EnumToolType toolType; public enum EnumToolType {MINER,PICKAXE,AXE,SHOVEL; public Set<Block> blocks=new HashSet<>();}'
   ctor=method(source(n),n)
  else:ctor=method(source(n),n)
  if n=='ItemCrucible':
   # Its real constructor also sets renderer and max charge; this suite only projects the complete attribute method.
   ctor='public ItemCrucible(float damage,double movement,ToolMaterial material){super(damage,movement,material);}'
 elif n=='WeaponSpecial':ctor=method(source(n),n)
 elif n=='ItemToolingWeapon':fields='protected float damage;public ItemToolingWeapon(float f){damage=f;}'
 code.append('public static class '+n+' extends '+base+' {'+fields+ctor+'\n'+body+'}')
# The exact held-stack presence test is supplied its existing nullable main-hand result, not a fake EntityPlayer.
s=(R/migrate.EVENT).read_text();expr=re.search(r'if\((com\.hbm\.util\.NativeHeldItem\.getMainHandNullable\(player\) != null && com\.hbm\.util\.NativeHeldItem\.getMainHandNullable\(player\)\.getAttributeModifiers\([^;]+)\) return;',s).group(1)
expr=expr.replace('com.hbm.util.NativeHeldItem.getMainHandNullable(player)','held')
code.append('public static boolean gauntletHeldCheck(ItemStack held){return '+expr+';}')
code.append('}')
text='\n'.join(code);(H/'generated-projections.java.txt').write_text(text)
(H/'runtime-scope.json').write_text(json.dumps({'complete_method_projections':list(migrate.OWNERS),'complete_constructor_projections':['ItemSwordAbility','ItemToolAbility','WeaponSpecial'],'expression_projection':'ModEventHandler ballistic-gauntlet held-stack query, nullable held input substituted for existing NativeHeldItem call','test_only_dependencies':['ModItems item identity holder','ItemToolAbility local EnumToolType blocks/type scaffold','ItemToolingWeapon no-attribute ancestor collapsed to real Item','Crucible test constructor, unrelated charge/renderer setup excluded'],'no_claim':['complete owners','world/player combat cooldown gameplay','renderers or block interaction','full mod build']},indent=2)+'\n')
mutants={
 'ability-blind-super':('Multimap multimap = HashMultimap.create();','Multimap multimap = super.getItemAttributeModifiers(slot);'),
 'custom-mainhand-only':('if(slot == net.minecraft.inventory.EntityEquipmentSlot.OFFHAND)','if(slot != net.minecraft.inventory.EntityEquipmentSlot.MAINHAND)'),
 'new-offhand-effects':('if(slot == net.minecraft.inventory.EntityEquipmentSlot.OFFHAND) return multimap;',''),
 'special-drop-original-baseline':('multimap.remove(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), damage);',''),
 'special-after-rounded-plus-one':('(double) (4.0F + super.getAttackDamage())','(double) (3.0F + super.getAttackDamage()) + 1.0D'),
 'special-double-evaluation':('(double) (4.0F + super.getAttackDamage())','4.0D + super.getAttackDamage()'),
 'special-clear-all-attributes':('multimap.remove(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), damage);','multimap.clear();'),
 'crucible-depleted-damage':('if(stack.getItemDamage() < stack.getMaxDamage())','if(true)'),
 'wrong-damage-uuid':('new AttributeModifier(ATTACK_DAMAGE_MODIFIER,','new AttributeModifier(ATTACK_SPEED_MODIFIER,'),
 'gauntlet-offhand-query':('held.getAttributeModifiers(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND)','held.getAttributeModifiers(net.minecraft.inventory.EntityEquipmentSlot.OFFHAND)'),
}
p=argparse.ArgumentParser();p.add_argument('--mutations',action='store_true');a=p.parse_args()
with tempfile.TemporaryDirectory(prefix='native-tool-attributes-') as tmp:
 t=Path(tmp);src=t/'ProductionToolProjections.java';src.write_text(text)
 def run():
  c=subprocess.run([str(J/'bin/javac'),'-encoding','UTF-8','-cp',CP,'-d',str(t),str(src),str(H/'NativeToolAttributeTest.java')],capture_output=True,text=True)
  if c.returncode:raise AssertionError(c.stdout+c.stderr)
  return subprocess.run([str(J/'bin/java'),'-cp',str(t)+':'+CP,'NativeToolAttributeTest'],capture_output=True,text=True)
 res=run();print(res.stdout);assert res.returncode==0,res.stderr
 if a.mutations:
  for name,(old,new) in mutants.items():
   assert old in text,name;src.write_text(text.replace(old,new));res=run();assert res.returncode!=0,'SURVIVED '+name
   print('KILLED',name,next((x.strip() for x in res.stderr.splitlines() if 'AssertionError:' in x),'runtime failure'))
