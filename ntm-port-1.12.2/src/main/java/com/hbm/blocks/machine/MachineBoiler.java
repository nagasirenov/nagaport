package com.hbm.blocks.machine;

import java.util.ArrayList;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.material.Mats;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import com.hbm.render.icon.IIcon;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class MachineBoiler extends Block {

	protected IIcon blockIcon;

	private final boolean isActive;

	@SideOnly(Side.CLIENT)
	private IIcon iconFront;
	@SideOnly(Side.CLIENT)
	private IIcon iconSide;

	public MachineBoiler(boolean blockState) {
		super(Material.IRON);
		isActive = blockState;
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {

		if(this == ModBlocks.machine_boiler_off) {
			this.iconFront = iconRegister.registerIcon(RefStrings.MODID + (this.isActive ? ":machine_boiler_front_lit" : ":machine_boiler_front"));
			this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":machine_boiler_base");
			this.iconSide = iconRegister.registerIcon(RefStrings.MODID + ":machine_boiler_side");
		}
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {

		if(side == 0 || side == 1)
			return this.blockIcon;

		return metadata == 0 && side == 3 ? this.iconFront : (side == metadata ? this.iconFront : this.iconSide);
	}


		@Override
	public void getDrops(net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> drops, net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, int fortune) {
		int meta = state.getBlock().getMetaFromState(state);
		net.minecraft.world.World w = world instanceof net.minecraft.world.World ? (net.minecraft.world.World) world : null;
		java.util.ArrayList<net.minecraft.item.ItemStack> ret = getDrops(w, pos.getX(), pos.getY(), pos.getZ(), meta, fortune);
		if(ret != null) drops.addAll(ret);
	}

public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
		ret.add(new ItemStack(ModItems.scraps, 3 + world.rand.nextInt(4), Mats.MAT_STEEL.id));
		return ret;
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		super.onBlockAdded(world, pos, state);
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		this.setDefaultDirection(world, x, y, z);
	}

	private void setDefaultDirection(World world, int x, int y, int z) {
		if(!world.isRemote) {
			net.minecraft.block.state.IBlockState state1 = com.hbm.world.WorldBlockState.read(world, x, y, z - 1);
			net.minecraft.block.state.IBlockState state2 = com.hbm.world.WorldBlockState.read(world, x, y, z + 1);
			net.minecraft.block.state.IBlockState state3 = com.hbm.world.WorldBlockState.read(world, x - 1, y, z);
			net.minecraft.block.state.IBlockState state4 = com.hbm.world.WorldBlockState.read(world, x + 1, y, z);

			byte b0 = 3;

			if(state1.isFullBlock() && !state2.isFullBlock()) {
				b0 = 3;
			}
			if(state2.isFullBlock() && !state1.isFullBlock()) {
				b0 = 2;
			}
			if(state3.isFullBlock() && !state4.isFullBlock()) {
				b0 = 5;
			}
			if(state4.isFullBlock() && !state3.isFullBlock()) {
				b0 = 4;
			}

			com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, b0, 2);
		}
	}

	@Override
	public void onBlockPlacedBy(World world, net.minecraft.util.math.BlockPos nativePlacementPos, net.minecraft.block.state.IBlockState nativePlacementState, EntityLivingBase player, ItemStack itemStack) {
		int x = nativePlacementPos.getX(), y = nativePlacementPos.getY(), z = nativePlacementPos.getZ();
		int i = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;

		if(i == 0) {
			com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 2, 2);
		}
		if(i == 1) {
			com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 5, 2);
		}
		if(i == 2) {
			com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 3, 2);
		}
		if(i == 3) {
			com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 4, 2);
		}
	}
}
