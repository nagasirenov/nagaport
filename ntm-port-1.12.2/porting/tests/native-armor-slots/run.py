#!/usr/bin/env python3
"""Exact callback/constructor projections plus real complete representative armor/model owners."""
from pathlib import Path
import argparse,subprocess,tempfile,json,re,hashlib,os
import check_source
H=Path(__file__).resolve().parent;R=H.parents[2];D=check_source.D
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,default=Path('/workspace/shared/nagaport-toolchain/java8'));p.add_argument('--classpath-file',type=Path,default=Path('/workspace/shared/nagaport-toolchain/native-classpath.txt'));a=p.parse_args();J=a.java_home;CP=a.classpath_file.read_text().strip()
check_source.main()
def source(f):return (R/f).read_bytes().decode()
def method(s,name):
 m=re.search(r'public (?:static )?(?:[\w<>]+ )?'+re.escape(name)+r'\([^)]*\)\s*\{',s);assert m,name
 start=m.start();end=m.end();depth=1
 while depth:
  if s[end]=='{':depth+=1
  elif s[end]=='}':depth-=1
  end+=1
 return s[start:end]
hooks={Path(f).stem:[e for e in row['edits'] if e.get('kind')=='hook-declaration'] for f,row in D['files'].items()};hooks={k:v for k,v in hooks.items() if v}
allhooks='\n'.join(e['new'] for es in hooks.values() for e in es)
itemnames=sorted(set(re.findall(r'ModItems\.(\w+)',allhooks)))
modelnames=sorted(set(re.findall(r'new (Model\w+)',allhooks)))
imports='''import java.util.*; import java.lang.reflect.*; import net.minecraft.item.*; import net.minecraft.inventory.EntityEquipmentSlot; import net.minecraft.entity.*; import net.minecraft.client.model.ModelBiped; import net.minecraft.entity.ai.attributes.AttributeModifier; import net.minecraft.util.ResourceLocation; import net.minecraftforge.fml.relauncher.*; import com.hbm.util.ArmorSlotOrder; import com.hbm.items.ModItems; import com.hbm.main.ResourceManager; import com.hbm.lib.RefStrings; import com.hbm.handler.ArmorModHandler; import com.hbm.render.model.ModelGlasses; import com.hbm.render.model.ModelArmorHEV; import com.hbm.render.item.NativeItemTextureRegistry;'''
code=[imports,'public class ProductionSlotProjections {',
'''public static class LegacyBase extends ItemArmor {
 public LegacyBase(){super(ArmorMaterial.IRON,0,EntityEquipmentSlot.HEAD);}
 public ModelBiped getArmorModel(EntityLivingBase e,ItemStack s,int slot){return null;}
 public String getArmorTexture(ItemStack s,Entity e,int slot,String type){return null;}
 public boolean isValidArmor(ItemStack s,int slot,Entity e){return false;}
}
public static class NativeBase extends ItemArmor { public NativeBase(){super(ArmorMaterial.IRON,0,EntityEquipmentSlot.HEAD);} }
public static class ProbeModel extends ModelBiped { public final int type; public ProbeModel(int type){this.type=type;} }
''']
for name in modelnames:
 if name not in ('ModelGlasses','ModelArmorHEV'):code.append('public static class '+name+' extends ProbeModel { public '+name+'(){super(0);} public '+name+'(int type){super(type);} }')
for name,es in hooks.items():
 for mode in ('Native','Legacy'):
  bodies='\n'.join(e['new' if mode=='Native' else 'old'] for e in es)
  declarations={}
  for var,model,arr in re.findall(r'(\w+)\s*=\s*new (Model\w+)(\[4\])?',bodies):declarations[var]=(model+'[]' if arr else 'ModelBiped')
  fields='\n'.join(t+' '+n+';' for n,t in sorted(declarations.items()))+'\nString texture="hbm:test/worn";'
  code.append('public static class '+name+mode+' extends '+mode+'Base {\n'+fields+'\n'+bodies+'\n}')
# Exact complete constructor bodies, with only the Java class name renamed for the projection.
ctors=[]
for f,row in D['files'].items():
 if not any(e.get('kind')=='call' and e['old'].startswith('super(') for e in row['edits']):continue
 name=Path(f).stem;body=method(source(f),name);projection='Ctor'+name;body=body.replace('public '+name+'(','public '+projection+'(',1)
 code.append('public static class '+projection+' extends ItemArmor { String texture; '+body+' }');ctors.append(projection)
# Exact complete radiation method + real Pair class. Native armor slot is selected by actual native ctor.
fsb='src/main/java/com/hbm/items/armor/ArmorFSB.java'
rad=method(source(fsb),'setRadResist')
code.append('''public static class HazmatRegistry { public static java.util.List<com.hbm.util.Tuple.Pair> external=new ArrayList<>();'''+''.join(re.findall(r'public static double (?:helmet|chest|legs|boots) = [^;]+;',source('src/main/java/com/hbm/handler/HazmatRegistry.java')))+'}')
code.append('public static class ArmorFSB extends ItemArmor { public double radResist; public ArmorFSB(int slot){super(ArmorMaterial.IRON,0,ArmorSlotOrder.fromIndex(slot));}\n'+rad+'}')
# Preserve complete AttributeModifier constructors and all UUID table values.
modexpr=[]
for f,row in D['files'].items():
 s=source(f)
 # Balanced parentheses extraction for each modifier using the converted field.
 for m in re.finditer(r'new AttributeModifier\(ArmorModHandler\.(?:fixedUUIDs|UUIDs)\[',s):
  start=m.start();end=m.end();depth=1
  while depth:
   if s[end]=='(':depth+=1
   elif s[end]==')':depth-=1
   end+=1
  expr=s[start:end]
  if 'ArmorSlotOrder.index' in expr:modexpr.append(dict(file=f,expression=expr))
code.append('public static class Modifiers extends ItemArmor { public Modifiers(int slot){super(ArmorMaterial.IRON,0,ArmorSlotOrder.fromIndex(slot));} public AttributeModifier get(int which,ItemStack armor,float health,float speed){switch(which){'+''.join('case '+str(i)+': return '+e['expression']+';' for i,e in enumerate(modexpr))+'default:throw new AssertionError();}} }')
assert source('src/main/java/com/hbm/inventory/container/ContainerArmorTable.java').count('inventory.mainInventory.size() + 3 - i')==1
code.append('public static int armorTableIndex(net.minecraft.entity.player.InventoryPlayer inventory,int i){return inventory.mainInventory.size() + 3 - i;}')
code.append('}')
text='\n'.join(code).replace('new Pair(', 'new com.hbm.util.Tuple.Pair(')
(H/'generated-projections.java.txt').write_text(text)
(H/'runtime-scope.json').write_text(json.dumps(dict(hooks={k:[e['element'] for e in v] for k,v in hooks.items()},item_fields=itemnames,model_dependencies=modelnames,constructor_projections=ctors,attribute_expressions=modexpr,complete_production_owners=['ArmorAshGlasses','MaskOfInfamy','ModArmor','ArmorModHandler'],complete_representative_models=['ModelGlasses','ModelArmorHEV','ModelArmorBase','ModelRendererObj']),indent=2)+'\n')
with tempfile.TemporaryDirectory(prefix='native-armor-slots-') as tmp:
 t=Path(tmp);files=[]
 def add(name,text):
  f=t/name;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(text);files.append(f)
 add('ProductionSlotProjections.java',text)
 add('com/hbm/items/ModItems.java','package com.hbm.items; public class ModItems { '+''.join('public static net.minecraft.item.Item '+x+' = new net.minecraft.item.Item();' for x in itemnames)+' }')
 add('com/hbm/items/armor/ItemArmorMod.java','''package com.hbm.items.armor; public class ItemArmorMod extends net.minecraft.item.Item { public int type; public boolean helmet,chestplate,leggings,boots; public ItemArmorMod(int mask){helmet=(mask&1)!=0;chestplate=(mask&2)!=0;leggings=(mask&4)!=0;boots=(mask&8)!=0;} }''')
 add('com/hbm/main/ResourceManager.java','''package com.hbm.main; public class ResourceManager { public static final com.hbm.render.mesh.MeshModel armor_goggles=null,armor_bj=null,armor_hev=null; public static final net.minecraft.util.ResourceLocation goggles=new net.minecraft.util.ResourceLocation("test:goggles"),hev_helmet=new net.minecraft.util.ResourceLocation("test:head"),hev_chest=new net.minecraft.util.ResourceLocation("test:chest"),hev_arm=new net.minecraft.util.ResourceLocation("test:arm"),hev_leg=new net.minecraft.util.ResourceLocation("test:leg"),wings_murk=new net.minecraft.util.ResourceLocation("test:wings"); }''')
 full=['com/hbm/util/ArmorSlotOrder.java','com/hbm/util/NullableInventoryStackNbt.java','com/hbm/util/Tuple.java','com/hbm/interfaces/Spaghetti.java','com/hbm/lib/RefStrings.java','com/hbm/items/armor/ArmorAshGlasses.java','com/hbm/items/armor/MaskOfInfamy.java','com/hbm/items/armor/ModArmor.java','com/hbm/handler/ArmorModHandler.java','com/hbm/render/model/ModelArmorBase.java','com/hbm/render/model/ModelGlasses.java','com/hbm/render/model/ModelArmorHEV.java','com/hbm/render/loader/ModelRendererObj.java','com/hbm/render/mesh/MeshModel.java','com/hbm/render/mesh/ObjMesh.java','com/hbm/render/item/NativeItemTextureRegistry.java']
 files += [R/'src/main/java'/f for f in full]+[H/'NativeArmorSlotsTest.java']
 subprocess.run([str(J/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-cp',CP,'-d',tmp]+list(map(str,files)),check=True)
 subprocess.run([str(J/'bin/java'),'-Djava.awt.headless=true','-cp',tmp+os.pathsep+CP,'NativeArmorSlotsTest',str(H/'runtime-scope.json')],check=True,cwd=tmp)
 mutations={
  'table-offhand-anchor':text.replace('return inventory.mainInventory.size() + 3 - i;','return inventory.getSizeInventory() - 1 - i;',1),
  'native-inventory-index-in-model-array':text.replace('models[com.hbm.util.ArmorSlotOrder.index(armorSlot)]','models[armorSlot.getIndex()]',1),
  'boots-valid-in-legs':text.replace('return armorType == net.minecraft.inventory.EntityEquipmentSlot.FEET;','return armorType == net.minecraft.inventory.EntityEquipmentSlot.LEGS;',1),
  'radiation-head-reversed':text.replace('double mult = com.hbm.util.ArmorSlotOrder.index(armorType) == 0','double mult = com.hbm.util.ArmorSlotOrder.index(armorType) == 3',1),
  'uuid-inventory-index':text.replace('com.hbm.util.ArmorSlotOrder.index(((ItemArmor)armor.getItem()).armorType)','((ItemArmor)armor.getItem()).armorType.getIndex()',1),
 }
 first=text.index('public static class ArmorAJRNative');annotation=text.index('@Override',first);decl=text.index('getArmorModel(',annotation)
 mutations['lost-native-virtual-hook']=text[:annotation]+text[annotation:decl].replace('@Override','',1)+'unusedArmorModel('+text[decl+len('getArmorModel('):]
 mutation_logs=[]
 for name,mutated in mutations.items():
  assert mutated!=text,name
  out=t/('mutant-'+name);out.mkdir();f=out/'ProductionSlotProjections.java';f.write_text(mutated)
  subprocess.run([str(J/'bin/javac'),'-cp',tmp+os.pathsep+CP,'-d',str(out),str(f)],check=True,capture_output=True,text=True)
  trial=subprocess.run([str(J/'bin/java'),'-Djava.awt.headless=true','-cp',str(out)+os.pathsep+tmp+os.pathsep+CP,'NativeArmorSlotsTest',str(H/'runtime-scope.json')],cwd=out,capture_output=True,text=True)
  assert trial.returncode!=0 and 'AssertionError' in trial.stderr,(name,trial.stdout,trial.stderr)
  mutation_logs.append(name+': '+next(l for l in trial.stderr.splitlines() if 'AssertionError' in l))
  print('PASS rejected production semantic mutant:',name)
 (H/'mutation-controls.log').write_text('\n'.join(mutation_logs)+'\n')
 (H/'runtime-inputs.json').write_text(json.dumps({str(f.relative_to(t)) if f.is_relative_to(t) else str(f.relative_to(R)):hashlib.sha256(f.read_bytes()).hexdigest() for f in files},indent=2)+'\n')
print('PASS bounded native armor slots; projections are not full NTM gameplay or all-model rendering acceptance')
