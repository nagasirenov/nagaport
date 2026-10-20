#!/usr/bin/env python3
from pathlib import Path
import subprocess,hashlib,json
H=Path(__file__).resolve().parent;R=H.parents[2];BASE='f5ce86cd607379cc9810896cf91d1fee86ce6331'
FILES={}
def edit(rel,old,new,n=1):
 if rel not in FILES:
  before=subprocess.check_output(['git','-C',str(R.parent),'show',BASE+':ntm-port-1.12.2/'+rel]);FILES[rel]={'before':before,'text':before.decode(),'rules':[]}
 d=FILES[rel];assert d['text'].count(old)==n,(rel,old,d['text'].count(old),n);d['text']=d['text'].replace(old,new);d['rules'].append([old,new,n])
def build():
 f='src/main/java/com/hbm/items/ItemInventory.java';nl='\r\n';n=lambda s:s.replace('\n',nl)
 edit(f,'import com.hbm.util.ItemStackUtil;','import com.hbm.util.NullableInventoryStackNbt;')
 edit(f,'getStackInSlot(i) != null && getStackInSlot(i).stackSize == 0','getStackInSlotNullable(i) != null && quantity(getStackInSlotNullable(i)) == 0')
 edit(f,'ItemStackUtil.addStacksToNBT(target, slots)','writeStacksToNBT(target, slots)')
 edit(f,'nbt.hasNoTags()','nbt.isEmpty()')
 edit(f,'byte[] abyte = CompressedStreamTools.compress(nbt);',n('java.io.ByteArrayOutputStream compressed = new java.io.ByteArrayOutputStream();\n\t\t\tCompressedStreamTools.writeCompressed(nbt, compressed);\n\t\t\tbyte[] abyte = compressed.toByteArray();'))
 edit(f,'this.getStackInSlot(i1)','this.getStackInSlotNullable(i1)')
 edit(f,'itemstack.stackSize > 0','quantity(itemstack) > 0');edit(f,'j1 > itemstack.stackSize','j1 > quantity(itemstack)');edit(f,'j1 = itemstack.stackSize;','j1 = quantity(itemstack);')
 edit(f,'itemstack.stackSize -= j1;',n('net.minecraft.item.Item item = itemstack.getItem();\n\t\t\t\t\t\t\tint damage = itemstack.getItemDamage();\n\t\t\t\t\t\t\tNBTTagCompound tag = itemstack.getTagCompound();\n\t\t\t\t\t\t\titemstack.shrink(j1);'))
 edit(f,'new ItemStack(itemstack.getItem(), j1, itemstack.getItemDamage())','new ItemStack(item, j1, damage)')
 edit(f,'int damage = itemstack.getItemDamage();',n('int damage = itemstack.getItemDamage();\n\t\t\t\t\t\t\t// Native empty masking must not replace a registered raw item with AIR.\n\t\t\t\t\t\t\t// Edge-only public snapshot; its capability serialization follows native rules.\n\t\t\t\t\t\t\tif(item == net.minecraft.init.Items.AIR) {\n\t\t\t\t\t\t\t\tNBTTagCompound identity = itemstack.writeToNBT(new NBTTagCompound());\n\t\t\t\t\t\t\t\titem = net.minecraft.item.Item.getByNameOrId(identity.getString("id"));\n\t\t\t\t\t\t\t\tdamage = item.getDamage(itemstack);\n\t\t\t\t\t\t\t}'))
 edit(f,'if (itemstack.hasTagCompound()) {','if (tag != null) {');edit(f,'entityitem.getEntityItem().setTagCompound((NBTTagCompound) itemstack.getTagCompound().copy());','entityitem.getItem().setTagCompound(tag.copy());');edit(f,'player.world.spawnEntityInWorld(entityitem);','player.world.spawnEntity(entityitem);')
 edit(f,'ItemStack stack = getStackInSlot(slot);','ItemStack stack = getStackInSlotNullable(slot);',2)
 edit(f,'stack.stackSize > amount','quantity(stack) > amount')
 edit(f,'return stack;'+nl+'\t}'+nl+nl+'\t@Override'+nl+'\tpublic void setInventorySlotContents','return stack == null ? ItemStack.EMPTY : stack;'+nl+'\t}'+nl+nl+'\t@Override'+nl+'\tpublic void setInventorySlotContents')
 edit(f,'stack = stack == null || stack.isEmpty() ? null : stack;','stack = stack == ItemStack.EMPTY ? null : stack;')
 edit(f,'stack.setCount(Math.min(stack.getCount(), this.getInventoryStackLimit()));','stack.setCount(Math.min(quantity(stack), this.getInventoryStackLimit()));')
 edit(f,'return stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack;'+nl+'\t}'+nl+nl+'\t@Override public ItemStack getStackInSlot','return stack == null ? ItemStack.EMPTY : stack;'+nl+'\t}'+nl+nl+'\t@Override public ItemStack getStackInSlot')
 edit(f,'@Override public ItemStack getStackInSlot(int slot) { return slots[slot]; }',n('public ItemStack getStackInSlotNullable(int slot) { return slots[slot]; }\n\t@Override public ItemStack getStackInSlot(int slot) { ItemStack stack = getStackInSlotNullable(slot); return stack == null ? ItemStack.EMPTY : stack; }'))
 edit(f,'isUseableByPlayer','isUsableByPlayer')
 extra='''
	/** Native boundary: internal vacancy remains null; non-null raw stacks retain identity. */
	@Override public boolean isEmpty() {
		for(ItemStack stack : slots) if(stack != null && !stack.isEmpty()) return false;
		return true;
	}
	@Override public void clear() { java.util.Arrays.fill(slots, null); markDirty(); }
	@Override public int getField(int id) { return 0; }
	@Override public void setField(int id, int value) { }
	@Override public int getFieldCount() { return 0; }
	@Override public net.minecraft.util.text.ITextComponent getDisplayName() {
		return hasCustomName() ? new TextComponentString(target.getDisplayName()) : new net.minecraft.util.text.TextComponentTranslation(getName());
	}

	/** Raw quantity is necessary for the original signed clamp and exactly-zero cleanup.
	 * Read-only, mapped access; no furnace-specific empty admission policy applies here. */
	protected static int quantity(ItemStack stack) {
		int count = stack.getCount();
		if(count != 0) return count;
		try { return QuantityField.FIELD.getInt(stack); }
		catch(IllegalAccessException failure) { throw new IllegalStateException("Cannot read item inventory stack quantity", failure); }
	}
	private static final class QuantityField {
		private static final java.lang.reflect.Field FIELD = net.minecraftforge.fml.relauncher.ReflectionHelper.findField(ItemStack.class, "stackSize", "field_77994_a");
	}

	/** Original nullable item-inventory list schema, deliberately local to this domain. */
	protected static void writeStacksToNBT(ItemStack owner, ItemStack[] stacks) {
		if(owner.getTagCompound() == null) owner.setTagCompound(new NBTTagCompound());
		net.minecraft.nbt.NBTTagList tags = new net.minecraft.nbt.NBTTagList();
		for(int i = 0; i < stacks.length; i++) {
			if(stacks[i] != null) {
				NBTTagCompound slot = new NBTTagCompound();
				slot.setByte("slot", (byte) i);
				stacks[i].writeToNBT(slot);
				tags.appendTag(slot);
			}
		}
		owner.getTagCompound().setTag("items", tags);
	}
	protected static ItemStack[] readStacksFromNBT(ItemStack owner, int count) {
		if(owner.getTagCompound() == null) return null;
		net.minecraft.nbt.NBTTagList tags = owner.getTagCompound().getTagList("items", 10);
		if(count == 0) count = tags.tagCount();
		ItemStack[] stacks = new ItemStack[count];
		for(int i = 0; i < count; i++) {
			NBTTagCompound slot = tags.getCompoundTagAt(i);
			byte index = slot.getByte("slot");
			ItemStack loaded = NullableInventoryStackNbt.read(slot);
			if(index >= 0 && index < stacks.length && loaded != null) stacks[index] = loaded;
		}
		return stacks;
	}
'''
 edit(f,nl+'}',n(extra)+nl+'}')
 for name in ['ItemLeadBox','ItemToolBox','ItemRebarPlacer']:
  f='src/main/java/com/hbm/items/tool/'+name+'.java'
  edit(f,'ItemStack[] fromNBT = ItemStackUtil.readStacksFromNBT(box, slots.length);','ItemStack[] fromNBT = readStacksFromNBT(box, slots.length);')
  edit(f,'if(!box.hasTagCompound())'+(' box.setTagCompound' if name=='ItemRebarPlacer' else '\n\t\t\t\tbox.setTagCompound'),'if(box.getTagCompound() == null)'+(' box.setTagCompound' if name=='ItemRebarPlacer' else '\n\t\t\t\tbox.setTagCompound'))
  edit(f,'getInventoryName()','getName()',2 if name=='ItemRebarPlacer' else 1)
  edit(f,'hasCustomInventoryName()','hasCustomName()',2 if name=='ItemRebarPlacer' else 1)
  if name=='ItemRebarPlacer':edit(f,'rebar.isUseableByPlayer(player)','rebar.isUsableByPlayer(player)')
 f='src/main/java/com/hbm/items/block/ItemBlockStorageCrate.java'
 edit(f,'isUseableByPlayer','isUsableByPlayer');edit(f,'this.getStackInSlot(i)','this.getStackInSlotNullable(i)')
 for name in ['LeadBox','ToolBox']:
  f='src/main/java/com/hbm/inventory/container/Container'+name+'.java';edit(f,'box.isUseableByPlayer(player)','box.isUsableByPlayer(player)')
  f='src/main/java/com/hbm/inventory/gui/GUI'+name+'.java';edit(f,'getInventoryName()','getName()');edit(f,'hasCustomInventoryName()','hasCustomName()')
 return FILES
if __name__=='__main__':
 import argparse;p=argparse.ArgumentParser();p.add_argument('--apply',action='store_true');a=p.parse_args();files=build();d={'baseline':BASE,'files':{},'protected':{}}
 for rel,row in files.items():
  after=row['text'].encode();d['files'][rel]={'before_sha256':hashlib.sha256(row['before']).hexdigest(),'after_sha256':hashlib.sha256(after).hexdigest(),'rules':row['rules']}
  if a.apply:assert (R/rel).read_bytes()==row['before'],rel;(R/rel).write_bytes(after)
 for rel in ['src/main/java/com/hbm/util/ItemStackUtil.java','src/main/java/com/hbm/util/NullableInventoryStackNbt.java','src/main/java/com/hbm/items/tool/ItemAmmoBag.java','src/main/java/com/hbm/items/tool/ItemCasingBag.java','src/main/java/com/hbm/items/tool/ItemPlasticBag.java','src/main/java/com/hbm/util/NativeHeldItem.java','src/main/java/com/hbm/sound/WorldSoundEffects.java','src/main/java/com/hbm/main/ModSoundEvents.java','src/main/java/com/hbm/lib/RefStrings.java'] + ['src/main/java/com/hbm/tileentity/machine/storage/TileEntity'+name+'.java' for name in ['CrateIron','CrateSteel','CrateDesh','CrateTungsten','CrateTemplate','Safe']]:
  d['protected'][rel]=hashlib.sha256((R/rel).read_bytes()).hexdigest()
 (H/'manifest.json').write_text(json.dumps(d,indent=2)+'\n')
