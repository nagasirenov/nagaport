#!/usr/bin/env python3
"""Capture exact, bounded bytecode/patch evidence from already cached jars."""
from pathlib import Path
import hashlib,io,json,re,subprocess,zipfile
HERE=Path(__file__).resolve().parent
JP=Path('/workspace/shared/nagaport-toolchain/java8/bin/javap')
OLD=Path('/tmp/nagaport-legacy-client.jar')
MOD=Path('/workspace/shared/nagaport/mods1710/HBM-NTM-.1.0.27_X5645_H261.jar')
FORGE=Path('/tmp/nagaport-forge1614-userdev.jar')
NATIVE=Path(next(p for p in Path('/workspace/shared/nagaport-toolchain/native-classpath.txt').read_text().strip().split(':') if '/forgeBin-' in p))
def javap(jar,owner):
 return subprocess.check_output([str(JP),'-cp',str(jar),'-c','-p',owner],text=True)
def methods(text,starts):
 lines=text.splitlines();result=[]
 for start in starts:
  first=next(i for i,l in enumerate(lines) if l.startswith(start));last=first+1
  while last<len(lines) and not re.match(r'^  (?:public |protected |private |static \{)',lines[last]):last+=1
  result.append('// javap lines %d-%d\n%s'%(first+1,last,'\n'.join(lines[first:last])))
 return '\n\n'.join(result)
def excerpt(text,key,before,after):
 lines=text.splitlines();i=next(i for i,l in enumerate(lines) if key in l);lo=max(0,i-before);hi=min(len(lines),i+after)
 return '// javap lines %d-%d\n%s'%(lo+1,hi,'\n'.join(lines[lo:hi]))
engine=[]
for jar,owner,starts in [
 (OLD,'adb',['  public com.google.common.collect.Multimap k();']),
 (OLD,'aeh',['  public aeh(adc);','  public float i();','  public com.google.common.collect.Multimap k();']),
 (OLD,'acg',['  protected acg(float, adc, java.util.Set);','  public com.google.common.collect.Multimap k();']),
 (OLD,'tj',['  public boolean equals(java.lang.Object);','  public int hashCode();']),
 (OLD,'add',['  public com.google.common.collect.Multimap D();']),
 (NATIVE,'net.minecraft.item.Item',['  public com.google.common.collect.Multimap<java.lang.String, net.minecraft.entity.ai.attributes.AttributeModifier> getItemAttributeModifiers(','  public com.google.common.collect.Multimap<java.lang.String, net.minecraft.entity.ai.attributes.AttributeModifier> getAttributeModifiers(']),
 (NATIVE,'net.minecraft.item.ItemSword',['  public net.minecraft.item.ItemSword(','  public float getAttackDamage();','  public com.google.common.collect.Multimap<java.lang.String, net.minecraft.entity.ai.attributes.AttributeModifier> getItemAttributeModifiers(']),
 (NATIVE,'net.minecraft.item.ItemTool',['  protected net.minecraft.item.ItemTool(float, float,','  protected net.minecraft.item.ItemTool(net.minecraft.item.Item$ToolMaterial,','  public com.google.common.collect.Multimap<java.lang.String, net.minecraft.entity.ai.attributes.AttributeModifier> getItemAttributeModifiers(']),
 (NATIVE,'net.minecraft.entity.ai.attributes.AttributeModifier',['  public boolean equals(java.lang.Object);','  public int hashCode();']),
 (NATIVE,'net.minecraft.item.ItemStack',['  public com.google.common.collect.Multimap<java.lang.String, net.minecraft.entity.ai.attributes.AttributeModifier> getAttributeModifiers(']),
]:
 engine.append('// SOURCE '+str(jar)+' :: '+owner+'\n'+methods(javap(jar,owner),starts))
old_equipment=methods(javap(OLD,'sv'),['  public void h();'])
old_equipment=old_equipment[:old_equipment.index('     198: aload_0')]
engine.append('// SOURCE '+str(OLD)+' :: EntityLivingBase.onUpdate equipment range 0 <= i < 5\n'+old_equipment)
engine.append('// SOURCE '+str(OLD)+' :: EntityPlayer.getEquipmentInSlot maps 0 to held and 1..4 to armor\n'+methods(javap(OLD,'yz'),['  public add q(int);']))
(HERE/'engine-methods.excerpts.txt').write_text('\n\n'.join(engine)+'\n')
original=[]
for owner in ['com.hbm.items.tool.'+n for n in ['ItemMirrorTool','ItemRBMKTool','ItemSwordAbility','ItemToolAbility','ItemToolingWeapon','WeaponSpecial']]+['com.hbm.items.weapon.GunB92','com.hbm.items.weapon.ItemCrucible']:
 text=javap(MOD,owner);short=owner.rsplit('.',1)[1]
 starts=['  public com.google.common.collect.Multimap ']
 if short in ('ItemSwordAbility','ItemToolAbility','ItemToolingWeapon','WeaponSpecial','ItemCrucible'):starts.insert(0,'  public '+owner+'(')
 if short=='ItemToolAbility':starts.append('  static {};')
 original.append('// SOURCE '+str(MOD)+' :: '+owner+'\n'+methods(text,starts))
for owner in ['com.hbm.items.tool.ItemTooling','com.hbm.items.tool.ItemCraftingDegradation']:
 text=subprocess.check_output([str(JP),'-cp',str(MOD),'-p',owner],text=True)
 original.append('// SOURCE '+str(MOD)+' :: complete declaration '+owner+'\n'+text)
(HERE/'original-custom-methods.excerpts.txt').write_text('\n\n'.join(original)+'\n')
# Original registration bytecode values prove the damage fields are not material damage aliases.
registry=javap(MOD,'com.hbm.main.MainRegistry');items=javap(MOD,'com.hbm.items.ModItems');registration=[]
for name in ['SCHRABIDIUM','SCHRABIDIUMHAMMER','HBM_STEEL','HBM_TITANIUM','HBM_CMB','SHIMMERSLEDGE','OPENER']:
 registration.append('// SOURCE original MainRegistry\n'+excerpt(registry,'// String '+name,0,13))
for name in ['crucible','schrabidium_sword','schrabidium_pickaxe','titanium_sword','cmb_sword','shimmer_sledge','bottle_opener','wrench_flipped','mese_gavel']:
 registration.append('// SOURCE original ModItems\n'+excerpt(items,'// String '+name,30,8))
(HERE/'original-registration.excerpts.txt').write_text('\n\n'.join(registration)+'\n')
with zipfile.ZipFile(FORGE) as forge:
 mappings=forge.read('conf/packaged.srg').decode().splitlines()
 keys=['func_111205_h','func_111283_C','func_150931_i','func_77964_b','func_77656_e','field_111210_e','func_111167_a','func_111166_b','func_111164_d','func_111169_c','ItemSword','ItemTool','Item$ToolMaterial']
 (HERE/'old-mappings.txt').write_text('\n'.join(l for l in mappings if any(k in l for k in keys))+'\n')
 with zipfile.ZipFile(io.BytesIO(forge.read('forgepatches.zip'))) as patches:
  patchparts=[]
  for owner,key,b,a in [('Item','public Multimap getAttributeModifiers',8,8),('ItemStack','getAttributeModifiers(this)',8,5),('Block','captureDrops(boolean',8,26)]:
   path='net/minecraft/'+('block/' if owner=='Block' else 'item/')+owner+'.java.patch'
   patch=patches.read(path).decode();lines=patch.splitlines();i=next(i for i,l in enumerate(lines) if key in l);lo=max(0,i-b);hi=min(len(lines),i+a)
   patchparts.append('// SOURCE '+str(FORGE)+'!/forgepatches.zip!/'+path+' lines '+str(lo+1)+'-'+str(hi)+'\n'+'\n'.join(lines[lo:hi]))
  (HERE/'forge-hooks.excerpts.txt').write_text('\n\n'.join(patchparts)+'\n')
(HERE/'inputs.sha256.json').write_text(json.dumps([{'path':str(p),'resolved_path':str(p.resolve()),'sha256':hashlib.sha256(p.read_bytes()).hexdigest()} for p in [OLD,MOD,FORGE,NATIVE]],indent=2)+'\n')
print('Captured exact old/native Item/Sword/Tool/Stack/AttributeModifier bytecode, all eight original hooks, original constructors and registrations, and actual Forge bridge/captureDrops patches.')
