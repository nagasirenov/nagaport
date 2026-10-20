#!/usr/bin/env python3
"""Complete nested declaration tests; outer hosts and crate registry/type collaborators are explicit fixtures."""
from pathlib import Path
import subprocess,tempfile,json,hashlib,re
H=Path(__file__).resolve().parent;R=H.parents[3];J=Path('/workspace/shared/nagaport-toolchain/java8');CP=Path('/workspace/shared/nagaport-toolchain/native-classpath.txt').read_text().strip()
def declaration(s,needle):
 start=s.index(needle);p=s.index('{',start);depth=1;end=p+1
 while depth:
  if s[end]=='{':depth+=1
  if s[end]=='}':depth-=1
  end+=1
 return s[start:end]
source='''import net.minecraft.item.*; import net.minecraft.init.*; import net.minecraft.block.Block; import net.minecraft.nbt.*; import net.minecraft.entity.player.EntityPlayer; import javax.annotation.Nonnull; import java.util.Random; import com.hbm.items.ItemInventory; import com.hbm.util.NullableInventoryStackNbt;
class ModItems { static Item toolbox; }
class ModBlocks { static Block crate_iron=Blocks.IRON_BLOCK,crate_steel=Blocks.GOLD_BLOCK,crate_desh=Blocks.DIAMOND_BLOCK,crate_tungsten=Blocks.EMERALD_BLOCK,crate_template=Blocks.LAPIS_BLOCK,safe=Blocks.STONE; }
// Explicit crate-type projections: only exact constructor count and size/name/custom-name methods.
abstract class TileEntityCrateBase extends net.minecraft.tileentity.TileEntity {
 protected ItemStack[] slots; public String customName;
 public TileEntityCrateBase(int count) { slots = new ItemStack[count]; }
 public int getSizeInventory() { return slots.length; }
 public boolean hasCustomName() { return this.customName != null && !this.customName.isEmpty(); }
 public void setCustomName(String name) { this.customName = name; markDirty(); }
 public abstract String getName();
}
'''
manifest={}
for name in ['Iron','Steel','Desh','Tungsten','Template','Safe']:
 simple='TileEntity'+('Crate'+name if name!='Safe' else name);p=R/('src/main/java/com/hbm/tileentity/machine/storage/'+simple+'.java');s=p.read_text();ctor=declaration(s,'public '+simple+'()');method=declaration(s,'public String getName()')
 source+='class '+simple+' extends TileEntityCrateBase { '+ctor+' '+method+' }\n';manifest[str(p.relative_to(R))]=hashlib.sha256(p.read_bytes()).hexdigest()
for name,inner,folder in [('ItemLeadBox','InventoryLeadBox','tool'),('ItemToolBox','InventoryToolBox','tool'),('ItemRebarPlacer','InventoryRebar','tool'),('ItemBlockStorageCrate','InventoryCrate','block')]:
 p=R/('src/main/java/com/hbm/items/'+folder+'/'+name+'.java');body=declaration(p.read_text(),'public static class '+inner+' extends ItemInventory');manifest[str(p.relative_to(R))]=hashlib.sha256(p.read_bytes()).hexdigest()
 host=('extends ItemBlock { '+name+'(Block b) { super(b); }' if folder=='block' else '{')
 source+='class '+name+' '+host+'\n'+body+'\n}\n'
(H/'generated-nested-declarations.java.txt').write_text(source)
with tempfile.TemporaryDirectory(prefix='native-item-inventory-subclasses-') as d:
 t=Path(d);(t/'NestedDeclarations.java').write_text(source);c=t/'classes';c.mkdir()
 files=[R/('src/main/java/'+p) for p in ['com/hbm/items/ItemInventory.java','com/hbm/util/NullableInventoryStackNbt.java','com/hbm/util/NativeHeldItem.java','com/hbm/sound/WorldSoundEffects.java','com/hbm/main/ModSoundEvents.java','com/hbm/lib/RefStrings.java']]
 for p in files:manifest[str(p.relative_to(R))]=hashlib.sha256(p.read_bytes()).hexdigest()
 for args in [[str(J/'bin/javac'),'-source','8','-target','8','-encoding','UTF-8','-cp',CP,'-d',str(c)]+list(map(str,files))+[str(t/'NestedDeclarations.java'),str(H/'NestedInventoryTest.java')],[str(J/'bin/java'),'-cp',str(c)+':'+CP,'NestedInventoryTest']]:
  r=subprocess.run(args,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True);print(r.stdout,end='',flush=True);assert r.returncode==0,r.returncode
(H/'receipt.json').write_text(json.dumps({'scope':'Four exact complete nested declarations, explicit outer type/registry hosts and six crate-type projections; not complete outer items or tile classes','production_sha256':manifest,'assertions':int(re.search(r'PASS (\d+) native nested',r.stdout).group(1)),'test_sha256':hashlib.sha256((H/'NestedInventoryTest.java').read_bytes()).hexdigest(),'generator_sha256':hashlib.sha256(Path(__file__).read_bytes()).hexdigest()},indent=2)+'\n')
