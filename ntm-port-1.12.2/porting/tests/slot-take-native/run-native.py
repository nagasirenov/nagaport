#!/usr/bin/env python3
"""Offline genuine Slot/Container dispatch. Exact complete selected callbacks, bounded owner projections.
Usage: run-native.py JAVA_HOME CLASSPATH_FILE LEGACY_CLIENT_JAR OUTPUT_DIR
No Minecraft/Forge class is stubbed; unrelated owner methods are not compiled.
"""
from pathlib import Path
import sys, subprocess, os, re, zipfile, json
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[3];SRC=ROOT/'ntm-port-1.12.2/src/main/java'
JAVA,CPF,LEGACY,OUT=map(lambda s:Path(s).resolve(),sys.argv[1:]);OUT.mkdir(parents=True,exist_ok=True)
CP=CPF.read_text().strip();GEN=OUT/'src';CLASSES=OUT/'classes';CLASSES.mkdir(exist_ok=True);FILES=[]
def write(path,s):
 p=GEN/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(s);FILES.append(p)
def source(path):return (SRC/path).read_bytes().decode()
def methods(text,signature):
 ret=[]
 for m in re.finditer(re.escape(signature),text):
  a=m.start();o=text.index('{',m.end());e=o+1;d=1
  while d:
   if text[e]=='{':d+=1
   elif text[e]=='}':d-=1
   e+=1
  ret.append(text[a:e])
 return ret
def run(args,verbose=True):
 p=subprocess.run(list(map(str,args)),stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True,cwd=OUT)
 if p.returncode:raise AssertionError(p.stdout)
 if verbose:print(p.stdout,end='')
 return p.stdout
# Full SlotCraftingOutput minus its separate, still-unported decrStackSize method.
# No substitution of its quantity algorithm; no accumulation parity claim.
s=source('com/hbm/inventory/SlotCraftingOutput.java');excluded=methods(s,'public ItemStack decrStackSize(')[0]
s=s.replace('\t@Override\n\t'+excluded+'\n','');assert 'stackSize' not in s
write('com/hbm/inventory/SlotCraftingOutput.java',s)
imports='import net.minecraft.entity.player.*; import net.minecraft.inventory.*; import net.minecraft.item.ItemStack; import probe.*;\n'
for owner in ['ContainerAnvil','ContainerArmorTable','ContainerWeaponTable']:
 s=source('com/hbm/inventory/container/'+owner+'.java');cb=methods(s,'public ItemStack onTake(');assert len(cb)==2
 text='package com.hbm.inventory.container;\n'+imports
 if owner=='ContainerAnvil':
  text+='import com.hbm.inventory.SlotCraftingOutput; import com.hbm.inventory.recipes.anvil.*;\n'
  text+='public class ContainerAnvil { public TestInventory input=new TestInventory("input",8); public TestInventory output=new TestInventory("output",1); public int updates;\n'
  text+='public Slot output(EntityPlayer p) { return new SlotCraftingOutput(p,output,0,0,0) { @Override '+cb[0]+' }; }\n'
  text+='public class SmithingSlot extends Slot { public SmithingSlot() { super(input,0,0,0); }\n'+methods(s,'public void onSlotChanged(')[0]+'\n@Override '+cb[1]+'\n}\n'
  text+='private void updateSmithing(){ updates++; ProbeState.events.add("smithing"); }\n}'
 elif owner=='ContainerArmorTable':
  text+='import com.hbm.handler.ArmorModHandler;\npublic class ContainerArmorTable { public TestInventory upgrades=new TestInventory("upgrades",ArmorModHandler.MOD_SLOTS); public TestInventory armor=new TestInventory("armor",1);\n'
  text+='public Slot output() { return new Slot(armor,0,0,0) { @Override '+cb[0]+' }; }\n'
  text+='public class UpgradeSlot extends Slot { public UpgradeSlot(int i) { super(upgrades,i,0,0); this.slotNumber=i; }\n@Override '+cb[1]+'\n}\n}'
 else:
  text+='import com.hbm.items.weapon.sedna.mods.XWeaponModManager;\npublic class ContainerWeaponTable { public TestInventory mods=new TestInventory("mods",7); public TestInventory gun=new TestInventory("gun",1); public int index=0;\n'
  text+='public Slot output() { return new Slot(gun,0,0,0) { @Override '+cb[0]+' }; }\n'
  text+='public class ModSlot extends Slot { public ModSlot(int i) { super(mods,i,0,0); this.slotNumber=i; }\n@Override '+cb[1]+'\n'+methods(s,'public void refreshInstalledMods(')[0]+'\n}\n}'
 write('com/hbm/inventory/container/'+owner+'.java',text)
# Every selected ordinary invocation is emitted verbatim into a wrapper whose original
# receiver and arguments have their compiler-proven native Slot/EntityPlayer/ItemStack types.
manifest=json.loads((HERE/'manifest.json').read_text());calls=[]
for f in manifest['files']:
 s=(ROOT/f['path']).read_text()
 for e in f['edits']:
  if e['kind']!='call':continue
  # Source before/after token positions are captured by matching exact unique invocation occurrence.
  before=zipfile.ZipFile(HERE/'baseline.zip').read(f['path']).decode();a=before.rfind('\n',0,e['start'])+1;b=before.index(';',e['start'])+1
  old=before[a:b].strip();new=old.replace('.onPickupFromSlot(','.onTake(')
  m=re.fullmatch(r'(\w+)\.onTake\((\w+), (\w+)\);',new);assert m,new
  recv,p,stack=m.groups();calls.append('public static void call%d(Slot %s, EntityPlayer %s, ItemStack %s) { %s }'%(len(calls),recv,p,stack,new))
assert len(calls)==32
write('probe/CallEdges.java','package probe;\n'+imports+'public class CallEdges {\n'+'\n'.join(calls)+'\n}')
# Domain collaborators only record selected calls. They do not claim recipe/upgrade/achievement correctness.
write('probe/ProbeState.java','''package probe; import java.util.*; import net.minecraft.item.ItemStack; public class ProbeState {
 public static List<String> events=new ArrayList<>(); public static ItemStack lastStack; public static int lastIndex; public static ItemStack[] lastMods;
 public static boolean failAchievement,failInstall; public static int craftAmount,achievements;
 public static void reset(){events.clear();lastStack=null;lastMods=null;lastIndex=-1;failAchievement=false;failInstall=false;craftAmount=0;achievements=0;}
}''')
write('probe/TestInventory.java','''package probe; import net.minecraft.inventory.InventoryBasic; import net.minecraft.item.ItemStack;
public class TestInventory extends InventoryBasic { public final String label; public int nullSlot=-1; public TestInventory(String s,int n){super(s,false,n);label=s;}
 @Override public ItemStack getStackInSlot(int i){return i==nullSlot?null:super.getStackInSlot(i);}
 @Override public ItemStack decrStackSize(int i,int n){ProbeState.events.add("decr:"+i+":"+n);return super.decrStackSize(i,n);}
 @Override public void markDirty(){ProbeState.events.add("dirty:"+label);super.markDirty();}
}''')
write('com/hbm/util/AchievementHandler.java','''package com.hbm.util; import net.minecraft.entity.player.EntityPlayer; import net.minecraft.item.ItemStack; import probe.ProbeState;
public class AchievementHandler {public static void fire(EntityPlayer p,ItemStack s){ProbeState.events.add("achievement");ProbeState.achievements++;if(ProbeState.failAchievement)throw new IllegalStateException("achievement");}}
''')
write('com/hbm/inventory/recipes/anvil/AnvilRecipes.java','''package com.hbm.inventory.recipes.anvil; import java.util.*;
public class AnvilRecipes { public static List<AnvilSmithingRecipe> recipes=new ArrayList<>(); public static List<AnvilSmithingRecipe> getSmithing(){return recipes;} }''')
write('com/hbm/inventory/recipes/anvil/AnvilSmithingRecipe.java','''package com.hbm.inventory.recipes.anvil; import net.minecraft.item.ItemStack; import probe.ProbeState;
public class AnvilSmithingRecipe { public int match,left,right;public String name;public AnvilSmithingRecipe(String n,int m,int l,int r){name=n;match=m;left=l;right=r;}
 public int matchesInt(ItemStack l,ItemStack r){ProbeState.events.add("match:"+name);return match;}
 public int amountConsumed(int i,boolean swapped){ProbeState.events.add("amount:"+i+":"+swapped);return (i==0)^swapped?left:right;}}
''')
write('com/hbm/handler/ArmorModHandler.java','''package com.hbm.handler; import net.minecraft.item.ItemStack; import net.minecraft.init.Items; import probe.ProbeState;
public class ArmorModHandler {public static final int MOD_SLOTS=9;public static boolean isApplicable(ItemStack armor,ItemStack mod){ProbeState.events.add("applicable:armor");return mod!=null&&!mod.isEmpty()&&mod.getItem()==Items.APPLE;}
 public static void removeMod(ItemStack armor,int index){ProbeState.events.add("remove:"+index);ProbeState.lastStack=armor;ProbeState.lastIndex=index;}}
''')
write('com/hbm/items/weapon/sedna/mods/XWeaponModManager.java','''package com.hbm.items.weapon.sedna.mods; import net.minecraft.item.ItemStack; import net.minecraft.init.Items; import probe.ProbeState;
public class XWeaponModManager {public static void install(ItemStack s,int index,ItemStack...mods){ProbeState.events.add("install:"+index);ProbeState.lastStack=s;ProbeState.lastIndex=index;ProbeState.lastMods=mods;if(ProbeState.failInstall)throw new IllegalStateException("install");}
 public static boolean isApplicable(ItemStack stack,ItemStack mod,int index,boolean x){ProbeState.events.add("applicable:weapon:"+index+":"+x);return mod!=null&&!mod.isEmpty()&&mod.getItem()==Items.APPLE;}}
''')
write('SlotTakeNativeTest.java',(HERE/'SlotTakeNativeTest.java').read_text())
run([JAVA/'bin/javac','-encoding','UTF-8','-cp',CP,'-d',CLASSES]+FILES)
run([JAVA/'bin/java','-cp',str(CLASSES)+os.pathsep+CP,'SlotTakeNativeTest'])
# Separate actual original/native Slot parent parity. Keep original class bytes unchanged;
# strip only signed-JAR metadata so the default-package fixture can coexist.
unsigned=OUT/'legacy-unsigned.jar'
with zipfile.ZipFile(LEGACY) as a,zipfile.ZipFile(unsigned,'w') as b:
 for e in a.infolist():
  if not e.filename.startswith('META-INF/'):b.writestr(e,a.read(e))
run([JAVA/'bin/javac','-cp',CP+os.pathsep+str(unsigned),'-d',CLASSES,HERE/'SlotParentParity.java'])
run([JAVA/'bin/java','-cp',str(CLASSES)+os.pathsep+CP+os.pathsep+str(unsigned),'SlotParentParity'])
for fn,cp,cls in [('native-slot',CP,'net.minecraft.inventory.Slot'),('native-container',CP,'net.minecraft.inventory.Container'),('original-slot',str(LEGACY),'aay'),('original-container',str(LEGACY),'zs')]:
 (OUT/(fn+'.javap')).write_text(run([JAVA/'bin/javap','-p','-c','-classpath',cp,cls],verbose=False))
print('PASS exact 7 callback projections and 32 native invocation edges; unrelated quantity/transfer bodies not compiled or certified')
