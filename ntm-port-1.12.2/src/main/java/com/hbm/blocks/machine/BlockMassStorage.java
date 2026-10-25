package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import com.hbm.blocks.IBlockMulti;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemLock;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.machine.TileEntityLockableBase;
import com.hbm.tileentity.machine.storage.TileEntityMassStorage;

import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import com.hbm.util.NullableInventoryStackNbt;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import com.hbm.render.icon.IIcon;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;

public class BlockMassStorage extends BlockContainer implements IBlockMulti, ILookOverlay, ITooltipProvider {
	public BlockMassStorage withSoundType(net.minecraft.block.SoundType sound) {
		super.setSoundType(sound);
		return this;
	}


	@SideOnly(Side.CLIENT) private IIcon[] iconTop;
	@SideOnly(Side.CLIENT) private IIcon[] iconSide;
	@SideOnly(Side.CLIENT) private IIcon[] iconFront;

	public BlockMassStorage() {
		super(Material.IRON);
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.iconTop = new IIcon[4];
		this.iconSide = new IIcon[4];
		this.iconFront = new IIcon[4];

		this.iconTop[0] = iconRegister.registerIcon(RefStrings.MODID + ":mass_storage_top_iron");
		this.iconSide[0] = iconRegister.registerIcon(RefStrings.MODID + ":mass_storage_side_iron");
		this.iconFront[0] = iconRegister.registerIcon(RefStrings.MODID + ":mass_storage_front_iron");

		this.iconTop[1] = iconRegister.registerIcon(RefStrings.MODID + ":mass_storage_top_desh");
		this.iconSide[1] = iconRegister.registerIcon(RefStrings.MODID + ":mass_storage_side_desh");
		this.iconFront[1] = iconRegister.registerIcon(RefStrings.MODID + ":mass_storage_front_desh");

		this.iconTop[2] = iconRegister.registerIcon(RefStrings.MODID + ":mass_storage_top");
		this.iconSide[2] = iconRegister.registerIcon(RefStrings.MODID + ":mass_storage_side");
		this.iconFront[2] = iconRegister.registerIcon(RefStrings.MODID + ":mass_storage_front");

		this.iconTop[3] = iconRegister.registerIcon(RefStrings.MODID + ":mass_storage_top_wood");
		this.iconSide[3] = iconRegister.registerIcon(RefStrings.MODID + ":mass_storage_side_wood");
		this.iconFront[3] = iconRegister.registerIcon(RefStrings.MODID + ":mass_storage_front_wood");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(CreativeTabs tab, net.minecraft.util.NonNullList<ItemStack> list) {
		list.add(new ItemStack(this, 1, 3));
		list.add(new ItemStack(this, 1, 0));
		list.add(new ItemStack(this, 1, 1));
		list.add(new ItemStack(this, 1, 2));
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		int meta = this.rectify(metadata);
		int dir = (metadata / getSubCount()) + 2;
		if(side == 0 || side == 1) return iconTop[meta];
		if(side == dir) return iconFront[meta];
		return iconSide[meta];
	}

	@Override
	public int damageDropped(net.minecraft.block.state.IBlockState state) {
		return rectify(state.getBlock().getMetaFromState(state));
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityMassStorage(getCapacity(meta));
	}

	public int getCapacity(int meta) {
		meta = rectify(meta);
		return meta == 3 ? 100 : meta == 0 ? 10_000 : meta == 1 ? 100_000 : meta == 2 ? 1_000_000 : 0;
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(), y = nativePos.getY(), z = nativePos.getZ();
		int side = nativeFacing != null ? nativeFacing.getIndex() : 0;

		if(world.isRemote) {
			return true;
		} else if(com.hbm.util.NativeHeldItem.getMainHandNullable(player) != null && (com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() instanceof ItemLock || com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() == ModItems.key_kit)) {
			return false;

		} else if(!player.isSneaking()) {
			TileEntity entity = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
			if(entity instanceof TileEntityMassStorage && ((TileEntityMassStorage) entity).canAccess(player)) {
				FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, x, y, z);
			}
			return true;
		} else {
			return false;
		}
	}

	private static boolean dropInv = true;

	public boolean removedByPlayer(net.minecraft.block.state.IBlockState state, World world, net.minecraft.util.math.BlockPos pos, EntityPlayer player, boolean willHarvest) {
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		if(!player.capabilities.isCreativeMode && !world.isRemote && willHarvest) {

			ItemStack drop = new ItemStack(this, 1, rectify(com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) )));
			ISidedInventory inv = (ISidedInventory)world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

			NBTTagCompound nbt = new NBTTagCompound();

			if(inv != null) {

				for(int i = 0; i < inv.getSizeInventory(); i++) {

					ItemStack stack = inv.getStackInSlot(i);
					if(stack == null || stack.isEmpty())
						continue;

					NBTTagCompound slot = new NBTTagCompound();
					stack.writeToNBT(slot);
					nbt.setTag("slot" + i, slot);
				}
			}

			if(inv instanceof TileEntityLockableBase) {
				TileEntityLockableBase lockable = (TileEntityLockableBase) inv;

				if(lockable.isLocked()) {
					nbt.setInteger("lock", lockable.getPins());
					nbt.setDouble("lockMod", lockable.getMod());
				}
			}

			if(inv instanceof TileEntityMassStorage && nbt.getKeySet().size() > 0) {
				TileEntityMassStorage storage = (TileEntityMassStorage) inv;
				nbt.setInteger("stack", storage.getStockpile());
			}

			if(!nbt.isEmpty()) {
				drop.setTagCompound(nbt);
			}

			world.spawnEntity(new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, drop));
		}

		dropInv = false;
		boolean flag = com.hbm.world.WorldAirRemoval.remove(world, x, y, z);
		dropInv = true;

		return flag;
	}

	@Override
	public void onBlockPlacedBy(World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, EntityLivingBase player, ItemStack stack) {
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		ISidedInventory inv = (ISidedInventory)world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(inv != null && stack.hasTagCompound()) {

			for(int i = 0; i < inv.getSizeInventory(); i++) {
				inv.setInventorySlotContents(i, new ItemStack(stack.getTagCompound().getCompoundTag("slot" + i)));
			}

			if(inv instanceof TileEntityMassStorage) {
				TileEntityMassStorage storage = (TileEntityMassStorage) inv;

				if(stack.getTagCompound().hasKey("lock")) {
					storage.setPins(stack.getTagCompound().getInteger("lock"));
					storage.setMod(stack.getTagCompound().getDouble("lockMod"));
					storage.lock();
				}

				storage.setStockpile(stack.getTagCompound().getInteger("stack"));
			}
		}

		super.onBlockPlacedBy(world, pos, state, player, stack);

		int i = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
		int meta = stack.getItemDamage();

		if(i == 0) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, meta, 2);
		if(i == 1) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, meta + 3 * getSubCount(), 2);
		if(i == 2) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, meta + 1 * getSubCount(), 2);
		if(i == 3) com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, meta + 2 * getSubCount(), 2);
	}

	@Override
	public void breakBlock(World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		Block block = state.getBlock();
		int meta = block.getMetaFromState(state);

		if(dropInv) {
			ISidedInventory sided = (ISidedInventory) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
			Random rand = world.rand;

			if(sided != null) {
				for(int i1 = 0; i1 < sided.getSizeInventory(); ++i1) {

					if(i1 == 1) continue; //do NOT drop the filter item

					ItemStack itemstack = sided.getStackInSlot(i1);

					if(itemstack != null && !itemstack.isEmpty()) {
						float f = rand.nextFloat() * 0.8F + 0.1F;
						float f1 = rand.nextFloat() * 0.8F + 0.1F;
						float f2 = rand.nextFloat() * 0.8F + 0.1F;

						while(itemstack.getCount() > 0) {
							int j1 = rand.nextInt(21) + 10;

							if(j1 > itemstack.getCount()) {
								j1 = itemstack.getCount();
							}

							// Capture before depletion: native empty stacks hide their item.
							net.minecraft.item.Item dropItem = itemstack.getItem();
							int dropMeta = itemstack.getItemDamage();
							NBTTagCompound dropTag = itemstack.getTagCompound();
							itemstack.shrink(j1);
							EntityItem entityitem = new EntityItem(world, x + f, y + f1, z + f2, new ItemStack(dropItem, j1, dropMeta));

							if(dropTag != null) {
								entityitem.getItem().setTagCompound(dropTag.copy());
							}

							float f3 = 0.05F;
							entityitem.motionX = (float) rand.nextGaussian() * f3;
							entityitem.motionY = (float) rand.nextGaussian() * f3 + 0.2F;
							entityitem.motionZ = (float) rand.nextGaussian() * f3;
							world.spawnEntity(entityitem);
						}
					}
				}

				world.updateComparatorOutputLevel(new net.minecraft.util.math.BlockPos(x, y, z), block);
			}
		}

		super.breakBlock(world, pos, state);
	}

	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState i, Random rand, int j) {
		return null;
	}

	@Override
	public int getSubCount() {
		return 4;
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {

		TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(!(te instanceof TileEntityMassStorage))
			return;

		TileEntityMassStorage storage = (TileEntityMassStorage) te;

		List<String> text = new ArrayList();
		String title = "Empty";
		boolean full = storage.type != null;

		if(full) {

			title = storage.type.getDisplayName();
			text.add(String.format(Locale.US, "%,d", storage.getStockpile()) + " / " + String.format(Locale.US, "%,d", storage.getCapacity()));

			double percent = (double) storage.getStockpile() / (double) storage.getCapacity();
			int charge = (int) Math.floor(percent * 10_000D);
			int color = ((int) (0xFF - 0xFF * percent)) << 16 | ((int)(0xFF * percent) << 8);

			text.add("&[" + color + "&]" + (charge / 100D) + "%");
		}

		ILookOverlay.printGeneric(event, title, full ? 0xffff00 : 0x00ffff, full ? 0x404000 : 0x004040, text);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {

		if(!stack.hasTagCompound()) return;

		ItemStack type = NullableInventoryStackNbt.read(stack.getTagCompound().getCompoundTag("slot1"));

		if(type != null) {
			list.add(TextFormatting.GOLD + type.getDisplayName());
			list.add(String.format(Locale.US, "%,d", stack.getTagCompound().getInteger("stack")) + " / " + String.format(Locale.US, "%,d", getCapacity(stack.getItemDamage())));
		}
	}

	@Override
	public boolean hasComparatorInputOverride(net.minecraft.block.state.IBlockState state) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(net.minecraft.block.state.IBlockState state, World world, net.minecraft.util.math.BlockPos blockPos) {
		int x = blockPos.getX();
		int y = blockPos.getY();
		int z = blockPos.getZ();
		return ((TileEntityMassStorage) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z))).redstone;
	}
}
