package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ICustomBlockHighlight;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.items.machine.ItemScraps;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityCrucible;

import api.hbm.block.ICrucibleAcceptor;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import com.hbm.lib.ForgeDirection;

public class MachineCrucible extends BlockDummyable implements ICrucibleAcceptor, ITooltipProvider {

	public MachineCrucible() {
		super(Material.ROCK);

		this.bounding.add(new AxisAlignedBB(-1.5D, 0D, -1.5D, 1.5D, 0.5D, 1.5D));
		this.bounding.add(new AxisAlignedBB(-1.25D, 0.5D, -1.25D, 1.25D, 1.5D, -1D));
		this.bounding.add(new AxisAlignedBB(-1.25D, 0.5D, -1.25D, -1D, 1.5D, 1.25D));
		this.bounding.add(new AxisAlignedBB(-1.25D, 0.5D, 1D, 1.25D, 1.5D, 1.25D));
		this.bounding.add(new AxisAlignedBB(1D, 0.5D, -1.25D, 1.25D, 1.5D, 1.25D));
		this.maxY = 0.999D; //item bounce prevention
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		
		if(meta >= 12) return new TileEntityCrucible();
		return new TileEntityProxyCombo().inventory();
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		
		if(world.isRemote) {
			return true;
		} else if(!player.isSneaking()) {
			int[] pos = this.findCore(world, x, y, z);

			if(pos == null)
				return false;
			if(com.hbm.util.NativeHeldItem.getMainHandNullable(player) != null && com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() instanceof ItemTool && ((ItemTool) com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem()).getToolClasses(com.hbm.util.NativeHeldItem.getMainHandNullable(player)).contains("shovel")) {
				TileEntityCrucible crucible = (TileEntityCrucible) world.getTileEntity(new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]));
				List<MaterialStack> stacks = new ArrayList();
				stacks.addAll(crucible.recipeStack);
				stacks.addAll(crucible.wasteStack);
				
				for(MaterialStack stack : stacks) {
					ItemStack scrap = ItemScraps.create(new MaterialStack(stack.material, stack.amount));
					if(!player.inventory.addItemStackToInventory(scrap)) {
						EntityItem item = new EntityItem(world, x + hitX, y + hitY, z + hitZ, scrap);
						world.spawnEntity(item);
					}
				}
				
				player.inventoryContainer.detectAndSendChanges();
				crucible.recipeStack.clear();
				crucible.wasteStack.clear();
				crucible.markDirty();
				
			} else {
				FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos[0], pos[1], pos[2]);
			}
			return true;
		} else {
			return true;
		}
	}

	@Override
	public int[] getDimensions() {
		return new int[] {1, 0, 1, 1, 1, 1};
	}

	@Override
	public int getOffset() {
		return 1;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block b, int i) {
		
		TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
		
		if(te instanceof TileEntityCrucible) {
			TileEntityCrucible crucible = (TileEntityCrucible) te;
			
			List<MaterialStack> stacks = new ArrayList();
			stacks.addAll(crucible.recipeStack);
			stacks.addAll(crucible.wasteStack);
			
			for(MaterialStack stack : stacks) {
				ItemStack scrap = ItemScraps.create(new MaterialStack(stack.material, stack.amount));
				EntityItem item = new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, scrap);
				world.spawnEntity(item);
			}
			
			crucible.recipeStack.clear();
			crucible.wasteStack.clear();
		}
		
		super.breakBlock(world, x, y, z, b, i);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldDrawHighlight(World world, int x, int y, int z) {
		return true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void drawHighlight(DrawBlockHighlightEvent event, World world, int x, int y, int z) {
		
		int[] pos = this.findCore(world, x, y, z);
		if(pos == null) return;
		TileEntity tile = world.getTileEntity(new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]));
		if(!(tile instanceof TileEntityCrucible)) return;
		TileEntityCrucible crucible = (TileEntityCrucible) tile;
		
		x = crucible.getPos().getX();
		y = crucible.getPos().getY();
		z = crucible.getPos().getZ();
		
		EntityPlayer player = event.getPlayer();
		float interp = event.getPartialTicks();
		double dX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) interp;
		double dY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) interp;
		double dZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)interp;
		float exp = 0.002F;

		ICustomBlockHighlight.setup();
		/*event.context.drawOutlinedBoundingBox(AxisAlignedBB.getBoundingBox(x - 1, y, z - 1, x + 2, y + 0.5, z + 2).expand(exp, exp, exp).getOffsetBoundingBox(-dX, -dY, -dZ), -1);
		event.context.drawOutlinedBoundingBox(AxisAlignedBB.getBoundingBox(x - 0.75, y + 0.5, z - 0.75, x + 1.75, y + 1.5, z + 1.75).expand(exp, exp, exp).getOffsetBoundingBox(-dX, -dY, -dZ), -1);
		event.context.drawOutlinedBoundingBox(AxisAlignedBB.getBoundingBox(x - 0.5, y + 0.75, z - 0.5, x + 1.5, y + 1.5, z + 1.5).expand(exp, exp, exp).getOffsetBoundingBox(-dX, -dY, -dZ), -1);*/
		for(AxisAlignedBB aabb : this.bounding) event.getContext().drawOutlinedBoundingBox(aabb.expand(exp, exp, exp).getOffsetBoundingBox(x - dX + 0.5, y - dY, z - dZ + 0.5), -1);
		ICustomBlockHighlight.cleanup();
	}

	@Override
	public boolean canAcceptPartialPour(World world, int x, int y, int z, double dX, double dY, double dZ, ForgeDirection side, MaterialStack stack) {
		
		int[] pos = this.findCore(world, x, y, z);
		if(pos == null) return false;
		TileEntity tile = world.getTileEntity(new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]));
		if(!(tile instanceof TileEntityCrucible)) return false;
		TileEntityCrucible crucible = (TileEntityCrucible) tile;
		
		return crucible.canAcceptPartialPour(world, x, y, z, dX, dY, dZ, side, stack);
	}

	@Override
	public MaterialStack pour(World world, int x, int y, int z, double dX, double dY, double dZ, ForgeDirection side, MaterialStack stack) {
		
		int[] pos = this.findCore(world, x, y, z);
		if(pos == null) return stack;
		TileEntity tile = world.getTileEntity(new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]));
		if(!(tile instanceof TileEntityCrucible)) return stack;
		TileEntityCrucible crucible = (TileEntityCrucible) tile;
		
		return crucible.pour(world, x, y, z, dX, dY, dZ, side, stack);
	}

	@Override public boolean canAcceptPartialFlow(World world, int x, int y, int z, ForgeDirection side, MaterialStack stack) { return false; }
	@Override public MaterialStack flow(World world, int x, int y, int z, ForgeDirection side, MaterialStack stack) { return null; }

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		this.addStandardInfo(stack, player, list, ext);
	}
}
