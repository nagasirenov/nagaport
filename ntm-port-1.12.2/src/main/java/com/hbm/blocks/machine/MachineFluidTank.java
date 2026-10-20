package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.IPersistentInfoProvider;
import com.hbm.entity.projectile.EntityBombletZeta;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Flammable;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IPersistentNBT;
import com.hbm.tileentity.IRepairable;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.storage.TileEntityMachineFluidTank;

import api.hbm.block.IToolable;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import com.hbm.lib.ForgeDirection;

public class MachineFluidTank extends BlockDummyable implements IPersistentInfoProvider, IToolable, ILookOverlay {

	public MachineFluidTank(Material mat) {
		super(mat);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if(meta >= 12) return new TileEntityMachineFluidTank();
		if(meta >= 6) return new TileEntityProxyCombo().fluid();
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {2, 0, 1, 1, 2, 2};
	}

	@Override
	public int getOffset() {
		return 1;
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		
		if(world.isRemote) {
			return true;
		} 
		else if(!player.isSneaking()) {
			int[] pos = this.findCore(world, x, y, z);

			if(pos == null)
				return false;
			
			TileEntityMachineFluidTank tank = (TileEntityMachineFluidTank) world.getTileEntity(new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]));
			
			if(tank != null) {
				if(tank.hasExploded) return false;
				FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos[0], pos[1], pos[2]);
			}
			return true;
		}
		else if(player.isSneaking()){
			int[] pos = this.findCore(world, x, y, z);

			if(pos == null)
				return false;
			
			TileEntityMachineFluidTank tank = (TileEntityMachineFluidTank) world.getTileEntity(new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]));
			
			if(tank != null) {
				if(tank.hasExploded) return false;
			if(com.hbm.util.NativeHeldItem.getMainHandNullable(player) != null && com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() instanceof IItemFluidIdentifier) {
				FluidType type = ((IItemFluidIdentifier) com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem()).getType(world, pos[0], pos[1], pos[2], com.hbm.util.NativeHeldItem.getMainHandNullable(player));

				tank.tank.setTankType(type);
				tank.markDirty();
				player.sendMessage(new TextComponentString("Changed type to ").setStyle(new Style().setColor(TextFormatting.YELLOW)).appendSibling(new TextComponentTranslation(type.getConditionalName())).appendSibling(new TextComponentString("!")));
				}
			} 
			return true;
		}else {
			return true;
		}
		
	}

	@Override
	protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);

		this.makeExtra(world, x - dir.offsetX + 1, y, z - dir.offsetZ + 1);
		this.makeExtra(world, x - dir.offsetX + 1, y, z - dir.offsetZ - 1);
		this.makeExtra(world, x - dir.offsetX - 1, y, z - dir.offsetZ + 1);
		this.makeExtra(world, x - dir.offsetX - 1, y, z - dir.offsetZ - 1);
	}
	
	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		return IPersistentNBT.getDrops(world, x, y, z, this);
	}

	@Override
	public void addInformation(ItemStack stack, NBTTagCompound persistentTag, EntityPlayer player, List list, boolean ext) {
		FluidTank tank = new FluidTank(Fluids.NONE, 0);
		tank.readFromNBT(persistentTag, "tank");
		list.add(TextFormatting.YELLOW + "" + tank.getFill() + "/" + tank.getMaxFill() + "mB " + tank.getTankType().getLocalizedName());
	}

	@Override
	public boolean canDropFromExplosion(Explosion explosion) {
		return false;
	}

	@Override
	public void onBlockExploded(World world, int x, int y, int z, Explosion explosion) {

		int[] pos = this.findCore(world, x, y, z);
		if(pos == null) return;
		TileEntity core = world.getTileEntity(new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]));
		if(!(core instanceof TileEntityMachineFluidTank)) return;
		
		TileEntityMachineFluidTank tank = (TileEntityMachineFluidTank) core;
		if(tank.lastExplosion == explosion) return;
		tank.lastExplosion = explosion;
		
		if(!tank.hasExploded) {
			tank.explode();
			
			if(explosion.exploder != null && explosion.exploder instanceof EntityBombletZeta) {
				if(tank.tank.getTankType().getTrait(FT_Flammable.class) == null) return;
				
				List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class,
						new AxisAlignedBB(x + 0.5, y + 0.5, z + 0.5, x + 0.5, y + 0.5, z + 0.5).grow(100, 100, 100));
				
				for(EntityPlayer p : players) com.hbm.advancement.NtmAdvancements.award(p, MainRegistry.achInferno);
			}
		} else {
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = pos[0]; int nativePlacementY = pos[1]; int nativePlacementZ = pos[2]; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
		}
	}

	@Override
	public boolean hasComparatorInputOverride() {
		return true;
	}

	@Override
	public int getComparatorInputOverride(World world, int x, int y, int z, int side) {

		int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );
		
		if(meta >= 6) {
			int[] pos = this.findCore(world, x, y, z);
			if(pos == null) return 0;
			TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]));
	
			if(!(te instanceof TileEntityMachineFluidTank))
				return 0;
	
			TileEntityMachineFluidTank tank = (TileEntityMachineFluidTank) te;
			return tank.getComparatorPower();
		}
		
		return 0;
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, int side, float fX, float fY, float fZ, ToolType tool) {
		
		if(tool != ToolType.TORCH) return false;
		return IRepairable.tryRepairMultiblock(world, x, y, z, this, player);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void printHook(Pre event, World world, int x, int y, int z) {
		IRepairable.addGenericOverlay(event, world, x, y, z, this);
	}
}
