package com.hbm.blocks.machine;

import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.state.IBlockState;
import api.hbm.block.ICrucibleAcceptor;
import api.hbm.block.IToolable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMold;
import com.hbm.items.machine.ItemMold.Mold;
import com.hbm.items.machine.ItemScraps;
import com.hbm.tileentity.machine.TileEntityFoundryCastingBase;
import com.hbm.util.i18n.I18nUtil;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import com.hbm.lib.ForgeDirection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class FoundryCastingBase extends FoundryBlock implements ICrucibleAcceptor, IToolable, ILookOverlay {

	protected FoundryCastingBase() {
		super();
	}



	@Override
	public boolean canAcceptPartialPour(World world, int x, int y, int z, double dX, double dY, double dZ, ForgeDirection side, MaterialStack stack) {
		return ((ICrucibleAcceptor) world.getTileEntity(new BlockPos(x, y, z))).canAcceptPartialPour(world, x, y, z, dX, dY, dZ, side, stack);
	}

	@Override
	public MaterialStack pour(World world, int x, int y, int z, double dX, double dY, double dZ, ForgeDirection side, MaterialStack stack) {
		return ((ICrucibleAcceptor) world.getTileEntity(new BlockPos(x, y, z))).pour(world, x, y, z, dX, dY, dZ, side, stack);
	}

	@Override
	public boolean canAcceptPartialFlow(World world, int x, int y, int z, ForgeDirection side, MaterialStack stack) {
		return ((ICrucibleAcceptor) world.getTileEntity(new BlockPos(x, y, z))).canAcceptPartialFlow(world, x, y, z, side, stack);
	}

	@Override
	public MaterialStack flow(World world, int x, int y, int z, ForgeDirection side, MaterialStack stack) {
		return ((ICrucibleAcceptor) world.getTileEntity(new BlockPos(x, y, z))).flow(world, x, y, z, side, stack);
	}





	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		int x=pos.getX(), y=pos.getY(), z=pos.getZ();
		if(world.isRemote) {
			return true;
		}

		TileEntityFoundryCastingBase cast = (TileEntityFoundryCastingBase) world.getTileEntity(new BlockPos(x, y, z));

		//remove casted item
		if(cast.slots[1] != null) {
			if(!player.inventory.addItemStackToInventory(cast.slots[1].copy())) {
				EntityItem item = new EntityItem(world, x + 0.5, y + getBoundingBox(world.getBlockState(new BlockPos(x,y,z)),world,new BlockPos(x,y,z)).maxY, z + 0.5, cast.slots[1].copy());
				world.spawnEntity(item);
			} else {
				player.inventoryContainer.detectAndSendChanges();
			}

			cast.slots[1] = null;
			cast.markDirty();
			notifyChange(world, new BlockPos(x, y, z));
			return true;
		}

		//insert mold
		if(!player.getHeldItem(hand).isEmpty() && player.getHeldItem(hand).getItem() == ModItems.mold && cast.slots[0] == null) {
			Mold mold = ((ItemMold) player.getHeldItem(hand).getItem()).getMold(player.getHeldItem(hand));

			if(mold.size == cast.getMoldSize()) {
				cast.slots[0] = player.getHeldItem(hand).copy();
				cast.slots[0].setCount(1);
				player.getHeldItem(hand).shrink(1);
				world.playSound(null, x + 0.5, y + 0.5, z + 0.5, com.hbm.main.ModSoundEvents.ITEM_UPGRADE_PLUG, net.minecraft.util.SoundCategory.PLAYERS, 1.0F, 1.0F);
				cast.markDirty();
				notifyChange(world, new BlockPos(x, y, z));
				return true;
			}
		}

		if(!player.getHeldItem(hand).isEmpty() && player.getHeldItem(hand).getItem() instanceof ItemTool && ((ItemTool) player.getHeldItem(hand).getItem()).getToolClasses(player.getHeldItem(hand)).contains("shovel")) {
			if(cast.amount > 0) {
				ItemStack scrap = ItemScraps.create(new MaterialStack(cast.type, cast.amount));
				if(!player.inventory.addItemStackToInventory(scrap)) {
					EntityItem item = new EntityItem(world, x + 0.5, y + getBoundingBox(world.getBlockState(new BlockPos(x,y,z)),world,new BlockPos(x,y,z)).maxY, z + 0.5, scrap);
					world.spawnEntity(item);
				} else {
					player.inventoryContainer.detectAndSendChanges();
				}
				cast.amount = 0;
				cast.type = null;
				cast.markDirty();
				notifyChange(world, new BlockPos(x, y, z));
			}
			return true;
		}

		return false;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		int x=pos.getX(), y=pos.getY(), z=pos.getZ();

		TileEntityFoundryCastingBase cast = (TileEntityFoundryCastingBase) world.getTileEntity(new BlockPos(x, y, z));
		if(cast.amount > 0) {
			ItemStack scrap = ItemScraps.create(new MaterialStack(cast.type, cast.amount));
			EntityItem item = new EntityItem(world, x + 0.5, y + getBoundingBox(world.getBlockState(new BlockPos(x,y,z)),world,new BlockPos(x,y,z)).maxY, z + 0.5, scrap);
			world.spawnEntity(item);
			cast.amount = 0; //just for safety
		}

		for(ItemStack stack : cast.slots) {
			if(stack != null) {
				EntityItem drop = new EntityItem(world, x + 0.5, y + getBoundingBox(world.getBlockState(new BlockPos(x,y,z)),world,new BlockPos(x,y,z)).maxY, z + 0.5, stack.copy());
				world.spawnEntity(drop);
			}
		}

		super.breakBlock(world, pos, state);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
		int x=pos.getX(), y=pos.getY(), z=pos.getZ();
		super.randomDisplayTick(state, world, pos, rand);

		TileEntityFoundryCastingBase cast = (TileEntityFoundryCastingBase) world.getTileEntity(new BlockPos(x, y, z));

		if(cast.amount > 0 && cast.amount >= cast.getCapacity()) {
			world.spawnParticle(net.minecraft.util.EnumParticleTypes.SMOKE_NORMAL, x + 0.25 + rand.nextDouble() * 0.5, y + getBoundingBox(world.getBlockState(new BlockPos(x,y,z)),world,new BlockPos(x,y,z)).maxY, z + 0.25 + rand.nextDouble() * 0.5, 0.0, 0.0, 0.0);
		}
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, int side, float fX, float fY, float fZ, ToolType tool) {

		if(tool != ToolType.SCREWDRIVER)
			return false;

		TileEntityFoundryCastingBase cast = (TileEntityFoundryCastingBase) world.getTileEntity(new BlockPos(x, y, z));

		if(cast.slots[0] == null) return false;
		if(cast.amount > 0) return false;

		if(!player.inventory.addItemStackToInventory(cast.slots[0].copy())) {
			EntityItem item = new EntityItem(world, x + 0.5, y + getBoundingBox(world.getBlockState(new BlockPos(x,y,z)),world,new BlockPos(x,y,z)).maxY, z + 0.5, cast.slots[0].copy());
			world.spawnEntity(item);
		} else {
			player.inventoryContainer.detectAndSendChanges();
		}

		cast.markDirty();
		notifyChange(world, new BlockPos(x, y, z));

		cast.slots[0] = null;
		cast.markDirty();

		return true;
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {
		TileEntityFoundryCastingBase cast = (TileEntityFoundryCastingBase) world.getTileEntity(new BlockPos(x, y, z));
		List<String> text = new ArrayList();

		if(cast.slots[0] == null) {
			text.add(TextFormatting.RED + I18nUtil.resolveKey("foundry.noCast"));
		} else if(cast.slots[0].getItem() == ModItems.mold){
			Mold mold = ((ItemMold) cast.slots[0].getItem()).getMold(cast.slots[0]);
			text.add(TextFormatting.BLUE + mold.getTitle());
		}

		if(cast.type != null && cast.amount > 0) {
			text.add(TextFormatting.YELLOW + cast.type.names[0] + ": " + cast.amount + " / " + cast.getCapacity());
		}

		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(this.getTranslationKey() + ".name"), 0xFF4000, 0x401000, text);
	}
}
