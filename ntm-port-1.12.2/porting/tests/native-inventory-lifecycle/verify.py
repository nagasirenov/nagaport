#!/usr/bin/env python3
"""Byte-exact source reversal and generation of native-interface callback probes.
Usage: python3 verify.py REPOSITORY OUTPUT_DIRECTORY
The generated probes contain exact production callbacks, not whole machine owners.
"""
import pathlib, re, subprocess, sys, json, hashlib
root=pathlib.Path(sys.argv[1]).resolve(); out=pathlib.Path(sys.argv[2]).resolve(); out.mkdir(parents=True,exist_ok=True)
base='ea933e28fe3a1a9a60e9d438411a02182b2ab22a'
prefix='ntm-port-1.12.2/src/main/java/'
paths=subprocess.check_output(['git','ls-tree','-r','--name-only',base,prefix],cwd=root,text=True).splitlines()
changed=[]; callbacks=[]; declarations=0; calls=0
for path in paths:
 if not path.endswith('.java'):continue
 old=subprocess.check_output(['git','show',base+':'+path],cwd=root); new=(root/path).read_bytes()
 if re.search(rb'(?:openInventory|closeInventory)\(\)',new):raise AssertionError('remaining old callback: '+path)
 expected=re.sub(rb'public void (openInventory|closeInventory)\(\)',rb'public void \1(net.minecraft.entity.player.EntityPlayer viewer)',old)
 expected=expected.replace(b'super.closeInventory();',b'super.closeInventory(viewer);')
 if '/container/' in path:
  expected=expected.replace(b'.openInventory();',b'.openInventory(invPlayer.player);')
  if b'.closeInventory();' in expected:
   param=re.search(rb'void onContainerClosed\(EntityPlayer (\w+)\)',expected)[1]
   expected=expected.replace(b'.closeInventory();',b'.closeInventory('+param+b');')
 if path.endswith('/items/block/ItemBlockStorageCrate.java'):expected=expected.replace(b'target.getTagCompound().hasNoTags()',b'target.getTagCompound().isEmpty()')
 assert new==expected,'unexpected source changes: '+path
 if old!=new:
  # Reverse only the transformations that occur in this file, retaining CRLF bytes.
  reverse=new.replace(b'net.minecraft.entity.player.EntityPlayer viewer',b'')
  reverse=reverse.replace(b'super.closeInventory(viewer);',b'super.closeInventory();')
  if '/container/' in path:
   reverse=reverse.replace(b'.openInventory(invPlayer.player);',b'.openInventory();')
   if b'.closeInventory();' in old:
    param=re.search(rb'void onContainerClosed\(EntityPlayer (\w+)\)',old)[1]
    reverse=reverse.replace(b'.closeInventory('+param+b');',b'.closeInventory();')
  if path.endswith('/items/block/ItemBlockStorageCrate.java'):reverse=reverse.replace(b'target.getTagCompound().isEmpty()',b'target.getTagCompound().hasNoTags()')
  assert reverse==old,'source reversal failed: '+path
  count=len(re.findall(rb'public void (?:openInventory|closeInventory)\(\)',old));declarations+=count
  calls+=len(re.findall(rb'(?<!void )(?:openInventory|closeInventory)\(\);',old))
  changed.append(dict(path=path,base_sha256=hashlib.sha256(old).hexdigest(),ported_sha256=hashlib.sha256(new).hexdigest(),callbacks=count))
  for m in re.finditer(r'public void (openInventory|closeInventory)\(net\.minecraft\.entity\.player\.EntityPlayer viewer\)\s*\{',new.decode()):
   s=new.decode();depth=1;end=m.end()
   while depth:
    if s[end]=='{':depth+=1
    if s[end]=='}':depth-=1
    end+=1
   callbacks.append((path,m[1],s[m.start():end]))
# Original-source callback parity, allowing only the already-landed world/sound API
# migration plus this patch's player parameter, super forwarding and NBT predicate.
for path,name,body in callbacks:
 original_path=path.replace('ntm-port-1.12.2/','ntm-original-pristine/')
 original=subprocess.check_output(['git','show',base+':'+original_path],cwd=root,text=True)
 match=re.search(r'public void '+name+r'\(\)\s*\{',original);assert match,original_path
 depth=1;end=match.end()
 while depth:
  if original[end]=='{':depth+=1
  if original[end]=='}':depth-=1
  end+=1
 old=original[match.start():end]
 old=old.replace(name+'()',name+'(net.minecraft.entity.player.EntityPlayer viewer)',1)
 old=old.replace('super.closeInventory();','super.closeInventory(viewer);')
 old=old.replace('target.stackTagCompound','target.getTagCompound()')
 old=old.replace('target.getTagCompound().hasNoTags()','target.getTagCompound().isEmpty()')
 old=old.replace('worldObj','world')
 old=old.replace('!world.isRemote','!this.world.isRemote')
 old=old.replace('this.world.playSoundEffect(', 'com.hbm.sound.WorldSoundEffects.play(this.world, ')
 old=old.replace('player.world.playSoundEffect(', 'com.hbm.sound.WorldSoundEffects.play(player.world, ')
 for axis in 'xyz':old=old.replace(axis+'Coord','this.pos.get'+axis.upper()+'()')
 assert re.sub(r'\s+','',old)==re.sub(r'\s+','',body),'original callback drift: '+path+' '+name
print('PASS original pristine parity: all 72 callback bodies, limited explicit world/sound migration, native parameter/super forwarding and map-empty predicate substitutions only.')
assert len(changed)==45 and declarations==72 and calls==18,(len(changed),declarations,calls)
(out/'source-manifest.json').write_text(json.dumps(dict(base=base,files=changed,declarations=declarations,calls=calls),indent=2)+'\n')
def methods(part):return '\n'.join(body for path,name,body in callbacks if path.endswith(part))
classes='''import net.minecraft.inventory.*; import net.minecraft.item.*; import net.minecraft.nbt.*;
import net.minecraft.entity.player.*; import net.minecraft.world.*; import net.minecraft.util.math.*; import java.util.*;
class LifecycleFixture extends InventoryBasic {
 public World world; public BlockPos pos=new BlockPos(-7,29,30); public EntityPlayer player; public ItemStack target; public int playersUsing;
 public LifecycleFixture(){super("fixture",false,1);}
 // Deliberate bounded collaborator: the large NBT ejection routine is not tested here.
 public NBTTagCompound checkNBT(NBTTagCompound n){LifecycleNativeTest.events.add("checkNBT");return n;}
}
'''
for name,part,parent in [('Crate','tileentity/machine/storage/TileEntityCrateBase.java','LifecycleFixture'),('Cabinet','tileentity/machine/storage/TileEntityFileCabinet.java','Crate'),('Mass','tileentity/machine/storage/TileEntityMassStorage.java','LifecycleFixture'),('ItemBase','items/ItemInventory.java','LifecycleFixture'),('ItemCrate','items/block/ItemBlockStorageCrate.java','ItemBase'),('ToolBox','items/tool/ItemToolBox.java','ItemBase'),('AmmoBag','items/tool/ItemAmmoBag.java','ItemBase'),('CasingBag','items/tool/ItemCasingBag.java','ItemBase'),('PlasticBag','items/tool/ItemPlasticBag.java','ItemBase')]:
 classes+='class '+name+' extends '+parent+' { static Random rand=new Random(9);\n'+methods(part)+'\n}\n'
# Every remaining exact no-op implementation is compiled against native IInventory.
empty=[body for path,name,body in callbacks if not body[body.index('{')+1:-1].strip()]
classes+='\n'.join('class Empty'+str(i)+' extends LifecycleFixture { @Override '+body+' }' for i,body in enumerate(empty))
(out/'LifecycleFixtures.java').write_text(classes)
print('PASS byte-exact CP70 reversal: 45 files, 72 declarations, 18 caller/super edges; no zero-argument lifecycle remains; generated native callback fixtures ('+str(len(empty))+' exact existing no-ops).')
