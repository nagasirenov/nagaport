#!/usr/bin/env python3
"""Genuine native platform; complete containers/base/helper/slots, explicit outer hosts."""
from pathlib import Path
import subprocess,tempfile,hashlib,json,re,zipfile
H=Path(__file__).resolve().parent;R=H.parents[2];S=R/'src/main/java';ROOT=Path('/workspace/shared/nagaport');T=Path('/workspace/shared/nagaport-toolchain');CP=(T/'native-classpath.txt').read_text().strip();J=T/'java8/bin'
def declaration(s,needle):
 start=s.index(needle);p=s.index('{',start);depth=1;end=p+1
 while depth:
  if s[end]=='{':depth+=1
  if s[end]=='}':depth-=1
  end+=1
 return s[start:end]
def source(p):
 path=S/p
 if path.exists():return path.read_bytes().decode()
 return subprocess.check_output(['git','-C',str(ROOT),'show','d7544326:ntm-port-1.12.2/src/main/java/'+p]).decode()
def sha(s):return hashlib.sha256(s.encode()).hexdigest()
complete=['com/hbm/items/ItemInventory.java','com/hbm/items/ItemInventoryTransfer.java','com/hbm/util/NullableInventoryStackNbt.java','com/hbm/util/NativeHeldItem.java','com/hbm/inventory/SlotPattern.java','com/hbm/inventory/SlotNonRetarded.java','com/hbm/interfaces/NotableComments.java','com/hbm/tileentity/IGUIProvider.java','com/hbm/sound/WorldSoundEffects.java','com/hbm/main/ModSoundEvents.java','com/hbm/lib/RefStrings.java']
manifest={'complete':{p:sha(source(p)) for p in complete},'projections':{}}
with tempfile.TemporaryDirectory(prefix='box-containers-native-') as td:
 d=Path(td);out=d/'classes';out.mkdir();files=[]
 def write(p,s):
  f=d/'src'/p;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(s);files.append(str(f))
 for p in complete:write(p,source(p))
 for name in ['LeadBox','ToolBox','RebarPlacer']:
  p='com/hbm/items/tool/Item'+name+'.java';s=source(p);needles=['public static class Inventory'+('Rebar' if name=='RebarPlacer' else name)+' extends ItemInventory']
  bodies=[declaration(s,n) for n in needles];manifest['projections'][p]={'complete_declarations':needles,'sha256':sha(s)}
  write(p,'package com.hbm.items.tool; import com.hbm.items.*; import com.hbm.items.block.ItemBlockStorageCrate; import com.hbm.main.MainRegistry; import com.hbm.inventory.SlotPattern; import net.minecraft.inventory.*; import net.minecraft.item.*; import net.minecraft.entity.player.*; import net.minecraft.nbt.*; import net.minecraft.world.World; public class Item'+name+' extends Item {\n'+'\n'.join(bodies)+'\n}')
 for name in ['ContainerLeadBox','ContainerToolBox']:
  p='com/hbm/inventory/container/'+name+'.java';s=source(p);needles=['public '+name+'(', 'public ItemStack transferStackInSlot(', 'public boolean canInteractWith(', 'public void onContainerClosed('];bodies=[declaration(s,n) for n in needles];manifest['projections'][p]={'exact_methods':needles,'sha256':sha(s),'excluded':'obsolete slotClick callback, unchanged and not activated'}
  typ='Inventory'+('LeadBox' if name=='ContainerLeadBox' else 'ToolBox')
  write(p,'package com.hbm.inventory.container; import com.hbm.items.ItemInventoryTransfer; import com.hbm.inventory.SlotNonRetarded; import com.hbm.items.tool.ItemLeadBox.InventoryLeadBox; import com.hbm.items.tool.ItemToolBox.InventoryToolBox; import net.minecraft.entity.player.*; import net.minecraft.inventory.*; import net.minecraft.item.*; public class '+name+' extends Container { private '+typ+' box;\n'+'\n'.join(bodies)+'\n}')
 fixtures={
 'com/hbm/main/MainRegistry.java':'package com.hbm.main; public class MainRegistry { public static Object instance=new Object(); }',
 'com/hbm/items/ModItems.java':'package com.hbm.items; public class ModItems { public static net.minecraft.item.Item toolbox=net.minecraft.init.Items.CHEST_MINECART; }',
 'com/hbm/items/block/ItemBlockStorageCrate.java':'package com.hbm.items.block; public class ItemBlockStorageCrate extends net.minecraft.item.Item {}',
 'com/hbm/util/ItemStackUtil.java':'package com.hbm.util; public class ItemStackUtil {}',
 'com/hbm/inventory/gui/GUILeadBox.java':'package com.hbm.inventory.gui; public class GUILeadBox { public GUILeadBox(net.minecraft.entity.player.InventoryPlayer p, com.hbm.items.tool.ItemLeadBox.InventoryLeadBox i) {} }',
 }
 for p,s in fixtures.items():write(p,s)
 files+=[str(H/'NativeBoxContainersTest.java')]
 subprocess.run([str(J/'javac'),'-cp',CP,'-d',str(out),str(H/'AdaptOriginalMerge.java')],check=True)
 subprocess.run([str(J/'java'),'-cp',str(out)+':'+CP,'AdaptOriginalMerge',str(ROOT/'mods1710/HBM-NTM-.1.0.27_X5645_H261.jar'),str(out/'OriginalMergeOracle.class')],check=True)
 manifest['original_mod_sha256']=hashlib.sha256((ROOT/'mods1710/HBM-NTM-.1.0.27_X5645_H261.jar').read_bytes()).hexdigest()
 manifest['adapted_original_merge_sha256']=hashlib.sha256((out/'OriginalMergeOracle.class').read_bytes()).hexdigest()
 for args in [[str(J/'javac'),'-encoding','UTF-8','-source','8','-target','8','-cp',str(out)+':'+CP,'-d',str(out)]+files,[str(J/'java'),'-Xmx400m','-cp',str(out)+':'+CP,'NativeBoxContainersTest']]:
  result=subprocess.run(args,cwd=d,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT);print(result.stdout,end='',flush=True)
  if result.returncode:raise SystemExit(result.returncode)
 # Repeat with the actual platform quantity field remapped to the SRG layout.
 mapper=d/'RemapQuantityField.java';mapper.write_bytes(subprocess.check_output(['git','-C',str(ROOT),'show','d7544326:ntm-port-1.12.2/porting/tests/difurnace-raw-reader/RemapQuantityField.java']))
 subprocess.run([str(J/'javac'),'-cp',CP,'-d',str(out),str(mapper)],check=True)
 native=None
 for entry in CP.split(':'):
  if Path(entry).is_file() and zipfile.is_zipfile(entry):
   with zipfile.ZipFile(entry) as z:
    if 'net/minecraft/item/ItemStack.class' in z.namelist():native=z.read('net/minecraft/item/ItemStack.class');break
 assert native is not None
 raw=d/'ItemStack.class';raw.write_bytes(native);mapped=d/'srg/net/minecraft/item/ItemStack.class'
 subprocess.run([str(J/'java'),'-cp',str(out)+':'+CP,'RemapQuantityField',str(raw),str(mapped)],check=True)
 srg=subprocess.run([str(J/'java'),'-Xmx400m','-cp',str(d/'srg')+':'+str(out)+':'+CP,'NativeBoxContainersTest'],cwd=d,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT);print(srg.stdout,end='',flush=True);assert srg.returncode==0
 manifest['platform_sha256']={'native':hashlib.sha256(native).hexdigest(),'srg_quantity':hashlib.sha256(mapped.read_bytes()).hexdigest()}
 # Each mutant must be rejected by the original-bytecode differential, not a source-only check.
 helper=source('com/hbm/items/ItemInventoryTransfer.java')
 changes={
 'normalize_any_empty_destination': [('current != null)', 'current != null && !current.isEmpty())'),('current == null)', 'current == null || current.isEmpty())')],
 'mask_signed_quantity': [('return ItemInventory.quantity(stack);','return stack.getCount();')],
 'mask_registered_item': [(declaration(helper,'private static Item rawItem('),'private static Item rawItem(ItemStack stack) { return stack.getItem(); }')],
 'native_empty_tag_equality': [(declaration(helper,'private static boolean tagsEqual('),'private static boolean tagsEqual(ItemStack left, ItemStack right) { return ItemStack.areItemStackTagsEqual(left, right); }')]
 }
 rejected={}
 for name,rules in changes.items():
  text=helper
  for before,after in rules:
   assert before in text;text=text.replace(before,after)
  p=d/name/'com/hbm/items/ItemInventoryTransfer.java';p.parent.mkdir(parents=True);p.write_text(text);classes=d/name/'classes';classes.mkdir()
  subprocess.run([str(J/'javac'),'-encoding','UTF-8','-cp',str(out)+':'+CP,'-d',str(classes),str(p)],check=True)
  mutant=subprocess.run([str(J/'java'),'-Xmx400m','-cp',str(classes)+':'+str(out)+':'+CP,'NativeBoxContainersTest'],cwd=d,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
  assert mutant.returncode!=0 and 'AssertionError: matrix' in mutant.stdout,(name,mutant.stdout)
  rejected[name]={'sha256':sha(text),'assertion':next(x for x in mutant.stdout.splitlines() if 'AssertionError:' in x)}
  print('PASS rejected mutant:',name,flush=True)
 manifest['rejected_mutants']=rejected
 manifest['assertions']=int(re.search(r'PASS (\d+) native box',result.stdout).group(1));manifest['fixtures']=fixtures
 manifest['test_sha256']=sha((H/'NativeBoxContainersTest.java').read_text());manifest['generator_sha256']=sha(Path(__file__).read_text());manifest['adapter_sha256']=sha((H/'AdaptOriginalMerge.java').read_text())
 assert manifest['complete']=={p:sha(source(p)) for p in complete},'production changed during test'
 for p,row in manifest['projections'].items():assert row['sha256']==sha(source(p)),'projection changed during test'
(H/'receipt.json').write_text(json.dumps(manifest,indent=2)+'\n')
